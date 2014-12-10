package autoderiv.rules;

import static autoderiv.Debug.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import autoderiv.Rule;
import autoderiv.Tools;

public class PatternRule implements Rule {
	private IProject project;
	private Pattern pattern;
	private boolean isDerived;

	public PatternRule(IProject project, Pattern p, boolean setAsDerived) {
		this.project = project;
		pattern = p;
		isDerived = setAsDerived;

		info("PatternRule created for project "+project.getName()+", pattern \""+p.pattern() + '"');
	}

	@Override
	public void applyOnProject(final IProgressMonitor progress) {
		try {
			project.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource res) throws CoreException {
					Matcher m = pattern.matcher(res.getProjectRelativePath().toPortableString());
					if(m.matches())
						Tools.checkedSetDerived(res, isDerived, progress);
					return true;
				}
			});
		} catch (CoreException e) { e.printStackTrace(); }
	}

	@Override
	public void applyOnResource(IResource res, IProgressMonitor progress) {
		Matcher m = pattern.matcher(res.getProjectRelativePath().toPortableString());
		if(m.matches())
			Tools.checkedSetDerived_nt(res, isDerived, progress);
	}

}
