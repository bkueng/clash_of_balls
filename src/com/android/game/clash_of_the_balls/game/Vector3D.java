package com.android.game.clash_of_the_balls.game;

import android.util.FloatMath;

public class Vector3D {

	public float x,y,z;
	

	public Vector3D() {
		x = 0.0f;
		y = 0.0f;
		z = 0.0f;
	}

	public Vector3D(float fx, float fy, float fz) {
		x = fx;
		y = fy;
		z = fz;
	}

	public Vector3D(Vector3D src) {
		x = src.x;
		y = src.y;
		z = src.z;
	}
	
	public void set(Vector3D src) {
		x = src.x;
		y = src.y;
		z = src.z;
	}
	public void set(float fx, float fy, float fz) {
		x = fx;
		y = fy;
		z = fz;
	}
	

	public float length() { 
		return FloatMath.sqrt((x * x) + (y * y) + (z * z));
	}
	
	public float lengthSquared() { 
		return (x * x) + (y * y) + (z * z);
	}

	public void normalize() {
		float len = length();

		if (len != 0.0f) {
			x /= len;
			y /= len;
			z /= len;
		}
	}

	public void add(Vector3D vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
	}
	public void add(float fx, float fy, float fz) {
		x += fx;
		y += fy;
		z += fz;
	}

	public void sub(Vector3D vector) {
		x -= vector.x;
		y -= vector.y;
		z -= vector.z;
	}


	public void mul(Vector3D vector) {
		x *= vector.x;
		y *= vector.y; 
		z *= vector.z; 
	}
	public void div(Vector3D vector) {
		x /= vector.x;
		y /= vector.y; 
		z /= vector.z; 
	}
	 
	public void mul(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
	}
	
	public void cross(Vector3D v) {
		float cx = y*v.z - z*v.y;
		float cy = z*v.x - x*v.z;
		float cz = x*v.y - y*v.x;
		x=cx;
		y=cy;
		z=cz;
	}
	public void dot(Vector3D v) {
		x*=v.x;
		y*=v.y;
		z*=v.z;
	}
	//angle in radians, range [0,pi]
	public float angle(Vector3D v) {
		float l1 = length();
		float l2 = v.length();
		if(l1 == 0.f || l2 == 0.f) return 0.f;
		return (float)Math.acos((x*v.x + y*v.y + z*v.z) / l1 / l2);
	}
	
	//rotate this vector around vector n, by angle radians
	//n will be normalized
	public void rotate(Vector3D n, float angle) {
		float ca=FloatMath.cos(angle);
		float sa=FloatMath.sin(angle);
		
		float len=n.length();
		if(len==0.0f || angle==0.f) return;
		
		n.mul(1.f/len);
		Vector3D v = n;
		
		//x
		float rx = x*(ca+v.x*v.x*(1.0f-ca))+
				y*(v.x*v.y*(1.0f-ca)-v.z*sa)+
				z*(v.x*v.z*(1.0f-ca)+v.y*sa);
		//y
		float ry = x*(v.x*v.y*(1.0f-ca)+v.z*sa)+
				y*(ca+v.y*v.y*(1.0f-ca))+
				z*(v.y*v.z*(1.0f-ca)-v.x*sa);
		//z
		float rz = x*(v.x*v.z*(1.0f-ca)-v.y*sa)+
				y*(v.y*v.z*(1.0f-ca)+v.x*sa)+
				z*(ca+v.z*v.z*(1.0f-ca));
		
		x = rx;
		y = ry;
		z = rz;
		
	}

}
