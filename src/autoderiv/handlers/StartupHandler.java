package autoderiv.handlers;

import org.eclipse.ui.IStartup;

public class StartupHandler implements IStartup {

	@Override
	public void earlyStartup() {
// nothing to do here...
		System.out.println("StartupHandler.earlyStartup()");
	}

}
