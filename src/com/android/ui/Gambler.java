package com.android.ui;
import java.util.Random;

import android.util.Log;

/**
 * The Gambler is a maze solving algorithm that chooses 1 of 4
 * directions at random and moves in that direction as long
 * as no obstacle is in its way.
 * @author adam
 *
 */
public class Gambler implements RobotDriver {
	Robot lr;
	Random r = new Random();
	float initialPower;
	int pathlength = 0;

	@Override
	public void setRobot(Robot r) throws UnsuitableRobotException {
		lr = r;
		initialPower = lr.getCurrentBatteryLevel();

	}

	@Override
	public boolean drive2Exit() throws Exception {
		while(!lr.isAtGoal() && !lr.hasStopped() ){
			Log.v("drive", "step");
			int choice = r.nextInt(4);
			Log.v("choice", "" + choice);
			if(choice == 0){
				int ahead = 0;
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					ahead = lr.distanceToObstacleAhead();
					pathlength += ahead;
				}
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					if(ahead==1){
						Thread.sleep(250);
						lr.move(1, true);
						Globals.mv.postInvalidate();
					}
				}
			}
			
			else if(choice == 1){
				int behind = 0;
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					behind = lr.distanceToObstacleBehind();
					pathlength += behind;
				}
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					if(behind ==1){
						lr.move(1, false);
						Thread.sleep(250);
						Globals.mv.postInvalidate();
					}
				}
			}
			else if(choice == 2){
				int left = 0;
				if(lr.getEnergyForStepForward() + (lr.getEnergyForFullRotation()/4) < lr.getCurrentBatteryLevel()){
					left = lr.distanceToObstacleOnLeft();
					pathlength += left;
				}
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					if(left == 1){
						lr.rotate(90);
						Globals.mv.postInvalidate();
						Thread.sleep(250);
						lr.move(1, true);
						Globals.mv.postInvalidate();
						Thread.sleep(500);
					}
				}
			}
			else if(choice == 3){
				int right = 0;
				if(lr.getEnergyForStepForward() + (lr.getEnergyForFullRotation()/4) < lr.getCurrentBatteryLevel()){
					right = lr.distanceToObstacleOnRight();
					pathlength += right;
				}
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					if(right == 1){
						lr.rotate(-90);
						Globals.mv.postInvalidate();
						Thread.sleep(500);
						lr.move(1, true);
						Globals.mv.postInvalidate();
						Thread.sleep(500);
					}
				}
			
			}
			System.out.println("pathlength: " + pathlength);
			
			
		}
		if(lr.isAtGoal())
			return true;
		
		return false;
	}

	@Override
	public float getEnergyConsumption() {
		return initialPower - lr.getCurrentBatteryLevel();
	}

	@Override
	public int getPathLength() {
		return pathlength;
	}

}
