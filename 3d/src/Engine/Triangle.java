package Engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Triangle implements Cloneable, Comparable<Triangle> {
	public Point v[] = new Point[3];
	public Color color = Color.DARK_GRAY;
	public boolean highlight = false;
	public String debug_name = "";
	private double points_per_area = 4 / 0.1; // Controls the resolution for scanning points on large triangles

	public Triangle() {
		v[0] = new Point();
		v[1] = new Point();
		v[2] = new Point();

		color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));

	}

	public String toString() {
		return "Triangle: " + "V1(" + v[0].x + "," + v[0].y + "," + v[0].z + ")" + "V2(" + v[1].x + "," + v[1].y + ","
				+ v[1].z + ")" + "V3(" + v[2].x + "," + v[2].y + "," + v[2].z + ")";
	}

	public Object clone() {
		Triangle temp = new Triangle();
		temp.v[0] = (Point) v[0].clone();
		temp.v[1] = (Point) v[1].clone();
		temp.v[2] = (Point) v[2].clone();
		temp.color = color;
		temp.highlight = highlight;
		temp.debug_name = debug_name;

		return temp;
	}

	@Override
	public int compareTo(Triangle o) {
		Point center1 = getCenter();
		Point center2 = o.getCenter();
		double center_dist1 = Math.sqrt(center1.x * center1.x + center1.y * center1.y + center1.z * center1.z);
		double center_dist2 = Math.sqrt(center2.x * center2.x + center2.y * center2.y + center2.z * center2.z);
		int order = 0;

		// The line origin is the "eye" aka position 0,0,0
		Point line_origin = new Point();
		line_origin.x = 0;
		line_origin.y = 0;
		line_origin.z = 0;

		// Line direction vector
		List<Vector> vectors_first_triangle = new ArrayList<Vector>(); // Vectors to points contained by the first
																		// triangle
		List<Vector> vectors_second_triangle = new ArrayList<Vector>(); // Vectors to points contained by the second
																		// triangle

		for (Point temp : getEquidistantPoints()) {
			vectors_first_triangle.add(new Vector(temp));
		}
		for (Point temp : o.getEquidistantPoints()) {
			vectors_second_triangle.add(new Vector(temp));
		}

		for (Vector line : vectors_first_triangle) {
			Point intersection = null;
			intersection = Vector.intersectPlane(o.getNormal(), line.normalize(), o.v[0], line_origin);
			if (intersection != null && o.isPointInside(intersection)) {
				// Now we can compare the triangles
				double dist = Math.sqrt(line.x * line.x + line.y * line.y + line.z * line.z);
				double intersect_dist = Math.sqrt(intersection.x * intersection.x + intersection.y * intersection.y
						+ intersection.z * intersection.z);
				if (!doubleEq(dist - intersect_dist, 0))
					order += -Double.compare(dist, intersect_dist);
			}
		}

		for (Vector line : vectors_second_triangle) {
			Point intersection = null;
			intersection = Vector.intersectPlane(getNormal(), line.normalize(), v[0], line_origin);
			if (intersection != null && isPointInside(intersection)) {
				// Now we can compare the triangles
				double dist = Math.sqrt(line.x * line.x + line.y * line.y + line.z * line.z);
				double intersect_dist = Math.sqrt(intersection.x * intersection.x + intersection.y * intersection.y
						+ intersection.z * intersection.z);
				if (!doubleEq(dist - intersect_dist, 0))
					order += -Double.compare(intersect_dist, dist);
			}
		}

		if (order == 0)
			return -Double.compare(center_dist1, center_dist2);

		return Integer.signum(order);

		// Default Painter's algorithm
		//
	}

	public List<Point> getEquidistantPoints() {
		List<Point> result = new ArrayList<Point>();

		// Heron's formula
		double len1 = Math.sqrt(Math.pow(v[1].x - v[0].x, 2) + Math.pow(v[1].y - v[0].y, 2) + Math.pow(v[1].z - v[0].z, 2));
		double len2 = Math.sqrt(Math.pow(v[2].x - v[1].x, 2) + Math.pow(v[2].y - v[1].y, 2) + Math.pow(v[2].z - v[1].z, 2));
		double len3 = Math.sqrt(Math.pow(v[0].x - v[2].x, 2) + Math.pow(v[0].y - v[2].y, 2) + Math.pow(v[0].z - v[2].z, 2));

		double s = (len1 + len2 + len3) / 2;
		double area = Math.sqrt(Math.max(0, s * (s - len1) * (s - len2) * (s - len3)));

		// Determining the levels required for the proper density
		// Each level returns 4 points
		// The formula works out to be: 4*( 3^level-1)/(3-1) --there's a geometric sum
		// involved
		// Also area increases with square law. We don't want the points to do the same
		double necesary_points = Math.max(4, Math.log(area * points_per_area)/Math.log(2)); 

		// Log base of 3
		// Also we need at least one level in order to generate 4 points
		int levels = (int) Math.max(1, Math.floor(Math.log10(necesary_points / 2 + 1) / Math.log10(3)));

		generateSamplePoints(this, levels, result);
		return result;
		
//		//Statistical generation of points - not good because triangles will flicker
//		for (int i = 0; i < necesary_points; i++) {
//			Point temp = new Point();
//			double distribution_a = Math.random();
//			double distribution_b = Math.random();
//
//			// Uniform distribution of points over the triangle
//			// Inspiration:
//			// https://math.stackexchange.com/questions/538458/how-to-sample-points-on-a-triangle-surface-in-3d
//			temp.x = (1 - Math.sqrt(distribution_a)) * v[0].x
//					+ (Math.sqrt(distribution_a) * (1 - distribution_b)) * v[1].x
//					+ (distribution_b * Math.sqrt(distribution_a)) * v[2].x;
//			temp.y = (1 - Math.sqrt(distribution_a)) * v[0].y
//					+ (Math.sqrt(distribution_a) * (1 - distribution_b)) * v[1].y
//					+ (distribution_b * Math.sqrt(distribution_a)) * v[2].y;
//			temp.z = (1 - Math.sqrt(distribution_a)) * v[0].z
//					+ (Math.sqrt(distribution_a) * (1 - distribution_b)) * v[1].z
//					+ (distribution_b * Math.sqrt(distribution_a)) * v[2].z;
//			result.add(temp);
//		}
//		result.add(v[0]);
//		result.add(v[1]);
//		result.add(v[2]);
//		result.add(getCenter());
//		return result;
	}

	// Returns points that sample the entire triangle
	// Level means how many times to reapply the algorithm for succesive
	// sub-triangles for better coverage
	private static void generateSamplePoints(Triangle t, int level, List<Point> result) {
		Point center = t.getCenter();
		Point p1 = new Point((t.v[0].x + center.x) / 2, (t.v[0].y + center.y) / 2, (t.v[0].z + center.z) / 2);
		Point p2 = new Point((t.v[1].x + center.x) / 2, (t.v[1].y + center.y) / 2, (t.v[1].z + center.z) / 2);
		Point p3 = new Point((t.v[2].x + center.x) / 2, (t.v[2].y + center.y) / 2, (t.v[2].z + center.z) / 2);

		result.add(center);
		result.add(p1);
		result.add(p2);
		result.add(p3);

		level--;
		if (level == 0)
			return;

		Triangle temp = new Triangle();

		temp.v[0] = t.v[0];
		temp.v[1] = center;
		temp.v[2] = t.v[1];
		generateSamplePoints(temp, level, result);

		temp.v[0] = t.v[1];
		temp.v[1] = center;
		temp.v[2] = t.v[2];
		generateSamplePoints(temp, level, result);

		temp.v[0] = t.v[2];
		temp.v[1] = center;
		temp.v[2] = t.v[0];
		generateSamplePoints(temp, level, result);
	}

	public boolean isPointInside(Point p) {
		return sameNormalSide(p, v[0], v[1], v[2]) && sameNormalSide(p, v[1], v[0], v[2])
				&& sameNormalSide(p, v[2], v[0], v[1]);

	}

	public Vector getNormal() {
		Vector vec1 = new Vector();
		Vector vec2 = new Vector();

		// Just pick two vectors for the normal
		vec1.x = v[1].x - v[0].x;
		vec1.y = v[1].y - v[0].y;
		vec1.z = v[1].z - v[0].z;

		vec2.x = v[2].x - v[0].x;
		vec2.y = v[2].y - v[0].y;
		vec2.z = v[2].z - v[0].z;

		return vec1.cross(vec2).normalize();
	}

	public Point getCenter() {
		Point temp = new Point();
		temp.x = (v[0].x + v[1].x + v[2].x) / 3;
		temp.y = (v[0].y + v[1].y + v[2].y) / 3;
		temp.z = (v[0].z + v[1].z + v[2].z) / 3;

		return temp;
	}

	// Checks if points belongs to half plane of triangle
	private boolean sameNormalSide(Point p, Point a, Point b, Point c) {
		Vector vec1 = new Vector();
		Vector vec2 = new Vector();
		Vector vec3 = new Vector();

		Vector cross1 = new Vector();
		Vector cross2 = new Vector();

		vec1.x = c.x - b.x;
		vec1.y = c.y - b.y;
		vec1.z = c.z - b.z;

		vec2.x = p.x - b.x;
		vec2.y = p.y - b.y;
		vec2.z = p.z - b.z;

		vec3.x = a.x - b.x;
		vec3.y = a.y - b.y;
		vec3.z = a.z - b.z;

		cross1 = vec1.cross(vec2);
		cross2 = vec1.cross(vec3);

		// Are the vectors pointint in aprox. same direction?
		return cross1.dot(cross2) >= 0;
	}

	private static boolean doubleEq(double a, double b) {
		double error = 0.000001;
		return Math.abs(a - b) <= error;
	}
}
