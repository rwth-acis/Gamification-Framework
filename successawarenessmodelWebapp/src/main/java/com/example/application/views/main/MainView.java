// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

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
    private SuccessModelView successModelView;
    
    private MeasureViewContainer measureContainer;
    private Button gamify;
    private Button addMeasureView;
    
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
        this.successModelView = new SuccessModelView();

        measureContainer = new MeasureViewContainer(this);
        (this.addMeasureView = new Button("Add measure")).addClickListener(e -> {
        	addMeasureView(new MeasureView(this));
        });
        (this.gamify = new Button("Gamify")).addClickListener(e -> {
        	try {
				gamify();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        });
        this.setMargin(true);
        this.add(new Component[] { (Component)this.hostName, (Component)this.identifier, (Component)this.password, (Component)this.successModelView, (Component)this.addMeasureView });
    }
    
    public void updateView() {
        this.removeAll();
        this.add(new Component[] { (Component)this.hostName, (Component)this.identifier, (Component)this.password, (Component)this.successModelView });
        this.add(measureContainer);
        this.add(new Component[] { (Component)this.addMeasureView, (Component)this.gamify });
    }
    
    public void removeMeasureView(final MeasureView measureView) {
    	measureContainer.removeMeasureView(measureView);
        this.updateView();
    }
    
    public void addMeasureView(final MeasureView view) {
    	measureContainer.addMeasureView(view);
        this.updateView();
    }
    
    public String getAuthentication() {
        return Base64.getEncoder().encodeToString(new String(this.identifier.getValue() + ":" + this.password.getValue()).getBytes());
    }
    
    public Map<String, Map<String, Object>> getAllGamifiedMeasures() {
        final Map<String, Map<String, Object>> res = new HashMap<String, Map<String, Object>>();
        for (final MeasureView view : this.measureContainer.getMeasureViews()) {
            res.put(view.getMeasureName(), view.getGamifiedMeasure());
        }
        return res;
    }
    
    public void gamify() throws IOException {
        final OkHttpClient client = new OkHttpClient().newBuilder().build();
        final MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder().url(this.hostName.getValue() + "/gamification/successawarenessmodel/setup").method("POST", body).addHeader("Accept", "*/*").addHeader("Content-Type", "text/plain").addHeader("Authorization", this.getAuthentication()).build();
        Response response = client.newCall(request).execute();
        final Map<String, Map<String, Object>> content = (Map<String, Map<String, Object>>)this.getAllGamifiedMeasures();
        final JSONObject jsonObj = new JSONObject();
        jsonObj.put((Object)"catalog", (Object)this.successModelView.getSuccessAwarenessModel());
        jsonObj.put((Object)"content", (Object)content);
        body = RequestBody.create(mediaType, jsonObj.toJSONString());
        request = new Request.Builder().url(this.hostName.getValue() + "/gamification/successawarenessmodel/" + this.successModelView.getGameId() + "?member=" + this.successModelView.getMemberId()).method("POST", body).addHeader("Accept", "*/*").addHeader("Content-Type", "text/plain").addHeader("Authorization", this.getAuthentication()).build();
        response = client.newCall(request).execute();
        
        
        System.out.println(jsonObj.toJSONString());
        String bodyResponse = response.body().string();
        if(bodyResponse.length() > 100) {
        	Notification.show("Gamification unsuccessful");
        }else if (response.code() == 200 && bodyResponse.length() < 100) {
            Notification.show("Gamification successful: " + bodyResponse);
        }
        else {
            Notification.show("Gamification unsuccessful: " + bodyResponse);
        }
    }
}
