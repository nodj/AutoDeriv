package autoderiv;

import static autoderiv.Debug.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import autoderiv.rules.PatternRule;
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

	private void parseRule(String line, int linenumber){
		info("Filter.parseRule() rule " + linenumber + " ["+line+"]");

		// filter out comments (after #char)
		int commentLocation = line.indexOf('#');
		if(commentLocation != -1)
			line = line.substring(0, commentLocation);

		// remove leading / trailing whitespace
		line = line.trim();
		if(line.length() == 0) return;

		info("usefull part line is : ["+line+"]");

		// line is a special line
		if(line.startsWith(">")){
//			line = line.substring(1).trim();
			// command line. 
//			if(line.startsWith("extension")){
//				
//			}
//			rules.add(new XXXRule(project,...));
			warn("commands not handled in this version");
			return;
		}

		boolean setAsDerived = true;
		if(line.startsWith("!")){
			line = line.substring(1).trim();
			setAsDerived =false;
		}

		// line is a simple path 
		String path = project.getLocation().toPortableString()+Path.SEPARATOR+line;
		boolean isValidPath = true;


		/*
		 * This part filter a rule so that illegal paths are not generating a 
		 * TreeRule.
		 * In order to keep a similar behavior for all OS, the restriction 
		 * doesn't takes into account the fact that these chars are illegal on
		 * windows only, and restrict their use in all cases. 
		 * This may not be optimal... Any idea ?
		 */
		if(File.separatorChar == '\\'){ // Windows
			String winBadChar = ":*?\"<>|"; // note / and \ are path separator, should stay allowed as path part
			for(int i = 0; i< winBadChar.length(); i++){
				if(line.indexOf(winBadChar.charAt(i))!=-1){
					isValidPath = false;
					break;
				}
			}
		}

		if(isValidPath){
			try {
				File f = new File(path);
				path = f.getCanonicalPath();
				info("parsed path: "+path);
			} catch (IOException e) {
				isValidPath = false;
			}
		}

		if(isValidPath){
			IPath p = new Path(path).makeRelativeTo(project.getLocation());
			rules.add(new TreeRule(project, p, setAsDerived));
			return;
		}

		//else, maybe its a regex. Uses Java Patterns with their syntax
		//@see http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
		try{
			Pattern p = Pattern.compile(line); // can explode
			rules.add(new PatternRule(project, p, setAsDerived));
			return;
		}catch(PatternSyntaxException e){
			// todo if it start with a *, maybe it's a simple extension filter ?
			
			warn("in rule " + linenumber + ": bad regexp ("+ line +"): " + e.getMessage());
		}
		


		warn("no use for line "+linenumber+" ["+line+"]");
	}

	private void parseRules(){
		rules.clear();
		info("Filter.parseRules() Parsing rules");
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
			int i = 0;
			while ((line = br.readLine()) != null) 
				parseRule(line, ++i);
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


}
