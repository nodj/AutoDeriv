package autoderiv.handlers;

import static autoderiv.Debug.*;
import org.eclipse.ui.IStartup;

public class StartupHandler implements IStartup {

	@Override
	public void earlyStartup() {
		// nothing to do here...
		info("StartupHandler.earlyStartup()");
	}

}
