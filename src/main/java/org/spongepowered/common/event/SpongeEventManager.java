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

import co.aikar.timings.TimingsManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.filter.FilterFactory;
import org.spongepowered.common.event.gen.DefineableClassLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpongeEventManager implements EventManager {

    private final Object lock = new Object();

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
    private final LoadingCache<Class<? extends Event>, RegisteredListener.Cache> handlersCache =
            Caffeine.newBuilder().initialCapacity(150).build((eventClass) -> bakeHandlers(eventClass));

    @Inject
    public SpongeEventManager(PluginManager pluginManager) {
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
            SpongeImpl.getLogger().warn("Failed to set event cache backing array, type was " + this.handlersCache.getClass().getName());
            SpongeImpl.getLogger().warn("  Caused by: " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    <T extends Event> RegisteredListener.Cache bakeHandlers(Class<T> rootEvent) {
        List<RegisteredListener<?>> handlers = Lists.newArrayList();
        Set<Class<? super T>> types = TypeToken.of(rootEvent).getTypes().rawTypes();

        synchronized (this.lock) {
            for (Class<? super T> type : types) {
                if (Event.class.isAssignableFrom(type)) {
                    handlers.addAll(this.handlersByEvent.get(type));
                }
            }
        }

        Collections.sort(handlers);
        return new RegisteredListener.Cache(handlers);
    }

    private static boolean isValidHandler(Method method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isAbstract(modifiers)
                || method.getDeclaringClass().isInterface()
                || method.getReturnType() != void.class) {
            return false;
        }

        Class<?>[] parameters = method.getParameterTypes();
        return parameters.length >= 1 && Event.class.isAssignableFrom(parameters[0]);
    }

    private void register(RegisteredListener<? extends Event> handler) {
        register(Collections.singletonList(handler));
    }

    private void register(List<RegisteredListener<? extends Event>> handlers) {
        synchronized (this.lock) {
            boolean changed = false;

            for (RegisteredListener<?> handler : handlers) {
                if (this.handlersByEvent.put(handler.getEventClass(), handler)) {
                    changed = true;
                    this.checker.registerListenerFor(handler.getEventClass());
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }
        }
    }

    /*private void enableFields(Collection<RegisteredListener<? extends Event>> handlers) {
        for (RegisteredListener<?> handler: handlers) {
            if (this.hasAnyListeners(handler.getEventClass())) {
                // We don't need to do a check for every subtype
                this.checker.updateFields(handler.getEventClass(), k -> true);
            }
        }
    }

    private void disableFields(Collection<RegisteredListener<? extends Event>> handlers) {
        for (RegisteredListener<?> handler: handlers) {
            this.checker.updateFields(handler.getEventClass(), this::hasAnyListeners);
        }
    }*/

    // Override in SpongeModEventManager
    protected boolean hasAnyListeners(Class<? extends Event> clazz) {
        return !this.handlersCache.get(clazz).getListeners().isEmpty();
    }

    public void registerListeners(PluginContainer plugin, Object listenerObject) {
        checkNotNull(plugin, "plugin");
        checkNotNull(listenerObject, "listener");

        if (this.registeredListeners.contains(listenerObject)) {
            SpongeImpl.getLogger().warn("Plugin {} attempted to register an already registered listener ({})", plugin.getId(),
                    listenerObject.getClass().getName());
            Thread.dumpStack();
            return;
        }

        List<RegisteredListener<? extends Event>> handlers = Lists.newArrayList();

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
                if (isValidHandler(method)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                    AnnotatedEventListener handler;
                    try {
                        handler = handlerFactory.create(listenerObject, method);
                    } catch (Exception e) {
                        SpongeImpl.getLogger().error("Failed to create handler for {} on {}", method, handle, e);
                        continue;
                    }

                    handlers.add(createRegistration(plugin, eventClass, listener, handler));
                } else {
                    SpongeImpl.getLogger().warn("The method {} on {} has @{} but has the wrong signature", method, handle.getName(),
                            Listener.class.getName());
                }
            }
        }

        this.registeredListeners.add(listenerObject);
        register(handlers);
    }

    private static <T extends Event> RegisteredListener<T> createRegistration(PluginContainer plugin, Class<T> eventClass, Listener listener,
            EventListener<? super T> handler) {
        return createRegistration(plugin, eventClass, listener.order(), listener.beforeModifications(), handler);
    }

    private static <T extends Event> RegisteredListener<T> createRegistration(PluginContainer plugin, Class<T> eventClass, Order order,
            boolean beforeModifications, EventListener<? super T> handler) {
        return new RegisteredListener<>(plugin, eventClass, order, handler, beforeModifications);
    }

    private PluginContainer getPlugin(Object plugin) {
        Optional<PluginContainer> container = this.pluginManager.fromInstance(plugin);
        checkArgument(container.isPresent(), "Unknown plugin: %s", plugin);
        return container.get();
    }

    @Override
    public void registerListeners(Object plugin, Object listener) {
        registerListeners(getPlugin(plugin), listener);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, Class<T> eventClass, EventListener<? super T> handler) {
        registerListener(plugin, eventClass, Order.DEFAULT, handler);
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, Class<T> eventClass, Order order, EventListener<? super T> handler) {
        register(createRegistration(getPlugin(plugin), eventClass, order, false, handler));
    }

    @Override
    public <T extends Event> void registerListener(Object plugin, Class<T> eventClass, Order order, boolean beforeModifications,
            EventListener<? super T> handler) {
        register(createRegistration(getPlugin(plugin), eventClass, order, beforeModifications, handler));
    }

    private void unregister(Predicate<RegisteredListener<?>> unregister) {
        synchronized (this.lock) {
            boolean changed = false;

            Iterator<RegisteredListener<?>> itr = this.handlersByEvent.values().iterator();
            while (itr.hasNext()) {
                RegisteredListener<?> handler = itr.next();
                if (unregister.test(handler)) {
                    itr.remove();
                    changed = true;
                    this.checker.unregisterListenerFor(handler.getEventClass());
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }
        }
    }

    @Override
    public void unregisterListeners(final Object listener) {
        checkNotNull(listener, "listener");
        unregister(handler -> listener.equals(handler.getHandle()));
        this.registeredListeners.remove(listener);
    }

    @Override
    public void unregisterPluginListeners(Object pluginObj) {
        final PluginContainer plugin = getPlugin(pluginObj);
        unregister(handler -> plugin.equals(handler.getPlugin()));
    }

    protected RegisteredListener.Cache getHandlerCache(Event event) {
        return this.handlersCache.get(checkNotNull(event, "event").getClass());
    }

    @SuppressWarnings("unchecked")
    protected static boolean post(Event event, List<RegisteredListener<?>> handlers) {
        TimingsManager.PLUGIN_EVENT_HANDLER.startTimingIfSync();
        for (@SuppressWarnings("rawtypes") RegisteredListener handler : handlers) {
            try {
                handler.getTimingsHandler().startTimingIfSync();
                ((AbstractEvent) event).currentOrder = handler.getOrder();
                handler.handle(event);
                handler.getTimingsHandler().stopTimingIfSync();
            } catch (Throwable e) {
                handler.getTimingsHandler().stopTimingIfSync();
                SpongeImpl.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin(), e);
            }
        }
        TimingsManager.PLUGIN_EVENT_HANDLER.stopTimingIfSync();
        ((AbstractEvent) event).currentOrder = null;

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Override
    public boolean post(Event event) {
        return post(event, getHandlerCache(event).getListeners());
    }

    public boolean post(Event event, boolean allowClientThread) {
        return post(event);
    }

    public boolean post(Event event, Order order) {
        return post(event, getHandlerCache(event).getListenersByOrder(order));
    }

}
