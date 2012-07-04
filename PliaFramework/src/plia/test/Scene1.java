package plia.test;

import plia.framework.debug.Debug;
import plia.framework.event.OnTouchListener;
import plia.framework.event.TouchEvent;
import plia.framework.math.Vector3;
import plia.framework.scene.*;
import plia.framework.scene.group.animation.Animation;
import plia.framework.scene.group.animation.PlaybackMode;
import plia.framework.scene.view.Button;

public class Scene1 extends Scene implements OnTouchListener
{
	private Layer<Group> layer1 = new Layer<Group>();
	private Layer<View> layer2 = new Layer<View>();
	private Group model1, model2, model3, model4, model5, model6, model7;
	private Terrain terrain;
	private Camera camera;
	
	private Button view1, view2;
	
	private Light backLight, keyLight, fillLight;
	private Light pointLight1, pointLight2;
	
	private Button screenButton, hierarchy;
	
	private Button button1, button2, button3, button4;

	public void onInitialize()
	{
		long start = System.nanoTime();
		model1 = model("buffylow.FBX");

		terrain = terrain("terrain/heightmap.png", "terrain/diffusemap.jpg", 60, 1200);
		terrain.setPosition(-600, -600, 0);

		keyLight = directionalLight(-1, -1, -1, 0.2f, 1, 1);
		backLight = directionalLight(3);
		fillLight = directionalLight(-1, 0, -0.45f, 1.4f);

		pointLight1 = pointLight(-100, 0, 30, 40, 2, 1, 0, 0);
		pointLight2 = pointLight(100, 0, 30, 40, 2, 0, 0, 1);

		camera = camera(Camera.PERSPECTIVE, 350, 400, 50, 250, 250, 50, 800);
		camera.setSky(skydome("sky_sphere01.jpg"));
		Scene.setMainCamera(camera);

		Animation animation1 = model1.getAnimation();
		animation1.play("idle");
		animation1.getAnimationClip("idle").set(35, 50, PlaybackMode.LOOP);

		model2 = model1.instantiate();
		model3 = model1.instantiate();
		model4 = model1.instantiate();
		model5 = model1.instantiate();
		model6 = model1.instantiate();
		model7 = model1.instantiate();
		
		model1.setPosition(250, 250, 40);
		model1.addChild(pointLight1, pointLight2);
		
		SphereCollider p = collider(30);
		PlaneCollider p2 = collider(0, 0, 1, 50, 50);

		SphereCollider bs = collider(30);
		bs.translate(0, 5, 30);
		
		model1.setCollider(p);
		model2.setCollider(p2);
		
		model3.setCollider(bs);
		
		model2.setPosition(300, 250, 40);
		model3.setPosition(200, 250, 40);
		
		model4.setPosition(350, 250, 40);
		model5.setPosition(350, 200, 40);
		model6.setPosition(400, 200, 40);
		model7.setPosition(400, 250, 40);

		view1 = button("sprite3.png", 12);
		view1.setName("Human");
		view1.setPosition(0.25f, 0.5f);
		view1.setScale(0.25f, 0.25f);
//		view1.setOnTouchListener(this);

		Animation animation2 = view1.getAnimation();
		animation2.play("idle");
		animation2.setFrameRate(12);
		animation2.getAnimationClip("idle").setPlaybackMode(PlaybackMode.LOOP);
		
		view2 = view1.instantiate();
		view2.setPosition(0, 0);
		view2.getAnimation().stop();
//		view2.setOnTouchListener(this);
		
		screenButton = button();
		hierarchy = button();
		
		screenButton.addChild(hierarchy);
		hierarchy.addChild(view1, view2);

//		screenButton.setOnTouchListener(this);
		
//		hierarchy.setActive(false);
		
		button1 = button();
		button2 = button();
		button3 = button();
		button4 = button();
		
		button1.setScale(0.5f, 0.5f);
		button2.setScale(0.5f, 0.5f);
		button3.setScale(0.5f, 0.5f);
		button4.setScale(0.5f, 0.5f);
		
		button2.setPosition(0, 0.5f);
		button3.setPosition(0.5f, 0);
		button4.setPosition(0.5f, 0.5f);

		layer1.addChild(model3, camera, keyLight, fillLight, backLight, terrain);
		layer2.addChild(button1, button2, button3, button4);

		addLayer(layer1);
		addLayer(layer2);
		
		button1.setOnTouchListener(this);
		button2.setOnTouchListener(this);
		button3.setOnTouchListener(this);
		button4.setOnTouchListener(this);
		
		terrain.attachBounds(bs);
		
		model3.setLookAt(camera);
		
		float end = (System.nanoTime() - start)/ 1000000f;
		log("Load Time : "+end+" ms");
	}

	public void onUpdate()
	{
//		camera.rotate(0, 0, 0.25f);
//		camera.translate(0, 0.25f, 0);
		
		Vector3 camForward = camera.getForward();
		backLight.setForward(-camForward.x, -camForward.y, -camForward.z);
		
//		model1.rotate(0, 0, 1);
//		model3.rotate(1, 0, 1);
//		model5.rotate(0, 1, 1);
//		model6.rotate(0, 1, 0);

//		Debug.drawLine(model1.getPosition(), vec3(250, 250, 100), new Color3(0.5f, 1, 0.5f));
		
//		Debug.drawBounds(model1.getBounds(), new Color3(0.5f, 1, 0.5f));
//		Debug.drawBounds(model2.getBounds(), new Color3(0.5f, 1, 0.5f));
		Debug.drawBounds(model3.getCollider(), color(0.5f, 1, 0.5f));
//		
//		if(Bounds.intersect(model1.getBounds(), model3.getBounds()))
//		{
////			log("Intersected");
//		}
	}

	public void onTouch(Button button, int action, float x, float y)
	{
		if(button == button1)
		{
			model3.translate(0, 0.5f, 0);
		}
		else if(button == button2)
		{
			model3.translate(0, -0.5f, 0);
		}
		
		else if(button == button3)
		{
			model3.rotate(0, 0, 1);
		}
		else if(button == button4)
		{
			model3.rotate(0, 0, -1);
		}
		
//		if(button == screenButton)
//		{
//			if(action == TouchEvent.ACTION_UP)
//			{
//				hierarchy.setActive(false);
//			}
//			else 
//				hierarchy.setActive(true);
//		}
		
//		if(button == view1)
//		{
//			button.setCenter(x, y);
//			
//			if(action == TouchEvent.ACTION_UP)
//			{
//				view2.getAnimation().play("idle");
//			}
//		}
//
//		if(button == view2)
//		{
//			model2.rotate(1, 0, -1);
//		}
	}
}
