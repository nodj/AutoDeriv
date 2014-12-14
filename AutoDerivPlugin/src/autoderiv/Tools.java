package autoderiv;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/** Useful things can goes here... */
public class Tools {

	// timing functions
	private static final long startns = System.nanoTime();
	public static double 	getmsd(){ return getns()*1e-6;}
	public static long 		getms(){ return (long)getmsd();}
	public static long 		getns()	{ return System.nanoTime()-startns; }


	// resource setting functions


	/** This plugin main task is to affect the 'derived' attribute. We should do
	 * that correctly. As a setDerived() call will fire a change event, we try
	 * to do this only if necessary. That the point of this checked version. */
	public static void checkedSetDerived(IResource res, boolean derived, IProgressMonitor progress) throws CoreException {
		if(res.isDerived() != derived)
			res.setDerived(derived, progress);
	}


	/**Same as the previous call, but handle the possible exception with a no-op reaction. */
	public static void checkedSetDerived_nt(IResource res, boolean derived, IProgressMonitor progress){
		if(res.isDerived() != derived){
			try {
				res.setDerived(derived, progress);
			} catch (CoreException e) { e.printStackTrace(); }
		}
	}

}
