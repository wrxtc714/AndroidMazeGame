package com.android.ui;

/**
 * This class generates a maze using Prim's algorithm.
 * @author adam
 *
 */
public class MazeBuilderPrim extends MazeBuilder {
	int[][] cellStatus;

	
	protected void generate() {
		// Generate the maze using Prim's algorithm
		int x = randNo(0, width -1); //starting x value
		int y = 0;
		cellStatus = new int[width][height];
		long [] frontier = new long[width*height];  //sufficient array size
		int frontierSize = 0;
		int nextSquare = 0;//for choosing the next of the frontier squares
		int nextX = 0;
		int nextY = 0;
		cells.setVirginToZero(x, y);
		while(true){
			cellStatus[x][y] = 2;
			for(int ii = 0; ii < 4; ii++){
				int dx = dirsx[ii];
				int dy = dirsy[ii];
				if(cells.canGo(x, y, dx, dy)){
					if(cellStatus[x+dx][y+dy] != 1){
						cellStatus[x+dx][y+dy] = 1; //1 means frontier
						frontier[frontierSize] = (((x+dx) * 100000) + (y+dy)); //encoding scheme to store 
																			//x and y values in single array.
																			//e.g.  (4,5) = 400000 + 5 = 40005
						frontierSize++;
					}
				}//if
			}//for
			
	
			if(frontierSize == 0){
				break; //end of loop. Maze complete
			}
			nextSquare = randNo(0,frontierSize -1);
			nextY = (int) (frontier[nextSquare] % 100000);
			nextX = (int) ((frontier[nextSquare] - nextY) / 100000);
			
			for(int ii = nextSquare; ii < frontierSize -1; ii++){ //delete from frontier array
				frontier[ii] = frontier[ii+1];
			}
			frontier[frontierSize -1] = 0;
			
			frontierSize--;
			int neighborDirection = findFinishedNeighbor(nextX, nextY);
			
			int directionx = dirsx[neighborDirection];
			int directiony = dirsy[neighborDirection];
			cells.deleteWall(nextX, nextY, directionx, directiony);
			cells.setVirginToZero(nextX, nextY);
			
			
			
			x = nextX;
			y = nextY;
		}//while
		
	}
	
	

	protected int findFinishedNeighbor(int x, int y){
		int[] neighbors = new int[4];  // array holding possible neighbors already in the maze
		int neighborCount = -1;  
		for (int ii = 0; ii < 4; ii++){
			int dx = dirsx[ii];
			int dy = dirsy[ii];
			if (x+dx >-1 && (x+dx) < width && (y+ dy) >-1 && (y+dy) < height){ //check if it's still inside
																			   //the maze walls.
				if(cellStatus[x+dx][y+dy] == 2){  //this neighbor is already in the maze
					neighborCount++;
					neighbors[neighborCount] = ii;
					
				}
			}
		}//for
		int randomNeighbor = randNo(0, neighborCount);
		if (neighborCount == -1){
			System.out.println("Can't find a valid neighbor square");
			return 0;
		}
		return neighbors[randomNeighbor];  //this returns a random neighbor of the valid
												// neighbors already in the maze.
	}

}
