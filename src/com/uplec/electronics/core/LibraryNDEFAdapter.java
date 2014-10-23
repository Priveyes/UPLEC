// THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS 
// WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE 
// TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY 
// DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS 
// ARISING FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS 
// OF THE CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.

package com.uplec.electronics.core;

import com.uplec.electronics.R;


import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
//import android.util.Log;

public class LibraryNDEFAdapter extends BaseAdapter{

	private Context context;
    private List<NDEFStructure> listOfNDEFMessages;
    LayoutInflater inflater;
    
	public LibraryNDEFAdapter(Context context, List<NDEFStructure> list)
    {
        this.context = context;
        this.listOfNDEFMessages = list;
        inflater = LayoutInflater.from(context);
    }
	
	@Override
	public int getCount() {
		return listOfNDEFMessages.size();
	}

	@Override
	public Object getItem(int position) {
		return listOfNDEFMessages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		NDEFStructure entry = listOfNDEFMessages.get(position);
        if (convertView == null)
        {
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.library_row, null);
        }
        
        TextView messageBlock = (TextView) convertView.findViewById(R.id.messageNdef);
        messageBlock.setText(entry.getMessage());

        TextView dateBlock = (TextView) convertView.findViewById(R.id.dateNdef);
        dateBlock.setText(entry.getDate());

        return convertView;
	}

}
