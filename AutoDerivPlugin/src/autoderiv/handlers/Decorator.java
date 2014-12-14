package autoderiv.handlers;

import static autoderiv.Debug.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import autoderiv.Cst;

public class Decorator extends LabelProvider implements ILightweightLabelDecorator{

	private Color	foregroundColor;
	private Color	backgroundColor;

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

	/** Will decorate specified resource.
	 * @param ir IResource to decorate
	 * @param decoration IDecoration to edit */
	private void effectiveDecorate(IResource ir, IDecoration decoration) {
//		decoration.setForegroundColor(new Color(Display.getDefault(), 80, 80, 80));
		if(Cst.DECORATION_PREFIX_ENABLED)
			decoration.addPrefix(Cst.DECORATION_PREFIX);
		if(Cst.DECORATION_SUFFIX_ENABLED)
			decoration.addSuffix(Cst.DECORATION_SUFFIX);
		if(Cst.DECORATION_FOREGROUND_ENABLED){
			if(foregroundColor==null)
				foregroundColor = new Color(Display.getDefault(),
						Cst.DECORATION_FOREGROUND_R,
						Cst.DECORATION_FOREGROUND_G,
						Cst.DECORATION_FOREGROUND_B);
			decoration.setForegroundColor(foregroundColor);
		}
		if(Cst.DECORATION_BACKGROUND_ENABLED){
			if(backgroundColor==null)
				backgroundColor = new Color(Display.getDefault(),
						Cst.DECORATION_BACKGROUND_R,
						Cst.DECORATION_BACKGROUND_G,
						Cst.DECORATION_BACKGROUND_B);
			decoration.setBackgroundColor(backgroundColor);
		}
	}

}
