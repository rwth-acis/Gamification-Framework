// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import com.vaadin.flow.component.ClickEvent;
import java.lang.invoke.SerializedLambda;
import i5.las2peer.apiTestModel.RequestAssertion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AssertionView extends VerticalLayout
{
    protected Button removeAssertion;
    
    public AssertionView(final MainView view) {
        this.removeAssertion = new Button("Remove assertion");
        this.removeAssertion.addClickListener(e->{
        	view.removeAssertionView(AssertionView.this);
        });
    }
    
    public abstract RequestAssertion getRequestAssertion();
}
