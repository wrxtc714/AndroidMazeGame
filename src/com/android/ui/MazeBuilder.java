package com.android.ui;

import java.util.Random;
import java.util.Vector;

import android.util.Log;



/**
 * This class has the responsibility to create a maze of given dimensions (width, height) together with a solution based on a distance matrix.
 * The Maze class depends on it. The MazeBuilder performs its calculations within its own separate thread such that communication between 
 * Maze and MazeBuilder operates as follows. Maze calls the build() method and provides width and height. Maze has a call back method newMaze that
 * this class calls to communicate a new maze and a BSP root node and a solution. 
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public abstract class MazeBuilder implements Runnable {

	// columns mean right, bottom, left, top (as implemented in getBit())
	// note that multiplication with -1 to a column switches directions
	public static int[] dirsx = { 1, 0, -1, 0 };
	public static int[] dirsy = { 0, 1, 0, -1 };
	int width, height, startx, starty;
	// conventional encoding of maze as a 2 dimensional integer array 
	// a single integer entry can hold information on walls, borders/bounds
	int[][] origdirs ;
	int[][] dists; // encodes the solution as distances towards the exit
	Cells cells = new Cells(0,0); // the internal representation of a maze as a matrix of cells

	Random random = new Random(); // random number generator to make randomized decisions
	Maze maze; // the maze that is constructed
	int partiters = 0;
	Vector<Seg> seglist;
	
	Thread buildThread; // computations are performed in own separated thread with this.run()
	int rooms; // number of rooms
	int expected_partiters;
	
	/**
	 * Constructor for a randomized maze generation
	 */
	public MazeBuilder(){}
	/**
	 * Constructor with option to make maze generation deterministic or random
	 */
	public MazeBuilder(boolean deterministic){
		if (true == deterministic)
		{
			// TODO: implement code that makes sure that if MazeBuilder.build is called for same skill level twice, same results
			//
//			random = new Random(20984390); //changes random to a Seeded Random number generator, so that you get the same results with
										   //the same input
		}
	}

	/**
	 * Generate an integer random number in interval [lo,hi] 
	 * @param lo
	 * @param hi
	 * @return random number within given range
	 */
	public int randNo(int lo, int hi) {
		//TODO: work on findbugs error message for negative min value where abs will not change sign!
		return (Math.abs(random.nextInt()) % (hi-lo+1)) + lo;
	}
	

	/**
	 * Method called in genNodes to determine the minimum of all such grades. 
	 * The method is static, i.e. it does not update internal attributes and just calculates the returned value.
	 * @param sl
	 * @param pe
	 * @return
	 */
	private static int grade_partition(Vector<Seg> sl, Seg pe) {
		int x  = pe.x;
		int y  = pe.y;
		int dx = pe.dx;
		int dy = pe.dy;
		int lcount = 0, rcount = 0, splits = 0;
		int inc = 1;
		if (sl.size() >= 100)
			inc = sl.size() / 50;
		// check all segments
		for (int i = 0; i < sl.size(); i += inc) {
			Seg se = (Seg) sl.elementAt(i);
			int df1x = se.x-x;
			int df1y = se.y-y;
			int sendx = se.x + se.dx;
			int sendy = se.y + se.dy;
			int df2x = sendx - x;
			int df2y = sendy - y;
			int nx = dy;
			int ny = -dx;
			int dot1 = df1x * nx + df1y * ny;
			int dot2 = df2x * nx + df2y * ny;
			if (getSign(dot1) != getSign(dot2)) {
				if (dot1 == 0)
					dot1 = dot2;
				else if (dot2 != 0) {
					splits++;
					continue;
				}
			}
			if (dot1 > 0 ||
					(dot1 == 0 && se.getDir() ==  pe.getDir())) {
				rcount++;
			} else if (dot1 < 0 ||
					(dot1 == 0 && se.getDir() == -pe.getDir())) {
				lcount++;
			} else {
				dbg("grade_partition problem: dot1 = "+dot1+", dot2 = "+dot2);
			}
		}
		return Math.abs(lcount-rcount) + splits * 3;
	}


	static int getSign(int num) {
		return (num < 0) ? -1 : (num > 0) ? 1 : 0;
	}

/**
 * 
 */
	abstract protected void generate();

/**
 * Establish valid exit position by breaking down wall to outside area.
 * @param remotex
 * @param remotey
 */
protected void setExitPosition(int remotex, int remotey) {
	int bit = 0;
	if (remotex == 0)
		bit = Cells.CW_LEFT;
	else if (remotex == width-1)
		bit = Cells.CW_RIGHT;
	else if (remotey == 0)
		bit = Cells.CW_TOP;
	else if (remotey == height-1)
		bit = Cells.CW_BOT;
	else
		dbg("Generate 1");
	cells.setBitToZero(remotex, remotey, bit);
	System.out.println("exit position set to zero: " + remotex + " " + remotey + " " + bit + ":" + cells.hasMaskedBitsFalse(remotex, remotey, bit));
}

	/**
	 * Sets the starting position (startx,starty) to the cell which is furthest away from the exit
	 */
	protected void setStartPositionToCellWithMaxDistance() {
		int x;
		int y;
		int d = 0;
		for (x = 0; x != width; x++)
			for (y = 0; y != height; y++) {
				if (dists[x][y] > d) {
					startx = x;
					starty = y;
					d = dists[x][y];
				}
			}
	}



	/**
	 * Computes distances to the exit position (ax,ay) for all cells in array dists.
	 * @param ax, exit position, x coordinate
	 * @param ay, exit position, y coordinate
	 */
	protected void computeDists(int ax, int ay) {
		int x, y;
		int inf = 99999999;
		// initialize the distance array with a value for infinity 
		for (x = 0; x != width; x++)
			for (y = 0; y != height; y++)
				dists[x][y] = inf;
		// set the final distance at the exit position
		dists[ax][ay] = 1;
		int[] masks = Cells.getMasks() ;
		boolean done;
		// go over this array as long as we can find something to do
		// MEMO: there are likely to be much smarter ways to distribute distances in a breadth first manner...
		// why not push identified cells with infinite distance on a "work to do" heap
		do {
			done = true;
			// check all entries in the distance array
			for (x = 0; x != width; x++)
				for (y = 0; y != height; y++) {
					int sx = x;
					int sy = y;
					int d = dists[sx][sy];
					if (d == inf) { // found work to do. 
						// Since the maze must have a way to the exit from any position the distance cannot be infinite
						done = false;
						continue;
					}
					// if the distance is not infinite, let's see if the cell has a neighbor that we can update and 
					// perform a depth first search on.
					int run = 0;
					while (true) {
						int n, nextn = -1;
						// check all four directions
						for (n = 0; n != 4; n++) {
							int nx = sx+dirsx[n];
							int ny = sy+dirsy[n];
							// if there is no wall in this direction and 
							// the reachable cell has a higher distance value
							// update the distance value and mark that cell as the next one
							if (cells.hasMaskedBitsFalse(sx, sy, masks[n]) &&
									dists[nx][ny] > d+1) {
								dists[nx][ny] = d+1;
								nextn = n;
							}
						}
						run++;
						if (nextn == -1)
							break; // exit the loop if we cannot find another cell to proceed with
						// update coordinates for next cell
						sx += dirsx[nextn];
						sy += dirsy[nextn];
						// update distance for next cell
						d++;
						// follow the nextn node on a depth-first-search path
					}
				}
		} while (!done);
	}


	/**
	 * Allocates space for a room of random dimensions in the maze.
	 * @return true if room is successfully placed, false otherwise
	 */
	private boolean placeRoom() {
		// get width and height of random size that are not too large
		// if too large return as a failed attempt
		int rw = randNo(3, 8);
		int rh = randNo(3, 8);
		if (rw >= width-4)
			return false;
		if (rh >= height-4)
			return false;
		// proceed for a given width and height
		// obtain a random position (rx,ry) such that room is located on as a rectangle with (rx,ry) and (rxl,ryl) as corner points
		// upper bound is chosen such that width and height of room fits maze area.
		int rx = randNo(1, width-rw-1);
		int ry = randNo(1, height-rh-1);
		int rxl = rx+rw-1;
		int ryl = ry+rh-1;
		// check all cells in this area if they already belong to a room
		// if this is the case, return false for a failed attempt
		if (cells.areaOverlapsWithRoom(rx, ry, rxl, ryl))
			return false ;
		// since the area is available, mark it for this room and remove all walls
		// from this on it is clear that we can place the room on the maze
		cells.markAreaAsRoom(rw, rh, rx, ry, rxl, ryl, random); 
		return true;
	}






	static final int map_unit = 128;
	int colchange;

	/**
	 * 
	 */
	private void genSegs() {
		int x, y;
		Vector<Seg> sl = new Vector<Seg>();

		for (y = 0; y != height; y++) {
			x = 0;
			while (x < width) {
				if (cells.hasNoWallOnTop(x, y)) {
					x++;
					continue;
				} 
				int startx = x;
				while (cells.hasWallOnTop(x, y)) {
					x++;
					if (x == width)
						break;
					if (cells.hasWallOnLeft(x, y))
						break;
				}
				sl.addElement(new Seg(x*map_unit, y*map_unit,
						(startx-x)*map_unit, 0, dists[startx][y], colchange));
			}
			x = 0;
			while (x < width) {
				if (cells.hasNoWallOnBottom(x, y)) {
					x++;
					continue;
				} 
				int startx = x;
				while (cells.hasWallOnBottom(x, y)) {
					x++;
					if (x == width)
						break;
					if (cells.hasWallOnLeft(x, y))
						break;
				}
				sl.addElement(new Seg(startx*map_unit, (y+1)*map_unit,
						(x-startx)*map_unit, 0,
						dists[startx][y], colchange));
			}
		} 
		for (x = 0; x != width; x++) {
			y = 0;
			while (y < height) {
				if (cells.hasNoWallOnLeft(x, y)) {
					y++;
					continue;
				} 
				int starty = y;
				while (cells.hasWallOnLeft(x, y)) {
					y++;
					if (y == height)
						break;
					if (cells.hasWallOnTop(x, y))
						break;
				}
				sl.addElement(new Seg(x*map_unit, starty*map_unit,
						0, (y-starty)*map_unit,
						dists[x][starty], colchange));
			}
			y = 0;
			while (y < height) {
				if (cells.hasNoWallOnRight(x, y)) {
					y++;
					continue;
				} 
				int starty = y;
				while (cells.hasWallOnRight(x, y)) {
					y++;
					if (y == height)
						break;
					if (cells.hasWallOnTop(x, y))
						break;
				}
				sl.addElement(new Seg((x+1)*map_unit, y*map_unit,
						0, (starty-y)*map_unit,
						dists[x][starty], colchange));
			}
		}
		seglist = sl;
		setPartitionBitForCertainSegments(sl);
		cells.setTopToOne(0, 0);
	}

	/**
	 * Set the partition bit to true for segments on the border and the direction is 0
	 * @param sl
	 */
	private void setPartitionBitForCertainSegments(Vector<Seg> sl) {
		for (int i = 0; i != sl.size(); i++) {
			Seg se = sl.elementAt(i);
			if (((se.x == 0 || se.x == width ) && se.dx == 0) ||
					((se.y == 0 || se.y == height) && se.dy == 0))
				se.partition = true;
		}
	}



	private BSPNode genNodes() {
		return genNodes(seglist);
	}

	/**
	 * 
	 * @param sl
	 * @return
	 */
	private BSPNode genNodes(Vector<Seg> sl) {
		// if there is no segment with a partition bit set to false, there is nothing else to do and we are at a leaf node
		if (countNonPartitions(sl) == 0)
			return new BSPLeaf(sl);
		// from the ones that have a partition bit set to false, pick a candidate with a low grade
		Seg pe = findPartitionCandidate(sl);
		// work on segment pe
		// mark pe as partitioned
		pe.partition = true;
		int x  = pe.x;
		int y  = pe.y;
		int dx = pe.dx;
		int dy = pe.dy;
		Vector<Seg> lsl = new Vector<Seg>();
		Vector<Seg> rsl = new Vector<Seg>();
		for (int i = 0; i != sl.size(); i++) {
			Seg se = (Seg) sl.elementAt(i);
			int df1x = se.x - x;
			int df1y = se.y - y;
			int sendx = se.x + se.dx;
			int sendy = se.y + se.dy;
			int df2x = sendx - x;
			int df2y = sendy - y;
			int nx = dy;
			int ny = -dx;
			int dot1 = df1x * nx + df1y * ny;
			int dot2 = df2x * nx + df2y * ny;
			if (getSign(dot1) != getSign(dot2)) {
				if (dot1 == 0)
					dot1 = dot2;
				else if (dot2 != 0) {
					// we need to split this
					int spx = se.x;
					int spy = se.y;
					if (dx == 0)
						spx = x;
					else
						spy = y;
					Seg sps1 = new Seg(se.x, se.y, spx-se.x, spy-se.y, se.dist, colchange);
					Seg sps2 = new Seg(spx, spy, sendx-spx, sendy-spy, se.dist, colchange);
					if (dot1 > 0) {
						rsl.addElement(sps1);
						lsl.addElement(sps2);
					} else {
						rsl.addElement(sps2);
						lsl.addElement(sps1);
					}
					sps1.partition = sps2.partition = se.partition;
					continue;
				}
			}
			if (dot1 > 0 || (dot1 == 0 && se.getDir() == pe.getDir())) {
				rsl.addElement(se);
				if (dot1 == 0)
					se.partition = true;
			} else if (dot1 < 0 || (dot1 == 0 && se.getDir() == -pe.getDir())) { 
				lsl.addElement(se);
				if (dot1 == 0)
					se.partition = true;
			} else {
				dbg("error xx 1 "+dot1);
			}
		}
		if (lsl.size() == 0)
			return new BSPLeaf(rsl);
		if (rsl.size() == 0)
			return new BSPLeaf(lsl);
		return new BSPBranch(x, y, dx, dy, genNodes(lsl), genNodes(rsl)); // recursion on both branches
	}

	/**
	 * @param sl
	 * @param pe
	 * @return
	 */
	private Seg findPartitionCandidate(Vector<Seg> sl) {
		Seg pe = null ;
		int bestgrade = 5000; // used to compute the minimum of all observed grade values, set to some high initial value
		int maxtries = 50; // constant, only used to determine skip
		// consider a subset of segments proportional to the number of tries, here 50, seems to randomize the access a bit
		int skip = (sl.size() / maxtries);
		if (skip == 0)
			skip = 1;
		for (int i = 0; i < sl.size(); i += skip) {
			Seg pk = (Seg) sl.elementAt(i);
			if (pk.partition)
				continue;
			partiters++;
			if ((partiters & 31) == 0) {
				if (maze.increasePercentage(partiters*100/expected_partiters))
				{
					// give main thread a chance to process keyboard events
					try {
						Thread.currentThread().sleep(10);
					} catch (Exception e) { }
				}
			}
			int grade = grade_partition(sl, pk);
			if (grade < bestgrade) {
				bestgrade = grade;
				pe = pk; // determine segment with smallest grade
			}
		}
		return pe;
	}

	/**
	 * Counts how many elements in the segment vector have their partition bit set to false
	 * @param sl
	 * @return
	 */
	private int countNonPartitions(Vector<Seg> sl) {
		int result = 0 ;
		for (int i = 0; i != sl.size(); i++)
			if (!(sl.elementAt(i)).partition)
				result++;
		return result;
	}

	private static void dbg(String str) {
		System.out.println("MazeBuilder: "+str);
	}



	/**
	 * Fill the given maze object with a newly computed maze according to parameter settings
	 * @param mz maze to be filled
	 * @param w width of requested maze
	 * @param h height of requested maze
	 * @param roomct number of rooms
	 * @param pc number of expected partiters
	 */
	public void build(Maze mz, int w, int h, int roomct, int pc) {
		
		width = w;
		height = h;
		maze = mz;
		rooms = roomct;
		cells = new Cells(w,h) ;
		origdirs = new int[w][h];
		dists = new int[w][h];
		expected_partiters = pc;
		Log.v("build", "mb");
		run();
		//buildThread = new Thread(this);
		//buildThread.start();
	}

	/**
	 * Main method to run construction of a new maze with a MazeBuilder in a thread of its own.
	 * This method is implicitly called by the build method when it sets up and starts a new thread for this object.
	 */
	public void run() {
		Log.v("Mazebuilder", "start");
		int tries = 250;

		colchange = randNo(0, 255);
		// create a maze where all walls and borders are up
		cells.initialize();
		// try to put as many rooms into the maze as requested but not more than the number of tries == 250
		while (tries > 0 && rooms > 0) {
			if (placeRoom())
				rooms--;
			else
				tries--;
		}
		// generate maze
		generate();
		Log.v("post", "generate");
		genSegs();
		partiters = 0;
		BSPNode root = genNodes();
		// dbg("partiters = "+partiters);
		// communicate results back to maze object
		Log.v("before", "newmaze");
		maze.newMaze(root, cells, dists, startx, starty);
		Log.v("Mazebuilder", "postNewmaze");
		if(Globals.maze.firstpersondrawer == null)
			Log.v("NULL" , "MAZEBUILDER");
		maze.mazeh = height;
		maze.mazew = width;
	}

	// TODO: bring thread communication up to date, stop is deprecated forever
	public void Interrupt() {
		buildThread.stop();
	}



}
