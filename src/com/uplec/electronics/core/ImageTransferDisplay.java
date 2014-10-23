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
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.util.Log;

public class ImageTransferDisplay extends Activity
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
		setContentView(R.layout.image_transfer_display);

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
		
	}		 
	
}