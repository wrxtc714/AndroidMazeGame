package com.android.ui;

import android.util.Log;

/**
 * This is the Robot implementation that I used for the 
 * maze navigating robots. It has available sensors on the 
 * front, back, left, and right. It also has a battery power
 * value that is decreased as the robot moves through the maze.
 * If the power value gets to 0, the robot stops.
 * @author adam
 *
 */
public class BasicRobot implements Robot {
	Maze maze;
	int power = 10000;
	
	boolean frontSensor = false;
	boolean backSensor = true;
	boolean rightSensor = true;
	boolean leftSensor = false;
	
	int sensePower;
	int rotatePower;
	int movePower;
	
	boolean stopped = false;
		
	public BasicRobot(Maze robotMaze){
		maze = robotMaze;
		frontSensor = true;
		leftSensor = true;	
		sensePower = 1;
		rotatePower = 2;
		movePower = 3;
		
	};
	

	@Override
	public int[] getCurrentPosition() {
		
		// Provides the current position (x,y) of the robot in the maze
		//as an array of of length 2 [x,y]
		int [] position;
		position = new int[2];
		int x = Globals.maze.px;
		position[0] = x;
		int y = Globals.maze.py;
		position[1] = y;
		return position;
		
	}

	@Override
	/** tells the robot if it is at the end of the maze **/
	public boolean isAtGoal() {
		boolean b1 = false;
		boolean b2 = false;
		boolean b3 = false;
		boolean b4 = false;
		
		try {
			if(frontSensor)
				if(canSeeGoalAhead())
					b1 = true;
			if(backSensor)
				if(canSeeGoalBehind())
					b2 = true;
			if(rightSensor)
				if(canSeeGoalOnRight())
					b3 = true;
			if(leftSensor)
				if(canSeeGoalOnLeft())
					b4 = true;
		
			if(b1 || b2 || b3 || b4)
				if(Globals.maze.mazedists[Globals.maze.px][Globals.maze.py] == 0){
					
					return true;
					
				}
			

		} catch (UnsupportedMethodException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int[] getCurrentDirection() {
		// Returns the current direction in an array of size 2
		int direction[];
		direction = new int[2];
		int dx = Globals.maze.dx;
		direction[0] = dx;
		int dy = Globals.maze.dy;
		direction[1] = dy;
		return direction;
	}

	@Override
	public float getCurrentBatteryLevel() {		
		return power;
	}

	@Override
	public float getEnergyForFullRotation() {
		// returns energy needed for 360 degree rotation
		return 4 * rotatePower;
	}

	@Override
	public float getEnergyForStepForward() {
		//returns energy needed for 1 step forward or backward
		return movePower;
	}
	
	public float getEnergyForSensor() {
		return sensePower;
	}

	@Override
	public boolean hasStopped() {
		// has the robot stopped? Power level or obstacle
		return stopped;
	}

	@Override
	public void rotate(int degree) throws UnsupportedArgumentException {
		if (hasStopped())
			return;
		// Rotates robot on the spot
		if((degree % 90) != 0)
			throw new UnsupportedArgumentException();
		int dir = degree / 90;
		Globals.maze.rotate(dir);
	
		power -= rotatePower;  //reduce power level
		if(getCurrentBatteryLevel() <= 0)
			stopped = true;
		
	}
	
	/** not a real rotate that is displayed on screen.
	 * just a helper method for other methods to simulate
	 * a rotation in the maze for obstacle detection
	 * @param degree
	 * @throws UnsupportedArgumentException
	 */
	public void freeRotate(int degree) throws UnsupportedArgumentException {
		if (hasStopped())
			return;
		// Rotates robot on the spot
		if((degree % 90) != 0)
			throw new UnsupportedArgumentException();
		int dir = degree / 90;
		Globals.maze.rotate(dir);
		
	}
	

	@Override
	public void move(int distance, boolean forward) throws HitObstacleException {
		if (hasStopped())
			return;	
		int dir = -1;
		if(forward)
			dir = 1;
		Log.v("move direction", ""+dir);
		if(!isClear(dir)){
			throw new HitObstacleException();
		}
		else{
			Globals.maze.walk(dir);
			power -= movePower * distance;
			if(getCurrentBatteryLevel() <= 0)
				stopped = true;
		}
		
		
	}
	
	
	/** checks for obstacles in front of you */
	public boolean isClear(int dir) {
		return Globals.maze.checkMove(dir);
	}
		
	
	

	@Override
	public boolean canSeeGoalAhead() throws UnsupportedMethodException {
		int x, y;
		if (hasStopped())
			return false;
		if(!frontSensor)
			throw new UnsupportedMethodException();
		x = Globals.maze.px + 1*Globals.maze.dx;
		y = Globals.maze.py +1*Globals.maze.dy;
		if(Globals.maze.checkMove(1) == true && Globals.maze.isEndPosition(x, y) )
			return true;
		else
			return false;
	
	}

	@Override
	public boolean canSeeGoalBehind() throws UnsupportedMethodException {
		int x,y;
		if (hasStopped())
			return false;
		if(!backSensor)
			throw new UnsupportedMethodException();
		x = Globals.maze.px + -1*Globals.maze.dx;
		y = Globals.maze.py + -1*Globals.maze.dy;
		if(Globals.maze.checkMove(-1) == true && Globals.maze.isEndPosition(x, y) )
			return true;
		else
			return false;
		
	}

	@Override
	public boolean canSeeGoalOnLeft() throws UnsupportedMethodException {
		int x,y;
		if(!leftSensor)
			throw new UnsupportedMethodException();
		if (hasStopped())
			return false;
		Globals.maze.rotate(1);
		x = Globals.maze.px + 1*Globals.maze.dx;
		y = Globals.maze.py + 1*Globals.maze.dy;
		
		if(Globals.maze.checkMove(1) == true && Globals.maze.isEndPosition(x, y) ){
			Globals.maze.rotate(-1);
			return true;
		}
		else{
			Globals.maze.rotate(-1);
			return false;
		}
		
		
	}

	@Override
	public boolean canSeeGoalOnRight() throws UnsupportedMethodException {
		int x,y;
	
		if(!rightSensor)
			throw new UnsupportedMethodException();
		if (hasStopped())
			return false;
		Globals.maze.rotate(-1);
		x = Globals.maze.px + 1*Globals.maze.dx;
		y = Globals.maze.py + 1*Globals.maze.dy;
		
		if(Globals.maze.checkMove(1) == true && Globals.maze.isEndPosition(x, y) ){
			Globals.maze.rotate(1);
			return true;
		}
		else{
			Globals.maze.rotate(1);
			return false;
		}
		
	}

	/** all of the distanceTo methods for left, right,... return 0 if a wall is 
	 * next to the robot on that side and 1 if there is no wall on that side
	 */
	@Override
	public int distanceToObstacleAhead() throws UnsupportedMethodException {
		int distance = 0;
		if(!frontSensor)
			throw new UnsupportedMethodException();
		if (hasStopped())
			return Integer.MAX_VALUE;
		if(isClear(1)){
			distance = 1;
		}
		
		power -= sensePower;
		if(getCurrentBatteryLevel() <= 0)
			stopped = true;
		Log.v("Ahead", "" + distance);
		return distance;

		
	}

	@Override
	public int distanceToObstacleOnLeft() throws UnsupportedMethodException {
		int distance = 0;
		if(!leftSensor)
			throw new UnsupportedMethodException();
		if (hasStopped())
			return Integer.MAX_VALUE;
		Globals.maze.rotate(1);
		if(isClear(1)){
			distance = 1;
		}
		Globals.maze.rotate(-1);
		//int returnValue = checkMultipleMoveLR(1);
		power -= sensePower;
		if(getCurrentBatteryLevel() <= 0)
			stopped = true;
		Log.v("Left", "" + distance);
		return distance;
	}

	@Override
	public int distanceToObstacleOnRight() throws UnsupportedMethodException {
		int distance = 0;
		if(!rightSensor)
			throw new UnsupportedMethodException();
		if (hasStopped())
			return Integer.MAX_VALUE;
		Globals.maze.rotate(-1);
		if(isClear(1)){
			distance = 1;
		}
		Globals.maze.rotate(1);
		power -= sensePower;
		if(getCurrentBatteryLevel() <= 0)
			stopped = true;
		Log.v("Right", "" + distance);
		return distance;
	}

	@Override
	public int distanceToObstacleBehind() throws UnsupportedMethodException {
		int distance = 0;
		if (hasStopped())
			return Integer.MAX_VALUE;
		if(!backSensor)
			throw new UnsupportedMethodException();
		

		if(isClear(-1)){
			distance = 1;
		}
		power -= sensePower;
		if(getCurrentBatteryLevel() <= 0)
			stopped = true;
		Log.v("Behind", "" + distance);
		return distance;
	}

}
