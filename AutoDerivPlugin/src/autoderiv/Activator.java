package autoderiv;

import static autoderiv.Debug.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import autoderiv.handlers.ChangeEventHandler;

/** The activator class controls the plug-in life cycle */
public class Activator extends AbstractUIPlugin {
	// The shared instance
	ChangeEventHandler listener;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Debug.log = getLog();
		info("AutoDeriv plugin starts");

		// it about the plugin configuration itself
		Cst.readConf();

		// launch the listener, + ini
		listener = new ChangeEventHandler();
		listener.startup();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		info("AutoDeriv plugin stops");
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		super.stop(context);
	}

}