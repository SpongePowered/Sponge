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
package org.spongepowered.common.mixin.core.world.storage;

import net.kyori.adventure.text.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

import java.util.UUID;

@Mixin(IServerWorldInfo.class)
public interface IServerWorldInfoMixin extends ServerWorldInfoBridge {

    @Override
    default boolean bridge$valid() {
        return false;
    }

    @Override
    default boolean bridge$customDifficulty() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$forceSetDifficulty(final Difficulty difficulty) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default boolean bridge$pvp() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$pvp(final boolean pvp) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default boolean bridge$enabled() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$enabled(final boolean enabled) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default boolean bridge$loadOnStartup() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$loadOnStartup(final boolean loadOnStartup) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default boolean bridge$keepSpawnLoaded() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$keepSpawnLoaded(final boolean keepSpawnLoaded) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default boolean bridge$generateSpawnOnLoad() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$generateSpawnOnLoad(final boolean generateSpawnOnLoad) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default SerializationBehavior bridge$serializationBehavior() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$serializationBehavior(final SerializationBehavior behavior) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default @Nullable Component bridge$displayName() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$displayName(final @Nullable Component displayName) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default InheritableConfigHandle<WorldConfig> bridge$configAdapter() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$configAdapter(final InheritableConfigHandle<WorldConfig> adapter) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default DimensionType bridge$dimensionType() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default void bridge$dimensionType(final DimensionType type, final boolean updatePlayers) {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Override
    default UUID bridge$uniqueId() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }

    @Nullable
    @Override
    default ServerWorld bridge$world() {
        throw new UnsupportedOperationException("Only Vanilla implementation server world properties are supported!");
    }
}
