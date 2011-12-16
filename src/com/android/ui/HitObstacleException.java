package com.android.ui;

/**
 * Thrown whenever the robot tries to move in a direction where
 * there is a wall.
 * @author adam
 *
 */
public class HitObstacleException extends Exception {

	private static final long serialVersionUID = -8500148352126330683L;

	public HitObstacleException() {
		super();
	}

}
