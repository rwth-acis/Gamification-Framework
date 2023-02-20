// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class AssertionContainer extends HorizontalLayout
{
    private List<AssertionView> views; 
    private MainView mainView;
    
    public AssertionContainer(final MainView view) {
    	this.setWidth("1000px");
    	views = new ArrayList<>();
    	mainView = view;
    }
    
    public void removeAssertionView(final AssertionView measureView) {
        this.views.remove(measureView);
        updateView();
        mainView.updateView();
    }
    
    public void addAssertionView(final AssertionView view) {
        this.views.add(view);
        updateView();
        mainView.updateView();
    }
    
    public List<AssertionView> getAssertionViews() {
    	return views;
    }
    
    private void updateView() {
    	this.removeAll();
    	for (final AssertionView view : getAssertionViews()) {
            this.add(view);
        }
    }
    
}
