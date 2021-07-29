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
package org.spongepowered.vanilla.launch.event;

import com.google.common.base.CaseFormat;
import io.leangen.geantyref.GenericTypeReflector;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.configurate.util.Types;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ListenerChecker {

    private static final boolean ALL_TRUE = Boolean.parseBoolean(System.getProperty("sponge.shouldFireAll", "").toLowerCase());
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("sponge.debugShouldFire", "").toLowerCase());

    private final Class<?> clazz;
    private Map<String, FieldData> fields = new HashMap<>();
    private Map<Class<?>, FieldData> fieldClassMap = new IdentityHashMap<>();

    private static String getName(Class<?> clazz) {
        // Properly account for inner classes. Class#getName uses a $
        // to separate inner classes, so the last '.' is the end of the package name
        //
        String name = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1).replace("$", "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
    }

    public ListenerChecker(Class<?> clazz) {
        this.clazz = clazz;
        for (Field field: this.clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                FieldData data = new FieldData(field);
                this.fieldClassMap.put(this.getClassForField(field), data);
                this.fields.put(field.getName(), data);
                if (ListenerChecker.ALL_TRUE) {
                    if (ListenerChecker.DEBUG) {
                        System.err.println(String.format("Forcing field %s to true!", field.getName()));
                    }
                    try {
                        field.set(null, true);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                throw new IllegalStateException(String.format("ShouldFire field %s must be public and static!", field));
            }
        }
    }

    public <T> void registerListenerFor(Class<T> eventClass) {
        this.updateFields(eventClass, true);
    }

    public <T> void unregisterListenerFor(Class<T> eventClass) {
        this.updateFields(eventClass, false);
    }

    private Class<?> getClassForField(final Field field) {
        final String name = field.getName();

        final Method[] methods = SpongeEventFactory.class.getMethods();
        for (final Method eventMethod : methods) {
            // Not all fields will directly correspond to an event in SpongeEventFactory.
            // For example, SpongeEventFactory has no method to create a ChangeBlockEvent,
            // (only methods for its subtypes), but ShouldFire.CHANGE_BLOCK_EVENT exists, and is valid
            // Therefore, we check all superinterfaces of each listed event.
            final Class<?> possibleType = Types.allSuperTypesAndInterfaces(eventMethod.getGenericReturnType())
                    .map(GenericTypeReflector::erase)
                    .filter(eventType -> {
                        final String eventMethodName = ListenerChecker.getName(eventType);
                        return name.equals(eventMethodName);
                    })
                    .findFirst().orElse(null);

            if (possibleType != null) {
                return possibleType;
            }
        }
        throw new IllegalStateException(String.format("ShouldFire field %s does not correspond to any SpongeAPI event! Check that the field is written in UPPER_CASE_UNDERSCORE format.", field));
    }

    public <T> void updateFields(Class<? super T> eventClass, boolean registering) {
        if (ListenerChecker.ALL_TRUE) {
            return;
        }

        // Walk up the tree. The event class and all its supertypes have their
        // flags touched, because there is a possibility of them being fired.

        // For example, if a listener is registered for SpawnEntityEvent.Custom,
        // then SpawnEntityEvent.Custom, SpawnEntityEvent, and Event (plus others)
        // will have their flags set to 'true'. When the implementation checks
        // ShouldFire.SPAWN_ENTITY_EVENT, it will be 'true', because there is a possibility
        // than something that matches the type 'SpawnEntityEvent' will have a listener.
        // The actual type might be SpawnEntityEvent.ChunkLoad, which might have no listeners.
        // However, the base flags takes into account that some of the subevent *do* have listeners.

        // We also need to walk down the tree, to ensure that we update any more specific flags.
        // For example, registering a listener for SpawnEntityEvent should cause ShouldFire.DROP_ITEM_EVENT_DISPENSE
        // to be set to 'true'. This allows the implementation to check the most-specific flag for its particular event,
        // while ensuring that all plugins listening for an event will recieve it

        @SuppressWarnings("unchecked")
        final Set<Class<? super T>> superTypes = Types.allSuperTypesAndInterfaces(eventClass)
                .map(t -> (Class<? super T>) GenericTypeReflector.erase(t))
                .filter(c -> c != eventClass)
                .collect(Collectors.toCollection(ReferenceOpenHashSet::new));

        for (final Map.Entry<Class<?>, FieldData> entry: this.fieldClassMap.entrySet()) {

            // We check for two things:

            // 1. The event class being registered is a supertype of the field type
            // (e.g. a plugin registers a listener for SpawnEntityEvent, and the field is DROP_ITEM_EVENT_DISPENSE)
            // In this case, we want to set the field to 'true', so that the implementation correctly
            // fires a more specific event (e.g. DropEventItem.Dispense) when a more general listener exists
            // (e.g. a listener for SpawnEntityEvent)

            // 2. The field type corresponds to one of the supertypes of the event class
            // (e.g. a plugin registers a listener for DropItemEvent.Dispense, and the field is SPAWN_ENTITY_EVENT)
            // In this case, we still want to set the field to 'true', so that the implementation can check
            // a more general field (e.g. SPAWN_ENTITY_EVENT) without needing to check every single subtype
            // The field type needs to be exactly equal to ensure that we don't turn on every single flag.
            // For example, every event has Event in its hierarchy by definition. If for some reason the
            // field ShouldFire.EVENT existed, we would want to turn it on. However, we would *not*
            // want to turn on any flags that extend from Event (which would be all of them).

            // The implementation can be thought of like this - we activate all of our ancestors
            // in the class hierarchy, and 'fan out' to all of our descendands. However, we do not activate
            // 'siblings' (e.g. registering a listener for SpawnEntityEvent will not active CHANGE_BLOCK_EVENT)
            // This gives maximum flexibility to the implementation - it can check a general flag like SPAWN_ENTITY_EVENT,
            // or a specific flag like DROP_ITEM_EVENT_DISPENSE)

            if (!eventClass.isAssignableFrom(entry.getKey()) && !superTypes.contains(entry.getKey())) {
                continue;
            }

            entry.getValue().update(registering);
        }
    }

    private static class FieldData {
        Field field;
        int listenerCount = 0;

        FieldData(Field field) {
            this.field = field;
        }

        void update(boolean increment) {
            if (increment) {
                this.listenerCount++;
            } else {
                this.listenerCount--;
            }
            if (this.listenerCount < 0) {
                SpongeCommon.logger().error(String.format("Decremented listener count to %s for field %s", this.listenerCount, this.field), new Exception("Dummy exception"));
            }
            boolean val = this.listenerCount > 0;

            if (ListenerChecker.DEBUG) {
                System.err.println(String.format("Updating field %s with value %s", this.field, val));
            }

            try {
                this.field.set(null, val);
            } catch (IllegalAccessException e) {
                SpongeCommon.logger().error(String.format("Error setting field %s to %s", this.field, val), e);
            }
        }
    }

}
