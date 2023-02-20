// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ModelView extends VerticalLayout
{
    private TextField gameId;
    private TextField memberId;
    
    public ModelView() {
        this.gameId = new TextField("Game id: ");
        this.memberId = new TextField("Member id: ");
        this.add(new Component[] { (Component)this.gameId, (Component)this.memberId });
    }
    
    public String getGameId() {
        return this.gameId.getValue();
    }
    
    public String getMemberId() {
        return this.memberId.getValue();
    }
}
