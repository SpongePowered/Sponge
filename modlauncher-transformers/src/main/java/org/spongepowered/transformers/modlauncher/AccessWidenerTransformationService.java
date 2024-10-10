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
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;
import net.fabricmc.accesswidener.AccessWidenerReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@DefaultQualifier(NonNull.class)
public class AccessWidenerTransformationService implements ITransformationService {

    public static final String NAME = "access_widener";
    public static final String ACCESS_WIDENER_EXTENSION = "accesswidener";
    public static final Supplier<TypesafeMap.Key<AccessWidenerTransformationService>>
        INSTANCE = IEnvironment.buildKey("sponge:aw", AccessWidenerTransformationService.class);
    static final Logger LOGGER = LogManager.getLogger();

    private final AccessWidener widener = new AccessWidener();
    private final AccessWidenerReader reader = new AccessWidenerReader(this.widener);
    private @MonotonicNonNull OptionSpec<String> configSpec;

    @NonNull
    @Override
    public String name() {
        return AccessWidenerTransformationService.NAME;
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        env.computePropertyIfAbsent(AccessWidenerTransformationService.INSTANCE.get(), k -> this);
    }

    @Override
    public void arguments(final BiFunction<String, String, OptionSpecBuilder> argumentBuilder) {
        this.configSpec = argumentBuilder.apply("config", "An AW file to apply at runtime")
            .withOptionalArg()
            .withValuesSeparatedBy(",");
    }

    @Override
    public void argumentValues(final OptionResult option) {
        if (this.configSpec != null) {
            for (final String value : option.values(this.configSpec)) {
                final URL resource = AccessWidenerTransformationService.class.getClassLoader().getResource(value);
                if (resource == null) {
                    throw new IllegalStateException("Could not locate AW resource specified on command line " + value);
                }
                this.offerResource(resource, "command-line");
            }
        }
        ITransformationService.super.argumentValues(option);
    }

    @Override
    public void initialize(final IEnvironment environment) {
    }

    @Override
    @SuppressWarnings("rawtypes") // :(
    public @NonNull List<ITransformer> transformers() {
        return Collections.singletonList(new AWTransformer(this.widener));
    }

    // register additional AWs with the transformation service
    public void offerResource(final URL resource, final String name) {
        if (resource.getFile().endsWith(AccessWidenerTransformationService.ACCESS_WIDENER_EXTENSION)) {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
                AccessWidenerTransformationService.LOGGER.debug("Reading access widener {} from {}", name, resource);
                this.reader.read(reader);
            } catch (final IOException ex) {
                AccessWidenerTransformationService.LOGGER.error("Failed to load access widener {} from {}", name, resource, ex);
            }
        } else {
            AccessWidenerTransformationService.LOGGER.warn("Offered access widener {} from {} that does not end with expected extension '{}'",
                name, resource, AccessWidenerTransformationService.ACCESS_WIDENER_EXTENSION);
        }
    }

    static class AWTransformer implements ITransformer<ClassNode> {
        private final AccessWidener widener;

        AWTransformer(final AccessWidener widener) {
            this.widener = widener;
        }

        @Override
        public @NonNull ClassNode transform(final ClassNode input, final ITransformerVotingContext context) {
            AccessWidenerTransformationService.LOGGER.debug("Transforming {}", context.getClassName());
            final ClassNode output = new ClassNode(Opcodes.ASM9);
            final ClassVisitor visitor = AccessWidenerClassVisitor.createClassVisitor(Opcodes.ASM9, output, this.widener);
            input.accept(visitor);
            return output;
        }

        @Override
        public @NonNull TransformerVoteResult castVote(final ITransformerVotingContext context) {
            final TransformerVoteResult result = context.getReason().equals(ITransformerActivity.CLASSLOADING_REASON) ? TransformerVoteResult.YES : TransformerVoteResult.NO;
            AccessWidenerTransformationService.LOGGER.debug("Voting on {} with reason {}, result: {}", context.getClassName(), context.getReason(), result);
            return result;
        }

        @Override
        public @NonNull Set<Target> targets() {
            final Set<String> classNames = this.widener.getTargets();
            final Set<Target> targets = new HashSet<>(classNames.size());
            for (final String clazz : classNames) {
                targets.add(Target.targetClass(clazz.replace('.', '/')));
            }
            return targets;
        }
    }
}
