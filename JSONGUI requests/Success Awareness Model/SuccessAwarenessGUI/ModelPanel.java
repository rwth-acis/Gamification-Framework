package SuccessAwarenessGUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public final class ModelPanel extends JPanel{

	private static final ModelPanel INSTANCE = new ModelPanel();
	
	
	
	public static ModelPanel getInstance(){
		return INSTANCE;
	}
	
	private JTextArea modelField;
	private JTextField gameField;
	private JTextField memberField;
	
	private ModelPanel(){
		super(new BorderLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		JPanel container = new JPanel(new GridBagLayout());
		
		JLabel modelLabel = new JLabel("Success Awareness Model:");
		modelField = new JTextArea();
		modelField.setColumns(30);
		modelField.setRows(10);
		
		JLabel gameLabel = new JLabel("Game id:");
		gameField = new JTextField();
		gameField.setColumns(30);
		
		JLabel memberLabel = new JLabel("Member id:");
		memberField = new JTextField();
		memberField.setColumns(30);
				
		container.add(modelLabel,cons);
		cons.gridx++;
		JScrollPane scrollPane = new JScrollPane(modelField);
		container.add(scrollPane,cons);
		cons.gridx = 0;
		cons.gridy++;
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
		previousBtn.addActionListener(e -> SuccessAwarenessModelGUI.getInstance().setCurrentPanel(ConfigPanel.getInstance()));
		JButton nextBtn = new JButton("Next (Gamify Measure)");
		
		CreateActionPanel nextPanel = new CreateActionPanel(this);
		nextBtn.addActionListener(e -> SuccessAwarenessModelGUI.getInstance().setCurrentPanel(nextPanel));
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
	
	public String getModel(){
		return modelField.getText();
	}	
	
	public String getGame(){
		return gameField.getText();
	}
	
	public String getMember(){
		return memberField.getText();
	}
}
