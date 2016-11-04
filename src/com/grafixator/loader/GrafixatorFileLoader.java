package com.grafixator.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.grafixator.GrafixatorConstants;
import com.grafixator.GrafixatorGame;
import com.grafixator.managers.BackgroundManager;
import com.grafixator.managers.TimeLineManager;
import com.grafixator.model.GrafixatorBackground;
import com.grafixator.model.GrafixatorConfiguration;
import com.grafixator.model.GrafixatorObject;
import com.grafixator.model.GrafixatorSprite;
import com.grafixator.model.GrafixatorTimeLine;
import com.grafixator.model.GrafixatorTimeLineEvent;
import com.grafixator.model.ImageSprite;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;


public class GrafixatorFileLoader {

    private TimeLineManager timeLineManager     = TimeLineManager.getInstance();
	int duplicateTilePadding;
	
	
	public static GrafixatorGame load(GrafixatorGame grafixatorGame, String fileName, SpriteBatch batch, String spriteSheetFileName, int tileWidth, int tileHeight, GrafixatorConfiguration gConfig) {
		GrafixatorFileLoader grafixatorLoader = new GrafixatorFileLoader();
		
		grafixatorGame.spriteList.clear();   // Clear out any sprites from previously loaded game.		

		HashMap<String, ArrayList<String[]>> tilePropertieMap = new HashMap<String, ArrayList<String[]>>();
		String spriteSheetName = "";
		String directory = getDirectory(fileName);

		FileHandle fh;
		
		if (gConfig.isUseExternalFileLocation()) {
//			ExternalFileHandleResolver lf = new ExternalFileHandleResolver();
		    fh = Gdx.files.local(fileName);
		  
		}
		else {
			fh = Gdx.files.internal(fileName);
		}
		
		try {
			grafixatorLoader.populateGrafixatorVariables(fh, grafixatorGame, tileWidth, tileHeight);
			tilePropertieMap  = readProperties(fh, grafixatorGame);
			
			if (spriteSheetFileName == null) {  
				spriteSheetName   =  directory + "/" + getSpriteSheetAttribute(fh, spriteSheetName, "imageFile");     
			}
			else {   // This code is if a custom spriteSheetFileName is provided
				spriteSheetName   = spriteSheetFileName; 
			}
			
			grafixatorLoader.populateGameData(batch, grafixatorGame, fh, spriteSheetName, tilePropertieMap);            
			grafixatorLoader.extractSpritesFromTiledMap(grafixatorGame);   // Any tile which has a movement property is removed from the tilemap and rendered separately.
			grafixatorLoader.readTimeLines(fh);
			grafixatorLoader.loadSprites(grafixatorGame, fh, directory);
			grafixatorLoader.loadCollisionObjects(grafixatorGame, fh, directory);
			grafixatorLoader.loadBackgrounds(fh, directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		grafixatorGame.startTimelines();
		
        if (gConfig.isUseBox2dLights()) {
        	grafixatorGame.rayHandler.setBlur(grafixatorGame.box2dLightsBlur);
        	grafixatorGame.rayHandler.setShadows(grafixatorGame.useBox2dShadows);
        	RayHandler.useDiffuseLight(grafixatorGame.diffuseBox2dLight);
          }
		
		return grafixatorGame;
	}
	
	/*
	 * Convenience method that loads in layer (in a Grafixator file) and returns the data in a 2x2 array.
	 * This can be useful for switching level data in game for example. 
	*/
	
	public static int[][] loadLevelData(String fileName, String layerName, boolean isExternal, int cols, int rows) {		
		int levelData[][] = new int[rows][cols];
		
		FileHandle fh=null;
		
		if (isExternal) {
//			ExternalFileHandleResolver lf = new ExternalFileHandleResolver();
			fh = Gdx.files.local(fileName);
			
		}
		else {
			fh = Gdx.files.internal(fileName);
		}
		
		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element;
		try {
			xml_element = xml.parse(fh);
			
			XmlReader.Element allLayers = xml_element.getChildByName("layers");

			Iterator<Element> tilesIterator = allLayers.getChildrenByName("layer").iterator();      

			while(tilesIterator.hasNext()){
				XmlReader.Element layerElement = (XmlReader.Element)tilesIterator.next();

				String currLayerName       = layerElement.getAttribute("name");
				Iterator<Element> iterator_level = layerElement.getChildrenByName("data").iterator();   

				int row=0;
				
				if (currLayerName.equals(layerName)) {

					while(iterator_level.hasNext()){
						XmlReader.Element level_line = iterator_level.next();
						String mapDataText = level_line.getText();
						String tiles[] = mapDataText.split(",");

						for (int col=0; col< tiles.length; col++) {
							if (col < cols && row < rows) {
							    levelData[row][col] = Integer.valueOf(tiles[col]);
							}
						}
						row++;
					}                

				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		return levelData;
	}


	private void populateGameData(SpriteBatch batch, GrafixatorGame grafixatorGame, FileHandle fh , String spriteSheetName, HashMap<String, ArrayList<String[]>> tilePropertieMap) throws IOException {
		TiledMap map;
         
		int padding=(duplicateTilePadding * 2);  // Padding in Grafixator is in pixels per side.  So 1 is 1 pixel to left, right, up and down (hence multiple by 2).

		grafixatorGame.world = new World(new Vector2(0.0f, 0.0f), true);
		grafixatorGame.world.setGravity(new Vector2(0, grafixatorGame.worldGravity));

		// Read Tiles
		Texture tilesTexture;
		tilesTexture = new Texture(Gdx.files.internal(spriteSheetName));

		TextureRegion[][] splitTiles;
		splitTiles     = TextureRegion.split(tilesTexture, grafixatorGame.tileWidth + padding, grafixatorGame.tileHeight + padding);
		grafixatorGame.allTiles = splitTiles;

		map = new TiledMap();
		MapLayers layers = map.getLayers();     

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = xml.parse(fh);

		XmlReader.Element allLayers = xml_element.getChildByName("layers");

		Iterator<Element> tilesIterator = allLayers.getChildrenByName("layer").iterator();      

		while(tilesIterator.hasNext()){
			XmlReader.Element layerElement = (XmlReader.Element)tilesIterator.next();

			String layerName       = layerElement.getAttribute("name");
			List <String>mapDataList = new ArrayList<String>();

			Iterator<Element> iterator_level = layerElement.getChildrenByName("data").iterator();   

			while(iterator_level.hasNext()){
				XmlReader.Element level_line = iterator_level.next();
				String mapDataText = level_line.getText();
				mapDataList.add(mapDataText);
			}                

			TiledMapTileLayer layer;

			layer = new TiledMapTileLayer(grafixatorGame.numberOfColumns, grafixatorGame.numberOfRows,   grafixatorGame.tileWidth,  grafixatorGame.tileWidth);
			layer.setName(layerName);

			if (layerName.equals("Collision Layer")) {
				grafixatorGame.mainLayer = layer;
			}
			populateLayerData(layer, mapDataList, tilePropertieMap, layers, splitTiles, grafixatorGame);
		}


		grafixatorGame.tiledMapRenderer = new OrthogonalTiledMapRenderer(map, batch);	 
		grafixatorGame.tiledMap = map;
	}
	
	public  void readTimeLines(FileHandle fh) throws IOException {

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = xml.parse(fh);

		XmlReader.Element xmlTimeLines = xml_element.getChildByName("timelines");	    
		Iterator<Element> timelineIterator = xmlTimeLines.getChildrenByName("timeline").iterator();		

		
		timeLineManager.gfxTimeLinesList.clear();
		timeLineManager.timeLinesList.clear();

		while(timelineIterator.hasNext()){
			XmlReader.Element timeLineElement = (XmlReader.Element) timelineIterator.next();

			String spriteId    = timeLineElement.getAttribute("spriteId");      
			String spriteIndex = timeLineElement.getAttribute("spriteIndex");   
			String loop = timeLineElement.getAttribute("loop");
			String rotateSprite = timeLineElement.getAttribute("rotateSprite");
			String timeLineSpeed = timeLineElement.getAttribute("speed");

			String xPos = timeLineElement.getAttribute("originXPos");
			String yPos = timeLineElement.getAttribute("originYPos");
			String easingFunction = timeLineElement.getAttribute("easing");
			String width  = timeLineElement.getAttribute("width");
			String height = timeLineElement.getAttribute("height");
			
			GrafixatorTimeLine grafixatorTimeLine = new GrafixatorTimeLine();
			grafixatorTimeLine.easingFunction 	= Integer.valueOf(easingFunction);
			grafixatorTimeLine.xPos 			= Integer.valueOf(xPos);
		    grafixatorTimeLine.yPos 			= Integer.valueOf(yPos);
		    grafixatorTimeLine.loop             = Boolean.valueOf(loop);
		    grafixatorTimeLine.timeLineSpeed    = Float.valueOf(timeLineSpeed);
		    grafixatorTimeLine.spriteId         = Integer.valueOf(spriteId);
		    grafixatorTimeLine.spriteIndex      = Integer.valueOf(spriteIndex);
		    grafixatorTimeLine.width            = Integer.valueOf(width);
		    grafixatorTimeLine.height           = Integer.valueOf(height);
			
			List<GrafixatorTimeLineEvent> timeLineEventsList = new ArrayList<GrafixatorTimeLineEvent>();
			
			XmlReader.Element events = timeLineElement.getChildByName("events");            
			Iterator<Element> event  = events.getChildrenByName("event").iterator();
			while(event.hasNext()){
				XmlReader.Element grafixatorEvent = (XmlReader.Element)event.next();

				float eventXPos	  = Float.valueOf(grafixatorEvent.getAttribute("x"));
				float eventYPos   = Float.valueOf(grafixatorEvent.getAttribute("y"));
				int eventEasing   = Integer.valueOf(grafixatorEvent.getAttribute("eventEasing", "0"));
				float eventDelay  = Float.valueOf(grafixatorEvent.getAttribute("eventDelay", "-1"));
				int noRepeats     = Integer.valueOf(grafixatorEvent.getAttribute("noRepeats",  "-1"));
				float eventTime   = Float.valueOf(grafixatorEvent.getAttribute("time",  "1"));
				
				Vector2 wayPoint = null;
				String wayPointStr = grafixatorEvent.getAttribute("wayPoint",  "");
				if (!wayPointStr.isEmpty() && wayPointStr.contains(",")) {
					String wayPointArray[] = wayPointStr.split(",");
					wayPoint = new Vector2(Float.valueOf(wayPointArray[0]), Float.valueOf(wayPointArray[1]));
					
				}
				
				GrafixatorTimeLineEvent timeLineEvent = new GrafixatorTimeLineEvent();
				timeLineEvent.eventEasingFunction = eventEasing;
				timeLineEvent.eventMoveToX 		 = eventXPos;
				timeLineEvent.eventMoveToY 		 = eventYPos;
				timeLineEvent.noRepeats    		 = noRepeats;
				timeLineEvent.eventTimeLineDelay = -1;
				timeLineEvent.wayPoint 			 = wayPoint;
				timeLineEvent.eventDuration      = eventTime;
				
				timeLineEventsList.add(timeLineEvent);
				
			}  // End of events.
			grafixatorTimeLine.timeLineEvents.addAll(timeLineEventsList);
			
			String key = spriteId + "," + spriteIndex;
 			timeLineManager.gfxTimeLinesList.put(key, grafixatorTimeLine);
			
		}
	}


	public  void loadSprites(GrafixatorGame grafixatorGame, FileHandle fh, String directory) {

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element=null;
		try {
			xml_element = xml.parse(fh);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Element allSprites = xml_element.getChildByName("sprites");

		// Not all games have spritesheets (And the free version for sure).
		if (allSprites==null || allSprites.getChildCount() == 0) return;

		String imageFileName = allSprites.getAttribute("imageFile", "");
		if (imageFileName.isEmpty()) return; // Should'nt happen this.

		String spriteSheetSpritesName = directory + "/" + imageFileName;

		Texture spritesTexture = new Texture(Gdx.files.internal(spriteSheetSpritesName));

		Iterator<Element> spritesIterator = allSprites.getChildrenByName("sprite").iterator();	
		while(spritesIterator.hasNext()){
			XmlReader.Element spriteElement = (Element)spritesIterator.next();
			String id                  = spriteElement.getAttribute("id");
			int spriteWidth            = spriteElement.getInt("width");
			int spriteHeight           = spriteElement.getInt("height");
			String spriteIsAnim        = spriteElement.getAttribute("isAnimation");
			int spriteNoFrames         = spriteElement.getInt("noFrames");
			String spriteSheetPosition = spriteElement.getAttribute("spriteSheetPosition");
			String positions           = spriteElement.getAttribute("positions");

			List<ImageSprite> pointsList= getPoints(positions, spriteWidth, spriteHeight);
			if (spriteIsAnim.equalsIgnoreCase("false")) {
				
				int spriteIndex = 0;
				for (ImageSprite point: pointsList) {
					int coords[] = getCoords(spriteSheetPosition);
					TextureRegion spriteTexture =extractTextureRegion(spritesTexture,  coords[0], coords[1], spriteWidth, spriteHeight);
					MapProperties spriteProperties = getSpriteProperties(spriteElement);
			        int w=(int) point.x/grafixatorGame.tileWidth;
			        int h=(int) point.y/grafixatorGame.tileHeight;	
					if (spriteProperties == null) spriteProperties = new MapProperties();
					addGrafixatorSpriteToGame(grafixatorGame,  null, spriteProperties, Integer.valueOf(id), spriteIndex, w, h, (int) point.x, (int) point.y, spriteWidth ,spriteHeight, false, spriteTexture, false, null);
					spriteIndex++;
				}
			}
			Animation animation=null;
			if (spriteIsAnim.equalsIgnoreCase("true")) {
				
				int spriteIndex = 0;
				for (ImageSprite point: pointsList) {
					TextureRegion spriteTexture =extractTextureRegion(spritesTexture,  (int) point.x, (int) point.y, spriteWidth, spriteHeight);
					MapProperties spriteProperties = getSpriteProperties(spriteElement);
					
					
					boolean isAnimation=false;
					
					isAnimation=true;
					if (animation ==null) {
					  animation=getSpriteAnimation(spriteSheetPosition,  spritesTexture,  spriteWidth,  spriteHeight, spriteNoFrames);
					  System.out.println("ANIMATION IS TRUE : : : : : " + spriteNoFrames + "  texture: " + spriteTexture.getRegionHeight());
					}
					if (spriteProperties == null) spriteProperties = new MapProperties();
					addGrafixatorSpriteToGame(grafixatorGame,  null, spriteProperties,  Integer.valueOf(id), spriteIndex, -1, -1, (int) point.x, (int) point.y, spriteWidth ,spriteHeight, false, spriteTexture, isAnimation, animation);
					spriteIndex++;
				}
			}



			// Check if any of the Bullet Sprite Properties are for Player/Enemy bullet textures.   If so set the texture (these do not have GrafixatorSprite objects)..
			Element propertiesElement2 = spriteElement.getChildByName("properties");

			
			if (propertiesElement2!=null) {
				boolean playerBullet= false;  // To handle special case of properties having playerBullet AND box2d light.
				boolean enemyBullet = false;  // To handle special case of properties having playerBullet AND box2d light.
                int enemyBulletTextureNo=0;   
				
				Iterator<Element> propertiesIterator = propertiesElement2.getChildrenByName("property").iterator();	
				while(propertiesIterator.hasNext()){
					XmlReader.Element propertyElement = (Element)propertiesIterator.next();
					String key          = propertyElement.getAttribute("key");
					String value        = propertyElement.getAttribute("value");
					
					if (key.equals(GrafixatorConstants.PROPERTY_KEY_PLAYER_BULLET)) {		                        
						playerBullet=true;     
					}
					if (key.equals(GrafixatorConstants.PROPERTY_KEY_ENEMY_BULLET)) {		                        
						enemyBullet=true;   
						enemyBulletTextureNo = GrafixatorUtils.getPropertyIntValue(value, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TEXTURE, 0);
					}
				}
				
				Iterator<Element> propertiesIterator2 = propertiesElement2.getChildrenByName("property").iterator();	

				while(propertiesIterator2.hasNext()){
					XmlReader.Element propertyElement = (Element)propertiesIterator2.next();
					String key          = propertyElement.getAttribute("key");
					String value        = propertyElement.getAttribute("value");

					if (key.equals(GrafixatorConstants.PROPERTY_KEY_PLAYER_BULLET)) {
						int coords[] = getCoords(spriteSheetPosition);
						grafixatorGame.bulletPlayerTexture=extractTextureRegion(spritesTexture,  coords[0], coords[1], spriteWidth, spriteHeight);            
					}
					if (key.equals(GrafixatorConstants.PROPERTY_KEY_ENEMY_BULLET)) {	
						int coords[] = getCoords(spriteSheetPosition);
						int textureNo  = GrafixatorUtils.getPropertyIntValue(value, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TEXTURE, 0);  
						grafixatorGame.bulletTextureEnemy[textureNo]=extractTextureRegion(spritesTexture,  coords[0], coords[1], spriteWidth, spriteHeight);                      
					}
					
					if (key.equals(GrafixatorConstants.PROPERTY_KEY_BOX2DLIGHT) && playerBullet) {
						setPlayerBulletBox2dLight(value, grafixatorGame);
					}
					if (key.equals(GrafixatorConstants.PROPERTY_KEY_BOX2DLIGHT) && enemyBullet) {
						setEnemyBulletBox2dLight(value, grafixatorGame, enemyBulletTextureNo);
					}

				}
			}

		}  // End of Sprite loop

	}
	public  void loadCollisionObjects(GrafixatorGame grafixatorGame, FileHandle fh, String directory) {
		
		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element=null;
		try {
			xml_element = xml.parse(fh);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Element allObjects = xml_element.getChildByName("collisionObjects");
		Iterator<Element> objectsIterator = allObjects.getChildrenByName("object").iterator();	
		
		while(objectsIterator.hasNext()){
			XmlReader.Element objectElement = (Element)objectsIterator.next();

			String shapeType       = objectElement.getAttribute("type");

			GrafixatorObject go = new GrafixatorObject();

			if (shapeType.equals("CIRCLE")) {
				go.shapeType = GrafixatorConstants.SHAPE_TYPE_CIRCLE;

				String origX   = objectElement.getAttribute("originX");
				String origY   = objectElement.getAttribute("originY");
				String radius  = objectElement.getAttribute("radius");

				go.origX  = Float.valueOf(origX);
				go.origY  = Float.valueOf(origY);
				go.radius = Float.valueOf(radius);
			}
			else if (shapeType.equals("RECTANGLE")) {
				go.shapeType = GrafixatorConstants.SHAPE_TYPE_RECTANGLE;

				String origX   = objectElement.getAttribute("originX");
				String origY   = objectElement.getAttribute("originY");
				String width   = objectElement.getAttribute("width");
				String height  = objectElement.getAttribute("height");

				go.origX  = Float.valueOf(origX);
				go.origY  = Float.valueOf(origY);
				go.width  = Float.valueOf(width);
				go.height = Float.valueOf(height);
			}
			else if (shapeType.equals("TRIANGLE") || shapeType.equals("PATH")) {
				if (shapeType.equals("TRIANGLE")) go.shapeType  = GrafixatorConstants.SHAPE_TYPE_TRIANGLE;
				else 							   go.shapeType = GrafixatorConstants.SHAPE_TYPE_PATH;

				String vertices  = objectElement.getAttribute("vertices");

				String verticesArray[] = vertices.split(" ");

				List<Vector2> verticesList = new ArrayList<Vector2>();

				for (String vert: verticesArray) {
					String coords[] = vert.split(",");
					Vector2 vec2 = new Vector2(Float.valueOf(coords[0]), Float.valueOf(coords[1]));
					verticesList.add(vec2);
				}

				go.vertices.addAll(verticesList);

			}
			grafixatorGame.collisionObjectsList.add(go);

		}

	}
	
	public void loadBackgrounds(FileHandle fh, String directory ) {
		BackgroundManager bManager = BackgroundManager.getInstance();

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element=null;
		try {
			xml_element = xml.parse(fh);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		XmlReader.Element xmlBackgrounds = xml_element.getChildByName("backgrounds");	 
		
		if (xmlBackgrounds == null) return; // Not every game has a background.
		
		Iterator<Element> backgroundsIterator = xmlBackgrounds.getChildrenByName("background").iterator();		

		
		while(backgroundsIterator.hasNext()) {
			XmlReader.Element spriteElement = (Element) backgroundsIterator.next();
			String fileName = spriteElement.getAttribute("fileName");
			String xPos     = spriteElement.getAttribute("xOrigin");
			String yPos     = spriteElement.getAttribute("yOrigin");
			String repeat_x = spriteElement.getAttribute("repeat-x");
			String repeat_y = spriteElement.getAttribute("repeat-y");
			String speedX   = spriteElement.getAttribute("speedX");
			String speedY   = spriteElement.getAttribute("speedY");

			
			Texture backgroundTexture = new Texture(Gdx.files.internal(directory + "/" + fileName));

			
            GrafixatorBackground gBack = new GrafixatorBackground();
            gBack.fileName = fileName;
            gBack.backgroundTexture = new TextureRegion(backgroundTexture);
            gBack.xPos = Float.valueOf(xPos);
            gBack.yPos = Float.valueOf(yPos);
            gBack.repeatX = Boolean.valueOf(repeat_x);
            gBack.repeatY = Boolean.valueOf(repeat_y);
            gBack.speedX  = Float.valueOf(speedX);
            gBack.speedY  = Float.valueOf(speedY);
            
            gBack.width   = backgroundTexture.getWidth();
            gBack.height  = backgroundTexture.getHeight();
            
            bManager.backgroundList.add(gBack);
  
		}  // End of backgroundsIterator loop
		
	}


	private static Animation getSpriteAnimation(String spriteSheetPosition, Texture spritesTexture, int spriteWidth, int spriteHeight, int spriteNoFrames) {

		List<ImageSprite> animationSpritesList= getPoints(spriteSheetPosition, spriteWidth, spriteHeight);

		TextureRegion spriteFrames[]   = new TextureRegion[spriteNoFrames];

		int frameNumber=0;
		for (ImageSprite framePoint: animationSpritesList) {
			spriteFrames[frameNumber]   = extractTextureRegion(spritesTexture, (int) framePoint.x, (int) framePoint.y, (int)  spriteWidth, (int)  spriteHeight);
			spriteFrames[frameNumber].flip(false,  false);
			frameNumber++;
		}

		Animation animation = new Animation(0.1f,spriteFrames);  

		return animation;

	}

	// Read all the Sprite properties and return them in a MapProperties map.
	private static MapProperties getSpriteProperties(XmlReader.Element spriteElement) {
		Element propertiesElement = spriteElement.getChildByName("properties");

		if (propertiesElement!=null) {
			Iterator<Element> propertiesIterator = propertiesElement.getChildrenByName("property").iterator();	
			MapProperties mapProperties = new MapProperties();

			while(propertiesIterator.hasNext()){
				XmlReader.Element propertyElement = (Element)propertiesIterator.next();
				String key          = propertyElement.getAttribute("key");
				String value        = propertyElement.getAttribute("value");
				mapProperties.put(key, value);
			}

			return mapProperties;
		}

		return null;
	}

	private void extractSpritesFromTiledMap(GrafixatorGame grafixatorGame) {

		if (grafixatorGame.mainLayer == null) return;  // A gfx file may not have a main collision layer.
		
		for (MapLayer layer: grafixatorGame.tiledMap.getLayers()) {
			TiledMapTileLayer currLayer = (TiledMapTileLayer) layer;
			
			for (int h=currLayer.getHeight(); h>= 0; h--) {
				for (int w=0; w< currLayer.getWidth(); w++) {           
					if (currLayer.getCell(w, h) != null) {
						MapProperties currentProperties = currLayer.getCell(w,h).getTile().getProperties();  
						addGrafixatorSpriteToGame(grafixatorGame, currLayer, currentProperties, -1, -1, w, h,  w * grafixatorGame.tileWidth, ( h * grafixatorGame.tileHeight), grafixatorGame.tileWidth, grafixatorGame.tileHeight, true, currLayer.getCell(w, h).getTile().getTextureRegion(), false, null);
					}

				}  // End of Width                  
			}   // End of Height loop   
		}
    
	}

	private static String getDirectory(String fileName) {

		if (fileName.contains("/")) {
			return fileName.substring(0, fileName.lastIndexOf('/'));
		}
		else  {
			return fileName.substring(0, fileName.lastIndexOf('\\'));
		}
	}

	// fileName is used for legacy only,, ie.  if xml filename not found then we construct one from filename.
	public static String getSpriteSheetAttribute(FileHandle fh, String fileName, String attribute)  throws IOException {
		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = xml.parse(fh);

		//  XmlReader.Element xmlSpriteSheet = xml_element.getChildByName("spriteSheet");       

		Element spriteSheetsElement = xml_element.getChildByName("spritesheets");
		String returnAttribute="";

		if (spriteSheetsElement!=null) {
			Iterator<Element> propertiesIterator = spriteSheetsElement.getChildrenByName("spritesheet").iterator(); 

			while(propertiesIterator.hasNext()){
				XmlReader.Element xmlSpriteSheet = (Element)propertiesIterator.next();
				returnAttribute   = xmlSpriteSheet.getAttribute(attribute);
				return returnAttribute;
			}
		}

		return fileName;
	}

	private void populateLayerData(TiledMapTileLayer layer, List<String> mapDataList,  HashMap<String, ArrayList<String[]>> tilePropertieMap, MapLayers layers, TextureRegion[][] splitTiles, GrafixatorGame grafixatorGame) {

		int height = layer.getHeight() - 1;
		int noTiles = (1024 / (grafixatorGame.tileWidth + duplicateTilePadding));

		for (int i=0; i < mapDataList.size();  i++) {
			String currLine = (String) mapDataList.get(i);
			String tiles[] = currLine.split(",");
			int tileYPos=0;

			for (int t=0; t< tiles.length; t++) {

				if (!tiles[t].equals("0")) {					
					Cell cell = new Cell();
					int xPos=-1 + Integer.valueOf(tiles[t]);
					if (xPos>= noTiles) {
						tileYPos=xPos/noTiles;
						xPos=xPos%noTiles;
					}
					else        tileYPos=0;

					if (xPos >= 0) {	
						TextureRegion tr = new TextureRegion(splitTiles[tileYPos][xPos],duplicateTilePadding, duplicateTilePadding, grafixatorGame.tileWidth, grafixatorGame.tileHeight);
						StaticTiledMapTile tile = new StaticTiledMapTile(tr);	
			
						// Add Properties to Tile
				//		tile.getTextureRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
						if (tilePropertieMap.containsKey(tiles[t])) {

							String tileNumber = tiles[t];

							ArrayList<String[]> propList = tilePropertieMap.get(tileNumber);

							for (int p=0; p< propList.size(); p++) {
								String keyValues[] = propList.get(p);
								tile.getProperties().put(keyValues[0], keyValues[1]);             
							}

						}
						tile.setId(Integer.valueOf(tiles[t]));
						cell.setTile(tile);
						layer.setCell(t, height-i, cell);	
					}	
				}				
			}   // tiles.length

		}   // for map	
		layers.add(layer);   
	}		

	private static HashMap<String, ArrayList<String[]>> readProperties(FileHandle fh, GrafixatorGame grafixatorGame) throws IOException {
		HashMap<String, ArrayList<String[]>> tilePropertiesMap = new HashMap<String, ArrayList<String[]>>();

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element = xml.parse(fh);

		XmlReader.Element xmlProperties = xml_element.getChildByName("properties");

		Iterator<Element> tilesIterator = xmlProperties.getChildrenByName("tile").iterator();		

		while(tilesIterator.hasNext()){
			XmlReader.Element tileElement = (XmlReader.Element)tilesIterator.next();
			boolean playerBullet=false;  // To handle special case of properties having playerBullet AND box2d light.
			boolean enemyBullet=false;  // To handle special case of properties having enemyBullet AND box2d light.
            int enemyBulletTextureNo=0;

			Iterator<Element> propertiesIterator = tileElement.getChildrenByName("property").iterator();	
			while(propertiesIterator.hasNext()){
				XmlReader.Element propertyElement = (Element)propertiesIterator.next();
				String key          = propertyElement.getAttribute("key");
				String value        = propertyElement.getAttribute("value");
				
				if (key.equals(GrafixatorConstants.PROPERTY_KEY_PLAYER_BULLET)) {		                        
					playerBullet=true;        
				}
				if (key.equals(GrafixatorConstants.PROPERTY_KEY_ENEMY_BULLET)) {		                        
					enemyBullet=true;     
					enemyBulletTextureNo = GrafixatorUtils.getPropertyIntValue(value, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TEXTURE, 0);
				}
			}

			String tileNumber = tileElement.getAttribute("number");

			Iterator<Element> tileProperties = tileElement.getChildrenByName("property").iterator();
			while(tileProperties.hasNext()){
				XmlReader.Element tileProperty = (XmlReader.Element)tileProperties.next();
				String keyValue[] = new String[2];
				String key = tileProperty.getAttribute("key");
				String value = tileProperty.getAttribute("value");
				keyValue[0] = key;
				keyValue[1] = value;

				addValues(tilePropertiesMap, tileNumber, keyValue);
				
				if (key.equals(GrafixatorConstants.PROPERTY_KEY_BOX2DLIGHT) && playerBullet) {
					setPlayerBulletBox2dLight(value, grafixatorGame);
				}
				
				if (key.equals(GrafixatorConstants.PROPERTY_KEY_BOX2DLIGHT) && enemyBullet) {
					setEnemyBulletBox2dLight(value, grafixatorGame, enemyBulletTextureNo);
				}

				//	 tilePropertiesMap.put(tileNumber, keyValue);   // TODO Use the Tile Value sometime??
			}
		}

		return tilePropertiesMap;
	}

	private static void addValues(HashMap<String, ArrayList<String[]>> tilePropertieMap, String key, String value[]) {
		ArrayList<String[]> tempList = null;
		if (tilePropertieMap.containsKey(key)) {
			tempList = tilePropertieMap.get(key);
			if(tempList == null) {
				tempList = new ArrayList<String[]>();
			}
			tempList.add(value);  
		} else {
			tempList = new ArrayList<String[]>();
			tempList.add(value);               
		}

		tilePropertieMap.put(key,tempList);
	}


	private void populateGrafixatorVariables(FileHandle fh, GrafixatorGame grafixatorGame, int tileWidth, int tileHeight)  {		
		//	if (!fh.file().exists()) return null;

		XmlReader xml = new XmlReader();
		XmlReader.Element xml_element=null;

		try {
			xml_element = xml.parse(fh);
		} catch (IOException e) {
			System.out.println("Error Parsing File: " + e.getMessage() );
		}

		String tw = xml_element.getAttribute("tilewidth");
		String th = xml_element.getAttribute("tileheight");

		String duplicateTilePaddingStr = xml_element.getAttribute("duplicateTilePadding", "0");

		if (tileWidth!=-1) {  // Use a custom provided tileWidth and tileHeight.
			grafixatorGame.tileHeight     = tileHeight;
			grafixatorGame.tileWidth      = tileWidth;	
		}
		else {
			grafixatorGame.tileHeight     = Integer.valueOf(tw);
			grafixatorGame.tileWidth      = Integer.valueOf(th);	
		}

		String noCols = xml_element.getAttribute("noCols");
		String noRows = xml_element.getAttribute("noRows");

		grafixatorGame.numberOfColumns= Integer.parseInt(noCols);
		grafixatorGame.numberOfRows   = Integer.parseInt(noRows);

		String gravity     = xml_element.getAttribute("gravity");
		String density     = xml_element.getAttribute("density");
		String friction    = xml_element.getAttribute("friction");
		String restitution = xml_element.getAttribute("restitution");	

		grafixatorGame.worldGravity     = Float.valueOf(gravity);
		grafixatorGame.worldDensity     = Float.valueOf(density);
		grafixatorGame.worldFriction    = Float.valueOf(friction);
		grafixatorGame.worldRestitution = Float.valueOf(restitution);

		String backgroundR = xml_element.getAttribute("bgR");
		String backgroundG = xml_element.getAttribute("bgG");
		String backgroundB = xml_element.getAttribute("bgB");

		grafixatorGame.backgroundColorR =  Float.valueOf(backgroundR);
		grafixatorGame.backgroundColorG =  Float.valueOf(backgroundG);
		grafixatorGame.backgroundColorB =  Float.valueOf(backgroundB);		
		
		// Particle Effect Settings
		Element settingsElement = xml_element.getChildByName("settings");

		String useParticles         = settingsElement.getAttribute("particleEffects", "true"); 
		String particleGravity      = settingsElement.getAttribute("particleGravity", "-9"); 
		String particleVelocity     = settingsElement.getAttribute("particleVelocity", "5"); 
		String noParticles          = settingsElement.getAttribute("noParticles", "20"); 
		String useBox2dShadows      = settingsElement.getAttribute("box2dLightsShadows", "true"); 
		String diffuseBox2dLight    = settingsElement.getAttribute("box2dDiffuseLight", "false"); 
		String box2dLightsBlur      = settingsElement.getAttribute("box2dLightsBlur",    "false"); 
		
		
		// Variables for Box2d Lights
		Element userDataElement = xml_element.getChildByName("userData");
		if (userDataElement !=null) {
			String userDataString         = userDataElement.getAttribute("data", ""); 
			grafixatorGame.userData = userDataString;
		}
		

		duplicateTilePadding = Integer.valueOf(duplicateTilePaddingStr);

		
		grafixatorGame.useBox2dShadows   = Boolean.parseBoolean(useBox2dShadows);
		grafixatorGame.diffuseBox2dLight = Boolean.parseBoolean(diffuseBox2dLight);
		grafixatorGame.box2dLightsBlur   = Boolean.parseBoolean(box2dLightsBlur);
		grafixatorGame.useParticles      = Boolean.parseBoolean(useParticles);
		grafixatorGame.particleGravity   = Float.valueOf(particleGravity);
		grafixatorGame.particleVelocity  = Float.valueOf(particleVelocity);
		grafixatorGame.noParticles       = Integer.valueOf(noParticles);

	}   // End of readGF   	


	public void addGrafixatorSpriteToGame(GrafixatorGame grafixatorGame,TiledMapTileLayer currLayer, MapProperties currentProperties, int spriteId, int spriteIndex,  int tileXPos, int tileYPos, int xPos, int yPos, 
			int spriteWidth, int spriteHeight, boolean isTile, TextureRegion spriteTextureRegion, boolean isAnimation, Animation animation) {

		if (currentProperties.getValues().hasNext() || timeLineManager.containsTimeLine(spriteId, spriteIndex)) {

			if (isAnimation) {   // Add half the sprite width as Animations can have direction which turn the player.
				xPos += spriteWidth/2;
			}

			Random rand= new Random();

			GrafixatorSprite sprite = new GrafixatorSprite();
			sprite.width=spriteWidth;
			sprite.height=spriteHeight;
			sprite.renderWidth = 1;
			sprite.xPos = xPos;
			sprite.yPos = yPos;              
			
            sprite.origX = tileXPos;
            sprite.origY = tileYPos;  
            
			sprite.status = GrafixatorSprite.SPRITE_STATUS_ACTIVE;

            if (timeLineManager.containsTimeLine(spriteId, spriteIndex)) {
                timeLineManager.addTimeLine(spriteId, spriteIndex, spriteTextureRegion, sprite);
            }
			
			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_PICKUP)) {
				sprite.isPickup=true;
			}

			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_PLAYER_BULLET)) {
				grafixatorGame.bulletPlayerTexture = spriteTextureRegion;
			}
			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_ENEMY_BULLET)) {
				String value = (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_ENEMY_BULLET);
				int textureNo  = GrafixatorUtils.getPropertyIntValue(value, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TEXTURE, 0);
				grafixatorGame.bulletTextureEnemy[textureNo] = spriteTextureRegion;
			}
			
            if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_SPRITE_MOVEMENT)) {
            	String propValue = (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_SPRITE_MOVEMENT);
            	sprite.spriteMovementType = GrafixatorUtils.getPropertyIntValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_MOVEMENT,0);
            	sprite.spriteSpeed        = GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_SPEED,0);
            	
            	int initDir = GrafixatorUtils.getPropertyIntValue(propValue, GrafixatorConstants.PROPERTY_VALUE_INIT_DIR,0);
            	if (initDir == 0) initDir = -1;   // Values are 0 (Left or Up) or 1 (Right or Down),  so if 0 set to -1;
            	
            	if (sprite.spriteMovementType == GrafixatorConstants.SPRITE_MOVEMENT_HORIZONTAL) {
            		sprite.xDir = initDir;
            	}
            	else if (sprite.spriteMovementType == GrafixatorConstants.SPRITE_MOVEMENT_VERTICAL) {
            		sprite.yDir = -initDir;
            	}
            }


			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_ROTATE)) {
				sprite.rotation=0;;
				sprite.rotationSpeed=4;// TODO get from properties
				sprite.rotationDirection=1;
				sprite.rotating=true;   

				String propValue = (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_ROTATE);


				if (propValue.equals(GrafixatorConstants.PROPERTY_VALUE_NO_VALUE)) {
					sprite.rotationSpeed=4; // 4 is a sensible default
				}
				else {
					sprite.rotationSpeed= GrafixatorUtils.getPropertyFloatValue(propValue,   GrafixatorConstants.PROPERTY_VALUE_SPRITE_ROTATION_SPEED, 4);
					boolean rotateCW    = GrafixatorUtils.getPropertyBooleanValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_ROTATE_CW,   true);
					if (rotateCW) sprite.rotationDirection=1;
					else          sprite.rotationDirection=-1;
				}
				
                boolean fullRotation = GrafixatorUtils.getPropertyBooleanValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_FULL_ROTATION, true);
                if (!fullRotation) {
                	sprite.fullRotation=false;
                	sprite.rotationStartAngle = GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_ROTATION_START_ANGLE, 0);
                	sprite.rotationEndAngle   = GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_ROTATION_END_ANGLE, 0);
                	
                	if (sprite.rotationDirection == -1) {  // If sprite is set to move clockwise, then set its initial direction to the Start Angle, otherwise End Angle
                		sprite.rotation = 360 - sprite.rotationStartAngle;
                	}
                	else {
                		sprite.rotation = 360 - sprite.rotationEndAngle;
                	}
                }
                else {
                	sprite.fullRotation=true;
                }

			}
			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_DESTRUCTIBLE)) {
				sprite.isDesctructible=true;;

				String propValue = (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_DESTRUCTIBLE);
				if (propValue.equals(GrafixatorConstants.PROPERTY_VALUE_NO_VALUE)) {
					sprite.hitsNeededToDestroy=1;
				}
				else {
					sprite.hitsNeededToDestroy = GrafixatorUtils.getPropertyIntValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_HITS_TO_DESTROY, 1);
					sprite.effectName          = GrafixatorUtils.getPropertyStringValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_EFFECTS);
				}

			}
			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_SHRINK_EXPAND)) {
				sprite.shrinkExpanding=true;
				sprite.spriteDecreasing=true;
				sprite.shrinkExpandSpeed=0.05f;     
			}

			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_ENEMY_FIRE)) {
				String propValue = (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_ENEMY_FIRE);
				sprite.enemyFire=true;
				sprite.enemyFireDirection = GrafixatorUtils.getPropertyIntValue(propValue, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE, 4);
			
				sprite.nextFireTime = System.currentTimeMillis() + 1000 + rand.nextInt(1000); 
				sprite.enemyFireSpeed=GrafixatorUtils.getPropertyIntValue(propValue, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_SPEED, 4);
				sprite.enemyFireInterval= GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_INTERVAL, 2);
				sprite.bulletTextureNo = GrafixatorUtils.getPropertyIntValue(propValue, GrafixatorConstants.PROPERTY_VALUE_ENEMY_FIRE_TEXTURE, 0);
			}


			if (isTile) {
				sprite.spriteTexture = spriteTextureRegion;
			}
			else {
				if (isAnimation) {
					sprite.spriteAnimation=animation; 
					sprite.isAnimation=true;
					sprite.spriteTexture=animation.getKeyFrames()[0];   // Get first frame of animation (this is uses in particle explosions). 
					sprite.xDir=1;         
				}
				else {
					sprite.spriteTexture = spriteTextureRegion;
				}
			}

			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_BOX2DLIGHT)) {
                	String propValue = (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_BOX2DLIGHT);

                	String   type    			= GrafixatorUtils.getPropertyStringValue(propValue, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_TYPE);
                	String   colorString    	= GrafixatorUtils.getPropertyStringValue(propValue, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_COLOR);
                	Color rayColor   			= GrafixatorUtils.getColorFromString(colorString, Color.RED);
                	int   noRays     			= GrafixatorUtils.getPropertyIntValue(propValue, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_NO_RAYS, 16);
                	Float lightDistance	 		= GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_DISTANCE, sprite.width*2);
                	Float coneAngle   			= GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_CONE_ANGLE,  90);
                	Float directionAngle    	= GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_DIRECTION_ANGLE,  90);
                	if (type.equals("POINT")) {
                		PointLight pl =  new PointLight(grafixatorGame.rayHandler, noRays, rayColor, lightDistance, xPos + spriteWidth/2, yPos + spriteHeight/2);
                		sprite.pointLight = pl;
                	}
                	else if (type.equals("CONE")) {
                		ConeLight cl =  new ConeLight(grafixatorGame.rayHandler, noRays, rayColor, lightDistance, xPos + spriteWidth/2, yPos + spriteHeight/2, directionAngle, coneAngle);
                		sprite.coneLight = cl;
                	}

			}
			

			if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_BOX2D_BODY) || currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_MAIN_CHARACTER) ||
                    currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_MOVING_PLATFORM))  {
				sprite.box2DBody=true;

				CircleShape circleShape = null;         
				circleShape = new CircleShape();
				float radius = (float) sprite.width/grafixatorGame.tileWidth /2;
				circleShape.setRadius(radius);


				float boxX = (float) sprite.width / grafixatorGame.tileWidth / 2;
				float boxY = (float) sprite.height / grafixatorGame.tileHeight / 2;

				PolygonShape squareShape = new PolygonShape();
				squareShape.setAsBox(boxX, boxY);

				FixtureDef fixtureDef = new FixtureDef();
				String propValue  = (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_BOX2D_BODY);

				BodyDef bodyDef = new BodyDef();
				bodyDef.type = BodyDef.BodyType.DynamicBody;   // This may be overwritten (e.g. if Body is a moving platform).
				
				if (currentProperties.containsKey("HERO_CHARACTER"))  {
					fixtureDef.shape = circleShape;    // Its a circle..    
				}
				else {
					String shapeType="";
                	if (propValue !=null && !propValue.isEmpty()) {   // For moving platforms there is is no property value.
                        shapeType = GrafixatorUtils.getPropertyStringValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_SHAPE);
                	}

					if (shapeType.equals(GrafixatorConstants.PROPERTY_VALUE_SPRITE_SHAPE_CIRCLE)) {
						fixtureDef.shape = circleShape;
					}
					else {
						fixtureDef.shape = squareShape;     
					}
					
	                if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_MOVING_PLATFORM)) {
	                	bodyDef.type = BodyDef.BodyType.KinematicBody; 
	                	sprite.isMovingPlatform=true;
	                }
	                else if (shapeType.equals(GrafixatorConstants.PROPERTY_VALUE_SPRITE_SHAPE_STATIC)) {
	                    bodyDef.type = BodyDef.BodyType.StaticBody;
	                }

				}
				

                

				if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_BOX2D_BODY))  {                              
					fixtureDef.density     = GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_DENSITY, 1.0f);
					fixtureDef.friction    = GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_FRICTION, 1.0f);                                
					fixtureDef.restitution = GrafixatorUtils.getPropertyFloatValue(propValue, GrafixatorConstants.PROPERTY_VALUE_SPRITE_RESTITUTION, 1.0f);

				}


				if (sprite.isAnimation) {  // Stop animations from rotating..
					bodyDef.fixedRotation=true;
				}

				float bodyXPos;
				if (isAnimation) {
					bodyXPos  =  xPos/grafixatorGame.tileWidth + 1;
				}
				else if (isTile) {
					bodyXPos  = tileXPos + ((float) (sprite.width/grafixatorGame.tileWidth)/2);
				}
				else {
					bodyXPos  = tileXPos + ((float) (sprite.width/2)/grafixatorGame.tileWidth);					
 				}

				float bodyYPos=getBox2DYPos(sprite, grafixatorGame);

				bodyDef.position.set(bodyXPos,  bodyYPos);

				if (currentProperties.containsKey(GrafixatorConstants.PROPERTY_KEY_MAIN_CHARACTER))  {
					sprite.isHero=true;                             
					grafixatorGame.heroSprite = sprite;
					grafixatorGame.mainCharStartX = new Float(sprite.xPos);
					grafixatorGame.mainCharStartY = new Float(sprite.yPos);
					
					fixtureDef.restitution = grafixatorGame.worldRestitution;
					fixtureDef.density     = grafixatorGame.worldDensity;
					fixtureDef.friction    = grafixatorGame.worldFriction;
					
					setMainCharacterProperties(grafixatorGame, (String) currentProperties.get(GrafixatorConstants.PROPERTY_KEY_MAIN_CHARACTER));
					
                    grafixatorGame.mainCharBody =  grafixatorGame.world.createBody(bodyDef);
                    grafixatorGame.mainCharBody.setFixedRotation(true);   // Don't allow body to rotate.
                    grafixatorGame. mainCharBody.createFixture(fixtureDef).setUserData("player");
                    sprite.body= grafixatorGame.mainCharBody;

				} 
				else {
					Body body = grafixatorGame.world.createBody(bodyDef);                        
					body.createFixture(fixtureDef);
					sprite.body=body;
				}


				squareShape.dispose();

			}

			if (isTile) {
				sprite.tileNumber = currLayer.getCell(tileXPos, tileYPos).getTile().getId();      
			}

			
			if (isTile) {
				currLayer.setCell(tileXPos, tileYPos, null);  
			}
			
			grafixatorGame.spriteList.add(sprite);
		}   // End of If Tile contains a Property...
		else {  // Sprite has no properties
			if (!isTile) {                     
				GrafixatorSprite sprite = new GrafixatorSprite();
				sprite.width=spriteWidth;
				sprite.height=spriteHeight;
				sprite.renderWidth = 1;
				sprite.xPos = xPos;
				sprite.yPos = yPos;
				sprite.xDir=1;
				sprite.isAnimation=isAnimation;
				sprite.spriteAnimation=animation;
				sprite.mapProperties= currentProperties;
				sprite.spriteTexture= spriteTextureRegion;
				sprite.stateTime = 0;
				sprite.status = GrafixatorSprite.SPRITE_STATUS_ACTIVE;
				sprite.id = 3;
				
				grafixatorGame.spriteList.add(sprite);
				
			}
		}

	}

	public static float getBox2DYPos(GrafixatorSprite sprite, GrafixatorGame gGame) {
		if (sprite.isAnimation) return  (sprite.yPos - sprite.height/2)/gGame.tileHeight; 
		else return (sprite.yPos +sprite.height/2)/gGame.tileHeight;
	}  

	// Set all the properties (movement/fire, camera) for the Main (controllable) character
	public static void setMainCharacterProperties(GrafixatorGame grafixatorGame, String value) {
		grafixatorGame.playerFire            = GrafixatorUtils.getPropertyIntValue(value, "fire", 4);
		grafixatorGame.timeBetweenBulletFire = GrafixatorUtils.getPropertyFloatValue(value, "bulletInterval", 50.0f) * 1000;
		grafixatorGame.bulletSpeed           = (int)  GrafixatorUtils.getPropertyFloatValue(value, "bulletSpeed", 16);
		grafixatorGame.bulletXOffset         = (int)  GrafixatorUtils.getPropertyFloatValue(value, "bulletXOffset",  0);
		grafixatorGame.bulletYOffset         = (int)  GrafixatorUtils.getPropertyFloatValue(value, "bulletYOffset",  0);

		grafixatorGame.charMovement          = GrafixatorUtils.getCharMovement(GrafixatorUtils.getPropertyStringValue(value, GrafixatorConstants.PROPERTY_VALUE_CHARACTER_MOVEMENT));

		grafixatorGame.charFixedRotation     =  GrafixatorUtils.getPropertyBooleanValue(value, "fixedRotation", false);
		grafixatorGame.charThrustForce       =  GrafixatorUtils.getPropertyFloatValue(value, "thrustForce", GrafixatorConstants.DEFAULT_SPACESHIP_THRUST_FORCE);
		grafixatorGame.charUpwardsForce      =  GrafixatorUtils.getPropertyFloatValue(value, "upwardsForce", GrafixatorConstants.DEFAULT_JUMP_UPWARDS_FORCE);

		grafixatorGame.charMoveToBoundary    = GrafixatorUtils.getPropertyBooleanValue(value, GrafixatorConstants.PROPERTY_VALUE_CHARACTER_MOVE_2_BOUNDARY, false);
		grafixatorGame.charRotateToDir       = GrafixatorUtils.getPropertyBooleanValue(value, GrafixatorConstants.PROPERTY_VALUE_CHARACTER_ROATE_TO_DIR, false);
		grafixatorGame.charFullSquare        = GrafixatorUtils.getPropertyBooleanValue(value, GrafixatorConstants.PROPERTY_VALUE_CHARACTER_FULL_SQUARE, false);
		grafixatorGame.charSpeed             = GrafixatorUtils.getPropertyFloatValue(value,   GrafixatorConstants.PROPERTY_VALUE_CHARACTER_SPEED, 4f);

		//		grafixatorGame.handleWallCollisions     = GrafixatorUtils.getPropertyBooleanValue(value, "detectWallCollisions", false);	
		//		grafixatorGame.handleEnemySpriteCollisions     = GrafixatorUtils.getPropertyBooleanValue(value, "detectEnemySpriteCollisions", false);	
		//		
		grafixatorGame.cameraControl         = GrafixatorUtils.getCameraControl(GrafixatorUtils.getPropertyStringValue(value, GrafixatorConstants.PROPERTY_VALUE_CAMERA_CONTROL));
		grafixatorGame.cameraSpeed           = GrafixatorUtils.getPropertyIntValue(value, GrafixatorConstants.PROPERTY_VALUE_CAMERA_SPEED, 4);
				
		grafixatorGame.charAllowDoubleJump     = GrafixatorUtils.getPropertyBooleanValue(value, "allowDoubleJump", false);					
		grafixatorGame.charJumpHorizontalForce = GrafixatorUtils.getPropertyFloatValue(value, "jumpForceHoriz", GrafixatorConstants.DEFAULT_JUMP_HORIZ_FORCE);					
		grafixatorGame.charJumpVerticalForce   = GrafixatorUtils.getPropertyFloatValue(value, "jumpForceVert",  GrafixatorConstants.DEFAULT_JUMP_VERTICAL_FORCE);
		grafixatorGame.charLinearVelocity      = GrafixatorUtils.getPropertyFloatValue(value, "playerVelocity",  GrafixatorConstants.DEFAULT_CHAR_LINEAR_VELOCITY);
				
		grafixatorGame.cameraAutoStop        = GrafixatorUtils.getPropertyBooleanValue(value, "cameraAutoStop", false);	

		if (grafixatorGame.cameraAutoStop) {
			grafixatorGame.cameraHorizStopPosition= GrafixatorUtils.getPropertyIntValue(value,  "cameraHorizStopPosition", 0);
			grafixatorGame.cameraVertStopPosition = GrafixatorUtils.getPropertyIntValue(value,  "cameraVerticalStopPosition", 0);
		}

	}

	// Extract the points from an Image Sprite (i.e. The position of the Grafixator Sprite).
	public static List<ImageSprite> getPoints(String points, int width, int height) {
		List<ImageSprite> pointsList = new ArrayList<ImageSprite>();

		if (points.isEmpty()) return pointsList;

		String[] pointsArray = points.split(";");

		for (String str: pointsArray) {
			str=str.replaceAll("\\(","");
			str=str.replaceAll("\\)","");
			String splitstring[] = str.split(",");

			ImageSprite point = new ImageSprite();
			point.x = Integer.valueOf(splitstring[0]);
			point.y = Integer.valueOf(splitstring[1]);
			point.width  = width;
			point.height = height;
			pointsList.add(point);
		}

		return pointsList;
	}
	
	public static void setPlayerBulletBox2dLight(String value, GrafixatorGame grafixatorGame) {
		int noRays = GrafixatorUtils.getPropertyIntValue(value, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_NO_RAYS, 16);
		Color rayColor = GrafixatorUtils.getColorFromString(GrafixatorUtils.getPropertyStringValue(value, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_COLOR), Color.RED);
		float distance = GrafixatorUtils.getPropertyFloatValue(value,  GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_DISTANCE, 120);   // Usually distance is around 3xwidth, so 120 is a guess.  Should never happen tho as it shd be populated.
		grafixatorGame.playerBulletPointLight = new PointLight(grafixatorGame.rayHandler, noRays, rayColor, distance, -1000000,-1000000);
	}

	public static void setEnemyBulletBox2dLight(String value, GrafixatorGame grafixatorGame, int textureNo) {
		int noRays = GrafixatorUtils.getPropertyIntValue(value, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_NO_RAYS, 16);
		Color rayColor = GrafixatorUtils.getColorFromString(GrafixatorUtils.getPropertyStringValue(value, GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_COLOR), Color.RED);
		float distance = GrafixatorUtils.getPropertyFloatValue(value,  GrafixatorConstants.PROPERTY_VALUE_BOX2DLIGHT_DISTANCE, 120);   // Usually distance is around 3xwidth, so 120 is a guess.  Should never happen tho as it shd be populated.

		grafixatorGame.enemyBulletPointLight[textureNo] = new PointLight(grafixatorGame.rayHandler, noRays, rayColor, distance, -1000000,-1000000);
	}

	private static TextureRegion extractTextureRegion(Texture texture, int xPos, int yPos, int width, int height) {
		return new TextureRegion(texture, xPos, yPos, width, height);
	}

	// Splits a String Coord (2,4) into an array of integers (with 2 in [0], and 4 in [1])
	public static int[] getCoords(String coordinate) {
		coordinate=coordinate.replaceAll("\\(","");
		coordinate=coordinate.replaceAll("\\)","");
		String splitstring[] = coordinate.split(",");

		int intArray[] = new int[2];
		intArray[0] = Integer.valueOf(splitstring[0]);
		intArray[1] = Integer.valueOf(splitstring[1]);
		return intArray;
	}


}
