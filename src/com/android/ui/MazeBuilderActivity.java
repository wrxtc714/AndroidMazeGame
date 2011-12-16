package com.android.ui;

import com.android.ui.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

/**
 * This activity consists of the screen which is shown whenever the maze is being 
 * generated, the maze generation is run in a seperate thread using Asynctask.
 * The UI is updated during the generation in the form of a progress bar.
 * @author adam
 *
 */
public class MazeBuilderActivity extends Activity implements View.OnKeyListener {
	private static final String TAG = "MazeBuilderActivity";

	ProgressBar bar;
	Button btn;
	int skill;
	int mode;
	Maze maze;
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			bar.incrementProgressBy(5);
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		Bundle b = this.getIntent().getExtras();
		skill = b.getInt("skill");
		mode = b.getInt("mode");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mazebuilderscreen);

		bar = (ProgressBar) findViewById(R.id.progressBar1);
		bar.setProgress(0);
		
		Log.v("SKILLLEVEL","" + skill);

		new BuildMazeTask().execute();
		

	}

	/** Seperate thread in which the maze is built **/
	class BuildMazeTask extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			Globals.maze = new Maze();
			publishProgress();
			Globals.maze.init();
			publishProgress();
			Globals.maze.build(skill);
			if(Globals.maze.mapdrawer == null)
				Log.v("NULL", "MAPDRAWER");
			publishProgress();
			Log.v(TAG, "width " + Globals.maze.mazew);
			
			return (null);
		}

		@Override
		protected void onProgressUpdate(Void... unused) {
			bar.incrementProgressBy(33);
		}

		@Override
		protected void onPostExecute(Void unused) {
			

			btn = new Button(MazeBuilderActivity.this);
			btn.setHeight(40);
			btn.setWidth(60);

			btn.setText("Go to Maze");

			MazeBuilderActivity.this.setContentView(btn);
			btn.setOnClickListener(new View.OnClickListener() {
			
				
				@Override
				public void onClick(View v) {
					
					Intent i = new Intent(MazeBuilderActivity.this,
							StatePlay.class);
					Bundle b = new Bundle();
					b.putInt("mode", mode);
					i.putExtras(b);
					startActivity(i);

				}
			});

		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Intent i = new Intent(MazeBuilderActivity.this, AMazeActivity.class);
			startActivity(i);
		}

		return false;
	}
	
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
	
		return true;
	}
	

}
