package com.android.gers.shopping.list;

import android.view.View;
import android.widget.CompoundButton;

public interface ListViewStatusChangeListener {
	public void statusChanged(int listPosition, View triggeredView, boolean isOn);
}
