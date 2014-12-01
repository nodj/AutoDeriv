package autoderiv;

import org.eclipse.core.runtime.IProgressMonitor;

public interface Rule {
	public void applyOnProject(IProgressMonitor progress);
}
