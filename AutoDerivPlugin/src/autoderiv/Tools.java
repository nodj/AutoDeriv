package autoderiv;

import static autoderiv.Cst.*;
import static autoderiv.Debug.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/** Useful things can goes here... */
public class Tools {

	// timing functions
	private static final long startns = System.nanoTime();
	public static double 	getmsd(){ return getns()*1e-6;}
	public static long 		getms(){ return (long)getmsd();}
	public static long 		getns()	{ return System.nanoTime()-startns; }


	// resource setting functions


	/** This plugin main task is to affect the 'derived' attribute. We should do
	 * that correctly. As a setDerived() call will fire a change event, we try
	 * to do this only if necessary. That the point of this checked version. */
	public static void checkedSetDerived(IResource res, boolean derived, IProgressMonitor progress) throws CoreException {
		if(res.isDerived() != derived)
			res.setDerived(derived, progress);
	}


	/**Same as the previous call, but handle the possible exception with a no-op reaction. */
	public static void checkedSetDerived_nt(IResource res, boolean derived, IProgressMonitor progress){
		if(res.isDerived() != derived){
			try {
				res.setDerived(derived, progress);
			} catch (CoreException e) { e.printStackTrace(); }
		}
	}


	public static void readConf() {
		File f = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".metadata").append(PLUGIN_CONF_FILE_NAME).toFile();
		if(f==null || !f.exists()){
			f = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(PLUGIN_CONF_FILE_NAME).toFile();
			if(f==null || !f.exists())
				return;
		}

		dbg("found plugin conf file at "+f.getAbsolutePath());

		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		try {
			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null){
				try {
					parseConfLine(line, ++i);
				} catch (Exception e) {
					warn("bad pluging conf parsing at line "+i);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		DECORATION_FOREGROUND_ENABLED =
				( DECORATION_FOREGROUND_R>0
				& DECORATION_FOREGROUND_G>0
				& DECORATION_FOREGROUND_B>0);

		DECORATION_BACKGROUND_ENABLED =
				( DECORATION_BACKGROUND_R > 0
				& DECORATION_BACKGROUND_G > 0
				& DECORATION_BACKGROUND_B > 0);

	}


	private static void parseConfLine(String line, int i) throws NumberFormatException {
		// filter out comments (after #char)
		int commentLocation = line.indexOf('#');
		if(commentLocation != -1)
			line = line.substring(0, commentLocation);
		if(line.trim().isEmpty()) return; // comments

		dbg("parse line ["+i+"]: "+ line);

		// check that its an affectation line
		String[] a = line.split("=");
		if(a.length==2){
			String key = a[0].trim();
			String val = a[1];
			String valt = a[1].trim();
			dbg("parsed: [" + key +"] = "+ valt);
			if(key.equals(PLUGIN_ID+TRACE_LOG_STR))
				Cst.TRACE_LOG = "true".equals(valt);
			if(key.equals(PLUGIN_ID+TRACE_STD_STR))
				Cst.TRACE_STD = "true".equals(valt);
			else if(key.equals(PLUGIN_ID+TRACE_DBG_STR))
				Cst.ENABLE_DBG = "true".equals(valt);
			else if(key.equals(PLUGIN_ID+TRACE_INFO_STR))
				Cst.ENABLE_INFO = "true".equals(valt);
			else if(key.equals(PLUGIN_ID+TRACE_WARN_STR))
				Cst.ENABLE_WARN = "true".equals(valt);


			else if(key.equals(PLUGIN_ID+DECORATION_SUFFIX_STR)){
				DECORATION_SUFFIX_ENABLED = true;
				DECORATION_SUFFIX = val;
			}
			else if(key.equals(PLUGIN_ID+DECORATION_PREFIX_STR)){
				DECORATION_PREFIX_ENABLED = true;
				DECORATION_PREFIX = val;
			}

			else if(key.equals(PLUGIN_ID+DECORATION_FOREGROUND_R_STR))
				DECORATION_FOREGROUND_R = Integer.parseInt(valt);
			else if(key.equals(PLUGIN_ID+DECORATION_FOREGROUND_G_STR))
				DECORATION_FOREGROUND_G = Integer.parseInt(valt);
			else if(key.equals(PLUGIN_ID+DECORATION_FOREGROUND_B_STR))
				DECORATION_FOREGROUND_B = Integer.parseInt(valt);

			else if(key.equals(PLUGIN_ID+DECORATION_BACKGROUND_R_STR))
				DECORATION_BACKGROUND_R = Integer.parseInt(valt);
			else if(key.equals(PLUGIN_ID+DECORATION_BACKGROUND_G_STR))
				DECORATION_BACKGROUND_G = Integer.parseInt(valt);
			else if(key.equals(PLUGIN_ID+DECORATION_BACKGROUND_B_STR))
				DECORATION_BACKGROUND_B = Integer.parseInt(valt);
			else
				warn("key not recognized at line: " + i);
		}
		else if(a.length==1){
			// it like an affectation to nothing
			String key = a[0].trim();

			if(key.equals(PLUGIN_ID+DECORATION_PREFIX_STR))
				DECORATION_PREFIX_ENABLED = false;
			else if(key.equals(PLUGIN_ID+DECORATION_SUFFIX_STR))
				DECORATION_SUFFIX_ENABLED = false;
			else
				warn("key not affected correctly: " + i);
		}
		else{
			warn("bad line: " + i);
		}
	}

}
