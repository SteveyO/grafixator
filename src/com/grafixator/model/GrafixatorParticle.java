package com.grafixator.model;


import java.util.Random;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Pool.Poolable;

public class GrafixatorParticle  implements Poolable {
    public String particleName;
    public String effectType;
    public float x;
    public float y;
    public float vx;
    public float vy;
    public Sprite sprite;
    public float gravity=300;
    public int numberOfParticles;
    public float alpha=1;  
    public int status;
    
    public static int EFFECT_TYPE_EXPLOSION = 1;
    public static int EFFECT_TYPE_PICKUP    = 2;
    
    public static int STATUS_ACTIVE    = 1;
    public static int STATUS_INACTIVE  = 2;
    
    
    public void init(Sprite sprite, float x, float y, float vx, float vy, float gravity, boolean randomRotation) {
        this.sprite=new Sprite(sprite);

        
        Random r = new Random();
        if (randomRotation) {          
          this.sprite.setRotation(r.nextInt(360));
        }
        
        this.x=x;
        this.y=y;
        this.vx=vx;
        this.vy=vy;
        this.gravity=gravity;
        this.alpha=1f;
    }
    
    public void setSize(float x, float y) {
        this.sprite.setSize(x, y);
    }
    
	@Override
	public void reset() {
		status=STATUS_INACTIVE;
	}
    
}