package net.nodj.autoderivplugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**Interface for a rule.
 * A rule may be applied on a specific workspace IResource, or on the whole IProject. */
public interface IRule {
	public void applyOnProject(IProgressMonitor progress);
	public void applyOnResource(IResource res, IProgressMonitor progress);
}
