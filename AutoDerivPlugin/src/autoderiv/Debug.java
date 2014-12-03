package autoderiv;

import java.io.PrintStream;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

public class Debug {
	public static boolean printInfo = true;
	private static final String PREINFO = "i.";

	public static boolean printWarn = true;
	private static final String PREWARN = "!.";

//	public static boolean printErr = true;
//	private static final String PREERR = "ERROR.";

	public static boolean printLog = true;
	public static ILog log;


	/**print an info text line (clickable from eclipse console) */
	public static void info(String str) {
		if (printInfo){
			common(PREINFO + str, System.out);
			log(Status.INFO, str);
		}
	}

	/**print a warn text line (clickable from eclipse console) */
	public static void warn(String str) {
		if (printWarn) {
			common(PREWARN + str, System.err);
			log(Status.WARNING, str);
		}
	}

// DISABLE FOR A PLUGIN PROJECT
	/**print a warn text line (clickable from eclipse console) */
//	public static void err(String str) {
//		if (printErr) {
//			common(PREERR + str, System.err);
//			if(log!=null) log.log(new Status(Status.ERROR, Activator.PLUGIN_ID, str));
//			System.exit(-1);
//		}
//	}


	/** @return the caller line number */
	public static int getLine() {
		StackTraceElement s = Thread.currentThread().getStackTrace()[2];
		return s.getLineNumber();
	}

	private static void common(String str, PrintStream flux) {
		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		flux.printf("%s:\t%s @ %s.%s(%s:%s)%n",Tools.getms(), str, s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber());
	}

	private static void log(int status_level, String str){
		if(printLog && log!=null) log.log(new Status(Status.WARNING, Activator.PLUGIN_ID, str));
	}

}
