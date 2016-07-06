package com.grafixator.model;

import java.util.ArrayList;
import java.util.List;

public class GrafixatorTimeLine {

    public int spriteId;   // If selected image is not a tile,   if it is a tile, then we will have a tileX and tile value.  
    public int spriteIndex;  
    
    public int xPos;
    public int yPos;
    
    public int height;   // The Height and width of the sprites in the timeline.   All timelines go through the centre point of the sprite.
    public int width;
    
    public boolean rotateSprite;
    public boolean loop;
    public float timeLineSpeed;
    public int easingFunction;
    
    public List<GrafixatorTimeLineEvent>  timeLineEvents = new ArrayList<GrafixatorTimeLineEvent>();            
}
