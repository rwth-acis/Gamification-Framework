package DevopsGUI;

import javax.swing.JLabel;
import javax.swing.JTextField;

import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.StatusCodeAssertion;

public class StatusCodeAssertionPanel extends RequestAssertionTypePanel{

	private JTextField statusCodeField;
	
	public 	StatusCodeAssertionPanel (){	
		JLabel statusCodeLabel = new JLabel("Status code:");
		statusCodeField = new JTextField();
		statusCodeField.setColumns(30);
	
		super.add(statusCodeLabel,statusCodeField);
	}
	
	public String getStatusCode(){
		return statusCodeField.getText();
	}

	@Override
	public RequestAssertion getAsssertion() {
		return new StatusCodeAssertion(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS, Integer.parseInt(getStatusCode()));
	}
}
