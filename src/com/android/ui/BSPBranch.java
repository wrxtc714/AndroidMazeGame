/**
 * 
 */
package com.android.ui;

/**
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
public class BSPBranch extends BSPNode {
    BSPNode lbranch, rbranch;
    int x, y, dx, dy;
    
    /**
     * Constructor
     * @param px
     * @param py
     * @param pdx
     * @param pdy
     * @param l
     * @param r
     */
    BSPBranch(int px, int py, int pdx, int pdy, BSPNode l, BSPNode r) {
	lbranch = l;
	rbranch = r;
	isleaf = false;
	x = px; y = py; dx = pdx; dy = pdy;
	xl = Math.min(l.xl, r.xl);
	xu = Math.max(l.xu, r.xu);
	yl = Math.min(l.yl, r.yl);
	yu = Math.max(l.yu, r.yu);
    }
}
