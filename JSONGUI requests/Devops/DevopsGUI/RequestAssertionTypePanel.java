package DevopsGUI;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import i5.las2peer.apiTestModel.RequestAssertion;

public abstract class RequestAssertionTypePanel extends JPanel{

	private JPanel showingPanel; 
	private GridBagConstraints cons;
	
	public RequestAssertionTypePanel() {
		super(new FlowLayout());
;
		this.setBorder(new LineBorder(Color.black,1));
		
		showingPanel = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5, 5, 5, 5);
		cons.gridx = cons.gridy = 0;
		this.add(showingPanel);
	}
	
	public void add(JLabel label, JComponent toAdd){
		showingPanel.add(label,cons);
		cons.gridx++;
		showingPanel.add(toAdd,cons);
		cons.gridx = 0;
		cons.gridy++;
	}
	
	public abstract RequestAssertion getAsssertion();

}
