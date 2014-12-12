package autoderiv;

import static autoderiv.Debug.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import autoderiv.handlers.ChangeEventHandler;

/** The activator class controls the plug-in life cycle */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "AutoDeriv";

	// The shared instance
	ChangeEventHandler listener;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		info("AutoDeriv plugin starts");
		listener = new ChangeEventHandler();
		listener.startup();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);

		Debug.log = getLog();

		Debug.printLog = true;
		Debug.printDbg = true;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		info("AutoDeriv plugin stops");
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		super.stop(context);
	}
}
