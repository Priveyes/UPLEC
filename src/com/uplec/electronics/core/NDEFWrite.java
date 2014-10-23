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

public class NDEFWrite extends Activity{
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private DataDevice ma = (DataDevice)getApplication();
	private byte[] NdefTextMessageToWrite;
	private byte[] NdefUrlMessageToWrite;
	private byte[] WriteStatus;
	private String NDEFTextMessage;
	private String NDEFUrlMessage;
	private long cpt = 0;
	
	byte[] GetSystemInfoAnswer = null;
	
	Button launchWrite;
	Button clearEdit;
	RadioButton rbOptionText;
	RadioButton rbOptionUrl;
	EditText ndefTextEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ndef_write);
		
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] {ndef,};
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
		ma = (DataDevice)getApplication();
		initListener();
	}
	
	@Override
    protected void onNewIntent(Intent intent) 
    {
    	// TODO Auto-generated method stub
    	super.onNewIntent(intent);
    	String action = intent.getAction();
    	if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
    	{
	    	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    	DataDevice dataDevice = (DataDevice)getApplication();
	    	dataDevice.setCurrentTag(tagFromIntent);
    	}
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
		launchWrite = (Button) findViewById(R.id.ndefWriteEdit);
		clearEdit = (Button) findViewById(R.id.ndefClearEdit);
		ndefTextEdit = (EditText) findViewById(R.id.etNdefWrite);
		rbOptionText = (RadioButton) findViewById(R.id.option1);
		rbOptionUrl = (RadioButton) findViewById(R.id.option2);
		
		clearEdit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				ndefTextEdit.setText("");
			}
		});
		
		rbOptionText.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				ndefTextEdit.setLines(6);
				ndefTextEdit.setText("");
			}
		});
		
		rbOptionUrl.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				ndefTextEdit.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT );
				ndefTextEdit.setText("http://www.");
				ndefTextEdit.setSelection(ndefTextEdit.getText().length());
			}
		});
		
		launchWrite.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (rbOptionText.isChecked() == true && ndefTextEdit.getText().length() > 0)
				{
					new StartWriteTask().execute();
					finishActivity(1);
				}	
				else if (rbOptionUrl.isChecked() == true && ndefTextEdit.getText().length() > 0)
				{
					//String tmp;
					
					//tmp = ndefTextEdit.getText().toString();
					//if (tmp.contains("http://www."))
					//{
					//	tmp = tmp.substring(11);
					//	ndefTextEdit.setText(tmp);
					//	Log.i("tmp == " + tmp, " ");
					//}
					new StartWriteTask2().execute();
					finishActivity(1);
				}
			}
		});
	}
	
	private class StartWriteTask extends AsyncTask<Void, Void, Void> {
	      private final ProgressDialog dialog = new ProgressDialog(NDEFWrite.this);
	      // can use UI thread here
	      protected void onPreExecute() 
	      {
	         this.dialog.setMessage("Programming...");
	         this.dialog.show();
	      }
	      // automatically done on worker thread (separate from UI thread)
	     
	      @Override
	      protected Void doInBackground(Void... params)
			{
				// TODO Auto-generated method stub
				DataDevice dataDevice = (DataDevice)getApplication();
				ma = (DataDevice)getApplication();
				
		    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
				  
		    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
		    	{
			        NDEFTextMessage = ndefTextEdit.getText().toString();
			        NdefTextMessageToWrite = NDEFMessages.ConvertStringToNDEF_Text_ByteArray(NDEFTextMessage, dataDevice);			        
			        			        
			        if(NdefTextMessageToWrite != null)
			        {

				        //1st : store TLF 4 bytes begining + write 0x00 to length byte(s)
				        byte[] TLV2write = new byte[4];
				        byte[] TLV2write_zeroLength = new byte[4];
				        for(int i=0; i<4; i++)
				        	TLV2write[i] = NdefTextMessageToWrite[i+4];
				        if (TLV2write[1] == 0xFF)
				        {
				        	TLV2write_zeroLength[0] = TLV2write[0];
				        	TLV2write_zeroLength[1] = 0x00;
				        	TLV2write_zeroLength[2] = 0x00;
				        	TLV2write_zeroLength[3] = 0x00;
				        }
				        else
				        {
				        	TLV2write_zeroLength[0] = TLV2write[0];
				        	TLV2write_zeroLength[1] = 0x00;
				        	TLV2write_zeroLength[2] = TLV2write[2];
				        	TLV2write_zeroLength[3] = TLV2write[3];
				        }
				        cpt = 0;
				        WriteStatus = null;
						while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
						{
							//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
							WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x01}, TLV2write_zeroLength, dataDevice);
							cpt ++;
						}
						
						if (WriteStatus[0] == 0x00)
						{
					        //2nd Write CCfield if o write error
					        byte[] CCfield2write = new byte[4];
					        for(int i=0; i<4; i++)
					        	CCfield2write[i] = NdefTextMessageToWrite[i];
					        cpt = 0;		
					        WriteStatus = null;
							while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
							{
								//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
								WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x00}, CCfield2write, dataDevice);
								cpt ++;
							}
							
							if (WriteStatus[0] == 0x00)
							{				        
						        //3rd write rest of the NDEF message if no previous errors
						        byte[] restNDEFmsg2write = new byte[NdefTextMessageToWrite.length];
						        for(int i=0; i<NdefTextMessageToWrite.length-8; i++)
						        	restNDEFmsg2write[i] = NdefTextMessageToWrite[i+8];
						        				       
					        	cpt = 0;		
					        	WriteStatus = null;
								while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
								{
									//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
									WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x02}, restNDEFmsg2write, dataDevice);
									cpt ++;
								}
								if (WriteStatus[0] == 0x00)
								{
									//4rth write store TLF 4 bytes begining with length byte(s)
									cpt = 0;		
									WriteStatus = null;
									while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
									{
										//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
										WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x01}, TLV2write, dataDevice);
										cpt ++;
									}
								}
							}
						}
			        }
			        else
			        {
			        	WriteStatus= new byte[] {(byte)0x02};//error code for message too long for memory
			        }
		    	}
				return null;

			}
	      // can use UI thread here
	      protected void onPostExecute(final Void unused)
	      {
	         if (this.dialog.isShowing())
	            this.dialog.dismiss();
	         if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) 
	         {	        	          
	        	 if (WriteStatus == null)
		         {
					Toast.makeText(ma.getApplicationContext(), "Error during write programming please retry", Toast.LENGTH_SHORT).show();
		         }	        		 
	        	 else if(WriteStatus[0] == (byte)0x00)	 
		         {
		        	 Toast.makeText(ma.getApplicationContext(), "Write OK", Toast.LENGTH_SHORT).show();
		        	 finish();
		         }	 
		         else if(WriteStatus[0] == (byte)0x02)
		         {
		        	 Toast.makeText(ma.getApplicationContext(), "The message you want to write is too long for the memory size", Toast.LENGTH_SHORT).show();
		         }
		         
		         else
		         {
					Toast.makeText(ma.getApplicationContext(), "Error during write programming please retry", Toast.LENGTH_SHORT).show();
		         }
	         }
	         else
	         {
	        	 Toast.makeText(ma.getApplicationContext(), "No tag detected", Toast.LENGTH_SHORT).show();
	         }
	      }
	   }
	
	private class StartWriteTask2 extends AsyncTask<Void, Void, Void> {
	      private final ProgressDialog dialog = new ProgressDialog(NDEFWrite.this);
	      // can use UI thread here
	      protected void onPreExecute() 
	      {
	         this.dialog.setMessage("Programming...");
	         this.dialog.show();
	      }
	      // automatically done on worker thread (separate from UI thread)
	      
	      @Override
	      protected Void doInBackground(Void... params)
			{
				// TODO Auto-generated method stub
				DataDevice dataDevice = (DataDevice)getApplication();
				ma = (DataDevice)getApplication();

		    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
				  
		    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
		    	{
		    		
			        NDEFUrlMessage = ndefTextEdit.getText().toString();
			        NdefUrlMessageToWrite = NDEFMessages.ConvertStringToNDEF_Url_ByteArray(NDEFUrlMessage, dataDevice);
					
			        if(NdefUrlMessageToWrite != null)
			        {
			        	//1st : store TLF 4 bytes begining + write 0x00 to length byte(s)
				        byte[] TLV2write = new byte[4];
				        byte[] TLV2write_zeroLength = new byte[4];
				        for(int i=0; i<4; i++)
				        	TLV2write[i] = NdefUrlMessageToWrite[i+4];
				        if (TLV2write[1] == 0xFF)
				        {
				        	TLV2write_zeroLength[0] = TLV2write[0];
				        	TLV2write_zeroLength[1] = 0x00;
				        	TLV2write_zeroLength[2] = 0x00;
				        	TLV2write_zeroLength[3] = 0x00;
				        }
				        else
				        {
				        	TLV2write_zeroLength[0] = TLV2write[0];
				        	TLV2write_zeroLength[1] = 0x00;
				        	TLV2write_zeroLength[2] = TLV2write[2];
				        	TLV2write_zeroLength[3] = TLV2write[3];
				        }
			        
				        cpt = 0;
						WriteStatus = null;						
						while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
						{
							//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
							WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x01}, TLV2write_zeroLength, dataDevice);
							cpt ++;
						}
						if (WriteStatus[0] == 0x00)
						{
					        //2nd Write CCfield if o write error
					        byte[] CCfield2write = new byte[4];
					        for(int i=0; i<4; i++)
					        	CCfield2write[i] = NdefUrlMessageToWrite[i];
					        cpt = 0;		
					        WriteStatus = null;
							while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
							{
								//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
								WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x00}, CCfield2write, dataDevice);
								cpt ++;
							}
							
							if (WriteStatus[0] == 0x00)
							{				        
						        //3rd write rest of the NDEF message if no previous errors
						        byte[] restNDEFmsg2write = new byte[NdefUrlMessageToWrite.length];
						        for(int i=0; i<NdefUrlMessageToWrite.length-8; i++)
						        	restNDEFmsg2write[i] = NdefUrlMessageToWrite[i+8];
						        				       
					        	cpt = 0;		
					        	WriteStatus = null;
								while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
								{
									//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
									WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x02}, restNDEFmsg2write, dataDevice);
									cpt ++;
								}
								if (WriteStatus[0] == 0x00)
								{
									//4rth write store TLF 4 bytes begining with length byte(s)
									cpt = 0;		
									WriteStatus = null;
									while ((WriteStatus == null || WriteStatus[0] == 1) && cpt <10)
									{
										//Used for DEBUG : Log.i("NDEFWrite", "Dan le WRITE MULTIPLE le cpt est � -----> " + String.valueOf(cpt));
										WriteStatus = NFCCommand.sendWriteMultipleBlockCommand(ma.getCurrentTag(), new byte[]{0x00,0x01}, TLV2write, dataDevice);
										cpt ++;
									}
								}
							}
						}
			        }
			        else
			        {
			        	WriteStatus= new byte[] {(byte)0x02};//error code for message too long for memory
			        }
		    	}
				return null;
			}
	      // can use UI thread here
	      protected void onPostExecute(final Void unused)
	      {
	         if (this.dialog.isShowing())
	            this.dialog.dismiss();
	         //finish();
	         if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) 
	         {	        	          
	        	 if (WriteStatus == null)
		         {
					Toast.makeText(ma.getApplicationContext(), "Error during write programming please retry", Toast.LENGTH_SHORT).show();
		         }	        		 
	        	 else if(WriteStatus[0] == (byte)0x00)	 
		         {
		        	 Toast.makeText(ma.getApplicationContext(), "Write OK", Toast.LENGTH_SHORT).show();
		        	 finish();
		         }	 
		         else if(WriteStatus[0] == (byte)0x02)
		         {
		        	 Toast.makeText(ma.getApplicationContext(), "The message you want to write is too long for the memory size", Toast.LENGTH_SHORT).show();
		         }
		         
		         else
		         {
					Toast.makeText(ma.getApplicationContext(), "Error during write programming please retry", Toast.LENGTH_SHORT).show();
		         }
	         }
	         else
	         {
	        	 Toast.makeText(ma.getApplicationContext(), "No tag detected", Toast.LENGTH_SHORT).show();
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
			 //if the tag has returned a god response 
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
				 	 ma.setProductName("Detected product");
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