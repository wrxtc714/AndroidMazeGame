package com.android.ui;

/**
 * This interface specifies a robot driver that operates a robot to escape from a given maze. 
 * 
 * Collaborators: Robot
 * 
 * Implementing classes: Gambler, CuriousGambler, WallFollower, Wizard
 * 
 * @author peterkemper
 *
 */
public interface RobotDriver {

	
	/**
	 * Assigns a robot platform to the driver. Not all robot configurations may be suitable such that the method 
	 * will throw an exception if the robot does not match minimal configuration requirements, e.g. providing a sensor
	 * to measure the distance to an object in a particular direction. 
	 * @param r robot to operate
	 * @throws UnsuitableRobotException if driver cannot operate the given robot
	 */
	void setRobot(Robot r) throws UnsuitableRobotException ;

	/**
	 * Drives the robot towards the exit given it exists and given the robot's energy supply lasts long enough. 
	 * @return true if driver successfully reaches the exit, false otherwise
	 * @throws exception if robot stopped due to an accident
	 */
	boolean drive2Exit() throws Exception ;
	
	/**
	 * Returns the total energy consumption of the journey
	 */
	float getEnergyConsumption() ;
	
	/**
	 * Returns the total length of the journey in number of cells traversed. The initial position counts as 0. 
	 */
	int getPathLength() ;
	
}
