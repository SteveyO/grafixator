package com.grafixator.loader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.grafixator.GrafixatorConstants;

public class GrafixatorUtils {

    /*
     * Gets a property from a string.. for example  "den=2.3;fric=53.23;"  get("fric") returns 53.23.
     * 
     */
    public static float getPropertyFloatValue(String propertyString, String prop, float defaultValue) {
        float defltValue = defaultValue;  // A default if nothing found
        prop+="=";
        
        String props[] = propertyString.split(";");
        
        for (int i=0; i<props.length; i++) {
            if (props[i].startsWith(prop)) {
                String val=props[i].substring(prop.length());
                return Float.valueOf(val);
            }
        }
               
        return defltValue;   
    }
    
    /*
     * Gets a property from a string.. for example  "den=2.3;fric=53.23;"  get("fric") returns 53.23.
     * 
     */
    public static boolean getPropertyBooleanValue(String propertyString, String prop, boolean defaultValue) {
        prop+="=";
        
        String props[] = propertyString.split(";");
        
        for (int i=0; i<props.length; i++) {
            if (props[i].startsWith(prop)) {
                String val=props[i].substring(prop.length());
                if (val !=null) {
                    if (val.toLowerCase().equals("true")) {
                        return true;
                    }
                    if (val.toLowerCase().equals("false")) {
                        return false;
                    }
                }
            }
        }
        
        return defaultValue;   
    }
    
    /*
     * Gets a property from a string.. for example  "den=2.3;fric=53.23;"  get("fric") returns 53.23.
     * 
     */
    
    
    /*
     * gets a property from a string.. for example  "den=2.3;fric=53.23;"  get("fric") returns 53.23.
     */
    public static int getPropertyIntValue(String propertyString, String prop, int defValue) {
    	int defaultValue = defValue;  // A default if nothing found
    	prop+="=";
    	
    	String props[] = propertyString.split(";");
    	
    	for (int i=0; i<props.length; i++) {
    		if (props[i].startsWith(prop)) {
    			String val=props[i].substring(prop.length());
    			return Integer.valueOf(val);
    		}
    	}
    	
    	return defaultValue;   
    }
    
    public static String getPropertyStringValue(String propertyString, String prop) {      
        prop+="=";
        
        String props[] = propertyString.split(";");
        
        for (int i=0; i<props.length; i++) {
            if (props[i].startsWith(prop)) {
                String val=props[i].substring(prop.length());
                return val;
            }
        }
        
        return "";   
    }
    
     // Method used for box2d contour tracing.
    public static int[][] populateStaticTilesArray(TiledMapTileLayer mainLayer) {
    	
    	int array[][] = new int[mainLayer.getHeight()][mainLayer.getWidth()];
    	
		// Populate all the box2d objects using contour tracing..
		for (int h=mainLayer.getHeight(); h>= 0; h--) {
			for (int w=0; w< mainLayer.getWidth(); w++) {			
				if (mainLayer.getCell(w, h) != null) {
					MapProperties currentProperties = mainLayer.getCell(w,h).getTile().getProperties();
					
					if (!currentProperties.containsKey("MOVE_HORIZONTALLY") &&
						!currentProperties.containsKey("ROTATE") &&
						!currentProperties.containsKey("MOVE_VERTICALLY") &&
						!currentProperties.containsKey("MAIN_CHARACTER") &&
						!currentProperties.containsKey("BOX2D_BODY")) { 
						    array[h][w] = 1;
					  }
					  else {
						    array[h][w] = 0;
					  }
					  
				}
			}
		}
		
		return array;
    }

    
    // Method used for storing all level data in 2x2 array.
    public static int[][] populateArray(TiledMapTileLayer mainLayer) {
        
        int array[][] = new int[mainLayer.getHeight()][mainLayer.getWidth()];
        
        // Populate all the box2d objects using contour tracing..
        for (int h=mainLayer.getHeight(); h>= 0; h--) {
            for (int w=0; w< mainLayer.getWidth(); w++) {			
                if (mainLayer.getCell(w, h) != null) {
                    MapProperties currentProperties = mainLayer.getCell(w,h).getTile().getProperties();
                    
                    if (!currentProperties.containsKey("MOVE_HORIZONTALLY") &&
                            !currentProperties.containsKey("ROTATE") &&
                            !currentProperties.containsKey("MOVE_VERTICALLY") &&
                            !currentProperties.containsKey("MAIN_CHARACTER") &&
                            !currentProperties.containsKey("BOX2D_BODY")) { 
                        array[h][w] = mainLayer.getCell(w,h).getTile().getId();
                    }
                    else {
                        array[h][w] = 0;
                    }
                    
                }
            }
        }
        
        return array;
    }
    
    


    // Method calculates how many squares down or left we can use for box2d contour tracing.    
	public static int howManySq(int x, int y, int dir, int array[][], int done[][], boolean markAsDone) {
		int noSq=0;

		if (dir==GrafixatorConstants.RIGHT) {
			int len=array[0].length-x;
			for (int i=0; i<len; i++) {                     
				if (array[y][x+i] !=0 && done[y][x+i] != GrafixatorConstants.BODY_CREATED) {
					noSq++;
					if (markAsDone) array[y][x+i]=GrafixatorConstants.BODY_CREATED;
				}
				else {
					return noSq;
				}
			}
		}
		if (dir==GrafixatorConstants.DOWN) {
			int len=array.length-y;
			for (int i=0; i<len; i++) {       

				if (array[y+i][x] !=0 && done[y+i][x] != GrafixatorConstants.BODY_CREATED) {
					if (markAsDone) array[y+i][x]=GrafixatorConstants.BODY_CREATED;
					noSq++;
				}
				else {
					return noSq;
				}
			}
		}      
		return noSq;
	}
	
    public static boolean overlapRectangles (int x1, int y1, int width1, int height1, int x2, int y2, int width2, int height2) {
        if (x1 < x2 + width2 && x1 + width1 > x2 && y1 < y2 + height2 && y1 + height1 > y2)
            return true;
        else
            return false;
    }
    
    public static boolean overlapRectangles (float x1, float y1, float width1, float height1, float x2, float y2, int width2, int height2) {
        if (x1 < x2 + width2 && x1 + width1 > x2 && y1 < y2 + height2 && y1 + height1 > y2)
            return true;
        else
            return false;
    }
    


   public static int getCharMovement(String charMovement) {       
       if (charMovement.equals("PLATFORMER")) return GrafixatorConstants.CHARACTER_PLATFORMER;
       else if (charMovement.equals("SPACESHIP")) return GrafixatorConstants.CHARACTER_SPACESHIP;
       else if (charMovement.equals("JUMP")) return GrafixatorConstants.CHARACTER_JUMP_UPWARDS;
       else if (charMovement.equals("CATAPULT")) return GrafixatorConstants.CHARACTER_CATAPULT;
       else if (charMovement.equals("SPACESHIP")) return GrafixatorConstants.CHARACTER_SPACESHIP;
       else return GrafixatorConstants.CHARACTER_STANDARD;
   }


   public static int getCameraControl(String cameraControl) {
       if (cameraControl.equals("HORIZ"))         return GrafixatorConstants.CAMERA_CONTROL_HORIZONTAL;
       else if (cameraControl.equals("VERTICAL")) return GrafixatorConstants.CAMERA_CONTROL_VERTICAL;
       else if (cameraControl.equals("CENTERED")) return GrafixatorConstants.CAMERA_CONTROL_PLAYER_CENTERED;
       else if (cameraControl.equals("VIEWPORT")) return GrafixatorConstants.CAMERA_CONTROL_VIEWPORT;
       else return GrafixatorConstants.CAMERA_CONTROL_NONE;
   }  
   
   /*
    * Converts a String colour to a Libgdx Color.  Used mostly for Box2d Ray Colors. 
    */
	public static  Color getColorFromString(String colorString, Color defaultColor) {
		if (colorString == null) return defaultColor;
		
		if (colorString.contains(",")) {
			String cols[] = colorString.split(",");

			if (cols.length == 3) {
				return new Color(Float.valueOf(cols[0]), Float.valueOf(cols[1]), Float.valueOf(cols[2]),1f);
			}
		}
		
		return defaultColor;  // Should never get here.  Fall back only.
	}
	
}
