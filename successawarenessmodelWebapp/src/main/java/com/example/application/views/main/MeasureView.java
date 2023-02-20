// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import com.vaadin.flow.component.ClickEvent;
import java.lang.invoke.SerializedLambda;
import java.util.HashMap;
import java.util.Map;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class MeasureView extends VerticalLayout
{
    private TextField measureName;
    private TextField valueToTrigger;
    private TextField actionId;
    private TextField points;
    private TextField name;
    private TextField description;
    private Button removeMeasure;
    private MainView mainView;
    
    public MeasureView(final MainView mainView) {
        this.measureName = new TextField("Measure name: ");
        (this.valueToTrigger = new TextField("Value to trigger: ")).setRequired(true);
        this.valueToTrigger.setErrorMessage("Must be given a value to trigger/goal");
        (this.actionId = new TextField("Action id: ")).setRequired(true);
        this.actionId.setErrorMessage("Must be given an action id");
        (this.points = new TextField("Points: ")).setRequired(true);
        this.points.setErrorMessage("Must be given points");
        this.name = new TextField("Name (Optional): ");
        this.description = new TextField("Description (Optional): ");
        this.removeMeasure = new Button("Remove measure");
        this.mainView = mainView;
        this.removeMeasure.addClickListener(e -> mainView.removeMeasureView(this));
        this.add(new Component[] { (Component)this.measureName, (Component)this.valueToTrigger, (Component)this.actionId, (Component)this.points, (Component)this.name, (Component)this.description, (Component)this.removeMeasure });
    }
    
    public Map<String, Object> getGamifiedMeasure() {
        final Map<String, String> action = new HashMap<String, String>();
        action.put("actionid", this.actionId.getValue());
        action.put("actionname", this.name.getValue());
        action.put("actiondesc", this.description.getValue());
        action.put("actionpointvalue", this.points.getValue());
        action.put("actionnotificationcheck", "true");
        action.put("actionnotificationmessage", "notif" + this.actionId.getValue());
        final Map<String, Object> gamifiedMeasure = new HashMap<String, Object>();
        gamifiedMeasure.put("service", "action");
        gamifiedMeasure.put("valueToTrigger", this.valueToTrigger.getValue());
        gamifiedMeasure.put("gamificationObject", action);
        return gamifiedMeasure;
    }
    
    public String getMeasureName() {
        return this.measureName.getValue();
    }
    
    public String getValueToTrigger() {
        return this.valueToTrigger.getValue();
    }
    
    public String getActionId() {
        return this.actionId.getValue();
    }
    
    public String getPoints() {
        return this.points.getValue();
    }
    
    public String getName() {
        return this.name.getValue();
    }
    
    public String getDescription() {
        return this.description.getValue();
    }
}
