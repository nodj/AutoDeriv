package net.nodj.autoderivplugin.handlers;

import static net.nodj.autoderivplugin.Debug.*;
import java.util.Collection;
import java.util.HashMap;
import net.nodj.autoderivplugin.Cst;
import net.nodj.autoderivplugin.Filter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

/**This class handles the active Filters of the plugin.
 * @author johan duparc (johan.duparc@gmail.com) */
public class FilterManager {

	/** ALL THE FILTERS |o/ */
	private static final HashMap<IProject, Filter> projectsFilter = new HashMap<IProject, Filter>();


	/** returns all the filters */
	public static Collection<Filter> getFilters()
	{ return projectsFilter.values(); }


	/** returns the Filter associated with specified project if any. Returns null in other cases. */
	public static Filter getByProj(IProject proj)
	{ return projectsFilter.get(proj); }


	/** Create a Filter for every project. Used when a wild master conf file appears */
	public static void filterForAll() {
		for (IProject proj : ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			if (!proj.isOpen()) continue;
			getOrCreateFilter(proj);
		}
	}


	/**Return this project Filter. Creates it if necessary.
	 * @param proj IProject related to the wanted Filter
	 * @return associated Filter object */
	public static Filter getOrCreateFilter(IProject proj) {
		Filter f = FilterManager.getByProj(proj);
		if(f == null){ // creation
			f = new Filter(proj);
			FilterManager.projectsFilter.put(proj, f);
			info("Project " + proj.getName() + " added to conf");
		}
		return f;
	}


	/** remove this project from AutoDeriv conf */
	public static void deleteFilter(IProject proj) {
		info("Project " + proj.getName() + " removed from conf");
		projectsFilter.remove(proj);
	}


	/** Apply all the Filters on their respective projects */
	public static void filterWorkspace(IProgressMonitor progress) {
		for(Filter f : projectsFilter.values())
			f.filterProject(progress);
	}


	/** check if the FilterManager has no Filter yet */
	public static boolean isEmpty() { return projectsFilter.isEmpty(); }


	/** Check if the given resource is a registered conf file */
	public static boolean isActiveConfFile(IResource r) {
		if(!r.getName().equals(Cst.CONF_FILE_NAME))
			return false;

		Filter f = getByProj(r.getProject());
		if(f == null)
			return false;

		return r.equals(f.getLocalConf());
	}


	/** remove markers from all Filters */
	public static void clearMarkers() {
		for(Filter f : projectsFilter.values())
			f.clearMarkers();
	}

}
