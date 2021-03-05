
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.time.Period;
import java.util.Arrays;

import javax.swing.JPanel;

import Engine.Triangle;

public class Panel extends JPanel {
	private static final int PREF_W = 700;
	private static final int PREF_H = 500;

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Main.scene.drawOnGraphics(g);
	}

}
