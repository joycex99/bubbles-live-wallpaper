package com.example.livewallpaper2;

import java.util.ArrayList;
import java.util.Random;

public class Circle {
	private int x, y, r, color, alpha, velocity;
	private boolean background;
	private boolean toBePopped;
	public ArrayList<int[]> sparks; //[cx, cy, r, vy]
	private boolean hasSparks;
	private int popCount;
	
	private Random rand = new Random();
	
	public Circle(int x, int y, int r, int c, int a, int v, boolean b) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.color = c;
		this.alpha = a;
		this.setVelocity(v);
		this.setBackground(b);
		this.setToBePopped(false);
		this.setHasSparks(false);
		this.sparks = new ArrayList<>();
		this.popCount = 255;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getVelocity() {
		return velocity;
	}

	public void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	public boolean isBackground() {
		return background;
	}

	public void setBackground(boolean background) {
		this.background = background;
	}

	public boolean isToBePopped() {
		return toBePopped;
	}

	public void setToBePopped(boolean toBePopped) {
		this.toBePopped = toBePopped;
	}
	
//	public ArrayList<int[]> getSparks() {
//		return this.sparks;
//	}
//	
//	public void setSparks(ArrayList<int[]> sparks) {
//		this.sparks = sparks;
//	}

	public boolean hasSparks() {
		return hasSparks;
	}

	public void setHasSparks(boolean hasSparks) {
		this.hasSparks = hasSparks;
	}

	public int getPopCount() {
		return popCount;
	}

	public void setPopCount(int popCount) {
		this.popCount = popCount;
	}

}
