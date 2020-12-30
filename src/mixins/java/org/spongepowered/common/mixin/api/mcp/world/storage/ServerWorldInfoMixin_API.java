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
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.api.world.generation.MutableWorldGenerationSettings;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.WorldSettingsAccessor;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(ServerWorldInfo.class)
@Implements(@Interface(iface = ServerWorldProperties.class, prefix = "serverWorldProperties$"))
public abstract class ServerWorldInfoMixin_API {

    // @formatter:off
    @Shadow private boolean initialized;
    @Shadow private UUID wanderingTraderId;
    @Shadow private WorldSettings settings;
    @Shadow @Final private DimensionGeneratorSettings worldGenSettings;

    @Shadow public abstract void shadow$setDifficulty(Difficulty difficulty);
    // @formatter:on

    @Intrinsic
    public boolean serverWorldProperties$isInitialized() {
        return this.initialized;
    }

    public MutableWorldGenerationSettings serverWorldProperties$getWorldGenerationSettings() {
        return (MutableWorldGenerationSettings) this.worldGenSettings;
    }

    public void serverWorldProperties$setHardcore(final boolean state) {
        ((WorldSettingsAccessor) (Object) this.settings).accessor$hardcode(state);
    }

    public void serverWorldProperties$setCommandsEnabled(final boolean state) {
        ((WorldSettingsAccessor) (Object) this.settings).accessor$allowCommands(state);
    }

    public void serverWorldProperties$setDifficulty(final org.spongepowered.api.world.difficulty.Difficulty difficulty) {
        this.shadow$setDifficulty((Difficulty) (Object) difficulty);
    }

    public Optional<UUID> serverWorldProperties$getWanderTraderUniqueId() {
        return Optional.ofNullable(this.wanderingTraderId);
    }

}
