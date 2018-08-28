package com.rthtech.bledemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends Activity {	
	Handler mhandler;
	Handler mhandlerSend;
	
	Context ctx;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent it = new Intent();
		String str;
		str = getResources().getConfiguration().locale.getCountry();
		if (str.equals("zh") || str.equals("CN")){
			it.setClass(this, ZhActivity.class);
		}else{
			it.setClass(this, SlActivity.class);
		}
		this.startActivity(it);
		finish();
	}
	
}
