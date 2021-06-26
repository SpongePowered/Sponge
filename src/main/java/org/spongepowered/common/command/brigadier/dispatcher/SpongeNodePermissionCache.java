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
package org.spongepowered.common.command.brigadier.dispatcher;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.commands.CommandSourceStackBridge;
import org.spongepowered.common.command.brigadier.tree.SpongePermissionWrappedLiteralCommandNode;
import org.spongepowered.common.service.server.permission.SpongePermissions;

import java.util.Collection;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;

public final class SpongeNodePermissionCache {

    private final static Pattern ILLEGAL_CHARS = Pattern.compile("[^a-zA-Z0-9]");
    private final static WeakHashMap<CommandNode<CommandSourceStack>, Supplier<String>> PERMISSION_MAP = new WeakHashMap<>();

    public static boolean canUse(
            final boolean isRoot,
            final SpongeCommandDispatcher dispatcher,
            final CommandNode<CommandSourceStack> node,
            final CommandSourceStack source
    ) {
        Supplier<String> supplier = SpongeNodePermissionCache.PERMISSION_MAP.get(node);
        if (supplier == null) {
            supplier = new CachingStringSupplier(() -> SpongeNodePermissionCache.createFromNode(dispatcher, node));
        }
        try {
            ((CommandSourceStackBridge) source).bridge$setPotentialPermissionNode(supplier);
            final boolean result = node.canUse(source);
            if (isRoot && node instanceof SpongePermissionWrappedLiteralCommandNode
                    && ((CommandSourceStackBridge) source).bridge$getCommandSource() instanceof ServerPlayer) {
                // If the entity is a player, then we should try to add it anyway.
                SpongePermissions.registerPermission(supplier.get(), 0);
            }
            return result;
        } finally {
            ((CommandSourceStackBridge) source).bridge$setPotentialPermissionNode(null);
        }
    }

    public static String createFromNode(
            final SpongeCommandDispatcher dispatcher,
            final CommandNode<CommandSourceStack> node) {
        final String permission;
        if (node.getRedirect() != null && !(node.getRedirect() instanceof RootCommandNode) && node.getCommand() == null) {
            final Supplier<String> permSupplier = SpongeNodePermissionCache.PERMISSION_MAP.get(node);
            if (permSupplier == null) {
                permission = SpongeNodePermissionCache.createFromNode(dispatcher, node.getRedirect());
            } else {
                permission = permSupplier.get();
            }
        } else {
            // get the root node.
            final Collection<String> path = dispatcher.getPath(node);
            final String pluginId;
            final String permString;
            if (path.isEmpty()) {
                pluginId = "unknown";
                permString = node.getName();
                // Add a warning here, it's likely due to a dangling redirect
                SpongeCommon.logger()
                        .warn("No path to command node with name {} could be found when generating its permission node. "
                                + "Unable to determine owning plugin - using \"unknown\" as plugin ID", permString);
            } else {
                final String original = path.iterator().next();
                pluginId = dispatcher.getCommandManager()
                        .commandMapping(original)
                        .map(x -> x.plugin().metadata().id()).orElseGet(() -> {
                            SpongeCommon.logger().error("Root command /{} does not have an associated plugin!", original);
                            return "unknown";
                        });
                permString = path.stream().map(x -> {
                    final String replaced = SpongeNodePermissionCache.ILLEGAL_CHARS.matcher(x).replaceAll("").toLowerCase(Locale.ROOT);
                    if (replaced.startsWith(pluginId)) {
                        return replaced.replaceFirst(pluginId, "");
                    }
                    if (replaced.isEmpty()) {
                        return "node";
                    }
                    return replaced;
                }).collect(Collectors.joining("."));
            }
            // We need to calculate this. getPath does not follow redirects thankfully.
            permission = pluginId + ".command." + permString + ".root";
        }
        SpongeNodePermissionCache.PERMISSION_MAP.put(node, () -> permission);
        return permission;
    }

    private static final class CachingStringSupplier implements Supplier<String> {

        private @Nullable String cached = null;
        private final Supplier<String> stringSupplier;

        private CachingStringSupplier(final Supplier<String> stringSupplier) {
            this.stringSupplier = stringSupplier;
        }

        @Override
        public String get() {
            if (this.cached == null) {
                this.cached = this.stringSupplier.get();
            }
            return this.cached;
        }
    }

}
