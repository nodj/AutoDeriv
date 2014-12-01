package autoderiv;

public class Tools {

	private static final long startns = System.nanoTime();

	public static double 	getmsd(){ return getns()*1e-6;}
	public static long 		getms(){ return (long)getmsd();}
	public static long 		getns()	{ return System.nanoTime()-startns; }

}
