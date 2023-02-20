// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SuccessModelView extends VerticalLayout
{
    private TextArea successAwarenessModel;
    private TextField gameId;
    private TextField memberId;
    
    public SuccessModelView() {
        (this.successAwarenessModel = new TextArea("Success Awareness Model: ")).setMinWidth("200");
        this.successAwarenessModel.setMinWidth("400");
        successAwarenessModel.setWidth("400px");
        successAwarenessModel.setMinWidth("400px");
        successAwarenessModel.setMaxWidth("400px");
        successAwarenessModel.setHeight("400px");
        successAwarenessModel.setMinHeight("400px");
        successAwarenessModel.setMaxHeight("400px");
        this.gameId = new TextField("Game id: ");
        this.memberId = new TextField("Member id: ");
        this.add(new Component[] { (Component)this.successAwarenessModel, (Component)this.gameId, (Component)this.memberId });
    }
    
    public String getSuccessAwarenessModel() {
        return this.successAwarenessModel.getValue();
    }
    
    public String getGameId() {
        return this.gameId.getValue();
    }
    
    public String getMemberId() {
        return this.memberId.getValue();
    }
}
