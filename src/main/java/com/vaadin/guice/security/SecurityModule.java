package com.vaadin.guice.security;

import com.google.common.collect.SetMultimap;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;

import com.vaadin.guice.annotation.GuiceView;
import com.vaadin.guice.security.annotation.AccessDeniedView;
import com.vaadin.guice.security.annotation.RestrictedTo;
import com.vaadin.guice.security.api.VisibilityManager;
import com.vaadin.guice.security.api.PathAccessEvaluator;
import com.vaadin.guice.security.api.RestrictedComponentHandler;
import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.get;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;

/*
* This module needs to be installed for guice-security to be set up.
*
* <p>
*
* </p>
* */
public abstract class SecurityModule extends AbstractModule {

    private final Reflections reflections;

    protected SecurityModule(Reflections reflections) {
        this.reflections = reflections.merge(new Reflections(new ConfigurationBuilder().forPackages("com.vaadin.guice.security")));
    }

    protected abstract Class<? extends PathAccessEvaluator> getPathAccessEvaluatorClass();

    @SuppressWarnings("unchecked")
    protected void configure() {
        final Set<Class<?>> restrictedTypes = reflections.getTypesAnnotatedWith(RestrictedTo.class, true);

        Multibinder<Component> restrictedComponentsMultibinder = newSetBinder(binder(), Component.class, AllRestrictedComponents.class);

        for (Class<?> restrictedType : restrictedTypes) {
            if (Component.class.isAssignableFrom(restrictedType)) {
                restrictedComponentsMultibinder.addBinding().to((Class<? extends Component>) restrictedType);
            } else if (!View.class.isAssignableFrom(restrictedType)) {
                throw new IllegalStateException("class " + restrictedType + " is annotated with @RestrictedTo but implements neither Component nor View");
            }
        }

        bind(new TypeLiteral<SetMultimap<String, Component>>() {}).toProvider(AccessPathsToComponentsProvider.class);

        final Set<Class<?>> accessDeniedViews = reflections.getTypesAnnotatedWith(AccessDeniedView.class);

        checkArgument(accessDeniedViews.size() <= 1, "more than one class annotated with @AccessDeniedView");

        String accessDeniedTarget = "";

        if (accessDeniedViews.size() == 1) {
            final Class<?> clazz = get(accessDeniedViews, 0);
            checkArgument(View.class.isAssignableFrom(clazz));

            GuiceView guiceViewAnnotation = clazz.getAnnotation(GuiceView.class);

            checkArgument(guiceViewAnnotation != null, "class " + clazz + " is annotated as AccessDeniedView but not as GuiceView");

            accessDeniedTarget = guiceViewAnnotation.name();
        }

        bind(String.class).annotatedWith(named("access_denied_view")).toInstance(accessDeniedTarget);

        bind(PathAccessEvaluator.class).to(getPathAccessEvaluatorClass());

        bind(RestrictedComponentHandler.class).to(RestrictedComponentHandlerImpl.class);

        bind(VisibilityManager.class).to(VisibilityManagerImpl.class);

        bindListener(Matchers.any(), new RestrictedTypeListener());
    }
}
