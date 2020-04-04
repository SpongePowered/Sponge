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
package org.spongepowered.common.bridge.world.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.Difficulty;
import net.minecraft.world.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.teleport.PortalAgentType;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

import java.util.Optional;
import java.util.UUID;

public interface WorldInfoBridge {

    @Nullable
    DimensionType bridge$getDimensionType();

    void bridge$setDimensionType(DimensionType type);

    SpongeDimensionType bridge$getLogicType();

    void bridge$setLogicType(org.spongepowered.api.world.dimension.DimensionType type);

    UUID bridge$getUniqueId();

    void bridge$setUniqueId(UUID uniqueId);

    boolean bridge$isEnabled();

    void bridge$setEnabled(boolean state);

    boolean bridge$isPVPEnabled();

    void bridge$setPVPEnabled(boolean state);

    boolean bridge$doesGenerateBonusChest();

    void bridge$setGenerateBonusChest(boolean state);

    boolean bridge$doesLoadOnStartup();

    void bridge$setLoadOnStartup(boolean state);

    boolean bridge$doesKeepSpawnLoaded();

    void bridge$setKeepSpawnLoaded(boolean state);

    boolean bridge$doesGenerateSpawnOnLoad();

    void bridge$setGenerateSpawnOnLoad(boolean state);

    SerializationBehavior bridge$getSerializationBehavior();

    void bridge$setSerializationBehavior(SerializationBehavior behavior);

    PortalAgentType bridge$getPortalAgent();

    boolean bridge$isModCreated();

    void bridge$setModCreated(boolean state);

    boolean bridge$isValid();

    boolean bridge$hasCustomDifficulty();

    /**
     * Sets the difficulty without marking it as custom
     */
    void bridge$forceSetDifficulty(Difficulty difficulty);

    void bridge$updatePlayersForDifficulty();

    void bridge$writeSpongeLevelData(CompoundNBT compound);

    void bridge$readSpongeLevelData(CompoundNBT compound);

    int bridge$getIndexForUniqueId(UUID uuid);

    Optional<UUID> bridge$getUniqueIdForIndex(int index);

    @Nullable SpongeConfig<WorldConfig> bridge$getConfigAdapter();

    boolean bridge$createWorldConfig();

    void bridge$saveConfig();
}
