package net.nodj.autoderivplugin;

import static net.nodj.autoderivplugin.Debug.*;
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
import net.nodj.autoderivplugin.rules.PatternRule;
import net.nodj.autoderivplugin.rules.TreeRule;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;


public class Filter {

	/** IProject on which the rule is applicable */
	private IProject project;

	/** Reference on the master conf file. Not sure if that comment is useful... Sorry readers */
	private static File masterConfFile;
	/** The local conf file (if any. It's possible that the filter exists just because of a master file) */
	private IResource localConfFile;

	/** Rules are stored here. Two groups because of the order of application. */
	private ArrayList<IRule> localRules = new ArrayList<IRule>();
	private ArrayList<IRule> masterRules = new ArrayList<IRule>();


	/**Create a Filter for this IProject
	 * @param p IProject to follow */
	public Filter(IProject p){
		project = p;
		localConfFile = project.findMember(Cst.CONF_FILE_NAME);
		// do NOT parse here. Parsing is called explicitly.
	}


	/**Parse a single line. Deduced rule may be added to the specified filters.
	 * @param filters Filter object(s) of destination.
	 * @param master wither the target is the master or the local rules.
	 * @param line the textual line.
	 * @param lineNumber in-file line number (debug purpose) */
	private static void parseRule(Collection<Filter> filters, boolean master, String line, int lineNumber){
		dbg("Filter.parseRule() rule " + lineNumber + " ["+line+"]");
//		if(filters.isEmpty()) return; // commented out cause the check is already done in the only call site

		// filter out comments (after '#' char) and remove whitespaces
		line = Tools.trimAfter(line, '#').trim();

		if(line.isEmpty()) return;

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

		/* check for inverter character.
		 * A line starting with a '!' sets resources as NOT derived */
		boolean setAsDerived = !line.startsWith("!");
		if(!setAsDerived) line = line.substring(1).trim();

		// line is a simple path (note that that tests are not very good)
		if(isValidPath(line)){
			for(Filter filter : filters){
				IProject proj = filter.project;
				String path = proj.getLocation().toPortableString()+Path.SEPARATOR+line;
				ArrayList<IRule> dest = master ? filter.masterRules : filter.localRules;
				IPath p = new Path(path).makeRelativeTo(proj.getLocation());
				dest.add(new TreeRule(proj, p, setAsDerived));
			}
			return;
		}

		//else, maybe its a regex. Uses Java Patterns with modified syntax
		String regex = adaptRegaxpToPatterns(line);

		try{
			// creation of the effective rule
			Pattern p = Pattern.compile(regex); // can explode

			// add it to target(s)
			for(Filter filter : filters){
				ArrayList<IRule> dest = (master ? filter.masterRules : filter.localRules);
				dest.add(new PatternRule(filter.project, p, setAsDerived));
			}
			return;
		}catch(PatternSyntaxException e){
			warn("in rule " + lineNumber + ": bad regexp ("+ line +"): " + e.getMessage());
		}

		warn("no use for line "+lineNumber+" ["+line+"]");
	}

	/**This method transforms fnmatch-like regex to java Pattern regex.
	 * @param regex fnmatch-regex
	 * @return java Pattern regex */
	private static String adaptRegaxpToPatterns(String regex) {
		//@see http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

		regex = regex.replace("*", "##ANY_CHAR##");
		regex = regex.replace(".", "##DOT_CHAR##");
		regex = regex.replace("?", "##ONE_CHAR##");

		regex = regex.replace("##ANY_CHAR##", ".*");
		regex = regex.replace("##DOT_CHAR##", "[.]");
		regex = regex.replace("##ONE_CHAR##", ".");
		return regex;
	}


	/** Try to determinate if a path is valid */
	private static boolean isValidPath(String potentialPath) {

		/* This part filter a rule so that illegal paths are not generating a
		 * TreeRule.
		 * In order to keep a similar behavior for all OS, the restriction
		 * doesn't takes into account the fact that these chars are illegal on
		 * windows only, and restrict their use in all cases.
		 * This may not be optimal... Any idea ? */
		String winBadChar = ":*?\"<>|"; // note / and \ are path separator, should stay allowed as path part
		for(int i = 0; i< winBadChar.length(); i++)
			if(potentialPath.indexOf(winBadChar.charAt(i))!=-1)
				return false;

		// the "getCanonicalPath" method may throw an exception in some bad path case. Try to use this.
		try {
			ResourcesPlugin.getWorkspace().getRoot().getLocation().append(potentialPath).toFile().getCanonicalPath();
		} catch (IOException e) {
			return false;
		}

		// all our tests pass... Probably a real path.
		return true;
	}


	/**Parse a conf file.
	 * Resulting rules will be append to all given Filters, in the local or
	 * master depending on the boolean.
	 * @param confFile textual rules
	 * @param filters destination Filter object(s)
	 * @param isMaster specifies if the target is the master or local rules */
	private static void parseRules(File confFile, Collection<Filter> filters, boolean isMaster){
		if(filters.isEmpty()) return; // this case may actually happen
		if(!confFile.exists()){
			warn("Filter.parseRules() What ???");
			return; // weird...
		}

		FileInputStream fis;
		try {
			fis = new FileInputStream(confFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		try {
			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null)
				parseRule(filters, isMaster, line, ++i);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/** parse master file for several Filter objects at once.
	 * Performs complex parsing once instead of once per Filter */
	public static void parseMasterRules(Collection<Filter> filters, IProgressMonitor progress){
		info("Filter.parseMasterRules() Parsing masterRules");

		for(Filter filter : filters){
			filter.masterRules.clear();
		}

		// no need to go further if the master file is not here
		if(!hasMasterConf()) return;

		// effective load
		progress.subTask("Parsing master conf file");
		parseRules(masterConfFile, filters, true);
	}


	/**Filter the whole project */
	public void filterProject(IProgressMonitor progress){
		progress.subTask("filter project "+project.getName());
		for(IRule iRule : masterRules) iRule.applyOnProject(progress);
		for(IRule iRule : localRules) iRule.applyOnProject(progress);
	}


	/**Important function that apply this filter on a specific resource.
	 * No recursion here. */
	public void filterResources(ArrayList<IResource> resources, IProgressMonitor progress){
		progress.subTask("filter resources in project "+project.getName());
		for(IResource res : resources){
			for(IRule iRule : masterRules) iRule.applyOnResource(res, progress);
			for(IRule iRule : localRules) iRule.applyOnResource(res, progress);
		}
	}


	/**just update the localRules.
	 * @note Do NOT filter anything after the parsing, it must be explicitly
	 * asked. This is in order to minimize the work (load time) at startup. */
	public void reparseLocalConf(IProgressMonitor progress){
		// todo hash useful text parts,
		// return if hash == old hash

		dbg("Filter.reparseLocalConf() Parsing localRules");
		localRules.clear();

		// find the conf file, return if not found
		localConfFile = project.findMember(Cst.CONF_FILE_NAME);
		if(!hasLocalConf()) return;

		// fill with the new rules
		progress.subTask("parse local conf for project "+project.getName());
		ArrayList<Filter> filters = new ArrayList<Filter>();
		filters.add(this);
		parseRules(localConfFile.getLocation().toFile(), filters, false);
	}


	/**Update the master conf file. Used to set, but also to remove the master
	 * conf file (set to null in that case)
	 * @param f new master conf file, or null in order to remove the MCF */
	public static void setMasterConfFile(File f)
	{ masterConfFile = f; }


	/**Check if a project contains a configuration file.
	 * Static version used when the Filter doesn't even exists.
	 * (The non-static version is a nonsense in that case)
	 * @param proj IProject to scan for a conf file
	 * @return true if the conf file is found */
	public static boolean hasLocalConf(IProject proj) {
		IResource conf = proj.findMember(Cst.CONF_FILE_NAME);
		return conf != null;
	}


	/**Checks that it has a local conf file associated. This makes sense cause
	 * the Filter may exists only because of the master conf file.
	 * @return true if the Filter has a local conf file associated. */
	public boolean hasLocalConf()
	{ return localConfFile != null; }


	/**Check if the master conf file is set (non-null) */
	public static boolean hasMasterConf()
	{ return masterConfFile != null; }


	public IResource getLocalConf() {
		return localConfFile;
	}

}
