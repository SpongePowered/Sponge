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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import net.minecraft.world.Difficulty;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.gen.GeneratorModifierType;
import org.spongepowered.api.world.storage.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(DerivedWorldInfo.class)
@Implements(@Interface(iface = ServerWorldProperties.class, prefix = "serverWorldProperties$"))
public abstract class DerivedWorldInfoMixin_API implements IServerWorldInfoMixin_API {

    // @formatter:off
    @Shadow @Final private IServerConfiguration worldData;
    @Shadow @Final private IServerWorldInfo wrapped;
    // @formatter:on

    @Nullable private Long impl$seed;
    @Nullable private Boolean impl$generateFeatures;
    @Nullable private Boolean impl$isHardcore;
    @Nullable private Boolean impl$isAllowCommands;
    @Nullable private Difficulty impl$difficulty;

    @Override
    public GeneratorModifierType getGeneratorModifierType() {
        return (GeneratorModifierType) this.shadow$getGenerator();
    }

    @Override
    public void setGeneratorModifierType(final GeneratorModifierType modifier) {
        Objects.requireNonNull(modifier);
        this.shadow$setGenerator((WorldType) modifier);
    }

    @Override
    public long getSeed() {
        if (this.impl$seed != null) {
            return this.impl$seed;
        }
        return this.worldData.worldGenSettings().seed();
    }

    @Override
    public void setSeed(final long seed) {
        this.impl$seed = seed;
    }

    @Override
    public boolean areFeaturesEnabled() {
        if (this.impl$generateFeatures != null) {
            return this.impl$generateFeatures;
        }
        return this.worldData.worldGenSettings().generateFeatures();
    }

    @Override
    public void setFeaturesEnabled(final boolean state) {
        this.impl$generateFeatures = state;
    }

    @Intrinsic
    public boolean serverWorldProperties$isHardcore() {
        if (this.impl$isHardcore != null) {
            return this.impl$isHardcore;
        }
        return this.shadow$isHardcore();
    }

    @Override
    public void setHardcore(final boolean state) {
        this.impl$isHardcore = state;
    }

    @Override
    public boolean areCommandsEnabled() {
        if (this.impl$isAllowCommands != null) {
            return this.impl$isAllowCommands;
        }
        return this.shadow$getAllowCommands();
    }

    @Override
    public void setCommandsEnabled(final boolean state) {
        this.impl$isAllowCommands = state;
    }

    @Override
    public org.spongepowered.api.world.difficulty.Difficulty getDifficulty() {
        if (this.impl$difficulty != null) {
            return ((org.spongepowered.api.world.difficulty.Difficulty) (Object) this.impl$difficulty);
        }
        return (org.spongepowered.api.world.difficulty.Difficulty) (Object) this.shadow$getDifficulty();
    }

    @Override
    public void setDifficulty(final org.spongepowered.api.world.difficulty.Difficulty difficulty) {
        this.impl$difficulty = ((Difficulty) (Object) difficulty);
    }

    @Override
    public Optional<UUID> getWanderTraderUniqueId() {
        return ((ServerWorldProperties) this.wrapped).getWanderTraderUniqueId();
    }

}
