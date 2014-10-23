// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.uplec.electronics.core;

import com.uplec.electronics.R;
import java.util.ArrayList;
import java.util.List;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class ScanRead extends Activity {

	EditText				etFrom;
	EditText				etTo;
	Button					launchRead;

	private NfcAdapter		mAdapter;
	private PendingIntent	mPendingIntent;
	private IntentFilter[]	mFilters;
	private String[][]		mTechLists;
	private DataDevice		ma						= (DataDevice) getApplication();
	private long			cpt						= 0;

	ListView				list;

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

	@Override protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] { ndef, };
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };

		initListener();
	}

	private void initListener() {
		etFrom = (EditText) findViewById(R.id.edFrom);
		etFrom.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		etFrom.addTextChangedListener(new TextWatcher() {

			@Override public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				int astart = etFrom.getSelectionStart();
				int aend = etFrom.getSelectionEnd();

				String FieldValue = s.toString().toUpperCase();
				if (Helper.checkDataHexa(FieldValue) == false) {
					etFrom.setTextKeepState(Helper.checkAndChangeDataHexa(FieldValue));
					etFrom.setSelection(astart - 1, aend - 1);
				} else
					etFrom.setSelection(astart, aend);
			}

			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}
		});

		etTo = (EditText) findViewById(R.id.edTo);
		etTo.setText("0001");
		etTo.addTextChangedListener(new TextWatcher() {

			@Override public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				int astart = etTo.getSelectionStart();
				int aend = etTo.getSelectionEnd();

				String FieldValue = s.toString().toUpperCase();
				if (FieldValue.contentEquals("0000")) etTo.setText("0001");

				if (Helper.checkDataHexa(FieldValue) == false) {
					etTo.setTextKeepState(Helper.checkAndChangeDataHexa(FieldValue));
					etTo.setSelection(astart - 1, aend - 1);
				} else
					etTo.setSelection(astart, aend);
			}

			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}
		});

		launchRead = (Button) findViewById(R.id.button_read);

		list = (ListView) findViewById(R.id.listView);

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub

				Bundle objetbunble = new Bundle();
				objetbunble.putString(GET_BLOCK_NAME, catBlocks[arg2]);
				objetbunble.putString(GET_BLOCK_VALUE, catValueBlocks[arg2]);
				// Used for DEBUG : Log.i("ScanRead", "LONG CLICK ****  "+ arg2 );

				Intent intent = new Intent(ScanRead.this, BasicWrite.class);
				intent.putExtras(objetbunble);
				startActivity(intent);

				return false;
			}
		});

		launchRead.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if (Helper.checkDataHexa(etFrom.getText().toString()) == true && Helper.checkDataHexa(etTo.getText().toString()) == true)
					new StartReadTask().execute();
				else {
					Toast.makeText(getApplicationContext(), "Invalid parameters, please modify", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		DataDevice ma = (DataDevice) getApplication();
		ma.setCurrentTag(tagFromIntent);
	}

	@Override protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
	}

	@Override protected void onPause() {
		cpt = 500;
		super.onPause();

	}

	private class StartReadTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog	dialog	= new ProgressDialog(ScanRead.this);

		// can use UI thread here

		protected void onPreExecute() {

			// Used for DEBUG : Log.i("ScanRead", "Button Read CLICKED **** On Pre Execute ");
			DataDevice dataDevice = (DataDevice) getApplication();

			GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(), dataDevice);

			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				startAddressString = etFrom.getText().toString();
				startAddressString = Helper.castHexKeyboard(startAddressString);
				startAddressString = Helper.FormatStringAddressStart(startAddressString, dataDevice);
				addressStart = Helper.ConvertStringToHexBytes(startAddressString);
				etFrom.setText(startAddressString);

				if (etTo.length() == 0) etTo.setText("0001");
				if (Integer.parseInt(etTo.getText().toString()) == 0) etTo.setText("0001");

				sNbOfBlock = etTo.getText().toString();
				// Used for DEBUG : Log.i("ScanRead", "sNbOfBlock 1 -----> " + sNbOfBlock);
				// sNbOfBlock = Helper.castHexKeyboard(sNbOfBlock);
				// Log.i("ScanRead", "sNbOfBlock 2 -----> " + sNbOfBlock);
				// sNbOfBlock = Helper.FormatStringNbBlock(sNbOfBlock, startAddressString ,dataDevice);
				// Log.i("ScanRead", "sNbOfBlock 3 -----> " + sNbOfBlock);
				sNbOfBlock = Helper.FormatStringNbBlockInteger(sNbOfBlock, startAddressString, dataDevice);
				// Used for DEBUG : Log.i("ScanRead", "sNbOfBlock 3 -----> " + sNbOfBlock);

				// numberOfBlockToRead = Helper.ConvertStringToHexBytes(sNbOfBlock);
				numberOfBlockToRead = Helper.ConvertIntTo2bytesHexaFormat(Integer.parseInt(sNbOfBlock));
				// Used for DEBUG : Log.i("ScanRead", "numberOfBlockToRead -----> " +
				// Helper.ConvertHexByteArrayToString(numberOfBlockToRead));
				etTo.setText(sNbOfBlock);
				// Used for DEBUG : Log.i("ScanRead", "sNbOfBlock 5 -----> " + sNbOfBlock);

				this.dialog.setMessage("Please, keep your phone close to the tag");
				this.dialog.show();
			} else {
				this.dialog.setMessage("Please, No tag detected");
				this.dialog.show();
				// Used for DEBUG : Log.i("ScanRead", "NON -----> " + sNbOfBlock);
			}

		}

		// automatically done on worker thread (separate from UI thread)
		@Override protected Void doInBackground(Void... params) {
			DataDevice dataDevice = (DataDevice) getApplication();
			ma = (DataDevice) getApplication();

			ReadMultipleBlockAnswer = null;
			cpt = 0;

			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {

				// if(Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <=1) //ex: 1 byte to be read
				// {
				// while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10 )
				// {
				// Log.i("ScanRead", "Dans le read SINGLE le cpt est � -----> " + String.valueOf(cpt));
				// ReadMultipleBlockAnswer = NFCCommand.SendReadSingleBlockCommand(dataDevice.getCurrentTag(),addressStart,
				// dataDevice);
				// cpt ++;
				// }
				// cpt = 0;
				// }
				// else if(ma.isMultipleReadSupported() == false) //ex: LRIS2K
				if (ma.isMultipleReadSupported() == false || Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <= 1) // ex:
																															// LRIS2K
				{
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
						// Used for DEBUG : Log.i("ScanRead", "Dans le several read single block le cpt est � -----> " +
						// String.valueOf(cpt));
						ReadMultipleBlockAnswer = NFCCommand.sendSeveralReadSingleBlockCommandsNbBlocks(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead, dataDevice);
						cpt++;
					}
					cpt = 0;
				} else if (Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) < 32) {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
						// Used for DEBUG : Log.i("ScanRead", "Dan le read MULTIPLE 1 le cpt est � -----> " +
						// String.valueOf(cpt));
						ReadMultipleBlockAnswer = NFCCommand.sendReadMultipleBlockCommandCustom(dataDevice.getCurrentTag(), addressStart, numberOfBlockToRead[1], dataDevice);
						cpt++;
					}
					cpt = 0;
				} else {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10) {
						// Used for DEBUG : Log.i("ScanRead", "Dans le read MULTIPLE 2 le cpt est � -----> " +
						// String.valueOf(cpt));
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

			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				// nbblocks = Helper.ConvertStringToInt(sNbOfBlock);
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
						list.setAdapter(adapter);
					} else // added to erase screen in case of read fail
					{
						// listOfData.clear();
						// DataReadAdapter adapter = new DataReadAdapter(getApplicationContext(), null);
						list.setAdapter(null);
						Toast.makeText(getApplicationContext(), "ERROR Read ", Toast.LENGTH_SHORT).show();
					}
				} else	// added to erase screen in case of read fail
				{
					// listOfData.clear();
					// DataReadAdapter adapter = new DataReadAdapter(getApplicationContext(), listOfData);
					// list.setAdapter(adapter);
					list.setAdapter(null);
					Toast.makeText(getApplicationContext(), "ERROR Read (no Tag answer) ", Toast.LENGTH_SHORT).show();
				}
			} else {
				// listOfData.clear();
				// DataReadAdapter adapter = new DataReadAdapter(getApplicationContext(), listOfData);
				// list.setAdapter(adapter);
				list.setAdapter(null);
				Toast.makeText(getApplicationContext(), "ERROR Read (no Tag answer) ", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// ***********************************************************************/
	// * the function Decode the tag answer for the GetSystemInfo command
	// * the function fills the values (dsfid / afi / memory size / icRef /..)
	// * in the myApplication class. return true if everything is ok.
	// ***********************************************************************/
	public boolean DecodeGetSystemInfoResponse(byte[] GetSystemInfoResponse) {
		// if the tag has returned a good response
		if (GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12) {
			DataDevice ma = (DataDevice) getApplication();
			String uidToString = "";
			byte[] uid = new byte[8];
			// change uid format from byteArray to a String
			for (int i = 1; i <= 8; i++) {
				uid[i - 1] = GetSystemInfoResponse[10 - i];
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
			ma.setDsfid(Helper.ConvertHexByteToString(GetSystemInfoResponse[10]));

			// *** AFI ***
			ma.setAfi(Helper.ConvertHexByteToString(GetSystemInfoResponse[11]));

			// *** MEMORY SIZE ***
			if (ma.isBasedOnTwoBytesAddress()) {
				String temp = new String();
				temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[13]);
				temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[12]);
				ma.setMemorySize(temp);
			} else
				ma.setMemorySize(Helper.ConvertHexByteToString(GetSystemInfoResponse[12]));

			// *** BLOCK SIZE ***
			if (ma.isBasedOnTwoBytesAddress())
				ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
			else
				ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[13]));

			// *** IC REFERENCE ***
			if (ma.isBasedOnTwoBytesAddress())
				ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[15]));
			else
				ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));

			return true;
		}

		// if the tag has returned an error code
		else
			return false;
	}

}