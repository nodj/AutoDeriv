package autoderiv;

import java.io.PrintStream;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

/** Prints, log... Nothing fancy here */
public class Debug {

	public static ILog log;
	private static final String PREINFO = "i.";
	private static final String PREWARN = "!.";
	private static final String PREDBG  = "d.";

	/**print an info text line (clickable from eclipse console) */
	public static void info(String str) {
		if (Cst.ENABLE_INFO)
			common(PREINFO + str, System.out, Status.INFO);
	}

	public static void dbg(String str) {
		if (Cst.ENABLE_DBG)
			common(PREDBG + str, System.out, Status.OK);
	}

	/**print a warn text line (clickable from eclipse console) */
	public static void warn(String str) {
		if (Cst.ENABLE_WARN)
			common(PREWARN + str, System.err, Status.WARNING);
	}

	private static void common(String str, PrintStream flux, int status_level) {
		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		String msg = String.format("%s:\t%s @ %s.%s(%s:%s)%n",Tools.getms(), str, s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber());
		if(Cst.TRACE_STD) flux.print(msg);
		if(Cst.TRACE_LOG && log!=null) log.log(new Status(status_level, Cst.PLUGIN_NAME, msg));
	}

}
