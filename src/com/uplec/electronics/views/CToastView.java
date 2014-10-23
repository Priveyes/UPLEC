package com.uplec.electronics.views;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uplec.electronics.R;

@EViewGroup ( R.layout.toast_custom ) public class CToastView extends LinearLayout {

     // =========================== Views
     @ViewById public ImageView ivImage;
     @ViewById public TextView  tvText;

     // =========================== Views
     /**
      * Basic constructor
      * 
      * @param context
      *             activity using this toast
      */
     public CToastView ( Context context ) {
          super(context);

     }

}
