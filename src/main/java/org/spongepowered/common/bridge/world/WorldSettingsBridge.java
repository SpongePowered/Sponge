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

import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.gen.WorldGenerationSettings;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

public interface WorldSettingsBridge {

    DimensionType bridge$getDimensionType();

    WorldGenerationSettings bridge$getWorldGenerationSettings();

    Difficulty bridge$getDifficulty();

    SerializationBehavior bridge$getSerializationBehavior();

    boolean bridge$doesKeepSpawnLoaded();

    boolean bridge$isEnabled();

    boolean bridge$loadOnStartup();

    boolean bridge$generateSpawnOnLoad();

    boolean bridge$isPVPEnabled();

    void bridge$setDimensionType(DimensionType dimensionType);

    void bridge$setWorldGenerationSettings(WorldGenerationSettings worldGenerationSettings);

    void bridge$setDifficulty(Difficulty difficulty);

    void bridge$setSerializationBehavior(SerializationBehavior behavior);

    void bridge$setEnabled(boolean state);

    void bridge$setLoadOnStartup(boolean state);

    void bridge$setKeepSpawnLoaded(@Nullable Boolean state);

    void bridge$setGenerateSpawnOnLoad(boolean state);

    void bridge$setPVPEnabled(boolean state);

    void bridge$setCommandsEnabled(boolean state);

    void bridge$populateInfo(IServerWorldInfoBridge infoBridge);

    void bridge$setInfoConfigAdapter(InheritableConfigHandle<WorldConfig> configAdapter);

    void bridge$setConfigExists(boolean worldConfigExists);
}
