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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
//import android.util.Log;

public class LockSectorManagement extends Activity
{
	private EditText value1;
	
	private Button buttonLockSector;

	private RadioButton rbOptionPwd1;
	private RadioButton rbOptionPwd2;
	private RadioButton rbOptionPwd3;

	private RadioButton rbOptionLockConfig00;
	private RadioButton rbOptionLockConfig01;
	private RadioButton rbOptionLockConfig10;
	private RadioButton rbOptionLockConfig11;
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private String blockName = null;
	private String blockValue = null;
	private long cpt = 0;
	
	byte[] GetSystemInfoAnswer = null;
	private byte[] LockSectorCommandAnswer = null;
	private byte PasswordNumber = (byte)0x01;
	private byte LockConfig = (byte)0x00;
	private byte[] SectorNumberAddress = null;
	private byte LockSectorByte = (byte)0x00;

	private static String GET_BLOCK_NAME = "blockname";
	private static String GET_BLOCK_VALUE = "blockvalue";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locksector_management);

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
		value1.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
		
		rbOptionPwd1 = (RadioButton) findViewById(R.id.pwd1);
		rbOptionPwd2 = (RadioButton) findViewById(R.id.pwd2);
		rbOptionPwd3 = (RadioButton) findViewById(R.id.pwd3);
		
		rbOptionLockConfig00 = (RadioButton) findViewById(R.id.LockConfig00);
		rbOptionLockConfig01 = (RadioButton) findViewById(R.id.LockConfig01);
		rbOptionLockConfig10 = (RadioButton) findViewById(R.id.LockConfig10);
		rbOptionLockConfig11 = (RadioButton) findViewById(R.id.LockConfig11);
		
		buttonLockSector = (Button) findViewById(R.id.button_writepassword);

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

		rbOptionLockConfig00.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				LockConfig = (byte)0x00;
			}
		});

		rbOptionLockConfig01.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				LockConfig = (byte)0x01;
			}
		});
		
		rbOptionLockConfig10.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				LockConfig = (byte)0x02;
			}
		});
		
		rbOptionLockConfig11.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				LockConfig = (byte)0x03;
			}
		});
		
		
		buttonLockSector.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				
				// TODO Auto-generated method stub
				new StartLockSectorTask().execute();
				
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});

	}

	private class StartLockSectorTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(LockSectorManagement.this);
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
				if(valueBlock1.length() == 0)
					valueBlock1 = "00";				
				value1.setText(Helper.FormatValueByteWrite(valueBlock1));
				
				int valueBlock1bis = Integer.parseInt(valueBlock1);	
				int intSectorAddress = valueBlock1bis * 0x20;				
				SectorNumberAddress = Helper.ConvertIntTo2bytesHexaFormat(intSectorAddress);							
				LockSectorByte = (byte)((byte)(LockConfig<<1) | (byte)(PasswordNumber<<3) | (byte)0x01);
				//Used for DEBUG : Log.i("ERROR == " + Helper.ConvertHexByteToString(LockConfig), "ERROR == " + Helper.ConvertHexByteToString(LockConfig));
				//Used for DEBUG : Log.i("ERROR == " + Helper.ConvertHexByteToString(PasswordNumber), "ERROR == " + Helper.ConvertHexByteToString(PasswordNumber));
				//Used for DEBUG : Log.i("ERROR == " + Helper.ConvertHexByteArrayToString(SectorNumberAddress), "ERROR == " + Helper.ConvertHexByteArrayToString(SectorNumberAddress));
				//Used for DEBUG : Log.i("ERROR == " + Helper.ConvertHexByteToString(LockSectorByte), "ERROR == " + Helper.ConvertHexByteToString(LockSectorByte));
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			LockSectorCommandAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
					//int valueBlock1 = Helper.ConvertStringToInt(value1.getText().toString());
					//int valueBlock2 = Helper.ConvertStringToInt(dataDevice.getProductName().substring(6, 2));
					//if (valueBlock2 <= valueBlock1)
					//{
						while ((LockSectorCommandAnswer == null || LockSectorCommandAnswer[0] == 1) && cpt <= 10)
						{
							LockSectorCommandAnswer = NFCCommand.sendLockSectorCommand(dataDevice.getCurrentTag(), SectorNumberAddress, LockSectorByte, dataDevice);
							cpt++;
						}
					//}
					//else
					//{
					//	LockSectorCommandAnswer[0] = (byte)0xFB;
					//}
	    	}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			if (this.dialog.isShowing())
				this.dialog.dismiss();
    		
			if (LockSectorCommandAnswer==null)
			{
				Toast.makeText(getApplicationContext(), "ERROR Lock Sector (No tag answer) ", Toast.LENGTH_SHORT).show();
			}
			else if(LockSectorCommandAnswer[0]==(byte)0x01)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Lock Sector ", Toast.LENGTH_SHORT).show();
    		}
    		else if(LockSectorCommandAnswer[0]==(byte)0xFA)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR : your RF product is not M24LRxx family ", Toast.LENGTH_SHORT).show();
    		}    
    		else if(LockSectorCommandAnswer[0]==(byte)0xFB)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR : sector number is not compliant with your RF product ", Toast.LENGTH_SHORT).show();
    		}    
    		else if(LockSectorCommandAnswer[0]==(byte)0xFF)
    		{
    			Toast.makeText(getApplicationContext(), "ERROR Lock Sector ", Toast.LENGTH_SHORT).show();
    		}    			
    		else if(LockSectorCommandAnswer[0]==(byte)0x00)
    		{
    			Toast.makeText(getApplicationContext(), "Lock Sector Sucessfull ", Toast.LENGTH_SHORT).show();
    			//finish();
    		}
    		else
    		{
    			Toast.makeText(getApplicationContext(), "Lock Sector ERROR ", Toast.LENGTH_SHORT).show();
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