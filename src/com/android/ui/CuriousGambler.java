package com.android.ui;
import java.util.*;

import android.util.Log;

/**
 * The CuriousGambler is a robot driver that uses a random value
 * to choose the next move, like the Gambler driver.  However, the
 * CuriousGambler has a bias toward cells that have not been visted
 * as many times as the other adjacent cells.  It uses a Hashmap 
 * implementation to store cells that have been visited and their
 * visit counts.
 * @author adam
 *
 */

public class CuriousGambler implements RobotDriver {
	Robot lr;
	Random r = new Random();
	float initialPower;
	int pathlength = 0;
	HashMap<String, Integer> hash = new HashMap<String, Integer>(Globals.maze.mazeh*Globals.maze.mazew);
	int x, y;
	@Override
	public void setRobot(Robot r) throws UnsuitableRobotException {
		lr = r;
		initialPower = lr.getCurrentBatteryLevel();

	}

	@Override
	public boolean drive2Exit() throws Exception {
		//boolean deciding = true;
		while(!lr.isAtGoal() && !lr.hasStopped() ){
			int available = 0;
			int [] choices = new int[4];
			
			String pos = Globals.maze.px + "-" + Globals.maze.py;
			if(hash.containsKey(pos)){
				int i = hash.get(pos);
				hash.remove(pos);
				hash.put(pos, i+1);
			}
			else{
				hash.put(pos, 1);
			}
			/** we now fill an array with all the adjacent cells that have no obstacle
			 * in our way. 
			 */
			
			if(lr.distanceToObstacleAhead() == 1){
				choices[available] = 0;
				available++;
			}
			if(lr.distanceToObstacleBehind() == 1){
				choices[available] = 1;
				available++;
			}
			if(lr.distanceToObstacleOnLeft() == 1){
				choices[available] = 2;
				available++;
			}
			if(lr.distanceToObstacleOnRight() == 1){
				choices[available] = 3;
				available++;
			}
			
			/** using that array we narrow down the choices to
			 * those cells which have been visited the least,
			 * storing those results in levelchoices[]
			 */
			
		
			int visitLevel = Integer.MAX_VALUE;
			int numberAtThisLevel = 0;
			int[] levelChoices = new int[4];
			for(int j = 0; j < available; j++){
				getPositionVals(choices[j]);
				String posit = x + "-" + y;
				int timesVisited;
				
				if(hash.containsKey(posit)){
					timesVisited = hash.get(posit);
				}
				else
					timesVisited = 0;
				
				if(timesVisited < visitLevel){
					visitLevel = timesVisited;
					numberAtThisLevel = 0;
					levelChoices = new int[4];
					levelChoices[numberAtThisLevel] = choices[j];
					numberAtThisLevel++;
				}
				else if(timesVisited == visitLevel){
					levelChoices[numberAtThisLevel] = choices[j];
					numberAtThisLevel++;
				}
				
				
			}
			/** choose one of the remaining choices at random **/
			int random = r.nextInt(numberAtThisLevel);
			int choice = levelChoices[random];
			Log.v("choice", "" + choice);
			if(choice == 0){
				int ahead = 0;
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					ahead = lr.distanceToObstacleAhead();
					pathlength += ahead;
				}
				if(lr.getEnergyForStepForward() < lr.getCurrentBatteryLevel()){
					if(ahead==1){
						
						
						lr.move(1, true);
						Globals.mv.postInvalidate();
						Thread.sleep(250);
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

	private void getPositionVals(int i) {
		if(i == 0){
			x = Globals.maze.px + 1*Globals.maze.dx;
			y = Globals.maze.py +1*Globals.maze.dy;
		}
		if(i == 1){
			x = Globals.maze.px + -1*Globals.maze.dx;
			y = Globals.maze.py + -1*Globals.maze.dy;
		}
		if(i == 2){
			Globals.maze.rotate(1);
			x = Globals.maze.px + 1*Globals.maze.dx;
			y = Globals.maze.py + 1*Globals.maze.dy;
			Globals.maze.rotate(-1);
		}
		if(i == 3){
			Globals.maze.rotate(-1);
			x = Globals.maze.px + 1*Globals.maze.dx;
			y = Globals.maze.py + 1*Globals.maze.dy;
			Globals.maze.rotate(1);
		}
		
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
