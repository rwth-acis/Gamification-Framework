package SuccessAwarenessGUI;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.simple.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SuccessAwarenessModelGUI extends JFrame{

	private static final SuccessAwarenessModelGUI INSTANCE = new SuccessAwarenessModelGUI();

	public static SuccessAwarenessModelGUI getInstance(){
		return INSTANCE;
	}

	private JPanel currentPanel;

	private SuccessAwarenessModelGUI() {
		super("Success Awareness Model");

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

	public void gamifySuccessAwarenessModel() throws IOException{
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();
		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder()
				.url(ConfigPanel.getInstance().getHost() + "/gamification/successawarenessmodel/setup")
				.method("POST", body)
				.addHeader("Accept", "*/*")
				.addHeader("Content-Type", "text/plain")
				.addHeader("Authorization", ConfigPanel.getInstance().getAuthentication())
				.build();

		Response response = client.newCall(request).execute();
		Map<String,Map<String,Object>> content = ActionsPanelManager.getInstance().getAllGamifiedMeasures();

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("catalog", ModelPanel.getInstance().getModel());
		jsonObj.put("content", content);

		body = RequestBody.create(mediaType, jsonObj.toJSONString());
		request = new Request.Builder()
				.url(ConfigPanel.getInstance().getHost() + "/gamification/successawarenessmodel/" + ModelPanel.getInstance().getGame() + "?member=" + ModelPanel.getInstance().getMember())
				.method("POST", body)
				.addHeader("Accept", "*/*")
				.addHeader("Content-Type", "text/plain")
				.addHeader("Authorization", ConfigPanel.getInstance().getAuthentication())
				.build();
		response = client.newCall(request).execute();
		if(response.code() == 200){
			JOptionPane.showMessageDialog(null, "Gamification succeded: " + response.body().string());
		}else{
			JOptionPane.showMessageDialog(null, "Gamification failed: " + response.body().string());
		}


	}

}
