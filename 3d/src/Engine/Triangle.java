package Engine;

import java.awt.Color;

public class Triangle implements Cloneable,Comparable<Triangle>{
	public Point v[]=new Point[3];
	public Color color=Color.DARK_GRAY;
	public boolean highlight=false;
	
	public Triangle() {
		v[0]=new Point();
		v[1]=new Point();
		v[2]=new Point();
		
		color=new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255));
		
	}
	
	public String toString() {
		return "Triangle: "+"V1("+v[0].x+","+v[0].y+","+v[0].z+")"+"V2("+v[1].x+","+v[1].y+","+v[1].z+")"+"V3("+v[2].x+","+v[2].y+","+v[2].z+")";
	}
	
	public Object clone(){  
		Triangle temp=new Triangle();
		temp.v[0]=(Point) v[0].clone();
		temp.v[1]=(Point) v[1].clone();
		temp.v[2]=(Point) v[2].clone();
		temp.color=color;
		temp.highlight=highlight;
		
	    return temp; 
	}

	@Override
	public int compareTo(Triangle o) {
		Point center1=getCenter();
		Point center2=o.getCenter();
		double dist1=Math.sqrt(center1.x*center1.x+center1.y*center1.y+center1.z*center1.z);
		double dist2=Math.sqrt(center2.x*center2.x+center2.y*center2.y+center2.z*center2.z);
		
		return -Double.compare(dist1,dist2);
	}
	
	
	
	public boolean isPointInside(Point p) {
		return sameNormalSide(p, v[0],v[1],v[2]) &&
				sameNormalSide(p, v[1],v[0],v[2]) &&
				sameNormalSide(p, v[2],v[0],v[1]);
		
	}
	public Vector getNormal() {
		Vector vec1=new Vector();
		Vector vec2=new Vector();
		
		// Just pick two vectors for the normal
		vec1.x=v[1].x-v[0].x;
		vec1.y=v[1].y-v[0].y;
		vec1.z=v[1].z-v[0].z;
		
		vec2.x=v[2].x-v[0].x;
		vec2.y=v[2].y-v[0].y;
		vec2.z=v[2].z-v[0].z;
		
		return vec1.cross(vec2).normalize();
	}
	
	public Point getCenter() {
		Point temp=new Point();
		temp.x=(v[0].x+v[1].x+v[2].x)/3;
		temp.y=(v[0].y+v[1].y+v[2].y)/3;
		temp.z=(v[0].z+v[1].z+v[2].z)/3;
		
		return temp;
	}
	
	// Checks if points belongs to half plane of triangle
	private boolean sameNormalSide(Point p,Point a,Point b,Point c) {
		Vector vec1=new Vector();
		Vector vec2=new Vector();
		Vector vec3=new Vector();
		
		Vector cross1=new Vector();
		Vector cross2=new Vector();
		
		vec1.x=c.x-b.x;
		vec1.y=c.y-b.y;
		vec1.z=c.z-b.z;
		
		vec2.x=p.x-b.x;
		vec2.y=p.y-b.y;
		vec2.z=p.z-b.z;
		
		vec3.x=a.x-b.x;
		vec3.y=a.y-b.y;
		vec3.z=a.z-b.z;
		
		cross1=vec1.cross(vec2);
		cross2=vec1.cross(vec3);
		
		// Are the vectors pointint in aprox. same direction?
		return cross1.dot(cross2)>=0;
	}
}
