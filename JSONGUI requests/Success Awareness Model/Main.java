import javax.swing.JOptionPane;
import javax.swing.UIManager;

import SuccessAwarenessGUI.SuccessAwarenessModelGUI;

public class Main {

	static{
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "Could not set look and feel:\n" + e.getMessage()
			, "Error setting look and feel", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		SuccessAwarenessModelGUI.getInstance();
	}
	
}
