package com.grafixator.managers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.grafixator.model.GrafixatorParticle;


public class ParticleEffectManager {

    
    private static ParticleEffectManager instance = null;
    private Random rand = new Random();
    private long lastCleanUp = System.currentTimeMillis();
    private static final int CLEAN_UP_TIME=3000; // Clean up every 3 seconds.
    
    private Vector3  particlePositionVector = new Vector3();   // Used for checking if Particle is offscreen (in which case removed).
    
    public List<GrafixatorParticle> particleList = new ArrayList<GrafixatorParticle>();

	private final Pool<GrafixatorParticle> particlePool = new Pool<GrafixatorParticle>() {
		@Override
		protected GrafixatorParticle newObject() {
			return new GrafixatorParticle();
		}
	};

    public Map<String, GrafixatorParticle> userCreatedEffects = new HashMap<String, GrafixatorParticle>();   // All the User Created Particle Effects.  We will start off with 2 default ones.  A general one and one for pickups.
    
    public static ParticleEffectManager getInstance() {
        if (instance == null) {
            instance = new ParticleEffectManager();
        }

        return instance;
    }
    
    public void addEffect(Sprite sprite, int effectType, float x, float y, int duration, int numberOfParticles, float speed, float gravity) {

	    if (effectType == GrafixatorParticle.EFFECT_TYPE_EXPLOSION) {
	        for(int i = 0; i < numberOfParticles; i++){            
	            int range1 = -10 + rand.nextInt(20);
	            int range2 = -10 + rand.nextInt(20);
	            
	            range1 *= speed;
	            range2 *= speed;
	            
	            GrafixatorParticle particle = particlePool.obtain();
	            particle.status = GrafixatorParticle.STATUS_ACTIVE;
	            particle.init(sprite,  x - sprite.getWidth()/2, y-sprite.getHeight(), (float) range1, (float) range2, gravity, true);
	            particleList.add(particle);
	        }
	    }
	    else if (effectType == GrafixatorParticle.EFFECT_TYPE_PICKUP) {
	        int radius=50 + rand.nextInt(60);
	        for(int i = 0; i < numberOfParticles; i++){
	            gravity=0;
                int range1= 10 * (int) (radius * Math.cos(i));
                int range2= 10 * (int) (radius * Math.sin(i));
	            
                range1 *= speed;
	            range2 *= speed;
	            
	            GrafixatorParticle particle = particlePool.obtain();
	            particle.status = GrafixatorParticle.STATUS_ACTIVE;
	            particle.init(sprite,  x - sprite.getWidth()/2, y-sprite.getHeight(), (float) range1, (float) range2, gravity, false);
	            particleList.add(particle);
	        }
	    }
    }
    
    
    public void renderParticleEffects(Batch batch) {
	    batch.enableBlending();
	    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
	    
        for(GrafixatorParticle p : particleList){     

            if (p.alpha <= 0) continue;
        	p.sprite.setPosition(p.x, p.y);        
            p.sprite.draw(batch);
            p.sprite.setColor(p.sprite.getColor().r, p.sprite.getColor().g, p.sprite.getColor().b,p.alpha);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public void updateParticles(float delta, OrthographicCamera camera) {
        for(GrafixatorParticle p : particleList){
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            p.vy += (-100*p.gravity) * delta;  

            if (p.alpha > 0.3) p.alpha-=0.04f; 
            else if (p.alpha > 0.1) p.alpha-=0.01f; 
            else if (p.alpha > 0.0) p.alpha-=0.005f; 
            else p.alpha=0f;
            
        }
        
        // Clean Up Particles.         
        if ((System.currentTimeMillis() - lastCleanUp) > CLEAN_UP_TIME) {
            Iterator <GrafixatorParticle> i = particleList.iterator();

            while (i.hasNext()) {
                GrafixatorParticle particle = i.next(); 
                particlePositionVector.x = particle.x;
                particlePositionVector.y = particle.y;
                
                if (camera!=null) {
                    if (!camera.frustum.pointInFrustum(particlePositionVector) || particle.alpha<=0f) {
                        i.remove();
                    }
                }
                else {   // This is for the particle effect being shown in the Particle Effecdt Editor.
                    if (particle.x > 3000 || particle.x < 100 || particle.y > 5000 || particle.y < -5000) {
                        i.remove();
                    }
                }
            }
            lastCleanUp = System.currentTimeMillis();

        }
        
    } 
    
    public void clearParticleList() {
        particleList.clear();
    }
    
}

