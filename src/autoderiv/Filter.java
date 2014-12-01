package autoderiv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import autoderiv.rules.TreeRule;

public class Filter {
	IResource confFile;
	IProject project;

	String x = "derived";
	ArrayList<Rule> rules = new ArrayList<Rule>();
	
	public Filter(IProject p, IResource r){
		confFile = r;
		project = p;
		parseRules();
	}

	private void parseRule(String line){
		System.out.println("Filter.parseRule() rule["+line+"]");
		
		Path p = new Path(line);
		rules.add(new TreeRule(project, p, true));
	}
	
	private void parseRules(){
		System.out.println("Filter.parseRules() Parsing rules");
		// todo
		File f = confFile.getLocation().toFile();
		if(!f.exists()){
			System.out.println("Filter.parseRules() What ???");
			return; // weird...
		}
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		 
		//Construct BufferedReader from InputStreamReader
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	 
		try {
			String line = null;
			while ((line = br.readLine()) != null) 
				parseRule(line);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	 
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
	
	public void filterResources(ArrayList<IResource> added, IProgressMonitor progress) throws CoreException{
		for(IResource r : added){
			updateDerivedProperty(r, progress);
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
		// TODO filter the whole project accordingly
		
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
