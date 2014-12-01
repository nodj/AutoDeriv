package autoderiv;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

public interface Rule {
	public void applyOnProject(IProgressMonitor progress);
	public void applyOnResource(IResource res, IProgressMonitor progress);
}
