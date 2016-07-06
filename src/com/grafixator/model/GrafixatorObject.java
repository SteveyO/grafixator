package com.grafixator.model;


import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

/*
 *  A Grafixator Collision object (rectangle, circle, triangle or list of vertices)
 *  
 *  Contains all the collision objects that were added
 */
public class GrafixatorObject {

	public int shapeType;   // See GrafixatorConstants  SHAPE_TYPE_*  for the different shape types.
	
	public float origX;
	public float origY;
	
	public List <Vector2> vertices = new ArrayList<Vector2>();
	
	public float width;
	public float height;
	
	public float radius;  // In case shapeType is a circle.
	
}

