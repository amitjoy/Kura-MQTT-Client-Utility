/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.amitinside.swt.layout.grid;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class GridLayoutUtil {

	private final GridLayout gridLayout;

	private GridLayoutUtil(GridLayout gridLayout) {
		this.gridLayout = gridLayout;
	}

	public static GridLayoutUtil applyGridLayout(Composite composite) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		composite.setLayout(gridLayout);
		return new GridLayoutUtil(gridLayout);
	}

	public static GridLayoutUtil onGridLayout(Composite composite) {
		final Layout layout = composite.getLayout();
		if (layout instanceof GridLayout) {
			return new GridLayoutUtil((GridLayout) layout);
		}
		throw new IllegalStateException(
				"Composite has to have a GridLayout. Has " + layout);
	}

	public GridLayoutUtil numColumns(int numColumns) {
		gridLayout.numColumns = numColumns;
		return this;
	}

	public GridLayoutUtil columnsEqualWidth(boolean columnsEqualWidth) {
		gridLayout.makeColumnsEqualWidth = columnsEqualWidth;
		return this;
	}

	public GridLayoutUtil horizontalSpacing(int horizontalSpacing) {
		gridLayout.horizontalSpacing = horizontalSpacing;
		return this;
	}

	public GridLayoutUtil verticalSpacing(int verticalSpacing) {
		gridLayout.verticalSpacing = verticalSpacing;
		return this;
	}

	public GridLayoutUtil marginWidth(int marginWidth) {
		gridLayout.marginWidth = marginWidth;
		return this;
	}

	public GridLayoutUtil marginHeight(int marginHeight) {
		gridLayout.marginHeight = marginHeight;
		return this;
	}

	public GridLayoutUtil marginTop(int marginTop) {
		gridLayout.marginTop = marginTop;
		return this;
	}

	public GridLayoutUtil marginBottom(int marginBottom) {
		gridLayout.marginBottom = marginBottom;
		return this;
	}

	public GridLayoutUtil marginLeft(int marginLeft) {
		gridLayout.marginLeft = marginLeft;
		return this;
	}

	public GridLayoutUtil marginRight(int marginRight) {
		gridLayout.marginRight = marginRight;
		return this;
	}

}
