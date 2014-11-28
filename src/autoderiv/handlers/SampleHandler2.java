package autoderiv.handlers;

import java.util.HashMap;
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
import org.eclipse.core.runtime.Status;
import autoderiv.Filter;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */ 
public class SampleHandler2 implements IResourceChangeListener{

	public static final String	CONF_FILE_NAME	= ".derived";
	HashMap<IProject, Filter> projectsFilter = new HashMap<IProject, Filter>();


	public class MyDeltaVisitor implements IResourceDeltaVisitor{

		@SuppressWarnings("restriction")
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			System.out.println("SampleHandler2.MyDeltaVisitor.visit()");
			IResource res = delta.getResource();
			System.out.println("SampleHandler2.MyDeltaVisitor.visit() res name:" + res.getName());
			IProject proj = res.getProject();
			String name = res.getName();
			boolean isProjResource = (res == proj);
			if(isProjResource){
				System.out.println("SampleHandler2.MyDeltaVisitor.visit() ------------------------------------");
			}

			boolean isconfile = name.equals(CONF_FILE_NAME) && (res.getParent() == proj);
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" was added.");

				//TODO check if this pass the filter ! 

				if(isconfile){
					// no need to check that we are in the correct folder as the 
					// search is recursive only for good cases
					System.out.println("SampleHandler2.MyDeltaVisitor.visit() EXCELENT ! the project is now conf");
					projectsFilter.remove(proj);
					Filter f = new Filter(proj, res);
					projectsFilter.put(proj, f);
					
					// filter the whole project
					WorkspaceJob wj = new WorkspaceJob("WorkspaceJob name ? see SmapleHandler2") {
						@Override
						public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
							f.filterProject(arg0); // fixme NullProgressMonitor may not be ideal...
							return new Status(Status.OK, "AutoDeriv", "project filtered");
						}
					};
					wj.schedule();
					
				}else{
					
					if(isProjResource) 	return true;	// project
					if(name.length()==0)return true;	// workspace
					return false; // as the conf file must be in the project folder
				}

				break;
			case IResourceDelta.REMOVED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" was removed.");
				if(isconfile){
					System.out.println("SampleHandler2.MyDeltaVisitor.visit() No longer configured as AutoDeriv");
					projectsFilter.remove(proj);
				}else{
					if(isProjResource) 	return true;	// project
					if(name.length()==0)return true;	// workspace
					return false; // as the conf file must be in the project folder
				}
				break;
			case IResourceDelta.CHANGED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" has changed.");
				Filter f = projectsFilter.get(proj);
				if(isconfile){
					if(f!=null)
						f.updateConf();
				}else{
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
				}

				break;
				//FIXME other cases ???
			}
			return true;
		}

	}

	public SampleHandler2() {}

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

		try {
			event.getDelta().accept(new MyDeltaVisitor());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

	}
}
