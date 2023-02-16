package DevopsGUI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DevopsModelGUI extends JFrame{

	private static final DevopsModelGUI INSTANCE = new DevopsModelGUI();

	public static DevopsModelGUI getInstance(){
		return INSTANCE;
	}

	private JPanel currentPanel;

	private DevopsModelGUI() {
		super("Devops Lifecycle Model");

		currentPanel = ConfigPanel.getInstance();
		this.setContentPane(currentPanel);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(900,900);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void setCurrentPanel(JPanel panel){
		currentPanel = panel;
		this.setContentPane(currentPanel);
		this.repaint();
		this.revalidate();
	}

	public void gamifyDevopsModel() throws IOException{
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("text/plain");

		List<RequestAssertion> assertions = TestRequestManager.getInstance().getRequestAssertions();

		
		JSONObject pathParams = new JSONObject();
		pathParams.put("boundary", TestPanel.getInstance().getBoundary());
		pathParams.put("contentType", TestPanel.getInstance().getContentType());
		TestRequest testRequest = new TestRequest(TestPanel.getInstance().getRequestType(), TestPanel.getInstance().getUrl(),
				pathParams,-1,null,assertions);
		TestCase test = new TestCase(TestPanel.getInstance().getTestName(), Arrays.asList(testRequest ));
		
		JSONArray allTests = new JSONArray();
		Map<String,String> action = CreateActionPanel.getInstance().getAction();
		
		JSONObject testingObj = new JSONObject();
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(byteArrayOut);
		os.writeObject(test);
		testingObj.put("test",Base64.getEncoder().encodeToString(byteArrayOut.toByteArray()));

		testingObj.put("gamificationObject", action);
		
		testingObj.put("starRating", Double.parseDouble(CreateActionPanel.getInstance().getRating()));
		testingObj.put("scope", CreateActionPanel.getInstance().getScope());

		allTests.add(testingObj);
		
		JSONObject toSend = new JSONObject();
		toSend.put("allTests", allTests.toJSONString());

		RequestBody body = RequestBody.create(mediaType, toSend.toJSONString());
		Request request = new Request.Builder()
				.url(ConfigPanel.getInstance().getHost() + "/gamification/devops/" + GameMemberPanel.getInstance().getGame() + "?member=" + GameMemberPanel.getInstance().getMember())
				.method("POST", body)
				.addHeader("Accept", "*/*")
				.addHeader("Content-Type", "text/plain")
				.addHeader("Authorization", ConfigPanel.getInstance().getAuthentication())
				.build();
		Response response = client.newCall(request).execute();
		if(response.code() == 200){
			JOptionPane.showMessageDialog(null, "Gamification succeded: " + response.body().string());
		}else{
			JOptionPane.showMessageDialog(null, "Gamification failed: " + response.body().string());
		}

	}

}
