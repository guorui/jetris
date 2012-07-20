package info.shuiyue.jetris;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class MainFrame extends Frame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MainFrame(){
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
	}
	
	public static void main(String[] args){
			MainFrame frame = new MainFrame();
			frame.add(new GameCanvas());
			frame.pack();
			frame.setVisible(true);
	}
}
