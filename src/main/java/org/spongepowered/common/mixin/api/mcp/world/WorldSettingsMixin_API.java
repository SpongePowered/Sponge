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
import net.minecraft.world.WorldType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;

import java.util.Collection;

@NonnullByDefault
@Mixin(WorldSettings.class)
@Implements(value = @Interface(iface = WorldArchetype.class, prefix = "archetype$"))
public abstract class WorldSettingsMixin_API implements WorldArchetype {

    @Shadow private boolean commandsAllowed;
    @Shadow private String generatorOptions;

    @Shadow public abstract long shadow$getSeed();
    @Shadow public abstract GameType getGameType();
    @Shadow public abstract boolean getHardcoreEnabled();
    @Shadow public abstract boolean isMapFeaturesEnabled();
    @Shadow public abstract WorldType getTerrainType();
    @Shadow public abstract boolean shadow$areCommandsAllowed();

    @Intrinsic
    public long archetype$getSeed() {
        return this.shadow$getSeed();
    }

    @Override
    public boolean isSeedRandomized() {
        return ((WorldSettingsBridge) this).bridge$isSeedRandomized();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.getGameType();
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.getTerrainType();
    }

    @Override
    public boolean usesMapFeatures() {
        return this.isMapFeaturesEnabled();
    }

    @Override
    public boolean isHardcore() {
        return this.getHardcoreEnabled();
    }

    @Intrinsic
    public boolean archetype$areCommandsAllowed() {
        return this.shadow$areCommandsAllowed();
    }

    @Override
    public boolean doesGenerateBonusChest() {
        return ((WorldSettingsBridge) this).bridge$getGeneratesBonusChest();
    }

    @Override
    public DimensionType getDimensionType() {
        return ((WorldSettingsBridge) this).bridge$getDimensionType();
    }

    @Override
    public Difficulty getDifficulty() {
        return ((WorldSettingsBridge) this).bridge$getDifficulty();
    }

    @Override
    public PortalAgentType getPortalAgentType() {
        return ((WorldSettingsBridge) this).bridge$getPortalAgentType();
    }

    @Override
    public DataContainer getGeneratorSettings() {
        return ((WorldSettingsBridge) this).bridge$getGeneratorSettings();
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
    public boolean loadOnStartup() {
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
    public boolean isPVPEnabled() {
        return ((WorldSettingsBridge) this).bridge$isPVPEnabled();
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        return ((WorldSettingsBridge) this).bridge$getGeneratorModifiers();
    }

    @Override
    public String getId() {
        return ((WorldSettingsBridge) this).bridge$getId();
    }

    @Override
    public String getName() {
        return ((WorldSettingsBridge) this).bridge$getName();
    }

}
