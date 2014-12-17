package net.nodj.autoderivplugin.handlers;

import static net.nodj.autoderivplugin.Debug.*;
import org.eclipse.ui.IStartup;

/** Used so that the plugin auto starts.
 * Not sure if this is appropriate... */
public class StartupHandler implements IStartup {

	@Override
	public void earlyStartup() {
		// nothing to do here...
		info("StartupHandler.earlyStartup()");
	}

}
