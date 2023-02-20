// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import java.util.Arrays;
import java.util.Collection;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class TestView extends VerticalLayout
{
    private TextField testName;
    private TextField testUrl;
    private TextField boundary;
    private TextField contentType;
    private ComboBox<String> request;
    private Label testLabel;
    
    public TestView() {
    	
        (this.testName = new TextField("Test name: ")).setRequired(true);
        (this.testUrl = new TextField()).setErrorMessage("Must be given a test name");
        (this.testUrl = new TextField("Test url (the route to the test): ")).setRequired(true);
        this.testUrl.setErrorMessage("Must be given a test url");
        
        this.testUrl.setWidth("300px");
        this.testUrl.setMaxWidth("300px");
        this.testUrl.setMinWidth("300px");
        
        (this.request = new ComboBox("Request Type")).setItems((Collection)Arrays.asList("GET", "POST", "DELETE", "PUT"));
        this.boundary = new TextField("Boundary (if your request uses a boundary to separate the input): ");
        this.boundary.setWidth("500px");
        this.boundary.setMaxWidth("500px");
        this.boundary.setMinWidth("500px");
        
        this.boundary.setValue("--32532twtfaweafwsgfaegfawegf4");
        this.contentType = new TextField("Content-Type (content-type of your test REST request): ");
        this.contentType.setValue("multipart/form-data");
        
        this.add(new Component[] { (Component)this.testName, (Component)this.testUrl, (Component)this.boundary, (Component)this.contentType, (Component)this.request });
    }
    
    public String getTestName() {
        return this.testName.getValue();
    }
    
    public String getTestUrl() {
        return this.testUrl.getValue();
    }
    
    public String getBoundary() {
        return this.boundary.getValue();
    }
    
    public String getContentType() {
        return this.contentType.getValue();
    }
    
    public String getRequest() {
        return (String)this.request.getValue();
    }
}
