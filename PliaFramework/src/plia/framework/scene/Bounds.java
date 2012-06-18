package plia.framework.scene;

import plia.framework.math.Matrix4;
import plia.framework.math.Vector3;
import plia.framework.math.Vector4;

public class Bounds extends Object3D
{
	protected Bounds()
	{
		
	}
	
	public static final boolean intersect(BoundingSphere a, BoundingPlane b)
	{
		return intersect(b, a);
	}
	
	public static final boolean intersect(BoundingPlane a, BoundingSphere b)
	{
		Vector4 ap0 = a.getP0();
		Vector4 ap1 = a.getP1();
		Vector4 ap2 = a.getP2();
		Vector4 ap3 = a.getP3();
		
		p0.set(ap0.x, ap0.y, ap0.z);
		p1.set(ap1.x, ap1.y, ap1.z);
		p2.set(ap2.x, ap2.y, ap2.z);
		p3.set(ap3.x, ap3.y, ap3.z);

		Matrix4 aw = a.getWorldMatrix();
		va.set(aw.m41, aw.m42, aw.m43);
		
		Matrix4 bw = b.getWorldMatrix();
		vb.set(bw.m41, bw.m42, bw.m43);
		
		// Sphere Center
		float x0 = bw.m41;
		float y0 = bw.m42;
		float z0 = bw.m43;
		
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
		
		if(d <= r)
		{
			// Circle Center inside Plane
			circleCenter.set(xc, yc, zc);
			boolean ccip = pointInPlane(circleCenter, p0, p1, p2, p3);
			
			if(ccip)
			{
				return true;
			}
			
			// Circle Center outside plane
			Vector3.subtract(p[0], p0, circleCenter);
			Vector3.subtract(p[1], p1, circleCenter);
			Vector3.subtract(p[2], p2, circleCenter);
			Vector3.subtract(p[3], p3, circleCenter);
			Vector3.subtract(pc, va, circleCenter); //va = plane origin
			
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
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static final boolean intersect(BoundingPlane a, BoundingPlane b)
	{
		Vector4 ap0 = a.getP0();
		Vector4 ap1 = a.getP1();
		Vector4 ap2 = a.getP2();
		Vector4 ap3 = a.getP3();
		
		p0.set(ap0.x, ap0.y, ap0.z);
		p1.set(ap1.x, ap1.y, ap1.z);
		p2.set(ap2.x, ap2.y, ap2.z);
		p3.set(ap3.x, ap3.y, ap3.z);

		///////////////////////////
		return false;
	}

	public static final boolean intersect(BoundingSphere a, BoundingSphere b)
	{
		Matrix4 aw = a.getWorldMatrix();
		Matrix4 bw = b.getWorldMatrix();
		
		va.set(aw.m41, aw.m42, aw.m43);
		vb.set(bw.m41, bw.m42, bw.m43);

		float ar = Math.abs(a.getRadius());
		float br = Math.abs(b.getRadius());

		float radius =  ar + br;
		float distance = Vector3.distance(va, vb);

		return distance <= radius;
	}
	
	private static Vector3 circleCenter = new Vector3(), pc = new Vector3(), dir = new Vector3(), lerp = new Vector3(), point = new Vector3();
	private static final Vector3[] p = { new Vector3(), new Vector3(), new Vector3(), new Vector3() };
	private static final Vector3 va = new Vector3(), vb = new Vector3();
	private static final Vector3 p0 = new Vector3(), p1 = new Vector3(), p2 = new Vector3(), p3 = new Vector3();
	private static final Vector3 p5 = new Vector3(), p6 = new Vector3(), p7 = new Vector3(), p8 = new Vector3();

	private static final boolean pointInPlane(Vector3 point, Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3)
	{
		Vector3.subtract(p5, p0, point);
		Vector3.subtract(p6, p1, point);
		Vector3.subtract(p7, p2, point);
		Vector3.subtract(p8, p3, point);
		
		Vector3.normalize(p5, p5);
		Vector3.normalize(p6, p6);
		Vector3.normalize(p7, p7);
		Vector3.normalize(p8, p8);
		
		float d1 = Vector3.dot(p5, p6);
		float d2 = Vector3.dot(p6, p7);
		float d3 = Vector3.dot(p7, p8);
		float d4 = Vector3.dot(p8, p5);

		float sum = (float) (Math.acos(d1)+Math.acos(d2)+Math.acos(d3)+Math.acos(d4));
		return sum >= 6.283185307f;
	}
}
