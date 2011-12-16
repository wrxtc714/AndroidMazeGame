package com.android.ui;


import java.util.HashMap;

import android.util.Log;

/**
 * The Wizard is a wall follower method that has available
 * to it the distance-from-exit values for each square in the maze.
 * So it walks along the shortest possible path until it reaches the exit.
 * @author adam
 *
 */
public class Wizard implements RobotDriver {
	Robot br;
	float initialPower;
	int pathlength = 0;
	Maze maze;
	int [] position;
	int [] direction;
	HashMap<String, Integer> hash = new HashMap<String, Integer>(Globals.maze.mazeh*Globals.maze.mazew);
	@Override
	public void setRobot(Robot r) throws UnsuitableRobotException {
		br = r;
		initialPower = br.getCurrentBatteryLevel();
	}
	
	public void setMaze(Maze m){
		maze = m;
	}

	@Override
	public boolean drive2Exit() throws Exception {
		
		
		int forward;
		int backward;
		int right;
		int left;
		while(!br.isAtGoal() && !br.hasStopped()){
			
			position = br.getCurrentPosition();
			direction = br.getCurrentDirection();
			
			String pos = position[0] + "-" + position[1];
			hash.put(pos, 1);
			
			if(Globals.maze.mazedists[position[0]][position[1]] == 1){
				Log.v("at 1 position", "now");
				if(br.canSeeGoalAhead()){
					br.move(1, true);
					Globals.mv.postInvalidate();
					break;
				}
				if(br.canSeeGoalBehind()){
					br.move(1, false);
					Globals.mv.postInvalidate();
					break;
				}
				if(br.canSeeGoalOnLeft()){
					br.rotate(90);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					br.move(1, true);
					Globals.mv.postInvalidate();
					break;
				}
				if(br.canSeeGoalAhead()){
					br.rotate(-90);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					br.move(1, true);
					Globals.mv.postInvalidate();
					break;
				}
			}
			
			forward = distanceVal(1);
			backward = distanceVal(-1);
			left = distanceVal(2);
			right = distanceVal(-2);
			//forward = SquareVisited(position, direction, 1 );
			//backward = SquareVisited(position, direction, -1 );
			//right = SquareVisited(position, direction, 2 );
			//left = SquareVisited(position, direction, -2 );
			
		
			int solDist = Integer.MAX_VALUE;
			int choice = -1;
		
			if(3*br.getEnergyForStepForward() < br.getCurrentBatteryLevel()){
				
				
				if(br.distanceToObstacleAhead() > 0 && forward < solDist){
					choice = 0;
					solDist = forward;
				}
				if(br.distanceToObstacleBehind() > 0 && backward < solDist){
					choice = 1;
					solDist = backward;
				}
				if(br.distanceToObstacleOnRight() > 0 && right < solDist){
					choice = 2;
					solDist = right;
				}
				if(br.distanceToObstacleOnLeft() > 0 && left < solDist){
					choice = 3;
					solDist = left;
				}
				
				System.out.println("choice: " + choice);
				//move after choice
				if(choice == 0){
					br.move(1, true);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					pathlength += 1;
				}
				if (choice == 1){
					br.move(1, false);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					pathlength +=1;
				}
				if (choice == 2){
					br.rotate(-90);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					br.move(1, true);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					pathlength +=1;
				}
				if (choice == 3){
					br.rotate(90);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					br.move(1, true);
					Globals.mv.postInvalidate();
					Thread.sleep(250);
					pathlength +=1;
				}
				else{
					System.out.println("help!");
				}
				if(br.getEnergyForStepForward() > br.getCurrentBatteryLevel()){
					break;
				}
			}
			
		}//while
		if(br.isAtGoal())
			return true;
		
		return false;
	}

	private int distanceVal(int i) {
		String pos = "";
		int x, y;
		if(i == 1 || i == -1){
			x = position[0] + i*direction[0];
			y = position[1] +i*direction[1];
			if(x < Globals.maze.mazew && y < Globals.maze.mazew && x >=0 && y >=0){
				pos = x + "-" + y; 
				if(!hash.containsKey(pos)){
					return Globals.maze.mazedists[x][y];
				}
			}
		
			return Integer.MAX_VALUE;
		}
		else if(i == 2 || i == -2){
			Globals.maze.rotate(1);
			i = i/2;
			x = Globals.maze.px + i*Globals.maze.dx;
			y = Globals.maze.py + i*Globals.maze.dy;
			Globals.maze.rotate(-1);
			if(x < Globals.maze.mazew && y < Globals.maze.mazew && x >=0 && y >=0){
				pos = x + "-" + y;
				if(!hash.containsKey(pos)){
					return Globals.maze.mazedists[x][y];
				}
				else{
					
				}
			}
			return Integer.MAX_VALUE;
			
		}
		else{
			return Integer.MAX_VALUE;
		}
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
