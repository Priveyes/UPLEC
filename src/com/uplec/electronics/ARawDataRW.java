package com.uplec.electronics;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.uplec.electronics.core.BasicWrite;
import com.uplec.electronics.core.DataDevice;
import com.uplec.electronics.core.DataRead;
import com.uplec.electronics.core.DataReadAdapter;
import com.uplec.electronics.core.Helper;
import com.uplec.electronics.core.NFCCommand;
import static com.uplec.electronics.utils.UPLECUtils.decodeSystemInfoResponse;


@EActivity(R.layout.activity_read_write_raw_data) public class ARawDataRW extends Activity {

	@ViewById Button		btnRead;
	@ViewById ListView		lvItems;

	// ///////////////////////////////////////
	byte[]					arrayBytesToWrite		= { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	byte[]					WriteSingleBlockAnswer;
	// //////////////////////////////////////////
	private NfcAdapter		mAdapter;
	private PendingIntent	mPendingIntent;
	private IntentFilter[]	mFilters;
	private String[][]		mTechLists;
	private DataDevice		ma						= (DataDevice) getApplication();
	private long			cpt						= 0;

	String[]				catBlocks				= null;
	String[]				catValueBlocks			= null;

	byte[]					GetSystemInfoAnswer		= null;
	byte[]					ReadMultipleBlockAnswer	= null;
	int						nbblocks				= 0;

	String					sNbOfBlock				= null;
	byte[]					numberOfBlockToRead		= null;

	String					startAddressString		= null;
	byte[]					addressStart			= null;

	List<DataRead>			listOfData				= null;

	private static String	GET_BLOCK_NAME			= "blockname";
	private static String	GET_BLOCK_VALUE			= "blockvalue";

	@AfterViews void afterViews() {
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] { ndef, };
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
		initListener();
	}

	private void initListener() {
		lvItems.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Bundle objetbunble = new Bundle();
				objetbunble.putString(GET_BLOCK_NAME, catBlocks[arg2]);
				objetbunble.putString(GET_BLOCK_VALUE, catValueBlocks[arg2]);
				Intent intent = new Intent(ARawDataRW.this, BasicWrite.class);
				intent.putExtras(objetbunble);
				startActivity(intent);

				return false;
			}
		});
	}

	@Click void btnRead() {
		// new StartReadTask().execute();
		new StartWriteRawBytesTask(this.arrayBytesToWrite, "0000").execute();
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		DataDevice ma = (DataDevice) getApplication();
		ma.setCurrentTag(tagFromIntent);
	}

	@Override protected void onResume() {
		super.onResume();
		mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
	}

	@Override protected void onPause() {
		cpt = 500;
		super.onPause();
	}

	private class StartReadTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog	dialog	= new ProgressDialog(ARawDataRW.this);

		// can use UI thread here
		protected void onPreExecute() {
			DataDevice dataDevice = (DataDevice) getApplication();
			GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(), dataDevice);

			if (decodeSystemInfoResponse(GetSystemInfoAnswer, (DataDevice) getApplication())) {
				startAddressString = Helper.castHexKeyboard("0000");
				startAddressString = Helper.FormatStringAddressStart(startAddressString, dataDevice);
				addressStart = Helper.ConvertStringToHexBytes(startAddressString);
				sNbOfBlock = Helper.FormatStringNbBlockInteger("0002", startAddressString, dataDevice);
				numberOfBlockToRead = Helper.ConvertIntTo2bytesHexaFormat(Integer.parseInt(sNbOfBlock));
				this.dialog.setMessage("Please, keep your phone close to the tag");
				this.dialog.show();
			} else {
				this.dialog.setMessage("Please, No tag detected");
				this.dialog.show();
			}

		}

		// automatically done on worker thread (separate from UI thread)
		@Override protected Void doInBackground(Void... params) {
			DataDevice dataDevice = (DataDevice) getApplication();
			ma = (DataDevice) getApplication();
			ReadMultipleBlockAnswer = null;
			cpt = 0;
			if (decodeSystemInfoResponse(GetSystemInfoAnswer, (DataDevice) getApplication())) {
				// ex:LRIS2K
				if (ma.isMultipleReadSupported() == false || Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <= 1) {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand.sendSeveralReadSingleBlockCommandsNbBlocks(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead, dataDevice);
						cpt++;
					}
					cpt = 0;
				} else if (Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) < 32) {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand.sendReadMultipleBlockCommandCustom(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead[1], dataDevice);
						cpt++;
					}
					cpt = 0;
				} else {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand.sendReadMultipleBlockCommandCustom2(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead, dataDevice);
						cpt++;
					}
					cpt = 0;
				}
			}
			return null;
		}

		// can use UI thread here
		protected void onPostExecute(final Void unused) {

			Log.i("ScanRead", "Button Read CLICKED **** On Post Execute ");
			if (this.dialog.isShowing()) this.dialog.dismiss();
			if (decodeSystemInfoResponse(GetSystemInfoAnswer, (DataDevice) getApplication())) {
				nbblocks = Integer.parseInt(sNbOfBlock);

				if (ReadMultipleBlockAnswer != null && ReadMultipleBlockAnswer.length - 1 > 0) {
					if (ReadMultipleBlockAnswer[0] == 0x00) {
						catBlocks = Helper.buildArrayBlocks(addressStart, nbblocks);
						catValueBlocks = Helper.buildArrayValueBlocks(ReadMultipleBlockAnswer, nbblocks);

						listOfData = new ArrayList<DataRead>();
						for (int i = 0; i < nbblocks; i++) {
							listOfData.add(new DataRead(catBlocks[i], catValueBlocks[i]));
						}
						DataReadAdapter adapter = new DataReadAdapter(getApplicationContext(), listOfData);
						lvItems.setAdapter(adapter);
					}
					// added to erase screen in case of read fail
					else {
						lvItems.setAdapter(null);
						Toast.makeText(getApplicationContext(), "ERROR Read ", Toast.LENGTH_SHORT).show();
					}
				}
				// added to erase screen in case of read fail
				else {
					lvItems.setAdapter(null);
					Toast.makeText(getApplicationContext(), "ERROR Read (no Tag answer) ", Toast.LENGTH_SHORT).show();
				}
			} else {
				lvItems.setAdapter(null);
				Toast.makeText(getApplicationContext(), "ERROR Read (no Tag answer) ", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private class StartWriteRawBytesTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog	dialog	= new ProgressDialog(ARawDataRW.this);
		private byte[]					bytesToWrite;
		private String					startAddressString;

		/**
		 * Start task
		 * @param arr byte array (each byte is in hexadecimal format)
		 * @param startAddr  address in decimal format (1 block = 0001) etc...
		 */
		public StartWriteRawBytesTask(byte[] arr, String startAddr) {
			this.bytesToWrite = arr;
			this.startAddressString = startAddr;

		}

		// can use UI thread here
		@Override protected void onPreExecute() {
			this.dialog.setMessage("Please, place your phone near the card");
			this.dialog.show();

			DataDevice dataDevice = (DataDevice) getApplication();
			GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(), dataDevice);

			if (decodeSystemInfoResponse(GetSystemInfoAnswer, dataDevice)) {
				this.startAddressString = Helper.FormatStringAddressStart(Helper.castHexKeyboard(this.startAddressString), dataDevice);
				addressStart = Helper.ConvertStringToHexBytes(startAddressString);
			}
		}

		// automatically done on worker thread (separate from UI thread)
		@Override protected Void doInBackground(Void... params) {
			cpt = 0;
			DataDevice dataDevice = (DataDevice) getApplication();

			WriteSingleBlockAnswer = null;
			if (decodeSystemInfoResponse(GetSystemInfoAnswer, (DataDevice) getApplication())) {
				while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1) && cpt <= 10) {
					WriteSingleBlockAnswer = NFCCommand.sendWriteMultipleBlockCommand(dataDevice.getCurrentTag(), addressStart, bytesToWrite, dataDevice);
					cpt++;
				}
			}
			return null;
		}

		// can use UI thread here
		@Override protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			if (WriteSingleBlockAnswer == null) {
				Toast.makeText(getApplicationContext(), "ERROR Write (No tag answer) ", Toast.LENGTH_SHORT).show();
				return;
			}

			if (WriteSingleBlockAnswer[0] == (byte) 0x01 || WriteSingleBlockAnswer[0] == (byte) 0xFF && WriteSingleBlockAnswer[0] != (byte) 0x00) {
				Toast.makeText(getApplicationContext(), "ERROR Write", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "Write Sucessfull ", Toast.LENGTH_SHORT).show();
			}

		}
	}
}