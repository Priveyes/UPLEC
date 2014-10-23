package com.uplec.electronics.utils;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;


/**
 * Shared preferences, stores user settings data
 * 
 * @author ihorkarpachev
 *
 */
@SharedPref(value = SharedPref.Scope.UNIQUE) public interface AppPref {

	// The field represents max value that user can input into edit text
	@DefaultInt(199) int maxValue();

	// The field writeAutomatically represents automatic writing the data when
	// discovery kit is close to device
	@DefaultBoolean(false) boolean writeAutomatically();
}
