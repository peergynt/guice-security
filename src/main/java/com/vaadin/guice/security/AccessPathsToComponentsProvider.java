package com.vaadin.guice.security;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.guice.annotation.UIScope;
import com.vaadin.ui.Component;

@UIScope
class AccessPathsToComponentsProvider implements Provider<SetMultimap<String, Component>> {

    // We are in the UI Scope, no need for concurrent map
    private SetMultimap<String, Component> accessPathesToComponentsMap;

    @Inject
    AccessPathsToComponentsProvider() {
        accessPathesToComponentsMap = HashMultimap.create();
    }

    public SetMultimap<String, Component> get() {
        return accessPathesToComponentsMap;
    }

}
