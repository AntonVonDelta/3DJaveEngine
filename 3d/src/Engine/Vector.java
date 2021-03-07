package Engine;

public class Vector extends Point implements Cloneable{
	public double xAngle;
	public double yAngle;
	public double zAngle;
	
	public Object clone(){  
		Vector temp=new Vector();
		temp.x=x;
		temp.y=y;
		temp.z=z;
		temp.xAngle=xAngle;
		temp.yAngle=yAngle;
		temp.zAngle=zAngle;
	    return temp; 
	}
	
	public Vector normalize() {
		Vector temp=new Vector();
		double r=Math.sqrt(x*x+y*y+z*z);
		temp.x=x/r;
		temp.y=y/r;
		temp.z=z/r;
		return temp;
	}
	public double dot(Vector other) {
		return x*other.x+y*other.y+z*other.z;
	}
	public double dot(Point other) {
		return x*other.x+y*other.y+z*other.z;
	}
	public Vector cross(Vector other) {
		Vector temp=new Vector();
		temp.x=y*other.z-z*other.y;
		temp.y=z*other.x-x*other.z;
		temp.z=x*other.y-y*other.x;
		
		return temp;
	}
	// Returns the point of intersection between line and plane
	// plane_normal 	- The normal of the plane
	// line_direction 	- The vector describing the line
	// plane_origin 	- Random point on the plane
	// line_origin		- Random point on line
	public static Point intersectPlane(Vector plane_normal,Vector line_direction,Point plane_origin,Point line_origin) {
		Point temp=new Point();
		
		// If the line is parallel to the plane (aka perpendicular on the normal) then return null
		if(doubleEq(plane_normal.dot(line_direction),0)  ) {
			return null;
		}
		plane_normal=plane_normal.normalize();
		line_direction=line_direction.normalize();
		
		// Plane eq: P0P1*N=0 <=> a(x-x0)+b(y-y0)+c(z-z0)=0
		// Line eq: P2=L0+c*t where t is a direction vector
		double t=(plane_normal.dot(plane_origin)-plane_normal.dot(line_origin))/plane_normal.dot(line_direction);
		temp.x=line_origin.x+t*line_direction.x;
		temp.y=line_origin.y+t*line_direction.y;
		temp.z=line_origin.z+t*line_direction.z;
		
		return temp;
	}
	
	
	private static boolean doubleEq(double a, double b) {
		double error = 0.000001;
		return Math.abs(a - b) <= error;
	}
}
