package autoderiv.rules;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import autoderiv.Rule;

/**@brief this Rule is used when the conf file specify a folder which is 
 * recursively derived.
 * Useful for build temp folder for example.
 * rule is like :
 * 		.obj
 * 		.dep 
 */
public class TreeRule implements Rule{

	// the tree node which is designated by the rule.
	IPath path;
	boolean isDerived;
	private IResource	member;
	IProject project;

	public TreeRule(IProject project, IPath specificRes, boolean d){
		this.project = project;
		path = specificRes;
		isDerived = d;
		member = project.findMember(path);
		
		// remove
		System.out.println("TreeRule.TreeRule() for res "+path);
		if(member!=null)
			System.out.println("Yes !");
	}

	public void applyOnProject(IProgressMonitor progress) {
		// initialization
		if(!checkIni()) return;
		
		// effective action
		try {
			member.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource res) throws CoreException {
					res.setDerived(isDerived, progress);
					return true;
				}
			});
		} catch (CoreException e) { e.printStackTrace(); }
	}

	boolean checkIni(){
		if(member == null) 
			member = project.findMember(path);
		return member != null && member.exists(); 
	}
	
	@Override
	public void applyOnResource(IResource res, IProgressMonitor progress) {
		// initialization
		if(!checkIni()) return;
		System.out.println("TreeRule.applyOnResource() 65");
		// check if the resource is handled by the rule
		boolean fits = false;
		switch(res.getType()){
		case IResource.FILE :
			fits = member.equals(res);
			break;
		case IResource.FOLDER :
			IFolder f = (IFolder) res;
			fits = f.findMember(res.getLocation()) != null;			
			break;
		default:
			// should not happen: resource is not supposed to be a project or /
			System.out.println("TreeRule.applyOnResource() NOT HANDLED");
		}
		System.out.println("TreeRule.applyOnResource() 80 "+fits);
		
		// the resources matches the rule, apply derived attribute
		if(fits){
			try {
				res.setDerived(isDerived, progress);
				System.out.println("TreeRule.applyOnResource() 86");
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}
