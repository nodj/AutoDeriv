package autoderiv;

import java.io.PrintStream;
import java.util.ArrayList;

public class Debug {
	public static boolean printDbg = true;
	public static boolean printInfo = true;
	public static boolean printWarn = true;
	public static boolean printErr = true;
	public static boolean printGlErr = true;
	private static final String PREINFO = "i.";
	private static final String PREWARN = "!.";
//	private static final String PREERR = "ERROR.";
	private static ArrayList<String> log = new ArrayList<String>();

	
	/**print an info text line (clickable from eclipse console) */
	public static void info(String str) {
		if (printInfo) common(PREINFO + str, System.out);
	}

	/**print a warn text line (clickable from eclipse console) */
	public static void warn(String str) {
		if (printWarn) common(PREWARN + str, System.err);
	}

// DISABLE FOR A PLUGIN PROJECT
	/**print a warn text line (clickable from eclipse console) */
//	public static void err(String str) {
//		if (printErr) {
//			common(PREERR + str, System.err);
//			System.exit(-1);
//		}
//	}

	/**silently add a log line */
	public static void log(String s) {
		log.add(s);
	}

	/** dump the whole log */
	public static void printLog() {
		for (String s : log)
			System.out.println(s);
		log.clear();
	}

	/** @return the caller line number */
	public static int getLine() {
		StackTraceElement s = Thread.currentThread().getStackTrace()[2];
		return s.getLineNumber();
	}

	private static void common(String str, PrintStream flux) {
		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		flux.printf("%s:\t%s @ %s.%s(%s:%s)%n",Tools.getms(), str, s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber());
	}

}
