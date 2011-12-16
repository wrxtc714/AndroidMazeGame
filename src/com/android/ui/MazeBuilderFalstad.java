package com.android.ui;

import java.util.Random;

/**
 * This is the old maze generation algorithm written by Falstad.
 * My maze generation algorithm is found in MazeBuilderPrim.
 * @author Falstad
 *
 */
public class MazeBuilderFalstad extends MazeBuilder{
	
	public MazeBuilderFalstad(){}
	
	
	public MazeBuilderFalstad (boolean deterministic){
		if (deterministic){
			random = new Random(20984390);
		}
	}
	
	protected void generate() {
		// pick position (x,y) with x being random, y being 0
		int x = randNo(0, width-1) ;
		int firstx = x ; // constant to memorize initial x coordinate
		int y = 0;       // no need to memorize initial y coordinate
		int dir = 0;
		int origdir = dir;
		cells.setVirginToZero(x, y);
		while (true) { 
			int dx = dirsx[dir], dy = dirsy[dir];
			if (!cells.canGo(x, y, dx, dy)) { 
				dir = (dir+1) & 3;
				if (origdir == dir) {
					// if back at origin (firstx,0) stop.
					if (x == firstx && y == 0)
						break; // exit loop at this point
					int odr = origdirs[x][y];
					dx = dirsx[odr];
					dy = dirsy[odr];
					x -= dx;
					y -= dy;
					origdir = dir = randNo(0, 3);
				}
			} else {
				cells.deleteWall(x, y, dx, dy);
				x += dx;
				y += dy;
				cells.setVirginToZero(x, y);
				origdirs[x][y] = dir;
				origdir = dir = randNo(0, 3);
			}
		} 

		// compute temporary distances for an (exit) point (x,y) = (width/2,height/2) 
		// which is located in the center of the maze
		computeDists(width/2, height/2);

		// find most remote point in maze somewhere on the border
		int remotex = -1, remotey = -1, remotedist = 0;
		for (x = 0; x != width; x++) {
			if (dists[x][0] > remotedist) {
				remotex = x;
				remotey = 0;
				remotedist = dists[x][0];
			}
			if (dists[x][height-1] > remotedist) {
				remotex = x;
				remotey = height-1;
				remotedist = dists[x][height-1];
			}
		}
		for (y = 0; y != height; y++) {
			if (dists[0][y] > remotedist) {
				remotex = 0;
				remotey = y;
				remotedist = dists[0][y];
			}
			if (dists[width-1][y] > remotedist) {
				remotex = width-1;
				remotey = y;
				remotedist = dists[width-1][y];
			}
		}

		// recompute distances for an exit point (x,y) = (remotex,remotey)
		computeDists(remotex, remotey);

		// identify cell with the greatest distance
		setStartPositionToCellWithMaxDistance();

		// make exit position at true exit 
		setExitPosition(remotex, remotey);
	}


}
