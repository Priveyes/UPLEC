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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.inputmethod.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;
//import android.util.Log;

public class EHmanagement extends Activity
{
	EditText valueEHconfigByte;	
	
	Button buttonReadEHconfig;
	Button buttonWriteEHconfig;
	Button buttonWriteD0config;

	EditText valueEHenableByte;
	
	Button buttonCheckEHenable;
	Button buttonResetEHenable;
	Button buttonSetEHenable;
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private String blockName = null;
	private String blockValue = null;
	private long cpt = 0;
	
	byte[] GetSystemInfoAnswer = null;
	private byte[] ReadEHconfigAnswer = null;
	private byte[] WriteEHconfigAnswer = null;
	private byte[] WriteD0configAnswer = null;	
	private byte[] CheckEHenableAnswer = null;
	private byte[] ResetEHenableAnswer = null;
	private byte[] SetEHenableAnswer = null;
	private byte dataToWrite = (byte) 0x00;

	private static String GET_BLOCK_NAME = "blockname";
	private static String GET_BLOCK_VALUE = "blockvalue";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.energy_harvesting);

		DataDevice dataDevice = (DataDevice)getApplication(); 


		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] {ndef,};
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };

		initListener();
		
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
		
		valueEHconfigByte = (EditText) findViewById(R.id.EHconfigByte);
		valueEHconfigByte.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		valueEHconfigByte.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			    // TODO Auto-generated method stub
				int astart = valueEHconfigByte.getSelectionStart();
				int aend = valueEHconfigByte.getSelectionEnd();
								
				String FieldValue = s.toString().toUpperCase();
				
				if (FieldValue.length() < 2)
				{
					valueEHconfigByte.setTextColor(0xffff0000); //RED color
					buttonWriteEHconfig.setClickable(false);
					buttonWriteD0config.setClickable(false);
				}
				else
				{
					valueEHconfigByte.setTextColor(0xff000000); //BLACK color
					buttonWriteEHconfig.setClickable(true);
					buttonWriteD0config.setClickable(true);
				}
				
				if (Helper.checkDataHexa(FieldValue) == false) 
				{
					valueEHconfigByte.setTextKeepState(Helper.checkAndChangeDataHexa(FieldValue));
					valueEHconfigByte.setSelection(astart-1, aend-1);
				}
				else
					valueEHconfigByte.setSelection(astart, aend);
				
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
		
		buttonReadEHconfig = (Button) findViewById(R.id.button_ReadEHconfig);
		buttonWriteEHconfig = (Button) findViewById(R.id.button_WriteEHconfig);
		buttonWriteD0config = (Button) findViewById(R.id.button_WriteD0config);

		valueEHenableByte = (EditText) findViewById(R.id.EHenableByte);
		valueEHenableByte.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);	
		
		buttonCheckEHenable = (Button) findViewById(R.id.button_CheckEHenable);
		buttonResetEHenable = (Button) findViewById(R.id.button_ResetEHenable);
		buttonSetEHenable = (Button) findViewById(R.id.button_SetEHenable);

		buttonReadEHconfig.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				
				//Close Keyboard before stating activity
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(valueEHconfigByte.getApplicationWindowToken(), 0);
	            
	            new StartReadEHconfigTask().execute();
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});

		buttonWriteEHconfig.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				
				//Close Keyboard before stating activity
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(valueEHconfigByte.getApplicationWindowToken(), 0);
	            
	            new StartWriteEHconfigTask().execute();
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});

		buttonWriteD0config.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				
				//Close Keyboard before stating activity
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(valueEHconfigByte.getApplicationWindowToken(), 0);
	            
	            new StartWriteD0configTask().execute();
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});

		buttonCheckEHenable.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				
				//Close Keyboard before stating activity
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(valueEHenableByte.getApplicationWindowToken(), 0);
	            
	            new StartCheckEHenableTask().execute();
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});
		
		buttonResetEHenable.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				
				//Close Keyboard before stating activity
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(valueEHenableByte.getApplicationWindowToken(), 0);
	            
	            new StartResetEHenableTask().execute();
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});
		
		buttonSetEHenable.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				
				//Close Keyboard before stating activity
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(valueEHenableByte.getApplicationWindowToken(), 0);
	            
	            new StartSetEHenableTask().execute();
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});	
		
	}

	private class StartReadEHconfigTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(EHmanagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			//this.dialog.setMessage("Please, place your phone near the card");
			//this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			ReadEHconfigAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((ReadEHconfigAnswer == null || ReadEHconfigAnswer[0] == 1) && cpt <= 10)
				{
					ReadEHconfigAnswer = NFCCommand.sendReadEHconfigCommand(dataDevice.getCurrentTag(), dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			//if (this.dialog.isShowing())
			//	this.dialog.dismiss();
    		
			if (ReadEHconfigAnswer==null)
			{
				String valueByte = "";
				valueEHconfigByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "ERROR Read EH CONFIG byte (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(ReadEHconfigAnswer[0]==(byte)0x01)
    		{
				String valueByte = "";
				valueEHconfigByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "ERROR Read EH CONFIG byte ", Toast.LENGTH_SHORT).show();
    		}
    		else if(ReadEHconfigAnswer[0]==(byte)0xFF)
    		{
    			String valueByte = "";
				valueEHconfigByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "ERROR Read EH CONFIG byte ", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(ReadEHconfigAnswer[0]==(byte)0x00)
    		{
    			valueEHconfigByte.setText(Helper.ConvertHexByteToString(ReadEHconfigAnswer[1]).toUpperCase());
    			Toast.makeText(getApplicationContext(), "Read EH CONFIG byte Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			String valueByte = "";
				valueEHconfigByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "Read EH CONFIG byte ERROR ", Toast.LENGTH_SHORT).show();
    		}    		
    		
		}
	}

	private class StartWriteEHconfigTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(EHmanagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			//this.dialog.setMessage("Please, place your phone near the card");
			//this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
	
				String value = valueEHconfigByte.getText().toString();
				
				if(value.length() == 0)
					value = "00";
	
				dataToWrite = Helper.ConvertStringToHexByte(value);
				
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			WriteEHconfigAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((WriteEHconfigAnswer == null || WriteEHconfigAnswer[0] == 1) && cpt <= 10)
				{
					WriteEHconfigAnswer = NFCCommand.sendWriteEHconfigCommand(dataDevice.getCurrentTag(), dataToWrite, dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			//if (this.dialog.isShowing())
			//	this.dialog.dismiss();
    		
			if (WriteEHconfigAnswer==null)
			{
				Toast.makeText(getApplicationContext(), "ERROR Write EH CONFIG byte (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(WriteEHconfigAnswer[0]==(byte)0x01)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Write EH CONFIG byte", Toast.LENGTH_SHORT).show();
    		}
    		else if(WriteEHconfigAnswer[0]==(byte)0xFF)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Write EH CONFIG byte", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(WriteEHconfigAnswer[0]==(byte)0x00)
    		{
    			Toast.makeText(getApplicationContext(), "Write EH CONFIG byte Sucessfull", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Write EH CONFIG byte ERROR", Toast.LENGTH_SHORT).show();
    		}    		
    		
		}
	}

	private class StartWriteD0configTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(EHmanagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			//this.dialog.setMessage("Please, place your phone near the card");
			//this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
	
				String value = valueEHconfigByte.getText().toString();
				
				if(value.length() == 0)
					value = "00";
	
				dataToWrite = Helper.ConvertStringToHexByte(value);
				
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			WriteEHconfigAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((WriteD0configAnswer == null || WriteD0configAnswer[0] == 1) && cpt <= 10)
				{
					WriteD0configAnswer = NFCCommand.aendWriteD0configCommand(dataDevice.getCurrentTag(), dataToWrite, dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			//if (this.dialog.isShowing())
			//	this.dialog.dismiss();
    		
			if (WriteD0configAnswer==null)
			{
				Toast.makeText(getApplicationContext(), "ERROR Write D0 CONFIG byte (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(WriteD0configAnswer[0]==(byte)0x01)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Write D0 CONFIG byte", Toast.LENGTH_SHORT).show();
    		}
    		else if(WriteD0configAnswer[0]==(byte)0xFF)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Write D0 CONFIG byte", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(WriteD0configAnswer[0]==(byte)0x00)
    		{
    			Toast.makeText(getApplicationContext(), "Write D0 CONFIG byte Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Write D0 CONFIG byte ERROR ", Toast.LENGTH_SHORT).show();
    		}    		
    		
		}
	}

	
	private class StartCheckEHenableTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(EHmanagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			//this.dialog.setMessage("Please, place your phone near the card");
			//this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);			  
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			CheckEHenableAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((CheckEHenableAnswer == null || CheckEHenableAnswer[0] == 1) && cpt <= 10)
				{
					CheckEHenableAnswer = NFCCommand.sendCheckEHenableCommand(dataDevice.getCurrentTag(), dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			//if (this.dialog.isShowing())
			//	this.dialog.dismiss();
    		
			if (CheckEHenableAnswer==null)
			{
				String valueByte = "XX";
				valueEHenableByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "ERROR Check EH ENABLE byte (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(CheckEHenableAnswer[0]==(byte)0x01)
    		{
				String valueByte = "XX";
				valueEHenableByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "ERROR Check EH ENABLE byte", Toast.LENGTH_SHORT).show();
    		}
    		else if(CheckEHenableAnswer[0]==(byte)0xFF)
    		{
    			String valueByte = "XX";
				valueEHenableByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "ERROR Check EH ENABLE byte", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(CheckEHenableAnswer[0]==(byte)0x00)
    		{
    			valueEHenableByte.setText(Helper.ConvertHexByteToString(CheckEHenableAnswer[1]).toUpperCase());
    			Toast.makeText(getApplicationContext(), "Check EH ENABLE byte Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			String valueByte = "XX";
				valueEHenableByte.setText(valueByte);
				Toast.makeText(getApplicationContext(), "Check EH ENABLE byte ERROR ", Toast.LENGTH_SHORT).show();
    		}    		
    		
		}
	}
	
	private class StartResetEHenableTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(EHmanagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			//this.dialog.setMessage("Please, place your phone near the card");
			//this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
	
				String value = valueEHenableByte.getText().toString();
								
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			ResetEHenableAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((ResetEHenableAnswer == null || ResetEHenableAnswer[0] == 1) && cpt <= 10)
				{
					ResetEHenableAnswer = NFCCommand.sendResetEHenableCommand(dataDevice.getCurrentTag(), dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			//if (this.dialog.isShowing())
			//	this.dialog.dismiss();
    		
			if (ResetEHenableAnswer==null)
			{
				Toast.makeText(getApplicationContext(), "ERROR Reset EH ENABLE (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(ResetEHenableAnswer[0]==(byte)0x01)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Reset EH ENABLE", Toast.LENGTH_SHORT).show();
    		}
    		else if(ResetEHenableAnswer[0]==(byte)0xFF)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Reset EH ENABLE", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(ResetEHenableAnswer[0]==(byte)0x00)
    		{
    			Toast.makeText(getApplicationContext(), "Reset EH ENABLE Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Reset EH ENABLE ERROR ", Toast.LENGTH_SHORT).show();
    		}    		
    		
		}
	}

	private class StartSetEHenableTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(EHmanagement.this);
		// can use UI thread here
		protected void onPreExecute() 
		{
			//this.dialog.setMessage("Please, place your phone near the card");
			//this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
	
				String value = valueEHenableByte.getText().toString();
								
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			SetEHenableAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
				while ((SetEHenableAnswer == null || SetEHenableAnswer[0] == 1) && cpt <= 10)
				{
					SetEHenableAnswer = NFCCommand.sendSetEHenableCommand(dataDevice.getCurrentTag(), dataDevice);
					cpt++;
				}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			//if (this.dialog.isShowing())
			//	this.dialog.dismiss();
    		
			if (SetEHenableAnswer==null)
			{
				Toast.makeText(getApplicationContext(), "ERROR Set EH ENABLE (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(SetEHenableAnswer[0]==(byte)0x01)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Set EH ENABLE", Toast.LENGTH_SHORT).show();
    		}
    		else if(SetEHenableAnswer[0]==(byte)0xFF)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Set EH ENABLE", Toast.LENGTH_SHORT).show();
    		}    		
    		else if(SetEHenableAnswer[0]==(byte)0x00)
    		{
    			Toast.makeText(getApplicationContext(), "Set EH ENABLE Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Set EH ENABLE ERROR ", Toast.LENGTH_SHORT).show();
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
			 else if (uid[0] == (byte) 0xD0)
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
			 	 ma.setBasedOnTwoBytesAddress(false);
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