package com.uplec.electronics;

import static com.uplec.electronics.utils.UPLECUtils.decodeSystemInfoResponse;
import static com.uplec.electronics.utils.UPLECUtils.log;

import java.util.LinkedList;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uplec.electronics.constants.GlobalConstatns;
import com.uplec.electronics.constants.GlobalConstatns.WRITING_STATE;
import com.uplec.electronics.core.DataDevice;
import com.uplec.electronics.core.Helper;
import com.uplec.electronics.core.NDEFMessages;
import com.uplec.electronics.core.NFCCommand;
import com.uplec.electronics.utils.AppPref_;
import com.uplec.electronics.utils.UPLECUtils;
import com.uplec.electronics.utils.UPLECUtils.FontCache;


@EActivity(R.layout.activity_main) public class AMain extends Activity {

	// ===================== views
	@ViewById public TextView				tw1, tw2, tw3, tw4, tw5, tw6, tw7, tw8, tw9, tw0, twUp, tvSettings, twDown, tvWrite, tvRead,
			tvWriteClearNDEFMessage;
	@ViewById EditText						etNumber;
	@AnimationRes(R.anim.shake) Animation	animationShaking;
	@AnimationRes(R.anim.fade_in) Animation	animationChangingMode;
	@ViewById ImageView						ivClear, ivMode;
	@Pref AppPref_							appPref;
	// ===================== views

	// ===================== variables
	// 0 - decrement value, 1 - increment value, 2 - leave number the same
	private int								changingNumberState	= 1;
	private byte[]							arrayBytesToWrite	= { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07 };
	private byte[]							writeBlockAnswer;
	private byte[]							getSystemInfoAnswer	= null;
	private ProgressDialog					dialog;
	// //////////////////////////////////////////

	private NfcAdapter						mAdapter;
	private PendingIntent					mPendingIntent;
	private IntentFilter[]					mFilters;
	private String[][]						mTechLists;
	private DataDevice						ma					= (DataDevice) getApplication();
	private byte[]							NdefTextMessageToWrite;
	private byte[]							WriteStatus;
	private String							NDEFTextMessage;
	private long							cpt					= 0;
	private String							sNDEFMessage		= "nothing";
	private byte[]							fullNdefMessage		= null;
	private long							numberOfBlockToRead	= 0;

	// ====================== variables

	@AfterViews void afterViews() {
		// hide soft keyboard, allow to input only through pinpad
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		// set typeface to edit text
		etNumber.setTypeface(FontCache.get(FontCache.DIGITAL_MONO_ITALIC, this));

		// Check for available NFC Adapter
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
			UPLECUtils.showCustomToast(this, "NFC is not supported by this device", R.drawable.warning);
			GlobalConstatns.IS_NFC_SUPPORTED = false;
		} else {
			mAdapter = NfcAdapter.getDefaultAdapter(this);
			mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
			mFilters = new IntentFilter[] { ndef, };
			mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
			ma = (DataDevice) getApplication();
		}

		String message = UPLECUtils.formatNumberToBytePattern(99);
		UPLECUtils.displayStepsInDialog(message, this);
	}

	@Click void tvSettings() {
		// tvSettings.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		// startActivity(new Intent(AMain.this, ASettings_.class));
		startActivity(new Intent(AMain.this, ARawDataRW_.class));
	}

	@Click void ivMode() {
		switch (changingNumberState) {
		// 0 means DECREMENT
			case WRITING_STATE.DECREMENT:
				// switch to next state
				changingNumberState = WRITING_STATE.INCREMENT;
				ivMode.setImageResource(R.drawable.arrow_up);
				UPLECUtils.showCustomToast(AMain.this, "INCREMENT MODE", R.drawable.arrow_up);
				break;

			// 1 means INCREMENT
			case WRITING_STATE.INCREMENT:
				// switch to next state
				changingNumberState = WRITING_STATE.CONSTANT;
				ivMode.setImageResource(R.drawable.arrow_constant);
				UPLECUtils.showCustomToast(AMain.this, "CONSTANT MODE", R.drawable.arrow_constant);
				break;

			// 2 means value stay the same
			case WRITING_STATE.CONSTANT:
				// switch to next state
				changingNumberState = WRITING_STATE.DECREMENT;
				ivMode.setImageResource(R.drawable.arrow_down);
				UPLECUtils.showCustomToast(AMain.this, "DECREMENT MODE", R.drawable.arrow_down);
				break;
		}
		ivMode.startAnimation(animationChangingMode);

	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			DataDevice dataDevice = (DataDevice) getApplication();
			dataDevice.setCurrentTag(tagFromIntent);
			if (appPref.writeAutomatically().get()) {
				tvWrite.performClick();
			}
		}
	}

	@Override protected void onResume() {
		super.onResume();
		GlobalConstatns.MAX_NUMBER = appPref.maxValue().get();

		if (!TextUtils.isEmpty(etNumber.getText().toString())) {
			try {
				if (GlobalConstatns.MAX_NUMBER < Integer.valueOf(etNumber.getText().toString())) {
					etNumber.setText(String.valueOf(GlobalConstatns.MAX_NUMBER));
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (GlobalConstatns.IS_NFC_SUPPORTED) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
		}
	}

	@Override protected void onPause() {
		super.onPause();
		cpt = 500;
	}

	/**
	 * Handle up button click (increment value)
	 */
	@Click void twUp() {
		twUp.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		// wrap method in try catch expression (prevent NPE and NFE)
		try {
			// if etNumber is not empty
			if (!TextUtils.isEmpty(etNumber.getText().toString())) {
				// get current value, convert in to int, and increment it
				int currentValue = Integer.valueOf(etNumber.getText().toString());
				currentValue++;
				// check is this value <= max number (is this number valid)
				if (currentValue <= GlobalConstatns.MAX_NUMBER) {
					etNumber.setText(String.valueOf(currentValue));
					UPLECUtils.setSelectionAfterText(etNumber);
					twUp.setBackgroundResource(R.drawable.green_circle);
					twDown.setBackgroundResource(R.drawable.circle);
				} else {
					etNumber.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
				}
			} else {
				twUp.setBackgroundResource(R.drawable.green_circle);
				twDown.setBackgroundResource(R.drawable.circle);
				// if string etNumber was empty, set it to one
				etNumber.setText(String.valueOf("1"));
				UPLECUtils.setSelectionAfterText(etNumber);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Handle down click (decrement value)
	 */
	@Click void twDown() {
		twDown.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		try {
			if (!TextUtils.isEmpty(etNumber.getText().toString())) {
				int currentValue = Integer.valueOf(etNumber.getText().toString());
				currentValue--;
				// check range after decrement
				if (currentValue <= GlobalConstatns.MAX_NUMBER && currentValue > 0) {
					etNumber.setText(String.valueOf(currentValue));
					UPLECUtils.setSelectionAfterText(etNumber);
					twUp.setBackgroundResource(R.drawable.circle);
					twDown.setBackgroundResource(R.drawable.green_circle);
				} else {
					etNumber.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
				}
			} else {
				// if string etNumber was empty, set it to one
				etNumber.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Clear message on the kit (board)
	 */
	@Click void tvWriteClearNDEFMessage() {
		tvWriteClearNDEFMessage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		// check is NFC supported
		if (GlobalConstatns.IS_NFC_SUPPORTED) {
			new StartWriteTask("").execute();
		} else {
			UPLECUtils.showCustomToast(this, "NFC is not supported by this device", R.drawable.warning);
		}
	}

	/**
	 * Clear etNumber (one digit)
	 */
	@Click void ivClear() {
		ivClear.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		if (!TextUtils.isEmpty(etNumber.getText().toString())) {
			etNumber.setText(etNumber.getText().toString().substring(0, etNumber.getText().toString().length() - 1));
			UPLECUtils.setSelectionAfterText(etNumber);
		}
	}

	/**
	 * Clear etNumber (clear everything)
	 * 
	 * @param clickedView
	 *            view that has been clicked
	 */
	@LongClick void ivClear(View clickedView) {
		ivClear.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		if (!TextUtils.isEmpty(etNumber.getText().toString())) {
			etNumber.getText().clear();
		}
	}

	/**
	 * Processing user input
	 * 
	 * @param numberView
	 */
	@AfterTextChange void etNumber(TextView numberView) {
		String value = numberView.getText().toString();
		if (!TextUtils.isEmpty(value)) {
			// validation of inputed value
			if (!isValidNumber(value)) {
				// play animation to notify user that number is > MAX_NUMBER
				etNumber.startAnimation(animationShaking);
				// remove last inserted digit and play animation
				etNumber.setText(value.substring(0, value.length() - 1));
			}
		}
	}

	/**
	 * Validate number typed by user
	 * 
	 * @param text
	 *            string representation of the number
	 */
	private boolean isValidNumber(String text) {
		boolean returnValue = false;
		try {
			// if inserted number > MAX_NUMBER, number is invalid, return false,
			// true otherwise
			return (Integer.valueOf(text) <= GlobalConstatns.MAX_NUMBER) && (Integer.valueOf(text) > 0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return returnValue;
	}

	/**
	 * Handle write button, attempt to write data
	 */
	@Click void tvWrite() {
		tvWrite.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		// check is nfc supported
		if (GlobalConstatns.IS_NFC_SUPPORTED) {
			if (TextUtils.isEmpty(etNumber.getText().toString())) {
				UPLECUtils.showCustomToast(this, "Please enter a valid number", R.drawable.warning);
				etNumber.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
			} else {
				new StartWriteTask(etNumber.getText().toString()).execute();
				// writeBytesRaw(arrayBytesToWrite, "0000");
			}
		} else {
			UPLECUtils.showCustomToast(this, "NFC is not supported by this device", R.drawable.warning);
		}
	}

	/**
	 * Handle read buttom, attempt to read data
	 */
	@Click void tvRead() {
		tvRead.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		// check is nfc supported
		if (GlobalConstatns.IS_NFC_SUPPORTED) {
			new StartReadTask().execute();
			// readBytesRaw("0000");
		} else {
			UPLECUtils.showCustomToast(this, "NFC is not supported by this device", R.drawable.warning);
		}
	}

	@Click void tw1() {
		tw1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "1");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw2() {
		tw2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "2");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw3() {
		tw3.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "3");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw4() {
		tw4.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "4");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw5() {
		tw5.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "5");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw6() {
		tw6.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "6");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw7() {
		tw7.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "7");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw8() {
		tw8.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "8");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw9() {
		tw9.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "9");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	@Click void tw0() {
		tw0.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
		etNumber.setText(etNumber.getText().toString() + "0");
		UPLECUtils.setSelectionAfterText(etNumber);
	}

	// /////////////////////////////////////////////// ASYNC TASK, WRITE MESSAGE
	// TO NFC CARD

	private class StartWriteTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog	dialog	= new ProgressDialog(AMain.this);
		private String					NDEFMessage;

		public StartWriteTask(String messageToWrite) {
			this.NDEFMessage = messageToWrite;
		}

		// can use UI thread here
		@Override protected void onPreExecute() {
			this.dialog.setMessage("Programming...");
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)

		@Override protected Void doInBackground(Void... params) {
			DataDevice dataDevice = (DataDevice) getApplication();
			ma = (DataDevice) getApplication();

			getSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(), dataDevice);

			if (decodeSystemInfoResponse(getSystemInfoAnswer, (DataDevice) getApplication())) {
				NDEFTextMessage = this.NDEFMessage;
				NdefTextMessageToWrite = NDEFMessages.ConvertStringToNDEF_Text_ByteArray(NDEFTextMessage, dataDevice);

				if (NdefTextMessageToWrite != null) {

					// 1st : store TLF 4 bytes beginning + write 0x00 to length
					// byte(s)
					byte[] TLV2write = new byte[4];
					byte[] TLV2write_zeroLength = new byte[4];
					for (int i = 0; i < 4; i++) {
						TLV2write[i] = NdefTextMessageToWrite[i + 4];
					}
					if (TLV2write[1] == 0xFF) {
						TLV2write_zeroLength[0] = TLV2write[0];
						TLV2write_zeroLength[1] = 0x00;
						TLV2write_zeroLength[2] = 0x00;
						TLV2write_zeroLength[3] = 0x00;
					} else {
						TLV2write_zeroLength[0] = TLV2write[0];
						TLV2write_zeroLength[1] = 0x00;
						TLV2write_zeroLength[2] = TLV2write[2];
						TLV2write_zeroLength[3] = TLV2write[3];
					}
					cpt = 0;
					WriteStatus = null;
					while ((WriteStatus == null || WriteStatus[0] == 1) && cpt < 10) {
						// Used for DEBUG : Log.i("NDEFWrite",
						// "Dan le WRITE MULTIPLE le cpt est � -----> " +
						// String.valueOf(cpt));
						WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[] { 0x00, 0x01 }, TLV2write_zeroLength, dataDevice);
						cpt++;
					}

					if (WriteStatus[0] == 0x00) {
						// 2nd Write CCfield if o write error
						byte[] CCfield2write = new byte[4];
						for (int i = 0; i < 4; i++) {
							CCfield2write[i] = NdefTextMessageToWrite[i];
						}
						cpt = 0;
						WriteStatus = null;
						while ((WriteStatus == null || WriteStatus[0] == 1) && cpt < 10) {
							// Used for DEBUG : Log.i("NDEFWrite",
							// "Dan le WRITE MULTIPLE le cpt est � -----> " +
							// String.valueOf(cpt));
							WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[] { 0x00, 0x00 }, CCfield2write, dataDevice);
							cpt++;
						}

						if (WriteStatus[0] == 0x00) {
							// 3rd write rest of the NDEF message if no previous
							// errors
							byte[] restNDEFmsg2write = new byte[NdefTextMessageToWrite.length];
							for (int i = 0; i < NdefTextMessageToWrite.length - 8; i++) {
								restNDEFmsg2write[i] = NdefTextMessageToWrite[i + 8];
							}

							cpt = 0;
							WriteStatus = null;
							while ((WriteStatus == null || WriteStatus[0] == 1) && cpt < 10) {
								// Used for DEBUG : Log.i("NDEFWrite",
								// "Dan le WRITE MULTIPLE le cpt est � -----> "
								// + String.valueOf(cpt));
								WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[] { 0x00, 0x02 }, restNDEFmsg2write, dataDevice);
								cpt++;
							}
							if (WriteStatus[0] == 0x00) {
								// 4rth write store TLF 4 bytes begining with
								// length byte(s)
								cpt = 0;
								WriteStatus = null;
								while ((WriteStatus == null || WriteStatus[0] == 1) && cpt < 10) {
									// Used for DEBUG : Log.i("NDEFWrite",
									// "Dan le WRITE MULTIPLE le cpt est � -----> "
									// + String.valueOf(cpt));
									WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[] { 0x00, 0x01 }, TLV2write, dataDevice);
									cpt++;
								}
							}
						}
					}
				} else {
					WriteStatus = new byte[] { (byte) 0x02 };// error code for
																// message too
																// long for
																// memory
				}
			}
			return null;

		}

		// can use UI thread here
		@Override protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (decodeSystemInfoResponse(getSystemInfoAnswer, (DataDevice) getApplication())) {
				if (WriteStatus == null) {
					Toast.makeText(ma.getApplicationContext(), "Error during write programming please retry", Toast.LENGTH_SHORT).show();
				} else if (WriteStatus[0] == (byte) 0x00) {
					// The clear message has been written
					if (this.NDEFMessage.equals("")) {
						// Display message that data has been written
						// successfully
						UPLECUtils.showCustomToast(AMain.this, "Data has been cleared", R.drawable.info);
						return;
					}

					// Display message that data has been written successfully
					UPLECUtils.showCustomToast(AMain.this, "Data has been succesfully written", R.drawable.info);

					// check state of the changing number and perform
					// appropriate
					// action
					switch (changingNumberState) {
						case 0:
							twDown.performClick();
							break;
						case 1:
							twUp.performClick();
							break;
						case 2:
							break;
					}
				} else if (WriteStatus[0] == (byte) 0x02) {
					Toast.makeText(ma.getApplicationContext(), "The message you want to write is too long for the memory size", Toast.LENGTH_SHORT).show();
				}

				else {
					Toast.makeText(ma.getApplicationContext(), "Error during write programming please retry", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(ma.getApplicationContext(), "No tag detected", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////
	// READING TASK

	private class StartReadTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog	dialog	= new ProgressDialog(AMain.this);

		// can use UI thread here
		@Override protected void onPreExecute() {
			this.dialog.setMessage("Please, keep your phone close to the tag.");
			this.dialog.show();
		}

		@Override protected Void doInBackground(Void... params) {

			DataDevice dataDevice = (DataDevice) getApplication();

			boolean msgIsEmpty = false;
			fullNdefMessage = null;

			// Check Tag detected (memory size, nb byte address ...) by reading
			// System Info
			byte[] getSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(), dataDevice);

			if (decodeSystemInfoResponse(getSystemInfoAnswer, (DataDevice) getApplication())) {

				byte[] resultBlock0 = new byte[4];
				byte[] resultBlock1 = new byte[12];
				cpt = 0;

				resultBlock0 = null;
				while ((resultBlock0 == null || resultBlock0[0] == 1) && cpt < 10) {
					resultBlock0 = NFCCommand.sendReadSingleBlockCommand(dataDevice.getCurrentTag(), new byte[] { 0x00, 0x00 }, dataDevice);
					cpt++;
				}

				// NDEF format : 4th first bytes of NDEF header
				// CC0 = E1h = NDEF message is present
				// CC1 = bit7-6 : Major version
				// bit5-4 : Minor version
				// bit3-2 : Read access (00:free access)
				// bit1-0 : Write access (00:free access / 10:write need
				// password / 11:no write access)
				// CC2 = Memory size of data field (CC2 *8)
				// CC3 = bit7-3 : rfu
				// bit2 : IC memory exceed 2040 bytes
				// bit1 : rfu
				// bit0 : 1=support Multiple read / read single only

				if (resultBlock0[0] == (byte) 0x00 && resultBlock0[1] == (byte) 0xE1) {
					// NDEF TAG Format valid
					cpt = 0;
					resultBlock1 = null;

					boolean boolMultipleReadCC3 = false;
					// CC3 bit0 analysis for Read Single /Read Multiple option
					if ((resultBlock0[4] & (byte) 0x01) == (byte) 0x01) {
						boolMultipleReadCC3 = true;
					} else {
						boolMultipleReadCC3 = false;
					}

					while ((resultBlock1 == null || resultBlock1[0] == 1) && cpt < 10) {
						resultBlock1 = NFCCommand.sendSeveralReadSingleBlockCommands(dataDevice.getCurrentTag(), new byte[] { 0x00, 0x01 }, new byte[] { 0x00, 0x0C }, dataDevice);
						cpt++;
					}

					byte[] resultBlockX = new byte[2];
					int resultBlockY = 0;

					int nbBytesToRead = 0;
					int numberOfBytesToRead = 0;

					// "00" = "Used to Padding"
					// "01" = "Defines details of the lock bits"
					// "02" = "Identify reserved memory areas"
					// "03" = "Contains NDEF message"
					// "FD" = "Tag proprietary information"
					// "FE" = "Last TLV block in the data area"

					if (resultBlock1[0] == (byte) 0x00 && resultBlock1[1] == (byte) 0x03) // "03"
																							// =
																							// "Contains NDEF message"
					{
						// LField : if byte1=00 -> message is empty or not well
						// written
						// Lfield : if byte1=FF -> Lfield coded on 2 bytes
						// (byte2byte3) else byte1
						if (resultBlock1[2] == (byte) 0x00) {
							msgIsEmpty = true;
						} else if (resultBlock1[2] == (byte) 0xFF) {
							resultBlockX[0] += resultBlock1[3];
							resultBlockX[1] += resultBlock1[4];
							numberOfBytesToRead = (Helper.Convert2bytesHexaFormatToInt(resultBlockX));
							numberOfBytesToRead += 12; // 10;
							if (numberOfBytesToRead % 4 > 0) {
								numberOfBytesToRead += (4 - (numberOfBytesToRead % 4)) + 4;
							}
							resultBlockX = Helper.ConvertIntTo2bytesHexaFormat(numberOfBytesToRead);
						} else {
							resultBlockX[0] += (byte) 0x00;
							resultBlockX[1] += resultBlock1[2];
							numberOfBytesToRead = (Helper.Convert2bytesHexaFormatToInt(resultBlockX));
							numberOfBytesToRead += 12; // 8;
							if (numberOfBytesToRead % 4 > 0) {
								numberOfBytesToRead += (4 - (numberOfBytesToRead % 4)) + 4;
							}
							resultBlockX = Helper.ConvertIntTo2bytesHexaFormat(numberOfBytesToRead);
						}
					} else if (resultBlock1[0] == (byte) 0x00 && resultBlock1[1] == (byte) 0x01) // "01"
																									// =
																									// "Defines details of the lock bits"
					{
						// resultBlock1[2]==(byte)0x03) length lock bits data
						// resultBlock1[3]==(byte)0x03) lock bits data 1
						// resultBlock1[4]==(byte)0x03) lock bits data 2
						// resultBlock1[5]==(byte)0x03) lock bits data 3

						// LField : if byte1=00 -> message is empty or not well
						// written
						// Lfield : if byte1=FF -> Lfield coded on 2 bytes
						// (byte2byte3) else byte1
						if (resultBlock1[6] == (byte) 0x00) {
							msgIsEmpty = true;
						} else if (resultBlock1[6] == (byte) 0xFF) {
							resultBlockX[0] += resultBlock1[7];
							resultBlockX[1] += resultBlock1[8];
							numberOfBytesToRead = (Helper.Convert2bytesHexaFormatToInt(resultBlockX));
							numberOfBytesToRead += 12; // 10;
							if (numberOfBytesToRead % 4 > 0) {
								numberOfBytesToRead += (4 - (numberOfBytesToRead % 4)) + 4;
							}
							resultBlockX = Helper.ConvertIntTo2bytesHexaFormat(numberOfBytesToRead);
						} else {
							resultBlockX[0] += (byte) 0x00;
							resultBlockX[1] += resultBlock1[7];
							numberOfBytesToRead = (Helper.Convert2bytesHexaFormatToInt(resultBlockX));
							numberOfBytesToRead += 12; // 8
							if (numberOfBytesToRead % 4 > 0) {
								numberOfBytesToRead += (4 - (numberOfBytesToRead % 4)) + 4;
							}
							resultBlockX = Helper.ConvertIntTo2bytesHexaFormat(numberOfBytesToRead);
						}
					}

					// bNumberOfBlock =
					// Helper.ConvertIntTo2bytesHexaFormat(numberOfBlockToRead);

					// //Lfield : if byte1=FF -> Lfield coded on 2 bytes
					// (byte2byte3) else byte1
					// if(resultBlock1[2]==(byte)0xFF)
					// numberOfBlockToRead = ((((byte)resultBlock1[3] << 8) |
					// (byte)resultBlock1[4]) / 4) + 10;
					// else
					// numberOfBlockToRead = (resultBlock1[2]/4)+10;

					// bNumberOfBlock =
					// Helper.ConvertIntTo2bytesHexaFormat(numberOfBlockToRead);

					if (msgIsEmpty == false) {
						cpt = 0;
						// if(numberOfBlockToRead <32 ||
						// boolMultipleReadCC3==false)

						if (boolMultipleReadCC3 == false || dataDevice.isMultipleReadSupported() == false) // ex:
																											// LRIS2K
																											// no
																											// Multiple
																											// read
																											// allowed
						{
							while ((fullNdefMessage == null || fullNdefMessage[0] == 1) && cpt < 10 && numberOfBytesToRead != 0) {
								// fullNdefMessage =
								// NFCCommand.SendReadMultipleBlockCommandCustom(dataDevice.getCurrentTag(),new
								// byte[]{0x00,0x00},
								// bNumberOfBlock[1], dataDevice);
								// fullNdefMessage =
								// NFCCommand.Send_several_ReadSingleBlockCommands(dataDevice.getCurrentTag(),new
								// byte[]{0x00,0x00},
								// nbBytesToRead, dataDevice);
								fullNdefMessage = NFCCommand.sendSeveralReadSingleBlockCommands(dataDevice.getCurrentTag(), new byte[] { 0x00, 0x00 }, resultBlockX, dataDevice);
								cpt++;
							}
						} else {
							while ((fullNdefMessage == null || fullNdefMessage[0] == 1) && cpt < 10 && numberOfBytesToRead != 0) {
								// fullNdefMessage =
								// NFCCommand.SendReadMultipleBlockCommandCustom2(dataDevice.getCurrentTag(),new
								// byte[]{0x00,0x00},
								// bNumberOfBlock, dataDevice);
								fullNdefMessage = NFCCommand.sendSeveralReadMultipleBlockCommands(dataDevice.getCurrentTag(), resultBlockX, dataDevice);
								// Used for DEBUG : Log.i("NDEF Text",
								// "fullNdefMessage **** : " +
								// Helper.ConvertHexByteArrayToString(fullNdefMessage));
								cpt++;
								// Used for DEBUG : Log.i("CPT ", "***** " +
								// String.valueOf(cpt));
							}

						}
					} else {
						fullNdefMessage = new byte[1];
						fullNdefMessage[0] = (byte) 0x00;
					}
				}

				else {
					// Not NDEF TAG Format
					numberOfBlockToRead = 0;
				}
			}
			// else

			return null;
		}

		@Override protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			if (fullNdefMessage == null) {
				UPLECUtils.showCustomToast(AMain.this, "No NDEF message", R.drawable.warning);
				return;
			} else if (fullNdefMessage.length == 1 && fullNdefMessage[0] == (byte) 0x00) {
				UPLECUtils.showCustomToast(AMain.this, "NDEF field equal to 0", R.drawable.warning);
				return;
			} else if (fullNdefMessage[0] == (byte) 0xAF) {
				UPLECUtils.showCustomToast(AMain.this, "Error during Read", R.drawable.warning);
				return;
			} else if (fullNdefMessage.length > 1) {
				byte[] NdefMessage = new byte[fullNdefMessage.length];
				for (int i = 1; i < fullNdefMessage.length; i++) {
					NdefMessage[i - 1] = fullNdefMessage[i];
				}
				sNDEFMessage = NDEFMessages.ConvertNDEF_ByteArrayToString_Adapted(NdefMessage);

				if (sNDEFMessage != "No Ndef Message Found") {
					sNDEFMessage = sNDEFMessage.replace("type:TEXT ", "Chair Number:").replace("\n", "").trim();
					UPLECUtils.showCustomToast(AMain.this, sNDEFMessage.equals("Chair Number:") ? "There is no data found" : sNDEFMessage, R.drawable.info);
				} else {
					UPLECUtils.showCustomToast(AMain.this, "No Tag ?", R.drawable.warning);
				}
			} else {
				sNDEFMessage = "No NDEF message";
			}
		}
	}

	// ///////////////// PROPER READING HANDLING /////////////////////////////////////////////

	@Background void readBytesRaw(String startAddr) {
		// display progress dialog
		toggleProgressDialogState(true);

		byte[] addressStart = null;

		writeBlockAnswer = null;

		String sNbOfBlock = null;

		DataDevice dataDevice = (DataDevice) getApplication();
		getSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(), dataDevice);

		cpt = 0;
		if (decodeSystemInfoResponse(getSystemInfoAnswer, (DataDevice) getApplication())) {
			// format address string
			startAddr = Helper.FormatStringAddressStart(Helper.castHexKeyboard(startAddr), dataDevice);
			addressStart = Helper.ConvertStringToHexBytes(startAddr);
			// Hardcoded amount of blocks (2)
			sNbOfBlock = Helper.FormatStringNbBlockInteger("0002", startAddr, dataDevice);
			byte[] numberOfBlockToRead = Helper.ConvertIntTo2bytesHexaFormat(Integer.parseInt(sNbOfBlock));

			// ex:LRIS2K
			if (ma.isMultipleReadSupported() == false || Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <= 1) {
				while ((writeBlockAnswer == null || writeBlockAnswer[0] == 1) && cpt <= 10) {
					writeBlockAnswer = NFCCommand.sendSeveralReadSingleBlockCommandsNbBlocks(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead, dataDevice);
					cpt++;
				}
				cpt = 0;
			} else if (Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) < 32) {
				while ((writeBlockAnswer == null || writeBlockAnswer[0] == 1) && cpt <= 10) {
					writeBlockAnswer = NFCCommand.sendReadMultipleBlockCommandCustom(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead[1], dataDevice);
					cpt++;
				}
				cpt = 0;
			} else {
				while ((writeBlockAnswer == null || writeBlockAnswer[0] == 1) && cpt <= 10) {
					writeBlockAnswer = NFCCommand.sendReadMultipleBlockCommandCustom2(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead, dataDevice);
					cpt++;
				}
				cpt = 0;
			}
			// TODO: Add method to process
			// this.dialog.setMessage("Please, keep your phone close to the tag");
			// this.dialog.show();
		} else {
			// this.dialog.setMessage("Please, No tag detected");
			// this.dialog.show();
		}
		processReadResponse(writeBlockAnswer, Integer.parseInt(sNbOfBlock), addressStart);
	}

	/**
	 * Process read response
	 * 
	 * @param response
	 *            response byte array
	 * @param numberOfBlock
	 *            amount of blocks (array / (SIZE OF BLOCK = 4))
	 * @param addressStart
	 *            binary representation of address
	 */
	@UiThread void processReadResponse(byte[] response, int numberOfBlock, byte[] addressStart) {
		toggleProgressDialogState(false);

		// check 3 conditions.
		// 1) Can we decode response?
		// 2) Response != null ?
		// 3) length of the response byte array should be greater then 0 ?
		if (decodeSystemInfoResponse(getSystemInfoAnswer, (DataDevice) getApplication()) && (response != null) && (response.length - 1 > 0)) {
			// if [0] byte equal 0x00 --> Success
			if (response[0] == 0x00) {
				UPLECUtils.log("Success");

				// cat blocks (address) = 0000, 0001 ...
				String[] catBlocks = Helper.buildArrayBlocks(addressStart, numberOfBlock);

				// catValueBlocks (values) = 1A 2B 3F 2A ...
				String[] catValueBlocks = Helper.buildArrayValueBlocks(response, numberOfBlock);

				for (int i = 0; i < numberOfBlock; i++) {
					UPLECUtils.log(catValueBlocks[i]);
				}
			} else {
				// added to erase screen in case of read fail
				Toast.makeText(getApplicationContext(), "ERROR Read ", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), "ERROR Read (no Tag answer) ", Toast.LENGTH_SHORT).show();
		}

	}

	// ///////////////// PROPER WRITING HANDLING /////////////////////////////////////////////
	/**
	 * Start task, which writes an array
	 * 
	 * @param arr
	 *            byte array (each byte is in hexadecimal format)
	 * @param startAddr
	 *            address in decimal format i.e. (1 block = 0001) etc...
	 */
	@Background void writeBytesRaw(byte[] arrayToWrite, String startAddr) {
		// display progress dialog
		toggleProgressDialogState(true);

		// start address in binary format
		byte[] addressStart = null;

		// attempts amount
		cpt = 0;

		// Get application instance
		DataDevice dataDevice = (DataDevice) getApplication();

		// send NFC command, get system info and check it
		getSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(), dataDevice);

		// Check if we can decode tag
		if (decodeSystemInfoResponse(getSystemInfoAnswer, dataDevice)) {
			// convert address to proper start address in binary format
			startAddr = Helper.FormatStringAddressStart(Helper.castHexKeyboard(startAddr), dataDevice);
			addressStart = Helper.ConvertStringToHexBytes(startAddr);
		}

		writeBlockAnswer = null;
		if (decodeSystemInfoResponse(getSystemInfoAnswer, (DataDevice) getApplication())) {
			// while amount of attempts <=10 or we write byte array properly
			while ((writeBlockAnswer == null || writeBlockAnswer[0] == 1) && cpt <= 10) {
				writeBlockAnswer = NFCCommand.sendWriteMultipleBlockCommand(dataDevice.getCurrentTag(), addressStart, arrayToWrite, dataDevice);
				cpt++;
			}
		}

		// process response
		processWriteResponse(writeBlockAnswer);
	}

	/**
	 * Process response from tag
	 * 
	 * @param response
	 *            byte array response to be checked on any error
	 */
	@UiThread void processWriteResponse(byte[] response) {
		// dismiss progress dialog
		toggleProgressDialogState(false);

		// if response == null, no tag answered, display proper error message
		if (response == null) {
			Toast.makeText(getApplicationContext(), "ERROR Write (No tag answer) ", Toast.LENGTH_SHORT).show();
			return;
		}

		// if [0] byte equal 0x01 or 0xff, or not equal 0x00 --> error occurred during writing, display error message
		if (response[0] == (byte) 0x01 || response[0] == (byte) 0xFF && response[0] != (byte) 0x00) {
			Toast.makeText(getApplicationContext(), "ERROR Write", Toast.LENGTH_SHORT).show();
		} else {
			// otherwise, everything is OK, display OK message
			Toast.makeText(getApplicationContext(), "Write Sucessfull ", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Change progress dialog state
	 * 
	 * @param isVisible
	 *            show dialog if true, dismiss it otherwise
	 */
	@UiThread void toggleProgressDialogState(boolean isVisible) {
		// lazy initialization
		if (null == dialog) {
			dialog = new ProgressDialog(this);
			dialog.setIndeterminate(true);
			dialog.setMessage("Thinking...");
		}
		// if value was true, display dialog, otherwise, hide it
		if (isVisible) {
			dialog.show();
		} else {
			dialog.dismiss();
		}
	}
}
