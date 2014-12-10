package autoderiv;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class Tools {

	private static final long startns = System.nanoTime();

	public static double 	getmsd(){ return getns()*1e-6;}
	public static long 		getms(){ return (long)getmsd();}
	public static long 		getns()	{ return System.nanoTime()-startns; }



	public static void checkedSetDerived(IResource res, boolean derived, IProgressMonitor progress) throws CoreException {
		if(res.isDerived() != derived)
			res.setDerived(derived, progress);
	}

	public static void checkedSetDerived_nt(IResource res, boolean derived, IProgressMonitor progress){
		if(res.isDerived() != derived){
			try {
				res.setDerived(derived, progress);
			} catch (CoreException e) { e.printStackTrace(); }
		}
	}


}
