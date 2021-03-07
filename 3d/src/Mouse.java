import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Mouse implements MouseListener{

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getButton()==MouseEvent.BUTTON3) {
			//Right click here
			Main.scene.highlighTriangle(arg0.getX()-13, arg0.getY()-35, false);
		}else {
			//Left click
			if(arg0.getClickCount()==2) {
				System.out.println("Dbl");
				Main.position_control=!Main.position_control;
				System.out.println("cam: "+Main.position_control);
			}else {
				System.out.println("Clck");
			}
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

}
