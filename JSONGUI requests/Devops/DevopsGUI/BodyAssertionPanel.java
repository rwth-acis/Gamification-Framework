package DevopsGUI;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import i5.las2peer.apiTestModel.BodyAssertion;
import i5.las2peer.apiTestModel.BodyAssertionOperator;
import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.ResponseBodyOperator;

public class BodyAssertionPanel extends RequestAssertionTypePanel{

	private JComboBox<String> optionsField;
	private JComboBox<String> typeField;
	private JTextField valueField;
	
	public 	BodyAssertionPanel (){	
		JLabel optionsLabel = new JLabel("Options:");
		optionsField = new JComboBox<>(new String[]{"Has type","Has field", "Has list entry that","All list entries"});
		optionsField.addActionListener(e -> {
			switch(optionsField.getSelectedIndex()){
			case 0:
//				BodyAssertionPanel.this.valueField.setText("");
				BodyAssertionPanel.this.typeField.setEnabled(true);
				BodyAssertionPanel.this.valueField.setEnabled(false);
				break;
			case 1:
//				BodyAssertionPanel.this.typeField.setText("");
				BodyAssertionPanel.this.typeField.setEnabled(false);
				BodyAssertionPanel.this.valueField.setEnabled(true);
				break;
			case 2:
			case 3:
				BodyAssertionPanel.this.typeField.setEnabled(true);
				BodyAssertionPanel.this.valueField.setEnabled(true);
				break;
			}
		});
		
		JLabel typeLabel = new JLabel("Type:");
		typeField = new JComboBox<>(new String[]{"JSONObject","JSONArray","String","Boolean"});
		
		JLabel valueLabel = new JLabel("Value:");
		valueField = new JTextField();
		valueField.setColumns(30);
		valueField.setEnabled(false);
		
	
		super.add(optionsLabel,optionsField);
		super.add(typeLabel,typeField);
		super.add(valueLabel,valueField);
	}
	
	public String getValue(){
		return valueField.getText();
	}
	
	public int getType(){
		return typeField.getSelectedIndex() + 2;
	}

	@Override
	public RequestAssertion getAsssertion() {
		int operatorId = -1;
		switch(optionsField.getSelectedIndex()) {
		case 0:
			operatorId = ResponseBodyOperator.HAS_TYPE.getId();
			break;
		case 1:
			operatorId = ResponseBodyOperator.HAS_FIELD.getId();
			break;	
		case 2:
			operatorId = ResponseBodyOperator.HAS_LIST_ENTRY_THAT.getId();
			break;
		case 3:
			operatorId = ResponseBodyOperator.ALL_LIST_ENTRIES.getId();
			break;
		}
		BodyAssertionOperator operator = new BodyAssertionOperator(-1, operatorId, -1, getType(),getValue(), null);
		return new BodyAssertion(-1,-1,-1,operator);
	}
}
