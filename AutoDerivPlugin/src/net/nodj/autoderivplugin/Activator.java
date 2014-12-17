package net.nodj.autoderivplugin;

import static net.nodj.autoderivplugin.Debug.*;
import net.nodj.autoderivplugin.handlers.ChangeEventHandler;
import net.nodj.autoderivplugin.handlers.Decorator;
import net.nodj.autoderivplugin.preferences.PrefCst;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/** The activator class controls the plug-in life cycle */
public class Activator extends AbstractUIPlugin implements IPropertyChangeListener {
	private static AbstractUIPlugin	plugin;
	// The shared instance
	ChangeEventHandler listener;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Debug.log = getLog();
		info("AutoDeriv plugin starts");

		Conf.read();

		// launch the listener, + ini
		listener = new ChangeEventHandler();
		listener.startup();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);

		getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		info("AutoDeriv plugin stops");
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		super.stop(context);
	}

	public static AbstractUIPlugin getDefault() {
		return plugin;
	}


	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// read all the parameters. Not optimal, but...
		Conf.read();

		String prop = event.getProperty();

		if(prop.equals(PrefCst.P_DECO_FOREGROUND)
		|| prop.equals(PrefCst.P_DECO_BACKGROUND)
		|| prop.equals(PrefCst.P_DECO_PREFIX)
		|| prop.equals(PrefCst.P_DECO_SUFFIX)
		|| prop.equals(PrefCst.P_DECO_ICON_LOC)
		|| prop.equals(PrefCst.P_ENABLE_DECO_ICON)
		|| prop.equals(PrefCst.P_DECO_LABEL_BCOLOR)
		|| prop.equals(PrefCst.P_DECO_LABEL_FCOLOR)
		|| prop.equals(PrefCst.P_DECO_LABEL_FONT)
		|| prop.equals(PrefCst.P_ENABLE_DECO_FONT)
		|| prop.equals(PrefCst.P_DECO_LABEL_TEXT) ){
			Decorator.updateUI();
			Decorator.discardCacheUI();
		}

		Debug.dbg("Property ["+ prop +"] change: "
				+ event.getOldValue() + " -> " + event.getNewValue());
	}
}













