package autoderiv.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import autoderiv.Filter;

/**@brief This class is the main IResourceChangeListener of the plug-in and must 
 * react, update properties, etc.
 * @author johan duparc (johan.duparc@gmail.com) */
public class SampleHandler2 implements IResourceChangeListener{

	public static final String	CONF_FILE_NAME	= ".derived";
	HashMap<IProject, Filter> projectsFilter = new HashMap<IProject, Filter>();

	public class VisitData{
		boolean confAdded = false; 
		boolean confDeleted = false; 
		boolean confUpdated = false; 
		IResource confFile = null;
		ArrayList<IResource> added = new ArrayList<IResource>();
		ArrayList<IResource> updated = new ArrayList<IResource>();
//		ArrayList<IResource> deleted (osef) = new ArrayList<IResource>();
	}

	public class MyDeltaVisitor implements IResourceDeltaVisitor{
		VisitData v; ///< the structure that contains the result of the visitation

		MyDeltaVisitor(VisitData v){ this.v = v; }


		public boolean confFileEventHandler(VisitData v, IResourceDelta delta){
			IResource res = delta.getResource();
			IProject proj = res.getProject();
			Filter f;

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				System.out.println("SampleHandler2.MyDeltaVisitor.visit() EXCELENT ! the project is now conf");
				v.confAdded = true; 
				v.confFile = res;
				break;
			case IResourceDelta.REMOVED:
				System.out.println("SampleHandler2.MyDeltaVisitor.visit() No longer configured as AutoDeriv");
				projectsFilter.remove(proj);
				// TODO restore default state of all project files. (which is ?)
				break;
			case IResourceDelta.CHANGED:
				f = projectsFilter.get(proj);
				if(f!=null)
					f.updateConf();
				break;
				//FIXME other cases ???
			}
			return true;
		}

		public boolean notConfFileEventHandler(VisitData v, IResourceDelta delta){
			IResource res = delta.getResource();
			IProject proj = res.getProject();
			String name = res.getName();
			boolean isProject = (res == proj);
			boolean isWorkspace = (name.length()==0);

			if(isProject)
				System.out.println("SampleHandler2.MyDeltaVisitor.visit() ------------------------------------");

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				System.out.println("Resource " + res.getFullPath()+" was added.");
				//TODO check if this pass the filter ! 
				if(isProject) 	return true;	// project
				if(isWorkspace) return true;	// workspace
				return false; // as the conf file must be in the project folder

			case IResourceDelta.REMOVED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" was removed.");
				if(isProject) 	return true;	// project
				if(isWorkspace) return true;	// workspace
				return false; // as the conf file must be in the project folder

			case IResourceDelta.CHANGED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" has changed.");
				Filter f = projectsFilter.get(proj);
				if(f!=null){
					WorkspaceJob wj = new WorkspaceJob("WorkspaceJob name ? see SmapleHandler2") {
						@Override
						public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
							f.updateDerivedProperty(res, arg0);
							return new Status(Status.OK, "AutoDeriv", "resource filtered");
						}
					};
					wj.schedule();
				}
				return true;
				//FIXME other cases ???
			}
			return true;
		}


		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource res = delta.getResource();
			IProject proj = res.getProject();
			// remove. DBG only
			String name = res.getName();
			System.out.println("SampleHandler2.MyDeltaVisitor.visit() res name:" + res.getName());

			boolean isconfile = name.equals(CONF_FILE_NAME) && (res.getParent() == proj);
			// handle the resource
			if(isconfile)
				return confFileEventHandler(v, delta);
			return notConfFileEventHandler(v, delta);
		}
	} // class MyDeltaVisitor



	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		System.out.println("SampleHandler2.resourceChanged() : " + event.toString());

		switch(event.getType()){
		case IResourceChangeEvent.POST_CHANGE:
			System.out.println("POST_CHANGE"); 
			break;
		case IResourceChangeEvent.POST_BUILD:
			System.out.println("POST_BUILD"); 
			break;
		case IResourceChangeEvent.PRE_BUILD:
			System.out.println("PRE_BUILD"); 
			break;
		case IResourceChangeEvent.PRE_CLOSE:
			System.out.println("PRE_CLOSE"); 
			break;
		case IResourceChangeEvent.PRE_DELETE:
			System.out.println("PRE_DELETE"); 
			break;
		case IResourceChangeEvent.PRE_REFRESH:
			System.out.println("PRE_REFRESH"); 
			break;
		default:
			System.out.println("default..."); 
			break;
		}

		HashMap<IProject, VisitData> perProjectVisitData = new HashMap<IProject, SampleHandler2.VisitData>();

		// loop in order to work on a per-projects basis
		IResourceDelta delta = event.getDelta();
		for( IResourceDelta ac : delta.getAffectedChildren()){
			IResource acRes = ac.getResource();
			IProject acProj = acRes.getProject();
			System.out.println("SampleHandler2.resourceChanged() proj name: " + acProj.getName() );

			// should the visit happen in the workspacejob thread ? defered ?
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
		IProgressMonitor progress = new NullProgressMonitor(); // todo : better progress ?
		
		WorkspaceJob wj = new WorkspaceJob("WorkspaceJob name ? see SmapleHandler2") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
				for( Entry<IProject, VisitData> a : perProjectVisitData.entrySet()){
					VisitData v = a.getValue();
					IProject proj = a.getKey();
					Filter f = projectsFilter.get(proj);
					
					if(v.confAdded){
						// filter the whole project with the new conf
						if (f != null) {
							projectsFilter.remove(proj);
							//TODO restore default state (all at 'not derived') ?
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
						// TODO let the project's Filter decide

					}
				}
				return new Status(Status.OK, "AutoDeriv", "IResourceChangeEvent managed");
			}
		};
		wj.schedule();

	}
}
