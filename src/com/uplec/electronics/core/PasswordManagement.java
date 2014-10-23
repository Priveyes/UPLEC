// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.uplec.electronics.core;

import com.uplec.electronics.R;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
//import android.util.Log;

public class PasswordManagement extends Activity
{
	private EditText value1;
	private EditText value2;
	private EditText value3;
	private EditText value4; 

	private boolean Value1Enable = true;
	private boolean Value2Enable = true;
	private boolean Value3Enable = true;
	private boolean Value4Enable = true;
	
	private Button buttonPresentPassword;
	private Button buttonWritePassword;
	private Button buttonClear;

	private RadioButton rbOptionPwd1;
	private RadioButton rbOptionPwd2;
	private RadioButton rbOptionPwd3;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private String blockName = null;
	private String blockValue = null;
	private long cpt = 0;
	
	byte[] GetSystemInfoAnswer = null;
	private byte[] PresentPasswordCommandAnswer = null;
	private byte[] WritePasswordCommandAnswer = null;
	private byte PasswordNumber = (byte)0x01;
	private byte[] PasswordData = new byte[4];

	private static String GET_BLOCK_NAME = "blockname";
	private static String GET_BLOCK_VALUE = "blockvalue";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password_management);

		Bundle objetbunble  = this.getIntent().getExtras();
		DataDevice dataDevice = (DataDevice)getApplication(); 

		//Get data from bundle
		if (objetbunble != null && objetbunble.containsKey(GET_BLOCK_NAME) && objetbunble.containsKey(GET_BLOCK_VALUE)) 
		{
			blockName = this.getIntent().getStringExtra(GET_BLOCK_NAME);
			blockValue = this.getIntent().getStringExtra(GET_BLOCK_VALUE);
			//Used for DEBUG : Log.i("ERROR == " + blockName, "ERROR == " + blockValue);
		}
		else //Error
		{
			blockName = null;
			blockValue = null;
		}

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] {ndef,};
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };

		initListener();
		
		if (blockName != null && blockValue != null)
		{
			String array[] =  blockValue.split(" ");
			value1.setText(Helper.FormatValueByteWrite(array[0]));
			value2.setText(Helper.FormatValueByteWrite(array[2]));
			value3.setText(Helper.FormatValueByteWrite(array[4]));
			value4.setText(Helper.FormatValueByteWrite(array[6]));
		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);     
		DataDevice ma = (DataDevice)getApplication();
		ma.setCurrentTag(tagFromIntent);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();
		mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
	}
	
	@Override
    protected void onPause() {
		cpt = 500;
		super.onPause();
    }

	private void initListener()
	{
		value1 = (EditText) findViewById(R.id.etvalue1);
		value1.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		value1.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			    // TODO Auto-generated method stub
				int astart = value1.getSelectionStart();
				int aend = value1.getSelectionEnd();
						
				String FieldValue = s.toString().toUpperCase();				
				
				if (Helper.checkDataHexa(FieldValue) == false) 
				{
					value1.setTextKeepState(Helper.checkAndChangeDataHexa(FieldValue));
					value1.setSelection(astart-1, aend-1);
				}
				else
					value1.setSelection(astart, aend);

				if (value1.getText().length() >0 && value1.getText().length() < 2)
				{
					value1.setTextColor(0xffff0000); //RED color
					buttonPresentPassword.setClickable(false);
					buttonWritePassword.setClickable(false);
					Value1Enable = false;
				}
				else
				{
					value1.setTextColor(0xff000000); //BLACK color					
					Value1Enable = true;
					if (Value1Enable == true &&
						Value2Enable == true &&
						Value3Enable == true &&
						Value4Enable == true)							
						{
							buttonPresentPassword.setClickable(true);
							buttonWritePassword.setClickable(true);
						}
						
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			    // TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			    // TODO Auto-generated method stub
				
			}
		});
		
		value2 = (EditText) findViewById(R.id.etvalue2);
		value2.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		value2.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			    // TODO Auto-generated method stub
				int astart = value2.getSelectionStart();
				int aend = value2.getSelectionEnd();
						
				String FieldValue = s.toString().toUpperCase();								
				
				if (Helper.checkDataHexa(FieldValue) == false) 
				{
					value2.setTextKeepState(Helper.checkAndChangeDataHexa(FieldValue));
					value2.setSelection(astart-1, aend-1);
				}
				else
					value2.setSelection(astart, aend);
				
				if (value2.getText().length() >0 && value2.getText().length() < 2)
				{
					value2.setTextColor(0xffff0000); //RED color
					buttonPresentPassword.setClickable(false);
					buttonWritePassword.setClickable(false);
					Value2Enable = false;
				}
				else
				{
					value2.setTextColor(0xff000000); //BLACK color
					Value2Enable = true;
					if (Value1Enable == true &&
						Value2Enable == true &&
						Value3Enable == true &&
						Value4Enable == true)							
						{
							buttonPresentPassword.setClickable(true);
							buttonWritePassword.setClickable(true);
						}
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			    // TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			    // TODO Auto-generated method stub
				
			}
		});
		
		value3 = (EditText) findViewById(R.id.etvalue3);
		value3.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		value3.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			    // TODO Auto-generated method stub
				int astart = value3.getSelectionStart();
				int aend = value3.getSelectionEnd();
						
				String FieldValue = s.toString().toUpperCase();								
				
				if (Helper.checkDataHexa(FieldValue) == false) 
				{
					value3.setTextKeepState(Helper.checkAndChangeDataHexa(FieldValue));
					value3.setSelection(astart-1, aend-1);
				}
				else
					value3.setSelection(astart, aend);
				
				if (value3.getText().length() >0 && value3.getText().length() < 2)
				{
					value3.setTextColor(0xffff0000); //RED color
					buttonPresentPassword.setClickable(false);
					buttonWritePassword.setClickable(false);
					Value3Enable = false;
				}
				else
				{
					value3.setTextColor(0xff000000); //BLACK color
					Value3Enable = true;
					if (Value1Enable == true &&
						Value2Enable == true &&
						Value3Enable == true &&
						Value4Enable == true)							
						{
							buttonPresentPassword.setClickable(true);
							buttonWritePassword.setClickable(true);
						}
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			    // TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			    // TODO Auto-generated method stub
				
			}
		});
		
		value4 = (EditText) findViewById(R.id.etvalue4);
		value4.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		value4.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			    // TODO Auto-generated method stub
				int astart = value4.getSelectionStart();
				int aend = value4.getSelectionEnd();
						
				String FieldValue = s.toString().toUpperCase();								
				
				if (Helper.checkDataHexa(FieldValue) == false) 
				{
					value4.setTextKeepState(Helper.checkAndChangeDataHexa(FieldValue));
					value4.setSelection(astart-1, aend-1);
				}
				else
					value4.setSelection(astart, aend);
				
				if (value4.getText().length() >0 && value4.getText().length() < 2)
				{
					value4.setTextColor(0xffff0000); //RED color
					buttonPresentPassword.setClickable(false);
					buttonWritePassword.setClickable(false);					
					Value4Enable = false;
				}
				else
				{
					value4.setTextColor(0xff000000); //BLACK color
					Value4Enable = true;
					if (Value1Enable == true &&
						Value2Enable == true &&
						Value3Enable == true &&
						Value4Enable == true)	
						{
							buttonPresentPassword.setClickable(true);
							buttonWritePassword.setClickable(true);
						}
				}
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			    // TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			    // TODO Auto-generated method stub
				
			}
		});
		
		rbOptionPwd1 = (RadioButton) findViewById(R.id.pwd1);
		rbOptionPwd2 = (RadioButton) findViewById(R.id.pwd2);
		rbOptionPwd3 = (RadioButton) findViewById(R.id.pwd3);
		
		buttonPresentPassword = (Button) findViewById(R.id.button_presentpassword);
		buttonWritePassword = (Button) findViewById(R.id.button_writepassword);
		buttonClear = (Button) findViewById(R.id.button_clear);

		rbOptionPwd1.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				PasswordNumber = (byte)0x01;
			}
		});

		rbOptionPwd2.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				PasswordNumber = (byte)0x02;
			}
		});
		
		rbOptionPwd3.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				PasswordNumber = (byte)0x03;
			}
		});
		
		buttonPresentPassword.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				
				// TODO Auto-generated method stub
				if (Helper.checkDataHexa(value1.getText().toString()) == true &&
					Helper.checkDataHexa(value2.getText().toString()) == true &&
					Helper.checkDataHexa(value3.getText().toString()) == true &&
					Helper.checkDataHexa(value4.getText().toString()) == true)
						new StartPresentPasswordTask().execute();
				else
				{
					Toast.makeText(getApplicationContext(), "Invalid password data, please modify", Toast.LENGTH_LONG).show();
				}
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});

		buttonWritePassword.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				
				// TODO Auto-generated method stub
				if (Helper.checkDataHexa(value1.getText().toString()) == true &&
					Helper.checkDataHexa(value2.getText().toString()) == true &&
					Helper.checkDataHexa(value3.getText().toString()) == true &&
					Helper.checkDataHexa(value4.getText().toString()) == true)
					new StartWritePasswordTask().execute();
				else
				{
					Toast.makeText(getApplicationContext(), "Invalid password data, please modify", Toast.LENGTH_LONG).show();
				}
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});
		
		buttonClear.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				value1.setText("");
				value2.setText("");
				value3.setText("");
				value4.setText("");
				
				Value1Enable = true;
				Value2Enable = true;
				Value3Enable = true;
				Value4Enable = true;				
				buttonPresentPassword.setClickable(true);
				buttonWritePassword.setClickable(true);
				
			}
		});
	}

	private class StartPresentPasswordTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(PasswordManagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			this.dialog.setMessage("Please, place your phone near the card");
			this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{		
	
				String valueBlock1 = value1.getText().toString();
				String valueBlock2 = value2.getText().toString();
				String valueBlock3 = value3.getText().toString();
				String valueBlock4 = value4.getText().toString();
				
				if(valueBlock1.length() == 0)
					valueBlock1 = "00";
				if(valueBlock2.length() == 0)
					valueBlock2 = "00";
				if(valueBlock3.length() == 0)
					valueBlock3 = "00";
				if(valueBlock4.length() == 0)
					valueBlock4 = "00";
				
				value1.setText(Helper.FormatValueByteWrite(valueBlock1));
				value2.setText(Helper.FormatValueByteWrite(valueBlock2));
				value3.setText(Helper.FormatValueByteWrite(valueBlock3));
				value4.setText(Helper.FormatValueByteWrite(valueBlock4));
	
				valueBlock1 = value1.getText().toString();
				valueBlock2 = value2.getText().toString();
				valueBlock3 = value3.getText().toString();
				valueBlock4 = value4.getText().toString();
				
				String valueBlockTotal = "";
				valueBlockTotal += valueBlock1 + valueBlock2;
				byte[] valueBlockWrite = Helper.ConvertStringToHexBytes(valueBlockTotal);
				
				PasswordData[0] = valueBlockWrite[0];
				PasswordData[1] = valueBlockWrite[1];
				
				valueBlockTotal = "";
				valueBlockTotal += valueBlock3 + valueBlock4;
				valueBlockWrite = Helper.ConvertStringToHexBytes(valueBlockTotal);
				
				PasswordData[2] = valueBlockWrite[0];
				PasswordData[3] = valueBlockWrite[1];
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			PresentPasswordCommandAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((PresentPasswordCommandAnswer == null || PresentPasswordCommandAnswer[0] == 1) && cpt <= 10)
				{
					PresentPasswordCommandAnswer = NFCCommand.sendPresentPasswordCommand(dataDevice.getCurrentTag(), PasswordNumber, PasswordData, dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			if (this.dialog.isShowing())
				this.dialog.dismiss();
    		
			if (PresentPasswordCommandAnswer==null)
			{
				Toast.makeText(getApplicationContext(), "ERROR Present Password (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(PresentPasswordCommandAnswer[0]==(byte)0x01)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Present Password ", Toast.LENGTH_SHORT).show();
    		}
    		else if(PresentPasswordCommandAnswer[0]==(byte)0xFF)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Present Password ", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(PresentPasswordCommandAnswer[0]==(byte)0x00)
    		{
    			Toast.makeText(getApplicationContext(), "Present Password Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Present Password ERROR ", Toast.LENGTH_SHORT).show();
    		}    		
    		
		}
	}

	private class StartWritePasswordTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(PasswordManagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			this.dialog.setMessage("Please, place your phone near the card");
			this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{		
	
				String valueBlock1 = value1.getText().toString();
				String valueBlock2 = value2.getText().toString();
				String valueBlock3 = value3.getText().toString();
				String valueBlock4 = value4.getText().toString();
				
				if(valueBlock1.length() == 0)
					valueBlock1 = "00";
				if(valueBlock2.length() == 0)
					valueBlock2 = "00";
				if(valueBlock3.length() == 0)
					valueBlock3 = "00";
				if(valueBlock4.length() == 0)
					valueBlock4 = "00";
				
				value1.setText(Helper.FormatValueByteWrite(valueBlock1));
				value2.setText(Helper.FormatValueByteWrite(valueBlock2));
				value3.setText(Helper.FormatValueByteWrite(valueBlock3));
				value4.setText(Helper.FormatValueByteWrite(valueBlock4));
	
				valueBlock1 = value1.getText().toString();
				valueBlock2 = value2.getText().toString();
				valueBlock3 = value3.getText().toString();
				valueBlock4 = value4.getText().toString();
				
				String valueBlockTotal = "";
				valueBlockTotal += valueBlock1 + valueBlock2;
				byte[] valueBlockWrite = Helper.ConvertStringToHexBytes(valueBlockTotal);
				
				PasswordData[0] = valueBlockWrite[0];
				PasswordData[1] = valueBlockWrite[1];
				
				valueBlockTotal = "";
				valueBlockTotal += valueBlock3 + valueBlock4;
				valueBlockWrite = Helper.ConvertStringToHexBytes(valueBlockTotal);
				
				PasswordData[2] = valueBlockWrite[0];
				PasswordData[3] = valueBlockWrite[1];
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			WritePasswordCommandAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((WritePasswordCommandAnswer == null || WritePasswordCommandAnswer[0] == 1) && cpt <= 10)
				{
					WritePasswordCommandAnswer = NFCCommand.sendWritePasswordCommand(dataDevice.getCurrentTag(), PasswordNumber, PasswordData, dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			if (this.dialog.isShowing())
				this.dialog.dismiss();
    		
			if (WritePasswordCommandAnswer==null)
			{
				Toast.makeText(getApplicationContext(), "ERROR Write Password (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(WritePasswordCommandAnswer[0]==(byte)0x01)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Write Password ", Toast.LENGTH_SHORT).show();
    		}
    		else if(WritePasswordCommandAnswer[0]==(byte)0xFF)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Write Password ", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(WritePasswordCommandAnswer[0]==(byte)0x00)
    		{
    			Toast.makeText(getApplicationContext(), "Write Password Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Write Password ERROR ", Toast.LENGTH_SHORT).show();
    		}    		
    		
		}
	}
	
	 //***********************************************************************/
	 //* the function Decode the tag answer for the GetSystemInfo command
	 //* the function fills the values (dsfid / afi / memory size / icRef /..) 
	 //* in the myApplication class. return true if everything is ok.
	 //***********************************************************************/
	 public boolean DecodeGetSystemInfoResponse (byte[] GetSystemInfoResponse)
	 {
		 //if the tag has returned a good response 
		 if(GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12)
		 { 
			 DataDevice ma = (DataDevice)getApplication();
			 String uidToString = "";
			 byte[] uid = new byte[8];
			 // change uid format from byteArray to a String
			 for (int i = 1; i <= 8; i++) 
			 {
				 uid[i - 1] = GetSystemInfoResponse[10 - i];
				 uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
			 }			 

			 //***** TECHNO ******
			 ma.setUid(uidToString);
			 if(uid[0] == (byte) 0xE0)
			 		 ma.setTechno("ISO 15693");
			 else if (uid[0] == (byte) 0xE0)
			 	 ma.setTechno("ISO 14443");
			 else
			 	 ma.setTechno("Unknown techno");			 
			 			
			 //***** MANUFACTURER ****
			 if(uid[1]== (byte) 0x02)
			 	 ma.setManufacturer("STMicroelectronics");
			 else if(uid[1]== (byte) 0x04)
			 	 ma.setManufacturer("NXP");
			 else if(uid[1]== (byte) 0x07)
			 	 ma.setManufacturer("Texas Instrument");
			 else
			 	 ma.setManufacturer("Unknown manufacturer");						 			
			 			 
			 //**** PRODUCT NAME *****
			 if(uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07)
			 {
			 	 ma.setProductName("LRI512");
			 	 ma.setMultipleReadSupported(false);
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 else if(uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17)
			 {
			 	 ma.setProductName("LRI64");
			 	 ma.setMultipleReadSupported(false);
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 else if(uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23)
			 {
			 	 ma.setProductName("LRI2K");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 else if(uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B)
			 {
			 	 ma.setProductName("LRIS2K");
			 	 ma.setMultipleReadSupported(false);	
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 else if(uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F)
			 {
			 	 ma.setProductName("M24LR64");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 }
			 else if(uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43)
			 {
			 	 ma.setProductName("LRI1K");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 else if(uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47)
			 {
			 	 ma.setProductName("LRIS64K");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 }
			 else if(uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B)
			 {
			 	 ma.setProductName("M24LR01E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 else if(uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F)
			 {
			 	 ma.setProductName("M24LR16E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 	 if(ma.isBasedOnTwoBytesAddress() == false)
				 	return false;
			 }
			 else if(uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53)
			 {
			 	 ma.setProductName("M24LR02E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 else if(uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57)
			 {
			 	 ma.setProductName("M24LR32E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 	 if(ma.isBasedOnTwoBytesAddress() == false)
				 	return false;
			 }
			 else if(uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B)
			 {
				 ma.setProductName("M24LR04E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 }
			 else if(uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F)
			 {
			 	 ma.setProductName("M24LR64E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 	 if(ma.isBasedOnTwoBytesAddress() == false)
				 	return false;
			 }
			 else if(uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63)
			 {
			 	 ma.setProductName("M24LR08E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 }
			 else if(uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67)
			 {
			 	 ma.setProductName("M24LR128E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 	 if(ma.isBasedOnTwoBytesAddress() == false)
				 	return false;
			 }
			 else if(uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F)
			 {
			 	 ma.setProductName("M24LR256E");
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 	 if(ma.isBasedOnTwoBytesAddress() == false)
				 	return false;
			 }
			 else if(uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB)
			 {
			 	 ma.setProductName("detected product");
			 	 ma.setBasedOnTwoBytesAddress(true);
			 	 ma.setMultipleReadSupported(true);
			 	 ma.setMemoryExceed2048bytesSize(true);
			 }	 
			 else
			 {
			 	 ma.setProductName("Unknown product");
			 	 ma.setMultipleReadSupported(false);
			 	 ma.setMemoryExceed2048bytesSize(false);
			 }
			 
			 //*** DSFID ***
			 ma.setDsfid(Helper.ConvertHexByteToString(GetSystemInfoResponse[10]));
			 
			//*** AFI ***
			 ma.setAfi(Helper.ConvertHexByteToString(GetSystemInfoResponse[11]));			 
			 
			//*** MEMORY SIZE ***
			 if(ma.isBasedOnTwoBytesAddress())
			 {
				 String temp = new String();
				 temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[13]);
				 temp += Helper.ConvertHexByteToString(GetSystemInfoResponse[12]);
				 ma.setMemorySize(temp);
			 }
			 else 
				 ma.setMemorySize(Helper.ConvertHexByteToString(GetSystemInfoResponse[12]));
			 
			//*** BLOCK SIZE ***
			 if(ma.isBasedOnTwoBytesAddress())
				 ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
			 else
				 ma.setBlockSize(Helper.ConvertHexByteToString(GetSystemInfoResponse[13]));

			//*** IC REFERENCE ***
			 if(ma.isBasedOnTwoBytesAddress())
				 ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[15]));
			 else
				 ma.setIcReference(Helper.ConvertHexByteToString(GetSystemInfoResponse[14]));
				 
			 return true;
		 }
		 
		//if the tag has returned an error code 
		 else
			 return false;
	 }	
	 
}