package net.nodj.autoderivplugin.preferences;

import net.nodj.autoderivplugin.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/** Class used to initialize default preference values. */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/** Unique class entry point.
	 * Not sure when this is called. Expected at plugin startup ?
	 * Set the default values for each parameters */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(PrefCst.P_STARTUP_CHECK, true);
		store.setDefault(PrefCst.P_ENABLE_MARKER, true);

		store.setDefault(PrefCst.P_OUTPUT_LOG, true);
		store.setDefault(PrefCst.P_OUTPUT_STD, false);
		store.setDefault(PrefCst.P_TRACE_WARN, true);
		store.setDefault(PrefCst.P_TRACE_INFO, false);
		store.setDefault(PrefCst.P_TRACE_DEBUG, false);

		store.setDefault(PrefCst.P_DECO_LABEL_TEXT, true);
		store.setDefault(PrefCst.P_DECO_PREFIX, "(");
		store.setDefault(PrefCst.P_DECO_SUFFIX, ")");

		store.setDefault(PrefCst.P_ENABLE_DECO_FONT, false);

		store.setDefault(PrefCst.P_DECO_LABEL_FCOLOR, true);
		store.setDefault(PrefCst.P_DECO_FOREGROUND, "80,80,80");
		store.setDefault(PrefCst.P_DECO_LABEL_BCOLOR, false);
		store.setDefault(PrefCst.P_DECO_BACKGROUND, "255,255,255");

		store.setDefault(PrefCst.P_ENABLE_DECO_ICON, true);
		store.setDefault(PrefCst.P_ENABLE_CONF_ICON, true);
		store.setDefault(PrefCst.P_DECO_ICON_LOC, "TOP_LEFT");
		store.setDefault(PrefCst.P_ICON_COLOR, "200,255,0");
	}

}
