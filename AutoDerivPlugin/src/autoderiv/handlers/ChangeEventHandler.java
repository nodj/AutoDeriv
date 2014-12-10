package autoderiv.handlers;
import static autoderiv.Debug.*;
import static autoderiv.handlers.FilterManager.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import autoderiv.Activator;
import autoderiv.Filter;
import autoderiv.Tools;

/**@brief This class is the main IResourceChangeListener of the plug-in and must
 * react, update properties, etc.
 * @author johan duparc (johan.duparc@gmail.com)
 * @todo fast exit if the project is not managed.
 * @todo active polling on master file ?
 **/
public class ChangeEventHandler implements IResourceChangeListener{

	private static long previousMasterConfLastModified = 0;
	private static File masterConfFile;
	private static IWorkspace workspace = ResourcesPlugin.getWorkspace();

	/**This is the main listener for this plugin. Any change on the workspace
	 * will call this method. We have to be prompt in here, else the UI will
	 * became less responsive. */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {

		// when a project is delete, quick exit as we don't care
		if(event.getType()==IResourceChangeEvent.PRE_DELETE) return;

		info("=====  ChangeEventHandler.resourceChanged() : " + event.toString() +"  =====");

//		switch(event.getType()){
//		case IResourceChangeEvent.POST_CHANGE: info("POST_CHANGE"); break;
//		case IResourceChangeEvent.POST_BUILD: info("POST_BUILD"); break;
//		case IResourceChangeEvent.PRE_BUILD: info("PRE_BUILD"); break;
//		case IResourceChangeEvent.PRE_CLOSE: info("PRE_CLOSE"); break;
//		case IResourceChangeEvent.PRE_DELETE:
//			// when a project is delete
//			info("PRE_DELETE");
//			return;
//		case IResourceChangeEvent.PRE_REFRESH: info("PRE_REFRESH"); break;
//		default: info("default..."); break;
//		}


		// The delta visitor has now done its job : listing work to do.
		// Now let apply the change in a compact way (only one WorkspaceJob if possible)
		WorkspaceJob wj = new WorkspaceJob(Activator.PLUGIN_ID + " - On Change Event Update Job") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor progress) throws CoreException {
				final HashMap<IProject, VisitData> perProjectVisitData = new HashMap<IProject, VisitData>();


				// loop in order to work on a per-projects basis
				IResourceDelta delta = event.getDelta();
				for (IResourceDelta ac : delta.getAffectedChildren()) {
					// todo should the visit happen in the WorkspaceJob thread ? Deferred ?
					VisitData v = new VisitData();
					perProjectVisitData.put(ac.getResource().getProject(), v);
					try {
						event.getDelta().accept(new MyDeltaVisitor(v));
					} catch (CoreException e1) {
						e1.printStackTrace();
					}
				}

				// also, check for master file edition
				final VisitData masterVisitData = handleMasterFile();

				// handle MCF deletion
				if(masterVisitData.confDeleted){
					// delete project not handled by AutoDeriv anymore
					for(IProject project : workspace.getRoot().getProjects()){
						Filter filter = getByProj(project);

						// assert that the project was correctly handled
						if(filter == null){
							warn("unexpected case. project "+project.getName()+" was not handled, but a master file was here...");
							continue;
						}

						// delete project without local conf
						if(!filter.hasLocalConf())
							deleteFilter(project);
					}

					// for all managed projects, clear master rules (parsing null cause a clear)
					Filter.setMasterConfFile(null);
					Filter.parseMasterRules(projectsFilter.values());
				}


				// handle project creation
				ArrayList<Filter> addedProjecsFilter = new ArrayList<Filter>();
				for (Entry<IProject, VisitData> a : perProjectVisitData.entrySet()) {
					VisitData v = a.getValue();
					IProject proj = a.getKey();
					progressSub(progress,"Handle Creation of project "+proj.getName());

					if(v.projAdded){
						// handle master file
						if(Filter.hasMasterConf())
							addedProjecsFilter.add(getOrCreateFilter(proj));

						// handle local file
						v.confAdded = Filter.hasLocalConf(proj);
					}
				}

				boolean masterUpdate = masterVisitData.confAdded || masterVisitData.confUpdated;
				if(masterUpdate){
					// update projects Filters
					filterForAll();
					progressSub(progress,"Parse master conf rules");
					Filter.parseMasterRules(projectsFilter.values());
				}else{
					// even if master file hasn't changed, update for new projects
					if(!addedProjecsFilter.isEmpty())
						Filter.parseMasterRules(addedProjecsFilter);
				}

				// handle per project VisitData
				for (Entry<IProject, VisitData> a : perProjectVisitData.entrySet()) {
					VisitData v = a.getValue();
					IProject proj = a.getKey();
					progressSub(progress,"Apply updates for project "+proj.getName());
					Filter f = null;

					// handle local configuration update
					if(v.confAdded || v.confUpdated){
						// filter the whole project with the new conf
						f = getOrCreateFilter(proj);
						f.reparseLocalConf();
						if(!masterUpdate){
							// no need to update if the master conf is updated.
							// These projects will be updated after
							f.filterProject(progress);
						}
					}

					else if(v.confDeleted){
						if(Filter.hasMasterConf()){
							deleteFilter(proj);
						}
					}

					else{
						// this may not be a managed project
						f = getByProj(proj);
						if(f==null) continue;
						f.filterResources(v.added, progress);
					}
				}

				if(masterUpdate){
					// apply updates
					progressSub(progress, "Apply master conf update");
					filterWorkspace(progress);
				}else{
					// even if master didn't changed, filter new projects
					for(Filter f : addedProjecsFilter)
						f.filterProject(progress);
				}

				// handle decoration
				// Decorate using current UI thread
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IDecoratorManager idm = PlatformUI.getWorkbench().getDecoratorManager();
						idm.update("autoderiv.handlers.Decorator");
					}
				});

				return new Status(Status.OK, "AutoDeriv", "IResourceChangeEvent managed");
			}
		};
		wj.schedule();
	}


	private void progressSub(IProgressMonitor progress, String string)
	{ progress.subTask(Activator.PLUGIN_ID + " - " +string); }


	/**Handle the master conf file.
	 * This MCF is not visible from eclipse. It is not subject to ChangeEvents
	 * or other nice Eclipse things. We have to handle the state evolution all
	 * by ourselves. Not nice...
	 * @return the VisitData that contains the main information from this state
	 * evolution */
	private VisitData handleMasterFile() {
		VisitData v = new VisitData();
		// get master conf
		masterConfFile = null;
		IPath masterConfFilePath = workspace.getRoot().getLocation();
		masterConfFilePath = masterConfFilePath.append(Filter.CONF_FILE_NAME);
		masterConfFile = masterConfFilePath.toFile();

		boolean masterConfFileExists = (masterConfFile!=null && masterConfFile.exists());

		boolean hadMasterConfFile = Filter.hasMasterConf();
		if(masterConfFileExists){
			long masterConfLastModified = masterConfFile.lastModified();
			Filter.setMasterConfFile(masterConfFile);
			if(hadMasterConfFile){
				if(masterConfLastModified > previousMasterConfLastModified){
					v.confUpdated = true;
					warn("Master Conf File UPDATED !");
				}// else, no evolution.
			}else{
				v.confAdded = true;
				warn("Master Conf File ADDED !");
			}
			previousMasterConfLastModified = masterConfLastModified;


		}else {
			if(hadMasterConfFile){
				v.confDeleted = true;
				warn("master conf file deleted !");
				previousMasterConfLastModified = 0;
			}
			//		else{ warn("no master conf file, but it's not a big news..."); }
		}
		return v;
	}



	/**@brief manage the initial state. But don't waste time here. */
	public void startup() {
		info("=====  ChangeEventHandler.startup()  =====");
		WorkspaceJob wj = new WorkspaceJob(Activator.PLUGIN_ID + " - Startup Update Job") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor progress) throws CoreException {
				deferedStartup(progress);
				return new Status(Status.OK, "AutoDeriv", "Startup managed");
			}
		};
		wj.schedule();
	}

	/**Called at startup. In fact, called within a "WorkspaceJob"
	 * @param progress given IProgressMonitor that we update for UX reasons */
	private void deferedStartup(IProgressMonitor progress) {
		info("ChangeEventHandler.deferedStartup()");

		// avoid multiple startup() call mess
		if(!projectsFilter.isEmpty()) return;

		// chrono
		double startupStart = Tools.getmsd();

		// Check masterfile
		dbg("ChangeEventHandler.deferedStartup() Check masterfile");
		VisitData masterVisit = handleMasterFile();
		boolean masterUpdated = masterVisit.confAdded || masterVisit.confUpdated;

		/* For each project, check if it contains a conf file. Parse it, but
		 * don't update files as it is a pure waste of time.
		 * Todo possibility to disable startup overall update
		 */
		for(IProject proj : workspace.getRoot().getProjects()){
			dbg("ChangeEventHandler.deferedStartup() on project ["+proj.getName()+"]");
			if(Filter.hasLocalConf(proj)){
				info("ChangeEventHandler.deferedStartup() project configured with AutoDeriv");
				Filter f = getOrCreateFilter(proj); // expected: Create only
				f.reparseLocalConf();
			}
		}

		if(masterUpdated){
			// assert that all projects have a Filter. Master will affect all projects.
			filterForAll();

			// update projects Filters
			Filter.parseMasterRules(projectsFilter.values());
		}


// todo if(userAcceptsAStartupCheck)
		// I hope this is not too long...
		filterWorkspace(progress);

		double startupEnd = Tools.getmsd();
		info("ChangeEventHandler.deferedStartup() took (ms) " + (startupEnd - startupStart));
	}

}
