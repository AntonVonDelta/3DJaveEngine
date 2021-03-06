package Engine;

import java.awt.Color;

public class Triangle implements Cloneable,Comparable<Triangle>{
	public Point v[]=new Point[3];
	public Color color=Color.DARK_GRAY;
	
	public Triangle() {
		v[0]=new Point();
		v[1]=new Point();
		v[2]=new Point();
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
		
	    return temp; 
	}

	@Override
	public int compareTo(Triangle o) {
		return (v[0].z+v[1].z+v[2].z)>(o.v[0].z+o.v[1].z+o.v[2].z)?-1:1;
	}
}
