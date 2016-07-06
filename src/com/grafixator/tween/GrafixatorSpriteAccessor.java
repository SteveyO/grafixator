package com.grafixator.tween;

import aurelienribon.tweenengine.TweenAccessor;
import com.grafixator.model.GrafixatorSprite;

public class GrafixatorSpriteAccessor implements TweenAccessor<GrafixatorSprite> {
    public static final int POS_XY = 1;
    public static final int CPOS_XY = 2;
    public static final int ROTATION = 3;
    public static final int XY_AND_ROTATE = 4;

    @Override
    public int getValues(GrafixatorSprite target, int tweenType, float[] returnValues) {
        switch (tweenType) {
            case POS_XY:
                returnValues[0] = target.xPos;
                returnValues[1] = target.yPos;
                return 2;


            case XY_AND_ROTATE:
                returnValues[0] = target.xPos;
                returnValues[1] = target.yPos;
                returnValues[2] = target.rotation;
            	return 3;

            case ROTATION: returnValues[0] = target.rotation; return 1;

            default: assert false; return -1;
        }
    }

    @Override
    public void setValues(GrafixatorSprite target, int tweenType, float[] newValues) {
        switch (tweenType) {
            case POS_XY: 
                 target.xPos=newValues[0];
                 target.yPos=newValues[1];            
                 break;
            case XY_AND_ROTATE: 
            	target.xPos=newValues[0];
            	target.yPos=newValues[1];            
            	target.rotation=newValues[2];            
            	break;
            case ROTATION: 
                 target.rotation = newValues[0]; 
                 break;


            default: assert false;
        }
    }
}
