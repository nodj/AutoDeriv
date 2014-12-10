package autoderiv;

import static autoderiv.Debug.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
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

	public static final String	CONF_FILE_NAME	= ".derived";
	static private File masterConfFile;
	private IResource localConfFile;
	private IProject project;
	private ArrayList<Rule> localRules = new ArrayList<Rule>();
	private ArrayList<Rule> masterRules = new ArrayList<Rule>();


	public Filter(IProject p){
		project = p;
		localConfFile = project.findMember(CONF_FILE_NAME);
		// do NOT parse here. Parsing is called explicitly.
	}

	private static void parseRule(Collection<Filter> filters, boolean master, String line, int lineNumber){
		info("Filter.parseRule() rule " + lineNumber + " ["+line+"]");
		if(filters.isEmpty()) return;

		// filter out comments (after #char)
		int commentLocation = line.indexOf('#');
		if(commentLocation != -1)
			line = line.substring(0, commentLocation);

		// remove leading / trailing whitespace
		line = line.trim();
		if(line.length() == 0) return;

		dbg("usefull part line is : ["+line+"]");

		// line is a special line
		if(line.startsWith(">")){
//			line = line.substring(1).trim();
			// command line.
//			if(line.startsWith("extension")){
//
//			}
//			if(line.startsWith("global.before.local")){
//			}
//			if(line.startsWith("global.after.local")){
//			}
//			localRules.add(new XXXRule(project,...));
			warn("commands not handled in this version");
			return;
		}

		boolean setAsDerived = true;
		if(line.startsWith("!")){
			line = line.substring(1).trim();
			setAsDerived =false;
		}

		boolean isValidPath = true;


		/* This part filter a rule so that illegal paths are not generating a
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
				String fakepath = filters.iterator().next().project.getLocation().append(line).toPortableString();
				File f = new File(fakepath);
				fakepath = f.getCanonicalPath();
			} catch (IOException e) {
				isValidPath = false;
			}
		}

		// line is a simple path
		if(isValidPath){
			for(Filter filter : filters){
				IProject proj = filter.project;
				String path = proj.getLocation().toPortableString()+Path.SEPARATOR+line;
				ArrayList<Rule> dest = master ? filter.masterRules : filter.localRules;
				IPath p = new Path(path).makeRelativeTo(proj.getLocation());
				dest.add(new TreeRule(proj, p, setAsDerived));
			}
			return;
		}

		//else, maybe its a regex. Uses Java Patterns with modified syntax
		//@see http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
		try{
			String regex = line;

			/* Check if the rule starts with '*'. While this is not OK for the
			 * java Pattern (this results in an exception), I think the user
			 * expectation is that the '*' should match anything. In Java
			 * Pattern, this is ".*" (instead of "*").
			 * -> I replace any '*' by the '.*' sequence*/

			regex = regex.replace("*", "##ANY_CHAR##");
			regex = regex.replace(".", "##DOT_CHAR##");
			regex = regex.replace("?", "##ONE_CHAR##");

			regex = regex.replace("##ANY_CHAR##", ".*");
			regex = regex.replace("##DOT_CHAR##", "[.]");
			regex = regex.replace("##ONE_CHAR##", ".");

			// creation of the effective rule
			Pattern p = Pattern.compile(regex); // can explode
//			for(Entry<IProject, ArrayList<Rule>> destEntry : filters){
//				IProject proj = destEntry.getKey();
//				ArrayList<Rule> dest = destEntry.getValue();
//				dest.add(new PatternRule(proj, p, setAsDerived));
//			}

			for(Filter filter : filters){
				IProject proj = filter.project;
				ArrayList<Rule> dest = master ? filter.masterRules : filter.localRules;
				dest.add(new PatternRule(proj, p, setAsDerived));
			}
			return;
		}catch(PatternSyntaxException e){
			warn("in rule " + lineNumber + ": bad regexp ("+ line +"): " + e.getMessage());
		}

		warn("no use for line "+lineNumber+" ["+line+"]");
	}


	private void parseRules(File confFile, Filter filter, boolean master) {
		ArrayList<Filter> filters = new ArrayList<Filter>();
		filters.add(filter);
		parseRules(confFile, filters, master);
	}


	private static void parseRules(File f, Collection<Filter> filters, boolean master){
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
				parseRule(filters, master, line, ++i);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}


	/** parse master file for several Filter at once.
	 * Avoid complex parsing operation to be performed once per project. This
	 * allows parsing once per workspace */
	public static void parseMasterRules(Collection<Filter> filters){
		info("Filter.parseMasterRules() Parsing masterRules");

		for(Filter filter : filters)
			filter.masterRules.clear();

		// no need to go further if the master file is not here
		if(masterConfFile == null) return;

		// effective load
		parseRules(masterConfFile, filters, true);
	}


	public void filterProject(IProgressMonitor progress){
		for(Rule rule : masterRules) rule.applyOnProject(progress);
		for(Rule rule : localRules) rule.applyOnProject(progress);
	}

	public void filterResources(ArrayList<IResource> added, IProgressMonitor progress) throws CoreException{
		for(IResource res : added){
			for(Rule rule : masterRules) rule.applyOnResource(res, progress);
			for(Rule rule : localRules) rule.applyOnResource(res, progress);
		}
	}

	/**just update the localRules.
	 * @note Do NOT filter anything after the parsing, it must be explicitly
	 * asked. This is in order to minimize the work (load time) at startup.
	 */
	public void reparseLocalConf(){
		localConfFile = project.findMember(CONF_FILE_NAME);

		localRules.clear();
		info("Filter.reparseLocalConf() Parsing localRules");
		if(!hasLocalConf()) return;
		File localConf = localConfFile.getLocation().toFile();

		parseRules(localConf, this, false);
	}


	public static void setMasterConfFile(File f) {
		masterConfFile = f;
	}

	public static boolean hasLocalConf(IProject proj) {
		IResource conf = proj.findMember(Filter.CONF_FILE_NAME);
		return conf != null;
	}

	public boolean hasLocalConf()
	{ return localConfFile != null; }

	public static boolean hasMasterConf()
	{ return masterConfFile != null; }

}
