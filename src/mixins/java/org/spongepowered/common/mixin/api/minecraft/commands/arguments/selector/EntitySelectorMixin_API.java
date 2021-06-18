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
package org.spongepowered.common.mixin.api.minecraft.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.selector.Selector;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin_API implements Selector {

    // @formatter:off
    @Shadow public abstract List<? extends net.minecraft.world.entity.Entity> shadow$findEntities(CommandSourceStack source) throws CommandSyntaxException;
    @Shadow @Final private int maxResults;
    @Shadow @Final private boolean includesEntities;
    // @formatter:on

    @Override
    @NonNull
    public Collection<Entity> select(@NonNull final ServerLocation location) throws IllegalStateException {
        return this.api$select(((MinecraftServer) Sponge.server()).createCommandSourceStack()
                .withLevel((net.minecraft.server.level.ServerLevel) location.world())
                .withPosition(VecHelper.toVanillaVector3d(location.position())));
    }

    @Override
    @NonNull
    public Collection<Entity> select(@NonNull final Entity entity) throws IllegalStateException {
        return this.api$select(((net.minecraft.world.entity.Entity) entity).createCommandSourceStack());
    }

    @Override
    @NonNull
    public Collection<Entity> select(@NonNull final CommandCause cause) {
        return this.api$select((CommandSourceStack) cause);
    }

    @Override
    public int limit() {
        return this.maxResults;
    }

    @Override
    public boolean playersOnly() {
        return !this.includesEntities;
    }

    private Collection<Entity> api$select(@NonNull final CommandSourceStack source) {
        try {
            // ensures we have the correct type and avoids compiler errors if we just try to cast directly.
            return this.shadow$findEntities(source).stream().map(x -> (Entity) x).collect(Collectors.toList());
        } catch (final CommandSyntaxException commandSyntaxException) {
            throw new IllegalArgumentException(commandSyntaxException.getMessage(), commandSyntaxException);
        }
    }

}
