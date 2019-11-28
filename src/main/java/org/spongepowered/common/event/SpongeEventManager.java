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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import org.spongepowered.common.event.tracking.phase.plugin.EventListenerPhaseContext;
import org.spongepowered.common.relocate.co.aikar.timings.TimingsManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.filter.FilterFactory;
import org.spongepowered.common.event.gen.DefineableClassLoader;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.item.inventory.custom.CustomInventory;
import org.spongepowered.common.item.inventory.custom.CustomInventoryListener;
import org.spongepowered.common.util.TypeTokenHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpongeEventManager implements EventManager {

    private static final TypeVariable<?> GENERIC_EVENT_TYPE = GenericEvent.class.getTypeParameters()[0];

    private final Object lock = new Object();
    protected final Logger logger;
    private final PluginManager pluginManager;
    private final Multimap<Class<?>, RegisteredListener<?>> handlersByEvent = HashMultimap.create();
    private final Map<ClassLoader, AnnotatedEventListener.Factory> classLoaders = Maps.newHashMap();
    private final Set<Object> registeredListeners = Sets.newHashSet();

    public final ListenerChecker checker = new ListenerChecker(ShouldFire.class);

    /**
     * A cache of all the handlers for an event type for quick event posting.
     * <p>The cache is currently entirely invalidated if handlers are added or
     * removed.</p>
     */
    protected final LoadingCache<EventType<?>, RegisteredListener.Cache> handlersCache =
            Caffeine.newBuilder().initialCapacity(150).build(this::bakeHandlers);

    @Inject
    public SpongeEventManager(Logger logger, PluginManager pluginManager) {
        this.logger = logger;
        this.pluginManager = checkNotNull(pluginManager, "pluginManager");

        // Caffeine offers no control over the concurrency level of the
        // ConcurrentHashMap which backs the cache. By default this concurrency
        // level is 16. We replace the backing map before any use can occur
        // a new ConcurrentHashMap with a concurrency level of 1
        try {
            // Cache impl class is UnboundedLocalLoadingCache which extends
            // UnboundedLocalManualCache

            // UnboundedLocalManualCache has a field 'cache' with an
            // UnboundedLocalCache which contains the actual backing map
            Field innerCache = this.handlersCache.getClass().getSuperclass().getDeclaredField("cache");
            innerCache.setAccessible(true);
            Object innerCacheValue = innerCache.get(this.handlersCache);
            Class<?> innerCacheClass = innerCacheValue.getClass(); // UnboundedLocalCache
            Field cacheData = innerCacheClass.getDeclaredField("data");
            cacheData.setAccessible(true);
            ConcurrentHashMap<Class<? extends Event>, RegisteredListener.Cache> newBackingData = new ConcurrentHashMap<>(150, 0.75f, 1);
            cacheData.set(innerCacheValue, newBackingData);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            this.logger.warn("Failed to set event cache backing array, type was " + this.handlersCache.getClass().getName());
            this.logger.warn("  Caused by: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    <T extends Event> RegisteredListener.Cache bakeHandlers(EventType<T> eventType) {
        final List<RegisteredListener<?>> handlers = new ArrayList<>();
        final Set<Class<? super T>> types = TypeToken.of(eventType.getType()).getTypes().rawTypes();

        synchronized (this.lock) {
            for (Class<? super T> type : types) {
                if (Event.class.isAssignableFrom(type)) {
                    final Collection<RegisteredListener<?>> listeners = this.handlersByEvent.get(type);
                    if (GenericEvent.class.isAssignableFrom(type)) {
                        final TypeToken<?> genericType = eventType.getGenericType();
                        checkNotNull(genericType);
                        for (RegisteredListener<?> listener : listeners) {
                            final TypeToken<?> genericType1 = listener.getEventType().getGenericType();
                            checkNotNull(genericType1);
                            if (TypeTokenHelper.isAssignable(genericType, genericType1)) {
                                handlers.add(listener);
                            }
                        }
                    } else {
                        handlers.addAll(listeners);
                    }
                }
            }
        }

        Collections.sort(handlers);
        return new RegisteredListener.Cache(handlers);
    }

    @Nullable
    private static String getHandlerErrorOrNull(Method method) {
        int modifiers = method.getModifiers();
        List<String> errors = new ArrayList<>();
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
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length == 0 || !Event.class.isAssignableFrom(parameters[0])) {
            errors.add("method must have an Event as its first parameter");
        }

        if (errors.isEmpty()) {
            return null;
        }
        return String.join(", ", errors);
    }

    private void register(RegisteredListener<? extends Event> handler) {
        register(Collections.singletonList(handler));
    }

    private void register(List<RegisteredListener<? extends Event>> handlers) {
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

    @SuppressWarnings("unchecked")
    private void registerListener(PluginContainer plugin, Object listenerObject) {
        checkNotNull(plugin, "plugin");
        checkNotNull(listenerObject, "listener");

        if (this.registeredListeners.contains(listenerObject)) {
            this.logger.warn("Plugin {} attempted to register an already registered listener ({})", plugin.getId(),
                    listenerObject.getClass().getName());
            Thread.dumpStack();
            return;
        }

        List<RegisteredListener<? extends Event>> handlers = Lists.newArrayList();
        Map<Method, String> methodErrors = new HashMap<>();

        Class<?> handle = listenerObject.getClass();
        ClassLoader handleLoader = handle.getClassLoader();

        AnnotatedEventListener.Factory handlerFactory = classLoaders.get(handleLoader);
        if (handlerFactory == null) {
            final DefineableClassLoader classLoader = new DefineableClassLoader(handleLoader);
            handlerFactory = new ClassEventListenerFactory("org.spongepowered.common.event.listener",
                    new FilterFactory("org.spongepowered.common.event.filters", classLoader), classLoader);
            classLoaders.put(handleLoader, handlerFactory);
        }

        for (Method method : handle.getMethods()) {
            Listener listener = method.getAnnotation(Listener.class);
            if (listener != null) {
                String error = getHandlerErrorOrNull(method);
                if (error == null) {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    final TypeToken eventType = TypeToken.of(method.getGenericParameterTypes()[0]);
                    AnnotatedEventListener handler;
                    try {
                        handler = handlerFactory.create(listenerObject, method);
                    } catch (Exception e) {
                        this.logger.error("Failed to create handler for {} on {}", method, handle, e);
                        continue;
                    }

                    handlers.add(createRegistration(plugin, eventType, listener, handler));
                } else {
                    methodErrors.put(method, error);
                }
            }
        }

        // getMethods() doesn't return private methods. Do another check to warn
        // about those.
        for (Class<?> handleParent = handle; handleParent != Object.class; handleParent = handleParent.getSuperclass()) {
            for (Method method : handleParent.getDeclaredMethods()) {
                if (method.getAnnotation(Listener.class) != null && !methodErrors.containsKey(method)) {
                    String error = getHandlerErrorOrNull(method);
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
        register(handlers);
    }

    private static <T extends Event> RegisteredListener<T> createRegistration(PluginContainer plugin, TypeToken<T> eventClass, Listener listener,
            EventListener<? super T> handler) {
        return createRegistration(plugin, eventClass, listener.order(), listener.beforeModifications(), handler);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Event> RegisteredListener<T> createRegistration(PluginContainer plugin, TypeToken<T> eventType, Order order,
            boolean beforeModifications, EventListener<? super T> handler) {
        TypeToken<?> genericType = null;
        if (GenericEvent.class.isAssignableFrom(eventType.getRawType())) {
            genericType = eventType.resolveType(GENERIC_EVENT_TYPE);
        }
        return new RegisteredListener(plugin, new EventType(eventType.getRawType(), genericType), order, handler, beforeModifications);
    }

    private PluginContainer getPlugin(Object plugin) {
        Optional<PluginContainer> container = this.pluginManager.fromInstance(plugin);
        checkArgument(container.isPresent(), "Unknown plugin: %s", plugin);
        return container.get();
    }

    @Override
    public void registerListeners(Object plugin, Object listener) {
        registerListener(getPlugin(plugin), listener);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, Class<T> eventClass, EventListener<? super T> listener) {
        registerListener(plugin, eventClass, Order.DEFAULT, listener);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, TypeToken<T> eventType, EventListener<? super T> listener) {
        registerListener(plugin, eventType, Order.DEFAULT, listener);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, Class<T> eventClass, Order order, EventListener<? super T> listener) {
        registerListener(plugin, eventClass, Order.DEFAULT, false, listener);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, TypeToken<T> eventType, Order order, EventListener<? super T> listener) {
        registerListener(plugin, eventType, Order.DEFAULT, false, listener);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, Class<T> eventClass, Order order, boolean beforeModifications,
            EventListener<? super T> listener) {
        registerListener(plugin, TypeToken.of(eventClass), Order.DEFAULT, false, listener);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, TypeToken<T> eventType, Order order, boolean beforeModifications,
            EventListener<? super T> listener) {
        register(createRegistration(getPlugin(plugin), eventType, order, beforeModifications, listener));
    }

    private void unregister(Predicate<RegisteredListener<?>> unregister) {
        boolean changed = false;

        synchronized (this.lock) {
            Iterator<RegisteredListener<?>> itr = this.handlersByEvent.values().iterator();
            while (itr.hasNext()) {
                RegisteredListener<?> handler = itr.next();
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

        if (listener instanceof CustomInventory) {
            unregister(handler -> handler.getHandle() instanceof CustomInventoryListener && listener.equals(((CustomInventoryListener) handler.getHandle()).getInventory()));
        } else {
            unregister(handler -> listener.equals(handler.getHandle()));
        }
    }

    @Override
    public void unregisterPluginListeners(Object pluginObj) {
        final PluginContainer plugin = getPlugin(pluginObj);
        unregister(handler -> plugin.equals(handler.getPlugin()));
    }

    @SuppressWarnings({"ConstantConditions", "unchecked", "rawtypes"})
    protected RegisteredListener.Cache getHandlerCache(Event event) {
        checkNotNull(event, "event");
        final Class<? extends Event> eventClass = event.getClass();
        final EventType<? extends Event> eventType;
        if (event instanceof GenericEvent) {
            eventType = new EventType(eventClass, checkNotNull(((GenericEvent) event).getGenericType()));
        } else {
            eventType = new EventType(eventClass, null);
        }
        return this.handlersCache.get(eventType);
    }

    @SuppressWarnings("unchecked")
    private boolean post(Event event, List<RegisteredListener<?>> handlers) {
        if (!Sponge.getServer().isMainThread()) {
            // If this event is being posted asynchronously then we don't want
            // to do any timing or cause stack changes
            for (@SuppressWarnings("rawtypes") RegisteredListener handler : handlers) {
                try {
                    if (event instanceof AbstractEvent) {
                        ((AbstractEvent) event).currentOrder = handler.getOrder();
                    }
                    handler.handle(event);
                } catch (Throwable e) {
                    SpongeImpl.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin(), e);
                }
            }
            if (event instanceof AbstractEvent) {
                ((AbstractEvent) event).currentOrder = null;
            }
            return event instanceof Cancellable && ((Cancellable) event).isCancelled();
        }
        TimingsManager.PLUGIN_EVENT_HANDLER.startTimingIfSync();
        for (@SuppressWarnings("rawtypes") RegisteredListener handler : handlers) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                 final PhaseContext<?> context = createPluginContext(handler);
                 final Timing timings = handler.getTimingsHandler()) {
                frame.pushCause(handler.getPlugin());
                if (context != null) {
                    context.buildAndSwitch();
                }
                timings.startTimingIfSync();
                if (event instanceof AbstractEvent) {
                    ((AbstractEvent) event).currentOrder = handler.getOrder();
                }
                handler.handle(event);
            } catch (Throwable e) {
                // TODO - add some better handling, especially since we have the stakc frame and phase context to boot
                final PrettyPrinter printer = new PrettyPrinter(60).add("Error with event listener handling").centre().hr();
                printer.add("A listener threw an exception while being handled, this is usually not a sponge bug.");
                this.logger.error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin(), e);
            }
        }
        if (event instanceof AbstractEvent) {
            ((AbstractEvent) event).currentOrder = null;
        }
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Nullable
    private EventListenerPhaseContext createPluginContext(RegisteredListener<?> handler) {
        if (PhaseTracker.getInstance().getCurrentState().allowsEventListener()) {
            return PluginPhase.Listener.GENERAL_LISTENER.createPhaseContext()
                .source(handler.getPlugin());
        }
        return null;
    }

    @Override
    public boolean post(Event event) {
        try {
            if (event instanceof InteractContainerEvent) { // Track usage of Containers
                ((ContainerBridge) ((InteractContainerEvent) event).getTargetInventory()).bridge$setInUse(true);
            }
            // Allow the client thread by default so devs can actually
            // call their own events inside the init events. Only allowing
            // this as long that there is no server available
            return post(event, !Sponge.isServerAvailable());
        } finally {
            if (event instanceof InteractContainerEvent) { // Finished using Container
                ((ContainerBridge) ((InteractContainerEvent) event).getTargetInventory()).bridge$setInUse(false);
            }
        }
    }

    public boolean postServer(Event event) {
        return post(event, false);
    }

    public boolean post(Event event, boolean allowClientThread) {
        return post(event, getHandlerCache(event).getListeners());
    }

    public boolean post(Event event, PluginContainer plugin) {
        return post(event, getHandlerCache(event).getListeners().stream()
                .filter(l -> l.getPlugin().equals(plugin))
                .collect(Collectors.toList()));
    }
}
