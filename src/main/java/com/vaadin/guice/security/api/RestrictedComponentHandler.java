package com.vaadin.guice.security.api;

import com.vaadin.ui.Component;

public interface RestrictedComponentHandler {
    void attachComponent(Component component, String permission);
    void detachComponent(Component component, String permission);
}
