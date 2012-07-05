package plia.fbxplugin.scene.geometry;

import plia.fbxplugin.core.FbxObject;

public class FbxSubDeformer extends FbxObject
{
	private final int subDeformerType;
	
	protected FbxSubDeformer(long uniqueID, int subDeformerType)
	{
		super(uniqueID);
		this.subDeformerType = subDeformerType;
	}

	public int getSubDeformerType()
	{
		return subDeformerType;
	}

	public static final int Cluster = 10001;
}