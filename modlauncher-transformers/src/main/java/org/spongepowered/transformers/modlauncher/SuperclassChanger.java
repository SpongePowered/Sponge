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
package org.spongepowered.transformers.modlauncher;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import cpw.mods.modlauncher.api.TypesafeMap;
import io.leangen.geantyref.TypeToken;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@DefaultQualifier(NonNull.class)
public class SuperclassChanger implements ITransformationService {

    public static final String NAME = "superclass_change";
    public static final String SUPER_CLASS_EXTENSION = "superclasschange";
    // Pulled from org.spongepowered.asm.launch.MixinLaunchPluginLegacy as
    // when Mixin prepares target class nodes, it requests other ITransformers
    // to load changes as necessary.
    public static final String MIXIN_PLUGIN_REASON = "mixin";
    public static final Supplier<TypesafeMap.Key<SuperclassChanger>>
        INSTANCE = IEnvironment.buildKey("sponge:scc", SuperclassChanger.class);
    static final Logger LOGGER = LogManager.getLogger();
    private static final TypeToken<Map<String, String>> CONFIG_TOKEN = new TypeToken<Map<String, String>>() {
    };

    private @MonotonicNonNull OptionSpec<String> configSpec;
    private final Map<String, String> superclassTargets = new ConcurrentHashMap<>();

    @NonNull
    @Override
    public String name() {
        return SuperclassChanger.NAME;
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        env.computePropertyIfAbsent(SuperclassChanger.INSTANCE.get(), k -> this);
    }

    @Override
    public void arguments(final BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
        this.configSpec = argumentBuilder.apply("config", "An SCC file to apply at runtime")
            .withRequiredArg()
            .ofType(String.class);
    }

    @Override
    public void argumentValues(final OptionResult option) {
        option.values(this.configSpec).forEach(file -> {
            SuperclassChanger.LOGGER.debug("Loading SCC file: " + file);
            final @Nullable URL resource = SuperclassChanger.class.getClassLoader().getResource(file);
            if (resource == null) {
                SuperclassChanger.LOGGER.warn("Could not find SCC file: " + file);
                return;
            }
            this.offerResource(resource, "command-line");
        });
    }

    @Override
    public void initialize(final IEnvironment environment) {
    }

    @Override
    @SuppressWarnings("rawtypes") // :(
    public @NonNull List<ITransformer> transformers() {
        return Collections.singletonList(
            new SuperclassTransformer(new ConcurrentHashMap<>(this.superclassTargets)));
    }

    // register additional AWs with the transformation service
    public void offerResource(final URL resource, final String name) {
        if (resource.getFile().endsWith(SuperclassChanger.SUPER_CLASS_EXTENSION)) {
            try {
                SuperclassChanger.LOGGER.debug("Reading superclass change {} from {}", name, resource);
                @Nullable final Map<String, String> superClassTargets = JacksonConfigurationLoader.builder()
                    .url(resource)
                    .build()
                    .load()
                    .get(SuperclassChanger.CONFIG_TOKEN);
                if (superClassTargets != null) {
                    this.superclassTargets.putAll(superClassTargets);
                }
            } catch (final IOException ex) {
                SuperclassChanger.LOGGER.error("Failed to load superclass change {} from {}", name, resource, ex);
            }
        } else {
            SuperclassChanger.LOGGER.warn(
                "Offered superclass change {} from {} that does not end with expected extension '{}'",
                name, resource, SuperclassChanger.SUPER_CLASS_EXTENSION
            );
        }
    }

    static class SuperclassTransformer implements ITransformer<ClassNode> {

        private final ConcurrentHashMap<String, String> superclassTargets;

        public SuperclassTransformer(final ConcurrentHashMap<String, String> superclassTargets) {
            this.superclassTargets = superclassTargets;
        }

        @Override
        public @NonNull ClassNode transform(final ClassNode input, final ITransformerVotingContext context) {
            final String inputKey = input.name.replace("/", ".");
            final String newSuperclass = this.superclassTargets.get(inputKey);
            if (newSuperclass == null) {
                SuperclassChanger.LOGGER.warn("No superclass change for {}", inputKey);
                return input;
            }
            final String sanitizedSuperClass = newSuperclass.replace('.', '/');

            input.methods.forEach(m -> SuperclassTransformer.transformMethod(m, input.superName, sanitizedSuperClass));
            input.superName = sanitizedSuperClass;
            return input;
        }

        private static void transformMethod(
            final MethodNode node, final String originalSuperclass, final String superClass
        ) {
            for (final MethodInsnNode insn : SuperclassTransformer.findSuper(node, originalSuperclass)) {
                insn.owner = superClass;
            }
        }

        private static List<MethodInsnNode> findSuper(
            final MethodNode method, final String originalSuperClass
        ) {
            final List<MethodInsnNode> nodes = new ArrayList<>();
            for (final AbstractInsnNode node : method.instructions.toArray()) {
                if (node.getOpcode() == Opcodes.INVOKESPECIAL && originalSuperClass.equals(
                    ((MethodInsnNode) node).owner)) {
                    nodes.add((MethodInsnNode) node);
                }
            }
            return nodes;
        }

        @Override
        public @NonNull TransformerVoteResult castVote(final ITransformerVotingContext context) {
            switch (context.getReason()) {
                case ITransformerActivity.CLASSLOADING_REASON:
                case SuperclassChanger.MIXIN_PLUGIN_REASON:
                    return TransformerVoteResult.YES;
                default:
                    return TransformerVoteResult.NO;
            }
        }

        @Override
        public @NonNull Set<Target> targets() {
            return this.superclassTargets
                .keySet()
                .stream()
                .map(s -> s.replace('.', '/'))
                .map(Target::targetClass)
                .collect(Collectors.toSet());
        }
    }

}
