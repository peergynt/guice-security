package com.vaadin.guice.security;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import com.vaadin.guice.annotation.GuiceViewChangeListener;
import com.vaadin.guice.annotation.UIScope;
import com.vaadin.guice.bus.UIEventBus;
import com.vaadin.guice.providers.CurrentUIProvider;
import com.vaadin.guice.security.annotation.RestrictedTo;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;

import java.util.Map;
import java.util.Set;

@UIScope
@GuiceViewChangeListener
class PathBasedAccessControl implements ViewChangeListener {

    PathBasedAccessControl(UIEventBus uiEventBus){
        uiEventBus.register(this);
    }

    @Inject
    private Provider<Map<String, Set<Component>>> accessPathesToComponents;

    @Inject
    @Named("access_denied_view")
    private String accessDeniedViewTarget;

    @Inject
    private PathAccessEvaluator pathAccessEvaluator;

    @Inject
    private CurrentUIProvider currentUIProvider;

    private Optional<String> accessNeededForCurrentView = Optional.absent();

    public void loginStateChanged() {
        for (Map.Entry<String, Set<Component>> accessPathToComponentSet : accessPathesToComponents.get().entrySet()) {
            String accessPath = accessPathToComponentSet.getKey();
            Set<Component> components = accessPathToComponentSet.getValue();

            final boolean accessGranted = pathAccessEvaluator.isGranted(accessPath);

            for (Component component : components) {
                component.setVisible(accessGranted);
            }
        }

        if (accessNeededForCurrentView.isPresent()) {
            boolean accessToCurrentViewGranted = pathAccessEvaluator.isGranted(accessNeededForCurrentView.get());

            if (!accessToCurrentViewGranted) {
                currentUIProvider.get().getNavigator().navigateTo(accessDeniedViewTarget);
            }
        }
    }

    public boolean beforeViewChange(ViewChangeEvent event) {
        final RestrictedTo restrictedTo = event.getNewView().getClass().getAnnotation(RestrictedTo.class);

        if (restrictedTo == null) {
            accessNeededForCurrentView = Optional.absent();
            return true;
        }

        String accessPath = restrictedTo.value();

        accessNeededForCurrentView = Optional.of(accessPath);

        boolean accessGranted = pathAccessEvaluator.isGranted(accessPath);

        if (!accessGranted) {
            currentUIProvider.get().getNavigator().navigateTo(accessDeniedViewTarget);
        }

        return accessGranted;
    }

    public void afterViewChange(ViewChangeEvent event) {
        ;
    }
}
