package plia.core.scene;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.opengl.GLES20;

import plia.core.GameObject;
import plia.core.Screen;
import plia.core.scene.animation.Animation;
import plia.core.scene.geometry.Dome;
import plia.core.scene.geometry.Geometry;
import plia.core.scene.geometry.Mesh;
import plia.core.scene.geometry.Plane;
import plia.core.scene.geometry.Quad;
import plia.core.scene.shading.Color3;
import plia.core.scene.shading.Shader;
import plia.core.scene.shading.ShaderProgram;
import plia.core.scene.shading.Texture2D;
import plia.math.Matrix3;
import plia.math.Matrix4;
import plia.math.Vector2;
import plia.math.Vector3;
import plia.math.Vector4;

@SuppressWarnings({"rawtypes"})
public final class Scene extends GameObject
{
	private Layer[] children = new Layer[32];
	private int childCount = 0;

	public Scene()
	{
		setName("Scene");
	}

	
	@Override
	public void update()
	{
		if(isActive())
		{
			for (int i = 0; i < childCount; i++)
			{
				children[i].update();
			}
		}
	}
	
	private final int indexOf(Layer layer)
	{
		for (int i = 0; i < children.length; i++)
		{
			if(layer == children[i])
			{
				return i;
			}
		}
		
		return -1;
	}

	public final boolean contains(Layer layer)
	{
		return indexOf(layer) != -1;
	}

	public final int getLayerCount()
	{
		return childCount;
	}

	public final Layer getLayer(int index)
	{
		if(index < childCount)
		{
			return children[index];
		}
		
		return null;
	}

	public final boolean addLayer(Layer layer)
	{
		if(indexOf(layer) == -1)
		{
			if(childCount >= children.length)
			{
				Layer[] arr = new Layer[children.length + 32];
				System.arraycopy(children, 0, arr, 0, children.length);
				children = arr;
			}
			
			children[childCount++] = layer;
			return true;
		}
		return false;
	}

	public final boolean removeLayer(Layer layer)
	{
		int i = indexOf(layer);
		if(i > -1 && i < childCount)
		{
			Layer[] arr = new Layer[children.length];
			System.arraycopy(children, 0, arr, 0, i);
			
			int i2 = i+1;
			int length = childCount - i2;
			if(length > 0)
			{
				System.arraycopy(children, i2, arr, i, length);
			}
			
			children = arr;
			childCount--;

			return true;
		}
		return false;
	}
	
	static Light defaultLight = new Light();
	static Camera mainCamera = new Camera();
	static boolean hasChangedProjection = true;
	static boolean hasChangedModelView = true;
	
	static
	{
		defaultLight.setForward(0, 0, -1);
	}
	
	public static Camera getMainCamera()
	{
		return mainCamera;
	}
	
	public static void setMainCamera(Camera mainCamera)
	{
		Scene.mainCamera = mainCamera;
	}
	
	private static float ratio;
	
	private static final Matrix4 modelViewProjectionMatrix = new Matrix4();
	private static final Matrix4 modelViewMatrix = new Matrix4();
	private static final Matrix4 projectionMatrix = new Matrix4();
	
	private static final Matrix4 orthogonalProjection = new Matrix4();
	private static final Matrix4 orthogonalModelView = new Matrix4();
	private static final Matrix4 orthogonalMVP = new Matrix4();

	private static final Matrix4 tempPalette = new Matrix4();
	
	private static final Vector3 target = new Vector3();
//	private static final Vector4 lightPos4 = new Vector4();
	private static final Vector4 lightPosTemp = new Vector4();

	private ArrayList<Sprite> sprites = new ArrayList<Sprite>();
	private ArrayList<Model> models = new ArrayList<Model>();
	private ArrayList<Terrain> terrains = new ArrayList<Terrain>();
	private ArrayList<Light> lights = new ArrayList<Light>();
	
	public static void onSurfaceChanged()
	{
		ratio = (float)Screen.getWidth() / Screen.getHeight();
		
		hasChangedProjection = true;
		
		Matrix4.createOrtho(orthogonalProjection, 0, 1, 1, 0, 1, 10);
		Matrix4.createLookAt(orthogonalModelView, 0, 0, 1, 0, 0, 0, 0, 1, 0);
		Matrix4.multiply(orthogonalMVP, orthogonalProjection, orthogonalModelView);
	}
	
	public static Matrix4 getModelViewMatrix()
	{
		return modelViewMatrix;
	}
	
	public static Matrix4 getProjectionMatrix()
	{
		return projectionMatrix;
	}
	
	public static Matrix4 getModelViewProjectionMatrix()
	{
		return modelViewProjectionMatrix;
	}

	// Draw State
	public void drawScene()
	{
		if(hasChangedProjection)
		{
			if(mainCamera.getProjectionType() == Camera.PERSPECTIVE)
			{
				Matrix4.createFrustum(projectionMatrix, -ratio, ratio, -1, 1, 1, mainCamera.getRange());
			}
			else
			{
				Matrix4.createOrtho(projectionMatrix, -ratio, ratio, -1, 1, 1, mainCamera.getRange());
			}

			hasChangedProjection = false;
		}
		if(hasChangedModelView)
		{
			Matrix4 world = mainCamera.getWorldMatrix();
			Vector3 eye = world.getTranslation();
			Vector3 forward = world.getForward();
			
			target.x = eye.x + (forward.x * 10);
			target.y = eye.y + (forward.y * 10);
			target.z = eye.z + (forward.z * 10);
			
			Vector3 up = world.getUp();
			
			Matrix4.createLookAt(modelViewMatrix, eye, target, up);
			
			hasChangedModelView = false;
		}
		
		Matrix4.multiply(modelViewProjectionMatrix, projectionMatrix, modelViewMatrix);
		
		for (int i = 0; i < getLayerCount(); i++)
		{
			recursiveLayer(getLayer(i));
		}
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		if(mainCamera.getSky() != null)
		{
			drawSky(mainCamera.getSky());
		}
		
		
//		GLES20.glDisable(GLES20.GL_CULL_FACE);

		GLES20.glCullFace(GLES20.GL_FRONT);
		drawTerrains();

		GLES20.glCullFace(GLES20.GL_BACK);
		for (int i = 0; i < models.size(); i++)
		{
			drawModel(models.get(i));
		}
		
//		GLES20.glEnable(GLES20.GL_BLEND);
//		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		for (int i = 0; i < sprites.size(); i++)
		{
			drawSprites(sprites.get(i));
		}
		
//		GLES20.glDisable(GLES20.GL_BLEND);

		sprites.clear();
		models.clear();
		terrains.clear();
		lights.clear();
	}
	
	private void drawSky(Sky sky)
	{
		if(sky instanceof SkyDome)
		{
			drawSkyDome((SkyDome) sky);
		}
	}
	
	private void drawSkyDome(SkyDome skyDome)
	{
		ShaderProgram shaderProgram = Shader.AMBIENT.getProgram(2);
		
		int program = shaderProgram.getProgramID();
		
		GLES20.glUseProgram(program);
		
		int vh = GLES20.glGetAttribLocation(program, "vertex");
		int uvh = GLES20.glGetAttribLocation(program, "uv");
		
		float sc = mainCamera.getRange() / 2.1f;
		
		Vector3 pos = mainCamera.getWorldMatrix().getTranslation();
		Matrix4 tm = new Matrix4();
		tm.setTranslation(pos.x, pos.y, 0);
		tm.m11 = sc;
		tm.m22 = sc;
		tm.m33 = sc;
		
		float[] mvpm = new float[16];
		Matrix4.multiply(modelViewProjectionMatrix, tm).copyTo(mvpm);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "modelViewProjectionMatrix"), 1, false, mvpm, 0);
		
		GLES20.glEnableVertexAttribArray(vh);
		GLES20.glVertexAttribPointer(vh, 3, GLES20.GL_FLOAT, false, 0, Dome.getVB());
		
		GLES20.glEnableVertexAttribArray(uvh);
		GLES20.glVertexAttribPointer(uvh, 2, GLES20.GL_FLOAT, false, 0, Dome.getUVB());
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, skyDome.getTexture().getTextureBuffer());
		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "baseTexture"), 0);
		
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, Dome.getIB().capacity(), GLES20.GL_UNSIGNED_INT, Dome.getIB());

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		GLES20.glDisableVertexAttribArray(vh);
		GLES20.glDisableVertexAttribArray(uvh);
	}
	
//	private void drawSky()
//	{
//		ShaderProgram shaderProgram = Shader.AMBIENT.getProgram(12);
//		
//		int program = shaderProgram.getProgramID();
//		
//		GLES20.glUseProgram(program);
//		
//		int vh = GLES20.glGetAttribLocation(program, "vertex");
//		int uvh = GLES20.glGetAttribLocation(program, "uv");
//		int mh = GLES20.glGetAttribLocation(program, "matIndex");
//		
//		float sc = mainCamera.getRange() / 2.1f;
//		
//		Vector3 pos = mainCamera.getPosition();
//		Matrix4 tm = new Matrix4();
//		tm.setTranslation(pos.x, pos.y, 0);
//		tm.m11 = sc;
//		tm.m22 = sc;
//		tm.m33 = sc;
//		
//		float[] mvpm = new float[16];
//		Matrix4.multiply(modelViewProjectionMatrix, tm).copyTo(mvpm);
//		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "modelViewProjectionMatrix"), 1, false, mvpm, 0);
//		
//		GLES20.glEnableVertexAttribArray(vh);
//		GLES20.glVertexAttribPointer(vh, 3, GLES20.GL_FLOAT, false, 0, Box.getVB());
//		
//		GLES20.glEnableVertexAttribArray(uvh);
//		GLES20.glVertexAttribPointer(uvh, 2, GLES20.GL_FLOAT, false, 0, Box.getUVB());
//		
//		GLES20.glEnableVertexAttribArray(mh);
//		GLES20.glVertexAttribPointer(mh, 1, GLES20.GL_FLOAT, false, 0, Box.getMB());
//		
//		SkyBox box = mainCamera.getSkyBox();
//		
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, box.getFront().getTextureBuffer());
//		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "frontMap"), 0);
//		
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, box.getBack().getTextureBuffer());
//		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "backMap"), 1);
//		
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, box.getLeft().getTextureBuffer());
//		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "leftMap"), 2);
//
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, box.getRight().getTextureBuffer());
//		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "rightMap"), 3);
//
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, box.getTop().getTextureBuffer());
//		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "topMap"), 4);
//
//		GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, box.getBottom().getTextureBuffer());
//		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "bottomMap"), 5);
//
//		GLES20.glDrawElements(GLES20.GL_TRIANGLES, Box.getIB().capacity(), GLES20.GL_UNSIGNED_INT, Box.getIB());
//
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//		
//		GLES20.glDisableVertexAttribArray(vh);
//		GLES20.glDisableVertexAttribArray(uvh);
//		GLES20.glDisableVertexAttribArray(mh);
//	}
	
	private void drawSprites(Sprite view)
	{
		Texture2D tex = view.getImageSrc();
		
		if(tex != null && view.isActive())
		{
			ShaderProgram shaderProgram = Shader.AMBIENT.getProgram(11);
			
			int program = shaderProgram.getProgramID();
	
			Vector2 position = view.getPosition();
			Vector2 scale = view.getScale();
			Matrix4 transformM = new Matrix4();
			transformM.setTranslation(position.x, position.y, 0);
			transformM.m11 = scale.x;
			transformM.m22 = scale.y;
	
			Matrix4 mvp = Matrix4.multiply(orthogonalMVP, transformM);
			
			GLES20.glUseProgram(program);
			
			int vh = GLES20.glGetAttribLocation(program, "vertex");
			int uvh = GLES20.glGetAttribLocation(program, "uv");
			
			float[] mvpm = new float[16];
			mvp.copyTo(mvpm);
			GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "modelViewProjectionMatrix"), 1, false, mvpm, 0);
			
			GLES20.glEnableVertexAttribArray(vh);
			GLES20.glVertexAttribPointer(vh, 2, GLES20.GL_FLOAT, false, 0, Quad.getVertexBuffer());
	
			if(view.hasAnimation())
			{
				float[] srcRect = new float[8];
				float frame = view.getAnimation().getCurrentFrame();
				float width = 1f / view.getAnimation().getTotalFrame();
				
				float x = frame * width;
				float xw = x + width;
	
				srcRect[0] = x;
				srcRect[1] = 0;
				
				srcRect[2] = x;
				srcRect[3] = 1;
				
				srcRect[4] = xw;
				srcRect[5] = 1;
				
				srcRect[6] = xw;
				srcRect[7] = 0;
				
				FloatBuffer sb = Quad.getSpriteUVBuffer();
				sb.clear();
				sb.put(srcRect).position(0);
				
				GLES20.glEnableVertexAttribArray(uvh);
				GLES20.glVertexAttribPointer(uvh, 2, GLES20.GL_FLOAT, false, 0, sb);
			}
			else
			{
				GLES20.glEnableVertexAttribArray(uvh);
				GLES20.glVertexAttribPointer(uvh, 2, GLES20.GL_FLOAT, false, 0, Quad.getUVBuffer());
			}
	
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureBuffer());
			GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "baseTexture"), 0);
			
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_BYTE, Quad.getIndicesBuffer());
			
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		}
	}
	
	private void drawModel(Model model)
	{
		boolean hasAnimation = model.hasAnimation();
		int geometryType = model.getGeometry().getType();
		
		Shader shader = model.getMaterial().getShader();
		
		int hasTexture = 0;
		
		Texture2D texture = model.getMaterial().getBaseTexture();
		
		if(texture != null)
		{
			hasTexture = 2;
		}
		
		int programIndx = 0;
		
		switch (geometryType)
		{
			case Geometry.MESH: programIndx = hasTexture; break;
			case Geometry.SKINNED_MESH: programIndx = 1 + hasTexture; break;
			default: break;
		}
		
		ShaderProgram program = shader.getProgram(programIndx);

		float[] matrixPalette = null;
		
		Mesh mesh =  ((Mesh)model.getGeometry());
		if(hasAnimation)
		{
			Animation animation = model.getAnimation();
			int frame = animation.getCurrentFrame() - mesh.getMatrixPaletteIndexOffset();
			
			float[][] matrixPaletteAll = mesh.getMatrixPalette();
			
			if(frame >= matrixPaletteAll.length)
			{
				frame = matrixPaletteAll.length - 1;
			}
			else if(frame < 0)
			{
				frame = 0;
			}
			
			matrixPalette = matrixPaletteAll[frame];
		}
		
		Matrix4 tmm = new Matrix4();
		Matrix3 nm = new Matrix3();
		
		Vector3 scale = model.getScale();
		Matrix4 world = model.getWorldMatrix().clone();
		world.m11 *= scale.x;
		world.m12 *= scale.y;
		world.m13 *= scale.z;
		
		world.m21 *= scale.x;
		world.m22 *= scale.y;
		world.m23 *= scale.z;
		
		world.m31 *= scale.x;
		world.m32 *= scale.y;
		world.m33 *= scale.z;

		if(geometryType == Geometry.MESH && hasAnimation)
		{
			Matrix4 tmv = new Matrix4();
			
			tempPalette.set(matrixPalette);
			Matrix4.multiply(tmv, world, tempPalette);
			Matrix4.multiply(tmm, tmv, model.getAxisRotation());
			
		}
		else
		{
			Matrix4.multiply(tmm, world, model.getAxisRotation());
		}

		Matrix3.createNormalMatrix(nm, tmm);



		int prg = program.getProgramID();
		GLES20.glUseProgram(prg);

		boolean isDiffuse = (shader == Shader.DIFFUSE);
		
		if(isDiffuse)
		{
			// Lights
			ArrayList<Light> ls = new ArrayList<Light>();
			ls.addAll(lights);
			
			setLightUniform(prg, ls);
			
//			float[] tm = new float[16];
//			projectionMatrix.copyTo(tm);
//			GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prg, "projectionMatrix"), 1, false, tm, 0);
//			
//			float[] tm1 = new float[16];
//			modelViewMatrix.copyTo(tm1);
//			GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prg, "modelViewMatrix"), 1, false, tm1, 0);
			
			float[] tm1 = new float[16];
			modelViewProjectionMatrix.copyTo(tm1);
			GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prg, "modelViewProjectionMatrix"), 1, false, tm1, 0);
			
			float[] tm2 = new float[9];
			nm.copyTo(tm2);
			GLES20.glUniformMatrix3fv(GLES20.glGetUniformLocation(prg, "normalMatrix"), 1, false, tm2, 0);
		}
		else
		{
//			Matrix4 mvp = Matrix4.multiply(projectionMatrix, tmm);
//			float[] tm = new float[16];
//			mvp.copyTo(tm);
//			GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prg, "modelViewProjectionMatrix"), 1, false, tm, 0);
			
			float[] tm1 = new float[16];
			modelViewProjectionMatrix.copyTo(tm1);
			GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prg, "modelViewProjectionMatrix"), 1, false, tm1, 0);
		}
		
		float[] tm3 = new float[16];
		tmm.copyTo(tm3);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(prg, "worldMatrix"), 1, false, tm3, 0);

		
//		program.setUniform(ShaderProgram.PROJECTION_MATRIX, projectionMatrix);
//		program.setUniform(ShaderProgram.MODELVIEW_MATRIX, tempTransformMatrix);
//		program.setUniform(ShaderProgram.NORMAL_MATRIX, tempNormalMatrix);

		int vh = GLES20.glGetAttribLocation(prg, "vertex");
		int nh = GLES20.glGetAttribLocation(prg, "normal");
		int uvh = GLES20.glGetAttribLocation(prg, "uv");
		
		int bwh = GLES20.glGetAttribLocation(prg, "boneWeights");
		int bih = GLES20.glGetAttribLocation(prg, "boneIndices");
		int bch = GLES20.glGetAttribLocation(prg, "boneCount");

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.getBuffer(0));
		GLES20.glEnableVertexAttribArray(vh);
		GLES20.glVertexAttribPointer(vh, 3, GLES20.GL_FLOAT, false, 0, 0);
		
		if(isDiffuse)
		{
			GLES20.glEnableVertexAttribArray(nh);
			GLES20.glVertexAttribPointer(nh, 3, GLES20.GL_FLOAT, false, 0, mesh.NORMALS_OFFSET);
		}
		
//		program.setAttribPointer(ShaderProgram.VERTEX_ATTRIBUTE, 3, 0, 0, mesh.getBuffer(0), VariableType.FLOAT);
//		program.setAttribPointer(ShaderProgram.NORMAL_ATTRIBUTE, 3, 0, mesh.NORMALS_OFFSET, mesh.getBuffer(0), VariableType.FLOAT);

		if(hasTexture == 2)
		{
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureBuffer());
			GLES20.glUniform1i(GLES20.glGetUniformLocation(prg, "diffuseMap"), 0);
			
			GLES20.glEnableVertexAttribArray(uvh);
			GLES20.glVertexAttribPointer(uvh, 2, GLES20.GL_FLOAT, false, 0, mesh.UV_OFFSET);
			
//			program.setAttribPointer(ShaderProgram.UV_ATTRIBUTE, 2, 0, mesh.UV_OFFSET, mesh.getBuffer(0), VariableType.FLOAT);
//			program.setUniformDiffuseMap(ShaderProgram.DIFFUSE_MAP, texture.getTextureBuffer());
		}
		else
		{
			Color3 baseColor3 = model.getMaterial().getBaseColor();
			GLES20.glUniform4f(GLES20.glGetUniformLocation(prg, "color"), baseColor3.r, baseColor3.g, baseColor3.b, 1);
			
//			program.setUniformColor(model.getMaterial().getBaseColor());
		}
		
		if(geometryType == Geometry.SKINNED_MESH && hasAnimation)
		{
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.getBuffer(2));
			GLES20.glEnableVertexAttribArray(bwh);
			GLES20.glVertexAttribPointer(bwh, 4, GLES20.GL_FLOAT, false, 0, 0);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.getBuffer(3));
			GLES20.glEnableVertexAttribArray(bih);
			GLES20.glVertexAttribPointer(bih, 4, GLES20.GL_SHORT, false, 0, 0);

			GLES20.glVertexAttrib1f(bch, 4);
			
//			program.setAttribPointer(ShaderProgram.BONE_WEIGHTS_ATTRIBUTE, 4, 0, 0, mesh.getBuffer(2), VariableType.FLOAT);
//			program.setAttribPointer(ShaderProgram.BONE_INDEXES_ATTRIBUTE, 4, 0, 0, mesh.getBuffer(3), VariableType.SHORT);
//			program.setAttrib(ShaderProgram.BONE_COUNT, 4);
			
			if(matrixPalette != null)
			{
				int mp = GLES20.glGetUniformLocation(prg, "matrixPalette");
				GLES20.glUniformMatrix4fv(mp, matrixPalette.length / 16, false, matrixPalette, 0);
				
//				program.setUniformMatrix4(ShaderProgram.MATRIX_PALETTE, matrixPalette);
			}
		}
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mesh.getBuffer(1));
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, mesh.INDICES_COUNT, GLES20.GL_UNSIGNED_INT, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		GLES20.glDisableVertexAttribArray(vh);
		GLES20.glDisableVertexAttribArray(nh);
		GLES20.glDisableVertexAttribArray(uvh);
		GLES20.glDisableVertexAttribArray(bwh);
		GLES20.glDisableVertexAttribArray(bih);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
//		program.drawTriangleElements(mesh.getBuffer(1), mesh.INDICES_COUNT);
	}
	
	private void drawTerrains()
	{
		if(terrains.size() > 0)
		{
			for (int i = 0; i < terrains.size(); i++)
			{
				Terrain terrain = terrains.get(i);
				
//				drawDisplacementTerrain(terrain);
				
				if(terrain instanceof MeshTerrain)
				{
					recursiveGroup(((MeshTerrain) terrain).getTerrainModel());
				}
				else
				{
					drawDisplacementTerrain((DisplacementTerrain) terrain);
				}
			}
		}
	}
	
	private void drawDisplacementTerrain(DisplacementTerrain terrain)
	{
		Matrix4 tmm = new Matrix4();
		tmm.setTranslation(terrain.getWorldMatrix().getTranslation());
		
		Matrix3 nm = new Matrix3();
		Matrix3.createNormalMatrix(nm, tmm);

		// Lights
		ArrayList<Light> ls = new ArrayList<Light>();
		ls.addAll(lights);
		
		ShaderProgram shaderProgram = Shader.DIFFUSE.getProgram(5);
		
		int program = shaderProgram.getProgramID();

		GLES20.glUseProgram(program);
		
		setLightUniform(program, ls);
		
//		float[] tm = new float[16];
//		projectionMatrix.copyTo(tm);
//		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "projectionMatrix"), 1, false, tm, 0);
//		
//		float[] tm1 = new float[16];
//		modelViewMatrix.copyTo(tm1);
//		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "modelViewMatrix"), 1, false, tm1, 0);

		float[] tm1 = new float[16];
		modelViewProjectionMatrix.copyTo(tm1);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "modelViewProjectionMatrix"), 1, false, tm1, 0);
		
		float[] tm2 = new float[9];
		nm.copyTo(tm2);
		GLES20.glUniformMatrix3fv(GLES20.glGetUniformLocation(program, "normalMatrix"), 1, false, tm2, 0);
		
		float[] tm3 = new float[16];
		tmm.copyTo(tm3);
		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "worldMatrix"), 1, false, tm3, 0);

		Texture2D diffuseMap = terrain.getBaseTexture();
		
		if(diffuseMap != null)
		{
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, terrain.getBaseTexture().getTextureBuffer());
			GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "diffuseMap"), 0);
		}
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, terrain.getNormalmap().getTextureBuffer());
		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "normalMap"), 1);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, terrain.getHeightmap().getTextureBuffer());
		GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "heightMap"), 2);

		GLES20.glUniform3f(GLES20.glGetUniformLocation(program, "terrainData"), terrain.getTerrainMaxHeight(), Plane.getInstance().getSegment(), terrain.getTerrainScale());
		
		int vh = GLES20.glGetAttribLocation(program, "vertex");

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, Terrain.getTerrainBuffer(0));
		GLES20.glEnableVertexAttribArray(vh);
		GLES20.glVertexAttribPointer(vh, 2, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, Terrain.getTerrainBuffer(1));
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, Plane.getInstance().getIndicesCount(), GLES20.GL_UNSIGNED_INT, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		GLES20.glDisableVertexAttribArray(vh);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}
	
	private void setLightUniform(int program, ArrayList<Light> lights)
	{
		int lightCount = lights.size();
		
		if(lightCount == 0)
		{
			lights.add(defaultLight);
			lightCount = 1;
		}
		
		GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "lightCount"), lightCount);

//		program.use();
//		program.setUniform(ShaderProgram.LIGHT_COUNT, lightCount);
		
		for (int i = 0; i < lightCount; i++)
		{
			Light light = lights.get(i);
			Matrix4 world = light.getWorldMatrix();
			int lt = light.getLightType();
			if(lt == Light.DIRECTIONAL_LIGHT)
			{
				Vector3 forward = world.getForward();
				lightPosTemp.set(-forward.x, -forward.y, -forward.z, 0);
			}
			else
			{
				lightPosTemp.set(world.getTranslation(), 1);
			}
			
//			Matrix4.multiply(lightPos4, modelViewMatrix, lightPosTemp);
			GLES20.glUniform4f(GLES20.glGetUniformLocation(program, "lightPosition["+i+"]"), lightPosTemp.x, lightPosTemp.y, lightPosTemp.z, lt);
			
			Color3 color = light.getColor();
			GLES20.glUniform4f(GLES20.glGetUniformLocation(program, "lightColor["+i+"]"), color.r, color.g, color.b, 1);
			
			GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "lightRange["+i+"]"), light.getRange());
			GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "lightIntensity["+i+"]"), light.getIntensity());
			
//			program.setUniformLight(i, lightPos4, light.getColor(), light.getRange(), light.getIntensity());
		}
	}
	
	private void recursiveLayer(Layer layer)
	{
		if(layer.isActive())
		{
			for (int i = 0; i < layer.getChildCount(); i++)
			{
				Node child = layer.getChild(i);
				if(child instanceof Group)
				{
					recursiveGroup((Group) child);
				}
				else if(child instanceof View)
				{
					recursiveView((View) child);
				}
			}
		}
	}
	
	private void recursiveView(View view)
	{
		if(view.isActive())
		{
			if(view instanceof Sprite)
			{
				sprites.add((Sprite) view);
			}
			
			for (int i = 0; i < view.getChildCount(); i++)
			{
				recursiveView(view.getChild(i));
			}
		}
	}
	
	private void recursiveGroup(Group obj)
	{
		if(obj.isActive())
		{
			if(obj instanceof Model)
			{
				models.add((Model) obj);
			}
			else if(obj instanceof Terrain)
			{
				terrains.add((Terrain) obj);
			}
			else if(obj instanceof Light)
			{
				lights.add((Light) obj);
			}
			
			for (int i = 0; i < obj.getChildCount(); i++)
			{
				Group child = obj.getChild(i);
				recursiveGroup(child);
			}
		}
	}

}