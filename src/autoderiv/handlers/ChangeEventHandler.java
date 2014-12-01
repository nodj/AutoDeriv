package autoderiv.handlers;
import static autoderiv.Debug.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import autoderiv.Filter;

/**@brief This class is the main IResourceChangeListener of the plug-in and must 
 * react, update properties, etc.
 * @author johan duparc (johan.duparc@gmail.com) 
 * @todo fast exit if the project is not managed.
 **/
public class ChangeEventHandler implements IResourceChangeListener{

	public static final String	CONF_FILE_NAME	= ".derived";
	HashMap<IProject, Filter> projectsFilter = new HashMap<IProject, Filter>();

	public class VisitData{
		boolean confAdded = false; 
		boolean confDeleted = false; 
		boolean confUpdated = false; 
		IResource confFile = null;
		ArrayList<IResource> added = new ArrayList<IResource>();
//		ArrayList<IResource> updated (osef) = new ArrayList<IResource>();
//		ArrayList<IResource> deleted (osef) = new ArrayList<IResource>();
	}

	public class MyDeltaVisitor implements IResourceDeltaVisitor{
		VisitData v; ///< the structure that contains the result of the visitation

		MyDeltaVisitor(VisitData v){ this.v = v; }


		public boolean confFileEventHandler(VisitData v, IResourceDelta delta){
			v.confFile = delta.getResource();

			switch (delta.getKind()) {
			case IResourceDelta.NO_CHANGE:
				return false;
			case IResourceDelta.ADDED_PHANTOM:
			case IResourceDelta.ADDED:
				info("ChangeEventHandler.MyDeltaVisitor.visit() EXCELENT ! the project is now conf");
				v.confAdded = true; 
				break;
			case IResourceDelta.REMOVED_PHANTOM:
			case IResourceDelta.REMOVED:
				info("ChangeEventHandler.MyDeltaVisitor.visit() No longer configured as AutoDeriv");
				v.confDeleted = true; 
				break;
			case IResourceDelta.CHANGED:
				info("ChangeEventHandler.MyDeltaVisitor.confFileEventHandler() conf edited");
				v.confUpdated = true;
				break;
			default:
				warn("ChangeEventHandler.MyDeltaVisitor.confFileEventHandler(): CASE NOT HANDLED");
			}
			return true;
		}

		public boolean notConfFileEventHandler(VisitData v, IResourceDelta delta){
			IResource res = delta.getResource();
			boolean isProject = (res.getType()==IResource.PROJECT);
			boolean isWorkspace = (res.getType()==IResource.ROOT);

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				info("Resource " + res.getFullPath()+" was added.");
				v.added.add(res);
				return true;

			case IResourceDelta.REMOVED:
				info("Resource "+res.getFullPath()+" was removed.");
				return isProject || isWorkspace; // so that we can see if the

			case IResourceDelta.CHANGED:
				info("Resource "+res.getFullPath()+" was updated.");
				return true; // as we may encounter some addition later

			case IResourceDelta.ADDED_PHANTOM:
			case IResourceDelta.REMOVED_PHANTOM:
				break;

			default:
				warn("ChangeEventHandler.MyDeltaVisitor.notConfFileEventHandler() case not implemented");
			}
			return true; // should not happen
		}


		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource res = delta.getResource();
			IProject proj = res.getProject();
			// remove. DBG only
			String name = res.getName();

			boolean isFile = (res.getType()==IResource.FILE);
			boolean isconfile = isFile && name.equals(CONF_FILE_NAME) && (res.getParent() == proj);
			// handle the resource
			if(isconfile)
				return confFileEventHandler(v, delta);
			return notConfFileEventHandler(v, delta);
		}
	} // class MyDeltaVisitor



	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		info("ChangeEventHandler.resourceChanged() : " + event.toString());

		switch(event.getType()){
		case IResourceChangeEvent.POST_CHANGE:
			info("POST_CHANGE"); 
			break;
		case IResourceChangeEvent.POST_BUILD:
			info("POST_BUILD"); 
			break;
		case IResourceChangeEvent.PRE_BUILD:
			info("PRE_BUILD"); 
			break;
		case IResourceChangeEvent.PRE_CLOSE:
			info("PRE_CLOSE"); 
			break;
		case IResourceChangeEvent.PRE_DELETE:
			// when a project is delete
			info("PRE_DELETE"); 
			return;
		case IResourceChangeEvent.PRE_REFRESH:
			info("PRE_REFRESH"); 
			break;
		default:
			info("default..."); 
			break;
		}

		HashMap<IProject, VisitData> perProjectVisitData = new HashMap<IProject, ChangeEventHandler.VisitData>();

		// loop in order to work on a per-projects basis
		IResourceDelta delta = event.getDelta();
		for( IResourceDelta ac : delta.getAffectedChildren()){
			IResource acRes = ac.getResource();
			IProject acProj = acRes.getProject();

			// should the visit happen in the WorkspaceJob thread ? Deferred ?
			VisitData v = new VisitData();
			perProjectVisitData.put(acProj, v);
			try {
				event.getDelta().accept(new MyDeltaVisitor(v));
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}


		// The delta visitor has now done its job : listing work to do.
		// Now let apply the change in a compact way (only one WorkspaceJob if possible)
//		IProgressMonitor progress = new NullProgressMonitor(); // todo : better progress ?

		WorkspaceJob wj = new WorkspaceJob("WorkspaceJob name ? see SmapleHandler2") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor progress) throws CoreException {
				for( Entry<IProject, VisitData> a : perProjectVisitData.entrySet()){
					VisitData v = a.getValue();
					IProject proj = a.getKey();
					Filter f = projectsFilter.get(proj);

					// HANDLE CONF EDITION
					if(v.confAdded){
						// filter the whole project with the new conf
						if (f != null) {
							projectsFilter.remove(proj);
							//TODO restore default state (all at 'not derived') before ?
						}
						f = new Filter(proj, v.confFile);
						projectsFilter.put(proj, f);
						f.filterProject(progress);
					}else if(v.confDeleted){
						if (f != null) {
							projectsFilter.remove(proj);
							// TODO restore default project state.
						}
					}else if(v.confUpdated){
						f = projectsFilter.get(proj);
						if(f==null){
							// act like if the conf file is just added
							f = new Filter(proj, v.confFile);
							projectsFilter.put(proj, f);
						}
						f.updateConf();
						f.filterProject(progress);
					}

					// HANDLE DATA EDITION

					// this may not be a managed project
					if(f==null) continue;

					f.filterResources(v.added, progress);
				}
				return new Status(Status.OK, "AutoDeriv", "IResourceChangeEvent managed");
			}
		};
		wj.schedule();

	}



	/**@brief manage the initial state. But don't waste time here. */
	public void startup() {
		/* For each project, check if it contains a conf file. Parse it, but 
		 * don't update files as it is a pure waste of time. We consider that it
		 * is already in a correct state.
		 */

		info("ChangeEventHandler.startup()");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		for(IProject proj : workspace.getRoot().getProjects()){
			IResource conf = proj.findMember(CONF_FILE_NAME);
			info("ChangeEventHandler.startup() on project ["+proj.getName()+"]");
			if(conf == null) continue;
			info("ChangeEventHandler.startup() project configured with AutoDeriv");
			Filter f = projectsFilter.get(proj);
			if(f!=null){
				projectsFilter.remove(proj); // dafuck?
				warn("ChangeEventHandler.startup() not expected at startup....");
			}
			f = new Filter(proj, conf);
			projectsFilter.put(proj, f);
			f.updateConf();
		}
	}

}
