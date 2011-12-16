package com.android.ui;

import java.util.Random;

import android.util.Log;

/**
 * The Wall Follower driver is a simple driver that follows
 * the wall on it's left hand side until it reaches the exit.
 * @author adam
 *
 */
public class WallFollower implements RobotDriver {
	Robot br;
	float initialPower;
	int pathlength = 0;
	@Override
	public void setRobot(Robot r) throws UnsuitableRobotException {
		br = r;
		initialPower = br.getCurrentBatteryLevel();
	}

	@Override
	public boolean drive2Exit() throws Exception {
		while(!br.isAtGoal() && !br.hasStopped()){
			
			if(2*br.getEnergyForStepForward() < br.getCurrentBatteryLevel()){
				int left = br.distanceToObstacleOnLeft();
				int ahead = br.distanceToObstacleAhead();
				Log.v("distanceONLEFT", "" + left);
				Log.v("distanceAHEAD", "" + ahead);
				
				
				if(left != 0 && br.getEnergyForFullRotation() < br.getCurrentBatteryLevel()){
					br.rotate(90);
					Globals.mv.postInvalidate();
					Thread.sleep(200);
					br.move(1, true);
					Globals.mv.postInvalidate();
					Thread.sleep(200);
					pathlength += 1;
				}
				else if(left == 0 && ahead != 0){
					br.move(1, true);
					Globals.mv.postInvalidate();
					Thread.sleep(200);
					pathlength += 1;
				}
				else if(left == 0 && ahead == 0){
					br.rotate(-90);
					Globals.mv.postInvalidate();
					Thread.sleep(200);
				}

				
				
			}
			if(br.getEnergyForStepForward() > br.getCurrentBatteryLevel()){
				break;
			}
		}
		if(br.isAtGoal())
			return true;
		
		return false;
	}

	@Override
	public float getEnergyConsumption() {
		return initialPower - br.getCurrentBatteryLevel();
	}

	@Override
	public int getPathLength() {
		return pathlength;
	}

}
