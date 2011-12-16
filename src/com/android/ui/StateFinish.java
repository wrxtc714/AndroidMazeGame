package com.android.ui;

import com.android.ui.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.*;
import android.view.KeyEvent;

/**
 * The final screen in the Android application which is displayed
 * upon completion and allows the user to return to the launch stage.
 * @author adam
 *
 */
public class StateFinish extends Activity implements View.OnKeyListener {
	Button btn;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.statefinish);

		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(StateFinish.this, AMazeActivity.class);
				startActivity(i);
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Toast.makeText(this, "Back to Start", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(StateFinish.this, AMazeActivity.class);
			startActivity(i);
		}

		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		return false;
	}
}
