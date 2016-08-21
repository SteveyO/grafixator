package com.grafixator.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.physics.box2d.Body;

import box2dLight.ConeLight;
import box2dLight.PointLight;

/**
 * 
 * @author SteveyO
 * 
 * A GrafixatorSprite is any sprite, image or animation.  
 * Any tile for which the developer has selected properties such as Box2D, Rotate, Bullet etc,  are also rendered as Grafixator Sprites (these are removed from the TiledMap).
 * 
 * Any Box2d or Box2dLight properties are attached to this object, as well as Destructable properties (no hits to destroy), rotation etc.
 * 
 * Grafixator Sprites are stored in the spriteList ArrayList (in your GrafixatorGame) and are usually rendered after the Colision tile layer.
 * 
 */

public class GrafixatorSprite {
    
    public int id;   // Used mostly for mapping timelines to sprites.
    
    public static final int SPRITE_STATUS_ACTIVE=1;
    public static final int SPRITE_STATUS_INACTIVE=2;
    
    public TextureRegion spriteTexture;
    public float   stateTime=0;  // Used for animation frames.
    
    public boolean isHero=false;  // Set to true for the main 'controllable' character.
    public boolean isAnimation=false;
	public boolean isMovingPlatform=false;
    public Animation spriteAnimation;
    
    public MapProperties mapProperties = new MapProperties();  // Only for non-tiles.  For tiles we use the tile properties.
	public List<ImageSprite> spritePositions = new ArrayList<ImageSprite>();  // All points for sprites.  The positions are the x and y of the rectangle. And width/height are of the sprite, but these may be resized.

    public String name;
    
    public String effectName="";     // The name of the Particle Effect to play if Sprite explodes.
    
    public boolean movingVertically;
    public boolean movingHorizontally;
    public boolean rotating;
    public boolean shrinkExpanding;
    public boolean box2DBody;
    public boolean isPickup=false;
    
    public boolean isDesctructible=false; // Can the sprite be shot?  Set as a Tile Property.
    public int hitsNeededToDestroy;       // This will default to 1, but can be increased in Tile Properties.
    public Body body;
    
	public int   spriteMovementType=-1; // -1 None,  0-Horizontal, 1-Vertical
	public float spriteSpeed;
    
	public PointLight pointLight;
	public ConeLight  coneLight;
    
    public float velocity;
    
    public boolean enemyFire=false;
    public int bulletTextureNo;   // 0-9 
    public int enemyFireDirection;
    public int enemyFireSpeed;
    public long lastFireTime=System.currentTimeMillis();
    public long nextFireTime=System.currentTimeMillis() + 10000000;
    
    
    public boolean spriteDecreasing=false;
    public float shrinkExpandSpeed;
    public float rotationSpeed;
    public boolean fullRotation;
    public int rotationDirection;
	public float rotationStartAngle;
	public float rotationEndAngle;
    
    public float renderWidth;
    
    public int width;
    public int height;
    
    public float xPos;
    public float yPos;
    public float rotation;
    public int xDir;
    public int yDir;
    public int status=SPRITE_STATUS_ACTIVE;
    
	public int origX;  // Original X Position in the Map.
	public int origY;  // Original Y Position in the Map.
    
    public int tileNumber;
    public String userData;
        
    
}
