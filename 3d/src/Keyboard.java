import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Keyboard implements KeyListener {
	Set<Integer> pressed_keys=new HashSet<Integer>();
	
	public void virtualHoldKey() {
		double turn_amount=10;
		double move_amount=0.15;
		
		
		if(pressed_keys.contains(KeyEvent.VK_A)) {
			if(Main.position_control) Main.control_position_sideways=-move_amount;
			else Main.control_angle_y+=turn_amount;
		}
		if(pressed_keys.contains(KeyEvent.VK_D)) {
			if(Main.position_control) Main.control_position_sideways=move_amount;
			else Main.control_angle_y-=turn_amount;
		}
		if(pressed_keys.contains(KeyEvent.VK_W)) {
			if(Main.position_control) Main.control_position_backforth=move_amount;
			else Main.control_angle_x+=turn_amount;
		}
		if(pressed_keys.contains(KeyEvent.VK_S)) {
			if(Main.position_control) Main.control_position_backforth=-move_amount;
			else Main.control_angle_x-=turn_amount;
		}
		if(pressed_keys.contains(KeyEvent.VK_SPACE)) {
			Main.control_position_altitudine=move_amount;
		}
		if(pressed_keys.contains(KeyEvent.VK_SHIFT)) {
			Main.control_position_altitudine=-move_amount;
		}
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
		pressed_keys.add(arg0.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
//		Main.control_angle_x=0;
//		Main.control_angle_y=0;
		
		Main.control_position_sideways=0;
		Main.control_position_altitudine=0;
		Main.control_position_backforth=0;
		
		pressed_keys.clear();
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
