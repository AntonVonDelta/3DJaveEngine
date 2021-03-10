package Engine;

public class Line {
	public Point a;
	public Point b;

	// Extraordinary code! Found here: https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
	// Returns 1 if the lines intersect, otherwise 0. In addition, if the lines
	// intersect the intersection point may be stored in the floats i_x and i_y.
	public static Point get_line_intersection(Point p0, Point p1, Point p2, Point p3)
	{
		Point result=new Point();
	    double s1_x, s1_y, s2_x, s2_y;
	    s1_x = p1.x - p0.x;     
	    s1_y = p1.y - p0.y;
	    s2_x = p3.x - p2.x;    
	    s2_y = p3.y - p2.y;

	    double s, t;
	    s = (-s1_y * (p0.x - p2.x) + s1_x * (p0.y - p2.y)) / (-s2_x * s1_y + s1_x * s2_y);
	    t = ( s2_x * (p0.y - p2.y) - s2_y * (p0.x - p2.x)) / (-s2_x * s1_y + s1_x * s2_y);

	    if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
	    {
	        // Collision detected
	    	result.x = p0.x + (t * s1_x);
	    	result.y = p0.y + (t * s1_y);
	        return result;
	    }

	    return null; // No collision
	}
}
