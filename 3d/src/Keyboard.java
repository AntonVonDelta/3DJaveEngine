import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Keyboard implements KeyListener {
	Map<Integer,Boolean> pressed_keys=new HashMap<Integer,Boolean>();
	
	public void virtualHoldKey() {
		double turn_amount=10;
		double move_amount=0.15;
		
		
		if(pressed_keys.getOrDefault(KeyEvent.VK_A,false)) {
			if(Main.position_control) Main.control_position_sideways=-move_amount;
			else Main.control_angle_y+=turn_amount;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_D,false)) {
			if(Main.position_control) Main.control_position_sideways=move_amount;
			else Main.control_angle_y-=turn_amount;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_W,false)) {
			if(Main.position_control) Main.control_position_backforth=move_amount;
			else Main.control_angle_x+=turn_amount;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_S,false)) {
			if(Main.position_control) Main.control_position_backforth=-move_amount;
			else Main.control_angle_x-=turn_amount;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_SPACE,false)) {
			Main.control_position_altitudine=move_amount;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_SHIFT,false)) {
			Main.control_position_altitudine=-move_amount;
		}
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
		pressed_keys.put(arg0.getKeyCode(),true);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if(pressed_keys.getOrDefault(KeyEvent.VK_A,false)) {
			Main.control_position_sideways=0;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_D,false)) {
			Main.control_position_sideways=0;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_W,false)) {
			Main.control_position_backforth=0;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_S,false)) {
			Main.control_position_backforth=0;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_SPACE,false)) {
			Main.control_position_altitudine=0;
		}
		if(pressed_keys.getOrDefault(KeyEvent.VK_SHIFT,false)) {
			Main.control_position_altitudine=0;
		}
		
		pressed_keys.put(arg0.getKeyCode(),false);
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
