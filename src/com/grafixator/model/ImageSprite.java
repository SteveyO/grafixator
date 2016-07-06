package com.grafixator.model;

// A Grafixator Sprite may be repeated in various different positions with different rotations, height etc.  This class represents their positions.

public class ImageSprite {
	public float x;
	public float y;
	public float width;
	public float height;
	public float rotation;

	public ImageSprite() {

	}

	public ImageSprite(float x, float y, float width, float height) {
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	}

	public void setLocation(float xPos, float yPos) {
		this.x = xPos;
		this.y = yPos;
	}

	public void resize(float w, float h) {
		this.width  = w;
		this.height = h;
	}

	public void resizeWidth(float w) {
		this.width  = w;
	}

	public void resizeHeight(float h) {
		this.height = h;
	}
}
