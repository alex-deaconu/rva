// Copyright © 2010 - May 2014 Rise Vision Incorporated.
// Use of this software is governed by the GPLv3 license
// (reproduced in the LICENSE file).

package com.risevision.ui.client.common.lists;

public interface SearchSortable {
	public boolean search(String query);
	public int compare(SearchSortable item, String column);
}
