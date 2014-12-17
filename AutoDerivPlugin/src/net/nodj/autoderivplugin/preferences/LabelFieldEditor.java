package net.nodj.autoderivplugin.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabelFieldEditor extends FieldEditor {

	private Label	l;

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

	public LabelFieldEditor(String text, Composite parent) {
		this(text, parent, false);
	}

	@Override protected void adjustForNumColumns(int numColumns) {
		((GridData) l.getLayoutData()).horizontalSpan = numColumns;
	}
	@Override protected void doFillIntoGrid(Composite parent, int numColumns) {
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		l.setLayoutData(gd);
	}
	@Override protected void doLoad() { }
	@Override protected void doLoadDefault() { }
	@Override protected void doStore() { }
	@Override public int getNumberOfControls() { return 0; }

}
