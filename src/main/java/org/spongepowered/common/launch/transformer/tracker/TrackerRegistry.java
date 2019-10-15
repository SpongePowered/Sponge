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
package org.spongepowered.common.launch.transformer.tracker;

import static java.util.Objects.requireNonNull;
import static org.objectweb.asm.Opcodes.ASM5;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class TrackerRegistry {

    static final Map<String, MethodEntry> methodLists = new HashMap<>();
    static final Set<String> trackerClasses = new HashSet<>();
    private static final Map<String, TrackedType> trackedTypes = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Initializes the method tracking.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        Launch.classLoader.addTransformerExclusion("org.spongepowered.common.launch.transformer.tracker.");
        Launch.classLoader.registerTransformer("org.spongepowered.common.launch.transformer.tracker.TrackerClassTransformer");
    }

    /**
     * Registers subtypes for the given base type. Registering sub types removes
     * in those cases the need to do instance checks.
     *
     * @param baseType The base type
     * @param subTypes The sub types
     */
    public static void registerKnownSubtypes(String baseType, String... subTypes) {
        registerKnownSubtypes(baseType, Arrays.asList(subTypes));
    }

    /**
     * Registers subtypes for the given base type. Registering sub types removes
     * in those cases the need to do instance checks.
     *
     * @param baseType The base type
     * @param subTypes The sub types
     */
    public static void registerKnownSubtypes(String baseType, Collection<String> subTypes) {
        trackedTypes.computeIfAbsent(baseType.replace('.', '/'), TrackedType::new).knownSubtypes
                .addAll(subTypes.stream().map(e -> e.replace('.', '/')).collect(Collectors.toList()));
    }

    /**
     * Registers a tracker class, in this class may methods be
     * annotated with {@link TrackerMethod}.
     *
     * @param trackerClass The tracker class
     */
    public static void registerTracker(String trackerClass) {
        requireNonNull(trackerClass, "trackerClass");
        // Don't transform the tracker class with the TrackerClassTransformer
        trackerClasses.add(trackerClass);

        final List<Entry> entries = new ArrayList<>();
        try {
            final String trackedMethodDesc = Type.getDescriptor(TrackerMethod.class);
            final ClassReader classReader = new ClassReader(trackerClass);
            classReader.accept(new ClassVisitor(ASM5) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    return new MethodVisitor(ASM5) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String annoDesc, boolean visible) {
                            if (annoDesc.equals(trackedMethodDesc)) {
                                entries.add(new Entry(name, desc, access));
                            }
                            return super.visitAnnotation(desc, visible);
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        final String trackerType = trackerClass.replace('.', '/');
        for (Entry entry : entries) {
            if (!Modifier.isStatic(entry.access)) {
                throw new IllegalArgumentException("A tracker method must be static, at " +
                        entry.name + ':' + entry.desc + " located in " + trackerClass);
            }
            if (!Modifier.isPublic(entry.access)) {
                throw new IllegalArgumentException("A tracker method must be public, at " +
                        entry.name + ':' + entry.desc + " located in " + trackerClass);
            }
            if (entry.desc.indexOf(')') == 1) {
                throw new IllegalArgumentException("At least one parameter must be present (this is the tracked type), at " +
                        entry.name + ':' + entry.desc + " located in " + trackerClass);
            }
            final int start = entry.desc.indexOf('L');
            if (start != 1) {
                throw new IllegalArgumentException("The tracked type may not be a primitive or array, at " +
                        entry.name + ':' + entry.desc + " located in " + trackerClass);
            }
            final int end = entry.desc.indexOf(';');
            // Extract the target type
            final String targetType = entry.desc.substring(start + 1, end);
            final TrackedType trackedType = trackedTypes.computeIfAbsent(targetType, TrackedType::new);
            // Extract the method desc we need to target/replace
            final String oldDesc = '(' + entry.desc.substring(end + 1);

            // Store the method
            final String id = targetType + ';' + entry.name + ';' + oldDesc;


            final MethodEntry methodEntry = methodLists.computeIfAbsent(id, id1 -> new MethodEntry(
                    Type.getArgumentTypes(oldDesc), Type.getReturnType(oldDesc)));
            if (methodEntry.entries.containsKey(trackedType)) {
                throw new IllegalArgumentException("Attempted to track a method twice, at " +
                        entry.name + ':' + entry.desc + " located in " + trackerClass);
            }
            methodEntry.entries.put(trackedType, new MethodEntry.TargetTracker(trackerType, entry.desc));
        }
    }

    private static final class Entry {

        private final String name;
        private final String desc;
        private final int access;

        private Entry(String name, String desc, int access) {
            this.access = access;
            this.name = name;
            this.desc = desc;
        }
    }
}
