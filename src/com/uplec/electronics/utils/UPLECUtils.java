package com.uplec.electronics.utils;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.uplec.electronics.BuildConfig;
import com.uplec.electronics.constants.GlobalConstatns;
import com.uplec.electronics.core.DataDevice;
import com.uplec.electronics.core.Helper;
import com.uplec.electronics.views.CToastView;
import com.uplec.electronics.views.CToastView_;


public class UPLECUtils {

	/**
	 * Log string into logcat (for debug purposes)
	 * 
	 * @param message
	 *            string representation of message to display
	 */
	public static void log(String message) {
		if (BuildConfig.DEBUG) {
			Log.w(GlobalConstatns.LOG_TAG, null != message ? message : "message null");
		}
	}

	/**
	 * Log string into logcat (for debug purposes)
	 * 
	 * @param message
	 *            integer representation of message to display
	 */
	public static void log(int message) {
		if (BuildConfig.DEBUG) {
			Log.w(GlobalConstatns.LOG_TAG, String.valueOf(message));
		}
	}

	/**
	 * Convert bytes array to binary string representation
	 * 
	 * @param bytes
	 *            to be converted
	 * @return string format of binary array
	 */
	public static String toBinary(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
		for (int i = 0; i < Byte.SIZE * bytes.length; i++) {
			sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
		}
		return sb.toString();
	}

	/**
	 * Converts string representation of binary array to bytes array
	 * 
	 * @param s string to be converted
	 * @return byte array
	 */
	public static byte[] fromBinary(String s) {
		int sLen = s.length();
		byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
		char c;
		for (int i = 0; i < sLen; i++) {
			if ((c = s.charAt(i)) == '1') {
				toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
			} else if (c != '0') {
				throw new IllegalArgumentException();
			}
		}
		return toReturn;
	}

	/**
	 * Display custom toast
	 * 
	 * @param activity
	 *            current context
	 * @param message
	 *            string to be displayed
	 * @param imageResourcesId
	 *            image id to display as toast image
	 */
	public static void showCustomToast(final Activity activity, final String message, final int imageResourcesId) {
		activity.runOnUiThread(new Runnable() {

			@Override public void run() {
				try {
					CToastView view = CToastView_.build(activity);
					view.tvText.setText(message);
					view.ivImage.setImageResource(imageResourcesId);
					Toast toast = new Toast(activity.getApplicationContext());
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setView(view);
					toast.show();
					view.tvText.startAnimation(AnimationUtils.loadAnimation(activity, android.R.anim.slide_in_left));
				}
				catch (Exception e) {
					UPLECUtils.log(e.getMessage());
					UPLECUtils.showToast(activity.getApplicationContext(), message);
				}
			}
		});
	}

	/**
	 * Show system toast message
	 * 
	 * @param applicationContext
	 *            current context
	 * @param message
	 *            string to display
	 */
	public static void showToast(Context applicationContext, String message) {
		if (null != applicationContext) {
			Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show();
		} else {
			log(message);
		}
	}

	/**
	 * For user convenience set cursor to the last position, after text in the EditText
	 * 
	 * @param et
	 *            EditText view to set selection in
	 */
	public static void setSelectionAfterText(EditText et) {
		if ((null != et) && (!TextUtils.isEmpty(et.getText().toString()))) {
			try {
				Selection.setSelection(et.getText(), et.getText().toString().length());
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * the function Decode the tag answer for the GetSystemInfo command
	 * the function fills the values (dsfid / afi / memory size / icRef /..)
	 * in the myApplication class. return true if everything is OK.
	 */
	public static boolean decodeSystemInfoResponse(byte[] systemInfoResponse, DataDevice ma) {
		// if the tag has returned a good response
		if (systemInfoResponse[0] == (byte) 0x00 && systemInfoResponse.length >= 12) {
			String uidToString = "";
			byte[] uid = new byte[8];
			// change uid format from byteArray to a String
			for (int i = 1; i <= 8; i++) {
				uid[i - 1] = systemInfoResponse[10 - i];
				uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
			}

			// ***** TECHNO ******
			ma.setUid(uidToString);
			if (uid[0] == (byte) 0xE0)
				ma.setTechno("ISO 15693");
			else if (uid[0] == (byte) 0xD0)
				ma.setTechno("ISO 14443");
			else
				ma.setTechno("Unknown techno");

			// ***** MANUFACTURER ****
			if (uid[1] == (byte) 0x02)
				ma.setManufacturer("STMicroelectronics");
			else if (uid[1] == (byte) 0x04)
				ma.setManufacturer("NXP");
			else if (uid[1] == (byte) 0x07)
				ma.setManufacturer("Texas Instrument");
			else
				ma.setManufacturer("Unknown manufacturer");

			// **** PRODUCT NAME *****
			if (uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07) {
				ma.setProductName("LRI512");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17) {
				ma.setProductName("LRI64");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23) {
				ma.setProductName("LRI2K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B) {
				ma.setProductName("LRIS2K");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F) {
				ma.setProductName("M24LR64");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43) {
				ma.setProductName("LRI1K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47) {
				ma.setProductName("LRIS64K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B) {
				ma.setProductName("M24LR01E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F) {
				ma.setProductName("M24LR16E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false) return false;
			} else if (uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53) {
				ma.setProductName("M24LR02E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57) {
				ma.setProductName("M24LR32E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false) return false;
			} else if (uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B) {
				ma.setProductName("M24LR04E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F) {
				ma.setProductName("M24LR64E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false) return false;
			} else if (uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63) {
				ma.setProductName("M24LR08E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67) {
				ma.setProductName("M24LR128E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false) return false;
			} else if (uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F) {
				ma.setProductName("M24LR256E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false) return false;
			} else if (uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB) {
				ma.setProductName("detected product");
				ma.setBasedOnTwoBytesAddress(true);
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else {
				ma.setProductName("Unknown product");
				ma.setBasedOnTwoBytesAddress(false);
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			}

			// *** DSFID ***
			ma.setDsfid(Helper.ConvertHexByteToString(systemInfoResponse[10]));

			// *** AFI ***
			ma.setAfi(Helper.ConvertHexByteToString(systemInfoResponse[11]));

			// *** MEMORY SIZE ***
			if (ma.isBasedOnTwoBytesAddress()) {
				String temp = new String();
				temp += Helper.ConvertHexByteToString(systemInfoResponse[13]);
				temp += Helper.ConvertHexByteToString(systemInfoResponse[12]);
				ma.setMemorySize(temp);
			} else
				ma.setMemorySize(Helper.ConvertHexByteToString(systemInfoResponse[12]));

			// *** BLOCK SIZE ***
			if (ma.isBasedOnTwoBytesAddress())
				ma.setBlockSize(Helper.ConvertHexByteToString(systemInfoResponse[14]));
			else
				ma.setBlockSize(Helper.ConvertHexByteToString(systemInfoResponse[13]));

			// *** IC REFERENCE ***
			if (ma.isBasedOnTwoBytesAddress())
				ma.setIcReference(Helper.ConvertHexByteToString(systemInfoResponse[15]));
			else
				ma.setIcReference(Helper.ConvertHexByteToString(systemInfoResponse[14]));

			return true;
		}

		// if the tag has returned an error code
		else
			return false;
	}

	/**
	 * Class represents font cache, helps to prevent memory leak
	 * 
	 * @author Karpachev Ihor
	 */
	public static class FontCache {

		/**
		 * Roboto and digit typefaces name
		 */
		public static final String					ROBOTO_BOLD			= "roboto_bold.ttf";
		public static final String					ROBOTO_THIN_ITALIC	= "roboto_thin_italic.ttf";
		public static final String					DIGITAL_MONO_ITALIC	= "digital_mono_italic.ttf";
		/**
		 * Caching fonts (loading typeface from assets is slow operation)
		 */
		private static Hashtable<String, Typeface>	fontCache			= new Hashtable<String, Typeface>();

		public static Typeface get(String name, Context context) {
			Typeface tf = fontCache.get(name);
			if (tf == null) {
				try {
					tf = Typeface.createFromAsset(context.getAssets(), name);
				}
				catch (Exception e) {
					return null;
				}
				fontCache.put(name, tf);
			}
			return tf;
		}
	}
}
