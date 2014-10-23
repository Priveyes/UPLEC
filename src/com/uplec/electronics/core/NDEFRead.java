// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.uplec.electronics.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import com.uplec.electronics.R;

// import android.util.Log;

public class NDEFRead extends Activity {
     private NfcAdapter     mAdapter;
     private PendingIntent  mPendingIntent;
     private IntentFilter[] mFilters;
     private String[][]     mTechLists;
     private DataDevice     ma      = (DataDevice) getApplication();

     String                 dateNow;
     public static String   EXTRA_1 = "EXTRA_1";
     private TextView       textTypeNdefMessage;
     private TextView       textViewNdefMessage;
     private TextView       textViewDateMessage;

     @Override protected void onCreate(Bundle savedInstanceState) {
          // TODO Auto-generated method stub
          super.onCreate(savedInstanceState);
          setContentView(R.layout.ndef_read);

          mAdapter = NfcAdapter.getDefaultAdapter(this);
          mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
          IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
          mFilters = new IntentFilter[] { ndef, };
          mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class.getName() } };
          ma = (DataDevice) getApplication();

          textViewNdefMessage = (TextView) findViewById(R.id.textViewNdefMessage);
          textViewDateMessage = (TextView) findViewById(R.id.textViewDateMessage);
          textTypeNdefMessage = (TextView) findViewById(R.id.text_type_ndef_message);

          Bundle extras = getIntent().getExtras();
          if ( extras == null ) { return; }

          String sReadNdef = extras.getString(EXTRA_1);
          Date dNow = new Date();
          SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd 'at' hh:mm:ss");
          dateNow = ft.format(dNow);
          // textViewNdefMessage.setText(sReadNdef);
          textViewDateMessage.setText(dateNow);

          // if(sReadNdef.length()>10)
          // temp = sReadNdef.substring(0, 10);

          /*
           * if(sReadNdef.length() == 0)
           * textTypeNdefMessage.setText(".");
           * else if(temp.contains("http://www"))
           * textTypeNdefMessage.setText("URl");
           * else if(temp.contains("NAME : ") ||
           * temp.contains("FIRST NAME : ") ||
           * temp.contains("TITLE : ") ||
           * temp.contains("TEL : ") ||
           * temp.contains("URL : ") ||
           * temp.contains("EMAIL : "))
           * textTypeNdefMessage.setText("Vcard");
           * else
           * textTypeNdefMessage.setText("Text");
           */

          String strTypeNdefMessage = "-";

          if ( sReadNdef.length() == 0 ) {
               strTypeNdefMessage = ".";
          } else {
               if ( sReadNdef.startsWith("type:SMARTPOSTER") ) {
                    strTypeNdefMessage = "smartposter ";
                    // sReadNdef.replaceFirst("Type:SMARTPOSTER ", "");
               }
               if ( sReadNdef.startsWith("type:TEXT") ) {
                    strTypeNdefMessage = "text ";
                    // sReadNdef.replaceFirst("Type:TEXT ", "");
               }
               if ( sReadNdef.startsWith("type:URI") ) {
                    strTypeNdefMessage = "uri ";
                    // sReadNdef.replaceFirst("Type:URI ", "");
               }
               if ( sReadNdef.startsWith("type:MIME:vcard") ) {
                    strTypeNdefMessage = "MIME vcard ";
                    // sReadNdef.replaceFirst("Type:MIME:vcard ", "");
               }
               if ( sReadNdef.startsWith("type:MIME:application") ) {
                    strTypeNdefMessage = "MIME application ";
                    // sReadNdef=sReadNdef.replaceFirst("Type:MIME:application ", "");
               }
               if ( sReadNdef.startsWith("type:MIME:text") ) {
                    strTypeNdefMessage = "MIME text ";
                    // sReadNdef.replaceFirst("Type:MIME:text ", "");
               }
               if ( sReadNdef.startsWith("type:MIME:image") ) {
                    strTypeNdefMessage = "MIME image ";
                    // sReadNdef.replaceFirst("Type:MIME:image ", "");
               }
               if ( sReadNdef.startsWith("type:MIME:audio") ) {
                    strTypeNdefMessage = "MIME audio ";
                    // sReadNdef.replaceFirst("Type:MIME:audio ", "");
               }
               if ( sReadNdef.startsWith("type:MIME:video") ) {
                    strTypeNdefMessage = "MIME video ";
                    // sReadNdef.replaceFirst("Type:MIME:video ", "");
               }
               if ( sReadNdef.startsWith("type:MIME:other") ) {
                    strTypeNdefMessage = "MIME ";
                    // sReadNdef=sReadNdef.replaceFirst("Type:MIME:other ", "");
               }
               if ( sReadNdef.startsWith("type:EXTERNAL") ) {
                    strTypeNdefMessage = "EXTERNAL type ";
                    // sReadNdef=sReadNdef.replaceFirst("Type:MIME:other ", "");
               }
          }

          textTypeNdefMessage.setText(strTypeNdefMessage);

          android.text.util.Linkify.addLinks(textViewNdefMessage, 1);

          textViewNdefMessage.setText(sReadNdef);

          // SAVE TO FILE
          boolean stat = saveTagToFile(sReadNdef, dateNow, textTypeNdefMessage.getText().toString());
     }

     public boolean saveTagToFile(String message, String date, String type) {
          boolean status = true;
          String FILENAME = "NDEFMessages.txt";
          String string = "<m>" + message + "<d>" + date + "<t>" + type + "<end>";

          try {
               File folder = new File(Environment.getExternalStorageDirectory(), "NfcV-Reader");
               if ( !folder.exists() ) {
                    folder.mkdir();
               }

               File file = new File(folder, FILENAME);
               if ( !file.exists() ) {
                    file.createNewFile();
               }

               FileWriter fos = new FileWriter(file, true);
               fos.append(string);
               fos.close();
          }

          catch (Exception e) {
               e.printStackTrace();
               Toast.makeText(this, "impossible d'ecrire", Toast.LENGTH_SHORT).show();
               return false;
          }

          return status;
     }

     public boolean readFromFile(String fileName) {
          boolean status = false;

          try {
               File folder = new File(Environment.getExternalStorageDirectory(), "NfcV-Reader");

               if ( !folder.exists() ) {
                    folder.mkdir();
               }

               File file = new File(folder, fileName);

               if ( !file.exists() ) {
                    file.createNewFile();
               }

               InputStream in = new BufferedInputStream(new FileInputStream(file));
          } catch (Exception e) {
               e.printStackTrace();
               Toast.makeText(this, "impossible de lire", Toast.LENGTH_SHORT).show();
          }

          return status;
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
}
