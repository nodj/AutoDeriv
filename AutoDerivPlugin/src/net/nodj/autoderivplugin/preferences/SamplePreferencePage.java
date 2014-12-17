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

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
// persisted in .metadata\.plugins\org.eclipse.core.runtime\.settings\net.nodj.AutoDerivPlugin.prefs
public class SamplePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SamplePreferencePage() {
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

		addField(new LabelFieldEditor("Startup", parent, true));
		addField(new BooleanFieldEditor(PrefCst.P_STARTUP_CHECK, 		"check resources derived state at startup", parent));

		addField(new LabelFieldEditor("Trace", parent, true));
		addField(new BooleanFieldEditor(PrefCst.P_OUTPUT_LOG, 			"output in the .log file (in workspace/.metadata/)", parent));
		addField(new BooleanFieldEditor(PrefCst.P_OUTPUT_STD, 			"output in the standard output streams (console)", parent));
		addField(new BooleanFieldEditor(PrefCst.P_TRACE_WARN, 			"trace warnings (recommended)", parent));
		addField(new BooleanFieldEditor(PrefCst.P_TRACE_INFO, 			"trace info", parent));
		addField(new BooleanFieldEditor(PrefCst.P_TRACE_DEBUG, 			"trace debug", parent));

		addField(new LabelFieldEditor("Decoration - label", parent, true));
		addField(new LabelFieldEditor("NOTE: decorations may be disabled at workspace level,", parent));
		addField(new LabelFieldEditor("see General > Appearance > Label Decorations", parent));
		addField(new BooleanFieldEditor(PrefCst.P_DECO_LABEL_TEXT, 		"affect the label of derived resources", parent));
		addField(new StringFieldEditor( PrefCst.P_DECO_PREFIX, 			"label prefix", parent));
		addField(new StringFieldEditor( PrefCst.P_DECO_SUFFIX, 			"label suffix", parent));
		addField(new BooleanFieldEditor(PrefCst.P_DECO_LABEL_FCOLOR, 	"override foreground color", parent));
		addField(new ColorFieldEditor(  PrefCst.P_DECO_FOREGROUND, 		"label foreground", parent));
		addField(new BooleanFieldEditor(PrefCst.P_DECO_LABEL_BCOLOR, 	"override background color", parent));
		addField(new ColorFieldEditor(  PrefCst.P_DECO_BACKGROUND, 		"label background", parent));
		addField(new BooleanFieldEditor(PrefCst.P_ENABLE_DECO_FONT, 	"override font", parent));
		addField(new FontFieldEditor(   PrefCst.P_DECO_LABEL_FONT, 		"derived resources font", parent));

		addField(new LabelFieldEditor("Decoration - icon", parent, true));
		addField(new BooleanFieldEditor(PrefCst.P_ENABLE_DECO_ICON, 	"show an icon on derived resources", parent));
		String[][] comboLocation = {
				{"top left", "TOP_LEFT"}, {"top right", "TOP_RIGHT"},
				{"bottom left", "BOTTOM_LEFT"}, {"bottom right", "BOTTOM_RIGHT"},
		};
		addField(new RadioGroupFieldEditor( PrefCst.P_DECO_ICON_LOC, "decoration icon location", 2,comboLocation, parent, true));
	}

	public void init(IWorkbench workbench) {
	}

}