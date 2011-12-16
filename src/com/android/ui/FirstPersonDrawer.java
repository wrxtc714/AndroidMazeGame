/**
 * 
 */
package com.android.ui;
import android.graphics.*;
import android.util.Log;
///import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Point;
import java.util.Vector;

/**
 * This class encapsulates all functionality for drawing the current view at the maze from a first person perspective.
 * It is an drawing agent with redraw_play as its public method to redraw the maze while the user plays, i.e. navigates through the maze.
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class FirstPersonDrawer {
	
	// keep local copies of values determined in Maze.java, all values are set in the constructor call
	// values are basically constants or shared data structures across Maze, MapDrawer and FirstPersonDrawer
	// constants, i.e. set in constructor call with values that are not subject to change in Maze
	int view_width = 400;
	int view_height = 400;
	int map_unit = 128;
	int step_size = map_unit/4;
	// map scale may be adjusted by user input, controlled in Maze
	int map_scale = 10 ;
	// shared data structures
	Cells mazecells ; // the current maze with its encoding of walls and borders
	Cells seencells ; // cells whose walls are currently visible
	int[][] mazedists ; // encodes the solution by the distance to the exit for each cell
	// width and height of map are chosen according to a user given skill level
	int mazew ; // width of current maze, i.e. number of cells for x coordinate
	int mazeh ; // height of current maze, i.e. number of cells for y coordinate
	// node is determined in MazeBuilder when creating the maze
	BSPNode bsp_root ;
	
	// angle, used in rotations
	int ang = 0 ;  // set in redraw_play
	
	// used in bounding box
	int zscale = view_height/2;

	// where to draw on
	//Graphics gc ;
	
	final int viewz = 50;  // constant from Maze.java

	/**
	 * Constructor
	 * @param mazecells TODO
	 * @param seencells TODO
	 * @param map_scale TODO
	 * @param mazedists TODO
	 * @param mazew TODO
	 * @param mazeh TODO
	 * @param gc TODO   MEMO: unlikely to be known when constructor is called, likely to by null
	 */
	public FirstPersonDrawer(int width, int height, int map_unit, int step_size, Cells mazecells, Cells seencells, int map_scale, int[][] mazedists, int mazew, int mazeh, BSPNode bsp_root){
		view_width = width ;
		view_height = height ;
		this.map_unit = map_unit ;
		this.step_size = step_size ;
		this.mazecells = mazecells ;
		this.seencells = seencells ;
		this.map_scale = map_scale ;
		this.mazedists = mazedists ;
		this.mazew = mazew ;
		this.mazeh = mazeh ;
		this.bsp_root = bsp_root ; 
		
		ang = 0 ;
		zscale = view_height/2;
	}

	// current position (px,py) scaled by map_unit and modified by view direction is stored in (viewx, viewy)
	int viewx ; // current position for view, x coordinate, calculated in redraw_play
	int viewy ; // current position for view, y coordinate, calculated in redraw_play
	// view direction (view_dx,view_dy)
	int view_dx ; // set in redraw_play
	int view_dy ; // set in redraw_play
	// set of ranges
	RangeSet rset ; // set in redraw_play
	
	// debug stuff
	boolean deepdebug = false;
	boolean all_visible = false;
	int traverse_node_ct;
	int traverse_ssector_ct;
	int drawrect_ct ;
	int drawrect_late_ct ;
	int drawrect_segment_ct ;
	int nesting = 0;
	
	private void dbg(String str) {
		System.out.println(str);
	}
	/// end debug stuff
	
	/**
	 * Draws the first person view on the screen during the game
	 * @param gc graphics handler to manipulate screen
	 * @param px x coordinate of current position, only used to get viewx
	 * @param py y coordinate of current position, only used to get viewy
	 * @param view_dx view direction, x coordinate
	 * @param view_dy view direction, y coordinate
	 * @param walk_step, only used to get viewx and viewy
	 * @param view_offset, only used to get viewx and viewy
	 * @param rset
	 * @param ang
	 */
	public void redrawPlay( int px, int py, int view_dx, int view_dy, int walk_step, int view_offset, RangeSet rset, int ang) {
		// new adjustment
		this.rset = rset ;
		this.view_dx = view_dx ;
		this.view_dy = view_dy ;
		this.ang = ang ;
		
		// calculate view
		viewx = (px*map_unit+map_unit/2) + viewd_unscale(view_dx*(step_size*walk_step-view_offset));
		viewy = (py*map_unit+map_unit/2) + viewd_unscale(view_dy*(step_size*walk_step-view_offset));
		// update graphics
		// draw black background on lower half
		//Globals.gw.setColor(Color.BLACK);
		//Globals.gw.fillRect(0, 0, view_width, view_height/2);
		// draw dark gray background on upper half
		
		
		// set color to white and draw what ever can be seen from the current position
		//Globals.gw.setColor(Color.WHITE);
		rset.set(0, view_width-1); // reset set of ranges to set with single new element (0,width-1)
		// debug: reset counters
		traverse_node_ct = traverse_ssector_ct =
			drawrect_ct = drawrect_late_ct = drawrect_segment_ct = 0;
		//
		traverse_node(bsp_root);
	}
	
	
	final int viewd_unscale(int x) {
		return x >> 16;
	}
	/**
	 * Helper method for clip3d
	 * @param denom
	 * @param num
	 * @param fp is a parameter whose internal attributes p1 and p2 may be modified
	 * @return
	 */
	static private boolean clipt(int denom, int num, FloatPair fp) {
		if (denom > 0) {
			double t = num * 1.0 / denom;
			if (t > fp.p2)
				return false;
			if (t > fp.p1)
				fp.p1 = t;	 // update fp
		} else if (denom < 0) {
			double t = num * 1.0 / denom;
			if (t < fp.p1)
				return false;
			if (t < fp.p2)
				fp.p2 = t; // update fp
		} else if (num > 0)
			return false;
		return true;
	}
	/**
	 * Helper method for bbox_visible and drawrect
	 * @param rp may be modified 
	 * @return
	 */
	static private boolean clip3d(RangePair rp) {
		int x1 = rp.x1, z1 = rp.z1, x2 = rp.x2, z2 = rp.z2;

		if (z1 > -4 && z2 > -4)
			return false;
		if (x1 > -z1 && x2 > -z2)
			return false;
		if (-x1 > -z1 && -x2 > -z2)
			return false;
		int dx = x2-x1;
		int dz = z2-z1;
		FloatPair fp = new FloatPair(0, 1);
		if (!clipt(-dx-dz, x1+z1, fp))
			return false;
		if (!clipt( dx-dz,-x1+z1, fp))
			return false;
		if (!clipt(-dz, z1-4, fp))
			return false;
		// update internals of parameter rp
		if (fp.p2 < 1) {
			rp.x2 = (int) (x1 + fp.p2*dx);
			rp.z2 = (int) (z1 + fp.p2*dz);
		}
		if (fp.p1 > 0) {
			rp.x1 += fp.p1*dx;
			rp.z1 += fp.p1*dz;
		}
		return true;
	}
	



	/**
	 * Recursive methode to explore bsp tree
	 * @param nn
	 */
	private void traverse_node(BSPNode nn) {
		traverse_node_ct++; // debug
		
		// Anchor, stop recursion at leaf nodes
		if (nn.isleaf) {
			traverse_ssector((BSPLeaf) nn);
			return;
		}
		
		// for intermediate nodes proceed recursively through all visible branches
		BSPBranch n = (BSPBranch) nn;
		
		// debug code
		if (deepdebug) {
			dbg("                               ".substring(0, nesting) +
					"traverse_node "+n.x+" "+n.y+" "+n.dx+" "+n.dy+" "+
					n.xl+" "+n.yl+" "+n.xu+" "+n.yu);
		}
		nesting++; // debug
		
		int dot = (viewx-n.x)*n.dy-(viewy-n.y)*n.dx;
		BSPNode lch = n.lbranch;
		BSPNode rch = n.rbranch;
		// if dot >= 0 consider right node before left node
		if (dot >= 0) {
			if (bbox_visible(rch.yu, rch.yl, rch.xl, rch.xu))
				traverse_node(rch);
		}
		// consider left node
		if (bbox_visible(lch.yu, lch.yl, lch.xl, lch.xu))
			traverse_node(lch);
		// if dot < 0 consider right node now (after left node)
		if (dot < 0) {
			if (bbox_visible(rch.yu, rch.yl, rch.xl, rch.xu))
				traverse_node(rch);
		}
		
		nesting--; // debug
	}

	/**
	 * Decide if the bounding box is visible
	 * @param ymax
	 * @param ymin
	 * @param xmin
	 * @param xmax
	 * @return
	 */
	private boolean bbox_visible(int ymax, int ymin, int xmin, int xmax) {
		int rp1x, rp1z;
		int rp2x, rp2z;
		int p1x, p1y, p2x, p2y, x1, x2;

		if (all_visible) // unused feature, presumably for debugging
			return true;
		// check a few simple cases up front
		if (rset.isEmpty())
			return false;
		if (ang >= 45 && ang <= 135 && viewy > ymax)
			return false;
		if (ang >= 225 && ang <= 315 && viewy < ymin)
			return false;
		if (ang >= 135 && ang <= 225 && viewx < xmin)
			return false;
		if ((ang >= 315 || ang <= 45) && viewx > xmax)
			return false;
		
		xmin -= viewx;
		ymin -= viewy;
		xmax -= viewx;
		ymax -= viewy;
		p1x = xmin; p2x = xmax;
		p1y = ymin; p2y = ymax;
		if (ymin < 0 && ymax > 0) {
			p1y = ymin; p2y = ymax;
			if (xmin < 0) {
				if (xmax > 0)
					return true;
				p1x = p2x = xmax;
			} else
				p1x = p2x = xmin;
		} else if (xmin < 0 && xmax > 0) {
			if (ymin < 0)
				p1y = p2y = ymax;
			else
				p1y = p2y = ymin;
		} else if ((xmin > 0 && ymin > 0) || (xmin < 0 && ymin < 0)) {
			p1x = xmax; p2x = xmin;
		}
		rp1x = -viewd_unscale(view_dy*p1x-view_dx*p1y);
		rp1z = -viewd_unscale(view_dx*p1x+view_dy*p1y);
		rp2x = -viewd_unscale(view_dy*p2x-view_dx*p2y);
		rp2z = -viewd_unscale(view_dx*p2x+view_dy*p2y);
		RangePair rp = new RangePair(rp1x, rp1z, rp2x, rp2z);
		if (!clip3d(rp))
			return false;
		x1 = rp.x1*zscale/rp.z1+(view_width/2);
		x2 = rp.x2*zscale/rp.z2+(view_width/2);
		if (x1 > x2) {
			int xj = x1;
			x1 = x2;
			x2 = xj;
		}
		Point p = new Point(x1, x2);
		return (rset.intersect(p));
	}
	/**
	 * Traverses all segments of this leaf and draws corresponding rectangles on screen
	 * @param n
	 */
	private void traverse_ssector(BSPLeaf n) {
		Vector<Seg> sl = n.slist;
		// debug
		traverse_ssector_ct++;
		if (deepdebug) {
			dbg("                               ".substring(0, nesting) +
					"traverse_ssector "+n.xl+" "+n.yl+" "+n.xu+" "+n.yu);
		}
		// for all segments of this node
	
		
		for (int i = 0; i != sl.size(); i++) {
			Seg seg = (Seg) sl.elementAt(i);
			int v1x = seg.x;
			int v1y = seg.y;
			int v2x = v1x+seg.dx;
			int v2y = v1y+seg.dy;
			// draw rectangle
			drawrect(seg, v1x, v1y, v2x, v2y);
			// debug
			if (deepdebug) {
				dbg("                               ".substring(0, nesting) +
						" traverse_ssector(" + i +") "+v1x+" "+v1y+" "+
						seg.dx+" "+seg.dy);
			}

		}
	}
	/**
	 * Draws segment on screen via graphics attribute gc
	 * Helper method for traverse_ssector
	 * @param seg whose seen attribute may be set to true
	 * @param ox1
	 * @param y1
	 * @param ox2
	 * @param y2
	 */
	private void drawrect(Seg seg, int ox1, int y1, int ox2, int y2) {
		//int y11, y12, y21, y22;
		int z1 = 0;
		int z2 = 100;

		drawrect_ct++; // debug, counter
		ox1 -= viewx; y1 -= viewy; z1 -= viewz;
		ox2 -= viewx; y2 -= viewy; z2 -= viewz;

		int y11, y12, y21, y22;
		y11 = y21 = -z1;
		y12 = y22 = -z2;

		int x1 ;
		int x2 ;
		x1 = -viewd_unscale(view_dy*ox1-view_dx*y1);
		z1 = -viewd_unscale(view_dx*ox1+view_dy*y1);
		x2 = -viewd_unscale(view_dy*ox2-view_dx*y2);
		z2 = -viewd_unscale(view_dx*ox2+view_dy*y2);

		RangePair rp = new RangePair(x1, z1, x2, z2);
		if (!clip3d(rp))
			return;

		y11 = y11*zscale/rp.z1+(view_height/2); // constant from here
		y12 = y12*zscale/rp.z1+(view_height/2); // constant from here
		y21 = y21*zscale/rp.z2+(view_height/2); // constant from here
		y22 = y22*zscale/rp.z2+(view_height/2); // constant from here
		x1 = rp.x1*zscale/rp.z1+(view_width/2); // constant from here
		x2 = rp.x2*zscale/rp.z2+(view_width/2); // constant from here
		if (x1 >= x2) /* reject backfaces */
			return;
		int x1i = x1;
		int xd = x2-x1;
		Globals.gw.setColor(seg.col);
		boolean drawn = false;
		drawrect_late_ct++; // debug, counter
		// loop variable is x1i, upper limit x2 is fixed
		while (x1i <= x2) {
			// check if there is an intersection, 
			// if there is none proceed exit the loop, 
			// if there is one, get it as (x1i,x2i)
			Point p = new Point(x1i, x2);
			if (!rset.intersect(p))
				break;
			x1i = p.x;
			int x2i = p.y;
			// let's work on the intersection (x1i,x2i)
			int xps[] = { x1i, x1i, x2i+1, x2i+1 };
			int yps[] = { y11+(x1i-x1)*(y21-y11)/xd,
					y12+(x1i-x1)*(y22-y12)/xd+1,
					y22+(x2i-x2)*(y22-y12)/xd+1,
					y21+(x2i-x2)*(y21-y11)/xd };
			Globals.gw.fillPolygon(xps, yps, 4);
			drawn = true;
			rset.remove(x1i, x2i);
			x1i = x2i+1;
			drawrect_segment_ct++; // debug, counter
		}
		if (drawn && !seg.seen) {
			udpateSeenCellsForSegment(seg);
		}
	}
	/**
	 * Sets the seencells bit for all cells of a segment
	 * @param seg
	 */
	private void udpateSeenCellsForSegment(Seg seg) {
		seg.seen = true; // updates the segment

		int sdx = seg.dx / map_unit; // constant, only set once here
		int sdy = seg.dy / map_unit; // constant, only set once here

		// get initial position right on loop variables sx, sy
		int sx = seg.x / map_unit;
		if (sdx < 0)
			sx--;
		int sy = seg.y / map_unit; 
		if (sdy < 0)
			sy--;
		
		// define constants to avoid method calls in following loop
		int sdsx = MazeBuilder.getSign(sdx); 
		int sdsy = MazeBuilder.getSign(sdy); 
		int bit = (sdx != 0) ? Cells.CW_TOP : Cells.CW_LEFT;  
		int len = Math.abs(sdx + sdy);  
		// true loop variables are (sx,sy), 		
		for (int i = 0; i != len; i++) {
			//seencells[sx][sy] |= bit;
			seencells.setBitToOne(sx, sy, bit) ; // updates the cell
			sx += sdsx;
			sy += sdsy;
		}
	}
}
