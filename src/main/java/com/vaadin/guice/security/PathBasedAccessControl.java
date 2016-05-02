package com.vaadin.guice.security;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import com.vaadin.guice.annotation.GuiceViewChangeListener;
import com.vaadin.guice.annotation.UIScope;
import com.vaadin.guice.security.annotation.RestrictedTo;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

import java.util.Map;
import java.util.Set;

@UIScope
@GuiceViewChangeListener
public abstract class PathBasedAccessControl implements ViewChangeListener {

    protected PathBasedAccessControl(){
        loginStateChanged();
    }

    @Inject
    private Provider<Map<String, Set<Component>>> accessPathesToComponents;

    @Inject
    @Named("access_denied_view")
    private String accessDeniedViewTarget;

    private Optional<String> accessNeededForCurrentView = Optional.absent();

    abstract boolean isAccessGranted(String accessPath);

    protected void loginStateChanged(){
        for (Map.Entry<String, Set<Component>> accessPathToComponentSet : accessPathesToComponents.get().entrySet()) {
            String accessPath = accessPathToComponentSet.getKey();
            Set<Component> components = accessPathToComponentSet.getValue();

            final boolean accessGranted = isAccessGranted(accessPath);

            for (Component component : components) {
                component.setVisible(accessGranted);
            }
        }

        if(accessNeededForCurrentView.isPresent()){
            boolean accessToCurrentViewGranted = isAccessGranted(accessNeededForCurrentView.get());

            if(!accessToCurrentViewGranted){
                UI.getCurrent().getNavigator().navigateTo(accessDeniedViewTarget);
            }
        }
    }

    public boolean beforeViewChange(ViewChangeEvent event) {
        final RestrictedTo restrictedTo = event.getNewView().getClass().getAnnotation(RestrictedTo.class);

        if(restrictedTo == null){
            accessNeededForCurrentView = Optional.absent();
            return true;
        }

        String accessPath = restrictedTo.value();

        accessNeededForCurrentView = Optional.of(accessPath);

        boolean accessGranted = isAccessGranted(accessPath);

        if(!accessGranted){
            UI.getCurrent().getNavigator().navigateTo(accessDeniedViewTarget);
        }

        return accessGranted;
    }

    public void afterViewChange(ViewChangeEvent event) {
        ;
    }
}



