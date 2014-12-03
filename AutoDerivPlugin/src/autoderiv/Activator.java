package autoderiv;

import static autoderiv.Debug.*;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import autoderiv.handlers.ChangeEventHandler;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "AutoDeriv"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	private IWorkspace workspace;
	ChangeEventHandler listener;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		info("Activator.start()");
		workspace = ResourcesPlugin.getWorkspace();
		listener = new ChangeEventHandler();
		listener.startup();
		workspace.addResourceChangeListener(listener);

		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		workspace.addResourceChangeListener(listener);
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() { return plugin; }

	/**Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 * @param path the path
	 * @return the image descriptor */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
