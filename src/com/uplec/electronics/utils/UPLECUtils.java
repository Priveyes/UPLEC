package com.uplec.electronics.utils;

import java.util.Hashtable;
import java.util.LinkedList;

import javax.xml.datatype.DatatypeFactory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.uplec.electronics.BuildConfig;
import com.uplec.electronics.constants.GlobalConstatns;
import com.uplec.electronics.constants.GlobalConstatns.LEFT_DIGITS_PATTERN;
import com.uplec.electronics.constants.GlobalConstatns.MIDDLE_DIGITS_PATTERN;
import com.uplec.electronics.constants.GlobalConstatns.RIGHT_DIGITS_PATTERN;
import com.uplec.electronics.core.DataDevice;
import com.uplec.electronics.core.Helper;
import com.uplec.electronics.views.CDialogSteps;
import com.uplec.electronics.views.CDialogSteps_;
import com.uplec.electronics.views.CToastView;
import com.uplec.electronics.views.CToastView_;


public class UPLECUtils {

	public static void displayStepsInDialog(String stepsMessage, Activity activity) {
		CDialogSteps view = CDialogSteps_.build(activity);
		view.tvSteps.setText(stepsMessage);
		Dialog d = new Dialog(activity);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.setContentView(view);
		d.show();
	}

	public static String bytesToHex(byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		String result = new String(hexChars);

		String humanReadableString = "";
		for (int i = 0; i < result.length(); i++) {
			humanReadableString += result.toCharArray()[i];
			if ((i + 1) % 2 == 0) {
				humanReadableString += "  ";
			}
		}
		return humanReadableString;
	}

	public static final String formatNumberToBytePattern(int number) {
		String result = "STEP 1 (read from NFC Tag)\n\n";

		byte[] response = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F };

		byte[] word1 = { response[0], response[1], response[2], response[3] };
		byte[] word2 = { response[4], response[5], response[6], response[7] };
		byte[] word3 = { response[8], response[9], response[10], response[11] };
		byte[] word4 = { response[12], response[13], response[14], response[15] };

		result += "[" + bytesToHex(word1) + "] [" + bytesToHex(word2) + "]\n[" + bytesToHex(word3) + "] [" + bytesToHex(word4) + "]";

		LinkedList<Integer> stackWithSeparateDigits = UPLECUtils.formatNumberForProcessor(number, GlobalConstatns.AMOUNT_OF_DIGITS_IN_PATTERN);

		int leftDigit = stackWithSeparateDigits.pop();
		int middleDigit = stackWithSeparateDigits.pop();
		int rightDigit = stackWithSeparateDigits.pop();

		byte[] leftBytePattern = getLeftDigitByteRepresentation(leftDigit);
		byte[] middleBytePattern = getMiddleDigitByteRepresentation(middleDigit);
		byte[] rightBytePattern = getRightDigitByteRepresentation(rightDigit);

		log(leftDigit + "xx == " + bytesToHex(leftBytePattern));
		log("x" + middleDigit + "x == " + bytesToHex(middleBytePattern));
		log("xx" + rightDigit + " == " + bytesToHex(rightBytePattern));

		result += "\n\nSTEP 2 (Parse user number)\n\n";

		result += leftDigit + "xx == " + bytesToHex(leftBytePattern);
		result += "\nx" + middleDigit + "x == " + bytesToHex(middleBytePattern);
		result += "\nxx" + rightDigit + " == " + bytesToHex(rightBytePattern);

		byte[] resultArray = new byte[8];

		for (int i = 0; i < 8; i++) {
			resultArray[i] = (byte) (leftBytePattern[i] + middleBytePattern[i] + rightBytePattern[i]);
		}
		result += "\n=====================================";
		result += String.format("\n%s%s%s == %s", leftDigit, middleDigit, rightDigit, bytesToHex(resultArray));

		result += "\n\nSTEP 3 (Copy words 3 & 4 -> 1 & 2)\n\n";

		word1 = word3;
		word2 = word4;

		result += "[" + bytesToHex(word1) + "] [" + bytesToHex(word2) + "]\n[" + bytesToHex(word3) + "] [" + bytesToHex(word4) + "]";

		result += "\n\nSTEP 4 (Preparing data to write)\n\n";

		word4 = GlobalConstatns.CONTROL_COMMANDS.UPDATE_0x7F;
		byte[] wordInputedByUser = { resultArray[4], resultArray[5], resultArray[6], resultArray[7] };		
		
		result += "[" + bytesToHex(word1) + "] [" + bytesToHex(word2) + "]\n[" + bytesToHex(wordInputedByUser) + "] [" + bytesToHex(word4) + "]";
		
		return result;

	}

	public static final byte[] getLeftDigitByteRepresentation(int value) {
		switch (value) {
			case 0:
				return LEFT_DIGITS_PATTERN._0;

			case 1:
				return LEFT_DIGITS_PATTERN._1;
		}

		return null;
	}

	public static final byte[] getMiddleDigitByteRepresentation(int value) {
		switch (value) {
			case 0:
				return MIDDLE_DIGITS_PATTERN._0;

			case 1:
				return MIDDLE_DIGITS_PATTERN._1;

			case 2:
				return MIDDLE_DIGITS_PATTERN._2;

			case 3:
				return MIDDLE_DIGITS_PATTERN._3;

			case 4:
				return MIDDLE_DIGITS_PATTERN._4;

			case 5:
				return MIDDLE_DIGITS_PATTERN._5;

			case 6:
				return MIDDLE_DIGITS_PATTERN._6;

			case 7:
				return MIDDLE_DIGITS_PATTERN._7;

			case 8:
				return MIDDLE_DIGITS_PATTERN._8;

			case 9:
				return MIDDLE_DIGITS_PATTERN._9;
		}

		return null;
	}

	public static final byte[] getRightDigitByteRepresentation(int value) {
		switch (value) {
			case 0:
				return RIGHT_DIGITS_PATTERN._0;

			case 1:
				return RIGHT_DIGITS_PATTERN._1;

			case 2:
				return RIGHT_DIGITS_PATTERN._2;

			case 3:
				return RIGHT_DIGITS_PATTERN._3;

			case 4:
				return RIGHT_DIGITS_PATTERN._4;

			case 5:
				return RIGHT_DIGITS_PATTERN._5;

			case 6:
				return RIGHT_DIGITS_PATTERN._6;

			case 7:
				return RIGHT_DIGITS_PATTERN._7;

			case 8:
				return RIGHT_DIGITS_PATTERN._8;

			case 9:
				return RIGHT_DIGITS_PATTERN._9;
		}

		return null;
	}

	/**
	 * Format number into specific digit format
	 * examples:
	 * <p>
	 * <b>123 -> ['1','2','3']</b>
	 * </p>
	 * <p>
	 * <b>93 -> ['0','9','3']</b>
	 * </p>
	 * <p>
	 * <b>5 -> ['0','0','5']</b>
	 * </p>
	 * <p>
	 * <b>0 -> ['0','0','0']</b>
	 * </p>
	 * 
	 * @param number
	 *            integer representation of number to convert
	 * @param amountOfDigitsInPattern
	 *            amount digits in the returned stack, in case if amount of digits in number < than amount of digits in pattern
	 *            all higher digits filled as zero
	 * @return linked list with numbers
	 */
	private static final LinkedList<Integer> formatNumberForProcessor(int number, int amountOfDigitsInPattern) {
		// stack to collect digits (queue behavior are required because digits into reverse order)
		LinkedList<Integer> stack = new LinkedList<Integer>();
		int numberToConvert = number;
		while (numberToConvert > 0) {
			stack.push(numberToConvert % 10);
			numberToConvert = numberToConvert / 10;
		}

		// add appropriate amount of 0 in the front of the queue in case if number < 100
		while (stack.size() < amountOfDigitsInPattern) {
			stack.push(0);
		}

		return stack;
	}

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
			Log.d(GlobalConstatns.LOG_TAG, String.valueOf(message));
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
	 * @param s
	 *            string to be converted
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
