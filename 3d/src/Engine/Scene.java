package Engine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import Structures.Graph;
import Structures.Pair;

public class Scene {
	private static boolean DEBUG = false;
	private static Point DEBUG_POINT = new Point();
	
	private List<Triangle> depth_sorted = new ArrayList<>();
	private boolean cache_sorted_triangles=false;
	
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
		
		if(!cache_sorted_triangles) {
			depth_sorted = new ArrayList<>();
			for (int i = 0; i < triangles.size(); i++) {
				Triangle world_triangle = triangles.get(i);
				Triangle camera_triangle=applyCameraTransformation(world_triangle);
				
				// Culling check
				if(!isTriangleVisible(camera_triangle)) continue;
				
				Triangle[] clipped_results = clipTriangle(camera_triangle);
				
				// The triangle is invisble
				if (clipped_results == null)
					continue;
				
				for (Triangle clipped_triangle : clipped_results) {
					depth_sorted.add(clipped_triangle);
				}
			}

			// Z Sorting for the depth
			Collections.sort(depth_sorted);	// Not good because a value of zero does not means objects have the rendering priority
			//depth_sorted=orderByZDepth(depth_sorted);	// CPU heavy when too many pixels/triangles
			//depth_sorted = looseySort(depth_sorted);		// The best so far

			cache_sorted_triangles=true;
		}


		// Draw the triangles
		for (Triangle temp : depth_sorted) {
			Triangle new_persp = new Triangle();
			for (int j = 0; j < 3; j++) {
				new_persp.v[j] = projectCameraCoordinatesToScreen(temp.v[j]);
			}

			if (temp.highlight) {
				g.setColor(Color.WHITE);
				g.fillPolygon(
						new int[] { (int) (new_persp.v[0].x), (int) (new_persp.v[1].x), (int) (new_persp.v[2].x) },
						new int[] { (int) (new_persp.v[0].y), (int) (new_persp.v[1].y), (int) (new_persp.v[2].y) }, 3);

				g.setColor(Color.BLACK);
				g.drawPolygon(
						new int[] { (int) (new_persp.v[0].x), (int) (new_persp.v[1].x), (int) (new_persp.v[2].x) },
						new int[] { (int) (new_persp.v[0].y), (int) (new_persp.v[1].y), (int) (new_persp.v[2].y) }, 3);

				for (Point pp : temp.getEquidistantPoints()) {
					Point projected = projectCameraCoordinatesToScreen(pp);
					g.setColor(Color.RED);
					g.fillArc((int) (projected.x), (int) (projected.y), 10, 10, 0, 360);
				}
			} else {
				// Shading
				Graphics2D g2=(Graphics2D)g;
				Color v0=temp.getVertexColor(this,0);
				Color v1=temp.getVertexColor(this, 1);
				Color v2=temp.getVertexColor(this, 2);
				
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setPaint(new BarycentricGradientPaint(
						new_persp.v[0].x, new_persp.v[0].y, 
						new_persp.v[1].x, new_persp.v[1].y, 
						new_persp.v[2].x, new_persp.v[2].y, 
						v0, v1, v2));
				
				g2.fillPolygon(
						new int[] { (int) (new_persp.v[0].x), (int) (new_persp.v[1].x), (int) (new_persp.v[2].x) },
						new int[] { (int) (new_persp.v[0].y), (int) (new_persp.v[1].y), (int) (new_persp.v[2].y) }, 3);
				
				
				g.drawPolygon(
						new int[] { (int) (new_persp.v[0].x), (int) (new_persp.v[1].x), (int) (new_persp.v[2].x) },
						new int[] { (int) (new_persp.v[0].y), (int) (new_persp.v[1].y), (int) (new_persp.v[2].y) }, 3);

				
				// Quick fix for white lines between triangles
				g2.setStroke(new BasicStroke(3));
				g2.drawLine( 
						(int) (new_persp.v[0].x), (int) (new_persp.v[0].y), 
						(int) (new_persp.v[1].x),(int)new_persp.v[1].y);
				
				// No Shading
				//g.setColor(temp.getColor(this));
				//g.fillPolygon(
				//		new int[] { (int) (new_persp.v[0].x), (int) (new_persp.v[1].x), (int) (new_persp.v[2].x) },
				//		new int[] { (int) (new_persp.v[0].y), (int) (new_persp.v[1].y), (int) (new_persp.v[2].y) }, 3);				
			}

			if (DEBUG) {
				// Project triangle center
				Point projected_center = projectCameraCoordinatesToScreen(temp.getCenter());
				g.setColor(Color.MAGENTA);
				g.fillArc((int) (projected_center.x), (int) (projected_center.y), 10, 10, 0, 360);
			}

		}

		if (DEBUG) {
			// Project debug point
			Point projected_center = projectCameraCoordinatesToScreen(DEBUG_POINT);
			g.setColor(Color.RED);
			g.fillArc((int) (projected_center.x), (int) (projected_center.y), 10, 10, 0, 360);
		}
	}

	public void setDimensions(int width, int height) {
		w = width;
		h = height;
	}

	public void read3DObject(String path) {
		File file = new File(path);

		if (path.contains(".txt")) {
			// Custom format

			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String st;
				while ((st = br.readLine()) != null) {
					String[] pos = st.split(" ");
					Triangle temp = new Triangle();

					if (st.startsWith("/") || pos.length < 3)
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
					if (pos.length >= 4) {
						// A color is defined
						String[] colors = pos[3].split(",");

						int r = Integer.parseInt(colors[0]);
						int g = Integer.parseInt(colors[1]);
						int b = Integer.parseInt(colors[2]);

						temp.color = new Color(r, g, b);
					}
					if (pos.length >= 5) {
						// A debug name is provided
						temp.debug_name = pos[4];
					}

					triangles.add(temp);
				}

			} catch (Exception e) {
				System.out.println("Can't read file");
				e.printStackTrace();
			}

			return;
		}

		// Object format
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			List<Point> vertexes = new ArrayList<Point>();
			List<Vector> normals = new ArrayList<Vector>();
			
			while ((st = br.readLine()) != null) {
				Triangle face = new Triangle();
				int mode = 0; // 0 - vertex mode; 1- triangle mode; 2 - normal
				String[] pos = st.split(" ");

				if (st.startsWith("/") || st.startsWith("#") || pos.length < 4)
					continue;

				// Uknown mode
				if (!pos[0].equals("v") && !pos[0].equals("f") && !pos[0].equals("vn"))
					continue;

				if(pos[0].equals("v")) mode=0;
				else if(pos[0].equals("f")) mode=1;
				else mode=2;

				if (pos.length >= 4) {
					if (mode == 0) {
						// A point is being built
						Point temp = new Point();

						temp.x = Double.parseDouble(pos[1]);
						temp.y = Double.parseDouble(pos[2]);
						temp.z = Double.parseDouble(pos[3]);

						vertexes.add(temp);
						continue;
					} else if(mode==1){
						// A triangle is being built
						String[] arg1=pos[1].split("/");
						String[] arg2=pos[2].split("/");
						String[] arg3=pos[3].split("/");
						
						String v1=arg1[0];
						String v2=arg2[0];
						String v3=arg3[0];
						
						int vertex1_index = Integer.parseInt(v1) - 1;
						int vertex2_index = Integer.parseInt(v2) - 1;
						int vertex3_index = Integer.parseInt(v3) - 1;

						face.v[0] = (Point) vertexes.get(vertex1_index).clone();
						face.v[1] = (Point) vertexes.get(vertex2_index).clone();
						face.v[2] = (Point) vertexes.get(vertex3_index).clone();

						// Also a texture and normal are defined
						if(arg1.length==3 && arg2.length==3 && arg3.length==3) {
							int n1_index=Integer.parseInt(arg1[2])-1;
							int n2_index=Integer.parseInt(arg2[2])-1;
							int n3_index=Integer.parseInt(arg3[2])-1;
							
							face.setVertexNormals(new Vector[] {
									normals.get(n1_index),normals.get(n2_index),normals.get(n3_index)
									});
						}
					}else {
						// A normal is being built
						double nx=Double.parseDouble(pos[1]);
						double ny=Double.parseDouble(pos[2]);
						double nz=Double.parseDouble(pos[3]);
						normals.add(new Vector(nx,ny,nz));
						
						continue;
					}
				}
				if (pos.length == 5 && mode == 1) {
					// A color is defined
					String[] colors = pos[4].split(",");

					int r = Integer.parseInt(colors[0]);
					int g = Integer.parseInt(colors[1]);
					int b = Integer.parseInt(colors[2]);

					face.color = new Color(r, g, b);
				}

				triangles.add(face);
			}

		} catch (Exception e) {
			System.out.println("Can't read file");
			e.printStackTrace();
		}
	}

	// Clips a triangle against the near plane
	// Requires camera coordinates
	// The coordinates relative to the camera aka transformed
	private Triangle[] clipTriangle(Triangle triangle) {
		List<Point> visible_points = new ArrayList<Point>();
		List<Point> hidden_points = new ArrayList<Point>();
		int orientation_hint=0;	// Used to reconstruct the new triangles while preserving the orientation
		
		//triangle = applyCameraTransformation(triangle);

		for(int i=0;i<triangle.v.length;i++) {
			Point p=triangle.v[i];
			if (isInBack(p)) {
				hidden_points.add(p);
			} else {
				orientation_hint+=i;
				visible_points.add(p);
			}
		}

		// All corners are visible. Thus the triangle is also visible
		if (visible_points.size() == 3)
			return new Triangle[] { triangle };
		if (visible_points.size() == 0)
			return null;

		// Two corners are visible
		if (visible_points.size() == 2) {
			// Clone in order to keep its properties
			Triangle temp1 = (Triangle) triangle.clone();
			Triangle temp2 = (Triangle) triangle.clone();

			Point intersect1 = clipLine(visible_points.get(0), hidden_points.get(0));
			Point intersect2 = clipLine(visible_points.get(1), hidden_points.get(0));
			
			if(orientation_hint%2==0) {
				temp1.v[0] = visible_points.get(0);
				temp1.v[1] = intersect1;
				temp1.v[2] = visible_points.get(1);

				temp2.v[0] = visible_points.get(1);
				temp2.v[1] = intersect1;
				temp2.v[2] = intersect2;
			}else {
				temp1.v[0] = visible_points.get(0);
				temp1.v[1] = visible_points.get(1);
				temp1.v[2] = intersect1;

				temp2.v[0] = visible_points.get(1);
				temp2.v[1] = intersect2;
				temp2.v[2] = intersect1;
			}


			return new Triangle[] { temp1, temp2 };
		}

		//// Just one corner is visible

		// Clone in order to keep its properties
		Triangle temp1 = (Triangle) triangle.clone();

		Point intersect1 = clipLine(visible_points.get(0), hidden_points.get(0));
		Point intersect2 = clipLine(visible_points.get(0), hidden_points.get(1));

		if(orientation_hint%2==0) {
			temp1.v[0] = visible_points.get(0);
			temp1.v[1] = intersect1;
			temp1.v[2] = intersect2;
		}else {
			temp1.v[0] = intersect1;
			temp1.v[1] = visible_points.get(0);
			temp1.v[2] = intersect2;
		}


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
			// temp.b = applyWorldTransformation(temp.b);
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
		// temp.a = applyWorldTransformation(temp.a);
		return temp.a;
	}

	
	// A special sort that isn't rigid
	// Takes into account than when comparing items the order may be flexible when
	// the comparison
	// returns 0 or equality
	// Better approach than default sort which will arrange those elements on the
	// same level
	private List<Triangle> looseySort(List<Triangle> list) {
		if (list.size()==0 || list.size() == 1)
			return list;
		Graph<Triangle> dependency_graph = new Graph<Triangle>();

		for (int i = 0; i < list.size() - 1; i++) {
			dependency_graph.addVertex(list.get(i));

			for (int j = i + 1; j < list.size(); j++) {
				Triangle first = list.get(i);
				Triangle second = list.get(j);
				int order = first.compareTo(second,this);

				if (order == 0)
					continue;
				if (order > 0)
					dependency_graph.addEdge(second, first);
				else
					dependency_graph.addEdge(first, second);
			}
		}

		// Also add the last triangle because it's possible it was skipped in the loop
		dependency_graph.addVertex(list.get(list.size() - 1));

		return dependency_graph.topologicalSort();
	}

	// Orders the triangles using a ZBuffer improvement without however the
	// rasterization and drawing
	// This is an improved Painter's algorithm
	// List must be camera relative triangles and clipped
	private List<Triangle> orderByZDepth(List<Triangle> list) {
		Graph<Triangle> dependency_graph = new Graph<Triangle>();
		Set<Triangle> unprocessed_triangles = new HashSet<Triangle>();

		// Sample the screen space
		for (int x = 0; x < w; x += 10) {
			for (int y = 0; y < h; y += 10) {
				List<Pair<Triangle>> heap = new ArrayList<Pair<Triangle>>();
				double virtual_x = ((double) x - w / 2) / (w / 2);
				double virtual_y = -((double) y - h / 2) / (w / 2); // We need negative here because on screen the
																	// points are flipped on y axis

				// The line origin is the "eye" aka position 0,0,0
				Point line_origin = new Point();
				line_origin.x = 0;
				line_origin.y = 0;
				line_origin.z = 0;

				// Line direction vector
				Vector line_direction = new Vector();
				line_direction.x = virtual_x;
				line_direction.y = virtual_y;
				line_direction.z = z_near; // This point is on the surface of the near plane
				line_direction = line_direction.normalize();

				for (Triangle temp : list) {
					if (isInBack(temp))
						continue;

					Vector triangle_normal = temp.getNormal();
					Point intersection = Vector.intersectPlane(triangle_normal, line_direction, temp.v[0], line_origin);

					if (intersection == null || !temp.isPointInside(intersection))
						continue;
					double dist = Math.sqrt(intersection.x * intersection.x + intersection.y * intersection.y
							+ intersection.z * intersection.z);

					// unprocessed_triangles.remove(temp);
					heap.add(new Pair<Triangle>(dist, temp));
				}

				Collections.sort(heap);
				
				if (heap.isEmpty())
					continue;
				if (heap.size() == 1) {
					dependency_graph.addVertex(heap.get(0).getValue());
					continue;
				}
				for (int i = 0; i < heap.size() - 1; i++) {
					Pair<Triangle> first = heap.get(i);
					Pair<Triangle> next_node = heap.get(i + 1);
					dependency_graph.addEdge(next_node.getValue(), first.getValue());
				}
			}
		}

//		for(Triangle temp:unprocessed_triangles) {
//			dependency_graph.addVertex(temp);
//		}

		// Sort the dependency graph topologically
		return dependency_graph.topologicalSort();
	}

	
	// Highlights clicked triangle
	// visible_triangle selects the triangle that is actually drawn on screen (which
	// might be incorrect based on the Z depth)
	// - set this to false to select the true z-visible triangle
	public void highlighTriangle(int x, int y, boolean visible_triangle) {
		// Calculate the virtual points corresponding to the screen position
		// This is calculated on the surface of the near plane
		double virtual_x = ((double) x - w / 2) / (w / 2);
		double virtual_y = -((double) y - h / 2) / (w / 2); // We need negative here because on screen the points are
															// flipped on y axis

		// The line origin is the "eye" aka position 0,0,0
		Point line_origin = new Point();
		line_origin.x = 0;
		line_origin.y = 0;
		line_origin.z = 0;

		// Line direction vector
		Vector line_direction = new Vector();
		line_direction.x = virtual_x;
		line_direction.y = virtual_y;
		line_direction.z = z_near; // This point is on the surface of the near plane
		line_direction = line_direction.normalize();

		Triangle closest_triangle = null;
		Triangle original_triangle = null;
		double closest_intersection_z = 1000;

		// We need to perform the detection on clipped triangles
		// Just using camera-transformed triangles is not enough for when the user is
		// inside an object or
		// clicking beyond a half-cut triangle
		for (Triangle temp_triangle : triangles) {
			Triangle camera_triangle=applyCameraTransformation(temp_triangle);
			Triangle[] clipped_results = clipTriangle(camera_triangle);

			// The triangle is invisble
			if (clipped_results == null)
				continue;

			for (Triangle clipped_triangle : clipped_results) {
				if (isInBack(clipped_triangle))
					continue;

				Vector triangle_normal = clipped_triangle.getNormal();
				Point intersection = Vector.intersectPlane(triangle_normal, line_direction, clipped_triangle.v[0],
						line_origin);

				if (intersection == null || !clipped_triangle.isPointInside(intersection))
					continue;

				if (visible_triangle) {
					if (closest_triangle == null || clipped_triangle.compareTo(closest_triangle,this) > 0) {
						closest_triangle = clipped_triangle;
						original_triangle = temp_triangle; // Keep a reference to the original triangle
						DEBUG_POINT = intersection;
					}
				} else {
					// We actually need to sort based to distance TO THE EYE!
					// A very subtle difference
					double dist = Math.sqrt(intersection.x * intersection.x + intersection.y * intersection.y
							+ intersection.z * intersection.z);

					if (original_triangle == null || dist < closest_intersection_z) {
						closest_intersection_z = dist;
						original_triangle = temp_triangle; // Keep a reference to the original triangle
						DEBUG_POINT = intersection;
					}
				}
			}
		}
		
		if (original_triangle != null) {
			cache_sorted_triangles=false;
			original_triangle.highlight = !original_triangle.highlight;
		}
	}

	// Adds a vector of the specified length and direction to the camera
	public void moveCameraXAxis(double amount) {
		camera.x += amount;
		if(amount!=0) cache_sorted_triangles=false;
	}
	// Adds a vector of the specified length and direction to the camera
	public void moveCameraYAxis(double amount) {
		camera.y += amount;
		if(amount!=0)cache_sorted_triangles=false;
	}
	// Adds a vector of the specified length and direction to the camera
	public void moveCameraZAxis(double amount) {
		camera.z += amount;
		if(amount!=0)cache_sorted_triangles=false;
	}
	public void rotateCameraXAxis(double amount) {
		if(amount!=camera.xAngle)cache_sorted_triangles=false;
		camera.xAngle = amount;
	}
	public void rotateCameraYAxis(double amount) {
		if(amount!=camera.yAngle)cache_sorted_triangles=false;
		camera.yAngle = amount;
	}
	public void rotateCameraZAxis(double amount) {
		if(amount!=camera.zAngle)cache_sorted_triangles=false;
		camera.zAngle = amount;
	}


	static Triangle xRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();

		for (int j = 0; j < 3; j++) {
			new_pos.v[j] = xRotate(triangle.v[j], amount_angle);
		}
		return new_pos;
	}
	static Point xRotate(Point point, double amount_angle) {
		if (amount_angle == 0)
			return (Point) point.clone();

		Point new_pos = new Point();
		amount_angle = Math.toRadians(amount_angle);

		new_pos.x = point.x;
		new_pos.y = point.y * Math.cos(amount_angle) + point.z * Math.sin(amount_angle);
		new_pos.z = point.z * Math.cos(amount_angle) - point.y * Math.sin(amount_angle);

		return new_pos;
	}
	static Triangle yRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();

		for (int j = 0; j < 3; j++) {
			new_pos.v[j] = yRotate(triangle.v[j], amount_angle);
		}
		return new_pos;
	}
	static Point yRotate(Point point, double amount_angle) {
		if (amount_angle == 0)
			return (Point) point.clone();

		Point new_pos = new Point();
		amount_angle = Math.toRadians(amount_angle);

		new_pos.x = point.x * Math.cos(amount_angle) - point.z * Math.sin(amount_angle);
		new_pos.y = point.y;
		new_pos.z = point.z * Math.cos(amount_angle) + point.x * Math.sin(amount_angle);

		return new_pos;
	}
	static Triangle zRotate(Triangle triangle, double amount_angle) {
		if (amount_angle == 0)
			return (Triangle) triangle.clone();

		Triangle new_pos = new Triangle();

		for (int j = 0; j < 3; j++) {
			new_pos.v[j] = zRotate(triangle.v[j], amount_angle);
		}
		return new_pos;
	}
	static Point zRotate(Point point, double amount_angle) {
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
		temp.z = 0;

		return temp;
	}
	// Projects the given point from the camera space onto the near plane
	// Takes into account perspective projection
	private Point projectCameraCoordinatesToNearPlane(Point p) {
		Point translated = (Point) p.clone();
		Point temp = new Point();
		temp.x =  translated.x * z_near / translated.z;
		temp.y = translated.y * z_near / translated.z;
		temp.z = z_near;

		return temp;
	}

	
	// Returns point in world coordinates
	// Takes camera relative point
	private Point applyWorldTransformation(Point p) {
		Point translated = new Point();

		// The rotation order counts!
		p = zRotate(p, camera.zAngle);
		p = xRotate(p, camera.xAngle);
		p = yRotate(p, camera.yAngle);

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
		translated = yRotate(translated, -camera.yAngle);
		translated = xRotate(translated, -camera.xAngle);
		translated = zRotate(translated, -camera.zAngle);

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
	// For vectors there is no translation. Only rotation
	private Vector applyCameraTransformation(Vector v) {
		Vector temp = (Vector) v.clone();
				
		// The rotation order counts!
		temp = new Vector(yRotate(temp, -camera.yAngle));
		temp = new Vector(xRotate(temp, -camera.xAngle));
		temp = new Vector(zRotate(temp, -camera.zAngle));

		return temp;
	}
	// Apllies the camera rotation and translation to the object given in world
	// coordinates
	private Triangle applyCameraTransformation(Triangle t) {
		Triangle translated = (Triangle) t.clone();
		translated.v[0] = applyCameraTransformation(t.v[0]);
		translated.v[1] = applyCameraTransformation(t.v[1]);
		translated.v[2] = applyCameraTransformation(t.v[2]);

		// Also rotate normals
		Vector[] n=translated.getVertexNormals();
		if(n!=null) {
			n[0] = applyCameraTransformation(n[0]);
			n[1] = applyCameraTransformation(n[1]);
			n[2] = applyCameraTransformation(n[2]);
		}
		return translated;
	}

	// Intersects in 2D space the given triangles
	// Applies coordinate projection on the triangles and calculates the center of the intersection area
	// Returns a vector in camera space to that point
	Vector cameraSpaceTriangleIntersection(Triangle a, Triangle b) {
		Vector result = new Vector();
		int points_count = 0;

		for (int i = 0; i < 3; i++) {
			Point p0 = projectCameraCoordinatesToNearPlane(a.v[i]); // This will be our origin
			Point p1 = projectCameraCoordinatesToNearPlane(a.v[(i + 1) % 3]);
			Vector prime_line_vec = new Vector();
			prime_line_vec.x = p1.x - p0.x;
			prime_line_vec.y = p1.y - p0.y;

			for (int j = 0; j < 3; j++) {
				Point p2 = projectCameraCoordinatesToNearPlane(b.v[j]);
				Point p3 = projectCameraCoordinatesToNearPlane(b.v[(j + 1) % 3]);
				Vector secund_line_vec = new Vector();
				secund_line_vec.x = p3.x - p2.x;
				secund_line_vec.y = p3.y - p2.y;

				Point intersection=Line.get_line_intersection(p0, p1, p2, p3);
				if(intersection==null) continue;
				
				points_count++;
				result.x += intersection.x;
				result.y +=intersection.y;
			}
		}

		// We assume the intersection of two triangles given to this function will only
		// generate 4 points and not less
		// Less would mean a corner is inside one of the triangles, case which would
		// already be ruled out before this function
		if (points_count == 0 || points_count <4 )
			return null;

		result.x /= points_count;
		result.y /= points_count;
		result.z=z_near;
		return result;

	}
	
	private boolean isTriangleVisible(Triangle t) {
		return new Vector(t.getCenter()).dot(t.getNormal())<0;
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
