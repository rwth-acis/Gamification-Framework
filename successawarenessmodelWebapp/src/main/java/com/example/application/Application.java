// 
// Decompiled by Procyon v0.5.36
// 

package com.example.application;

import org.springframework.boot.SpringApplication;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.page.AppShellConfigurator;

@SpringBootApplication
@Theme("mytodo")
@NpmPackage(value = "line-awesome", version = "1.3.0")
public class Application implements AppShellConfigurator
{
    public static void main(final String[] args) {
        SpringApplication.run((Class)Application.class, args);
    }
}
