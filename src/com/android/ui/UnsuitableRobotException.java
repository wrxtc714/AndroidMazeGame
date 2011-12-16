package com.android.ui;

/** 
 * Thrown if a robotdriver mode is passed a robot that does not have the
 * sufficient sensors available.
 * @author adam
 *
 */
public class UnsuitableRobotException extends Exception {

	
	private static final long serialVersionUID = -4922028511813606562L;

	public UnsuitableRobotException(){
		super();
	}
}
