package com.android.gers.shopping.list;

import android.widget.CompoundButton;

public interface ListViewCheckBoxListener {
	public void checkBoxChanged(int listPosition, CompoundButton buttonView, boolean isChecked);
}
