package com.android.gers.shopping.list;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;


public class DialogValidator implements TextWatcher {

	private Button button;

	public DialogValidator(Button button) {
		this.button = button;
		this.button.setEnabled(false);
	}
	
	public void afterTextChanged(Editable s) {
		//nothing
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		//nothing
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() == 0) {
			button.setEnabled(false);
		} else {
			button.setEnabled(true);
		}
	}
	
}
