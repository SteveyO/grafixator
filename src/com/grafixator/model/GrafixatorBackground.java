package com.grafixator.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * 
 * @author SteveyO
 * 
 * A Grafixator Background.
 * 
 * Grafixator supports up to 3 Backgrounds which are rendered by the Background Manager.
 * repeatX and repeatY should be self explanatory, they just repeat the backgrounds to take up the whole of the screen width or height.
 * speedX and speedY allows backgrounds to rendered at different speed,  useful for parallax background.
 *
 */
public class GrafixatorBackground {
   public String fileName;
   public String shortFileName;
   
   public TextureRegion backgroundTexture;
   
   public float xPos;
   public float yPos;
   
   public boolean repeatX;
   public boolean repeatY;

   public float speedX;
   public float speedY;
   
   public float originalXPos;   // To reset the background after playing.
   public float originalYPos;
   
   public int width;
   public int height;
   
   public int xOffset;
   public int yOffset;
   
   
}

