package Engine;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Scene {
	private static int w, h;
	private static List<Triangle> triangles = new ArrayList<Triangle>();
	private Vector camera = new Vector();

	private double z_near;
	private double fov_angle;

	public Scene() {
		camera.x = 0;
		camera.y = 0;
		camera.z = 0;
		z_near = 1;// w/2*Math.tan(Math.toRadians(Main.fov_angle/2));
		w = 1000;
		h = 500;
		fov_angle = Math.toDegrees(Math.atan(1 / z_near)); // 1 means the dimensions/half width of the near plane
	}

	public void drawOnGraphics(Graphics g) {
		g.clearRect(0, 0, w, h);

		List<Triangle> depth_sorted = new ArrayList<>();

		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			Triangle[] clipped_results = clipTriangle(temp);

			// The triangle is invisble
			if (clipped_results == null)
				continue;

			for (Triangle clipped_triangle : clipped_results) {
				depth_sorted.add(clipped_triangle);
			}
		}

		// Z Sorting for the depth
		Collections.sort(depth_sorted);

		// Draw the triangles
		for (Triangle temp : depth_sorted) {
			Triangle new_persp = new Triangle();
			for (int j = 0; j < 3; j++) {
				new_persp.v[j] = projectCameraCoordinatesToScreen(temp.v[j]);
			}

			g.setColor(temp.color);
			g.fillPolygon(new int[] { (int) (new_persp.v[0].x), (int) (new_persp.v[1].x), (int) (new_persp.v[2].x) },
					new int[] { (int) (new_persp.v[0].y), (int) (new_persp.v[1].y), (int) (new_persp.v[2].y) }, 3);
		}
	}

	public void setDimensions(int width, int height) {
		w = width;
		h = height;
	}

	public void read3DObject(String path) {
		File file = new File(path);

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {
				String[] pos = st.split(" ");
				Triangle temp = new Triangle();

				if (st.startsWith("/") || pos.length<3)
					continue;

				if (pos.length >= 3) {
					String[] points = pos[0].split(",");

					temp.v[0].x = Double.parseDouble(points[0]);
					temp.v[0].y = Double.parseDouble(points[1]);
					temp.v[0].z = Double.parseDouble(points[2]);

					points = pos[1].split(",");
					temp.v[1].x = Double.parseDouble(points[0]);
					temp.v[1].y = Double.parseDouble(points[1]);
					temp.v[1].z = Double.parseDouble(points[2]);

					points = pos[2].split(",");
					temp.v[2].x = Double.parseDouble(points[0]);
					temp.v[2].y = Double.parseDouble(points[1]);
					temp.v[2].z = Double.parseDouble(points[2]);
				}
				if (pos.length > 3) {
					// A color is defined
					String[] colors = pos[3].split(",");

					int r = Integer.parseInt(colors[0]);
					int g = Integer.parseInt(colors[1]);
					int b = Integer.parseInt(colors[2]);

					temp.color = new Color(r, g, b);
				}
				triangles.add(temp);
			}

		} catch (Exception e) {
			System.out.println("Can't read file");
			e.printStackTrace();
		}
	}

	// Clips a triangle against the near plane
	// Requires world coordinates
	// The coordinates relative to the camera aka transformed
	private Triangle[] clipTriangle(Triangle triangle) {
		List<Point> visible_points = new ArrayList<Point>();
		List<Point> hidden_points = new ArrayList<Point>();

		triangle=applyCameraTransformation(triangle);
		
		for (Point p : triangle.v) {
			if (isInBack(p)) {
				hidden_points.add(p);
			} else
				visible_points.add(p);
		}

		// All corners are visible. Thus the triangle is also visible
		if (visible_points.size() == 3)
			return new Triangle[] { triangle };
		if (visible_points.size() == 0)
			return null;

		// Two corners are visible
		if (visible_points.size() == 2) {
			Triangle temp1 = new Triangle();
			Triangle temp2 = new Triangle();

			temp1.color = triangle.color;
			temp2.color = triangle.color;

			Point intersect1 = clipLine(visible_points.get(0), hidden_points.get(0));
			Point intersect2 = clipLine(visible_points.get(1), hidden_points.get(0));

			temp1.v[0] = visible_points.get(0);
			temp1.v[1] = intersect1;
			temp1.v[2] = visible_points.get(1);

			temp2.v[0] = intersect1;
			temp2.v[1] = intersect2;
			temp2.v[2] = visible_points.get(1);

			return new Triangle[] { temp1, temp2 };
		}

		// Just one corner is visible
		Triangle temp1 = new Triangle();
		temp1.color = triangle.color;

		Point intersect1 = clipLine(visible_points.get(0), hidden_points.get(0));
		Point intersect2 = clipLine(visible_points.get(0), hidden_points.get(1));

		temp1.v[0] = visible_points.get(0);
		temp1.v[1] = intersect1;
		temp1.v[2] = intersect2;

		return new Triangle[] { temp1 };
	}

	// Clips a line against the front near plane
	// Requires camera coordinates
	// The coordinates are translated/transformed relative to the camera coordinates
	// Returns the intersection point in camera coordinates
	private Point clipLine(Point translated_a, Point translated_b) {
		Line temp = new Line();

		if (!isInBack(translated_a)) {
			// Point a is visible
			// Point b hidden
			temp.a = (Point) translated_a.clone();
			temp.b = new Point();

			if (doubleEq(translated_b.x - translated_a.x, 0)) {
				// Perpendicular line
				temp.b.x = translated_b.x;
				temp.b.z = z_near;
			} else {
				double b1 = z_near; // 0*x1+b1=y1 -->the near plane line equation
				double a2 = (translated_b.z - translated_a.z) / (translated_b.x - translated_a.x); // The slope of
																									// clipping line
				double b2 = translated_b.z - a2 * translated_b.x;

				temp.b.x = (b1 - b2) / a2;
				temp.b.z = b1; // This must be b1 because we hust intersected //a2 * temp.b.x + b2;
			}

			// Find the y intersection for the vertical component
			// X becomes the Y axis
			// Y becomes the Z axis
			if (doubleEq(translated_b.y - translated_a.y, 0)) {
				// Perpendicular line
				temp.b.y = translated_b.y;
			} else {
				double b1 = z_near;
				double a2 = (translated_b.z - translated_a.z) / (translated_b.y - translated_a.y);
				double b2 = translated_b.z - a2 * translated_b.y;
				temp.b.y = (b1 - b2) / a2;
			}

			// This point was calculated with camera coordinates. Convert them to world
			//temp.b = applyWorldTransformation(temp.b);
			return temp.b;
		}

		// Point a is hidden
		// Point b is visible
		temp.b = (Point) translated_b.clone();
		temp.a = new Point();

		if (doubleEq(translated_b.x - translated_a.x, 0)) {
			// Perpendicular line
			temp.a.x = translated_b.x;
			temp.a.z = z_near;
		} else {
			double b1 = z_near; // 0*x1+b1=y1 -->the near plane line equation
			double a2 = (translated_b.z - translated_a.z) / (translated_b.x - translated_a.x); // The slope of clipping
																								// line
			double b2 = translated_b.z - a2 * translated_b.x;

			temp.a.x = (b1 - b2) / a2;
			temp.a.z = b1; // This must be b1 because we hust intersected //a2 * temp.a.x + b2;
		}

		// Find the y intersection for the vertical component
		// X becomes the Y axis
		// Y becomes the Z axis
		if (doubleEq(translated_b.y - translated_a.y, 0)) {
			// Perpendicular line
			temp.a.y = translated_b.y;
		} else {
			double b1 = z_near;
			double a2 = (translated_b.z - translated_a.z) / (translated_b.y - translated_a.y);
			double b2 = translated_b.z - a2 * translated_b.y;
			temp.a.y = (b1 - b2) / a2;
		}

		// This point was calculated with camera coordinates. Convert them to world
		//temp.a = applyWorldTransformation(temp.a);
		return temp.a;
	}

	// Adds a vector of the specified length and direction to the camera
	public void moveCameraXAxis(double amount) {
		camera.x += amount;
	}
	// Adds a vector of the specified length and direction to the camera
	public void moveCameraYAxis(double amount) {
		camera.y += amount;
	}
	// Adds a vector of the specified length and direction to the camera
	public void moveCameraZAxis(double amount) {
		camera.z += amount;
	}

	public void rotateCameraXAxis(double amount) {
		camera.xAngle=amount;
	}

	public void rotateCameraYAxis(double amount) {
		camera.yAngle=amount;
	}

	public void rotateCameraZAxis(double amount) {
		camera.zAngle=amount;
	}

	// Translates all triangles to camera position, rotates and translates back
	private void xRotateTriangles(double amount_angle) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			Triangle translated=applyCameraTransformation(temp);
			temp.v = applyWorldTransformation(xRotate(translated, amount_angle)).v;
		}
	}
	// Translates all triangles to camera position, rotates and translates back
	private void yRotateTriangles(double amount_angle) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			Triangle translated=applyCameraTransformation(temp);
			temp.v = applyWorldTransformation(yRotate(translated, amount_angle)).v;
		}
	}
	// Translates all triangles to camera position, rotates and translates back
	private void zRotateTriangles(double amount_angle) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			Triangle translated=applyCameraTransformation(temp);
			temp.v = applyWorldTransformation(zRotate(translated, amount_angle)).v;
		}
	}

	
	private static Triangle xRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();

		for (int j = 0; j < 3; j++) {
			new_pos.v[j]=xRotate(triangle.v[j], amount_angle);
		}
		return new_pos;
	}
	private static Point xRotate(Point point,double amount_angle) {
		if (amount_angle == 0)
			return (Point) point.clone();

		Point new_pos = new Point();
		amount_angle = Math.toRadians(amount_angle);

		new_pos.x = point.x;
		new_pos.y = point.y * Math.cos(amount_angle) + point.z * Math.sin(amount_angle);
		new_pos.z = point.z * Math.cos(amount_angle) - point.y * Math.sin(amount_angle);
		
		return new_pos;
	}
	
	private static Triangle yRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();

		for (int j = 0; j < 3; j++) {
			new_pos.v[j]=yRotate(triangle.v[j], amount_angle);
		}
		return new_pos;
	}
	private static Point yRotate(Point point,double amount_angle) {
		if (amount_angle == 0)
			return (Point) point.clone();

		Point new_pos = new Point();
		amount_angle = Math.toRadians(amount_angle);

		new_pos.x = point.x * Math.cos(amount_angle) - point.z * Math.sin(amount_angle);
		new_pos.y = point.y;
		new_pos.z = point.z * Math.cos(amount_angle) + point.x * Math.sin(amount_angle);
		
		return new_pos;
	}
	
	private static Triangle zRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();

		for (int j = 0; j < 3; j++) {
			new_pos.v[j] = zRotate(triangle.v[j], amount_angle);
		}
		return new_pos;
	}
	private static Point zRotate(Point point,double amount_angle) {
		if (amount_angle == 0)
			return (Point) point.clone();

		Point new_pos = new Point();
		amount_angle = Math.toRadians(amount_angle);

		new_pos.x = point.x * Math.cos(amount_angle) - point.y * Math.sin(amount_angle);
		new_pos.y = point.y * Math.cos(amount_angle) + point.x * Math.sin(amount_angle);
		new_pos.z = point.z;
		
		return new_pos;
	}


	// Check if World point is in FOV horizontally and vertically
	private boolean isInFOV(Point p) {
		Point translated = applyCameraTransformation(p);
		boolean hor_result = false;
		boolean ver_result = false;

		double dx = translated.x, dy = translated.y, dz = translated.z;
		double horizontal_angle = Math.toDegrees(Math.atan2(dx, dz));
		double vertical_angle = Math.toDegrees(Math.atan2(dy, dz));

		double minAngle = normalizeAngle(-fov_angle / 2);
		double maxAngle = normalizeAngle(fov_angle / 2);

		if (minAngle < maxAngle) {
			hor_result = horizontal_angle > minAngle && horizontal_angle < maxAngle;
			ver_result = vertical_angle > minAngle && vertical_angle < maxAngle;

		} else {
			hor_result = (horizontal_angle > minAngle || horizontal_angle < maxAngle);
			ver_result = (vertical_angle > minAngle || vertical_angle < maxAngle);
		}
		return hor_result && ver_result;
	}

	// Check if point is behind the near plance
	// Requires camera coordinate system points
	// Check if point is hidden because of the near plane
	private boolean isInBack(Point translated) {
		double dx = translated.x, dy = translated.y, dz = translated.z;

		return (dz - z_near >= 0 ? false : true);
	}

	// Check if triangle is hidden because it's behind the near plane
	private boolean isInBack(Triangle t) {
		return isInBack(t.v[0]) && isInBack(t.v[1]) && isInBack(t.v[2]);
	}

	// Projects coordinates from the World system to the screen
	// The points are translated inside according to the camera
	private Point projectWorldCoordinatesToScreen(Point p) {
		Point translated = applyCameraTransformation(p);
		return projectCameraCoordinatesToScreen(translated);
	}

	// Projects points in the camera coordinate space to the screen
	// The points should be transformed according to the camera position
	// (translated)
	private Point projectCameraCoordinatesToScreen(Point p) {
		Point translated = (Point) p.clone();
		Point temp = new Point();

		temp.x = w / 2 + translated.x * z_near / translated.z * w / 2;

		// We also use width for the vertical in order to get a uniform scale
		// Need to substract from the height because on the screen the y axis is
		// reversed
		temp.y = h / 2 - translated.y * z_near / translated.z * w / 2;
		temp.z = p.z;

		return temp;
	}

	// Returns point in world coordinates
	// Takes camera relative point
	private Point applyWorldTransformation(Point p) {
		Point translated = new Point();

		// The rotation order counts!
		p=zRotate(p, camera.zAngle);
		p=xRotate(p, camera.xAngle);
		p=yRotate(p, camera.yAngle);
		
		
		translated.x = p.x + camera.x;
		translated.y = p.y + camera.y;
		translated.z = p.z + camera.z;

		return translated;
	}
	// Returns point in world coordinates
	// Takes camera relative point
	private Line applyWorldTransformation(Line l) {
		Line translated = new Line();
		translated.a = applyWorldTransformation(l.a);
		translated.b = applyWorldTransformation(l.b);

		return translated;
	}
	// Returns point in world coordinates
	// Takes camera relative point
	private Triangle applyWorldTransformation(Triangle t) {
		Triangle translated = new Triangle();
		translated.v[0] = applyWorldTransformation(t.v[0]);
		translated.v[1] = applyWorldTransformation(t.v[1]);
		translated.v[2] = applyWorldTransformation(t.v[2]);
		translated.color = t.color;

		return translated;
	}

	
	// Apllies the camera rotation and translation to the object given in world
	// coordinates
	private Point applyCameraTransformation(Point p) {
		Point translated = new Point();
		translated.x = p.x - camera.x;
		translated.y = p.y - camera.y;
		translated.z = p.z - camera.z;

		// The rotation order counts!
		translated=yRotate(translated, -camera.yAngle);
		translated=xRotate(translated, -camera.xAngle);
		translated=zRotate(translated, -camera.zAngle);
		
		return translated;
	}

	// Apllies the camera rotation and translation to the object given in world
	// coordinates
	private Line applyCameraTransformation(Line l) {
		Line translated = new Line();
		translated.a = applyCameraTransformation(l.a);
		translated.b = applyCameraTransformation(l.b);

		return translated;
	}

	// Apllies the camera rotation and translation to the object given in world
	// coordinates
	private Triangle applyCameraTransformation(Triangle t) {
		Triangle translated = new Triangle();
		translated.v[0] = applyCameraTransformation(t.v[0]);
		translated.v[1] = applyCameraTransformation(t.v[1]);
		translated.v[2] = applyCameraTransformation(t.v[2]);
		translated.color = t.color;

		return translated;
	}

	public Vector getCameraVec() {
		return (Vector) camera.clone();
	}
	
	
	
	public static int[] splice(int arr[], int index) {
		int temp[] = new int[arr.length];
		temp = Arrays.copyOfRange(arr, 0, arr.length);

		for (int i = index; i < arr.length - 1; i++) {
			temp[i] = temp[i + 1];
		}
		return temp;
	}

	private static boolean doubleEq(double a, double b) {
		double error = 0.000001;
		return Math.abs(a - b) <= error;
	}
	
	private static double normalizeAngle(double angle) {
		// [0,360)
		angle = angle % 360;
		if (angle < 0)
			angle += 360;

		return angle;
	}
}
