package autoderiv;

import static autoderiv.Debug.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
		info("Filter.parseRule() rule["+line+"]");

		Path p = new Path(line);
		rules.add(new TreeRule(project, p, true));
	}

	private void parseRules(){
		rules.clear();
		info("Filter.parseRules() Parsing rules");
		// todo
		File f = confFile.getLocation().toFile();
		if(!f.exists()){
			warn("Filter.parseRules() What ???");
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


	public void filterProject(IProgressMonitor progress){
		for(Rule rule : rules){
			rule.applyOnProject(progress);
		}
	}

	public void filterResources(ArrayList<IResource> added, IProgressMonitor progress) throws CoreException{
		for(IResource res : added){
			for(Rule rule : rules){
				rule.applyOnResource(res, progress);
			}
		}
	}

	/**just update the rules. 
	 * @note Do NOT filter anything after the parsing, it must be explicitly 
	 * asked. This is in order to minimize the work (load time) at startup. 
	 */
	public void updateConf(){
		parseRules();
	}

	public boolean updateDerivedProperty(IResource res, IProgressMonitor progress) throws CoreException{
		boolean resDerived = false; // replace with actual filtering


		IPath ipath = res.getProjectRelativePath();
		resDerived = ipath.toPortableString().startsWith(x);

//		if(resDerived && !res.isDerived()){
//			info("Filter.updateDerivedProperty() : "+res.getName() + " set DERIVED");
//		}
		res.setDerived(resDerived, progress);

//		info("Filter.isDerived() toOSString "+ ipath.toOSString());
//		info("Filter.isDerived() toString "+ ipath.toString());
//		info("Filter.isDerived() toPortableString "+ ipath.toPortableString());

		return resDerived;
	}

}
