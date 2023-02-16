package DevopsGUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class CreateActionPanel extends JPanel{

	private static CreateActionPanel INSTANCE;
	
	public static CreateActionPanel create(JPanel previous){
		INSTANCE = new CreateActionPanel(previous);
		return INSTANCE;
	}
	
	public static CreateActionPanel getInstance(){
		return INSTANCE;
	}
	

	private JPanel previousPanel;
	private JPanel nextPanel;

	private JComboBox<String> scopeField;
	private JTextField starRatingField;
	private JTextField actionIdField;
	private JTextField pointsField;
	private JTextField nameField;
	private JTextField descriptionField;

	public CreateActionPanel(JPanel previousPanel){
		super(new BorderLayout());
		this.previousPanel = previousPanel;

		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;

		JPanel container = new JPanel(new GridBagLayout());

		JLabel scopeLabel = new JLabel("Scope:");
		scopeField = new JComboBox<>(new String[]{"code","build","test","release","deploy","operator","monitor"});

		container.add(scopeLabel,cons);
		cons.gridx++;
		container.add(scopeField,cons);
		cons.gridx = 0;
		cons.gridy++;

		JLabel ratingLabel = new JLabel("Star rating (0-5):");
		starRatingField = new JTextField();
		starRatingField.setColumns(30);

		JLabel actionIdLabel = new JLabel("Action id:");
		actionIdField = new JTextField();
		actionIdField.setColumns(30);

		JLabel pointsLabel = new JLabel("Points:");
		pointsField = new JTextField();
		pointsField.setColumns(30);

		JLabel nameLabel = new JLabel("Name (Optional):");
		nameField = new JTextField();
		nameField.setColumns(30);

		JLabel descriptionLabel = new JLabel("Description (Optional):");
		descriptionField = new JTextField();
		descriptionField.setColumns(30);

		container.add(scopeLabel,cons);
		cons.gridx++;
		container.add(scopeField,cons);
		cons.gridx = 0;
		cons.gridy++;

		container.add(ratingLabel,cons);
		cons.gridx++;
		container.add(starRatingField,cons);
		cons.gridx = 0;
		cons.gridy++;

		container.add(actionIdLabel,cons);
		cons.gridx++;
		container.add(actionIdField,cons);
		cons.gridx = 0;
		cons.gridy++;

		container.add(pointsLabel,cons);
		cons.gridx++;
		container.add(pointsField,cons);
		cons.gridx = 0;
		cons.gridy++;

		container.add(nameLabel,cons);
		cons.gridx++;
		container.add(nameField,cons);
		cons.gridx = 0;
		cons.gridy++;

		container.add(descriptionLabel,cons);
		cons.gridx++;
		container.add(descriptionField,cons);
		cons.gridx = 0;
		cons.gridy++;


		JPanel btnContainer = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;
		
		JButton previousBtn = new JButton("Previous (Edit test assertion)");
		previousBtn.addActionListener(e -> DevopsModelGUI.getInstance().setCurrentPanel(previousPanel));

		JButton gamifyBtn = new JButton("Gamify devops model");
		gamifyBtn.addActionListener(e ->{
			try{	
				DevopsModelGUI.getInstance().gamifyDevopsModel();

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		btnContainer.add(previousBtn,cons);
		cons.gridx++;
		btnContainer.add(gamifyBtn,cons);

		JPanel wrapper = new JPanel(new GridBagLayout());
		cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;

		wrapper.add(container,cons);
		cons.gridy++;
		wrapper.add(btnContainer,cons);

		add(wrapper,BorderLayout.CENTER);
	}

	public Map<String,String> getAction(){
		if(!actionIdField.getText().isEmpty() && !pointsField.getText().isEmpty() && !starRatingField.getText().isEmpty()){
			Map<String,String> action = new HashMap<>();
			action.put("actionid", actionIdField.getText());
			action.put("actionname", nameField.getText().isEmpty() ? "name" : nameField.getText());
			action.put("actiondesc", descriptionField.getText().isEmpty() ? "desc" : descriptionField.getText());
			action.put("actionpointvalue", pointsField.getText());
			action.put("actionnotificationcheck", "true");
			action.put("actionnotificationmessage", actionIdField.getText() + " notification message");
			return action;
		}
		return null;
	}

	public String getScope(){
		return scopeField.getItemAt(scopeField.getSelectedIndex());
	}
	
	public String getRating(){
		return starRatingField.getText();
	}

}

