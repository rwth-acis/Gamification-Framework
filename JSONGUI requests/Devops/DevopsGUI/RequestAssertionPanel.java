package DevopsGUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class RequestAssertionPanel extends JPanel{
	
	private JComboBox<String> requestField;
	private JTextField testUrlField;
	private JTextField boundaryField;
	private JTextField contentTypeField;
	
	private JPanel assertionPanelWrapper;
	private RequestAssertionTypePanel assertionPanel;
	private int currentPanel;
	
	private JPanel previousPanel;
	private JPanel nextPanel;
	

	public RequestAssertionPanel(JPanel previousPanel){
		super(new BorderLayout());
		TestRequestManager.getInstance().add(this);

		
		this.previousPanel = previousPanel;
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		JPanel container = new JPanel(new GridBagLayout());
			
		assertionPanelWrapper = new JPanel();
		assertionPanel = new StatusCodeAssertionPanel();
		currentPanel = 0;
		assertionPanelWrapper.add(assertionPanel);
		JLabel requestLabel = new JLabel("Request:");
		requestField = new JComboBox<>(new String[]{"Status Code Assertion","Body Assertion"});
		requestField.addActionListener(e -> {
			if(requestField.getSelectedIndex() != currentPanel){
				currentPanel = requestField.getSelectedIndex();
				switch(currentPanel){
				case 0:
					assertionPanel = new StatusCodeAssertionPanel();
					break;
				case 1:
					assertionPanel = new BodyAssertionPanel();
					break;
				}
				assertionPanelWrapper.removeAll();
				assertionPanel.repaint();
				assertionPanel.revalidate();
				assertionPanelWrapper.add(assertionPanel);
				RequestAssertionPanel.this.repaint();
				RequestAssertionPanel.this.revalidate();
			}
		});
				
		container.add(requestLabel,cons);
		cons.gridx++;
		container.add(requestField,cons);
		
		JPanel btnContainer = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		String previousText = "";
		if(previousPanel instanceof TestPanel)
			previousText = "Previous (Edit test settings)";
		else
			previousText = "Previous (Edit test assertion)";
		
		JButton previousBtn = new JButton(previousText);
		previousBtn.addActionListener(e -> DevopsModelGUI.getInstance().setCurrentPanel(previousPanel));
		JButton nextBtn = new JButton("Next (Continue creating test requests)");
		nextBtn.addActionListener(e ->{
			if(nextPanel == null)
				nextPanel = new RequestAssertionPanel(RequestAssertionPanel.this);
			DevopsModelGUI.getInstance().setCurrentPanel(nextPanel);	
		});
		JButton createActionBtn = new JButton("Next (Create Action)");
		createActionBtn.addActionListener(e ->{ 
			if(nextPanel == null)
				nextPanel = CreateActionPanel.create(RequestAssertionPanel.this);
			DevopsModelGUI.getInstance().setCurrentPanel(nextPanel);
		});
		
		btnContainer.add(previousBtn,cons);
		cons.gridx++;
		btnContainer.add(nextBtn,cons);
		cons.gridx++;
		btnContainer.add(createActionBtn,cons);
				
		JPanel wrapper = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		wrapper.add(container,cons);
		cons.gridy++;
		wrapper.add(assertionPanelWrapper,cons);
		cons.gridy++;
		wrapper.add(btnContainer,cons);
		
		add(wrapper,BorderLayout.CENTER);
	}
	
	public String getRequestType(){
		return requestField.getItemAt(requestField.getSelectedIndex()).toString();
	}	
	
	public String getBoundary(){
		return boundaryField.getText();
	}
	
	public String getUrl(){
		return testUrlField.getText();
	}
	
	public String getContentType(){
		return contentTypeField.getText();
	}
	
	public String getMember(){
		return testUrlField.getText();
	}

	public RequestAssertionTypePanel getAssertionPanel() {
		return assertionPanel;
	}
	
	
}
