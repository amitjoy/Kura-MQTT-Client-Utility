package com.amitinside.swt.layout.grid;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

public class GridDataUtil {

	private final GridData gridData;

	private GridDataUtil(GridData gridData) {
		this.gridData = gridData;
	}

	public static GridDataUtil applyGridData(Control control) {
		final GridData gridData = new GridData();
		control.setLayoutData(gridData);
		return new GridDataUtil(gridData);
	}

	public static GridDataUtil onGridData(Control control) {
		final Object layoutData = control.getLayoutData();
		if (layoutData instanceof GridData) {
			return new GridDataUtil((GridData) layoutData);
		}
		throw new IllegalStateException(
				"Control must have GridData layout data. Has " + layoutData);
	}

	public GridDataUtil withHorizontalFill() {
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		return this;
	}

	public GridDataUtil withVerticalFill() {
		gridData.horizontalAlignment = SWT.TOP;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		return this;
	}

	public GridDataUtil withFill() {
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		return this;
	}

	public GridDataUtil withCenterCollapse() {
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = false;
		return this;
	}

	public GridDataUtil grabExcessHorizontalSpace(
			boolean grabExcessHorizontalSpace) {
		gridData.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
		return this;
	}

	public GridDataUtil grabExcessVerticalSpace(boolean grabExcessVerticalSpace) {
		gridData.grabExcessVerticalSpace = grabExcessVerticalSpace;
		return this;
	}

	public GridDataUtil horizontalSpan(int horizontalSpan) {
		gridData.horizontalSpan = horizontalSpan;
		return this;
	}

	public GridDataUtil verticalSpan(int verticalSpan) {
		gridData.verticalSpan = verticalSpan;
		return this;
	}

	public GridDataUtil minimumHeight(int minimumHeight) {
		gridData.minimumHeight = minimumHeight;
		return this;
	}

	public GridDataUtil minimumWidth(int minimumWidth) {
		gridData.minimumWidth = minimumWidth;
		return this;
	}

	public GridDataUtil verticalIndent(int verticalIndent) {
		gridData.verticalIndent = verticalIndent;
		return this;
	}

	public GridDataUtil horizontalIndent(int horizontalIndent) {
		gridData.horizontalIndent = horizontalIndent;
		return this;
	}

	public GridDataUtil heightHint(int heightHint) {
		gridData.heightHint = heightHint;
		return this;
	}

	public GridDataUtil widthHint(int widthHint) {
		gridData.widthHint = widthHint;
		return this;
	}

	public GridDataUtil verticalAlignment(int verticalAlignment) {
		gridData.verticalAlignment = verticalAlignment;
		return this;
	}

	public GridDataUtil horizontalAlignment(int horizontalAlignment) {
		gridData.horizontalAlignment = horizontalAlignment;
		return this;
	}

	public GridDataUtil exclude(boolean exclude) {
		gridData.exclude = exclude;
		return this;
	}

}
