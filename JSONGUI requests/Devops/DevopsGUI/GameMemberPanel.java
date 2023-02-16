package DevopsGUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class GameMemberPanel extends JPanel{

	private static final GameMemberPanel INSTANCE = new GameMemberPanel();
	
	
	
	public static GameMemberPanel getInstance(){
		return INSTANCE;
	}
	
	private JTextField gameField;
	private JTextField memberField;
	
	private GameMemberPanel(){
		super(new BorderLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		JPanel container = new JPanel(new GridBagLayout());

		JLabel gameLabel = new JLabel("Game id:");
		gameField = new JTextField();
		gameField.setColumns(30);
		
		JLabel memberLabel = new JLabel("Member id:");
		memberField = new JTextField();
		memberField.setColumns(30);
				
		
		container.add(gameLabel,cons);
		cons.gridx++;
		container.add(gameField,cons);
		cons.gridx = 0;
		cons.gridy++;
		container.add(memberLabel,cons);
		cons.gridx++;
		container.add(memberField,cons);
		cons.gridx = 0;
		cons.gridy++;
		
				
		JPanel btnContainer = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		JButton previousBtn = new JButton("Previous (Edit configurations)");
		previousBtn.addActionListener(e -> DevopsModelGUI.getInstance().setCurrentPanel(ConfigPanel.getInstance()));
		JButton nextBtn = new JButton("Next (Create Test case)");
		
		nextBtn.addActionListener(e -> DevopsModelGUI.getInstance().setCurrentPanel(TestPanel.getInstance()));
		btnContainer.add(previousBtn,cons);
		cons.gridx++;
		btnContainer.add(nextBtn,cons);
				
		JPanel wrapper = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		wrapper.add(container,cons);
		cons.gridy++;
		wrapper.add(btnContainer,cons);
		
		add(wrapper,BorderLayout.CENTER);
	}
	
	public String getGame(){
		return gameField.getText();
	}
	
	public String getMember(){
		return memberField.getText();
	}
}
