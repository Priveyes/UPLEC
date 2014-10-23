package com.uplec.electronics.views;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplec.electronics.R;


@EViewGroup(R.layout.dialog_explanation_steps) public class CDialogSteps extends LinearLayout {

	// ========================================== VIEWS
	@ViewById public TextView	tvSteps;

	// ================================= VARIABLES

	public CDialogSteps(Context context) {
		super(context);
	}

}
