package com.grafixator;


public class GrafixatorConstants {
    
    public final static String VERSION="0.1";

    public static final int CHARACTER_STANDARD=0;   
    public static final int CHARACTER_PLATFORMER=1; 
    public static final int CHARACTER_SPACESHIP=2;  
    public static final int CHARACTER_JUMP_UPWARDS=3;
    public static final int CHARACTER_CATAPULT=4;
    
    public static final int GAME_STATUS_MOVING=0;
    public static final int GAME_STATUS_STATIC=1;
    public static final int GAME_STATUS_MOVING_FULL_SQUARE=2;  //  Sprite is moving to a boundary, but has completed a full square (so at this point the player may change direction). 
    
	public static final int SHAPE_TYPE_PATH=1;
	public static final int SHAPE_TYPE_CIRCLE=2;
	public static final int SHAPE_TYPE_RECTANGLE=3;
	public static final int SHAPE_TYPE_TRIANGLE=4;
    
    public final static int CAMERA_CONTROL_NONE             = -1;
    public final static int CAMERA_CONTROL_PLAYER_CENTERED  =  1;
    public final static int CAMERA_CONTROL_HORIZONTAL       =  2;
    public final static int CAMERA_CONTROL_VERTICAL         =  3;
    public final static int CAMERA_CONTROL_VIEWPORT         =  4;
    public final static int CAMERA_CONTROL_PLAYER_CENTERED_X  =  5;
    public final static int CAMERA_CONTROL_PLAYER_CENTERED_Y  =  6;
        
    public static final String PROPERTY_KEY_BOX2D_BODY        = "BOX2D_BODY";
    public static final String PROPERTY_KEY_BOX2DLIGHT        = "BOX2DLIGHT";
    public static final String PROPERTY_KEY_ENEMY_SPRITE      = "ENEMY_SPRITE";
    public static final String PROPERTY_KEY_ROTATE            = "ROTATE";
    public static final String PROPERTY_KEY_SHRINK_EXPAND     = "SHRINK_EXPAND";
    public static final String PROPERTY_KEY_PICKUP            = "PICKUP";
    public static final String PROPERTY_KEY_MOVING_PLATFORM   = "MOVING_PLATFORM";
    public static final String PROPERTY_KEY_SPRITE_MOVEMENT   = "SPRITE_MOVEMENT";
    public static final String PROPERTY_KEY_MAIN_CHARACTER    = "HERO_CHARACTER";
    public static final String PROPERTY_KEY_PLAYER_BULLET     = "PLAYER_BULLET";
    public static final String PROPERTY_KEY_ENEMY_BULLET      = "ENEMY_BULLET";
    public static final String PROPERTY_KEY_DESTRUCTIBLE      = "DESTRUCTIBLE";
    public static final String PROPERTY_KEY_ENEMY_FIRE        = "ENEMY_FIRE";
    
    public static final String PROPERTY_VALUE_NO_VALUE="NO_VALUE";
    public static final String PROPERTY_VALUE_SPRITE_VELOCITY="VEL";
    public static final String PROPERTY_VALUE_SPRITE_FRICTION="FRI";
    public static final String PROPERTY_VALUE_SPRITE_RESTITUTION="RES";
    public static final String PROPERTY_VALUE_SPRITE_DENSITY="DEN";
    public static final String PROPERTY_VALUE_SPRITE_SHAPE="SHP";
    
    // Box2dLights
    public static final String PROPERTY_VALUE_BOX2DLIGHT_TYPE="BTYP";
    public static final String PROPERTY_VALUE_BOX2DLIGHT_DISTANCE="BDIS";
    public static final String PROPERTY_VALUE_BOX2DLIGHT_CONE_ANGLE="BCANG";
    public static final String PROPERTY_VALUE_BOX2DLIGHT_DIRECTION_ANGLE="BDANG";
    public static final String PROPERTY_VALUE_BOX2DLIGHT_COLOR="BCOL";
    public static final String PROPERTY_VALUE_BOX2DLIGHT_NO_RAYS="BRAYS";
    
    public static final String PROPERTY_VALUE_SPRITE_SHAPE_STATIC="ST";
    public static final String PROPERTY_VALUE_SPRITE_SHAPE_CIRCLE="CI";
    public static final String PROPERTY_VALUE_SPRITE_SHAPE_SQUARE="SQ";
    public static final String PROPERTY_VALUE_SPRITE_ROTATION_SPEED="RS";
    public static final String PROPERTY_VALUE_SPRITE_ROTATE_CW="RD";
    public static final String PROPERTY_VALUE_SPRITE_FULL_ROTATION="FULLR";
    public static final String PROPERTY_VALUE_SPRITE_ROTATION_START_ANGLE="ROTS";
    public static final String PROPERTY_VALUE_SPRITE_ROTATION_END_ANGLE="ROTE";
    public static final String PROPERTY_VALUE_SPRITE_HITS_TO_DESTROY="HITS";
    public static final String PROPERTY_VALUE_SPRITE_EFFECTS="EFF";
    
    public static final String PROPERTY_VALUE_SPRITE_MOVEMENT= "SpriteMovement";
    public static final String PROPERTY_VALUE_SPRITE_SPEED   = "SpriteSpeed";
    
    public static final String PROPERTY_VALUE_CHARACTER_MOVEMENT        ="movementType";
    public static final String PROPERTY_VALUE_CHARACTER_MOVE_2_BOUNDARY ="moveToBoundary";
    public static final String PROPERTY_VALUE_CHARACTER_ROATE_TO_DIR    ="charRotateToDir";
    public static final String PROPERTY_VALUE_CHARACTER_FULL_SQUARE     ="fullSquare";
    public static final String PROPERTY_VALUE_CHARACTER_SPEED           ="charSpeed";
    public static final String PROPERTY_VALUE_CAMERA_CONTROL            ="cameraControl";
    public static final String PROPERTY_VALUE_CAMERA_SPEED              ="cameraSpeed";
    
        
    public static final String PROPERTY_VALUE_ENEMY_FIRE_TEXTURE="FireTexture";
    public static final String PROPERTY_VALUE_ENEMY_FIRE       ="FireDir";
    public static final String PROPERTY_VALUE_ENEMY_FIRE_SPEED ="FireSpeed";
    public static final int    PROPERTY_VALUE_ENEMY_FIRE_TYPE_PLAYER=0;
    public static final int    PROPERTY_VALUE_ENEMY_FIRE_TYPE_UP    =1;
    public static final int    PROPERTY_VALUE_ENEMY_FIRE_TYPE_DOWN  =2;
    public static final int    PROPERTY_VALUE_ENEMY_FIRE_TYPE_LEFT  =3;
    public static final int    PROPERTY_VALUE_ENEMY_FIRE_TYPE_RIGHT =4;
    public static final int    PROPERTY_VALUE_ENEMY_FIRE_TYPE_BOSS =5;
    
    public static final int SPRITE_MOVEMENT_HORIZONTAL=0;
    public static final int SPRITE_MOVEMENT_VERTICAL=1;
    
    // Platformer Games
    public static final float DEFAULT_JUMP_VERTICAL_FORCE    = 8f;    
    public static final float DEFAULT_JUMP_HORIZ_FORCE       = 5f;    
    public static final float DEFAULT_CHAR_LINEAR_VELOCITY   = 6f; 
    
    public static final int BODY_CREATED=-1;
    public static final int DOWN=1;
    public static final int RIGHT=2;    
    
    
    // Different player fire types.
    public final static int PLAYER_FIRE_NONE             = -1;
    public final static int PLAYER_FIRE_PLAYER_DIRECTION =  1;
    public final static int PLAYER_FIRE_HORIZONTAL       =  2;
    public final static int PLAYER_FIRE_VERTICAL         =  3;
    
    // Default values for Character Movements.
    public static final float DEFAULT_SPACESHIP_THRUST_FORCE = 13f;   // Spaceship game
    public static final float DEFAULT_JUMP_UPWARDS_FORCE     = 5f;    // Jump Upwards Game

}
