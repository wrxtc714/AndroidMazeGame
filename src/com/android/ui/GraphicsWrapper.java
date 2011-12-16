package com.android.ui;

import android.content.Context;
import android.graphics.*;

import android.graphics.drawable.*;
import android.graphics.drawable.shapes.*;
import android.util.Log;
import android.view.View;

import java.awt.Graphics;


/**
 * This is the Graphics wrapper class that handles all 
 * maze graphics drawing for the application.  The bulk
 * of the work is done by the fillpolygon method, which
 * is passed arrays of points from which to draw the walls
 * of the maze to a canvas.
 * 
 * @author adam
 *
 */
public class GraphicsWrapper {
	
	int color;
	Rect rectangle;
	Paint paint = new Paint();
	Point point;	
	Maze maze;
	Path path = new Path();
	int walkStep;
	Canvas canvas;

	
	public void setCanvas(Canvas canvas){
		this.canvas = canvas;
	}
	
	public Canvas getCanvas(){
		return canvas;
	}

	
	public int makeColor(int r, int g, int b) {
		color =  Color.rgb(r,g,b);
		return color;
	}
	

	public void setColor(int color) {

		paint.setColor(color);
	}
	
	public void fillRect(int left, int right, int top, int bottom){
		rectangle = new Rect(left, right, top, bottom);
		Log.v("fill", "rect");
		canvas.drawRect(rectangle, paint);
		
	}
	

	
	public void fillPolygon(int[] a, int b[], int c){
		path = new Path();
		int x, y;
		for (int n = 0; n < c ; n++){
			x = a[n];
			y = b[n];
			if(n == 0)		
				path.moveTo(x,y);
			else
				path.lineTo(x,y);
		}
		canvas.drawPath(path, paint);
	
	}
	public void setPoint(Point point){
		this.point = point;
	}
	
	public Point getPoint(){
		return point;
	}
	public Point makePoint(int x, int y){
		point = new Point(x,y);	
		return point;
	}
	public Maze getMaze() {
		return maze;
	}

	public void setMaze(Maze maze) {
		this.maze = maze;
	}

	public int getColor() {
		return color;
	}

	public void drawLine(int nx1, int ny1, int nx2, int ny12) {
		canvas.drawLine(nx1, ny1, nx2, ny12, paint);	
	}

	public void fillOval(int i, int j, int cirsiz, int cirsiz2) {
		//canvas.drawOval(oval, paint);
		
	}

	
	
}


