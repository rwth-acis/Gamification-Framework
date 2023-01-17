package SuccessAwarenessGUI;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class CreateActionPanel extends JPanel{


	private JPanel previousPanel;
	private JPanel nextPanel;

	private JTextField measureField;
	private JTextField valueToTriggerField;
	private JTextField actionIdField;
	private JTextField pointsField;
	private JTextField nameField;
	private JTextField descriptionField;

	public CreateActionPanel(JPanel previousPanel){
		super(new BorderLayout());
		ActionsPanelManager.getInstance().add(this);
		this.previousPanel = previousPanel;

		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5,5,5,5);
		cons.gridx = cons.gridy = 0;

		JPanel container = new JPanel(new GridBagLayout());

		JLabel measureLabel = new JLabel("Measure name:");
		measureField = new JTextField();
		measureField.setColumns(30);

		container.add(measureLabel,cons);
		cons.gridx++;
		container.add(measureField,cons);
		cons.gridx = 0;
		cons.gridy++;

		JLabel valueToTriggerLabel = new JLabel("Value to trigger/Goal:");
		valueToTriggerField = new JTextField();
		valueToTriggerField.setColumns(30);

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

		container.add(measureLabel,cons);
		cons.gridx++;
		container.add(measureField,cons);
		cons.gridx = 0;
		cons.gridy++;

		container.add(valueToTriggerLabel,cons);
		cons.gridx++;
		container.add(valueToTriggerField,cons);
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
		
		String previousText = "";
		if(previousPanel instanceof ModelPanel)
			previousText = "Previous (Edit success awareness model, game and member)";
		else
			previousText = "Previous (Edit gamified measure)";
		JButton previousBtn = new JButton(previousText);
		previousBtn.addActionListener(e -> SuccessAwarenessModelGUI.getInstance().setCurrentPanel(previousPanel));

		JButton nextBtn = new JButton("Continue gamifying measures");
		nextBtn.addActionListener(e ->{

			if(CreateActionPanel.this.nextPanel == null)
				CreateActionPanel.this.nextPanel = new CreateActionPanel(CreateActionPanel.this);

			SuccessAwarenessModelGUI.getInstance().setCurrentPanel(CreateActionPanel.this.nextPanel);	
		});

		JButton gamifyBtn = new JButton("Gamify success awareness model");
		gamifyBtn.addActionListener(e ->{
			try{	
				SuccessAwarenessModelGUI.getInstance().gamifySuccessAwarenessModel();

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		btnContainer.add(previousBtn,cons);
		cons.gridx++;
		btnContainer.add(nextBtn,cons);
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

	public Map<String,Object> getGamifiedMeasure(){
		if(!actionIdField.getText().isEmpty() && !pointsField.getText().isEmpty() && !valueToTriggerField.getText().isEmpty()){
			Map<String,String> action = new HashMap<>();
			action.put("actionid", actionIdField.getText());
			action.put("actionname", nameField.getText());
			action.put("actiondesc", descriptionField.getText());
			action.put("actionpointvalue", pointsField.getText());
			action.put("actionnotificationcheck", "true");
			action.put("actionnotificationmessage", actionIdField.getText() + " notification message");


			Map<String,Object> gamifiedMeasure = new HashMap<>();
			gamifiedMeasure.put("service", "action");
			gamifiedMeasure.put("valueToTrigger", valueToTriggerField.getText());
			gamifiedMeasure.put("gamificationObject", action);

			return gamifiedMeasure;
		}
		return null;
	}

	public String getMeasureName(){
		if(measureField.getText().isEmpty())
			return null;
		return measureField.getText();
	}

	public JPanel getNext(){
		return nextPanel;
	}

	public JPanel getPrevious(){
		return previousPanel;
	}
}

