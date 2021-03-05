package Engine;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scene {
	private static int w, h;
	private static List<Triangle> triangles = new ArrayList<Triangle>();
	private Vector camera = new Vector();

	private double z_near = 5;
	private double fov_angle = 60;

	public Scene() {
		camera.x = 0;
		camera.y = 0;
		camera.z = 0;
		z_near = 1;// w/2*Math.tan(Math.toRadians(Main.fov_angle/2));
		w = 1000;
		h = 500;
		fov_angle=Math.toDegrees(Math.atan(1/z_near));	// 1 means the dimensions/half width of the near plane
	}

	public void drawOnGraphics(Graphics g) {
		g.clearRect(0, 0, w, h);

		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			Triangle new_persp = new Triangle();
			if (!isInFOV(temp)) {
				// We need clipping
				Point[] points=clipTriangle(temp);
				int[] x_coord=new int[points.length];
				int[] y_coord=new int[points.length];
				
				for(int j=0;j<points.length;j++) {
					Point transformed=projectWorldCoordinatesToScreen(points[j]);
					
					x_coord[j]=(int)transformed.x;
					y_coord[j]=(int)transformed.y;
				}
				
				g.setColor(temp.color);
				g.fillPolygon(x_coord,y_coord, points.length);
				
			} else {
				for (int j = 0; j < 3; j++) {
					new_persp.v[j] = projectWorldCoordinatesToScreen(temp.v[j]);
				}
				
				g.setColor(temp.color);
				g.fillPolygon(	new int[] { (int) (new_persp.v[0].x), (int) (new_persp.v[1].x), (int) (new_persp.v[2].x) },
								new int[] { (int) (new_persp.v[0].y), (int) (new_persp.v[1].y), (int) (new_persp.v[2].y) }, 3);

			}
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
				
				if(st.startsWith("/")) continue;
				
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
				if(pos.length>3) {
					// A color is defined
					String[] colors = pos[3].split(",");
					
					int r = Integer.parseInt(colors[0]);
					int g = Integer.parseInt(colors[1]);
					int b = Integer.parseInt(colors[2]);
					
					temp.color=new Color(r,g,b);
				}
				triangles.add(temp);
			}

		} catch (Exception e) {
			System.out.println("Can't read file");
			e.printStackTrace();
		}
	}

	// Clips a triangle agains the near plane
	// The coordinates are un-translated
	private Point[] clipTriangle(Triangle triangle) {
		List<Point> points = new ArrayList<Point>();
		Line result = null;

		// Take first line
		result = clipLine(triangle.v[0], triangle.v[1]);
		if (result != null) {
			points.add(result.a);
			points.add(result.b);
		}

		// Take next line
		result = clipLine(triangle.v[1], triangle.v[2]);
		if (result != null) {
			points.add(result.a);
			points.add(result.b);
		}

		// Take next line - hope the polygon filler doesn't mind a extra line
		result = clipLine(triangle.v[2], triangle.v[0]);
		if (result != null) {
			points.add(result.a);
			points.add(result.b);
		}

		return (Point[]) points.toArray(new Point[points.size()]);
	}

	// Clips a line against the front near plane
	// The coordinates are un-translated relative to the camera aka world
	// coordinates
	// Returns world points
	private Line clipLine(Point a, Point b) {
		Point translated_a = applyCameraTransformation(a);
		Point translated_b = applyCameraTransformation(b);

		Line temp = new Line();

		if (isInBack(a) && isInBack(b))
			return null; // Can't clip a hidden line
		if (!isInBack(a) && !isInBack(b)) { // The line is perfectly visible

			temp.a = (Point) a.clone();
			temp.b = (Point) b.clone();
			return temp;
		}

		if (!isInBack(a)) {
			// Point a is visible
			// Point b hidden
			temp.a = (Point) a.clone();
			temp.b = new Point();

			if(doubleEq(translated_b.x - translated_a.x,0)) {
				// Perpendicular line
				temp.b.x=translated_b.x;
				temp.b.z=z_near;
			}else {
				double b1 = z_near; // 0*x1+b1=y1 -->the near plane line equation
				double a2 = (translated_b.z - translated_a.z) / (translated_b.x - translated_a.x); // The slope of clipping line
				double b2 = translated_b.z - a2 * translated_b.x;

				temp.b.x = (b1 - b2) / a2;
				temp.b.z = b1;	// This must be b1 because we hust intersected	//a2 * temp.b.x + b2;
			}


			// Find the y intersection for the vertical component
			// X becomes the Y axis
			// Y becomes the Z axis
			if(doubleEq(translated_b.y - translated_a.y,0)) {
				// Perpendicular line
				temp.b.y=translated_b.y;
			}else {
				double b1 = z_near;
				double a2 = (translated_b.z - translated_a.z) / (translated_b.y - translated_a.y);
				double b2 = translated_b.z - a2 * translated_b.y;
				temp.b.y = (b1 - b2) / a2;
			}

			// This point was calculated with camera coordinates. Convert them to world
			temp.b = applyWorldTransformation(temp.b);
			return temp;
		}

		// Point a is hidden
		// Point b is visible
		temp.b = (Point) b.clone();
		temp.a = new Point();


		if(doubleEq(translated_b.x - translated_a.x,0)) {
			// Perpendicular line
			temp.a.x=translated_b.x;
			temp.a.z=z_near;
		}else {
			double b1 = z_near; // 0*x1+b1=y1 -->the near plane line equation
			double a2 = (translated_b.z - translated_a.z) / (translated_b.x - translated_a.x); // The slope of clipping line
			double b2 = translated_b.z - a2 * translated_b.x;

			temp.a.x = (b1 - b2) / a2;
			temp.a.z = b1;	// This must be b1 because we hust intersected	//a2 * temp.a.x + b2;
		}


		// Find the y intersection for the vertical component
		// X becomes the Y axis
		// Y becomes the Z axis
		if(doubleEq(translated_b.y - translated_a.y,0)) {
			// Perpendicular line
			temp.a.y=translated_b.y;
		}else {
			double b1 = z_near;
			double a2 = (translated_b.z - translated_a.z) / (translated_b.y - translated_a.y);
			double b2 = translated_b.z - a2 * translated_b.y;
			temp.a.y = (b1 - b2) / a2;
		}

		// This point was calculated with camera coordinates. Convert them to world
		temp.a = applyWorldTransformation(temp.a);
		return temp;
	}

	public void translateSceneXAxis(double amount) {
		camera.x -= amount;
	}

	public void translateSceneYAxis(double amount) {
		camera.y -= amount;
	}

	public void translateSceneZAxis(double amount) {
		camera.z -= amount;
	}

	public void rotateSceneXAxis(double amount) {
		xRotateTriangles(amount);
	}

	public void rotateSceneYAxis(double amount) {
		yRotateTriangles(amount);
	}

	public void rotateSceneZAxis(double amount) {
		zRotateTriangles(amount);
	}

	private void xRotateTriangles(double amount_angle) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			temp.v = xRotate(temp, amount_angle).v;
		}
	}

	private void yRotateTriangles(double amount_angle) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			temp.v = yRotate(temp, amount_angle).v;
		}
	}

	private void zRotateTriangles(double amount_angle) {
		for (int i = 0; i < triangles.size(); i++) {
			Triangle temp = triangles.get(i);
			temp.v = zRotate(temp, amount_angle).v;
		}
	}

	private static Triangle xRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();
		amount_angle = Math.toRadians(amount_angle);

		for (int j = 0; j < 3; j++) {
			new_pos.v[j].x = triangle.v[j].x;
			new_pos.v[j].y = triangle.v[j].y * Math.cos(amount_angle) + triangle.v[j].z * Math.sin(amount_angle);
			new_pos.v[j].z = triangle.v[j].z * Math.cos(amount_angle) - triangle.v[j].y * Math.sin(amount_angle);
		}
		return new_pos;
	}

	private static Triangle yRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();
		amount_angle = Math.toRadians(amount_angle);

		for (int j = 0; j < 3; j++) {
			new_pos.v[j].x = triangle.v[j].x * Math.cos(amount_angle) - triangle.v[j].z * Math.sin(amount_angle);
			new_pos.v[j].y = triangle.v[j].y;
			new_pos.v[j].z = triangle.v[j].z * Math.cos(amount_angle) + triangle.v[j].x * Math.sin(amount_angle);
		}
		return new_pos;
	}

	private static Triangle zRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();
		amount_angle = Math.toRadians(amount_angle);

		for (int j = 0; j < 3; j++) {
			new_pos.v[j].x = triangle.v[j].x * Math.cos(amount_angle) - triangle.v[j].y * Math.sin(amount_angle);
			new_pos.v[j].y = triangle.v[j].y * Math.cos(amount_angle) + triangle.v[j].x * Math.sin(amount_angle);
			new_pos.v[j].z = triangle.v[j].z;
		}
		return new_pos;
	}

	private static double normalizeAngle(double angle) {
		// [0,360)
		angle = angle % 360;
		if (angle < 0)
			angle += 360;

		return angle;
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

	private boolean isInFOV(Triangle t) {
		return isInFOV(t.v[0]) && isInFOV(t.v[1]) && isInFOV(t.v[2]);
	}

	// Check if point is hidden because of the near plane
	private boolean isInBack(Point p) {
		Point translated = applyCameraTransformation(p);
		double dx = translated.x, dy = translated.y, dz = translated.z;

		return (dz - z_near >= 0 ? false : true);
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

	// Apllies the camera rotation and translation to the object given in world
	// coordinates
	private Point applyCameraTransformation(Point p) {
		Point translated = new Point();
		translated.x = p.x - camera.x;
		translated.y = p.y - camera.y;
		translated.z = p.z - camera.z;

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

		return translated;
	}

//    public  Point strip3D(Point pa,Point pb) {
//    	Point temp=new Point();
//    	
//    	
//    	if(!(isInBack(pa) ^ isInBack(pb))) {
//    		temp.x=100000;
//    		temp.y=100000;	//should not process but I don't want to return null
//    	}
//    	
//		double dz0=pa.z-camera.z;
//		double dz1=pb.z-camera.z;
//		double dx=isInBack(pa)?pa.x-pb.x:pb.x-pa.x;
//		double dy=isInBack(pa)?pa.y-pb.y:pb.y-pa.y;
//		
//		double a=(!isInBack(pa)?dz0:dz1);
//		double b=Math.abs((isInBack(pa)?dz0:dz1));
//		double offx=a*dx/(a+b);
//		double offy=a*dy/(a+b);
//		
//		if(isInBack(pb)) {
//			temp.x=(int)((pa.x+offx-camera.x)*resizeW+w/2);
//			temp.y=(int)(-(pa.y+offy-camera.y)*resizeH+h/2);
//		}else {
//			temp.x=(int)((pb.x+offx-camera.x)*resizeW+w/2);
//			temp.y=(int)(-(pb.y+offy-camera.y)*resizeH+h/2);
//		}
//		
//		return temp;
//    }

	public static int[] splice(int arr[], int index) {
		int temp[] = new int[arr.length];
		temp = Arrays.copyOfRange(arr, 0, arr.length);

		for (int i = index; i < arr.length - 1; i++) {
			temp[i] = temp[i + 1];
		}
		return temp;
	}
	
	private static boolean doubleEq(double a,double b) {
		double error=0.000001;
		return Math.abs(a-b)<=error;
	}
}
