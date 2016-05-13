package com.vaadin.guice.security;

import java.lang.reflect.Field;

import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.vaadin.guice.security.api.PathAccessEvaluator;
import com.vaadin.server.ClientConnector.AttachEvent;
import com.vaadin.server.ClientConnector.AttachListener;
import com.vaadin.ui.Component;

class RestrictedComponentInjector<T> implements MembersInjector<T> {
    private final Field field;
    private final String permission;
    private final Provider<? extends Component> componentProvider;
    private final Provider<PathAccessEvaluator> accessProvider;

    // FIXME: we still want to be able to programmatically re-evaluate visibility
    //        when attaching, register the component in the UI
    //        when re-evaluate visibility - > evaluate for all attached components
    //        when detaching, remove component from map
    //        the map should have UI scope
    @SuppressWarnings("serial")
    private final AttachListener attachListener = new AttachListener() {
        public void attach(AttachEvent event) {
            boolean visible = accessProvider.get().isGranted(permission);
            Component component = (Component) event.getSource();
            component.setVisible(visible);
        }
    };

    RestrictedComponentInjector(Field field, String permission, Provider<? extends Component> componentProvider,
            Provider<PathAccessEvaluator> accessProvider) {
        this.field = field;
        this.permission = permission;
        this.componentProvider = componentProvider;
        this.accessProvider = accessProvider;
        field.setAccessible(true);
    }

    public void injectMembers(T t) {
        try {
            Component component = componentProvider.get();
            component.addAttachListener(attachListener);
            field.set(t, component);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}

