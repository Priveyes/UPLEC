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

// import android.util.Log;

public class StandardMenu extends Activity {

	private Button			buttonReadManagement;
	private Button			buttonWriteManagement;
	private Button			buttonFileManagement;
	private Button			buttonImageManagement;
	private Button			buttonPresentPasswordManagement;
	private Button			buttonLockSectorManagement;
	private Button			buttonEnergyHarvestingManagement;

	private NfcAdapter		mAdapter;
	private PendingIntent	mPendingIntent;
	private IntentFilter[]	mFilters;
	private String[][]		mTechLists;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.standard_menu);

		// Bundle objetbunble = this.getIntent().getExtras();
		// DataDevice dataDevice = (DataDevice)getApplication();

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mFilters = new IntentFilter[] { ndef, };
		mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };

		initListener();
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
		super.onPause();
	}

	private void initListener() {

		buttonReadManagement = (Button) findViewById(R.id.ReadManagementButton);
		buttonWriteManagement = (Button) findViewById(R.id.WriteManagementButton);
		buttonFileManagement = (Button) findViewById(R.id.FileManagementButton);
		buttonImageManagement = (Button) findViewById(R.id.ImageManagementButton);
		buttonPresentPasswordManagement = (Button) findViewById(R.id.PresentPasswordManagementButton);
		buttonLockSectorManagement = (Button) findViewById(R.id.LockSectorManagementButton);
		buttonEnergyHarvestingManagement = (Button) findViewById(R.id.EnergyHarvestingManagementButton);

		buttonReadManagement.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(StandardMenu.this, ScanRead.class);
				startActivity(i);
			}
		});

		buttonWriteManagement.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(StandardMenu.this, BasicWrite.class);
				startActivity(i);
			}
		});

		buttonFileManagement.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				// Intent i = new Intent(StandardMenu.this, FileManagement.class);
				Intent i = new Intent(StandardMenu.this, FileManagement.class);
				startActivity(i);
			}
		});

		buttonImageManagement.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				// Intent i = new Intent(StandardMenu.this, FileManagement.class);
				Intent i = new Intent(StandardMenu.this, ImageTransfer.class);
				startActivity(i);
			}
		});

		buttonPresentPasswordManagement.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(StandardMenu.this, PasswordManagement.class);
				startActivity(i);
			}
		});

		buttonLockSectorManagement.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(StandardMenu.this, LockSectorManagement.class);
				startActivity(i);
			}
		});

		buttonEnergyHarvestingManagement.setOnClickListener(new OnClickListener() {

			@Override public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(StandardMenu.this, EHmanagement.class);
				startActivity(i);
			}
		});

	}

}
