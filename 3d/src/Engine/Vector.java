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
}
