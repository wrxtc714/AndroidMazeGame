package com.android.ui;

/**
 * This interface specifies methods to operate a robot that resides in a maze on a particular location and with a particular direction.
 * An implementing class will configure the robot with sensors and a location within an existing maze.
 * It provides an operating platform for a robotdriver that experiences a maze (the real world) through the sensors and actors of this robot interface.
 * 
 * Note that a robot may be very limited in its mobility, e.g. only 90 degree left or right turns, which makes sense in the artificial terrain of a maze,
 * and its sensing capability, e.g. only a sensor on its front or left to detect remote obstacles. Left/right is a notion relative to the robot's direction 
 * or relative to the underlying maze. To avoid a confusion, the latter is considered a direction in an absolute sense and it may be better to describe it as 
 * north, south, east, west than up, down, right, left. 
 * 
 * A robot comes with a battery level that is depleted during operations such that a robot may actually stop if it runs out of energy.
 * This interface supports energy consideration. 
 * A robot may also stop when hitting an obstacle. 
 * 
 * WARNING: the use of CW_BOT/CW_TOP and (0,1) in Cells and Mazebuilder is inconsistent with the MapDrawer which has position (0,0) at the lower left and
 * a southbound direction on the map is (0,-1). Or in other words, the maze is drawn upside down by the MapDrawer. The rotation is calculated with 
 * polar coordinates (angle) towards a cartesian coordinate system where a southbound direction is (dx,dy)=(0,-1).
 * 
 * Implementing classes: movable robots with distance sensors of different kind. 
 * 
 * Collaborators: a maze class to be explored, a robotdriver class that operates robot
 * 
 * @author peterkemper
 *
 */
public interface Robot {

	/**
	 * Turn robot on the spot. If given degree is not supported by existing robot, method throws a corresponding exception. 
	 * For example, a robot may only be able to turn left (90) degrees or right (-90) degrees. The angle is measured in a counterclockwise manner
	 * as it is common for polar coordinates.
	 * @param degree specifies in which direction to turn (negative values turn right, positive values turn left), actual implementation may be limited to a few discrete value settings. 
	 * @throws UnsupportedArgumentException if the robot does not support a given degree value. 
	 */
	void rotate(int degree) throws UnsupportedArgumentException ;
	/**
	 * Moves robot forward or backward a given number of steps. A step matches a single cell.
	 * Since a robot may only have a distance sensor in its front, driving backwards may happen blindly as distance2Obstacle may not provide values for that direction.
	 * If the robot runs out of energy somewhere on its way, it stops, which can be checked by hasStopped() and by checking the battery level. 
	 * @param distance is the number of cells to move according to the robots current direction if forward = true, opposite direction if forward = false
	 * @param forward specifies if the robot should move forward (true) or backward (false)
	 * @throws HitObstacleException if robot hits an obstacle like a wall or border, which also make the robot stop, i.e. hasStopped() = true 
	 */
	void move(int distance, boolean forward) throws HitObstacleException ;
	/**
	 * Provides the current position as (x,y) coordinates for the maze cell as an array of length 2 with [x,y].
	 * Note that 0 <= x < width, 0 <= y < height of the maze. 
	 * @return array of length 2, x = array[0], y=array[1]
	 */
	int[] getCurrentPosition() ;
	/**
	 * Tells if current position is at the goal. Used to recognize termination of a search.
	 * Note that goal recognition is limited by the sensing functionality of robot such that isAtGoal returns false
	 * even if it is positioned directly at the exit but has no distance sensor towards the exit direction. 
	 * @return true if robot is at the goal and has a distance sensor in the direction of the goal, false otherwise
	 */
	boolean isAtGoal() ;
	/**
	 * Provides the current direction as (dx,dy) values for the robot as an array of length 2 with [dx,dy].
	 * Note that dx,dy are elements of {-1,0,1} and as in bitmasks masks in Cells.java and dirsx,dirsy in MazeBuilder.java.
	 * 
	 * @return array of length 2, dx = array[0], dy=array[1]
	 */	
	int[] getCurrentDirection() ;
	/**
	 * The robot has a given battery level (energy level) that it draws energy from during operations. 
	 * The particular energy consumption is device dependent such that a call for distance2Obstacle may use less energy than a move forward operation.
	 * If battery level <= 0 then robot stops to function and hasStopped() is true.
	 * @return current battery level, level is > 0 if operational. 
	 */
	float getCurrentBatteryLevel() ;
	/**
	 * Gives the energy consumption for a full 360 degree rotation.
	 * Scaling by other degrees approximates the corresponding consumption. 
	 * @return energy for a full rotation
	 */
	float getEnergyForFullRotation() ;
	/**
	 * Gives the energy consumption for moving 1 step forward.
	 * For simplicity, we assume that this equals the energy necessary to move 1 step backwards and that scaling by a larger number of moves is 
	 * approximately the corresponding multiple.
	 * @return energy for a single step forward
	 */
	float getEnergyForStepForward() ;
	/**
	 * Tells if the robot has stopped for reasons like lack of energy, hitting an obstacle, etc.
	 * @return true if the robot has stopped, false otherwise
	 */
	boolean hasStopped() ;
	/**
	 * Tells if a sensor can identify the goal in the robot's current forward direction from the current position.
	 * @return true if the goal (here: exit of the maze) is visible in a straight line of sight
	 * @throws UnsupportedMethodException if robot has no sensor in this direction
	 */
	boolean canSeeGoalAhead() throws UnsupportedMethodException ;
	/**
	 * Methods analogous to canSeeGoalAhead but for a the robot's current backward direction
	 * @return true if the goal (here: exit of the maze) is visible in a straight line of sight
	 * @throws UnsupportedMethodException if robot has no sensor in this direction
	 */
	boolean canSeeGoalBehind() throws UnsupportedMethodException ;
	/**
	 * Methods analogous to canSeeGoalAhead but for the robot's current left direction (left relative to forward)
	 * @return true if the goal (here: exit of the maze) is visible in a straight line of sight
	 * @throws UnsupportedMethodException if robot has no sensor in this direction
	 */
	boolean canSeeGoalOnLeft() throws UnsupportedMethodException ;
	/**
	 * Methods analogous to canSeeGoalAhead but for the robot's current right direction (right relative to forward)
	 * @return true if the goal (here: exit of the maze) is visible in a straight line of sight
	 * @throws UnsupportedMethodException if robot has no sensor in this direction
	 */
	boolean canSeeGoalOnRight() throws UnsupportedMethodException ;

	/**
	 * Tells the distance to an obstacle (a wall or border) for a the robot's current forward direction.
	 * Distance is measured in the number of cells towards that obstacle, e.g. 0 if current cell has a wall in this direction
	 * @return number of steps towards obstacle if obstacle is visible in a straight line of sight, Integer.MAX_VALUE otherwise
	 * @throws UnsupportedArgumentException if not supported by robot
	 */
	int distanceToObstacleAhead() throws UnsupportedMethodException ;
	/**
	 * Methods analogous to distanceToObstacleAhead but for the robot's current left direction (left relative to forward)
	 * @return number of steps towards obstacle if obstacle is visible in a straight line of sight, Integer.MAX_VALUE otherwise
	 * @throws UnsupportedArgumentException if not supported by robot
	 */
	int distanceToObstacleOnLeft() throws UnsupportedMethodException ;
	/**
	 * Methods analogous to distanceToObstacleAhead but for the robot's current right direction (right relative to forward)
	 * @return number of steps towards obstacle if obstacle is visible in a straight line of sight, Integer.MAX_VALUE otherwise
	 * @throws UnsupportedArgumentException if not supported by robot
	 */
	int distanceToObstacleOnRight() throws UnsupportedMethodException ;
	/**
	 * Methods analogous to distanceToObstacleAhead but for a the robot's current backward direction
	 * @return number of steps towards obstacle if obstacle is visible in a straight line of sight, Integer.MAX_VALUE otherwise
	 * @throws UnsupportedArgumentException if not supported by robot
	 */
	int distanceToObstacleBehind() throws UnsupportedMethodException ;
	

}
