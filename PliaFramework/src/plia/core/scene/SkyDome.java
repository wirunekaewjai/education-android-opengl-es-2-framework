package plia.core.scene;

import plia.core.scene.shading.Texture2D;

public class SkyDome extends Sky
{
	private Texture2D texture;
	
	public Texture2D getTexture()
	{
		return texture;
	}
	
	public void setTexture(Texture2D texture)
	{
		this.texture = texture;
	}
}
