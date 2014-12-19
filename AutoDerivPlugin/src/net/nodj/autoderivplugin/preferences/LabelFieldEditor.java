package net.nodj.autoderivplugin.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/** This class is used to add simple text with no user interaction in a
 * preference page which extends FieldEditorPreferencePage.
 * The text may be bold or not, there is not much to see here. */
public class LabelFieldEditor extends FieldEditor {

	private Label l; // the swt widget

	/**Creates a new line of simple text
	 * @param bold simple formatting option, bold is nice for titles... */
	public LabelFieldEditor(String text, Composite parent, boolean bold) {
		init("", text);
		l = new Label(parent, 0);
		l.setText(text);
		Font font  = parent.getFont();
		if(bold){
			FontData parentFontData = font.getFontData()[0];
			parentFontData.setStyle(SWT.BOLD);
			font = new Font(parent.getFont().getDevice(), parentFontData);
		}
		l.setFont(font);
		createControl(parent);
	}

	/** for simple text without formating */
	public LabelFieldEditor(String text, Composite parent)
	{ this(text, parent, false); }


	// the 2 following methods are used to have a correct resize reaction
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) l.getLayoutData()).horizontalSpan = numColumns;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		l.setLayoutData(gd);
	}

	// following methods are used for persistence matter. We don't care about that for a simple label
	@Override protected void doLoad() { }
	@Override protected void doLoadDefault() { }
	@Override protected void doStore() { }
	@Override public int getNumberOfControls() { return 0; }

}
