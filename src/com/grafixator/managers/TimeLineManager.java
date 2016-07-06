package com.grafixator.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.grafixator.model.GrafixatorSprite;
import com.grafixator.model.GrafixatorTimeLine;
import com.grafixator.model.GrafixatorTimeLineEvent;
import com.grafixator.tween.GrafixatorSpriteAccessor;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Back;
import aurelienribon.tweenengine.equations.Bounce;
import aurelienribon.tweenengine.equations.Circ;
import aurelienribon.tweenengine.equations.Cubic;
import aurelienribon.tweenengine.equations.Elastic;
import aurelienribon.tweenengine.equations.Expo;
import aurelienribon.tweenengine.equations.Linear;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.tweenengine.equations.Quint;
import aurelienribon.tweenengine.equations.Sine;

public class TimeLineManager {
    private static TimeLineManager instance = null;
    public Map<String, GrafixatorTimeLine> gfxTimeLinesList   = new HashMap<String, GrafixatorTimeLine>();   // Key is either a tile in 1,2 format or just a String if a Asset.
    
    public List <Timeline>  timeLinesList = new ArrayList<Timeline>();
    
    public static TimeLineManager getInstance() {
        if (instance == null) {
            instance = new TimeLineManager();
        }
        return instance;
    }
    
    public void addTimeLine(int spriteId, int spriteIndex, TextureRegion textureRegion, final GrafixatorSprite sprite) {
        for (final GrafixatorTimeLine tl: gfxTimeLinesList.values()) {

            if (tl.spriteId == spriteId && tl.spriteIndex == spriteIndex) {                     
                Timeline timeline = Timeline.createSequence();

                for (final GrafixatorTimeLineEvent event: tl.timeLineEvents) {                	
                	
                    TweenCallback callback = new TweenCallback() {
                        
                        @Override
                        public void onEvent(int type, BaseTween<?> arg1) {
                        	if (type==TweenCallback.START) {

                        		if (tl.rotateSprite) {
                        			sprite.rotation= event.eventRotation;
                        		}

                        		if (sprite.isAnimation) {       
                        			if (event.eventDirection !=0) {   // If moving vertically then keep the last sprite direction.
                        			   sprite.xDir    = event.eventDirection;
                        			}
                        		}
                        	}
                        }
                    };   // End of Callback

                    
                    TweenEquation equation;
                   
                    if (event.eventEasingFunction != -1) equation = getEasingEquation(event.eventEasingFunction);   // The Event Easing has been set (and is different to the Global Settings).
                    else equation = getEasingEquation(tl.easingFunction);
                    
                    Tween tween = Tween.to(sprite, GrafixatorSpriteAccessor.POS_XY, event.eventDuration)  
                    		.ease(equation)   
                    		.setCallback(callback)                            
                    		.setCallbackTriggers(TweenCallback.START)    
                    		.target(event.eventMoveToX, event.eventMoveToY);
                	
                    float repeatDelay=0;

                	if (event.eventTimeLineDelay != -1) {
                		repeatDelay = event.eventTimeLineDelay;
                		tween.delay(event.eventTimeLineDelay);
                	}
                	if (event.noRepeats >= 1) {
                		tween.repeatYoyo(event.noRepeats * 2, repeatDelay);   // Multiply by 2 so we always have an even number (and no missed points).
                	}
                	
                	// add way points (if any).
                	if (event.wayPoint !=null) {
                		tween.waypoint(event.wayPoint.x - tl.width/2, event.wayPoint.y - tl.height/2);
                	}
                	
                    if (tl.loop) {
                        timeline.push(tween).repeat(990999, 0);
                    }
                    else {
                        timeline.push(tween);
                    }
                    
                }// End of Event Loop.
                timeLinesList.add(timeline);
            }
        }
    }
    
    public void startAllTimeLines(TweenManager tweenManager) {
    	 for ( Timeline timeline : timeLinesList) {
    		 timeline.start(tweenManager);
    	 }
    }
    
    // Each Sprite Timeline is stored using its Sprite ID + Index Position as the Key.  This function checks if a Sprite has a Timeline (and if so the addTimeLIne function is called to create the TImeLIne Sequence for that sprite.).
    public boolean containsTimeLine(int spriteId, int spriteIndex) {
        String key = Integer.toString(spriteId) + "," + spriteIndex;        
        return gfxTimeLinesList.containsKey(key);        
    }
    
    public TweenEquation getEasingEquation(int easingFunction) {
    	if (easingFunction == 1)      return  Sine.IN;
    	else if (easingFunction == 2) return Quint.INOUT;
    	else if (easingFunction == 3) return Expo.OUT;
    	else if (easingFunction == 4) return Bounce.OUT;
    	else if (easingFunction == 5) return Back.OUT;
    	else if (easingFunction == 6) return Circ.OUT;
    	else if (easingFunction == 7) return Elastic.OUT;
    	else if (easingFunction == 8) return Cubic.OUT;
    	else if (easingFunction == 9) return Quad.OUT;

    	return Linear.INOUT;
    	
    }
    
}
