package net.nodj.autoderivplugin;

import java.io.PrintStream;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

/** Prints, log... Nothing fancy here */
public class Debug {

	private static int msgCount=0;
	public static ILog log;
	private static final String PREINFO = "i.";
	private static final String PREWARN = "!.";
	private static final String PREDBG  = "d.";

	/**print an info text line (clickable from eclipse console) */
	public static void info(String str) {
		if(!Conf.OUTPUT_STD && !Conf.OUTPUT_LOG) return;
		if (Conf.TRACE_INFO)
			common(PREINFO + str, System.out, Status.INFO);
	}

	/**print a debug text line (clickable from eclipse console) */
	public static void dbg(String str) {
		if(!Conf.OUTPUT_STD && !Conf.OUTPUT_LOG) return;
		if (Conf.TRACE_DEBUG)
			common(PREDBG + str, System.out, Status.OK);
	}

	/**print a warn text line (clickable from eclipse console) */
	public static void warn(String str) {
		if(!Conf.OUTPUT_STD && !Conf.OUTPUT_LOG) return;
		if (Conf.TRACE_WARN)
			common(PREWARN + str, System.err, Status.WARNING);
	}

	/** common part the check the stack, prints useful stuff, etc... */
	private static void common(String str, PrintStream flux, int status_level) {
		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		String msg = String.format("[msg %s] %s:\t%s @ %s.%s(%s:%s)%n",msgCount,Tools.getms(), str, s.getClassName(), s.getMethodName(), s.getFileName(), s.getLineNumber());
		msgCount++;
		if(Conf.OUTPUT_STD) flux.print(msg);
		if(Conf.OUTPUT_LOG && log!=null) log.log(new Status(status_level, Cst.PLUGIN_NAME, msg));
	}

}
