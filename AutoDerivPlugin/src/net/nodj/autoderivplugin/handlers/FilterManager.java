package net.nodj.autoderivplugin.handlers;

import static net.nodj.autoderivplugin.Debug.*;
import java.util.Collection;
import java.util.HashMap;
import net.nodj.autoderivplugin.Filter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

/**This class handles the active Filters of the plugin.
 * @author johan duparc (johan.duparc@gmail.com) */
public class FilterManager {

	/// ALL THE FILTERS |o/
	private static final HashMap<IProject, Filter> projectsFilter = new HashMap<IProject, Filter>();

	public static Collection<Filter> getFilters()
	{ return projectsFilter.values(); }

	public static Filter getByProj(IProject proj)
	{ return projectsFilter.get(proj); }

	/** Create a Filter for every project. Used when a wild master conf file appears */
	public static void filterForAll() {
		for (IProject proj : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			if (!proj.isOpen())
				continue;
			else
				getOrCreateFilter(proj);
	}

	/**Return this project Filter. Creates it if necessary.
	 * @param proj IProject related to the wanted Filter
	 * @return associated Filter object */
	public static Filter getOrCreateFilter(IProject proj) {
		Filter f = FilterManager.getByProj(proj);
		if(f == null){ // creation
			f = new Filter(proj);
			FilterManager.projectsFilter.put(proj, f);
			warn("Project " + proj.getName() + " added to conf");
		}
		return f;
	}


	/** remove this project from AutoDeriv conf */
	public static void deleteFilter(IProject proj) {
		warn("Project " + proj.getName() + " removed from conf");
		projectsFilter.remove(proj);
	}


	public static void filterWorkspace(IProgressMonitor progress) {
		for(Filter f : projectsFilter.values()){
			f.filterProject(progress);
		}
	}

	public static boolean isEmpty() { return projectsFilter.isEmpty(); }



}
