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
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.service.ServiceRegistration;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.config.SpongeConfigs;
import org.spongepowered.common.config.common.ServicesCategory;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.event.lifecycle.ProvideServiceEventImpl;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.common.service.ban.SpongeBanService;
import org.spongepowered.common.service.pagination.SpongePaginationService;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.whitelist.SpongeWhitelistService;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public final class SpongeServiceProvider implements ServiceProvider {

    // Contains all the services and defaults.
    private static final ImmutableList<Service<?>> AVAILABLE_SERVICES =
            ImmutableList.<Service<?>>builder()
                    .add(new Service<>(
                            BanService.class,
                            ServicesCategory.ServicePluginSubCategory::getBanService,
                            SpongeBanService.class))
                    .add(new Service<>(
                            EconomyService.class,
                            ServicesCategory.ServicePluginSubCategory::getEconomyService,
                            null))
                    .add(new Service<>(
                            PaginationService.class,
                            ServicesCategory.ServicePluginSubCategory::getPaginationService,
                            SpongePaginationService.class))
                    .add(new Service<>(
                            PermissionService.class,
                            ServicesCategory.ServicePluginSubCategory::getPermissionService,
                            SpongePermissionService.class))
                    .add(new Service<>(
                            WhitelistService.class,
                            ServicesCategory.ServicePluginSubCategory::getWhitelistService,
                            SpongeWhitelistService.class))
                    .build();
    // --

    private final Game game;
    private final Injector injector;
    private final Map<Class<?>, Registration<?>> services = new HashMap<>();

    @Inject
    public SpongeServiceProvider(final Game game, final Injector injector) {
        this.game = game;
        this.injector = injector;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public final <T> Optional<T> provide(@NonNull final Class<T> serviceClass) {
        final Registration<T> registration = (Registration<T>) this.services.get(serviceClass);
        if (registration != null) {
            return Optional.of(registration.service());
        }
        return Optional.empty();
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public final <T> Optional<ServiceRegistration<T>> getRegistration(@NonNull final Class<T> serviceClass) {
        return Optional.ofNullable((ServiceRegistration<T>) this.services.get(serviceClass));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T> T provideUnchecked(final Class<T> serviceClass) {
        final Registration<T> registration = (Registration<T>) this.services.get(serviceClass);
        if (registration != null) {
            return registration.service();
        }
        throw new IllegalStateException("Service registration does not exist.");
    }

    @Override
    @NonNull
    public final BanService banService() {
        return this.provideUnchecked(BanService.class);
    }

    @Override
    @NonNull
    public final Optional<EconomyService> economyService() {
        return this.provide(EconomyService.class);
    }

    @Override
    @NonNull
    public final PaginationService paginationService() {
        return this.provideUnchecked(PaginationService.class);
    }

    @Override
    @NonNull
    public final PermissionService permissionService() {
        return this.provideUnchecked(PermissionService.class);
    }

    @Override
    @NonNull
    public final WhitelistService whitelistService() {
        return this.provideUnchecked(WhitelistService.class);
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
                SpongeConfigs.getCommon().get().getServicesCategory().getServicePlugin();

        // We loop over all available services and try to discover each one.
        // This does NOT support third party service interfaces, only impls.
        for (final Service<?> candidate : AVAILABLE_SERVICES) {

            // If the configuration file has a specific plugin ID, we look for it.
            final String pluginId = candidate.providePluginId(servicePluginSubCategory);
            final boolean isSpecific = !pluginId.isEmpty() && !pluginId.equals("?");
            final Class<?> serviceClass = candidate.getServiceClass();
            final String serviceName = serviceClass.getSimpleName();

            Registration<?> registration = null;
            if (isSpecific) {
                final Optional<PluginContainer> specificPluginContainer =
                        Launcher.getInstance().getPluginManager().getPlugin(pluginId);
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
                        prettyPrinter.log(SpongeCommon.getLogger(), Level.ERROR);
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
                    prettyPrinter.log(SpongeCommon.getLogger(), Level.ERROR);
                }
            } else {
                final Collection<PluginContainer> toQuery = Launcher.getInstance().getPluginManager().getPlugins();
                registration = this.attemptRegistration(toQuery, candidate);
            }

            if (registration == null) {
                // If we don't have a registration, we try a Sponge one (which is lowest priority)
                registration = this.createRegistration(
                        candidate,
                        Launcher.getInstance().getCommonPlugin());
            }

            // If after all that we have a registration, we... register it.
            if (registration != null) {
                this.services.put(candidate.getServiceClass(), registration);
                SpongeCommon.getLogger().info("Registered service [{}] to plugin '{}'.",
                        registration.clazz.getSimpleName(),
                        registration.pluginContainer.getMetadata().getId());
            }
        }

    }

    @Nullable
    private <T> Registration<T> attemptRegistration(final Collection<PluginContainer> pluginContainers, final Service<T> service) {
        Registration<T> registration = null;
        final Iterator<PluginContainer> pluginContainerIterator = pluginContainers.iterator();
        while (registration == null && pluginContainerIterator.hasNext()) {
            final PluginContainer pluginContainer = pluginContainerIterator.next();
            if (!Launcher.getInstance().getLauncherPlugins().contains(pluginContainer)) {
                // If this succeeds, the while loop will end.
                registration = this.getSpecificRegistration(pluginContainer, service);
            }
        }
        return registration;
    }

    @Nullable
    private <T> Registration<T> getSpecificRegistration(final PluginContainer container, final Service<T> service) {
        final ProvideServiceEventImpl<T> event = new ProvideServiceEventImpl<>(Cause.of(EventContext.empty(), this.game), this.game, TypeToken.of(service.getServiceClass()));

        // This is the actual query - a generic event.
        try {
            ((SpongeEventManager) this.game.getEventManager()).post(event, container);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        if (event.getSuggestion() != null) {
            try {
                return new Registration<>(service.getServiceClass(), event.getSuggestion().get(), container);
            } catch (final Throwable e) { // if the service can't be created
                SpongeCommon.getLogger().error("Could not create service {} from plugin {}.",
                        service.getServiceClass().getSimpleName(),
                        container.getMetadata().getId(),
                        e);
            }
        }
        return null;
    }

    @Nullable
    private <T> Registration<T> createRegistration(final Service<T> service, final PluginContainer container) {
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
        @NonNull
        public Class<T> serviceClass() {
            return this.clazz;
        }

        @Override
        @NonNull
        public T service() {
            return this.object;
        }

        @Override
        @NonNull
        public PluginContainer pluginContainer() {
            return this.pluginContainer;
        }
    }

    static final class Service<T> {

        @NonNull private final Class<T> service;
        @Nullable private final Class<? extends T> defaultServiceClass;
        @NonNull private final Function<ServicesCategory.ServicePluginSubCategory, String> configEntryProvider;

        Service(@NonNull final Class<T> service,
                @NonNull final Function<ServicesCategory.ServicePluginSubCategory, String> configEntryProvider,
                @Nullable final Class<? extends T> defaultServiceClass) {
            this.service = service;
            this.defaultServiceClass = defaultServiceClass;
            this.configEntryProvider = configEntryProvider;
        }

        Class<T> getServiceClass() {
            return this.service;
        }

        String providePluginId(final ServicesCategory.ServicePluginSubCategory servicePluginSubCategory) {
            return this.configEntryProvider.apply(servicePluginSubCategory);
        }

        boolean suppliesDefault() {
            return this.defaultServiceClass != null;
        }

        @Nullable
        T provideDefaultService(final Injector injector) {
            if (this.defaultServiceClass != null) {
                return injector.getInstance(this.defaultServiceClass);
            } else {
                return null;
            }
        }
    }

}
