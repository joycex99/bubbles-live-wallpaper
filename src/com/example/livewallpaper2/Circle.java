package com.example.livewallpaper2;

import java.util.ArrayList;

public class Circle {
	private float x, y, r, velocity;
	private int color, alpha;
	private boolean background;
	private boolean beenReplaced;
	private boolean toBePopped;
	public ArrayList<float[]> sparks; //[cx, cy, r, vy]
	private boolean hasSparks;
	private int popCount;
	
	public Circle(float x, float y, float r, int c, int a, float v, boolean b) {
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
		this.setBeenReplaced(false);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
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

	public float getVelocity() {
		return velocity;
	}

	public void setVelocity(float velocity) {
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

	public boolean hasBeenReplaced() {
		return beenReplaced;
	}

	public void setBeenReplaced(boolean beenReplaced) {
		this.beenReplaced = beenReplaced;
	}

}
