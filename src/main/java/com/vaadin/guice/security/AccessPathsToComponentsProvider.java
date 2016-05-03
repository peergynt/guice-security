package com.vaadin.guice.security;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.vaadin.guice.annotation.UIScope;
import com.vaadin.guice.security.annotation.RestrictedTo;
import com.vaadin.ui.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

@UIScope
class AccessPathsToComponentsProvider implements Provider<Map<String, Set<Component>>> {

    private Map<String, Set<Component>> accessPathesToComponentsMap;

    @Inject
    AccessPathsToComponentsProvider(@AllRestrictedComponents Set<Component> restrictedComponents) {
        accessPathesToComponentsMap = new HashMap<String, Set<Component>>(restrictedComponents.size());

        for (Component restrictedComponent : restrictedComponents) {
            RestrictedTo restrictedTo = restrictedComponent.getClass().getAnnotation(RestrictedTo.class);

            checkState(restrictedTo != null);

            String accessPath = restrictedTo.value();

            Set<Component> components = accessPathesToComponentsMap.get(accessPath);

            if (components == null) {
                components = new HashSet<Component>();
                accessPathesToComponentsMap.put(accessPath, components);
            }

            components.add(restrictedComponent);
        }
    }

    public Map<String, Set<Component>> get() {
        return accessPathesToComponentsMap;
    }
}
