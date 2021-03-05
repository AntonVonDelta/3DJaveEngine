package Engine;

public class Point implements Cloneable{
	public double x,y,z;
	
	public Object clone(){  
		Point temp=new Point();
		temp.x=x;
		temp.y=y;
		temp.z=z;
		return temp;
	}
}
