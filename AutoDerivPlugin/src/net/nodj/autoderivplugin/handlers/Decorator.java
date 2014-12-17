package net.nodj.autoderivplugin.handlers;

import static net.nodj.autoderivplugin.Debug.*;
import net.nodj.autoderivplugin.Conf;
import net.nodj.autoderivplugin.Cst;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Decorator extends LabelProvider implements ILightweightLabelDecorator{
	private static ImageDescriptor derivedIcon;

	static{
		loadIcon("gray");
	}

	private static Color	foregroundColor;
	private static Color	backgroundColor;
	private static Font		font;

	/**Called automagically by Eclipse.
	 * I use this fct to filter the resources we decide to decorate or not.
	 * see effectiveDecorate method, which effectively affect the IDecoration
	 */
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
					effectiveDecorate(ir, decoration);
			} catch (Exception e) {
				info("element " + element.toString() + " not usable. "
						+ "Type is " + element.getClass().getCanonicalName());
			}
			return;
		}

		// easy case. Item is an IResource child.
		IResource objectResource = (IResource) element;
		if(objectResource.isDerived()){
			effectiveDecorate(objectResource, decoration);
		}
	}


	public static void loadIcon(String variation) {
		ImageDescriptor newDerivedIcon = AbstractUIPlugin.imageDescriptorFromPlugin(Cst.PLUGIN_ID, "icons/d8/"+variation+".png");
		if(newDerivedIcon!=null)
			derivedIcon = newDerivedIcon;
	}


	/** Will decorate specified resource.
	 * @param ir IResource to decorate
	 * @param decoration IDecoration to edit */
	private void effectiveDecorate(IResource ir, IDecoration decoration) {
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

		if(Conf.DECO_ICON_ENABLED)
			decoration.addOverlay(derivedIcon, Conf.DECO_ICON_LOC);
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


	public static void discardCacheUI() {
		foregroundColor = null;
		backgroundColor = null;
		font = null;
	}

}
