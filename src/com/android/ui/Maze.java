package com.android.ui;

//import java.awt.*;

import android.graphics.Color;
import android.util.Log;

/**
 * Class handles the user interaction for the maze. 
 * It implements a state-dependent behavior that controls the display and reacts to key board input from a user. 
 * After refactoring the original code from an applet into a panel, it is wrapped by a MazeApplication to be a java application 
 * and a MazeApp to be an applet for a web browser. At this point user keyboard input is first dealt with a key listener
 * and then handed over to a Maze object by way of the keyDown method.
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class Maze {

	
	static final int view_width = 400;
	static final int view_height = 400;
	int zscale = view_height/2;

	static final int map_unit = 128;
	static final int view_offset = map_unit/8;
	static final int step_size = map_unit/4;

	private RangeSet rset;

	//private Image buffer_img;
	
	public int state;			// keeps track of the current GUI state, one of STATE_TITLE,...,STATE_FINISH, mainly used in redraw()
	// user can navigate 
	// title -> generating -(escape) -> title
	// title -> generation -> play -(escape)-> title
	// title -> generation -> play -> finish -> title
	// STATE_PLAY is the main state where the user can navigate through the maze in a first person view
	static final int STATE_TITLE = 1;
	static final int STATE_GENERATING = 2;
	static final int STATE_PLAY = 3;
	static final int STATE_FINISH = 4;
	
	private int percentdone = 0; // describes progress during generation phase
	public boolean showMaze;		 	// toggle switch to show overall maze on screen
	public boolean showSolution;		// toggle switch to show solution in overall maze on screen
	public boolean solving;			// toggle switch 

	final int viewz = 50;    
	int viewx, viewy, ang;
	int dx, dy;  // current direction
	int px, py ; // current position on maze grid (x,y)
	int walk_step;
	int view_dx, view_dy; // current view direction


	// debug stuff
	boolean deepdebug = false;
	boolean all_visible = false;
	boolean new_game = false;
	

	int mazew; // width of maze
	int mazeh; // height of maze
	// grid for maze
	Cells mazecells ;
	int[][] mazedists;
	Cells seencells ;
	//BSPNode bsp_root;
	
	public boolean map_mode; // true: display map of maze, false: do not display map of maze
	// map_mode is toggled by user keyboard input, causes a call to draw_map during play mode
	//int map_scale; relocated to mapdrawer

	// MapDrawer to perform drawing of maps
	public MapDrawer mapdrawer ;
	// Drawer to get the first person perspective
	FirstPersonDrawer firstpersondrawer ;
	// Mazebuilder is used to calculate a new maze together with a solution
	// The maze is computed in a separate thread. It is started in the local Build method.
	// The calculation communicates back by calling the local newMaze() method.
	public MazeBuilder mazebuilder;


	
	// The user picks a skill level between 0 - 9, a-f 
	// The following arrays transform this into corresponding dimensions for the result maze as well as the number of rooms and parts
	static int skill_x[] =     { 4, 12, 15, 20, 25, 25, 35, 35, 40, 60,
		70, 80, 90, 110, 150, 300 };
	static int skill_y[] =     { 4, 12, 15, 15, 20, 25, 25, 35, 40, 60,
		70, 75, 75,  90, 120, 240 };
	static int skill_rooms[] = { 0,  2,  2,  3,  4,  5, 10, 10, 20, 45,
		45, 50, 50,  60,  80, 160 };
	static int skill_partct[] = { 60,
		600, 900, 1200,
		2100, 2700, 3300,
		5000, 6000, 13500,
		19800, 25000, 29000,
		45000, 85000, 85000*4 };

	// fixing a value matching the escape key
	final int ESCAPE = 27;


	/**
	 * Call back method for MazeBuilder to communicate newly generated maze as reaction to a call to build()
	 * @param root node for traversals, used for the first person perspective
	 * @param cells encodes the maze with its walls and border
	 * @param dists encodes the solution by providing distances to the exit for each position in the maze
	 * @param startx current position, x coordinate
	 * @param starty current position, y coordinate
	 */
	public void newMaze(BSPNode root, Cells c, int dists[][], int startx, int starty) {
		Log.v("newmaze", "");
		showMaze = showSolution = solving = false;
		mazecells = c ;
		mazedists = dists;
		seencells = new Cells(mazew+1,mazeh+1) ;
		//bsp_root = root; // delegated to firstpersondrawer
		//dx = 1; dy = 0;
		setCurrentDirection(1, 0) ;
		setCurrentPosition(startx,starty) ;
		walk_step = 0;
		view_dx = dx<<16; 
		view_dy = dy<<16;
		ang = 0;
		map_mode = false;
		// mazew and mazeh have been set in build() method before mazebuider was called to generate a new maze.
		// reset map_scale in mapdrawer to a value of 10
		mapdrawer = new MapDrawer(view_width,view_height,map_unit,step_size, mazecells, seencells, 10, mazedists, mazew, mazeh) ;
	
		firstpersondrawer = new FirstPersonDrawer(view_width,view_height,map_unit,step_size, mazecells, seencells, 10, mazedists, mazew, mazeh, root) ;
		if(Globals.maze.firstpersondrawer == null)
			Log.v("NULL" , "NEWMAZE");
		// set the current state for the state-dependent behavior
		state = STATE_PLAY;
	
	}
	


	
	private void setCurrentPosition(int x, int y)
	{
		px = x ;
		py = y ;
	}
	private void setCurrentDirection(int x, int y)
	{
		dx = x ;
		dy = y ;
		
	}
	
	
	void buildInterrupted() {
		state = STATE_TITLE;
		redraw();
		mazebuilder = null;
	}

	final double radify(int x) {
		return x*Math.PI/180;
	}



	/**
	 * Updates graphical output on screen, called from MazeBuilder
	 */
	public void redraw() {
		
		redrawPlay();
	}

	
	/**
	 * Allows external increase to percentage in generating mode with subsequence graphics update
	 * @param pc gives the new percentage on a range [0,100]
	 * @return true if percentage was updated, false otherwise
	 */
	public boolean increasePercentage(int pc) {
		if (percentdone < pc && pc < 100) {
			percentdone = pc;
			if (state == STATE_GENERATING)
			{
				;
			}
			else
				dbg("Warning: Receiving update request for percentage while not in generating state, skip redraw.") ;
			return true ;
		}
		return false ;
	}
	/**
	 * Helper method for redraw to draw screen during the game. If map_mode is true, i.e. the user wants to see the overall map,
	 * the map is drawn only on a small rectangle inside the maze area. The current position is located is centered such that 
	 * it may happen that only a part of the map fits the display and is thus visible.
	 * @param gc graphics handler to manipulate screen
	 */
	private void redrawPlay() {
		firstpersondrawer.redrawPlay(px, py, view_dx, view_dy, walk_step, view_offset, rset, ang) ;
		
		if (map_mode) {
			mapdrawer.draw_map( px, py, walk_step, view_dx, view_dy, showMaze, showSolution) ;
			mapdrawer.draw_currentlocation(view_dx, view_dy) ;
		}
	}
	
	


	/////////////////////// Methods for debugging ////////////////////////////////
	private void dbg(String str) {
		System.out.println(str);
	}

	private void logPosition() {
		if (!deepdebug)
			return;
		dbg("x="+viewx/map_unit+" ("+
				viewx+") y="+viewy/map_unit+" ("+viewy+") ang="+
				ang+" dx="+dx+" dy="+dy+" "+view_dx+" "+view_dy);
	}
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Helper method for walk()
	 * @param dir
	 * @return true if there is no wall in this direction
	 */
	public boolean checkMove(int dir) {
		// obtain appropriate index for direction (CW_BOT, CW_TOP ...) 
		// for given direction parameter
		int a = ang/90;
		if (dir == -1)
			a = (a+2) & 3; // TODO: check why this works
		// check if cell has walls in this direction
		// returns true if there are no walls in this direction
		int[] masks = Cells.getMasks() ;
		return mazecells.hasMaskedBitsFalse(px, py, masks[a]) ;
	}



	private void rotateStep() {
		ang = (ang+1800) % 360;
		view_dx = (int) (Math.cos(radify(ang))*(1<<16));
		view_dy = (int) (Math.sin(radify(ang))*(1<<16));
		//moveStep();
	}

	

	private void rotateFinish() {
		//dx = (int) Math.cos(radify(ang));
		//dy = (int) Math.sin(radify(ang));
		setCurrentDirection((int) Math.cos(radify(ang)), (int) Math.sin(radify(ang))) ;
		
		logPosition();
	}

	private void walkFinish(int dir) {
		//px += dir*dx; py += dir*dy;
		setCurrentPosition(px + dir*dx, py + dir*dy) ;
		
		if (isEndPosition(px,py)) {
			Log.v("YOU", "FINISHED");
			state = STATE_FINISH;
			//redraw();
		}
		walk_step = 0;
		logPosition();
	}

	/**
	 * checks if the given position is outside the maze
	 * @param x
	 * @param y
	 * @return true if position is outside, false otherwise
	 */
	public boolean isEndPosition(int x, int y) {
		return x < 0 || y < 0 || x >= mazew || y >= mazeh;
	}



	synchronized void walk(int dir) {
		if (!checkMove(dir))
			return;
		for (int step = 0; step != 4; step++) {
			walk_step += dir;
			//moveStep();
		}
		
		walkFinish(dir);
	}

	synchronized void rotate(int dir) {
		int origang = ang;
		int steps = 4;
		
		for (int step = 0; step != steps; step++) {
			ang = origang + dir*(90*(step+1))/steps;
			rotateStep();
		}
		
		rotateFinish();
	}

	/**
	 * Helper method for solveStep
	 * @param n
	 */
	private void rotateTo(int n) {
		int a = ang/90;
		if (n == a)
			;
		else if (n == ((a+2) & 3)) {
			rotate(1);
			rotate(1);
		} else if (n == ((a+1) & 3)) {
			rotate(1);
		} else
			rotate(-1);
	}

	/**
	 * Method is only called in update() method (which serves redraw())
	 */
	synchronized private void solveStep() {
		solving = false;
		int d = mazedists[px][py];
		Globals.gw.setColor(Color.YELLOW);
		// case 1: we are not directly next to the final position
		if (d > 1) {
			int n = getDirectionIndexTowardsSolution(px,py,d);
			if (n == 4)
				dbg("HELP!");
			rotateTo(n);
			walk(1);
			//repaint(25);
			solving = true;
			return;
		}
		// case 2: we are one step close to the final position
		int n;
		int[] masks = Cells.getMasks() ;
		for (n = 0; n < 4; n++) {
			// skip this direction if there is a wall or border
			if (mazecells.hasMaskedBitsGTZero(px, py, masks[n]))
				continue;
			// stop if position in this direction is end position
			if (isEndPosition(px+MazeBuilder.dirsx[n], py+MazeBuilder.dirsy[n]))
				break ;
		}
		rotateTo(n);
		walk(1);
	}


	private int getDirectionIndexTowardsSolution(int x, int y, int d) {
		int[] masks = Cells.getMasks() ;
		for (int n = 0; n < 4; n++) {
			if (mazecells.hasMaskedBitsTrue(x,y,masks[n]))
				continue;
				int dx = MazeBuilder.dirsx[n];
				int dy = MazeBuilder.dirsy[n];
				int dn = mazedists[x+dx][y+dy];
				if (dn < d)
					return n ;
		}
		return 4 ;
	}

	


	public void init() {
		state = STATE_TITLE;
		rset = new RangeSet();
	}

	public void start() {
		redraw();
	}

	/**
	 * Method obtains a new Mazebuilder and has it compute new maze, 
	 * it is only used in keyDown()
	 * @param skill level determines the width, height and number of rooms for the new maze
	 */
	void build(int skill) {
		state = STATE_GENERATING;
		percentdone = 0;
		//redraw();
		mazebuilder = new MazeBuilderFalstad();
		mazew = skill_x[skill];
		mazeh = skill_y[skill];
		int roomcount = skill_rooms[skill];
		Log.v("mazeb", "buildmethod");
		mazebuilder.build(this, mazew, mazeh, roomcount, skill_partct[skill]);
		
		if(Globals.maze.firstpersondrawer == null)
			Log.v("NULL" , "IN BUILD");
		// mazebuilder calls back by calling newMaze() to return newly generated maze
	}
	

}
