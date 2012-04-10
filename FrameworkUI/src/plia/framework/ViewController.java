package plia.framework;

import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public abstract class ViewController implements IController, Runnable
{
	private View mView = null;
	private Vector<View> touchHoldList = null;
	
	public ViewController(View view)
	{
		Framework.getInstance().addRunnable(this);
		mView = view;
		touchHoldList = new Vector<View>();
	}
	
	public ViewController(Context context, int layoutResID)
	{
		Framework.getInstance().addRunnable(this);
		View view = LayoutInflater.from(context).inflate(layoutResID, null);
		mView = view;
		touchHoldList = new Vector<View>();
	}
	
	public View getView()
	{
		return mView;
	}
	
	public View findViewById(int id)
	{
		return mView.findViewById(id);
	}
	
	public void registerTouchHoldEvent(View view)
	{	
		if(!touchHoldList.contains(view))
		{
			touchHoldList.add(view);
			view.setClickable(true);
		}
	}
	
	public void registerTouchHoldEvent(int id)
	{
		View view = mView.findViewById(id);
		
		if(!touchHoldList.contains(view))
		{
			touchHoldList.add(view);
			view.setClickable(true);
		}
	}
	
	public void unregisterTouchHoldEvent(View view)
	{
		if(touchHoldList.contains(view))
		{
			touchHoldList.remove(view);
		}
	}
	
	public void unregisterTouchHoldEvent(int id)
	{
		View view = mView.findViewById(id);
		
		if(touchHoldList.contains(view))
		{
			touchHoldList.remove(view);
		}
	}
	
	//
	public void changeView(int layoutResID, ViewController controller)
	{
		
	}
	
	
	//
	
	public void onTouchHoldEvent(View v)
	{
		
	}

	@Override
	public void run()
	{
		for (View view : touchHoldList)
		{
			if(view.isPressed())
			{
				onTouchHoldEvent(view);
			}
		}
	}
	
}

interface IController
{
	void update(ViewController observer, Object...objects);
}
