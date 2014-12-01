package autoderiv.rules;

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
	}

	public void applyOnProject(IProgressMonitor progress) {
		// initialization
		if(member == null) 
			member = project.findMember(path);
		
		// member may not exist
		if(member == null || !member.exists()) 
			return;
		
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

}
