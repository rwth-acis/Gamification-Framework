package DevopsGUI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import i5.las2peer.apiTestModel.RequestAssertion;

public class TestRequestManager {

	private static final TestRequestManager INSTANCE = new TestRequestManager();
	
	public static TestRequestManager getInstance(){
		return INSTANCE;
	}
	
	private List<RequestAssertionPanel> panels;
	private TestRequestManager(){
		panels = new ArrayList<>(); 
	}
	
	
	public void add(RequestAssertionPanel newPanel){
		panels.add(newPanel);
	}
	
	public List<RequestAssertion> getRequestAssertions(){
		List<RequestAssertion> res = new ArrayList<>();
		for(RequestAssertionPanel panel: panels){
			res.add(panel.getAssertionPanel().getAsssertion());
		}
		return res;
	}
	
}
