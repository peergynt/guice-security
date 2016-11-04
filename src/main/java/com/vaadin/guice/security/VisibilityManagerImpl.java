package com.vaadin.guice.security;

import com.google.common.base.Optional;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import com.vaadin.guice.annotation.GuiceViewChangeListener;
import com.vaadin.guice.annotation.UIScope;
import com.vaadin.guice.security.annotation.RestrictedTo;
import com.vaadin.guice.security.api.VisibilityManager;
import com.vaadin.guice.security.api.PathAccessEvaluator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

@UIScope
@GuiceViewChangeListener
class VisibilityManagerImpl implements ViewChangeListener, VisibilityManager {

    private static final long serialVersionUID = 7130577225824856031L;

    @Inject
    private Provider<SetMultimap<String, Component>> accessPathesToComponentsProvider;

    @Inject
    @Named("access_denied_view")
    private String accessDeniedViewTarget;

    @Inject
    private PathAccessEvaluator pathAccessEvaluator;

    @Inject
    private Provider<UI> currentUIProvider;

    private Optional<String> accessNeededForCurrentView = Optional.absent();

    public void evaluateVisibility() {
        SetMultimap<String, Component> accessPathesToComponents = accessPathesToComponentsProvider.get();
        for (String accessPath: accessPathesToComponents.keySet()) {
            final boolean accessGranted = pathAccessEvaluator.isGranted(accessPath);
            for (Component component : accessPathesToComponents.get(accessPath)) {
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
