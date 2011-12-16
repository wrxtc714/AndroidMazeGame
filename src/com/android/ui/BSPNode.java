/**
 * 
 */
package com.android.ui;

/**
 * Superclass for BSPBranch and Leaf nodes
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 *  
 */
public class BSPNode {
    public int xl, yl, xu, yu;
    public boolean isleaf;
}