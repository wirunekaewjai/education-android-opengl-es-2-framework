package plia.framework;

import static android.opengl.GLES20.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public abstract class Framework extends GameThread1 implements IFramework
{
	private FrameLayout baseLayout = null;
	private GLRenderer renderer = null;
	private FrameLayout guiLayout = null;
	
	public Framework(Context context)
	{
		super(context);
		
		Activity activity = ((Activity)context);
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		baseLayout = new FrameLayout(context);
		renderer = new GLRenderer(context);
		guiLayout = new FrameLayout(context);
		
		baseLayout.addView(renderer);
		baseLayout.addView(guiLayout);
		addRunnable(renderer);
	}
	
	@Override
	public void start()
	{
		((Activity)baseLayout.getContext()).setContentView(baseLayout);
		super.start();
	}
	
	@Override
	public void render()
	{
		// TODO Something
		
		Log.e("Framework", "Rendering");
		
		// ///////////////
	}
	
	public void addView(ViewController controller)
	{
		guiLayout.addView(controller.getView());
	}
	
	public void setOrientation(int requestedOrientation)
	{
		((Activity)getContext()).setRequestedOrientation(requestedOrientation);
	}

	//
	class GLRenderer extends GLSurfaceView implements Renderer, Runnable
	{
		private int width, height;
		private float ratio;

		public GLRenderer(Context context)
		{
			super(context);

			this.setEGLContextClientVersion(2);
			this.setRenderer(this);
			this.setRenderMode(RENDERMODE_WHEN_DIRTY);
		}
		
		@Override
		protected void onDetachedFromWindow()
		{
			// TODO Auto-generated method stub
			super.onDetachedFromWindow();
			
//			stop();
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{
			initialize();
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height)
		{
			this.width = width;
			this.height = height;
			this.ratio = (float) width / height;
		}

		@Override
		public void onDrawFrame(GL10 gl)
		{
			update();
			
			//Rendering
			glEnable(GL_DEPTH_TEST);
			glEnable(GL_CULL_FACE);

			glViewport(0, 0, renderer.width, renderer.height);

			glClearColor(1, 0, 0, 1.0f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			render();
			
			glDisable(GL_DEPTH_TEST);
			glDisable(GL_CULL_FACE);
		}

		@Override
		public void run()
		{
			requestRender();
		}
	}
}

interface IFramework
{
	void initialize();
	void update();
	void render();
}