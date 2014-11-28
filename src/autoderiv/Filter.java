package autoderiv;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class Filter {
	IResource confFile;
	IProject project;

	String x = "derived";

	public Filter(IProject p, IResource r){
		confFile = r;
		project = p;
		parseRules();
	}

	private void parseRules(){
		// todo
	}

	private void filterResourceRec(IResource r, IProgressMonitor progress) throws CoreException{
		updateDerivedProperty(r, progress);
		if(r instanceof IFolder){
			IFolder f = (IFolder) r;
			for(IResource child : f.members()){
				filterResourceRec(child, progress);
			}
		}
	}

	public void filterProject(IProgressMonitor progress){
		System.out.println("Filter.filterProject()");
		try {
			for(IResource r : project.members()){
				filterResourceRec(r, progress);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void updateConf(){
		parseRules();
	}

	public boolean updateDerivedProperty(IResource res, IProgressMonitor progress) throws CoreException{
		boolean resDerived = false; // replace with actual filtering


		IPath ipath = res.getProjectRelativePath();
		resDerived = ipath.toPortableString().startsWith(x);

		if(resDerived && !res.isDerived()){
			System.out.println("Filter.updateDerivedProperty() : "+res.getName() + " set DERIVED");
		}
		res.setDerived(resDerived, progress);
		
		System.out.println("Filter.isDerived() toOSString "+ ipath.toOSString());
		System.out.println("Filter.isDerived() toString "+ ipath.toString());
		System.out.println("Filter.isDerived() toPortableString "+ ipath.toPortableString());

		return resDerived;
	}

}
