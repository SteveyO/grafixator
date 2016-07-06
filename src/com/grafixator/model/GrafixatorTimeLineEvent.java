package com.grafixator.model;

import com.badlogic.gdx.math.Vector2;

public class GrafixatorTimeLineEvent {

    public enum GrafixatorEventType {
        MOVE_SPRITE,
        FIRE_ENEMY_BULLET
    }
    
    public int eventEasingFunction=-1;  // If not set then use the Global TimeLine Easing.
    public float eventDuration;   		// How long to move to the new position.
    public float eventSpeed=-1;   		// If set (not -1) then the speed of the event.   If not set then the speed is taken from the master TimeLine (GrafixatorTimeLIne object)
    public int   noRepeats=-1;    		// If set the timeline will be repeated.
    public float eventMoveToX;   		// New X Position of the Sprite
    public float eventMoveToY;
    public float eventRotation;
    public int   eventDirection;    	// For Animations to set the direction the sprite should face.
    public float eventTimeLineDelay = -1;  // The deleay between starting each event.s
    
    public Vector2 wayPoint;    // Timeline waypoint.  Can only have 1 per event.

    
}
