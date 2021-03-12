import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Engine.Point;
import Engine.Scene;
import Engine.Triangle;
import Engine.Vector;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import java.awt.Graphics;



public class Main {
	public Main instance;
	
	static JFrame window1 = new JFrame("sdf");
	public static int w=1000,h=500;
	public static Panel panel;
	public static Graphics graph;
	public static Keyboard keyboard;
	
	public static boolean position_control=true;
	public static double control_angle_x=0;
	public static double control_angle_y=0;
	public static double control_position_sideways=0;
	public static double control_position_altitudine=0;
	public static double control_position_backforth=0;
	
	public static Scene scene;
	
	public static void main( String args[]) throws Exception{
		scene=new Scene();
		panel=new Panel();
		keyboard=new Keyboard();
		
		panel.setSize(w,h);
		
		window1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//window1.setBounds(0, 0, 1000, 1000);
		window1.setTitle("3D Shooting Stars");
		window1.setSize(w, h);

		window1.getContentPane().add(panel,BorderLayout.CENTER);
		
        //window1.pack();
        window1.setVisible(true);
        
        window1.addMouseListener(new Mouse());
        window1.addKeyListener(keyboard);
        window1.addComponentListener(new Component());
        
		graph=panel.getGraphics();
        
		scene.setDimensions(w, h);
		
		scene.read3DObject("obj.txt");
		//scene.read3DObject("axis.obj");
		scene.read3DObject("spaceship.obj");
		//scene.read3DObject("mountains.obj");
		//scene.read3DObject("teapot.obj");
		//scene.read3DObject("angry_face.obj");
		
		while(true) {
			keyboard.virtualHoldKey();
			loop();
			Thread.sleep(20);
		}
	}

	
    public static void loop() {
    	scene.rotateCameraYAxis(control_angle_y);
    	scene.rotateCameraXAxis(control_angle_x);
    	
    	
    	// Math for keeping the controls straight even after camera rotation
    	Vector cam=scene.getCameraVec();
    	
    	double x_component=Math.cos(Math.toRadians(cam.yAngle+90))*control_position_backforth;
    	double y_component=0;
    	double z_component=Math.sin(Math.toRadians(cam.yAngle+90))*control_position_backforth;
    	
    	x_component+=Math.cos(Math.toRadians(cam.yAngle))*control_position_sideways;
    	z_component+=Math.sin(Math.toRadians(cam.yAngle))*control_position_sideways;
    	y_component=control_position_altitudine;
    	
    	scene.moveCameraXAxis(x_component);
    	scene.moveCameraYAxis(y_component);
    	scene.moveCameraZAxis(z_component);
    	panel.repaint();
    }
    
    
    
    
}
