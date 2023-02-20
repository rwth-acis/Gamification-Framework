// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application.views.main;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class MeasureViewContainer extends HorizontalLayout
{
    private List<MeasureView> measureViews;
    private MainView mainView;
    
    public MeasureViewContainer(final MainView mainView) {
    	measureViews = new ArrayList<>();
    	this.mainView = mainView;
    }
    

    public void removeMeasureView(final MeasureView measureView) {
        this.measureViews.remove(measureView);
        updateView();
        mainView.updateView();
    }
    
    public void addMeasureView(final MeasureView view) {
        this.measureViews.add(view);
        updateView();
        mainView.updateView();
    }
    
    public List<MeasureView> getMeasureViews() {
    	return measureViews;
    }
    
    private void updateView() {
    	this.removeAll();
    	for (final MeasureView view : getMeasureViews()) {
            this.add(view);
        }
    }
}
