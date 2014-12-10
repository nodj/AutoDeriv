package autoderiv.handlers;

import static autoderiv.Debug.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import autoderiv.Filter;

/**This class 'reads' the change event deltas, and fills structure (VisitData)
 * with much simpler information.
 * (Later, a job will use these VisitData to process the workspace.)
 * @author johan duparc (johan.duparc@gmail.com) */
public class MyDeltaVisitor implements IResourceDeltaVisitor{
	VisitData v; ///< the structure that contains the result of the visitation

	MyDeltaVisitor(VisitData v){ this.v = v; }

	/**Called when a local conf file is modified
	 * @return true if we should visit the tree children */
	public boolean confFileEventHandler(VisitData v, IResourceDelta delta){
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


	/**Called far any non-conf file
	 * @return true if we should visit tree children */
	public boolean notConfFileEventHandler(VisitData v, IResourceDelta delta){
		IResource res = delta.getResource();
//		boolean isProject = (res.getType()==IResource.PROJECT);
//		boolean isWorkspace = (res.getType()==IResource.ROOT);

		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			if(res.getProject()==res){
				warn("Project added !!!" + res.getFullPath());
				v.projAdded = true;
				// we have to parse the whole project later, no need to pursue here
				return false;
			}
//			info("Resource " + res.getFullPath()+" was added.");
			v.added.add(res);
			return true;

		case IResourceDelta.REMOVED:
			return false;
//			if(res.getProject()==res){
//				warn("Project Removed !!!" + res.getFullPath());
//			}
//			info("Resource "+res.getFullPath()+" was removed.");
//			return isProject || isWorkspace; // so that we can see if the conf file was removed

		case IResourceDelta.CHANGED:
//			info("Resource "+res.getFullPath()+" was updated.");
			return true; // as we may encounter some addition later

		case IResourceDelta.ADDED_PHANTOM:
		case IResourceDelta.REMOVED_PHANTOM:
			break;

		default:
			warn("ChangeEventHandler.MyDeltaVisitor.notConfFileEventHandler() case not implemented");
		}
		warn("should not happen");
		return true;
	}


	/**Called for every delta of the change event.
	 * This implementation only dispatch to the confFileEventHandler and
	 * notConfFileEventHandler methods. */
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource res = delta.getResource();
		IProject proj = res.getProject();
		// remove. DBG only
		String name = res.getName();

		boolean isFile = (res.getType()==IResource.FILE);
		boolean isconfile = isFile && name.equals(Filter.CONF_FILE_NAME) && (res.getParent() == proj);
		// handle the resource
		if(isconfile)
			return confFileEventHandler(v, delta);
		return notConfFileEventHandler(v, delta);
	}

} // class MyDeltaVisitor