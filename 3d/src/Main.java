import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Engine.Point;
import Engine.Scene;
import Engine.Triangle;

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
	
	public static boolean position_control=false;
	public static double control_angle_x=0;
	public static double control_angle_y=0;
	public static double control_position_x=0;
	public static double control_position_y=0;
	public static double control_position_z=0;
	
	public static Scene scene;
	
	public static void main( String args[]) throws Exception{
		scene=new Scene();
		panel=new Panel();
		panel.setSize(w,h);
		
		window1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//window1.setBounds(0, 0, 1000, 1000);
		window1.setTitle("3D Shooting Stars");
		window1.setSize(w, h);

		window1.getContentPane().add(panel,BorderLayout.CENTER);
		
        //window1.pack();
        window1.setVisible(true);
        
        window1.addMouseListener(new Mouse());
        window1.addKeyListener(new Keyboard());
        window1.addComponentListener(new Component());
        
		graph=panel.getGraphics();
        
		scene.setDimensions(w, h);
		scene.read3DObject("obj.txt");
		
		while(true) {loop();Thread.sleep(20);}
	}

	
    public static void loop() {
    	scene.rotateSceneYAxis(control_angle_y);
    	scene.rotateSceneXAxis(control_angle_x);
    	
    	scene.translateSceneXAxis(control_position_x);
    	scene.translateSceneYAxis(control_position_y);
    	scene.translateSceneZAxis(control_position_z);
    	panel.repaint();
    }
    
    
    
    
}
