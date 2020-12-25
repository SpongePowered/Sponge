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
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGenerationSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;

@Mixin(WorldSettings.class)
public abstract class WorldSettingsMixin_API implements WorldArchetype {

    //@formatter:off
    @Shadow @Final private net.minecraft.world.Difficulty difficulty;

    @Shadow public abstract GameType shadow$gameType();
    @Shadow public abstract boolean shadow$hardcore();
    @Shadow public abstract boolean shadow$allowCommands();
    //@formatter:on

    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.shadow$gameType();
    }

    @Override
    public boolean isHardcore() {
        return this.shadow$hardcore();
    }

    @Override
    public boolean areCommandsEnabled() {
        return this.shadow$allowCommands();
    }

    @Override
    public WorldType getDimensionType() {
        return (WorldType) ((WorldSettingsBridge) this).bridge$getDimensionType();
    }

    @Override
    public Difficulty getDifficulty() {
        return (Difficulty) (Object) this.difficulty;
    }

    @Override
    public SerializationBehavior getSerializationBehavior() {
        return ((WorldSettingsBridge) this).bridge$getSerializationBehavior();
    }

    @Override
    public boolean isEnabled() {
        return ((WorldSettingsBridge) this).bridge$isEnabled();
    }

    @Override
    public boolean doesLoadOnStartup() {
        return ((WorldSettingsBridge) this).bridge$loadOnStartup();
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return ((WorldSettingsBridge) this).bridge$doesKeepSpawnLoaded();
    }

    @Override
    public boolean doesGenerateSpawnOnLoad() {
        return ((WorldSettingsBridge) this).bridge$generateSpawnOnLoad();
    }

    @Override
    public WorldGenerationSettings getWorldGeneratorSettings() {
        return ((WorldSettingsBridge) this).bridge$getWorldGenerationSettings();
    }

    @Override
    public boolean isPVPEnabled() {
        return ((WorldSettingsBridge) this).bridge$isPVPEnabled();
    }
}
