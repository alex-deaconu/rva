// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.ui.client.common.widgets.demographics;

import com.risevision.ui.client.common.widgets.RiseMultiSelectWidget;

public class ServicesProvidedWidget extends RiseMultiSelectWidget {
	public ServicesProvidedWidget() {
		super();
		
		loadData();
	}

	private void loadData() {
		addItem("Consulting", "consulting");
		addItem("Creative Design", "creativeDesign");
		addItem("Advertising Sales and Management", "advertising");
		addItem("Web Design and Development", "webDesign");
		addItem("Installation", "installation");
		addItem("Training", "training");
		addItem("Support", "support");
		addItem("Service", "service");
		addItem("Display Sales", "displaySales");
		addItem("Computer Sales", "computerSales");
		addOther();
	}
}