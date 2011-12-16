package com.android.ui;

import java.util.Random;

/**
 * This class encapsulates all access to a grid of cells. Each cell encodes whether walls or borders/bounds to rooms 
 * or to the outer border of the maze exist.
 * The class resulted from refactoring the int[][] cells area in the original Maze and Mazebuilder classes into a class of its own.
 * The internal two-dimensional array matches with a grid of cells as follows:
 * cells[0,y] form the left border, hence there is a wall on  left.
 * cells[width-1,y] form the right border, hence there is a wall on right.
 * cells[x,0] form the top border, hence there is a wall on top.
 * cells[x,height-1] form the bottom border, hence there is a wall on bottom.
 * The upper left corner is seen as position [0][0].
 * 
 * Note that for a calculated maze, at least one cell on the border will have a missing wall for an exit somewhere.
 * 
 * Walls and borders are separated concepts. A border is not removed by the maze generation procedure. It is used to mark 
 * the outside border of the maze but also internal rooms. Walls can be taken down by the maze generation procedure.
 * 
 * The internal encoding of walls for each cell into a single integer per cell is performed with bit operations (&,|) and 
 * thus error prone. An encapsulation within this class localizes all bit operations for this encoding.
 * 
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class Cells {
	// Integer constants to encode 4 possible walls for a single cell (CW = cell wall) on top, bottom, left, right in a single byte of an integer
	static final int CW_TOP = 1;  // 2^0
	static final int CW_BOT = 2;  // 2^1
	static final int CW_LEFT = 4; // 2^2
	static final int CW_RIGHT = 8;// 2^3
	static final int CW_VIRGIN = 16;	 // 2^4
	static final int CW_ALL = CW_TOP|CW_BOT|CW_LEFT|CW_RIGHT; // constant to simplify check if all walls are present
	// Integer constants to encode 4 possible sides that touch a border (or bound)
	// a separate encoding of borders allows for flexible layouts (not just rectangles)
	// Note: encoding matches the wall encoding with respect to directions such that same encoding applies plus a shift
	static final int CW_BOUND_SHIFT = 5; // used to shift encoding from dirsx, dirsy below from wall range to bound range 
	static final int CW_TOP_BOUND = 32; // 2^5
	static final int CW_BOT_BOUND = 64; // 2^5
	static final int CW_LEFT_BOUND = 128; // 2^7
	static final int CW_RIGHT_BOUND = 256; // 2^8
	static final int CW_ALL_BOUNDS =
		CW_TOP_BOUND|CW_BOT_BOUND|CW_LEFT_BOUND|CW_RIGHT_BOUND; // constant to simplify check if all all bounds are present
	static final int CW_IN_ROOM = 512; // 2^9

	// we put all encodings into a single array such that it is easier to iterate over the array
	// note that the numerical values are used for bitwise calculations so a refactoring with other values in an enumeration can break the code
	static final int masks[] = { CW_RIGHT, CW_BOT, CW_LEFT,  CW_TOP };
	
	private int width;
	private int height ;
	protected int[][] cells;
	/**
	 * Constructor
	 */
	public Cells(int w, int h) {
		width = w ;
		height = h ;
		cells = new int[w][h];
	}
	
	int getWidth(){
		return width;
	}
	
	int getHeight(){
		return height;
	}

	/**
	 * Gets hold of an internal array of length 4 with bitmasks for all directions: Right, Bottom, Left, Top
	 * @return array of bitmasks
	 */
	static public int[] getMasks() {
		return masks ;
	}
	/**
	 * checks if adjacent cells (x,y) and its neighbor (x+dx,y+dy) are not separated by a border 
	 * and (x+dx,y+dy) has not been visited before.
	 * This method is used in the MazeBuilder class.
	 * @precondition borders limit the outside of the maze area
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @return
	 */
	public boolean canGo(int x, int y, int dx, int dy) {
		// borders limit rooms (but for doors) and the outside limit of the maze
		if (hasMaskedBitsTrue(x, y, (getBit(dx, dy) << CW_BOUND_SHIFT)))
			return false;
		// if there is no border, neighbor should be in legal range of values
		return isVirgin(x+dx, y+dy);
	}

	/**
	 * encodes (dx,dy) into a bit pattern for right, left, top, bottom direction
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 * @return bit pattern, 0 in case of an error
	 */
	private int getBit(int dx, int dy) {
		int bit = 0;
		switch (dx + dy * 2) {
		case 1:  bit = CW_RIGHT; break; //  dx=1,  dy=0
		case -1: bit = CW_LEFT;  break; //  dx=-1, dy=0
		case 2:  bit = CW_BOT;   break; //  dx=0,  dy=1
		case -2: bit = CW_TOP;   break; //  dx=0,  dy=-1
		default:  break;
		}
		return bit;
	}
	/////////////////////// set methods to set specific bits to zero ///////////////////////
	/**
	 * sets given bit in to zero in given cell
	 * @param x
	 * @param y
	 * @param cw_bit like CW_LEFT, CW_RIGHT, CW_TOP, CW_BOTTOM
	 */
	public void setBitToZero(int x, int y, int cw_bit) {
		cells[x][y] &= ~cw_bit;
	}
	/**
	 * Sets all wall bits to zero for a given cell and direction
	 * @param x
	 * @param y
	 * @param dx
	 * @param dy
	 */
	public void setAllToZero(int x, int y) {
		//cells[x][y] &= ~MazeBuilder.CW_ALL;
		setBitToZero(x, y, CW_ALL) ;
	}

	/**
	 * Sets the virgin flag to zero for a given cell
	 * @param x
	 * @param y
	 */
	public void setVirginToZero(int x, int y) {
		//cells[x][y] &= ~MazeBuilder.CW_VIRGIN;
		setBitToZero(x,y,CW_VIRGIN) ; 
	}
	
	/**
	 * Sets the wall bit to zero for a given cell and direction
	 * @param x
	 * @param y
	 * @param dx
	 * @param dy
	 */
	public void setWallToZero(int x, int y, int dx, int dy) {
		setBitToZero(x, y, getBit(dx, dy));
	}
	/**
	 * Sets the bound bit to zero for a given cell and direction
	 * @param x
	 * @param y
	 * @param dx
	 * @param dy
	 */
	public void setBoundToZero(int x, int y, int dx, int dy) {
		int bit = getBit(dx, dy);
		//cells[x][y] &= ~(bit << MazeBuilder.CW_BOUND_SHIFT);
		setBitToZero(x,y,(bit << CW_BOUND_SHIFT)) ; 
	}
	
	////////////////// set method to set additional bits to one //////////////////////
	public void setBitToOne(int x, int y, int bitmask) {
		cells[x][y] |= bitmask ;
	}
	/**
	 * Sets the bound and wall bit to one for a given cell and direction
	 * @param x
	 * @param y
	 * @param dx
	 * @param dy
	 */
	public void setBoundAndWallToOne(int x, int y, int dx, int dy) {
		int bit = getBit(dx, dy);
		//cells[x][y] |= bit | (bit<<MazeBuilder.CW_BOUND_SHIFT);
		setBitToOne(x, y, (bit | (bit<< CW_BOUND_SHIFT)));
	}
	/**
	 * Sets the InRoom bit to one for a given cell and direction
	 * @param x
	 * @param y
	 * @param dx
	 * @param dy
	 */
	public void setInRoomToOne(int x, int y) {
		setBitToOne(x, y, CW_IN_ROOM);
	}


	public void setTopToOne(int x, int y) {
		//cells[x][y] |= MazeBuilder.CW_TOP;
		setBitToOne(x, y, CW_TOP);
	}

	/**
	 * Initialize maze such that all cells have not been visited (CW_VIRGIN), all walls are up (CW_ALL),
	 * and borders are set as a rectangle (CW_*_BOUND).
	 */
	public void initialize() {
		int x, y;
	
		for (x = 0; x != width; x++) {
			for (y = 0; y != height; y++) {
				//cells[x][y] = MazeBuilder.CW_VIRGIN | MazeBuilder.CW_ALL;
				setBitToOne(x, y, (CW_VIRGIN | CW_ALL));
			}
			//cells[x][0] |= MazeBuilder.CW_TOP_BOUND;
			setBitToOne(x, 0, CW_TOP_BOUND);
			//cells[x][height-1] |= MazeBuilder.CW_BOT_BOUND;
			setBitToOne(x, height-1, CW_BOT_BOUND);
		} 
		for (y = 0; y != height; y++) {
			//cells[0][y] |= MazeBuilder.CW_LEFT_BOUND;
			setBitToOne(0, y, CW_LEFT_BOUND);
			//cells[width-1][y] |= MazeBuilder.CW_RIGHT_BOUND;
			setBitToOne(width-1, y, CW_RIGHT_BOUND);
		}
	}

	/**
	 * Checks if there is a cell in the given area that belongs to a room.
	 * The first corner is at the upper left position, the second corner is at the lower right position.
	 * @param rx 1st corner, x coordinate
	 * @param ry 1st corner, y coordinate
	 * @param rxl 2nd corner, x coordinate
	 * @param ryl 2nd corner, y coordinate
	 */
	public boolean areaOverlapsWithRoom(int rx, int ry, int rxl, int ryl) {
		int x, y;
		// loop start and end are chosen such that there is at least one cell between area and any existing room
		for (x = rx-1; x <= rxl+1; x++)
		{
			for (y = ry-1; y <= ryl+1; y++)
			{
				if (isInRoom(x, y))
					return true ;
			}
		}
		return false ;
	}
	/**
	 * Delete a border/bound between to adjacent cells (x,y) and (x+dx,y+dy).
	 * Only used in markAreaAsRoom.
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	private void deleteBound(int x, int y, int dx, int dy) {
		// same code as for delwall but we need to shift the bit for the direction to the byte that encodes the bounds
		setBoundToZero(x, y, dx, dy);
		setBoundToZero(x+dx, y+dy, -dx, -dy) ;
	}

	/**
	 * Add a wall and a border/bound between to adjacent cells (x,y) and (x+dx,y+dy).
	 * Only used in markAreaAsRoom.
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	private void addBoundWall(int x, int y, int dx, int dy) {
		setBoundAndWallToOne(x, y, dx, dy);
		setBoundAndWallToOne(x+dx, y+dy, -dx, -dy);
	}
	/**
	 * Delete a wall between to adjacent cells (x,y) and (x+dx,y+dy).
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @param dx direction x, in { -1, 0, 1} obtained from dirsx[]
	 * @param dy direction y, in { -1, 0, 1} obtained from dirsy[]
	 */
	public void deleteWall(int x, int y, int dx, int dy) {
		// delete wall on (x,y)
		setWallToZero(x, y, dx, dy);
		// delete same wall but for adjacent cell
		setWallToZero(x+dx, y+dy, -dx, -dy);
	}
	/**
	 * Generate an integer random number in interval [lo,hi] 
	 * @param lo
	 * @param hi
	 * @return random number within given range
	 */
	private int randNo(Random random, int lo, int hi) {
		//TODO: work on findbugs error message for negative min value where abs will not change sign!
		return (Math.abs(random.nextInt()) % (hi-lo+1)) + lo;
	}
	/**
	 * Marks a given area as a room on the maze and positions up to five doors randomly.
	 * The first corner is at the upper left position, the second corner is at the lower right position.
	 * Assumes that given area is located on the map and does not intersect with any existing room.
	 * The walls of a room are declared as borders to prevent the generation mechanism from tearing them down.
	 * Of course there must be a few segments where doors can be created so the border protection is removed there.
	 * @param rw room width
	 * @param rh room height
	 * @param rx 1st corner, x coordinate
	 * @param ry 1st corner, y coordinate
	 * @param rxl 2nd corner, x coordinate
	 * @param ryl 2nd corner, y coordinate
	 * @param r random number stream to obtain values for door positions
	 */
	public void markAreaAsRoom(int rw, int rh, int rx, int ry, int rxl, int ryl, Random r) {
		// clear all cells in area of room from all walls and borders
		// mark all cells in area as being inside the room
		int x;
		int y;
		for (x = rx; x <= rxl; x++)
			for (y = ry; y <= ryl; y++) { 
				setAllToZero(x, y);
				setInRoomToOne(x, y);
			} 
		// add a bound and a wall all around the area for this room
		for (x = rx; x <= rxl; x++) {
			addBoundWall(x, ry, 0, -1);
			addBoundWall(x, ryl, 0, 1);
		} 
		for (y = ry; y <= ryl; y++) {
			addBoundWall(rx, y, -1, 0);
			addBoundWall(rxl, y, 1, 0);
		}
		// knock down some walls for doors
		int wallct = (rw+rh)*2; // counter for the total number of walls
		// check at most 5 walls
		for (int ct = 0; ct != 5; ct++) { 
			int door = randNo(r, 0, wallct-1); // pick a random wall
			// calculate position and direction of this wall
			int dx, dy;
			if (door < rw*2) {
				y = (door < rw) ? 0 : rh-1;
				dy = (door < rw) ? -1 : 1;
				x = door % rw;
				dx = 0;
			} else {
				door -= rw*2;
				x = (door < rh) ? 0 : rw-1;
				dx = (door < rh) ? -1 : 1;
				y = door % rh;
				dy = 0;
			} 
			// tear down the border protection.
			// It remains a wall that the generation mechanism can then tear down.
			deleteBound(x+rx, y+ry, dx, dy);
		}
	}
	//////////////////// get methods (is..., has...) for various flags ///////////////////////
	public boolean hasMaskedBitsTrue(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) != 0;
	}
	/**
	 * tells if InRoom flag is set for given cell
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isInRoom(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_IN_ROOM) != 0;
		return hasMaskedBitsTrue(x, y, CW_IN_ROOM);
	}
	private boolean isVirgin(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_VIRGIN) != 0;
		return hasMaskedBitsTrue(x, y, CW_VIRGIN);
	}
	public boolean hasWallOnRight(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_RIGHT) != 0;
		return hasMaskedBitsTrue(x, y, CW_RIGHT);
	}
	public boolean hasWallOnLeft(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_LEFT) != 0;
		return hasMaskedBitsTrue(x, y, CW_LEFT);
	}
	public boolean hasWallOnTop(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_TOP) != 0;
		return hasMaskedBitsTrue(x, y, CW_TOP);
	}
	public boolean hasWallOnBottom(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_BOT) != 0;
		return hasMaskedBitsTrue(x, y, CW_BOT);
	}
	
	////// convenience methods for readability, negated existence test for a specific wall
	public boolean hasNoWallOnBottom(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_BOT) == 0;
		return !hasMaskedBitsTrue(x, y, CW_BOT);
	}
	public boolean hasNoWallOnTop(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_TOP) == 0;
		return !hasMaskedBitsTrue(x, y, CW_TOP);
	}
	public boolean hasNoWallOnLeft(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_LEFT) == 0;
		return !hasMaskedBitsTrue(x, y, CW_LEFT);
	}
	public boolean hasNoWallOnRight(int x, int y) {
		//return (cells[x][y] & MazeBuilder.CW_RIGHT) == 0;
		return !hasMaskedBitsTrue(x, y, CW_RIGHT);
	}
	

	public boolean hasMaskedBitsFalse(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) == 0;
	}
	// unclear at this point why greater is checked instead of inequality to zero in code
	// method resulted from refactoring
	public boolean hasMaskedBitsGTZero(int x, int y, int bitmask) {
		return (cells[x][y] & bitmask) > 0;
	}

	
	
	/**
	 * Methods dumps internal data into a string, intended usage is for debugging purposes. 
	 * Maze is represent as a matrix of integer values.
	 */
	public String toString() {
		String s = "" ;
		for (int i = 0 ; i < width ; i++)
		{
			for (int j = 0 ; j < height ; j++)
				s += " i:" + i + " j:" + j + "=" + cells[i][j] ;
			s += "\n" ;
		}
		return s ;
	}
}