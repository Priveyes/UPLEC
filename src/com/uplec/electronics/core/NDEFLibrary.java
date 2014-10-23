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
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
//import android.util.Log;

public class NDEFLibrary extends Activity{
	
	private ListView listView;
	private Button buttonDeleteAll;
	
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private DataDevice ma = (DataDevice)getApplication();
	
	List<NDEFStructure> listOfNDEFMessages = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ndef_library);
		
		listView = (ListView)findViewById(R.id.listView);
		buttonDeleteAll = (Button)findViewById(R.id.button_delete_all);
		
		mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mFilters = new IntentFilter[] {
                ndef,
        };
        
        mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };         
        ma = (DataDevice)getApplication();
		
       String all_file = getAllLibrary ();
       listOfNDEFMessages = parseTextFile(all_file);
       LibraryNDEFAdapter adapter = new LibraryNDEFAdapter(this, listOfNDEFMessages);
       listView.setAdapter(adapter);
       
       buttonDeleteAll.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				
	        	File f = new File(Environment.getExternalStorageDirectory() +"/NfcV-Reader/NDEFMessages.txt"); 
	        	boolean deleted = f.delete();
	        	if(deleted)
	        	{
	        		Toast toast = Toast.makeText(getApplicationContext(), "delete ok", Toast.LENGTH_SHORT);
		        	toast.show();
		        	finish();
	        	}
	        	else
	        	{
	        		Toast toast = Toast.makeText(getApplicationContext(), "No record found.", Toast.LENGTH_SHORT);
		        	toast.show();
	        	}
			}
		});
       
	}
	
	public List<NDEFStructure> parseTextFile (String allFile)
	{
		String[] eachMessage =null;
		String[] messages = null;
		String[] dates = null;
		String[] types = null;
		List<NDEFStructure> listNDEFMessages = new ArrayList<NDEFStructure>();
		
		eachMessage = allFile.split("<m>");
		
		for(int i=1; i<eachMessage.length;i++)
		{
			messages = eachMessage[i].split("<d>");
			dates = messages[1].split("<t>");
			types = dates[1].split("<end>");
			listNDEFMessages.add(new NDEFStructure(messages[0],dates[0],types[0]));
		}
		
		return listNDEFMessages;
	}
	
	public String getAllLibrary ()
	{
		String mReadString = "";
		String all_file = "";
		try{
		File f = new File(Environment.getExternalStorageDirectory() +"/NfcV-Reader/NDEFMessages.txt"); 
		FileInputStream fileIS = new FileInputStream(f); 
		BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS)); 
		
		while((mReadString = buf.readLine()) != null)
		{
			all_file += mReadString;
		}
		
		}
		catch (IOException e) 
		{
			 Toast toast = Toast.makeText(getApplicationContext(), "No NDEF message", Toast.LENGTH_SHORT);
        	 toast.show();
        	 finish();
		}
		return all_file;
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
}
