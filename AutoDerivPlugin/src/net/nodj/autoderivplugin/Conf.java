package net.nodj.autoderivplugin;

import net.nodj.autoderivplugin.preferences.PrefCst;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**Here are stored the plugin settings.
 * Most of these options are accessible through the plugin preference page. */
public class Conf {

	public static boolean	STARTUP_CHECK;
	public static boolean	OUTPUT_LOG;
	public static boolean	OUTPUT_STD;
	public static boolean	TRACE_WARN;
	public static boolean	TRACE_INFO;
	public static boolean	TRACE_DEBUG;
	public static boolean	DECO_LABEL_TEXT;
	public static String	DECO_PREFIX;
	public static String	DECO_SUFFIX;
	public static RGB		DECO_FOREGROUND;
	public static RGB		DECO_BACKGROUND;
	public static boolean	DECO_FONT_ENABLED;
	public static FontData	DECO_FONT_DATA;
	public static boolean	DECO_ICON_ENABLED;
	public static int		DECO_ICON_LOC;
	public static boolean	DECO_LABEL_BCOLOR;
	public static boolean	DECO_LABEL_FCOLOR;

	public static void read() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		STARTUP_CHECK = store.getBoolean(PrefCst.P_STARTUP_CHECK);

		OUTPUT_LOG = store.getBoolean(PrefCst.P_OUTPUT_LOG);
		OUTPUT_STD = store.getBoolean(PrefCst.P_OUTPUT_STD);
		boolean anyOutput = OUTPUT_LOG | OUTPUT_STD;
		if(anyOutput){
			TRACE_WARN = store.getBoolean(PrefCst.P_TRACE_WARN);
			TRACE_INFO = store.getBoolean(PrefCst.P_TRACE_INFO);
			TRACE_DEBUG = store.getBoolean(PrefCst.P_TRACE_DEBUG);
		}
		TRACE_WARN  &= anyOutput;
		TRACE_INFO  &= anyOutput;
		TRACE_DEBUG &= anyOutput;

		DECO_LABEL_TEXT = store.getBoolean(PrefCst.P_DECO_LABEL_TEXT);
		if(DECO_LABEL_TEXT){
			DECO_PREFIX = store.getString (PrefCst.P_DECO_PREFIX);
			if(DECO_PREFIX.length()==0) DECO_PREFIX=null;
			DECO_SUFFIX = store.getString (PrefCst.P_DECO_SUFFIX);
			if(DECO_SUFFIX.length()==0) DECO_SUFFIX=null;
		}

		DECO_LABEL_FCOLOR = store.getBoolean(PrefCst.P_DECO_LABEL_FCOLOR);
		if(DECO_LABEL_FCOLOR)
			DECO_FOREGROUND = PreferenceConverter.getColor(store, PrefCst.P_DECO_FOREGROUND);

		DECO_LABEL_BCOLOR = store.getBoolean(PrefCst.P_DECO_LABEL_BCOLOR);
		if(DECO_LABEL_BCOLOR)
			DECO_BACKGROUND = PreferenceConverter.getColor(store, PrefCst.P_DECO_BACKGROUND);

		DECO_FONT_ENABLED = store.getBoolean(PrefCst.P_ENABLE_DECO_FONT);
		if(DECO_FONT_ENABLED)
			DECO_FONT_DATA = PreferenceConverter.getFontData(store, PrefCst.P_DECO_LABEL_FONT);

		DECO_ICON_ENABLED = store.getBoolean(PrefCst.P_ENABLE_DECO_ICON);
		if(DECO_ICON_ENABLED){
			String loc_str = store.getString(PrefCst.P_DECO_ICON_LOC);
			if(loc_str.equals("TOP_LEFT"))
				DECO_ICON_LOC = IDecoration.TOP_LEFT;
			if(loc_str.equals("TOP_RIGHT"))
				DECO_ICON_LOC = IDecoration.TOP_RIGHT;
			if(loc_str.equals("BOTTOM_LEFT"))
				DECO_ICON_LOC = IDecoration.BOTTOM_LEFT;
			if(loc_str.equals("BOTTOM_RIGHT"))
				DECO_ICON_LOC = IDecoration.BOTTOM_RIGHT;
		}


	}

}

