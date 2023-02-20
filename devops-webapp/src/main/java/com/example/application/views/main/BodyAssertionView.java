// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import i5.las2peer.apiTestModel.BodyAssertion;
import i5.las2peer.apiTestModel.BodyAssertionOperator;
import i5.las2peer.apiTestModel.ResponseBodyOperator;
import i5.las2peer.apiTestModel.RequestAssertion;
import com.vaadin.flow.component.Component;
import java.util.Collection;
import java.util.Arrays;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;

public class BodyAssertionView extends AssertionView
{
    private TextField value;
    private ComboBox<String> options;
    private ComboBox<String> type;
    
    public BodyAssertionView(final MainView view) {
        super(view);
        (this.options = (ComboBox<String>)new ComboBox("Options")).setItems((Collection)Arrays.asList("Has type", "Has Field", "Has list entry that", "All list entries"));
        this.options.setRequired(true);
        this.options.setErrorMessage("Must have a type");
        (this.type = (ComboBox<String>)new ComboBox("Options")).setItems((Collection)Arrays.asList("JSONObject", "JSONArray", "String", "Boolean"));
        (this.value = new TextField("Value: ")).setRequired(true);
        this.value.setErrorMessage("Must be given a status code to test");
        this.add(new Component[] { (Component)this.options, (Component)this.type, (Component)this.value, (Component)this.removeAssertion });
    }
    
    public RequestAssertion getRequestAssertion() {
        int operatorId = -1;
        final String s = (String)this.options.getValue();
        switch (s) {
            case "Has type": {
                operatorId = ResponseBodyOperator.HAS_TYPE.getId();
                break;
            }
            case "Has Field": {
                operatorId = ResponseBodyOperator.HAS_FIELD.getId();
                break;
            }
            case "Has list entry that": {
                operatorId = ResponseBodyOperator.HAS_LIST_ENTRY_THAT.getId();
                break;
            }
            case "All list entries": {
                operatorId = ResponseBodyOperator.ALL_LIST_ENTRIES.getId();
                break;
            }
        }
        final BodyAssertionOperator operator = new BodyAssertionOperator(-1, operatorId, -1, this.getType(), this.getValue(), (BodyAssertionOperator)null);
        return (RequestAssertion)new BodyAssertion(-1, -1, -1, operator);
    }
    
    public String getValue() {
        return this.value.getValue();
    }
    
    public int getType() {
        final String s = (String)this.options.getValue();
        switch (s) {
            case "JSONObject": {
                return 2;
            }
            case "JSONArray": {
                return 3;
            }
            case "String": {
                return 4;
            }
            case "Boolean": {
                return 5;
            }
            default: {
                return 1;
            }
        }
    }
}
