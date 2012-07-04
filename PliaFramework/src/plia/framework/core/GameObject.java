package plia.framework.core;

import plia.framework.math.Vector2;
import plia.framework.math.Vector3;
import plia.framework.math.Vector4;
import plia.framework.scene.PlaneCollider;
import plia.framework.scene.SphereCollider;
import plia.framework.scene.Camera;
import plia.framework.scene.Group;
import plia.framework.scene.Light;
import plia.framework.scene.SkyBox;
import plia.framework.scene.SkyDome;
import plia.framework.scene.Terrain;
import plia.framework.scene.group.shading.Color3;
import plia.framework.scene.group.shading.Color4;
import plia.framework.scene.group.shading.Texture2D;
import plia.framework.scene.view.Button;
import plia.framework.scene.view.Sprite;
import android.util.Log;

public class GameObject
{
	protected String name;
	protected boolean active = true;
	
	public GameObject()
	{
		this.name = "GameObject";
	}
	
	public GameObject(String name)
	{
		this.name = name;
	}
	
	protected void copyTo(GameObject gameObject)
	{
		gameObject.active = this.active;
		gameObject.name = this.name;
	}

	public GameObject instantiate()
	{
		GameObject copy = new GameObject();
		this.copyTo(copy);
		return copy;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	protected void update()
	{
		
	}
	
	public void log(Object value)
	{
		Log.e("Plia Framework", value+"");
	}
	
	///
	public static final Group model(String fbx_path)
	{
		return GameObjectManager.loadModel(fbx_path);
	}
	
	public static final Terrain terrain(String heightmapSrc, int maxHeight, int scale)
	{
		return GameObjectManager.createTerrain(heightmapSrc, maxHeight, scale);
	}
	
	public static final Terrain terrain(String heightmapSrc, String diffusemapSrc, int maxHeight, int scale)
	{
		Terrain t = GameObjectManager.createTerrain(heightmapSrc, maxHeight, scale);
		t.setBaseTexture(GameObjectManager.loadTexture2D(diffusemapSrc));
		
		return t;
	}
	
	public static final Light directionalLight(float intensity)
	{
		Light light = new Light();
		light.setIntensity(intensity);
		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);

		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ, float intensity)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ, float red, float green, float blue)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);
		light.setColor(red, green, blue);

		return light;
	}
	
	public static final Light directionalLight(float forwardX, float forwardY, float forwardZ, float intensity, float red, float green, float blue)
	{
		Light light = new Light();
		light.setForward(forwardX, forwardY, forwardZ);
		light.setColor(red, green, blue);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);

		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range, float intensity)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range, float red, float green, float blue)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);
		light.setColor(red, green, blue);

		return light;
	}
	
	public static final Light pointLight(float posX, float posY, float posZ, float range, float intensity, float red, float green, float blue)
	{
		Light light = new Light();
		light.setLightType(Light.POINT_LIGHT);
		light.setRange(range);
		light.setPosition(posX, posY, posZ);
		light.setColor(red, green, blue);
		light.setIntensity(intensity);
		
		return light;
	}
	
	public static final Camera camera(int projectionType)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		
		return camera;
	}
	
	public static final Camera camera(int projectionType, float range)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		camera.setRange(range);
		
		return camera;
	}
	
	public static final Camera camera(int projectionType, float posX, float posY, float posZ, float range)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		camera.setPosition(posX, posY, posZ);
		camera.setRange(range);
		
		return camera;
	}
	
	public static final Camera camera(int projectionType, float posX, float posY, float posZ, float targetX, float targetY, float targetZ, float range)
	{
		Camera camera = new Camera();
		camera.setProjectionType(projectionType);
		camera.setPosition(posX, posY, posZ);
		camera.setLookAt(new Vector3(targetX, targetY, targetZ));
		camera.setRange(range);
		
		return camera;
	}
	
	public static final SkyDome skydome(String textureSrc)
	{
		SkyDome dome = new SkyDome();
		dome.setTexture(tex2D(textureSrc));
		
		return dome;
	}
	
	public static final SkyBox skybox(String src)
	{
		int indexOfDot = src.lastIndexOf(".");
		
		String s1 = src.substring(0, indexOfDot);
		String s2 = src.substring(indexOfDot, src.length());
		
		String frontSrc  = s1+"_front"+s2;
		String backSrc 	 = s1+"_back"+s2;
		String leftSrc 	 = s1+"_left"+s2;
		String rightSrc  = s1+"_right"+s2;
		String topSrc 	 = s1+"_top"+s2;
		String bottomSrc = s1+"_bottom"+s2;
		
		Log.e("", frontSrc+", "+backSrc+", "+leftSrc+", "+rightSrc+", "+topSrc+", "+bottomSrc);
		
		
		return new SkyBox(tex2D(frontSrc), tex2D(backSrc), tex2D(leftSrc), tex2D(rightSrc), tex2D(topSrc), tex2D(bottomSrc));
	}
	
	public static final SkyBox skyBox(String frontSrc, String backSrc, String leftSrc, String rightSrc, String topSrc, String bottomSrc)
	{
		return new SkyBox(tex2D(frontSrc), tex2D(backSrc), tex2D(leftSrc), tex2D(rightSrc), tex2D(topSrc), tex2D(bottomSrc));
	}
	
	public static final Sprite sprite(String imgSrc)
	{
		return GameObjectManager.createSprite(imgSrc);
	}
	
	public static final Sprite sprite(String imgSrc, int frame)
	{
		Sprite sprite = new Sprite();
		sprite.setImageSrc(GameObjectManager.loadTexture2D(imgSrc), frame);
		
		return sprite;
	}
	
	public static final Button button()
	{
		return new Button();
	}
	
	public static final Button button(String imgSrc)
	{
		return GameObjectManager.createButton(imgSrc);
	}
	
	public static final Button button(String imgSrc, int frame)
	{
		Button sprite = new Button();
		sprite.setImageSrc(GameObjectManager.loadTexture2D(imgSrc), frame);
		
		return sprite;
	}
	
	public static final Texture2D tex2D(String path)
	{
		return GameObjectManager.loadTexture2D(path);
	}
	
	public static final PlaneCollider collider(float upX, float upY, float upZ, float scaleX, float scaleY)
	{
		PlaneCollider boundingPlane = new PlaneCollider();
		boundingPlane.setScale(scaleX, scaleY, 0);
		boundingPlane.setUp(upX, upY, upZ);
		return boundingPlane;
	}
	
	public static final PlaneCollider collider(Vector3 up, Vector2 scale)
	{
		PlaneCollider boundingPlane = new PlaneCollider();
		boundingPlane.setScale(scale.x, scale.y, 0);
		boundingPlane.setUp(up);
		return boundingPlane;
	}
	
	public static final SphereCollider collider(float radius)
	{
		return new SphereCollider(radius);
	}
	
	//
	public static final Vector2 vec2()
	{
		return new Vector2();
	}
	
	public static final Vector2 vec2(float value)
	{
		return new Vector2(value, value);
	}
	
	public static final Vector2 vec2(float x, float y)
	{
		return new Vector2(x, y);
	}
	
	public static final Vector3 vec3()
	{
		return new Vector3();
	}
	
	public static final Vector3 vec3(float value)
	{
		return new Vector3(value, value, value);
	}
	
	public static final Vector3 vec3(float x, float y, float z)
	{
		return new Vector3(x, y, z);
	}
	
	public static final Vector4 vec4()
	{
		return new Vector4();
	}
	
	public static final Vector4 vec4(float value)
	{
		return new Vector4(value, value, value, value);
	}
	
	public static final Vector4 vec4(float x, float y, float z, float w)
	{
		return new Vector4(x, y, z, w);
	}
	
	public static final Color3 color(float r, float g, float b)
	{
		return new Color3(r, g, b);
	}
	
	public static final Color4 color(float r, float g, float b, float a)
	{
		return new Color4(r, g, b, a);
	}
}
