package com.vaadin.guice.security;

import java.lang.reflect.Field;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.vaadin.guice.security.annotation.RestrictedVisibility;
import com.vaadin.guice.security.api.RestrictedComponentHandler;
import com.vaadin.ui.Component;

class RestrictedTypeListener implements TypeListener {

    public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
        Class<?> clazz = typeLiteral.getRawType();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Component.class.isAssignableFrom(field.getType()) &&
                        field.isAnnotationPresent(RestrictedVisibility.class)) {
                    RestrictedVisibility annotation = field.getAnnotation(RestrictedVisibility.class);
                    @SuppressWarnings("unchecked")
                    Class<? extends Component> fieldClass = (Class<? extends Component>) field.getType();
                    Provider<? extends Component> componentProvider = typeEncounter.getProvider(fieldClass);
                    Provider<RestrictedComponentHandler> componentHandler = typeEncounter.getProvider(RestrictedComponentHandler.class);
                    typeEncounter.register(new RestrictedComponentInjector<T>(field, annotation.value(), componentProvider, componentHandler));
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

}

