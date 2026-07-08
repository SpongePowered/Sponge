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
package org.spongepowered.vanilla.applaunch.transformation;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.common.applaunch.transformation.SuperclassChangeTransformer;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.discovery.ResourceLoading;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class VanillaSuperclassChangeTransformer extends SuperclassChangeTransformer implements ITransformer<ClassNode> {
    private static final String MIXIN_PLUGIN_REASON = "mixin";
    private static final String[] LABELS = { SuperclassChangeTransformer.NAME };

    private final PluginDiscovery discovery;

    public VanillaSuperclassChangeTransformer(final PluginDiscovery discovery) {
        this.discovery = discovery;
    }

    @Override
    public String[] labels() {
        return LABELS;
    }

    @Override
    public ClassNode transform(final ClassNode input, final ITransformerVotingContext context) {
        this.transform(input);
        return input;
    }

    @Override
    public TransformerVoteResult castVote(final ITransformerVotingContext context) {
        return switch (context.getReason()) {
            case ITransformerActivity.CLASSLOADING_REASON, VanillaSuperclassChangeTransformer.MIXIN_PLUGIN_REASON -> TransformerVoteResult.YES;
            default -> TransformerVoteResult.NO;
        };
    }

    @Override
    public Set<Target> targets() {
        return this.targetClasses().stream().map(Target::targetClass).collect(Collectors.toSet());
    }

    @Override
    protected Collection<URL> collectResources() {
        final Collection<URL> resources = new ArrayList<>();
        for (final PluginResource plugin : this.discovery.resources(ResourceLoading.GAME_LIBRARY)) {
            final Optional<String> attribute = plugin.property(SuperclassChangeTransformer.MANIFEST_ATTRIBUTE);
            if (attribute.isPresent()) {
                for (final String scPath : attribute.get().split(",")) {
                    try {
                        resources.add(plugin.locateResource(scPath).get().toURL());
                    } catch (final Exception e) {
                        LOGGER.warn("Failed to locate superclass changer {} from {}", scPath, plugin);
                    }
                }
            }
        }
        return resources;
    }
}
