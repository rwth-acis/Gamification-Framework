// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageTitle("Main")
@Route("")
public class MainView extends VerticalLayout
{
	private TextField hostName;
	private TextField identifier;
	private PasswordField password;
	private ModelView modelView;
	
	private AssertionContainer container;
	
	private TestView testView;
	private ActionView actionView;
	private Button addStatusAssertionView;
	private Button addBodyAssertionView;
	private Button gamify;

	public MainView() {
		this.setSizeFull();
        this.hostName = new TextField("Host name (gamification framework location): ");
        hostName.setWidth("600px");
        hostName.setMinWidth("600px");
        hostName.setMaxWidth("600px");
        this.identifier = new TextField("Identifier (id of the agent in the gamification framework that is executing this action): ");
        identifier.setWidth("600px");
        identifier.setMinWidth("600px");
        identifier.setMaxWidth("600px");
        this.password = new PasswordField("Password (password of that respective agent): ");
        password.setWidth("400px");
        password.setMinWidth("400px");
        password.setMaxWidth("400px");
		this.modelView = new ModelView();
		this.testView = new TestView();
		this.actionView = new ActionView();

		container = new AssertionContainer(this);
		(this.addStatusAssertionView = new Button("Add status code assertion")).addClickListener(e -> addAssertionView(new StatusCodeAssertionView(this)));
		(this.addBodyAssertionView = new Button("Add body assertion")).addClickListener(e -> addAssertionView(new BodyAssertionView(this)));
		(this.gamify = new Button("Gamify")).addClickListener(e -> {
			try {
				gamify();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		this.setMargin(true);
		this.add(new Component[] { (Component)this.hostName, (Component)this.identifier, (Component)this.password, (Component)this.modelView, (Component)this.testView,container, (Component)this.addStatusAssertionView, (Component)this.addBodyAssertionView, (Component)this.actionView, (Component)this.gamify });
	}

	public List<RequestAssertion> getRequestAssertions() {
		final List<RequestAssertion> res = new ArrayList<RequestAssertion>();
		for (final AssertionView panel : this.container.getAssertionViews()) {
			res.add(panel.getRequestAssertion());
		}
		return res;
	}

	public void updateView() {
		this.removeAll();
		this.add(new Component[] { (Component)this.hostName, (Component)this.identifier, (Component)this.password, (Component)this.modelView, (Component)this.testView });
		this.add(container);
		this.add(new Component[] { (Component)this.addStatusAssertionView, (Component)this.addBodyAssertionView, (Component)this.actionView, (Component)this.gamify });
	}

	public void removeAssertionView(final AssertionView assertionView) {
		this.container.removeAssertionView(assertionView);
		this.updateView();
	}

	public void addAssertionView(final AssertionView view) {
		this.container.addAssertionView(view);
		this.updateView();
	}

	public String getAuthentication() {
		return Base64.getEncoder().encodeToString((this.identifier.getValue() + ":" +  this.password.getValue()).getBytes());
	}

	public void gamify() throws IOException {
		final OkHttpClient client = new OkHttpClient().newBuilder().build();
		final MediaType mediaType = MediaType.parse("text/plain");
		final List<RequestAssertion> assertions = (List<RequestAssertion>)this.getRequestAssertions();
		final JSONObject pathParams = new JSONObject();
		pathParams.put((Object)"boundary", (Object)this.testView.getBoundary());
		pathParams.put((Object)"contentType", (Object)this.testView.getContentType());
		final TestRequest testRequest = new TestRequest(this.testView.getRequest(), this.testView.getTestUrl(), pathParams, -1, (String)null, (List)assertions);
		final TestCase test = new TestCase(this.testView.getTestName(), (List)Arrays.asList(testRequest));
		final JSONArray allTests = new JSONArray();
		final Map<String, String> action = (Map<String, String>)this.actionView.getAction();
		final JSONObject testingObj = new JSONObject();
		final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		final ObjectOutputStream os = new ObjectOutputStream(byteArrayOut);
		os.writeObject(test);

		testingObj.put((Object)"test", (Object)Base64.getEncoder().encodeToString(byteArrayOut.toByteArray()));
		testingObj.put((Object)"gamificationObject", (Object)action);
		testingObj.put((Object)"starRating", (Object)Double.parseDouble(this.actionView.getRating()));
		testingObj.put((Object)"scope", (Object)this.actionView.getScope());
		allTests.add((Object)testingObj);
		final JSONObject toSend = new JSONObject();
		toSend.put((Object)"allTests", (Object)allTests.toJSONString());
		final RequestBody body = RequestBody.create(mediaType, toSend.toJSONString());

		System.out.println(toSend.toJSONString());
		System.out.println(this.hostName.getValue() + "/gamification/devops/" + this.modelView.getGameId() + this.modelView.getMemberId());

		final Request request = new Request.Builder().url(this.hostName.getValue() + "/gamification/devops/" + this.modelView.getGameId() + "?member=" + this.modelView.getMemberId()).method("POST", body).addHeader("Accept", "*/*").addHeader("Content-Type", "text/plain").addHeader("Authorization", this.getAuthentication()).build();
		final Response response = client.newCall(request).execute();
		String bodyRes = response.body().string(); 
		System.out.println(bodyRes);
		if(bodyRes.length()  > 100) {
			Notification.show("Gamification unsuccessful");
		}else if (response.code() == 200 || !response.isSuccessful()) {
			Notification.show("Gamification successed: " + bodyRes);
		}
		else {
			Notification.show("Gamification unsuccessful: " + bodyRes);
		}
	}
}
