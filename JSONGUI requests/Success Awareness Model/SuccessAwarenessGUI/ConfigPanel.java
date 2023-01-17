package SuccessAwarenessGUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class ConfigPanel extends JPanel{

	private static final ConfigPanel INSTANCE = new ConfigPanel();
	
	public static ConfigPanel getInstance(){
		return INSTANCE;
	}
	
	private JTextField hostField;
	private JTextField idField;
	private JPasswordField passwordField;
	
	private ConfigPanel(){
		super(new BorderLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		JPanel container = new JPanel(new GridBagLayout());
		
		JLabel hostLabel = new JLabel("Host name:");
		hostField = new JTextField();
		hostField.setColumns(30);
		
		
		JLabel idLabel = new JLabel("Identifier:");
		idField = new JTextField();
		idField.setColumns(30);
		
		JLabel passwordLabel = new JLabel("Password:");
		passwordField = new JPasswordField();
		passwordField .setColumns(30);
		
		
		container.add(hostLabel,cons);
		cons.gridx++;
		container.add(hostField,cons);
		cons.gridx = 0;
		cons.gridy++;
		
		container.add(idLabel,cons);
		cons.gridx++;
		container.add(idField,cons);
		cons.gridx = 0;
		cons.gridy++;
		
		container.add(passwordLabel,cons);
		cons.gridx++;
		container.add(passwordField,cons);
		cons.gridx = 0;
		cons.gridy++;
		
		
		JPanel wrapper = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		JButton nextBtn = new JButton("Next (Add success awareness model, game and member)");
		nextBtn.addActionListener(e -> SuccessAwarenessModelGUI.getInstance().setCurrentPanel(ModelPanel.getInstance()));
		
		wrapper.add(container,cons);
		cons.gridy++;
		wrapper.add(nextBtn,cons);
		
		add(wrapper,BorderLayout.CENTER);
	}
	
	public String getHost(){
		return hostField.getText();
	}
	
	public String getAuthentication(){
		return Base64.getEncoder().encodeToString(new String(idField.getText() + ":" + passwordField.getPassword()).getBytes());
	}
	
}
