package plia.core.scene;

import java.util.ArrayList;

import android.util.Log;

import plia.core.debug.Debug;
import plia.core.scene.shading.Color3;
import plia.math.Matrix4;
import plia.math.Vector2;
import plia.math.Vector3;
import plia.math.Vector4;

public class CurveCollider extends Collider
{
	private ArrayList<SphereCollider> colliders = new ArrayList<SphereCollider>();
	
	protected ArrayList<Vector2> corners = new ArrayList<Vector2>();
	protected float z0, z1;
	
	protected CurveCollider(float step, float height, boolean extrudeHeightFromCenter, Vector2...p)
	{
		if(extrudeHeightFromCenter)
		{
			float hh = height/2f;
			z0 = -hh;
			z1 = hh;
		}
		else
		{
			z0 = 0;
			z1 = height;
		}
	}
	
	@Override
	protected void onUpdateHierarchy(boolean parentHasChanged)
	{
		super.onUpdateHierarchy(parentHasChanged);
		
		for (SphereCollider collider : colliders)
		{
			collision(collider);
		}
	}
	
	private static int[] indx = new int[3];
	
	private void collision(SphereCollider b)
	{
		indx[0] = getClosetPlaneIndex(b);
		indx[1] = indx[0]-1;
		indx[2] = indx[0]+1;
		
		if(indx[1] < 0)
		{
			indx[1] = getPlaneCount()-1;
		}
		
		if(indx[2] == getPlaneCount())
		{
			indx[2] = 0;
		}
		
//		Log.e("Indx", indx[0]+", "+indx[1]+", "+indx[2]);
		
		Color3 color = new Color3(1, 0, 0);
		
		for (int j = 0; j < indx.length; j++)
		{
			Vector3[] plane = getCorner(indx[j]);
			
			Vector3 c1 = Vector3.add(Vector3.add(plane[0], plane[1]), Vector3.add(plane[2], plane[3]));
			Vector3 center = new Vector3(c1.x / 4f, c1.y / 4f, c1.z / 4f);
			
			Debug.drawLine(plane[0], plane[1], color);
			Debug.drawLine(plane[1], plane[2], color);
			Debug.drawLine(plane[2], plane[3], color);
			Debug.drawLine(plane[3], plane[0], color);
			
//			boolean isOverlap = Collider.intersect(b, center, plane[0], plane[3], plane[2], plane[1]);
			
			Vector3 p0 = Vector3.subtract(plane[0], center);
			Vector3 p1 = Vector3.subtract(plane[3], center);
			Vector3 p2 = Vector3.subtract(plane[2], center);
			Vector3 p3 = Vector3.subtract(plane[1], center);
			
			Matrix4 bw = b.getWorldMatrix();
			
			// Sphere Center
			float x0 = bw.m41 - center.x;
			float y0 = bw.m42 - center.y;
			float z0 = bw.m43 - center.z;
			
			float r = b.getRadius();
			
			// Plane Equation
			float A = (p1.y*p2.z)+(p0.y*p1.z)+(p0.z*p2.y) - (p1.y*p0.z)-(p2.y*p1.z)-(p2.z*p0.y);
			float B = (p0.x*p2.z)+(p1.z*p2.x)+(p0.z*p1.x) - (p2.x*p0.z)-(p1.z*p0.x)-(p2.z*p1.x);
			float C = (p0.x*p1.y)+(p0.y*p2.x)+(p1.x*p2.y) - (p2.x*p1.y)-(p2.y*p0.x)-(p1.x*p0.y);
			float D = (p0.x*p1.y*p2.z)+(p0.y*p1.z*p2.x)+(p0.z*p1.x*p2.y) - (p2.x*p1.y*p0.z)-(p2.y*p1.z*p0.x)-(p2.z*p1.x*p0.y);
			
			float AABBCC = ((A*A)+(B*B)+(C*C));
			float AxBxCxD = ((A*x0)+(B*y0)+(C*z0)+(D));
			
			float AAxBXCxD = (A * AxBxCxD) / AABBCC;
			float BAxBXCxD = (B * AxBxCxD) / AABBCC;
			float CAxBXCxD = (C * AxBxCxD) / AABBCC;
			
			//Intersected circle center
			float xc = x0 - AAxBXCxD;
			float yc = y0 - BAxBXCxD;
			float zc = z0 - CAxBXCxD;
			
			float d = (float) (Math.abs(AxBxCxD) / Math.sqrt(AABBCC));
			
			// Radius of Circle
			float R = (float) Math.sqrt((r*r) - (d*d));
			
//			Log.e(d+"", r+"");
			boolean isOverlap = false;
			
			Vector3 circleCenter = new Vector3();
			
			if(d <= r)
			{
				// Circle Center inside Plane
				circleCenter = new Vector3(xc, yc, zc);
				boolean ccip = Collider.pointInPlane(circleCenter, p0, p1, p2, p3);

				if(ccip)
				{
					Vector3 start = Vector3.add(center, circleCenter);
					Vector3 e0 = Vector3.add(p0, center);
					Vector3 e1 = Vector3.add(p1, center);
					Vector3 e2 = Vector3.add(p2, center);
					Vector3 e3 = Vector3.add(p3, center);

					color = new Color3(1, 0, 0);
					Debug.drawLine(start, e0, color);
					Debug.drawLine(start, e1, color);
					Debug.drawLine(start, e2, color);
					Debug.drawLine(start, e3, color);

					isOverlap = true;
				}
				else
				{
					// Circle Center outside plane
					Vector3.subtract(p[0], p0, circleCenter);
					Vector3.subtract(p[1], p1, circleCenter);
					Vector3.subtract(p[2], p2, circleCenter);
					Vector3.subtract(p[3], p3, circleCenter);
					Vector3.subtract(pc, new Vector3(), circleCenter); //va = plane origin
					
					Vector3.normalize(pc, pc);
					
					int start = 0;
					int end = 0;
					
					float[] dot = new float[4];
					
					for (int i = 0; i < 4; i++)
					{
						p[i] = p[i].getNormalized();
						dot[i] = Vector3.dot(p[i], pc);			
					}
					
					// Find 2 min dot
					for (int i = 0; i < dot.length; i++)
					{
						if(dot[i] <= dot[start])
						{
							end = start;
							start = i;
						}
					}

					for (float t = 0; t < 1.001f; t+=0.1f)
					{
						Vector3.lerp(lerp, p[start], p[end], t);
						Vector3.normalize(lerp, lerp);
						Vector3.scale(dir, lerp, R);
						Vector3.add(point, circleCenter, dir);
						
						if(pointInPlane(point, p0, p1, p2, p3))
						{
							Vector3 s1 = Vector3.add(center, point);
							Vector3 e0 = Vector3.add(p0, center);
							Vector3 e1 = Vector3.add(p1, center);
							Vector3 e2 = Vector3.add(p2, center);
							Vector3 e3 = Vector3.add(p3, center);
							
							color = new Color3(1, 1, 0);
							Debug.drawLine(s1, e0, color);
							Debug.drawLine(s1, e1, color);
							Debug.drawLine(s1, e2, color);
							Debug.drawLine(s1, e3, color);
							
							isOverlap = true;
							break;
						}
					}
				}
				
				if(isOverlap)
				{
//					Log.e("Is Overlap", b.getName());
					Vector3 intersectionPoint = Vector3.add(center, circleCenter);
					Vector3 objPoint = Vector3.add(center, point);
					
					Vector3 dir = Vector3.subtract(objPoint, intersectionPoint).getNormalized();
					float dist = Vector3.distance(intersectionPoint, objPoint);

					Vector3 v1 = Vector3.subtract(plane[2], plane[0]);
					Vector3 v2 = Vector3.subtract(plane[3], plane[0]);
					
					Vector3 n = Vector3.cross(v1, v2).getNormalized();
					
					Vector3 surfaceBack = new Vector3(-n.x, -n.y, -n.z);
					
					
					Vector3 up = new Vector3(0, 0, 1);
					Vector3 left = Vector3.cross(up, n).getNormalized();
					Vector3 right = Vector3.cross(n, up).getNormalized();

					Group group = (!b.isRoot()) ? b.parent : b;
					
					Vector3 forward = group.getForward();
					
					float dleft = Vector3.dot(left, forward);
					float dright = Vector3.dot(right, forward);
					float dford = Vector3.dot(n, forward);
					float dback = Vector3.dot(surfaceBack, forward);
					
					if(dback > dford)
					{
						if(dleft >= dright && dleft < 1)
						{
							group.setForward(left);
						}
						else if(dright < 1)
						{
							group.setForward(right);
						}
					}

//					Vector3 reflect = Vector3.reflect(forward, n);
//					group.setForward(reflect);
					break;
				}
			}
			
//			Log.e("Center", center.toString());
			
		}
	}
	
	public float getZ0()
	{
		return z0 + getPosition().z;
	}
	
	public float getZ1()
	{
		return z1 + getPosition().z;
	}
	
	public int getPlaneCount()
	{
		return corners.size() - 1;
	}
	
	public Vector3[] getCorner(int index)
	{
		Vector2 a = corners.get(index);
		Vector2 b = corners.get(index+1);

		Matrix4 world = getWorldMatrix();
		
		float z0 = getZ0();
		float z1 = getZ1();
		
		Vector4 P0 = Matrix4.multiply(world, new Vector4(a.x, a.y, z0, 1));
		Vector4 P1 = Matrix4.multiply(world, new Vector4(b.x, b.y, z0, 1));
		Vector4 P2 = Matrix4.multiply(world, new Vector4(b.x, b.y, z1, 1));
		Vector4 P3 = Matrix4.multiply(world, new Vector4(a.x, a.y, z1, 1));
		
		Vector3[] corner = { new Vector3(P0), new Vector3(P1), new Vector3(P2), new Vector3(P3) };
		return corner;
	}
	
	public int getClosetPlaneIndex(Group obj)
	{
		Vector3 pos3 = obj.getPosition();
		Vector2 pos = new Vector2(pos3);
		
		int indx = 0;
		float distMin = -1;
		
		for (int i = 0; i < corners.size(); i++)
		{
			float dist = Vector2.distance(pos, corners.get(i));
			if(distMin == -1 || dist < distMin)
			{
				distMin = dist;
				indx = i;
			}
		}
		
		return Math.min(getPlaneCount(), Math.max(0, indx-1));
	}
	
	public void addCollider(SphereCollider collider)
	{
		colliders.add(collider);
	}
	
	public void removeCollider(SphereCollider collider)
	{
		colliders.remove(collider);
	}
	
	private static Vector3 circleCenter = new Vector3(), pc = new Vector3(), dir = new Vector3(), lerp = new Vector3(), point = new Vector3();
	private static final Vector3[] p = { new Vector3(), new Vector3(), new Vector3(), new Vector3() };
}
