package com.mygdx.runai;

import android.os.Bundle;
import android.view.KeyEvent;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication{

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		//initialize( new RunAI(), config);
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

}
