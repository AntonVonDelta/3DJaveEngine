import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener {

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		double turn_amount=10;
		double move_amount=0.15;
		
		switch(arg0.getKeyCode()) {
		case KeyEvent.VK_A:
			if(Main.position_control) Main.control_position_x=move_amount;
			else Main.control_angle_y=turn_amount;
			break;
			
			
		case KeyEvent.VK_D:
			if(Main.position_control) Main.control_position_x=-move_amount;
			else Main.control_angle_y=-turn_amount;
			break;
			
		case KeyEvent.VK_W:
			if(Main.position_control) Main.control_position_z=-move_amount;
			else Main.control_angle_x=turn_amount;
			break;
		
			
		case KeyEvent.VK_S:
			if(Main.position_control) Main.control_position_z=move_amount;
			else Main.control_angle_x=-turn_amount;
			break;
			
		case KeyEvent.VK_SPACE:
			Main.control_position_y=-move_amount;
			break;
			
		case KeyEvent.VK_SHIFT:
			Main.control_position_y=move_amount;
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		Main.control_angle_x=0;
		Main.control_angle_y=0;
		
		Main.control_position_x=0;
		Main.control_position_y=0;
		Main.control_position_z=0;
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
