package DevopsGUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public final class TestPanel extends JPanel{

	private static final TestPanel INSTANCE = new TestPanel();
	
	
	
	public static TestPanel getInstance(){
		return INSTANCE;
	}
	
	private JTextField testNameField;
	private JComboBox<String> requestField;
	private JTextField testUrlField;
	private JTextField boundaryField;
	private JTextField contentTypeField;
	
	private TestPanel(){
		super(new BorderLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		JPanel container = new JPanel(new GridBagLayout());
		
		JLabel testNameLabel = new JLabel("Test Name:");
		testNameField = new JTextField();
		testNameField.setColumns(30);
		
		JLabel requestLabel = new JLabel("Request:");
		requestField = new JComboBox<>(new String[]{"GET","POST"});
		
		JLabel testUrlLabel = new JLabel("Test url:");
		testUrlField = new JTextField();
		testUrlField.setColumns(30);
		
		JLabel boundaryLabel = new JLabel("(Optional) Boundary:");
		boundaryField = new JTextField();
		boundaryField.setColumns(30);
		
		JLabel contentTypeLabel = new JLabel("(Optional) Content-Type:");
		contentTypeField = new JTextField();
		contentTypeField.setColumns(30);
				
		container.add(testNameLabel,cons);
		cons.gridx++;
		JScrollPane scrollPane = new JScrollPane(testNameField);
		container.add(scrollPane,cons);
		cons.gridx = 0;
		cons.gridy++;
		container.add(requestLabel,cons);
		cons.gridx++;
		container.add(requestField,cons);
		cons.gridx = 0;
		cons.gridy++;
		container.add(testUrlLabel,cons);
		cons.gridx++;
		container.add(testUrlField,cons);
		cons.gridx = 0;
		cons.gridy++;
		container.add(boundaryLabel,cons);
		cons.gridx++;
		container.add(boundaryField,cons);
		cons.gridx = 0;
		cons.gridy++;
		container.add(contentTypeLabel,cons);
		cons.gridx++;
		container.add(contentTypeField,cons);
		cons.gridx = 0;
		cons.gridy++;
		
				
		JPanel btnContainer = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		JButton previousBtn = new JButton("Previous (Edit game and member)");
		previousBtn.addActionListener(e -> DevopsModelGUI.getInstance().setCurrentPanel(ConfigPanel.getInstance()));
		JButton nextBtn = new JButton("Next (Gamify Devops Phase)");
		
		RequestAssertionPanel next = new RequestAssertionPanel(this);
		nextBtn.addActionListener(e -> DevopsModelGUI.getInstance().setCurrentPanel(next));
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
	
	public String getTestName(){
		return testNameField.getText();
	}
	
	public String getRequestType(){
		return requestField.getItemAt(requestField.getSelectedIndex());
	}	
	
	public String getBoundary(){
		return boundaryField.getText().isEmpty() ? "--32532twtfaweafwsgfaegfawegf4" : boundaryField.getText();
	}
	
	public String getUrl(){
		return testUrlField.getText();
	}
	
	public String getContentType(){
		return contentTypeField.getText().isEmpty() ? "multipart/form-data" : contentTypeField.getText();
	}
}
