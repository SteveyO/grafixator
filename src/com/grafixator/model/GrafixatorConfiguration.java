package com.grafixator.model;

/**
 * 
 * @author SteveyO
 * 
 * GrafixatorConfiguration is used when loading in a grafixator game.  One of its nice features is the ability to load in different tilesets (other than the filename specified in the Grafixator XML save file).
 * This allows you to load in different tilesets for different screen resolutions.  External file locations are supported also (in case graphics resources need to be loaded outside your apk).
 *
 */
public class GrafixatorConfiguration {
	private boolean useBox2d          = true;
	private boolean useBox2dLights    = true;
	private boolean useParticleEngine = true;
	private boolean useExternalFileLocation = false;
	private String  spriteSheetName;    // Only use this if you want to use a different SpriteSheet than the one in your Grafixator File (i.e. useful for providing different graphics for different mobile screen resolutions).
	private int  tileWidth;          // use only for different spritesheets (see above comment).
	private int  tileHeight;         // use only for different spritesheets (see above comment).


	public boolean isUseExternalFileLocation() {
		return useExternalFileLocation;
	}
	public void setUseExternalFileLocation(boolean useExternalFileLocation) {
		this.useExternalFileLocation = useExternalFileLocation;
	}
	public String getSpriteSheetName() {
		return spriteSheetName;
	}
	public void setSpriteSheetName(String spriteSheetName) {
		this.spriteSheetName = spriteSheetName;
	}
	public int getTileWidth() {
		return tileWidth;
	}
	public void setTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;
	}
	public int getTileHeight() {
		return tileHeight;
	}
	public void setTileHeight(int tileHeight) {
		this.tileHeight = tileHeight;
	}
	public boolean isUseBox2d() {
		return useBox2d;
	}
	public void setUseBox2d(boolean useBox2d) {
		this.useBox2d = useBox2d;
	}
	public boolean isUseBox2dLights() {
		return useBox2dLights;
	}
	public void setUseBox2dLights(boolean useBox2dLights) {
		this.useBox2dLights = useBox2dLights;
	}
	public boolean isUseParticleEngine() {
		return useParticleEngine;
	}
	public void setUseParticleEngine(boolean useParticleEngine) {
		this.useParticleEngine = useParticleEngine;
	}
}
