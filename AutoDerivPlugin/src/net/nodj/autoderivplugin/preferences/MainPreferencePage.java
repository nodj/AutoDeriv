package net.nodj.autoderivplugin.preferences;

import net.nodj.autoderivplugin.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

// persisted in .metadata\.plugins\org.eclipse.core.runtime\.settings\net.nodj.AutoDerivPlugin.prefs
public class MainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("AutoDeriv plugin preference page");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		addField(new LabelFieldEditor  ("General", parent, true));
		addField(new BooleanFieldEditor(PrefCst.P_STARTUP_CHECK, 		"Check resources derived state at startup", parent));
		addField(new BooleanFieldEditor(PrefCst.P_ENABLE_MARKER, 		"Add markers in .derived file (per-rule info or warn)", parent));

		addField(new LabelFieldEditor  ("Trace", parent, true));
		addField(new BooleanFieldEditor(PrefCst.P_OUTPUT_LOG, 			"Output in the .log file (in workspace/.metadata/)", parent));
		addField(new BooleanFieldEditor(PrefCst.P_OUTPUT_STD, 			"Output in the standard output streams (console)", parent));
		addField(new BooleanFieldEditor(PrefCst.P_TRACE_WARN, 			"Trace warnings (recommended)", parent));
		addField(new BooleanFieldEditor(PrefCst.P_TRACE_INFO, 			"Trace info", parent));
		addField(new BooleanFieldEditor(PrefCst.P_TRACE_DEBUG, 			"Trace debug", parent));

		addField(new LabelFieldEditor  ("Decoration - Label", parent, true));
		addField(new LabelFieldEditor  ("NOTE: decorations may be disabled at workspace level,", parent));
		addField(new LabelFieldEditor  ("See General > Appearance > Label Decorations", parent));
		addField(new BooleanFieldEditor(PrefCst.P_DECO_LABEL_TEXT, 		"Affect the label of derived resources", parent));
		addField(new StringFieldEditor (PrefCst.P_DECO_PREFIX, 			"Label prefix", parent));
		addField(new StringFieldEditor (PrefCst.P_DECO_SUFFIX, 			"Label suffix", parent));
		addField(new BooleanFieldEditor(PrefCst.P_DECO_LABEL_FCOLOR, 	"Override foreground color", parent));
		addField(new ColorFieldEditor  (PrefCst.P_DECO_FOREGROUND, 		"Label foreground", parent));
		addField(new BooleanFieldEditor(PrefCst.P_DECO_LABEL_BCOLOR, 	"Override background color", parent));
		addField(new ColorFieldEditor  (PrefCst.P_DECO_BACKGROUND, 		"Label background", parent));
		addField(new BooleanFieldEditor(PrefCst.P_ENABLE_DECO_FONT, 	"Override font", parent));
		addField(new FontFieldEditor   (PrefCst.P_DECO_LABEL_FONT, 		"Derived resources font", parent));

		addField(new LabelFieldEditor("Decoration - Icon", parent, true));
		addField(new ColorFieldEditor  (PrefCst.P_ICON_COLOR, 			"Icon color (hue only)", parent));
		addField(new BooleanFieldEditor(PrefCst.P_ENABLE_CONF_ICON, 	"Replace icon of .derived conf file", parent));
		addField(new BooleanFieldEditor(PrefCst.P_ENABLE_DECO_ICON, 	"Show an icon on derived resources", parent));
		String[][] comboLocation = {
			{"top left", 	"TOP_LEFT"}, 	{"top right", 	"TOP_RIGHT"},
			{"bottom left", "BOTTOM_LEFT"}, {"bottom right","BOTTOM_RIGHT"},
		};
		addField(new RadioGroupFieldEditor( PrefCst.P_DECO_ICON_LOC, "Decoration icon location", 2,comboLocation, parent, true));
	}

	public void init(IWorkbench workbench){}
}
