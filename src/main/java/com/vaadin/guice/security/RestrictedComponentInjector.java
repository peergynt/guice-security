package com.vaadin.guice.security;

import java.lang.reflect.Field;

import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.vaadin.guice.security.api.RestrictedComponentHandler;
import com.vaadin.server.ClientConnector.AttachEvent;
import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.ui.Component;

class RestrictedComponentInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final String permission;
    private final Provider<? extends Component> componentProvider;
    private final Provider<RestrictedComponentHandler> componentHandler;

    @SuppressWarnings("serial")
    private final AttachListener attachListener = new AttachListener() {
        public void attach(AttachEvent event) {
            componentHandler.get().attachComponent((Component) event.getSource(), permission);
        }
    };

    @SuppressWarnings("serial")
    private final DetachListener detachListener = new DetachListener() {
        public void detach(DetachEvent event) {
            componentHandler.get().detachComponent((Component) event.getSource(), permission);
        }
    };

    RestrictedComponentInjector(Field field, String permission, Provider<? extends Component> componentProvider,
            Provider<RestrictedComponentHandler> componentHandler) {
        this.field = field;
        this.permission = permission;
        this.componentProvider = componentProvider;
        this.componentHandler = componentHandler;
        field.setAccessible(true);
    }

    public void injectMembers(T t) {
        try {
            Component component = componentProvider.get();
            component.addAttachListener(attachListener);
            component.addDetachListener(detachListener);
            field.set(t, component);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}

