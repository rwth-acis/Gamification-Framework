// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.RequestAssertion;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;

public class StatusCodeAssertionView extends AssertionView
{
    private TextField statusCode;
    
    public StatusCodeAssertionView(final MainView view) {
        super(view);
        (this.statusCode = new TextField("Status Code: ")).setRequired(true);
        this.statusCode.setErrorMessage("Must be given a status code to test");
        this.add(new Component[] { (Component)this.statusCode, (Component)this.removeAssertion });
    }
    
    public RequestAssertion getRequestAssertion() {
        return (RequestAssertion)new StatusCodeAssertion(0, Integer.parseInt(this.getStatusCode()));
    }
    
    public String getStatusCode() {
        return this.statusCode.getValue();
    }
}
