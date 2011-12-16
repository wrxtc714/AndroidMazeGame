package com.android.ui;


import com.android.ui.R;

import android.graphics.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.*;
import android.view.KeyEvent;
import android.util.*;
import android.graphics.*;

/**
 * The main maze navigation activity responsible for 
 * drawing the maze to the screen and receiving user 
 * input to move the robot around the maze in manual mode
 * and to display the map and solution.
 * @author adam
 *
 */

public class StatePlay extends Activity implements View.OnKeyListener {
	private static final String TAG = "StatePlay";
	Button btn;
	MapView mv;
	int mode;
	BasicRobot br;
	RobotDriver driver;
	
	

	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Bundle b = this.getIntent().getExtras();
		mode = b.getInt("mode");
		mv = new MapView(this);
		Globals.mv = mv;
		setContentView(Globals.mv);
		switch(mode){
			case 0: break; //Manual
			case 1:
				br = new BasicRobot(Globals.maze);
				driver = new Gambler();
				try {
					driver.setRobot(br);
				} catch (UnsuitableRobotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new RobotTask().execute();
				
				break; //Gambler
				
			case 2: 
				br = new BasicRobot(Globals.maze);
				driver = new CuriousGambler();
				try {
					driver.setRobot(br);
				} catch (UnsuitableRobotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new RobotTask().execute();
				break; //Curious Gambler
			
			case 3:
				br = new BasicRobot(Globals.maze);
				driver = new WallFollower();
				try {
					driver.setRobot(br);
				} catch (UnsuitableRobotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new RobotTask().execute();
				break; //Wall Follower
			case 4: 
				br = new BasicRobot(Globals.maze);
				driver = new Wizard();
				try {
					driver.setRobot(br);
				} catch (UnsuitableRobotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new RobotTask().execute();
				break; //Wizard
		}
	}
	
	class RobotTask extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			try {
				Log.v("drive", "2exit");
				if(driver.drive2Exit() == false){
					Toast.makeText(StatePlay.this, "Fuel Empty", Toast.LENGTH_SHORT).show();
					Intent i = new Intent(StatePlay.this, AMazeActivity.class);
					startActivity(i);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return (null);
		}
	}
	    

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if(mode == 0){
			switch (keyCode) {
			/** navigation key commands **/
			case KeyEvent.KEYCODE_DPAD_UP: 
				Globals.maze.walk(1);
				Globals.mv.invalidate();
				Log.v("walkstage", "complete");
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				//Globals.gw.ArrayInit();
				Globals.maze.walk(-1);
				Globals.mv.invalidate();
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				//Globals.gw.ArrayInit();
				Globals.maze.rotate(-1);
				mv.invalidate();
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				//Globals.gw.ArrayInit();
				Log.v("keypadleft", "down");
				Globals.maze.rotate(1);
				Log.v("rotate", "finished");
				Globals.mv.invalidate();
				return true;
			
			/** Map view key commands **/
			case KeyEvent.KEYCODE_M:
				Log.v("mapkey", "pressed");
				Globals.maze.map_mode = !Globals.maze.map_mode;
				Globals.mv.invalidate();
				return true;
			case KeyEvent.KEYCODE_Z:
				Globals.maze.showMaze = !Globals.maze.showMaze;
				Globals.mv.invalidate();
				return true;
			case KeyEvent.KEYCODE_S:
				Globals.maze.showSolution = !Globals.maze.showSolution;
				Globals.mv.invalidate();
				return true;
			case KeyEvent.KEYCODE_PLUS:
				Log.v("keycode", "plus");
				if(Globals.maze.showMaze){
					Globals.maze.mapdrawer.incrementMapScale();
					Log.v("inside", "plus keycode");
					Globals.mv.invalidate();
				}
				return true;
			case KeyEvent.KEYCODE_MINUS:
				if(Globals.maze.showMaze){
					Globals.maze.mapdrawer.decrementMapScale();
					Globals.mv.invalidate();
				}
				return true;
	
			case KeyEvent.KEYCODE_BACK:
				Toast.makeText(this, "Back to Start", Toast.LENGTH_SHORT).show();
				Intent i = new Intent(StatePlay.this, AMazeActivity.class);
				startActivity(i);
				return true;
			}
			
		}
		else{
			/** this is in a non-manual mode **/
			switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					Toast.makeText(this, "Back to Start", Toast.LENGTH_SHORT).show();
					Intent i = new Intent(StatePlay.this, AMazeActivity.class);
					startActivity(i);
					return true;
				case KeyEvent.KEYCODE_M:
					Log.v("mapkey", "pressed");
					Globals.maze.map_mode = !Globals.maze.map_mode;
					Globals.mv.invalidate();
					return true;
				case KeyEvent.KEYCODE_Z:
					Globals.maze.showMaze = !Globals.maze.showMaze;
					Globals.mv.invalidate();
					return true;
				case KeyEvent.KEYCODE_S:
					Globals.maze.showSolution = !Globals.maze.showSolution;
					Globals.mv.invalidate();
					return true;
				case KeyEvent.KEYCODE_PLUS:
					Log.v("keycode", "plus");
					if(Globals.maze.showMaze){
						Globals.maze.mapdrawer.incrementMapScale();
						Log.v("inside", "plus keycode");
						Globals.mv.invalidate();
					}//
					return true;
				case KeyEvent.KEYCODE_MINUS:
					if(Globals.maze.showMaze){
						Globals.maze.mapdrawer.decrementMapScale();
						Globals.mv.invalidate();
					}
					return true;
			}
			
		}

		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}
	
		
	
	class MapView extends View {	
		public MapView(Context context){
			super(context);
			setFocusableInTouchMode(true);
		    setFocusable(true);
		}
		


		@Override protected void onDraw(Canvas canvas) {
			if(Globals.maze.state == 4){
				Log.v("stateFinished", "activity");
				Intent i = new Intent(StatePlay.this, StateFinish.class);
				startActivity(i);
			}
			else{
				Globals.gw.setCanvas(canvas);
				Globals.gw.setColor(Color.DKGRAY);
				canvas.drawRect(0, 200, 400, 400, Globals.gw.paint);
				
				if(Globals.maze.firstpersondrawer == null){
				}
				else{
					Globals.maze.start();
				}
				
				
			}
			
			
			
		}
		
	}
		

}

