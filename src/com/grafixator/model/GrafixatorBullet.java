package com.grafixator.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool.Poolable;

import box2dLight.PointLight;
/**
 * 
 * @author SteveyO
 * 
 * A Grafixator Bullet.  
 * Grafixator Bullets implement Poolable for zero object allocation.  See https://github.com/libgdx/libgdx/wiki/Memory-management for more information.
 * 
 * Grafixator bullets can also have Box2d Lights attached.
 *
 */
public class GrafixatorBullet  implements Poolable {
    public static long lastBulletFiredTime=System.currentTimeMillis();
    
    public static final int BULLET_STATUS_ACTIVE=1;
    public static final int BULLET_STATUS_INACTIVE=2;
    
    public TextureRegion texture;
    public boolean fireByPlayer;
    public float bulletXPos = -300f;
    public float bulletYPos = -300f;
    public float bulletXDir;
    public float bulletYDir;
    public float rotation;
    public int width=30;
    public int height=30;
    public int status=BULLET_STATUS_ACTIVE;
    public int speed;
    
	public PointLight pointLight;
	
	public void init(boolean fireByPlayer, int speed, TextureRegion tr, float xPos, float yPos) {
		this.fireByPlayer = fireByPlayer;
		this.speed        = speed;
		this.texture     = tr;
		this.bulletXPos   = xPos;
		this.bulletYPos   = yPos;
	}
	
	@Override
	public void reset() {
		status=BULLET_STATUS_INACTIVE;
	}
}

