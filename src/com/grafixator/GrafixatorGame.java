package com.grafixator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;
import com.grafixator.loader.GrafixatorFileLoader;
import com.grafixator.loader.GrafixatorUtils;
import com.grafixator.managers.BackgroundManager;
import com.grafixator.managers.ParticleEffectManager;
import com.grafixator.managers.TimeLineManager;
import com.grafixator.model.GrafixatorBullet;
import com.grafixator.model.GrafixatorConfiguration;
import com.grafixator.model.GrafixatorObject;
import com.grafixator.model.GrafixatorParticle;
import com.grafixator.model.GrafixatorSprite;
import com.grafixator.tween.GrafixatorSpriteAccessor;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import box2dLight.PointLight;
import box2dLight.RayHandler;


public class GrafixatorGame {
	
	public TiledMapRenderer tiledMapRenderer;
	public TiledMap tiledMap;  
	public TiledMapTileLayer mainLayer;    // Stores a reference to the main (Collision) layer.

	public GrafixatorSprite heroSprite;  // Convenience way to get the hero (main/controllable character), rather than loop through the spriteList.  
	public Body mainCharBody;

	
	public GrafixatorConfiguration gConfiguration;
	public World world;    									// Box2D World
	public RayHandler rayHandler;
	public ArrayList<GrafixatorSprite> spriteList          = new ArrayList<GrafixatorSprite>();     // A list of all movable objects in the game.  These will be moved/rendered when game is played.
	
	public List<Rectangle>             collisionRectangles = new ArrayList<Rectangle>();            // A list of Rectangles, for none Box2D Games (from main collision layer).
	public List<GrafixatorBullet>      bulletsList         = new ArrayList<GrafixatorBullet>();     // As suggested, a list of all bullets (player and enemy).
    
    public List<GrafixatorObject>      collisionObjectsList   = new ArrayList<GrafixatorObject>();  // List of all Collision Objects (Paths/Vertices, Circles, Triangles or Rectangles). These are added as Box2D shapes to your game.

	
	// bullet pool (as to not create Bullet Instances in Game Loop).
	private final Pool<GrafixatorBullet> bulletPool = new Pool<GrafixatorBullet>() {
		@Override
		protected GrafixatorBullet newObject() {
			return new GrafixatorBullet();
		}
	};
	
  
	// Debug Stuff
	public boolean debugBox2D=false;
	private Box2DDebugRenderer debugRenderer;
	private Matrix4 debugMatrix;  
	
    public String userData;   // Any game data that may be useful.

	public int tileWidth;
	public int tileHeight;
	public int numberOfColumns;
	public int numberOfRows;
	public float backgroundColorR;
	public float backgroundColorG;
	public float backgroundColorB;

	public float worldGravity;
	public float worldDensity;
	public float worldFriction;
	public float worldRestitution;
	
	public float lastCamXPos;   // For updating the Backgrounds
	public float lastCamYPos;
	
	// Variables for the Universal Tween Engine
	public TweenManager 	 tweenManager;
	public TimeLineManager 	 timeLineManager   = TimeLineManager.getInstance();
	public BackgroundManager backgroundManager = BackgroundManager.getInstance();

	// Default particle Effect settings;
	public boolean useParticles=true; 
	public float   particleGravity; 
	public float   particleVelocity; 
	public int     noParticles; 

	//  If scrolling Camera, these variables set the position to stop scrolling (if specified).
	public boolean cameraAutoStop=false;
	public int cameraVertStopPosition=0;    // Stop the camera after a certain point
	public int cameraHorizStopPosition=0;  

	// Mouse Click (Jump Upwards) variables
	public float    charUpwardsForce=GrafixatorConstants.DEFAULT_JUMP_UPWARDS_FORCE;

	// Thrust Force (For SpaceShip game)
	public float    charThrustForce=GrafixatorConstants.DEFAULT_SPACESHIP_THRUST_FORCE;
    public boolean charFixedRotation = false;
	
	// Character Variables
	public int  charMovement;
	public boolean charMoveToBoundary;
	public boolean charRotateToDir;
	public boolean charFullSquare;
	public float   charSpeed;
	public int     movementStatus=GrafixatorConstants.GAME_STATUS_STATIC;
	public int     pixelsMoved=0;  // This variable is used if Character Full Block movement is selected (in which case must be a multiple of player sprite width/height).


	// Camera Variables
	public float cameraSpeed=0;
	public int   cameraControl;	
	public int   cameraViewHoriz=50;   // If a Camera Viewport is selected,  the Horizontal percentage a player can move inside before the camera scrolls.
	public int   cameraViewVert=50;
	
    // Variables for Catapult fire
    public boolean isPlayerBeingDragged;
    public float origPlayerXPos;
    public float origPlayerYPos;
    public float xOffset, yOffset;
	
	//Variables for Platformer Games
	boolean isGrounded;
	public float   charJumpHorizontalForce;
	public float   charJumpVerticalForce;
	public float   charLinearVelocity;
	public boolean charAllowDoubleJump;
	int noJumps;
	
	
	// Variables for Box2d Lights
	public boolean useBox2dShadows;
	public boolean diffuseBox2dLight;
	public boolean box2dLightsBlur;

	// Player Bullet Variables
	public int playerFire=-1;
	public int bulletSpeed=16;
	public int bulletXOffset=0;
	public int bulletYOffset=0;
	public  TextureRegion bulletPlayerTexture;		
	public  TextureRegion bulletTextureEnemy[] = new TextureRegion[9];
	public  Random rand = new Random();  // For randomizing the next bullet fire time.

	public Vector3 playerPosition = new Vector3();                // Used for SpaceShip Games (to centre the camera).
	public  Vector3 enemyPosition = new Vector3();                // Used for checking if sprite is offscreen (if so then don't fire).
	private Vector3       bulletPositionVector = new Vector3();   // Used for checking if bullet is offscreen (in which case made inactive).
	public float          timeBetweenBulletFire=200;

	private static long CLEAN_UP_BULLET_TIME = 5000;  // Every 5 seconds remove inactive bullets from list.
	public static long lastBulletCleanUp=System.currentTimeMillis();

	public float mainCharStartX=0;  // Main Character start position (used for resetting to its initial position).
	public float mainCharStartY=0;
	
	public PointLight    playerBulletPointLight;                       // Box2dlight for the player bullet (if any)
	public PointLight    enemyBulletPointLight[] = new PointLight[9];  // Box2dlights for the enemy bullets (if any).

	public int particlesDefaultNumber=20;  
	
	public int layerRenderCounter=0;

	public GrafixatorGame(GrafixatorConfiguration gConfig) {
		gConfiguration = gConfig;
		// Initialize the Tween Engine
		Tween.registerAccessor(GrafixatorSprite.class,  new GrafixatorSpriteAccessor());
		tweenManager = new TweenManager();
		Tween.setWaypointsLimit(1);
        Tween.setCombinedAttributesLimit(3);
        
        // BOX2D Stuff..
        if (gConfiguration.isUseBox2d() || gConfiguration.isUseBox2dLights()) {
        	world = new World(new Vector2(0.0f, 0.0f), true);
        	debugRenderer = new Box2DDebugRenderer();

        	world.setGravity(new Vector2(0, -9));   // -9 is the default.  It will get overwritten by the value in the grafixator file.
        }
        if (gConfiguration.isUseBox2dLights()) {
          rayHandler = new RayHandler(world);
        }
        
	}
	
	public GrafixatorGame loadGrafixatorGame(String fileName, SpriteBatch batch) {
		if (gConfiguration.getSpriteSheetName() != null) {
		    return GrafixatorFileLoader.load(this, fileName, batch, gConfiguration.getSpriteSheetName(), gConfiguration.getTileWidth(), gConfiguration.getTileHeight(), gConfiguration);
		}
		else {
			return GrafixatorFileLoader.load(this, fileName, batch, null, -1, -1, gConfiguration);
		}
	}
	
	public void startTimelines() {
		 timeLineManager.startAllTimeLines(tweenManager);
	}

	public void render(Batch batch, OrthographicCamera gameCam, int renderSpritesAfter) {
		if (tiledMapRenderer!=null && tiledMap !=null) {
			tiledMapRenderer.setView(gameCam);  

			int layerRenderCounter=0;
			batch.begin();

			for (Iterator<MapLayer> layersIter = tiledMap.getLayers().iterator(); layersIter.hasNext();){
				TiledMapTileLayer currLayer = (TiledMapTileLayer) (layersIter.next());
				tiledMapRenderer.renderTileLayer(currLayer);

				if (layerRenderCounter==renderSpritesAfter) {
					renderSpriteList(batch);
				}
				layerRenderCounter++;
			}

			batch.end();
		}
		
		if (debugBox2D) {
			debugMatrix = gameCam.combined.cpy().scale(tileWidth, tileHeight, 0f);
			debugRenderer.render(world, debugMatrix);
		}
	}
	
	public void drawBackgrounds(Batch batch, OrthographicCamera gameCam ) {
		backgroundManager.render(batch, gameCam);
	}

	public void renderSpriteList(Batch batch) {

		for (int s=0; s<spriteList.size(); s++) {
			GrafixatorSprite currSprite = spriteList.get(s);
			
			if (currSprite.status == GrafixatorSprite.SPRITE_STATUS_INACTIVE) continue;

			if (currSprite.isAnimation) {
				
				currSprite.stateTime += Gdx.graphics.getDeltaTime();
				if (currSprite.stateTime < 0) currSprite.stateTime=0;

			    if (charMovement == GrafixatorConstants.CHARACTER_PLATFORMER || !currSprite.isHero) {   // For Platform Games we flip sprites when they change direction.
			    batch.draw(currSprite.spriteAnimation.getKeyFrame(currSprite.stateTime, true), 
			    		currSprite.xDir < 0 ? currSprite.xPos+currSprite.width : currSprite.xPos, 
			            currSprite.yPos,  
			            currSprite.width/2,
			            currSprite.height/2,
			            currSprite.xDir < 0 ? -currSprite.width : currSprite.width,
			            currSprite.height,
			            currSprite.renderWidth,    
			            1f, 
			            currSprite.rotation);	
			    }
			    else {
				    batch.draw(currSprite.spriteAnimation.getKeyFrame(currSprite.stateTime, true), 
				    		currSprite.xPos, 
				            currSprite.yPos,  
				            currSprite.width/2,
				            currSprite.height/2,
				            currSprite.width,
				            currSprite.height,
				            currSprite.renderWidth,    
				            1f, 
				            currSprite.rotation);	
			    }                              
			}
			else {
				batch.draw(currSprite.spriteTexture, 
						currSprite.xPos, 
						currSprite.yPos,  
						currSprite.width/2,
						currSprite.height/2,
						currSprite.width,
						currSprite.height,
						currSprite.renderWidth, 
						1f, 
						currSprite.rotation);
			}
		}      // End of for spriteList   
	}	


	public void updateSprites(OrthographicCamera camera) {
		for (int s=0; s<spriteList.size(); s++) {
			GrafixatorSprite currSprite = spriteList.get(s);

			if (currSprite.status == GrafixatorSprite.SPRITE_STATUS_INACTIVE) continue;
			
			if (currSprite.pointLight !=null) {
				currSprite.pointLight.setPosition(currSprite.xPos + currSprite.width/2, currSprite.yPos + currSprite.height/2);	
			}
			if (currSprite.coneLight !=null) {
				if (charFixedRotation && currSprite.isHero) {
					if (currSprite.xDir == 1) currSprite.coneLight.setDirection(currSprite.rotation);
					else if (currSprite.xDir == -1) currSprite.coneLight.setDirection(-180 + currSprite.rotation);
				}

				else if (currSprite.rotating || (currSprite.isHero && charMovement == GrafixatorConstants.CHARACTER_SPACESHIP)) {
					currSprite.coneLight.setDirection(currSprite.rotation+90);
				}        	

				currSprite.coneLight.setPosition(currSprite.xPos + currSprite.width/2, currSprite.yPos + currSprite.height/2);	
			}

			currSprite.xPos+=currSprite.xDir * currSprite.velocity;
			currSprite.yPos+=currSprite.yDir * currSprite.velocity;


			if (currSprite.isPickup && heroSprite !=null) {
				if (GrafixatorUtils.overlapRectangles(heroSprite.xPos, heroSprite.yPos, heroSprite.width, heroSprite.height, (int) currSprite.xPos, (int) currSprite.yPos, currSprite.width, currSprite.height)) {
					if (useParticles) {
						ParticleEffectManager.getInstance().addEffect(new Sprite(currSprite.spriteTexture), GrafixatorParticle.EFFECT_TYPE_PICKUP, currSprite.xPos + currSprite.width/2, currSprite.yPos + currSprite.height, 100, 6, 1, 500);						             
					}
					currSprite.status = GrafixatorSprite.SPRITE_STATUS_INACTIVE;
                    if (currSprite.pointLight !=null) 		{ currSprite.pointLight.remove();  		}
                    if (currSprite.coneLight !=null)  		{ currSprite.coneLight.remove();  		}
				}
			}

			// Handle Player Firing
			if (currSprite.enemyFire) {
				if (System.currentTimeMillis() > currSprite.nextFireTime) {

					enemyPosition.x = currSprite.xPos;
					enemyPosition.y = currSprite.yPos;

					if (camera.frustum.pointInFrustum(enemyPosition)) {
						addEnemyBullet((int) currSprite.xPos, (int) currSprite.yPos, currSprite.width, currSprite.height, currSprite.bulletTextureNo, currSprite.enemyFireDirection, currSprite.enemyFireSpeed, currSprite.rotation);
						currSprite.nextFireTime = System.currentTimeMillis() + 1000 + rand.nextInt(1000); 
					}                       

				}

			}
            if (currSprite.isHero) {
                
                if (charMovement == GrafixatorConstants.CHARACTER_JUMP_UPWARDS) {
                	
                	if (cameraAutoStop) {
                		if ( cameraSpeed > 0 && camera.position.x < cameraHorizStopPosition || cameraSpeed < 0 && camera.position.x > cameraHorizStopPosition) {
                		currSprite.xPos+=cameraSpeed;
                		}
                	}
                	else {
                		currSprite.xPos+=cameraSpeed;
                	}
                	
         
                //    currSprite.xPos=getSpriteXPosFromBox2DBody(currSprite);
                  currSprite.yPos=getSpriteYPosFromBox2DBody(currSprite);
              	  currSprite.body.setTransform((currSprite.xPos + currSprite.width/2)  / tileWidth, (currSprite.yPos + currSprite.height/2) / tileHeight, 0f);
              	  
              	  
                }
                else if (charMovement == GrafixatorConstants.CHARACTER_CATAPULT) {
                	if (!isPlayerBeingDragged) {
                    currSprite.xPos=getSpriteXPosFromBox2DBody(currSprite);
                    currSprite.yPos=getSpriteYPosFromBox2DBody(currSprite);
                	}
                }
                else if (charMovement == GrafixatorConstants.CHARACTER_PLATFORMER) {
                	currSprite.xPos=getSpriteXPosFromBox2DBody(currSprite);
                	currSprite.yPos=getSpriteYPosFromBox2DBody(currSprite);
                }
                else if (charMovement == GrafixatorConstants.CHARACTER_SPACESHIP ) {
                    currSprite.xPos=getSpriteXPosFromBox2DBody(currSprite);
                    currSprite.yPos=getSpriteYPosFromBox2DBody(currSprite);                
                }

            }	 
            else {

              if (currSprite.isMovingPlatform) { 
            	  currSprite.body.setTransform((currSprite.xPos + currSprite.width/2)  / tileWidth, 
            			                       (currSprite.yPos + currSprite.height/2) / tileHeight, 0f);
              }
              else if (currSprite.box2DBody) { 
            	  currSprite.xPos=getSpriteXPosFromBox2DBody(currSprite);
            	  currSprite.yPos=getSpriteYPosFromBox2DBody(currSprite);
            	  currSprite.rotation= (currSprite.body.getAngle() * 180) / 3.1415f;  
              }
            }

			if (currSprite.rotating) {
				currSprite.rotation+=currSprite.rotationSpeed*currSprite.rotationDirection;

			}

			if (currSprite.shrinkExpanding) {

				if (currSprite.spriteDecreasing) {
					currSprite.renderWidth-=currSprite.shrinkExpandSpeed;
					if (currSprite.renderWidth<currSprite.shrinkExpandSpeed) {
						currSprite.spriteDecreasing=false;
					}
				}
				if (!currSprite.spriteDecreasing) {
					currSprite.renderWidth+=currSprite.shrinkExpandSpeed;
					if (currSprite.renderWidth>1) {
						currSprite.spriteDecreasing=true;
					}
				}

			}

		}      // End of for spriteList          
	}

	public void jumpUpwards() {		
			mainCharBody.setLinearVelocity(0, charUpwardsForce);
	}
	
	// Returns true if bullet hits enemy.
	public boolean drawAndUpdateBullets(Batch batch, OrthographicCamera camera) {
		for (GrafixatorBullet bullet: bulletsList) {          
			if (bullet.status==GrafixatorBullet.BULLET_STATUS_INACTIVE) continue;

			batch.draw(bullet.texture, bullet.bulletXPos, bullet.bulletYPos, bullet.width/2, bullet.height/2, bullet.width, bullet.height, 1f, 1f, bullet.rotation);

			for (int s=0; s<spriteList.size(); s++) {
				GrafixatorSprite currSprite = spriteList.get(s);

				if (currSprite.status == GrafixatorSprite.SPRITE_STATUS_INACTIVE) continue;

				// Collision with a sprite                   
				if (currSprite!=null && currSprite.isDesctructable && !currSprite.isHero && bullet.fireByPlayer && GrafixatorUtils.overlapRectangles((int) currSprite.xPos, (int) currSprite.yPos, currSprite.width, currSprite.height, (int) bullet.bulletXPos, (int) bullet.bulletYPos, bullet.width,bullet.height)) {

					bullet.status = GrafixatorBullet.BULLET_STATUS_INACTIVE;
            	    if (bullet.pointLight !=null) { bullet.pointLight.remove();  bullet.pointLight = null; }
					
					if(currSprite.hitsNeededToDestroy <=1) {

						currSprite.status=GrafixatorSprite.SPRITE_STATUS_INACTIVE;  
                        if (currSprite.pointLight !=null) 		{ currSprite.pointLight.remove();  		}
                        if (currSprite.coneLight !=null)  		{ currSprite.coneLight.remove();  		}


						if (currSprite.body !=null) {       // If sprite that has been shot has a body then remove it.
							world.destroyBody(currSprite.body);
						}

						if (useParticles) {
							ParticleEffectManager.getInstance().addEffect(new Sprite(currSprite.spriteTexture), GrafixatorParticle.EFFECT_TYPE_EXPLOSION, currSprite.xPos + currSprite.width/2, currSprite.yPos + currSprite.height, 100, noParticles, particleVelocity, particleGravity);
						}

					}
					else {
						currSprite.hitsNeededToDestroy--;
					}
				}                                                   
			}


			bullet.bulletXPos += bullet.bulletXDir * bullet.speed;
			bullet.bulletYPos += bullet.bulletYDir * bullet.speed;  
			
	        // If there is a box2dlight associated with the bullet then update its position.
	        if (bullet.pointLight !=null) {
	        	bullet.pointLight.setPosition(bullet.bulletXPos + bullet.width/2, bullet.bulletYPos + bullet.height/2);
	        }

			for (Rectangle rect: collisionRectangles) {
				if (GrafixatorUtils.overlapRectangles(rect.x, rect.y, rect.width, rect.height, 
						bullet.bulletXPos,  bullet.bulletYPos, bullet.width, bullet.height)) {
					bullet.status = GrafixatorBullet.BULLET_STATUS_INACTIVE;
            	    if (bullet.pointLight !=null) { bullet.pointLight.remove();  bullet.pointLight = null; }
					
					if (bullet.fireByPlayer) {
						if (useParticles) {
							ParticleEffectManager.getInstance().addEffect( new Sprite(bulletPlayerTexture), GrafixatorParticle.EFFECT_TYPE_EXPLOSION, bullet.bulletXPos , bullet.bulletYPos + bullet.height/2, 100, noParticles, particleVelocity, particleGravity);
						}
					}
					else {
						if (useParticles) {
							ParticleEffectManager.getInstance().addEffect( new Sprite(bullet.texture), GrafixatorParticle.EFFECT_TYPE_EXPLOSION, bullet.bulletXPos , bullet.bulletYPos + bullet.height/2, 100, noParticles, particleVelocity, particleGravity);
						}
					}

					return true;
				}

			}

			bulletPositionVector.x = bullet.bulletXPos;
			bulletPositionVector.y = bullet.bulletYPos;

			if (!camera.frustum.pointInFrustum(bulletPositionVector)) {
				bullet.status = GrafixatorBullet.BULLET_STATUS_INACTIVE;
        	    if (bullet.pointLight !=null) { bullet.pointLight.remove();  bullet.pointLight = null; }
			}

		}


		if ( (System.currentTimeMillis() -lastBulletCleanUp) >= CLEAN_UP_BULLET_TIME)  {

	    	GrafixatorBullet currBullet;
		    for (int i = bulletsList.size(); --i > 0;) {
		    	currBullet = bulletsList.get(i);
		    	if (currBullet.status == GrafixatorBullet.BULLET_STATUS_INACTIVE) {
		    		bulletsList.remove(i);
		    		if (currBullet.pointLight !=null) { currBullet.pointLight.remove();  currBullet.pointLight = null; }
		    		bulletPool.free(currBullet);
		    	}
		    }
		    
			lastBulletCleanUp = System.currentTimeMillis();
		}       

		return false;
	}  


	public boolean fireBullet() {

		if (heroSprite == null || bulletPlayerTexture == null) return false;
		
		if ( (System.currentTimeMillis() - GrafixatorBullet.lastBulletFiredTime) >= timeBetweenBulletFire)  {
			GrafixatorBullet.lastBulletFiredTime = System.currentTimeMillis();

			GrafixatorBullet bullet = bulletPool.obtain();
			
            bullet.init(true, bulletSpeed,  bulletPlayerTexture, 
            		heroSprite.xPos + (heroSprite.width/2  - bulletPlayerTexture.getRegionWidth()/2), 
            		heroSprite.yPos + (heroSprite.height/2 - bulletPlayerTexture.getRegionHeight()/2));
            

			if (heroSprite.isAnimation) {
				bullet.bulletYPos = heroSprite.yPos - (heroSprite.height/2 + bulletPlayerTexture.getRegionHeight()/2);
			}

			bullet.width=bulletPlayerTexture.getRegionWidth();
			bullet.height=bulletPlayerTexture.getRegionHeight();
			bullet.rotation = heroSprite.rotation;
			bullet.status = GrafixatorBullet.BULLET_STATUS_ACTIVE;

			if (playerFire == GrafixatorConstants.PLAYER_FIRE_VERTICAL) {
				bullet.bulletYDir = bulletSpeed/bulletSpeed;
			}
			else if (playerFire == GrafixatorConstants.PLAYER_FIRE_HORIZONTAL) {
				bullet.bulletXDir = bulletSpeed/bulletSpeed;
			}
			else {
				bullet.bulletXDir = heroSprite.xDir;
				bullet.bulletYDir = heroSprite.yDir;    
			}
			
            if (playerFire == GrafixatorConstants.PLAYER_FIRE_PLAYER_DIRECTION || (charMovement == GrafixatorConstants.CHARACTER_SPACESHIP && !charFixedRotation)) {
                float angle = heroSprite.rotation;
                angle+=90;
                angle=(float) Math.toRadians(angle);
                bullet.bulletXDir = (float)  Math.cos(angle);
                bullet.bulletYDir = (float)  Math.sin(angle);   
            }
            else if (playerFire == GrafixatorConstants.PLAYER_FIRE_VERTICAL) {
            	if (heroSprite.yDir !=0) {
            		bullet.bulletXDir = 0;
            		bullet.bulletYDir = heroSprite.yDir; 
            	}
            }
            else if (playerFire == GrafixatorConstants.PLAYER_FIRE_HORIZONTAL) {
            	if (heroSprite.xDir !=0) {
            		bullet.bulletXDir = heroSprite.xDir; 
            		bullet.bulletYDir = 0; 
            	}
            }
            else {
                bullet.bulletXDir = heroSprite.xDir;
                bullet.bulletYDir = heroSprite.yDir;    
            }


			if (charMovement == GrafixatorConstants.CHARACTER_SPACESHIP) {
				float angle = heroSprite.rotation;
				angle+=90;
				angle=(float) Math.toRadians(angle);
				bullet.bulletXDir = (float)  Math.cos(angle);
				bullet.bulletYDir = (float)  Math.sin(angle);        
			}
			
            if (playerBulletPointLight !=null) {                    	 
            	bullet.pointLight = new PointLight(rayHandler, playerBulletPointLight.getRayNum(),  playerBulletPointLight.getColor(),  playerBulletPointLight.getDistance(), bullet.bulletXPos + bullet.width/2, bullet.bulletYPos + bullet.height/2);
            }


			bulletsList.add(bullet);    
			return true;           
		}        

		return false;
	}

	public void update(float deltaTime, OrthographicCamera camera) {
		tweenManager.update(Gdx.graphics.getDeltaTime());
		
		if (charFullSquare && (movementStatus == GrafixatorConstants.GAME_STATUS_MOVING || movementStatus == GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE)) {
			pixelsMoved+=charSpeed;
			heroSprite.xPos += heroSprite.xDir * charSpeed;
			heroSprite.yPos += heroSprite.yDir * charSpeed;

			if (heroSprite.xDir !=0 && pixelsMoved > 0 &&  pixelsMoved >= tileWidth) {
				if (charMoveToBoundary) {
					pixelsMoved=0;
					if (!canMove(heroSprite, heroSprite.xDir, 0)) {
						movementStatus=GrafixatorConstants.GAME_STATUS_STATIC;
					}
					else {
						movementStatus=GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE; 
					}
				}
				else {
					movementStatus = GrafixatorConstants.GAME_STATUS_STATIC; pixelsMoved=0;
				}
			}
			else if (heroSprite.yDir !=0 && pixelsMoved > 0 && pixelsMoved >= tileHeight) {
				if (charMoveToBoundary) {
					pixelsMoved=0;
					if (!canMove(heroSprite, 0, heroSprite.yDir)) {
						movementStatus=GrafixatorConstants.GAME_STATUS_STATIC;
					}
					else {
						movementStatus=GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE;  
					}
				}
				else {
					movementStatus = GrafixatorConstants.GAME_STATUS_STATIC; pixelsMoved=0;
				}

			}
			else {
				if (movementStatus==GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE) movementStatus=GrafixatorConstants.GAME_STATUS_MOVING;
			}                            
		}
		
		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);  
		

    	if (lastCamXPos != camera.position.x || lastCamYPos != camera.position.y) {
		   backgroundManager.updateBackgrounds(camera.position.x - lastCamXPos, camera.position.y - lastCamYPos, lastCamXPos > camera.position.x, lastCamYPos > camera.position.y);
	    }
    	
		lastCamXPos = camera.position.x;
		lastCamYPos = camera.position.y;
	}

	public void addEnemyBullet(int xPos, int yPos,int width, int height,  int textureNumber, int type,  int speed, float angle) {

		GrafixatorBullet bullet = bulletPool.obtain();
        
        bullet.texture = bulletTextureEnemy[textureNumber];
		if (bullet.texture==null) return;
		
        bullet.init(false, bulletSpeed,  bullet.texture, 
        		xPos + width/2  - bullet.texture.getRegionWidth()/2, 
        		yPos + height/2 - bullet.texture.getRegionHeight()/2);
                
        bullet.status = GrafixatorBullet.BULLET_STATUS_ACTIVE;
		
		
		bullet.fireByPlayer=false;
		bullet.speed=speed;
		bullet.texture = bulletTextureEnemy[textureNumber];;
		bullet.rotation = angle;

		if (type == GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TYPE_DOWN)       { bullet.bulletYDir=-1;  bullet.bulletXDir=0;  }
		else if (type == GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TYPE_UP)    { bullet.bulletYDir=1;   bullet.bulletXDir=0;  }
		else if (type == GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TYPE_RIGHT) { bullet.bulletXDir=1;   bullet.bulletYDir=0;  }
		else if (type == GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TYPE_LEFT)  { bullet.bulletXDir=-1;  bullet.bulletYDir=0;  }
		else if (type == GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TYPE_PLAYER)  { 

			if (heroSprite != null) {
				int r = -80 + rand.nextInt(160);   // Make it a bit random, so bullets don't always go directly to player.
				float bulletAngle = (float)  Math.atan2(yPos - heroSprite.yPos + r, xPos - heroSprite.xPos + r);
				bulletAngle += Math.PI;
				bullet.bulletXDir = (float)  Math.cos(bulletAngle);
				bullet.bulletYDir = (float)  Math.sin(bulletAngle);  
				bullet.rotation = (float) Math.toDegrees(bulletAngle) - 90;

			}
			else return;
		}
		else if (type == GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TYPE_BOSS)  { 
			bullet.bulletXDir = (float)  Math.cos(Math.toRadians(bullet.rotation-90));
			bullet.bulletYDir = (float)  Math.sin(Math.toRadians(bullet.rotation-90));  
		}

		if (bullet.texture==null) return;
		bullet.width =bullet.texture.getRegionWidth();
		bullet.height=bullet.texture.getRegionHeight();

		bullet.bulletXPos = xPos + width/2  - bullet.texture.getRegionWidth()/2;
		bullet.bulletYPos = yPos + (height/2 - bullet.texture.getRegionHeight()/2);

		bulletsList.add(bullet);

	}
	
	public void handleCameraMovement(OrthographicCamera camera) {
	       if (cameraControl == GrafixatorConstants.CAMERA_CONTROL_HORIZONTAL) {
	        	if (cameraAutoStop) {
	        		if (cameraSpeed > 0 && camera.position.x < cameraHorizStopPosition || cameraSpeed < 0 && camera.position.x > cameraHorizStopPosition) {
	        			camera.position.x+=cameraSpeed;
	        		}
	        	}
	        	else {
	        		 camera.position.x+=cameraSpeed;
	        	}
	            
	            
	            if (charMovement == GrafixatorConstants.CHARACTER_STANDARD && heroSprite !=null) {
	            	heroSprite.xPos+=cameraSpeed;
	            }
	        }  
	        else if (cameraControl == GrafixatorConstants.CAMERA_CONTROL_VERTICAL) {
	        	if (cameraAutoStop) {
	        		if (cameraSpeed > 0 && camera.position.y < cameraVertStopPosition || cameraSpeed < 0 && camera.position.y > cameraVertStopPosition) {
	        			camera.position.y+=cameraSpeed;
	        		}
	        	}
	        	else {
	        		camera.position.y+=cameraSpeed;
	       	    }
	            
	            if (charMovement == GrafixatorConstants.CHARACTER_STANDARD && heroSprite !=null) {
	            	heroSprite.yPos+=cameraSpeed;
	             }
	        }  
	        else if (cameraControl == GrafixatorConstants.CAMERA_CONTROL_PLAYER_CENTERED ) {
	        	if (!isPlayerBeingDragged && heroSprite !=null) {
	        		 playerPosition.set(heroSprite.xPos, heroSprite.yPos, 0);
	                 camera.position.lerp(playerPosition, 0.4f);
	        	}
	        }	    
	        else if (heroSprite !=null && cameraControl == GrafixatorConstants.CAMERA_CONTROL_VIEWPORT) {

	            if (heroSprite.xPos < camera.position.x) {
	                if (Math.abs(heroSprite.xPos - camera.position.x)   > (Gdx.graphics.getWidth() * cameraViewHoriz/ 200)) {
	                    if (charMovement == GrafixatorConstants.CHARACTER_STANDARD) {
	                    	camera.position.x-=charSpeed;
	                    }
	                    else if (charMovement == GrafixatorConstants.CHARACTER_PLATFORMER) {
	                    	camera.position.x= heroSprite.xPos + (Gdx.graphics.getWidth() * cameraViewHoriz/ 200);
	                    }
	                }
	            }
	            else if (heroSprite.xPos > camera.position.x) {
	                if (Math.abs(heroSprite.xPos - camera.position.x)   >( Gdx.graphics.getWidth() * cameraViewHoriz/ 200)) {
	                    if (charMovement == GrafixatorConstants.CHARACTER_STANDARD) {
	                    	camera.position.x+=charSpeed;
	                    }
	                    else if (charMovement == GrafixatorConstants.CHARACTER_PLATFORMER) {
	                    	camera.position.x= heroSprite.xPos - (Gdx.graphics.getWidth() * cameraViewHoriz/ 200);
	                    }                    

	                }
	            }

	            if (heroSprite.yPos < camera.position.y) {
	                if (Math.abs(heroSprite.yPos - camera.position.y)   > (Gdx.graphics.getHeight() * cameraViewVert/ 200)) {
	                    camera.position.y-=charSpeed;
	                }
	            }
	            else if (heroSprite.yPos > camera.position.y) {
	                if (Math.abs(heroSprite.yPos - camera.position.y)   > (Gdx.graphics.getHeight() * cameraViewVert/ 200)) {
	                	camera.position.y+=charSpeed;
	                }
	            }
	            
	        }

	}


	public void populateCollisionRectangles(boolean populateBox2dCollisions, boolean populateRectangles) {

		if (mainLayer == null) return;  // A grafixator file may not have a main collision layer.

		// Populate all the box2d objects using contour tracing..
		int array[][] = GrafixatorUtils.populateStaticTilesArray(mainLayer);
		int done[][] = array;


		for (int h=0; h < mainLayer.getHeight();  h++) {
			for (int w=0; w< mainLayer.getWidth(); w++) {           

				if (array[h][w] !=0 && done[h][w] != GrafixatorConstants.BODY_CREATED) {
					int noDown =GrafixatorUtils.howManySq(w, h, GrafixatorConstants.DOWN, array, done, false);
					int noRight=GrafixatorUtils.howManySq(w, h, GrafixatorConstants.RIGHT, array, done, false);

					int height;
					int width;


					float box2DXPos=1;
					float box2DYPos=1;

					if (noRight>=noDown) {
						GrafixatorUtils.howManySq(w, h, GrafixatorConstants.RIGHT, array, done, true);
						width=noRight * tileWidth;
						height = tileHeight;

						if (noRight %2 == 0) box2DXPos =  w  + 0.5f + noRight/2;
						else                 box2DXPos =  w  + (noRight + 1) / 2; 

						box2DYPos =    h;                               
					}
					else {
						GrafixatorUtils.howManySq(w, h, GrafixatorConstants.DOWN, array, done, true);
						width  = tileWidth;
						height = noDown * tileHeight;
						box2DXPos =   w + 1; 

						if (noDown %2 == 0) box2DYPos =  h  + (noDown)/2  - 0.5f;
						else                box2DYPos =  h  + (noDown)/2;

					}
					box2DYPos+=0.5f;
					box2DXPos-=0.5f;


					float boxX = (float) width / tileWidth / 2;
					float boxY = (float) height / tileHeight / 2;

					if (populateBox2dCollisions) {
						PolygonShape polyShape = new PolygonShape();
						polyShape.setAsBox(boxX, boxY);


						FixtureDef fixtureDef = new FixtureDef();
						fixtureDef.shape = polyShape;
						fixtureDef.density     = worldDensity;
						fixtureDef.friction    = worldFriction;
						fixtureDef.restitution = worldRestitution;

						BodyDef bodyDef = new BodyDef();
						bodyDef.type = BodyDef.BodyType.StaticBody;

						bodyDef.position.set(box2DXPos,box2DYPos);


						Body body = world.createBody(bodyDef);
						body.createFixture(fixtureDef);
						polyShape.dispose();
					}

					if (populateRectangles) {
						Rectangle rect = new Rectangle(w * tileWidth, h * tileHeight, boxX * 2 * tileWidth, boxY * tileHeight * 2);
						collisionRectangles.add(rect);
					}
				}   // End of if... 
			}
		}   
		
		// Populate all the Grafixator Objects and convert them to Box2d Shapes for Box2d Collision.
		if (populateBox2dCollisions) {
			
			for (GrafixatorObject go: collisionObjectsList) {
				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyDef.BodyType.StaticBody;
				
				if (go.shapeType == GrafixatorConstants.SHAPE_TYPE_RECTANGLE) {
					   bodyDef.position.set((go.origX + go.width/2)/tileWidth, (go.origY + go.height/2)/tileHeight);
				}
				
				Body body = world.createBody(bodyDef);

				if (go.shapeType == GrafixatorConstants.SHAPE_TYPE_PATH || go.shapeType == GrafixatorConstants.SHAPE_TYPE_TRIANGLE) {
					ChainShape chainShape = new ChainShape();

					int arraySize = go.vertices.size();
					
					if (go.shapeType == GrafixatorConstants.SHAPE_TYPE_TRIANGLE) arraySize++;  // Add another vertice to close off triangle
					
					Vector2[] worldVertices = new Vector2[arraySize];

					for (int i=0; i<go.vertices.size(); i++) {
						worldVertices[i] = new Vector2(go.vertices.get(i).x /tileWidth,  go.vertices.get(i).y / tileHeight);
					}
					
					if (go.shapeType == GrafixatorConstants.SHAPE_TYPE_TRIANGLE) {   // Close off triangle
						worldVertices[3] = new Vector2(go.vertices.get(0).x / tileWidth,  go.vertices.get(0).y /tileHeight);
					}

					chainShape.createChain(worldVertices);
					body.createFixture(chainShape,1.0f);
					chainShape.dispose();
				}
				else if (go.shapeType == GrafixatorConstants.SHAPE_TYPE_CIRCLE) {
					CircleShape circleShape = new CircleShape();
					circleShape.setPosition(new Vector2(go.origX/tileWidth, go.origY/tileHeight));
					
					circleShape.setRadius(go.radius/tileHeight);
					body.createFixture(circleShape,1.0f);
					circleShape.dispose();
				}
				else if (go.shapeType == GrafixatorConstants.SHAPE_TYPE_RECTANGLE) {
					PolygonShape polyShape = new PolygonShape();
					polyShape.setAsBox(go.width/tileWidth/2, go.height/tileHeight/2);
					
					body.createFixture(polyShape, 1.0f);
					polyShape.dispose();
					
				}
			}
			
		}  // End of if PopulateBox2dCollisions
	}

	public void checkGameKeyPresses(OrthographicCamera camera) {
		if (Gdx.input.isKeyPressed(Keys.ENTER))  { 
			fireBullet(); 
		}

		if (Gdx.input.isKeyPressed(Keys.LEFT))  { camera.position.x-=10; }
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) { camera.position.x+=10; }
		if (Gdx.input.isKeyPressed(Keys.UP))    { camera.position.y+=10; }
		if (Gdx.input.isKeyPressed(Keys.DOWN))  { camera.position.y-=10; }

		if (charMovement == GrafixatorConstants.CHARACTER_STANDARD) {
			checkKeyPressesStandardGame(Gdx.input.isKeyPressed(Keys.A), Gdx.input.isKeyPressed(Keys.D), Gdx.input.isKeyPressed(Keys.W), Gdx.input.isKeyPressed(Keys.S));
			
		}
		else if (charMovement == GrafixatorConstants.CHARACTER_SPACESHIP) {
			checkKeyPressesSpaceShipGame(Gdx.input.isKeyPressed(Keys.LEFT), Gdx.input.isKeyPressed(Keys.RIGHT), Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT));
		}
		else if (charMovement == GrafixatorConstants.CHARACTER_JUMP_UPWARDS || charMovement == GrafixatorConstants.CHARACTER_CATAPULT) {
			if (Gdx.input.justTouched()) {
				handleTouchDown(Gdx.input.getX(), Gdx.input.getY(), true);
			}
		}

	}   
	
	public void checkKeyPressesPlatformGame(boolean moveLeft, boolean moveRight, boolean doJump) {

		if (doJump)  {  

			if (Math.abs(mainCharBody.getLinearVelocity().x) < 0.5) {   // If character is going very slow.  Jump upwards,  otherwise use its linear velocity.
				mainCharBody.setLinearVelocity(0, charJumpVerticalForce);
			}
			else {
				mainCharBody.setLinearVelocity((heroSprite.xDir * charJumpHorizontalForce), charJumpVerticalForce);
			}
			isGrounded=false;
			noJumps++;
		}

		if (moveLeft)  {  

			if (isGrounded) {
				mainCharBody.setLinearVelocity(-charLinearVelocity, 0);
			}
			else {   // Allow moving in the air during jumping..
				mainCharBody.setLinearVelocity(-charLinearVelocity, mainCharBody.getLinearVelocity().y);
			}
			heroSprite.xDir=-1;
		}
		if (moveRight)  { 
			if (isGrounded) {
				mainCharBody.setLinearVelocity(charLinearVelocity, 0);
			}
			else {   // Allow moving in the air during jumping..
				mainCharBody.setLinearVelocity(charLinearVelocity, mainCharBody.getLinearVelocity().y);
			}
			heroSprite.xDir=1;
		} 

	}
	
 public void checkKeyPressesStandardGame(boolean moveLeft, boolean moveRight, boolean moveUp, boolean moveDown) {
		if (moveLeft && canMove(heroSprite, -1, 0))  {
			if (charFullSquare) {

				if (movementStatus == GrafixatorConstants.GAME_STATUS_STATIC || movementStatus==GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE) {

					movementStatus  = GrafixatorConstants.GAME_STATUS_MOVING; pixelsMoved = 0;
					if (charRotateToDir) {heroSprite.rotation=270;}
					heroSprite.xDir=-1; heroSprite.yDir=0;
				}
			}
			else {
				heroSprite.xPos-=charSpeed;  
				if (charRotateToDir) {heroSprite.rotation=270;}
				heroSprite.xDir=-1; heroSprite.yDir=0;
			}

		}
		else if (moveRight && canMove(heroSprite,  1, 0))  {
			if (charFullSquare) {
				if (movementStatus == GrafixatorConstants.GAME_STATUS_STATIC || movementStatus==GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE) {
					movementStatus  = GrafixatorConstants.GAME_STATUS_MOVING; pixelsMoved = 0;
					if (charRotateToDir) {heroSprite.rotation=90;}
					heroSprite.xDir=1; heroSprite.yDir=0;

				}
			}
			else {
				heroSprite.xPos+=charSpeed;
				if (charRotateToDir) {heroSprite.rotation=90;}
				heroSprite.xDir=1; heroSprite.yDir=0;

			}
		}

		if (moveUp && canMove(heroSprite,  0, 1))  {
			if (charFullSquare) {
				if (movementStatus == GrafixatorConstants.GAME_STATUS_STATIC || movementStatus==GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE) {
					movementStatus  = GrafixatorConstants.GAME_STATUS_MOVING; pixelsMoved = 0;
					if (charRotateToDir) {heroSprite.rotation=0;}
					heroSprite.xDir=0; heroSprite.yDir=1;

				}
			}
			else {
				heroSprite.yPos+=charSpeed;
				if (charRotateToDir) {heroSprite.rotation=0;}
				heroSprite.xDir=0; heroSprite.yDir=1;

			}

		}
		else if (moveDown && canMove(heroSprite,  0, -1))  { 
			if (charFullSquare) {                   
				if (movementStatus == GrafixatorConstants.GAME_STATUS_STATIC || movementStatus==GrafixatorConstants.GAME_STATUS_MOVING_FULL_SQUARE) {
					movementStatus  = GrafixatorConstants.GAME_STATUS_MOVING; pixelsMoved = 0;
					if (charRotateToDir) {heroSprite.rotation=180;}
					heroSprite.xDir=0; heroSprite.yDir=-1;
				}
			}
			else {
				heroSprite.yPos-=charSpeed;
				if (charRotateToDir) {heroSprite.rotation=180;}
				heroSprite.xDir=0; heroSprite.yDir=-1;
			}
		}
	}

	
	
	public void checkKeyPressesSpaceShipGame(boolean leftPressed, boolean rightPressed, boolean thrust) {
		if (heroSprite == null) return;
		
		if (leftPressed)  {  
			heroSprite.rotation+=5F;
		}
		if (rightPressed)  { 
			heroSprite.rotation-=5F;
		}

		if (thrust)  { 
			mainCharBody.setFixedRotation(true);
			float angle = heroSprite.rotation;
			angle-=90;
			angle=(float) Math.toRadians(angle);
			float forceX = (float)  Math.cos(angle);
			float forceY = (float)  Math.sin(angle); 

            forceX = -charThrustForce * forceX;                
            forceY = -charThrustForce * forceY;              
            
            mainCharBody.applyForceToCenter(forceX, forceY, true);
		}
	}

	public boolean canMove(GrafixatorSprite sprite, int xDir, int yDir) {

		for (Rectangle rect: collisionRectangles) {
			if (GrafixatorUtils.overlapRectangles(rect.x, rect.y, rect.width, rect.height, 
					sprite.xPos + (xDir * charSpeed), 
					sprite.yPos + (yDir * charSpeed), sprite.width, sprite.height)) {
				return false;
			}

		}

		return true;
	}
	
	public float getSpriteXPosFromBox2DBody(GrafixatorSprite sprite) {
	    return sprite.xPos=(tileWidth * sprite.body.getPosition().x) - sprite.width/2;    
	}
	
	public float getSpriteYPosFromBox2DBody(GrafixatorSprite sprite) {
	    if (sprite.isAnimation) return (tileHeight * sprite.body.getPosition().y) - sprite.height/2; 
	    else return (tileHeight * sprite.body.getPosition().y) - sprite.height/2;
	}
	
	public void handleTouchDown(float x, float y, boolean isTouchDown) {
		if (mainCharBody !=null) {

			if (charMovement == GrafixatorConstants.CHARACTER_CATAPULT) {
				if (!isPlayerBeingDragged && isTouchDown
						&& GrafixatorUtils.overlapRectangles(x, y, 1, 1, heroSprite.xPos, heroSprite.yPos,heroSprite.width, heroSprite.height)) {   // Add another condition to check player has been clicked on.

					isPlayerBeingDragged=true;
					origPlayerXPos=heroSprite.xPos;
					origPlayerYPos=heroSprite.yPos;
					mainCharBody.setLinearVelocity(0f, 0f);
					mainCharBody.setAngularVelocity(0);

					xOffset = x % origPlayerXPos;
					yOffset = y % origPlayerYPos;
				}
				else {
					float scaleDownFactor=0.1f;

					if (isPlayerBeingDragged) {

						float moveX = origPlayerXPos - heroSprite.xPos;
						float moveY = origPlayerYPos - heroSprite.yPos;
						mainCharBody.applyForceToCenter(moveX/scaleDownFactor, moveY/scaleDownFactor, true);
					}

					isPlayerBeingDragged=false;
				}

			}
			else if (charMovement == GrafixatorConstants.CHARACTER_JUMP_UPWARDS) {
				mainCharBody.setLinearVelocity(0, charUpwardsForce);
			}
			mainCharBody.applyForce(mainCharBody.getLocalCenter().x, mainCharBody.getLocalCenter().y, mainCharBody.getLocalCenter().x, -3400, true);
		}

	}
	
	public void handleCatapultDrag(float x, float y) {	   
		if (isPlayerBeingDragged && heroSprite != null) {
			heroSprite.xPos=x - xOffset;
			heroSprite.yPos=y - yOffset; 
		}
	}

}
