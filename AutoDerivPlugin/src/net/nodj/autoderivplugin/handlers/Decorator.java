package net.nodj.autoderivplugin.handlers;

import static net.nodj.autoderivplugin.Debug.*;
import net.nodj.autoderivplugin.Conf;
import net.nodj.autoderivplugin.Cst;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**This class handles the resource decoration.
 * Derived resources are decorated
 * conf file (.derived file) are differently decorated too. */
public class Decorator extends LabelProvider implements ILightweightLabelDecorator{
	/** mini icon (8x8) located on one corner of a derived resource */
	private static ImageDescriptor derivedIcon;

	/** icon (16x16) which replaces the icon of an active .derived conf file */
	private static ImageDescriptor confFileIcon;

	private static IDecorationContext defaultContext;

	static{
		defaultContext = DecorationContext.DEFAULT_CONTEXT;
		allowReplace(defaultContext);
	}

	// label decoration attributes
	private static Color	foregroundColor;
	private static Color	backgroundColor;
	private static Font		font;

	/**Called automagically by Eclipse.
	 * I use this fct to filter the resources we decide to decorate or not.
	 * see effectiveDecorate method, which effectively affect the IDecoration */
	@Override
	public void decorate(Object element, IDecoration decoration) {
		/* we only decorate IResources (IFile / IFolder mainly) but some awkward
		 * classes are not in the IResource object tree, but are convertible
		 * into IResources...
		 * This happens for CContainers which are like folder in the CDT model.
		 * This block tries to decorate these resources. The catch block occurs
		 * when the element is neither in the IResource tree, nor convertible.
		 */
		if(!(element instanceof IResource)){
			if (!(element instanceof PlatformObject)) return;

			// try the conversion thing for PlateformObject things
			try {
				IResource ir = (IResource) ((PlatformObject) element).getAdapter(IResource.class);
				if (ir != null && ir.isDerived())
					effectiveDecorateDerived(ir, decoration);
			} catch (Exception e) {
				info("element " + element.toString() + " not usable. "
						+ "Type is " + element.getClass().getCanonicalName());
			}
			return;
		}

		// easy case. Item is an IResource child.
		IResource objectResource = (IResource) element;

		if(FilterManager.isActiveConfFile(objectResource))
			effectiveDecorateConfFile(objectResource, decoration);

		if(objectResource.isDerived())
			effectiveDecorateDerived(objectResource, decoration);
	}


	private static void loadIcons() {
		int hueOffset = (int) (Conf.DECO_ICON_COLOR.getHSB()[0]+360-83);
		hueOffset %= 360;
		derivedIcon = loadIcon("derived.png", hueOffset);
		confFileIcon = loadIcon("conffile.png", hueOffset);
	}


	/** Try to force a DecorationContext to accept the REPLACE decoration method */
	private static void allowReplace(IDecorationContext context) {
		DecorationContext dcontext = (DecorationContext) context;
		Object propertyValue = dcontext.getProperty(IDecoration.ENABLE_REPLACE);
		boolean add = (propertyValue==null);
		if(!add)
			if(propertyValue instanceof Boolean)
				add = (!(Boolean)propertyValue);
		if(add)
			dcontext.putProperty(IDecoration.ENABLE_REPLACE, Boolean.TRUE);
	}


	/** load an icon from the icons folder */
	public static ImageDescriptor loadIcon(String filename) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Cst.PLUGIN_ID, "icons/" + filename);
	}


	/** change the icon hue */
	public static ImageDescriptor loadIcon(String filename, int hueOffset) {
		hueOffset = (int) ((Conf.DECO_ICON_COLOR.getHSB()[0]+360-83)%360);
		ImageDescriptor normal = loadIcon(filename);
		if(hueOffset==0) return normal;
		ImageData data = normal.getImageData();
		PaletteData pal = data.palette;
		RGB rgb = new RGB(0,0,0);

		for (int y = 0; y < data.height; y++) {
			for (int x = 0; x < data.width; x++) {
				int p = data.getPixel(x, y);
				rgb.red = (p & pal.redMask) >> -pal.redShift;
				rgb.green = (p & pal.greenMask) >> -pal.greenShift;
				rgb.blue = (p & pal.blueMask) >> -pal.blueShift;
				float[] hsb = rgb.getHSB();
				rgb = new RGB((hsb[0] + hueOffset) % 360, hsb[1], hsb[2]);
				p = (rgb.red << -pal.redShift) + (rgb.green << -pal.greenShift) + (rgb.blue << -pal.blueShift);
				data.setPixel(x, y, p);
			}
		}
		return ImageDescriptor.createFromImageData(data);
	}


	/** Will decorate specified resource.
	 * @param ir IResource to decorate
	 * @param decoration IDecoration to edit */
	private void effectiveDecorateDerived(IResource ir, IDecoration decoration) {
		if(Conf.DECO_LABEL_TEXT){
			if(Conf.DECO_PREFIX!=null)
				decoration.addPrefix(Conf.DECO_PREFIX);
			if(Conf.DECO_SUFFIX!=null)
				decoration.addSuffix(Conf.DECO_SUFFIX);
		}

		if(Conf.DECO_LABEL_FCOLOR){
			if(foregroundColor==null)
				foregroundColor = new Color(Display.getDefault(), Conf.DECO_FOREGROUND);
			decoration.setForegroundColor(foregroundColor);
		}

		if(Conf.DECO_LABEL_BCOLOR){
			if(backgroundColor==null)
				backgroundColor = new Color(Display.getDefault(), Conf.DECO_BACKGROUND);
			decoration.setBackgroundColor(backgroundColor);
		}

		if(Conf.DECO_FONT_ENABLED){
			if(font==null)
				font = new Font(Display.getDefault(), Conf.DECO_FONT_DATA);
			decoration.setFont(font);
		}

		if(Conf.DECO_ICON_ENABLED){
			if(derivedIcon==null)
				loadIcons();
			decoration.addOverlay(derivedIcon, Conf.DECO_ICON_LOC);
		}
	}


	/** decorates conf files */
	private void effectiveDecorateConfFile(IResource objectResource, IDecoration decoration) {
		if(!Conf.DECO_CONF_ENABLED)
			return;

		IDecorationContext context = decoration.getDecorationContext();
		if(context != defaultContext)
			allowReplace(context);

		if(confFileIcon==null)
			loadIcons();
		decoration.addOverlay(confFileIcon, IDecoration.REPLACE);
	}


	/** handle re-decoration of the whole workspace */
	public static void updateUI() {
		// Decorate using current UI thread
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				PlatformUI.getWorkbench().getDecoratorManager().update("net.nodj.autoderivplugin.handlers.Decorator");
			}
		});
	}

	/** Called when some parameters are modified by the user in order to make
	 * changes active asap */
	public static void discardCacheUI() {
		foregroundColor = null;
		backgroundColor = null;
		font = null;
		confFileIcon = null;
		derivedIcon = null;
	}

}
