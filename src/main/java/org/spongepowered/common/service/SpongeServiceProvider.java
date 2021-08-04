/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.service;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.service.ServiceRegistration;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.config.common.ServicesCategory;
import org.spongepowered.common.event.lifecycle.AbstractProvideServiceEventImpl;
import org.spongepowered.common.event.manager.SpongeEventManager;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class SpongeServiceProvider implements ServiceProvider {

    private final Game game;
    private final Injector injector;
    private final Map<Class<?>, Registration<?>> services = new HashMap<>();

    @Inject
    public SpongeServiceProvider(final Game game, final Injector injector) {
        this.game = game;
        this.injector = injector;
    }

    protected final Game getGame() {
        return this.game;
    }

    protected abstract List<Service<?>> servicesToSelect();

    @Override
    @SuppressWarnings("unchecked")
    public final <T> @NonNull Optional<T> provide(final @NonNull Class<T> serviceClass) {
        final Registration<T> registration = (Registration<T>) this.services.get(serviceClass);
        if (registration != null) {
            return Optional.of(registration.service());
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> @NonNull Optional<ServiceRegistration<T>> registration(final @NonNull Class<T> serviceClass) {
        return Optional.ofNullable((ServiceRegistration<T>) this.services.get(serviceClass));
    }

    @SuppressWarnings("unchecked")
    protected final <T> @NonNull T provideUnchecked(final Class<T> serviceClass) {
        final Registration<T> registration = (Registration<T>) this.services.get(serviceClass);
        if (registration != null) {
            return registration.service();
        }
        throw new IllegalStateException("Service registration does not exist.");
    }

    // Service Discovery

    /**
     * Discovers services by querying plugins with the
     * {@link ProvideServiceEvent}. To be called at the appropriate moment in
     * the lifecycle.
     */
    public final void init() {
        if (!this.services.isEmpty()) {
            throw new IllegalStateException("Services have already been initialised");
        }

        final ServicesCategory.ServicePluginSubCategory servicePluginSubCategory =
                SpongeConfigs.getCommon().get().services.plugins;

        // We loop over all available services and try to discover each one.
        // This does NOT support third party service interfaces, only impls.
        for (final Service<?> candidate : this.servicesToSelect()) {

            // If the configuration file has a specific plugin ID, we look for it.
            final String pluginId = candidate.providePluginId(servicePluginSubCategory);
            final boolean isSpecific = !pluginId.isEmpty() && !pluginId.equals("?");
            final Class<?> serviceClass = candidate.getServiceClass();
            final String serviceName = serviceClass.getSimpleName();

            Registration<?> registration = null;
            if (isSpecific) {
                final Optional<PluginContainer> specificPluginContainer =
                        Launch.instance().pluginManager().plugin(pluginId);
                if (specificPluginContainer.isPresent()) {
                    registration = this.getSpecificRegistration(specificPluginContainer.get(), candidate);
                    if (registration == null) {
                        final PrettyPrinter prettyPrinter = new PrettyPrinter(80)
                                .add("Service Registration Failed: %s (Service Not Provided)", serviceName)
                                .hr()
                                .addWrapped("Sponge is configured to obtain the service %s from the plugin with ID %s," +
                                                "however, that plugin did not provide any service implementation when " +
                                                "requested.",
                                        serviceName, pluginId)
                                .add()
                                .add("To fix this problem, do one of the following:")
                                .add()
                                .add(" * Check that the plugin %s can actually provide the service (check the plugin" +
                                        "   documentation if you need more assistance with that plugin).", pluginId)
                                .add(" * Set the config entry for this service to \"?\" to let Sponge find another " +
                                        "   plugin to provide the service.")
                                .add(" * Set the config entry for this service to the ID of another plugin that can" +
                                        "   provide the service.");
                        if (candidate.suppliesDefault()) {
                            prettyPrinter.add()
                                    .add("Sponge will continue using the inbuilt default service.");
                        }
                        prettyPrinter.log(SpongeCommon.logger(), Level.ERROR);
                    }
                } else {
                    final PrettyPrinter prettyPrinter = new PrettyPrinter(80)
                            .add("Service Registration Failed: %s (Plugin Not Found)", serviceName)
                            .hr()
                            .addWrapped("Sponge is configured to obtain the service %s from the plugin with ID %s," +
                                    "however, that plugin isn't installed.", serviceName, pluginId)
                            .add()
                            .add("To fix this problem, do one of the following:")
                            .add()
                            .add(" * Install the plugin %s", pluginId)
                            .add(" * Set the config entry for this service to \"?\" to let Sponge find another " +
                                    "   plugin to provide the service.")
                            .add(" * Set the config entry for this service to the ID of another plugin that can" +
                                    "   provide the service.");
                    if (candidate.suppliesDefault()) {
                        prettyPrinter.add()
                                .add("Sponge will continue using the inbuilt default service.");
                    }
                    prettyPrinter.log(SpongeCommon.logger(), Level.ERROR);
                }
            } else {
                final Collection<PluginContainer> toQuery = Launch.instance().pluginManager().plugins();
                registration = this.attemptRegistration(toQuery, candidate);
            }

            if (registration == null) {
                // If we don't have a registration, we try a Sponge one (which is lowest priority)
                registration = this.createRegistration(
                        candidate,
                        Launch.instance().commonPlugin());
            }

            // If after all that we have a registration, we... register it.
            if (registration != null) {
                this.services.put(candidate.getServiceClass(), registration);
                SpongeCommon.logger().info("Registered service [{}] to plugin '{}'.",
                        registration.clazz.getSimpleName(),
                        registration.pluginContainer.metadata().id());
            }
        }

    }

    private <T> @Nullable Registration<T> attemptRegistration(final Collection<PluginContainer> pluginContainers, final Service<T> service) {
        Registration<T> registration = null;
        final Iterator<PluginContainer> pluginContainerIterator = pluginContainers.iterator();
        while (registration == null && pluginContainerIterator.hasNext()) {
            final PluginContainer pluginContainer = pluginContainerIterator.next();
            if (!Launch.instance().launcherPlugins().contains(pluginContainer)) {
                // If this succeeds, the while loop will end.
                registration = this.getSpecificRegistration(pluginContainer, service);
            }
        }
        return registration;
    }

    protected abstract <T> AbstractProvideServiceEventImpl<T> createEvent(final PluginContainer container, final Service<T> service);

    private <T> @Nullable Registration<T> getSpecificRegistration(final PluginContainer container, final Service<T> service) {
        // This is the actual query - a generic event.
        final AbstractProvideServiceEventImpl<T> event = this.createEvent(container, service);
        try {
            ((SpongeEventManager) this.getGame().eventManager()).postToPlugin(event, container);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        if (event.getSuggestion() != null) {
            try {
                return new Registration<>(service.getServiceClass(), event.getSuggestion().get(), container);
            } catch (final Throwable e) { // if the service can't be created
                SpongeCommon.logger().error("Could not create service {} from plugin {}.",
                        service.getServiceClass().getSimpleName(),
                        container.metadata().id(),
                        e);
            }
        }
        return null;
    }

    private <T> @Nullable Registration<T> createRegistration(final Service<T> service, final PluginContainer container) {
        final T impl = service.provideDefaultService(this.injector);
        if (impl == null) {
            return null;
        }
        return new Registration<>(service.getServiceClass(), impl, container);
    }

    static final class Registration<T> implements ServiceRegistration<T> {

        private final Class<T> clazz;
        private final T object;
        private final PluginContainer pluginContainer;

        private Registration(final Class<T> clazz, final T object, final PluginContainer pluginContainer) {
            this.clazz = clazz;
            this.object = Preconditions.checkNotNull(object, "The service must have an implementation!");
            this.pluginContainer = pluginContainer;
        }

        @Override
        public @NonNull Class<T> serviceClass() {
            return this.clazz;
        }

        @Override
        public @NonNull T service() {
            return this.object;
        }

        @Override
        public @NonNull PluginContainer pluginContainer() {
            return this.pluginContainer;
        }
    }

    protected static final class Service<T> {

        @NonNull private final Class<T> service;
        private final @Nullable Class<? extends T> defaultServiceClass;
        @NonNull private final Function<ServicesCategory.ServicePluginSubCategory, String> configEntryProvider;

        public Service(final @NonNull Class<T> service,
                final @NonNull Function<ServicesCategory.ServicePluginSubCategory, String> configEntryProvider,
                final @Nullable Class<? extends T> defaultServiceClass) {
            this.service = service;
            this.defaultServiceClass = defaultServiceClass;
            this.configEntryProvider = configEntryProvider;
        }

        public Class<T> getServiceClass() {
            return this.service;
        }

        public String providePluginId(final ServicesCategory.ServicePluginSubCategory servicePluginSubCategory) {
            return this.configEntryProvider.apply(servicePluginSubCategory);
        }

        public boolean suppliesDefault() {
            return this.defaultServiceClass != null;
        }

        public @Nullable T provideDefaultService(final Injector injector) {
            if (this.defaultServiceClass != null) {
                return injector.getInstance(this.defaultServiceClass);
            } else {
                return null;
            }
        }
    }

}
