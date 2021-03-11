package Engine;

public class Point implements Cloneable{
	public double x,y,z;
	
	public Point() {
		x=0;
		y=0;
		z=0;
	}
	public Point(double pos_x,double pos_y,double pos_z) {
		x=pos_x;
		y=pos_y;
		z=pos_z;
	}
	
	// Returns a vector from this point to the one given
	public Vector to(Point other) {
		Vector result=new Vector();
		result.x=other.x-x;
		result.y=other.y-y;
		result.z=other.z-z;
		return result;
	}
	
	public Object clone(){  
		Point temp=new Point();
		temp.x=x;
		temp.y=y;
		temp.z=z;
		return temp;
	}
}
