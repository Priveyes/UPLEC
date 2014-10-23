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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
//import android.util.Log;

public class Scan extends Activity
{
	private DataDevice dataDevice;
	
	private TextView uidTextView;
	private TextView manufacturerTextView;
	private TextView productNameTextView;
	private TextView technoTextView;
	private TextView dsfidTextView;
	private TextView afiTextView;
	private TextView memorySizeTextView;
	private TextView blockSizeTextView;
	private TextView icReferenceTextView;	
	private Button basicFormatButton;
	private Button ndefFormatButton;
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	DataDevice ma = (DataDevice)getApplication();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan);
		//Used for DEBUG : Log.i("NEW INTENT", "!!! INTENT SCAN !!!");
		
		//Log.i("NFCCOmmand", "Response18 ");
		
		mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mFilters = new IntentFilter[] {ndef,};
        mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };         
        ma = (DataDevice)getApplication();
		dataDevice = (DataDevice)getApplication();
		//Log.i("NFCCOmmand", "Response21a ");
		fillLayoutScan();
		//Log.i("NFCCOmmand", "Response20 ");
		init_listener_button();
		//Log.i("NFCCOmmand", "Response19 ");
	}
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);     
		DataDevice ma = (DataDevice)getApplication();
		ma.setCurrentTag(tagFromIntent);
		//Log.i("NFCCOmmand", "Response20 ");
	}
	
	 @Override
	    protected void onPause() 
	    {
	    	// TODO Auto-generated method stub
		 	//Used for DEBUG : Log.v("NFCappsActivity.java", "ON PAUSE SCAN ACTIVITY");
		 //Log.i("NFCCOmmand", "Response21b ");
	    	super.onPause();
	    	mAdapter.disableForegroundDispatch(this);
	    	return;
	    }
	
	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		//Log.i("NFCCOmmand", "Response22a ");
		super.onResume();
		//Log.i("NFCCOmmand", "Response22b ");
		mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
		//Log.i("NFCCOmmand", "Response22c ");
	}
	
	private void init_listener_button() 
	{
		// TODO Auto-generated method stub
		basicFormatButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				//Used for DEBUG : Log.i("BTN", "BASIC FORMAT");
				Intent i = new Intent(Scan.this, StandardMenu.class);
				startActivity(i);
			}
		});
		
		// TODO Auto-generated method stub
		ndefFormatButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				//Used for DEBUG : Log.i("BTN", "NDEF FORMAT");
				Intent i = new Intent(Scan.this, NDEFMenu.class);
				startActivity(i);
			}
		});

	}

	private void fillLayoutScan()
	{
		//Log.i("NFCCOmmand", "Response24a ");
		uidTextView = (TextView) this.findViewById(R.id.uid);
        manufacturerTextView = (TextView) this.findViewById(R.id.manufacturer);
        productNameTextView = (TextView) this.findViewById(R.id.product_name);
        technoTextView = (TextView) this.findViewById(R.id.techno);
        dsfidTextView = (TextView) this.findViewById(R.id.dsfid);
        afiTextView  = (TextView) this.findViewById(R.id.afi);
        memorySizeTextView  = (TextView) this.findViewById(R.id.memory_size);
        blockSizeTextView  = (TextView) this.findViewById(R.id.number_block);
        icReferenceTextView  = (TextView) this.findViewById(R.id.ref);
        
        basicFormatButton = (Button) this.findViewById(R.id.basicFormatButton);
        ndefFormatButton = (Button) this.findViewById(R.id.ndefFormatButton);        
        
        uidTextView.setText(dataDevice.getUid().toUpperCase());
        manufacturerTextView.setText(dataDevice.getManufacturer());
        productNameTextView.setText(dataDevice.getProductName());
        technoTextView.setText(dataDevice.getTechno());
        dsfidTextView.setText(dataDevice.getDsfid().toUpperCase());
        afiTextView.setText(dataDevice.getAfi().toUpperCase());
        
        String sTemp = dataDevice.getMemorySize();
        sTemp = Helper.StringForceDigit(sTemp, 4);
        int iTemp = Helper.ConvertStringToInt(sTemp);
		iTemp++;
		
		/* Previously displayed as Hexadecimal number 
		if(iTemp<=256)
		{
			sTemp = Helper.ConvertHexByteToString((byte)iTemp);
		}
		else
		{
			byte[] bTemp = Helper.ConvertIntTo2bytesHexaFormat(iTemp);
			sTemp = Helper.ConvertHexByteArrayToString(bTemp);
		}
		memorySizeTextView.setText("Number of block = " + sTemp);
		*/
		
		//Memory size displayed in decimal
		memorySizeTextView.setText("Number of block = " + iTemp);
        
        sTemp = dataDevice.getBlockSize();
        sTemp = Helper.StringForceDigit(sTemp, 4);
		iTemp = Helper.ConvertStringToInt(sTemp);
		iTemp++;
		sTemp = Helper.ConvertHexByteToString((byte)iTemp);
        blockSizeTextView.setText("Number of byte of one block = " + sTemp);
        icReferenceTextView.setText(dataDevice.getIcReference().toUpperCase());
        //Log.i("NFCCOmmand", "Response24b ");
	}
}
