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
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventHandler;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.common.Sponge;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SpongeEventManager implements EventManager {

    private final Object lock = new Object();

    private final PluginManager pluginManager;
    private final AnnotatedEventHandler.Factory handlerFactory = new ClassEventHandlerFactory("org.spongepowered.common.event.handler");
    private final Multimap<Class<?>, RegisteredHandler<?>> handlersByEvent = HashMultimap.create();

    /**
     * A cache of all the handlers for an event type for quick event posting.
     * <p>The cache is currently entirely invalidated if handlers are added or removed.</p>
     */
    private final LoadingCache<Class<? extends Event>, RegisteredHandler.Cache> handlersCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends Event>, RegisteredHandler.Cache>() {
                @Override
                public RegisteredHandler.Cache load(Class<? extends Event> eventClass) throws Exception {
                    return bakeHandlers(eventClass);
                }
            });

    @Inject
    public SpongeEventManager(PluginManager pluginManager) {
        this.pluginManager = checkNotNull(pluginManager, "pluginManager");
    }

    private RegisteredHandler.Cache bakeHandlers(Class<?> rootEvent) {
        List<RegisteredHandler<?>> handlers = Lists.newArrayList();
        @SuppressWarnings({"unchecked", "rawtypes"})
        Set<Class<?>> types = (Set) TypeToken.of(rootEvent).getTypes().rawTypes();

        synchronized (this.lock) {
            for (Class<?> type : types) {
                if (Event.class.isAssignableFrom(type)) {
                    handlers.addAll(this.handlersByEvent.get(type));
                }
            }
        }

        Collections.sort(handlers);
        return new RegisteredHandler.Cache(handlers);
    }

    private static boolean isValidHandler(Method method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isAbstract(modifiers)
                || method.getDeclaringClass().isInterface()
                || method.getReturnType() != void.class) {
            return false;
        }

        Class<?>[] parameters = method.getParameterTypes();
        return parameters.length == 1 && Event.class.isAssignableFrom(parameters[0]);
    }

    private void register(RegisteredHandler<?> handler) {
        register(Collections.<RegisteredHandler<?>>singletonList(handler));
    }

    private void register(List<RegisteredHandler<?>> handlers) {
        synchronized (this.lock) {
            boolean changed = false;

            for (RegisteredHandler<?> handler : handlers) {
                if (this.handlersByEvent.put(handler.getEventClass(), handler)) {
                    changed = true;
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }
        }
    }

    public void register(PluginContainer plugin, Object listener) {
        checkNotNull(plugin, "plugin");
        checkNotNull(listener, "listener");

        List<RegisteredHandler<?>> handlers = Lists.newArrayList();

        Class<?> handle = listener.getClass();
        for (Method method : handle.getMethods()) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                if (isValidHandler(method)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                    AnnotatedEventHandler handler;
                    try {
                        handler = this.handlerFactory.create(listener, method);
                    } catch (Exception e) {
                        Sponge.getLogger().error("Failed to create handler for {} on {}", method, handle, e);
                        continue;
                    }

                    handlers.add(createRegistration(plugin, eventClass, subscribe, handler));
                } else {
                    Sponge.getLogger().warn("The method {} on {} has @{} but has the wrong signature", method, handle.getName(),
                            Subscribe.class.getName());
                }
            }
        }

        register(handlers);
    }

    private static <T extends Event> RegisteredHandler<T> createRegistration(PluginContainer plugin, Class<T> eventClass, Subscribe subscribe,
            EventHandler<? super T> handler) {
        return createRegistration(plugin, eventClass, subscribe.order(), subscribe.ignoreCancelled(), handler);
    }

    private static <T extends Event> RegisteredHandler<T> createRegistration(PluginContainer plugin, Class<T> eventClass, Order order,
            boolean ignoreCancelled, EventHandler<? super T> handler) {
        return new RegisteredHandler<T>(plugin, eventClass, order, handler, ignoreCancelled);
    }

    private PluginContainer getPlugin(Object plugin) {
        Optional<PluginContainer> container = this.pluginManager.fromInstance(plugin);
        checkArgument(container.isPresent(), "Unknown plugin: %s", plugin);
        return container.get();
    }

    @Override
    public void register(Object plugin, Object listener) {
        register(getPlugin(plugin), listener);
    }

    @Override
    public <T extends Event> void register(Object plugin, Class<T> eventClass, EventHandler<? super T> handler) {
        register(plugin, eventClass, Order.DEFAULT, handler);
    }

    @Override
    public <T extends Event> void register(Object plugin, Class<T> eventClass, Order order, EventHandler<? super T> handler) {
        register(createRegistration(getPlugin(plugin), eventClass, order, false, handler));
    }

    private void unregister(Predicate<RegisteredHandler<?>> unregister) {
        synchronized (this.lock) {
            boolean changed = false;

            Iterator<RegisteredHandler<?>> itr = this.handlersByEvent.values().iterator();
            while (itr.hasNext()) {
                RegisteredHandler<?> handler = itr.next();
                if (unregister.apply(handler)) {
                    itr.remove();
                    changed = true;
                }
            }

            if (changed) {
                this.handlersCache.invalidateAll();
            }
        }
    }

    @Override
    public void unregister(final Object listener) {
        checkNotNull(listener, "listener");
        unregister(new Predicate<RegisteredHandler<?>>() {

            @Override
            public boolean apply(RegisteredHandler<?> handler) {
                return listener.equals(handler.getHandle());
            }
        });
    }

    @Override
    public void unregisterPlugin(Object pluginObj) {
        final PluginContainer plugin = getPlugin(pluginObj);
        unregister(new Predicate<RegisteredHandler<?>>() {

            @Override
            public boolean apply(RegisteredHandler<?> handler) {
                return plugin.equals(handler.getPlugin());
            }
        });
    }

    protected RegisteredHandler.Cache getHandlerCache(Event event) {
        return this.handlersCache.getUnchecked(checkNotNull(event, "event").getClass());
    }

    @SuppressWarnings("unchecked")
    protected static boolean post(Event event, List<RegisteredHandler<?>> handlers) {
        for (@SuppressWarnings("rawtypes") RegisteredHandler handler : handlers) {
            try {
                handler.handle(event);
            } catch (Throwable e) {
                Sponge.getLogger().error("Could not pass {} to {}", event.getClass().getSimpleName(), handler.getPlugin(), e);
            }
        }

        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    @Override
    public boolean post(Event event) {
        return post(event, getHandlerCache(event).getHandlers());
    }

    public boolean post(Event event, Order order) {
        return post(event, getHandlerCache(event).getHandlersByOrder(order));
    }

    @Override
    public <T extends Event> void register(Object plugin, Class<T> eventClass, Order order, boolean beforeModifications,
            EventHandler<? super T> handler) {
        // TODO Auto-generated method stub
    }

}
