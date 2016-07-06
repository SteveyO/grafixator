package com.grafixator.managers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.grafixator.model.GrafixatorBackground;

/*
 * BackgroundManager singleton.
 * Renders Grafixator Backgrounds with the appropriate speed and Repeat (Horizontal, Vertical or both).
 */
public class BackgroundManager {
	
	private static BackgroundManager instance = null;
	
	public List<GrafixatorBackground> backgroundList = new ArrayList<GrafixatorBackground>();

	public Rectangle rect = new Rectangle();  // For collision Detection with the clicked background
	
	float startX;
	float startY;
	float camStartPosX;
	float camStartPosY;
	
	int noXBackgrounds;
	int noYBackgrounds;
	
	public static BackgroundManager getInstance() {
		if (instance == null) {
			instance = new BackgroundManager();
		}
		return instance;
	}
	
	public void createNewBackground(GrafixatorBackground background) {
		backgroundList.add(background);		
	}
	
	
	public void render(Batch batch, OrthographicCamera tileMapCamera) {

		for (GrafixatorBackground bg: backgroundList) {			
				 startX=0;
				 startY=0;
				 camStartPosX = tileMapCamera.position.x - Gdx.graphics.getWidth()/2;
				 camStartPosY = tileMapCamera.position.y - Gdx.graphics.getHeight()/2;

				 if (bg.xPos > camStartPosX) {
					 startX = camStartPosX - (( camStartPosX - bg.xPos) % bg.width) - bg.width;					
				 }
				 else {
					 startX = camStartPosX - (( camStartPosX - bg.xPos) % bg.width);
				 }

				 if (bg.yPos > camStartPosY) {
					 startY = camStartPosY + Gdx.graphics.getHeight() - ( camStartPosY - bg.yPos) % bg.height - bg.height;
				 }
				 else {
					 startY = camStartPosY + Gdx.graphics.getHeight() - ( camStartPosY - bg.yPos) % bg.height;
				 }
			 
				 
				 // Repeat BG both X and Y.
			     if (bg.repeatX && bg.repeatY) {   
			    	 noXBackgrounds = Gdx.graphics.getWidth() / bg.width + 2;
			    	 noYBackgrounds = Gdx.graphics.getHeight() / bg.height + 2;
			    	 for (int b=0; b<noXBackgrounds; b++) {
			    		 for (int b2=0; b2<noYBackgrounds; b2++) {
			    			 batch.draw(bg.backgroundTexture, (int) startX + (b*bg.width),  startY - (b2*bg.height)+b2);	
			    		 }
			    	 }
			     }
			     else if (bg.repeatX) {   
					 noXBackgrounds = (Gdx.graphics.getWidth() / bg.width) + 2;
					 for (int b=0; b<noXBackgrounds; b++) {
				         batch.draw(bg.backgroundTexture, startX + (b*bg.width), bg.yPos);	
					 }
				 }
			     
			     else if (bg.repeatY) {   
			    	 noYBackgrounds = Gdx.graphics.getHeight() / bg.height + 2;
	
			    	 for (int b=0; b<noYBackgrounds; b++) {
			    		 batch.draw(bg.backgroundTexture, bg.xPos, startY - (b*bg.height));	
			    	 }
			     }
				 else {
					    batch.draw(bg.backgroundTexture, bg.xPos, bg.yPos);
				 }
		}
	}
	
	public void updateBackgrounds(float xAmount, float yAmount, boolean moveLeft, boolean moveUp) {

	    for (GrafixatorBackground background: backgroundList) {	
	    	if (xAmount !=0) {
	    		if (moveLeft) {
	    			background.xPos += background.speedX;
	    		}
	    		else {
	    			background.xPos -= background.speedX;
	    		}
	    	}
	    	if (yAmount !=0) {
	    		if (moveUp) {
	    			background.yPos += background.speedY;
	    		}
	    		else {
	    			background.yPos -= background.speedY;
	    		}
	    	}
	    }
	}
	
}
