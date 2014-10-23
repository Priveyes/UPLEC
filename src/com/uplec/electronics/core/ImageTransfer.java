// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.uplec.electronics.core;

import com.uplec.electronics.R;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
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
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.view.inputmethod.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.util.Log;

public class ImageTransfer extends Activity
{

	Button buttonReadImageFromTag;
	Button buttonWriteImageInTag;

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private String blockName = null;
	private String blockValue = null;
	private long cpt = 0;
	
	byte[] GetSystemInfoAnswer = null;
	
	private byte[] WriteSingleBlockAnswer = null;
	private byte[] ReadMultipleBlockAnswer = null;
	private byte[] ReadFirstBlockAnswer = null;
	
	byte [] numberOfBlockToRead = null;
	
	private byte [] addressStart = null;
	private byte [] addressFirstStart = null;
	private byte[] dataToWrite = new byte[4];
	
	private byte[] bufferFile = null;;
	private int blocksToWrite = 0; 
	
	private boolean FileError = false;
	private boolean FileFormatSOFError = false;
	private boolean FileFormatEOFError = false;
	
	private EditText textWriteImageInTagName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_transfer_menu);

		Bundle objetbunble  = this.getIntent().getExtras();
		DataDevice dataDevice = (DataDevice)getApplication(); 

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0,new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] {ndef,};
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
		
		FileError = false;
		
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
		
		textWriteImageInTagName = (EditText) findViewById(R.id.editTextWriteImageInTagName);
		//value1.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		textWriteImageInTagName.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			    // TODO Auto-generated method stub
				int astart = textWriteImageInTagName.getSelectionStart();
				int aend = textWriteImageInTagName.getSelectionEnd();
						
				String FieldValue = s.toString();//.toUpperCase();
				
				if (FieldValue.length() == 0)
				{
					textWriteImageInTagName.setTextColor(0xffff0000); //RED color
					buttonWriteImageInTag.setClickable(false);
				}
				else
				{
					textWriteImageInTagName.setTextColor(0xff000000); //BLACK color									
					buttonWriteImageInTag.setClickable(true);
						
				}
				
				if (Helper.checkFileName(FieldValue) == false) 
				{
					textWriteImageInTagName.setTextKeepState(Helper.checkAndChangeFileName(FieldValue));
					textWriteImageInTagName.setSelection(astart-1, aend-1);
				}
				else
					textWriteImageInTagName.setSelection(astart, aend);
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
		
		buttonReadImageFromTag = (Button) findViewById(R.id.ReadImageFromTagButton);
		buttonWriteImageInTag = (Button) findViewById(R.id.WriteImageInTagButton);

		buttonReadImageFromTag.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub								
				  
				new StartReadImageFromTagTask().execute();
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});
		
		buttonWriteImageInTag.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub

				//Close Keyboard before stating activity
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(textWriteImageInTagName.getApplicationWindowToken(), 0);
	            
				new StartWriteImageInTagTask().execute();				
				//Used for DEBUG : Log.i("Write", "SUCCESS");
			}
		});
		
	}
			
	private class StartReadImageFromTagTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(ImageTransfer.this);
		// can use UI thread here
		protected void onPreExecute() 
		{									
			
			FileError = false;			
			FileFormatSOFError = false;
			FileFormatEOFError = false;
			
			this.dialog.setMessage("Please, place your phone near the card");
			this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
								
				int MemorySizeBlocksNumber = (Helper.ConvertStringToInt((dataDevice.getMemorySize().replace(" ", ""))) + 1);
				
				addressStart = Helper.ConvertIntTo2bytesHexaFormat(0x00);
		    	
				numberOfBlockToRead = Helper.ConvertIntTo2bytesHexaFormat(MemorySizeBlocksNumber);								
				
		         this.dialog.setMessage("Please, keep your phone close to the tag");
		         this.dialog.show();
				
				
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();			
			
			ReadMultipleBlockAnswer = null;
			ReadFirstBlockAnswer = null;
			if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
			  {
				
				// Read First 2 bytes for JPG start of file : FF D8
				addressFirstStart = Helper.ConvertIntTo2bytesHexaFormat(0x00);
				while((ReadFirstBlockAnswer == null || ReadFirstBlockAnswer[0] == 1) && cpt <= 10)
				{
					//Used for DEBUG : Log.i("ScanRead", "Dans le several read single block le cpt est � -----> " + String.valueOf(cpt));
					ReadFirstBlockAnswer = NFCCommand.sendReadSingleBlockCommand(dataDevice.getCurrentTag(),addressFirstStart,dataDevice);
					cpt ++;
				}
				cpt = 0;
				
				if (ReadFirstBlockAnswer[0] == (byte)0x00)
				{
					if (ReadFirstBlockAnswer[1] == (byte)0xFF && ReadFirstBlockAnswer[2] == (byte)0xD8)
					{
						
						//if(Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <=1) //ex: 1 byte to be read
						//{
						//	while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10 )
						//	{
						//		Log.i("ScanRead", "Dans le read SINGLE le cpt est � -----> " + String.valueOf(cpt));
						//		ReadMultipleBlockAnswer = NFCCommand.SendReadSingleBlockCommand(dataDevice.getCurrentTag(),addressStart, dataDevice);
						//		cpt ++;
						//	}
						//	cpt = 0;
						//}
						//else if(ma.isMultipleReadSupported() == false) //ex: LRIS2K
						if(dataDevice.isMultipleReadSupported() == false || Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <=1) //ex: LRIS2K
						{
							while((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10)
							{
								//Used for DEBUG : Log.i("ScanRead", "Dans le several read single block le cpt est � -----> " + String.valueOf(cpt));
								ReadMultipleBlockAnswer = NFCCommand.sendSeveralReadSingleBlockCommandsNbBlocksJpg(dataDevice.getCurrentTag(),addressStart,numberOfBlockToRead, dataDevice);
								cpt ++;
							}
							cpt = 0;
						}
						else if(Helper.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <32)
						{
							while((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10)
							{
								//Used for DEBUG : Log.i("ScanRead", "Dan le read MULTIPLE 1 le cpt est � -----> " + String.valueOf(cpt));
								ReadMultipleBlockAnswer = NFCCommand.sendReadMultipleBlockCommandCustomJpg(dataDevice.getCurrentTag(),addressStart,numberOfBlockToRead[1], dataDevice);
								cpt ++;
							}
							cpt = 0;
						}
						else
						{
							while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1) && cpt <= 10)
							{
								//Used for DEBUG : Log.i("ScanRead", "Dans le read MULTIPLE 2 le cpt est � -----> " + String.valueOf(cpt));
								ReadMultipleBlockAnswer = NFCCommand.sendReadMultipleBlockCommandCustom2Jpg(dataDevice.getCurrentTag(),addressStart,numberOfBlockToRead, dataDevice);
								cpt ++;
							}
							cpt = 0;
						}
						
						
							
						int MemorySizeBytes = (Helper.ConvertStringToInt((dataDevice.getMemorySize().replace(" ", ""))) + 1)*4;
						
						//if (ReadMultipleBlockAnswer[0] == (byte)0x00 && ReadMultipleBlockAnswer.length > MemorySizeBytes)
						if (ReadMultipleBlockAnswer[0] == (byte)0x00)
						{
							byte DataRead[] = new byte [ReadMultipleBlockAnswer.length+1];
							int i=1;
							while (i<=MemorySizeBytes)
							{
								DataRead[i-1] = ReadMultipleBlockAnswer[i];
								i++;
							}
							
							try{					
								
								File folder = new File(Environment.getExternalStorageDirectory(), "NfcV-Reader");
								if (!folder.exists())
									folder.mkdir();
								
								String strSaveFileName = "temp_ImageFromTag.jpg";
								
								FileOutputStream fileIS;
								fileIS = new FileOutputStream(Environment.getExternalStorageDirectory() + "/NfcV-Reader/" + strSaveFileName.replace(" ",""));
								
								//if (fileSize > MemorySizeBytes)
								//{
								//	Toast toast = Toast.makeText(getApplicationContext(), "File too big versus Memory size", Toast.LENGTH_SHORT);
								//	toast.show();
								//	//finish();
								//}
								//else if (fileSize == 0 )
								//{
								//	Toast toast = Toast.makeText(getApplicationContext(), "File empty", Toast.LENGTH_SHORT);
								//	toast.show();
								//	//finish();
								//} 			
								fileIS.write(DataRead);
								
								}
							catch (IOException e) 
							{
								FileError = true; 
								Toast toast = Toast.makeText(getApplicationContext(), "File cannot be created", Toast.LENGTH_SHORT);
					        	toast.show();
					        	//finish();
							}
							
						}
						else if (ReadMultipleBlockAnswer[0] == (byte)0xAE)
						{
							FileFormatEOFError = true;
						}
					}					
					else
					{
						FileFormatSOFError = true;
					}
			  }
			}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			if (this.dialog.isShowing())
				this.dialog.dismiss();
    	
			boolean ErrorLoading = true;
			

			if (FileFormatSOFError == true) 
			{
				Toast.makeText(getApplicationContext(), "Error : START OF FILE JPG format not found in Memory", Toast.LENGTH_SHORT).show();
			}
			else if (FileFormatEOFError == true) 
			{
				Toast.makeText(getApplicationContext(), "Error : END OF FILE JPG format not found in Memory", Toast.LENGTH_SHORT).show();
			}
			else if (FileError == false)
			{
				if (ReadMultipleBlockAnswer==null)
				{
					Toast.makeText(getApplicationContext(), "ERROR reading JPG picture (No tag answer)", Toast.LENGTH_SHORT).show();
				}
				else if(ReadMultipleBlockAnswer[0]==(byte)0x01)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR reading JPG picture", Toast.LENGTH_SHORT).show();
	    		}
	    		else if(ReadMultipleBlockAnswer[0]==(byte)0xFF)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR reading JPG picture", Toast.LENGTH_SHORT).show();
	    		}
	    		else if(ReadMultipleBlockAnswer[0]==(byte)0xAE)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR on format JPG picture", Toast.LENGTH_SHORT).show();
	    		}    				
	    		else if(ReadMultipleBlockAnswer[0]==(byte)0xAF)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR reading JPG picture", Toast.LENGTH_SHORT).show();
	    		}    				
	    		else if(ReadMultipleBlockAnswer[0]==(byte)0x00)
	    		{
		    		Toast.makeText(getApplicationContext(), "Reading JPG picture Sucessfull", Toast.LENGTH_SHORT).show();
		    		ErrorLoading = false;
		    		//finish();
	    		}
	    		else
	    		{
	    			Toast.makeText(getApplicationContext(), "Reading JPG picture ERROR", Toast.LENGTH_SHORT).show();
	    		}    		
			}
			
			if (ErrorLoading)
			{
   			 	final Integer[] imageResIds = new Integer[] {R.drawable.selector_image_transfer_error};
     			ImageView iv = (ImageView)findViewById(R.id.imageViewTransfer);
     			iv.setImageResource(imageResIds[0]);
			}
			else
			{
    			try
    			{
    			  //File fileName = new File(root, file);
    			  //String dirFileName = fileName.toString();
    			  String dirFileName = Environment.getExternalStorageDirectory() + "/NfcV-Reader/"+"temp_ImageFromTag.jpg";
    			  //Toast.makeText(getApplicationContext(), dirFileName, Toast.LENGTH_LONG).show();
    			  ImageView iv = (ImageView)findViewById(R.id.imageViewTransfer);
    			  iv.setImageBitmap(BitmapFactory.decodeFile(dirFileName));
    			  //super.setContentView(iv);
    			}
    			catch(Exception e)
    			{
    			  Toast.makeText(getApplicationContext(), "ERROR : Image can't be load correctly", Toast.LENGTH_LONG).show();	    				        
    			}
    			
    			// TODO Auto-generated method stub
				Intent i = new Intent(ImageTransfer.this, ImageTransferDisplay.class);
				startActivity(i);
				
			}
			
		}		
	}

	private class StartWriteImageInTagTask extends AsyncTask<Void, Void, Void> 
	{
		private final ProgressDialog dialog = new ProgressDialog(ImageTransfer.this);
		// can use UI thread here
		protected void onPreExecute() 
		{						
			
			this.dialog.setMessage("Please, keep your phone near the card ... write on going");
			this.dialog.show();

			DataDevice dataDevice = (DataDevice)getApplication();
			
	    	GetSystemInfoAnswer = NFCCommand.sendGetSystemInfoCommandCustom(dataDevice.getCurrentTag(),dataDevice);
			  
	    	if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
	    	{
								
				int MemorySizeBytes = (Helper.ConvertStringToInt((dataDevice.getMemorySize().replace(" ", ""))) + 1)*4;
				FileError = false;
				
				//read binary file
				try{
					File folder = new File(Environment.getExternalStorageDirectory(), "NfcV-Reader");
					if (!folder.exists())
						folder.mkdir();
					
					String strFileName = textWriteImageInTagName.getText().toString();					
					
					File f = new File(Environment.getExternalStorageDirectory() + "/NfcV-Reader/" + strFileName.replace(" ","")); 
					FileInputStream fileIS = new FileInputStream(f); 
					BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS)); 
					int fileSize = (int) f.length();
					
					//if ((fileSize % 4) > 0)
					//{
					//	Toast toast = Toast.makeText(getApplicationContext(), "File format error : multiple of 4 bytes need (4 bytes per block)", Toast.LENGTH_SHORT);
					//	toast.show();
					//	FileError = true;
					//	//finish();
					//}
					//else if (fileSize > MemorySizeBytes)
					if (fileSize > MemorySizeBytes)
					{
						Toast toast = Toast.makeText(getApplicationContext(), "File too big for your memory Tag", Toast.LENGTH_SHORT);
			        	toast.show();
			        	FileError = true;
			        	//finish();
					}
					else if (fileSize == 0 )
					{
						Toast toast = Toast.makeText(getApplicationContext(), "File empty", Toast.LENGTH_SHORT);
			        	toast.show();
			        	FileError = true;
			        	//finish();
					}
					else
					{
						bufferFile = new byte[MemorySizeBytes];	 					 
						fileIS.read(bufferFile);
						int initialByteSize = fileSize;
						if ((fileSize % 4) > 0)
						{
				         	int additionBytes = 4-(fileSize % 4);
				         	for (int i=0; i<additionBytes; i++)
				         		bufferFile[fileSize+i+1] = (byte)0xFF;
				         	fileSize += additionBytes;
				         	blocksToWrite = (int) fileSize / 4;								
						}
						else	
							blocksToWrite = (int) fileSize / 4;				
					
						//check jpg format
						if(!(bufferFile[0] == (byte)0xFF && bufferFile[1] == (byte)0xD8))
						{
							FileFormatSOFError = true;	
							FileError = true;
						}
						else if (!(bufferFile[initialByteSize-2] == (byte)0xFF && bufferFile[initialByteSize-1] == (byte)0xD9))
						{
							FileFormatEOFError = true;
							FileError = true;
						}
					}
				}
				catch (IOException e) 
				{					
					 Toast toast = Toast.makeText(getApplicationContext(), "File not found or not formated as binary file", Toast.LENGTH_SHORT);
		        	 toast.show();
		        	 FileError = true;
		        	 //finish();
				}								
				
	    	}
		}
		
		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{
			// TODO Auto-generated method stub
			cpt = 0;
			DataDevice dataDevice = (DataDevice)getApplication();
			
			if (FileError == false)
			{
				WriteSingleBlockAnswer = null;
				if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
		    	{	
		    		int ResultWriteAnswer = 0;
					for (int iAddressStart = 0; iAddressStart < blocksToWrite; iAddressStart++)
					{
						addressStart = Helper.ConvertIntTo2bytesHexaFormat(iAddressStart);
						dataToWrite[0] = bufferFile[iAddressStart*4];
						dataToWrite[1] = bufferFile[iAddressStart*4+1];
						dataToWrite[2] = bufferFile[iAddressStart*4+2];
						dataToWrite[3] = bufferFile[iAddressStart*4+3];
						cpt=0;					
						WriteSingleBlockAnswer = null;
						while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1) && cpt <= 10)
						{	
							WriteSingleBlockAnswer = NFCCommand.sendWriteSingleBlockCommand(dataDevice.getCurrentTag(), addressStart, dataToWrite, dataDevice);
							cpt++;
						}
						if(WriteSingleBlockAnswer[0]!=(byte)0x00)
						{
							ResultWriteAnswer = ResultWriteAnswer + 1;
							WriteSingleBlockAnswer[0] = (byte)0xE1;
							return null;
						}
					}
					if(ResultWriteAnswer>0)
						WriteSingleBlockAnswer[0] = (byte)0xFF;
					else
						WriteSingleBlockAnswer[0] = (byte)0x00;
		    	}
			}
			return null;
		}
		
		// can use UI thread here
		protected void onPostExecute(final Void unused)
		{
			if (this.dialog.isShowing())
				this.dialog.dismiss();
    		
			if (FileFormatSOFError == true)				
			{
				Toast.makeText(getApplicationContext(), "JPF format error (no SOF) ", Toast.LENGTH_SHORT).show();
			}	
			else if (FileFormatEOFError == true)
			{
				Toast.makeText(getApplicationContext(), "JPF format error (no EOF) ", Toast.LENGTH_SHORT).show();
			}
			else if (FileError == false) 
			{
				if (WriteSingleBlockAnswer==null)				
				{
					Toast.makeText(getApplicationContext(), "ERROR image transfer (No tag answer) ", Toast.LENGTH_SHORT).show();
				}
				else if(WriteSingleBlockAnswer[0]==(byte)0x01)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR image transfer ", Toast.LENGTH_SHORT).show();
	    		}
	    		else if(WriteSingleBlockAnswer[0]==(byte)0xFF)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR image Transfer ", Toast.LENGTH_SHORT).show();
	    		} 
	    		else if(WriteSingleBlockAnswer[0]==(byte)0xE1)
	    		{
	    			Toast.makeText(getApplicationContext(), "ERROR image Transfer process stopped", Toast.LENGTH_SHORT).show();
	    		}
	    		else if(WriteSingleBlockAnswer[0]==(byte)0x00)
	    		{
	    			Toast.makeText(getApplicationContext(), "Image transfer Sucessfull ", Toast.LENGTH_SHORT).show();
	    			//finish();
	    		}
	    		else
	    		{
	    			Toast.makeText(getApplicationContext(), "Image Transfer ERROR ", Toast.LENGTH_SHORT).show();
	    		}
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