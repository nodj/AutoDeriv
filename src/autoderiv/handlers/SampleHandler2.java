package autoderiv.handlers;

import java.util.HashMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
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
			
			Filter f = projectsFilter.get(proj);
			
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" was added.");
				if(name != CONF_FILE_NAME){
					if(isProjResource) 	return true;	// project
					if(name.length()==0)return true;	// workspace
					return false; // as the conf file must be in the project folder
				}
				break;
			case IResourceDelta.REMOVED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" was removed.");
				break;
			case IResourceDelta.CHANGED:
				System.out.print("Resource ");
				System.out.print(res.getFullPath());
				System.out.println(" has changed.");
				break;
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
