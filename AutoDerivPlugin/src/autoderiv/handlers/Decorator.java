package autoderiv.handlers;

import static autoderiv.Debug.*;
import java.util.Set;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

public class Decorator extends LabelProvider implements ILightweightLabelDecorator{

	private static final String	suffix = " <derived>";	// todo used defined
	private static boolean	once = false;

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
		if(once){
			once = true;
			ColorRegistry cr = JFaceResources.getColorRegistry();
			Set<String> ks = cr.getKeySet();
			for(String s:ks){
				System.out.println("Decorator.effectiveDecorate() s="+s);
			}
		}
//		decoration.setForegroundColor(new Color(Display.getDefault(), 80, 80, 80));
		decoration.addSuffix(suffix);
	}

}
