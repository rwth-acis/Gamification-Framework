// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import java.util.HashMap;
import java.util.Map;
import com.vaadin.flow.component.Component;
import java.util.Collection;
import java.util.Arrays;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ActionView extends VerticalLayout
{
    private TextField starRating;
    private ComboBox<String> scope;
    private TextField actionId;
    private TextField points;
    private TextField name;
    private TextField description;
    
    public ActionView() {
        (this.starRating = new TextField("Star rating (0-5): ")).setRequired(true);
        this.starRating.setErrorMessage("Must set a star rating from 1 to 5");
        (this.scope = new ComboBox("Scope: ")).setItems((Collection)Arrays.asList("code", "test", "deploy", "feedback", "build", "operate", "monitor"));
        this.scope.setRequired(true);
        this.scope.setErrorMessage("Must be given a DevOps phase");
        (this.actionId = new TextField("Action id: ")).setRequired(true);
        this.actionId.setErrorMessage("Must be given an action id");
        (this.points = new TextField("Points: ")).setRequired(true);
        this.points.setErrorMessage("Must be given points");
        this.name = new TextField("Name (Optional): ");
        this.description = new TextField("Description (Optional): ");
        this.add(new Component[] { (Component)this.scope, (Component)this.starRating, (Component)this.actionId, (Component)this.points, (Component)this.name, (Component)this.description });
    }
    
    public Map<String, String> getAction() {
        final Map<String, String> action = new HashMap<String, String>();
        action.put("actionid", this.actionId.getValue());
        action.put("actionname", this.name.getValue());
        action.put("actiondesc", this.description.getValue());
        action.put("actionpointvalue", this.points.getValue());
        action.put("actionnotificationcheck", "true");
        action.put("actionnotificationmessage", this.actionId.getValue());
        return action;
    }
    
    public String getScope() {
        return (String)this.scope.getValue();
    }
    
    public String getRating() {
        return this.starRating.getValue();
    }
}
