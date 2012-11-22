package com.android.gers.shopping.list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;

public class SimpleInputDialog {
	
	public interface DialogClickListener {
		void buttonClicked(int id, DialogInterface dialog, int whichButton);
	}
	
	public static AlertDialog SimpleInputDialogBuilder(Context context, final DialogClickListener listener, final int dialogId, String title, String message, View viewToUse) {
		AlertDialog.Builder alertBuilder = 
				new AlertDialog.Builder(context)
					.setTitle(title);
		
		if (message != null) {
			alertBuilder.setMessage(message);
		}
					
		alertBuilder.setView(viewToUse);
		
		alertBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listener.buttonClicked(dialogId, dialog, which);
			}
		});
		alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listener.buttonClicked(dialogId, dialog, which);
			}
		});
		
		AlertDialog dialog = alertBuilder.create();
		dialog.show();

		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		return dialog;
	}
	
}
