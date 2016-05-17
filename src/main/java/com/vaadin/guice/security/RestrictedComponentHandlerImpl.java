package com.vaadin.guice.security;

import javax.inject.Inject;

import com.google.common.collect.SetMultimap;
import com.vaadin.guice.annotation.UIScope;
import com.vaadin.guice.security.api.PathAccessEvaluator;
import com.vaadin.guice.security.api.RestrictedComponentHandler;
import com.vaadin.ui.Component;

@UIScope
public class RestrictedComponentHandlerImpl implements RestrictedComponentHandler {

    @Inject
    private SetMultimap<String, Component> accessPathesToComponents;

    @Inject
    private PathAccessEvaluator pathAccessEvaluator;

    public void attachComponent(Component component, String permission) {
        component.setVisible(pathAccessEvaluator.isGranted(permission));
        accessPathesToComponents.put(permission, component);
    }

    public void detachComponent(Component component, String permission) {
        accessPathesToComponents.remove(permission, component);
    }

}
