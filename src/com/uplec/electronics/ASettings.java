package com.uplec.electronics;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.uplec.electronics.utils.AppPref_;
import com.uplec.electronics.utils.UPLECUtils;


/**
 * Class represents user settings
 * 
 * @author ihorkarpachev
 */
@EActivity(R.layout.activity_settings) public class ASettings extends Activity {

	// ========================================== VIEWS
	@ViewById EditText	etMaxNumber;
	@ViewById CheckBox	cbWriteAutomatically;
	@ViewById TextView	tvOk, tvCancel;
	@Pref AppPref_		appPref;

	@AfterViews void afterViews() {
		etMaxNumber.setText(String.valueOf(appPref.maxValue().get()));
		cbWriteAutomatically.setChecked(appPref.writeAutomatically().get());
		UPLECUtils.setSelectionAfterText(etMaxNumber);
	}

	/**
	 * Handle cancel click
	 */
	@Click void tvCancel() {
		onBackPressed();
	}

	/**
	 * OK click, save settings
	 */
	@Click void tvOk() {
		if (!TextUtils.isEmpty(etMaxNumber.getText().toString())) {
			try {
				// save maximum number, and write automatically value
				appPref.edit().writeAutomatically().put(cbWriteAutomatically.isChecked()).maxValue().put(Integer.valueOf(etMaxNumber.getText().toString())).apply();
				tvCancel.performClick();
			}
			catch (Exception ex) {
				ex.printStackTrace();
				UPLECUtils.showCustomToast(ASettings.this, "Error occured, incorrect number value", R.drawable.error);
			}
		}
	}
}