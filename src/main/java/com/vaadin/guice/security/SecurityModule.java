package com.vaadin.guice.security;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import com.vaadin.guice.annotation.GuiceView;
import com.vaadin.guice.annotation.UIModule;
import com.vaadin.guice.security.annotation.AccessDeniedView;
import com.vaadin.guice.security.annotation.RestrictedTo;
import com.vaadin.navigator.View;
import com.vaadin.ui.Component;

import org.reflections.Reflections;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.gwt.thirdparty.guava.common.collect.Iterables.get;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;

/*
* This module needs to be installed for guice-security to be set up.
*
* <p>
*
* </p>
* */
@UIModule
public abstract class SecurityModule extends AbstractModule {

    private final Reflections reflections;

    protected SecurityModule(Reflections reflections) {
        this.reflections = reflections;
    }

    protected abstract Class<? extends PathAccessEvaluator> getPathAccessEvaluatorClass();

    @SuppressWarnings("unchecked")
    protected void configure() {
        final Set<Class<?>> restrictedTypes = reflections.getTypesAnnotatedWith(RestrictedTo.class, true);

        Multibinder<Component> restrictedComponentsMultibinder = newSetBinder(binder(), Component.class, RestrictedTo.class);

        for (Class<?> restrictedType : restrictedTypes) {
            if (Component.class.isAssignableFrom(restrictedType)) {
                restrictedComponentsMultibinder.addBinding().to((Class<? extends Component>) restrictedType);
            } else if (!View.class.isAssignableFrom(restrictedType)) {
                throw new IllegalStateException("class " + restrictedType + " is annotated with @RestrictedTo but implements neither Component nor View");
            }
        }

        bind(new TypeLiteral<Map<String, Set<Component>>>() {
        }).toProvider(AccessPathsToComponentsProvider.class);

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

        bind(PathBasedAccessControl.class).asEagerSingleton();
    }
}
