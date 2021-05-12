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
package org.spongepowered.common.event;

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Engine;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.GenericEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.ContainerBridge;
import org.spongepowered.common.event.filter.FilterFactory;
import org.spongepowered.common.event.gen.DefineableClassLoader;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.EventListenerPhaseContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import co.aikar.timings.sponge.TimingsManager;
import org.spongepowered.common.util.EngineUtil;
import org.spongepowered.common.util.TypeTokenUtil;
import org.spongepowered.configurate.util.Types;
import org.spongepowered.plugin.PluginContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public final class SpongeEventManager implements EventManager {

    private static final TypeVariable<?> GENERIC_EVENT_TYPE = GenericEvent.class.getTypeParameters()[0];

    private final Object lock;
    protected final Logger logger;
    private final Multimap<Class<?>, RegisteredListener<?>> handlersByEvent;
    private final Map<ClassLoader, AnnotatedEventListener.Factory> classLoaders;
    private final Set<Object> registeredListeners;

    public final ListenerChecker checker;

    /**
     * A cache of all the handlers for an event type for quick event posting.
     * <p>The cache is currently entirely invalidated if handlers are added or
     * removed.</p>
     */
    protected final LoadingCache<EventType<?>, RegisteredListener.Cache> handlersCache =
            Caffeine.newBuilder().initialCapacity(150).build(this::bakeHandlers);

    @Inject
    public SpongeEventManager(final Logger logger) {
        this.logger = logger;
        this.lock = new Object();
        this.handlersByEvent = HashMultimap.create();
        this.classLoaders = new IdentityHashMap<>();
        this.registeredListeners = new ReferenceOpenHashSet<>();
        this.checker = new ListenerChecker(ShouldFire.class);

        // Caffeine offers no control over the concurrency level of the
        // ConcurrentHashMap which backs the cache. By default this concurrency
        // level is 16. We replace the backing map before any use can occur
        // a new ConcurrentHashMap with a concurrency level of 1
        try {
            // Cache impl class is UnboundedLocalLoadingCache which extends
            // UnboundedLocalManualCache

            // UnboundedLocalManualCache has a field 'cache' with an
            // UnboundedLocalCache which contains the actual backing map
            final Field innerCache = this.handlersCache.getClass().getSuperclass().getDeclaredField("cache");
            innerCache.setAccessible(true);
            final Object innerCacheValue = innerCache.get(this.handlersCache);
            final Class<?> innerCacheClass = innerCacheValue.getClass(); // UnboundedLocalCache
            final Field cacheData = innerCacheClass.getDeclaredField("data");
            cacheData.setAccessible(true);
            final ConcurrentHashMap<Class<? extends Event>, RegisteredListener.Cache> newBackingData =
                    new ConcurrentHashMap<>(150, 0.75f, 1);
            cacheData.set(innerCacheValue, newBackingData);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            this.logger.warn("Failed to set event cache backing array, type was " + this.handlersCache.getClass().getName());
            this.logger.warn("  Caused by: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    <T extends Event> RegisteredListener.Cache bakeHandlers(final EventType<T> eventType) {
        final List<RegisteredListener<?>> handlers = new ArrayList<>();
        final Stream<? extends Class<?>> types = Types.allSuperTypesAndInterfaces(eventType.getType())
                .map(GenericTypeReflector::erase)
                .filter(Event.class::isAssignableFrom);

        // TODO: Move @Includes and @Excludes from filters to the baking process, this simplifies the generated
        //       filter code and makes the filter baking target more specific handlers.
        synchronized (this.lock) {
            for (final Iterator<? extends Class<?>> it = types.iterator(); it.hasNext();) {
                final Class<?> type = it.next();
                final Collection<RegisteredListener<?>> listeners = this.handlersByEvent.get(type);
                if (GenericEvent.class.isAssignableFrom(type)) {
                    final Type genericType = Objects.requireNonNull(eventType.getGenericType());
                    for (final RegisteredListener<?> listener : listeners) {
                        final Type genericType1 = Objects.requireNonNull(listener.getEventType().getGenericType());
                        if (TypeTokenUtil.isAssignable(genericType, genericType1)) {
                            handlers.add(listener);
                        }
                    }
                } else {
                    handlers.addAll(listeners);
                }
            }
        }

        Collections.sort(handlers);
        return new RegisteredListener.Cache(handlers);
    }

    private static @Nullable String getHandlerErrorOrNull(Method method) {
        final int modifiers = method.getModifiers();
        final List<String> errors = new ArrayList<>();
        if (Modifier.isStatic(modifiers)) {
            errors.add("method must not be static");
        }
        if (!Modifier.isPublic(modifiers)) {
            errors.add("method must be public");
        }
        if (Modifier.isAbstract(modifiers)) {
            errors.add("method must not be abstract");
        }
        if (method.getDeclaringClass().isInterface()) {
            errors.add("interfaces cannot declare listeners");
        }
        if (method.getReturnType() != void.class) {
            errors.add("method must return void");
        }
        final Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length == 0 || !Event.class.isAssignableFrom(parameters[0])) {
            errors.add("method must have an Event as its first parameter");
        }
        if (errors.isEmpty()) {
            return null;
        }
        return String.join(", ", errors);
    }

    private void register(final RegisteredListener<? extends Event> handler) {
        this.register(Collections.singletonList(handler));
    }

    private void register(final List<RegisteredListener<? extends Event>> handlers) {
        boolean changed = false;

        synchronized (this.lock) {
            for (RegisteredListener<?> handler : handlers) {
                final Class<?> raw = handler.getEventType().getType();
                if (this.handlersByEvent.put(raw, handler)) {
                    changed = true;
                    this.checker.registerListenerFor(raw);
                }
            }
        }

        if (changed) {
            this.handlersCache.invalidateAll();
        }
    }

    private void registerListener(PluginContainer plugin, Object listenerObject) {
        checkNotNull(plugin, "plugin");
        checkNotNull(listenerObject, "listener");

        if (this.registeredListeners.contains(listenerObject)) {
            this.logger.warn("Plugin {} attempted to register an already registered listener ({})", plugin.metadata().id(),
                    listenerObject.getClass().getName());
            Thread.dumpStack();
            return;
        }

        final List<RegisteredListener<? extends Event>> handlers = new ArrayList<>();
        final Map<Method, String> methodErrors = new HashMap<>();

        final Class<?> handle = listenerObject.getClass();
        final ClassLoader handleLoader = handle.getClassLoader();

        AnnotatedEventListener.Factory handlerFactory = this.classLoaders.get(handleLoader);
        if (handlerFactory == null) {
            final DefineableClassLoader classLoader = new DefineableClassLoader(handleLoader);
            handlerFactory = new ClassEventListenerFactory("org.spongepowered.common.event.listener",
                    new FilterFactory("org.spongepowered.common.event.filters", classLoader), classLoader);
            this.classLoaders.put(handleLoader, handlerFactory);
        }

        for (final Method method : handle.getMethods()) {
            final Listener listener = method.getAnnotation(Listener.class);
            if (listener != null) {
                final String error = SpongeEventManager.getHandlerErrorOrNull(method);
                if (error == null) {
                    final Type eventType = method.getGenericParameterTypes()[0];
                    final AnnotatedEventListener handler;
                    try {
                        handler = handlerFactory.create(listenerObject, method);
                    } catch (final Exception e) {
                        this.logger.error("Failed to create handler for {} on {}", method, handle, e);
                        continue;
                    }

                    handlers.add(SpongeEventManager.createRegistration(plugin, eventType, listener, handler));
                } else {
                    methodErrors.put(method, error);
                }
            }
        }

        // getMethods() doesn't return private methods. Do another check to warn
        // about those.
        for (Class<?> handleParent = handle; handleParent != Object.class; handleParent = handleParent.getSuperclass()) {
            for (final Method method : handleParent.getDeclaredMethods()) {
                if (method.getAnnotation(Listener.class) != null && !methodErrors.containsKey(method)) {
                    final String error = SpongeEventManager.getHandlerErrorOrNull(method);
                    if (error != null) {
                        methodErrors.put(method, error);
                    }
                }
            }
        }

        for (Map.Entry<Method, String> method : methodErrors.entrySet()) {
            this.logger.warn("Invalid listener method {} in {}: {}", method.getKey(),
                    method.getKey().getDeclaringClass().getName(), method.getValue());
        }

        this.registeredListeners.add(listenerObject);
        this.register(handlers);
    }

    private static <T extends Event> RegisteredListener<T> createRegistration(final PluginContainer plugin, final Type eventClass,
            final Listener listener, final EventListener<? super T> handler) {
        return SpongeEventManager.createRegistration(plugin, eventClass, listener.order(), listener.beforeModifications(), handler);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Event> RegisteredListener<T> createRegistration(final PluginContainer plugin, final Type eventType,
            final Order order, final boolean beforeModifications, final EventListener<? super T> handler) {
        Type genericType = null;
        final Class<?> erased = GenericTypeReflector.erase(eventType);
        if (GenericEvent.class.isAssignableFrom(erased)) {
            genericType = TypeTokenUtil.typeArgumentFromSupertype(eventType, GenericEvent.class, 0);
        }
        return new RegisteredListener(plugin, new EventType(erased, genericType), order, handler, beforeModifications);
    }

    @Override
    public void registerListeners(final PluginContainer plugin, final Object listener) {
        this.registerListener(plugin, listener);
    }

    @Override
    public <T extends Event> void registerListener(final PluginContainer plugin, final Class<T> eventClass, final EventListener<? super T> listener) {
        this.registerListener(plugin, eventClass, Order.DEFAULT, listener);
    }

    @Override
    public <T extends Event> void registerListener(final PluginContainer plugin, final TypeToken<T> eventType,
            final EventListener<? super T> listener) {
        this.registerListener(plugin, eventType, Order.DEFAULT, listener);
    }

    @Override
    public <T extends Event> void registerListener(final PluginContainer plugin, final Class<T> eventClass, final Order order,
            final EventListener<? super T> listener) {
        this.registerListener(plugin, eventClass, Order.DEFAULT, false, listener);
    }

    @Override
    public <T extends Event> void registerListener(final PluginContainer plugin, final TypeToken<T> eventType, final Order order,
            final EventListener<? super T> listener) {
        this.registerListener(plugin, eventType, Order.DEFAULT, false, listener);
    }

    @Override
    public <T extends Event> void registerListener(final PluginContainer plugin, final Class<T> eventClass, final Order order,
            final boolean beforeModifications, final EventListener<? super T> listener) {
        this.registerListener(plugin, TypeToken.get(eventClass), Order.DEFAULT, false, listener);
    }

    @Override
    public <T extends Event> void registerListener(final PluginContainer plugin, final TypeToken<T> eventType, final Order order,
            final boolean beforeModifications, final EventListener<? super T> listener) {
        this.register(SpongeEventManager.createRegistration(plugin, eventType.getType(), order, beforeModifications, listener));
    }

    private void unregister(final Predicate<RegisteredListener<?>> unregister) {
        boolean changed = false;

        synchronized (this.lock) {
            final Iterator<RegisteredListener<?>> itr = this.handlersByEvent.values().iterator();
            while (itr.hasNext()) {
                final RegisteredListener<?> handler = itr.next();
                if (unregister.test(handler)) {
                    itr.remove();
                    changed = true;
                    // TODO: This doesn't seem right, even as it was before
                    this.checker.unregisterListenerFor(handler.getEventType().getType());
                    this.registeredListeners.remove(handler.getHandle());
                }
            }
        }

        if (changed) {
            this.handlersCache.invalidateAll();
        }
    }

    @Override
    public void unregisterListeners(final Object listener) {
        checkNotNull(listener, "listener");
        this.unregister(handler -> listener.equals(handler.getHandle()));
    }

    @Override
    public void unregisterPluginListeners(final PluginContainer plugin) {
        checkNotNull(plugin, "plugin");
        this.unregister(handler -> plugin.equals(handler.getPlugin()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected RegisteredListener.Cache getHandlerCache(final Event event) {
        checkNotNull(event, "event");
        final Class<? extends Event> eventClass = event.getClass();
        final EventType<? extends Event> eventType;
        if (event instanceof GenericEvent) {
            eventType = new EventType(eventClass, checkNotNull(((GenericEvent) event).paramType().getType()));
        } else {
            eventType = new EventType(eventClass, null);
        }
        return this.handlersCache.get(eventType);
    }

    @SuppressWarnings("unchecked")
    private boolean post(final Event event, final List<RegisteredListener<?>> handlers) {
        final Engine engine = EngineUtil.determineEngine();

        // If this event is being posted asynchronously then we don't want
        // to do any timing or cause stack changes
        if (engine == null) {
            for (final RegisteredListener handler : handlers) {
                try {
                    if (event instanceof AbstractEvent) {
                        ((AbstractEvent) event).currentOrder = handler.getOrder();
                    }
                    SpongeCommon.setActivePlugin(handler.getPlugin());
                    handler.handle(event);
                } catch (final Throwable e) {
                    SpongeCommon.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin(), e);
                } finally {
                    SpongeCommon.setActivePlugin(null);
                }
            }
            if (event instanceof AbstractEvent) {
                ((AbstractEvent) event).currentOrder = null;
            }
            return event instanceof Cancellable && ((Cancellable) event).isCancelled();
        }
        TimingsManager.PLUGIN_EVENT_HANDLER.startTimingIfSync();
        for (@SuppressWarnings("rawtypes") final RegisteredListener handler : handlers) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame();
                 final PhaseContext<?> context = this.createPluginContext(handler);
                 final Timing timings = handler.getTimingsHandler()) {
                frame.pushCause(handler.getPlugin());
                if (context != null) {
                    context.buildAndSwitch();
                }
                timings.startTimingIfSync();
                if (event instanceof AbstractEvent) {
                    ((AbstractEvent) event).currentOrder = handler.getOrder();
                }
                SpongeCommon.setActivePlugin(handler.getPlugin());
                handler.handle(event);
            } catch (Throwable e) {
                this.logger.error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin().metadata().id(), e);
            } finally {
                SpongeCommon.setActivePlugin(null);
            }
        }
        if (event instanceof AbstractEvent) {
            ((AbstractEvent) event).currentOrder = null;
        }
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    private @Nullable EventListenerPhaseContext createPluginContext(final RegisteredListener<?> handler) {
        if (PhaseTracker.getInstance().getPhaseContext().allowsEventListener()) {
            return PluginPhase.Listener.GENERAL_LISTENER.createPhaseContext(PhaseTracker.getInstance())
                .source(handler.getPlugin());
        }
        return null;
    }

    @Override
    public boolean post(final Event event) {
        try {
            if (event instanceof InteractContainerEvent) { // Track usage of Containers
                ((ContainerBridge) ((InteractContainerEvent) event).container()).bridge$setInUse(true);
            }
            // Allow the client thread by default so devs can actually
            // call their own events inside the init events. Only allowing
            // this as long that there is no server available
            return this.post(event, this.getHandlerCache(event).getListeners());
        } finally {
            if (event instanceof InteractContainerEvent) { // Finished using Container
                ((ContainerBridge) ((InteractContainerEvent) event).container()).bridge$setInUse(false);
            }
        }
    }

    public boolean post(final Event event, final PluginContainer plugin) {
        final List<RegisteredListener<?>> listeners = this.getHandlerCache(event).getListeners();
        final List<RegisteredListener<?>> pluginListeners = listeners.stream()
            .filter(l -> l.getPlugin() == plugin)
            .collect(Collectors.toList());
        return this.post(event, pluginListeners);
    }
}
