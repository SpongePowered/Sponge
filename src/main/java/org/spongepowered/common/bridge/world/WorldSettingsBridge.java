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
package org.spongepowered.common.bridge.world;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.util.Collection;

import javax.annotation.Nullable;

public interface WorldSettingsBridge {

    String bridge$getId();

    String bridge$getName();

    DimensionType bridge$getDimensionType();

    Difficulty bridge$getDifficulty();

    boolean bridge$getGeneratesBonusChest();

    boolean bridge$isSeedRandomized();

    PortalAgentType bridge$getPortalAgentType();

    DataContainer bridge$getGeneratorSettings();

    SerializationBehavior bridge$getSerializationBehavior();

    boolean bridge$doesKeepSpawnLoaded();

    boolean bridge$isEnabled();

    boolean bridge$loadOnStartup();

    boolean bridge$generateSpawnOnLoad();

    boolean bridge$isPVPEnabled();

    Collection<WorldGeneratorModifier> bridge$getGeneratorModifiers();

    void bridge$setId(String id);

    void bridge$setName(String name);

    void bridge$setDimensionType(DimensionType dimensionType);

    void bridge$setDifficulty(Difficulty difficulty);

    void bridge$setSerializationBehavior(SerializationBehavior behavior);

    void bridge$setGeneratorSettings(DataContainer generatorSettings);

    void bridge$setGeneratorModifiers(ImmutableList<WorldGeneratorModifier> generatorModifiers);

    void bridge$setEnabled(boolean state);

    void bridge$setLoadOnStartup(boolean state);

    void bridge$setKeepSpawnLoaded(@Nullable Boolean state);

    void bridge$setGenerateSpawnOnLoad(boolean state);

    void bridge$setPVPEnabled(boolean state);

    void bridge$setCommandsAllowed(boolean state);

    void bridge$setGenerateBonusChest(boolean state);

    void bridge$setPortalAgentType(PortalAgentType type);

    void bridge$setRandomSeed(boolean state);

    @Nullable
    Boolean bridge$internalKeepSpawnLoaded();
}
