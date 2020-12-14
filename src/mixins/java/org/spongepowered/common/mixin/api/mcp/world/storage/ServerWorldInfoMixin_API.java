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

import com.google.gson.JsonParseException;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.world.gen.GeneratorModifierType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
@Mixin(ServerWorldInfo.class)
public abstract class ServerWorldInfoMixin_API implements IServerWorldInfoMixin_API {

    // @formatter:off

    @Shadow private UUID wanderingTraderId;
    @Shadow private WorldSettings settings;
    @Shadow @Final private DimensionGeneratorSettings worldGenSettings;

    @Shadow public abstract void shadow$setDifficulty(Difficulty difficulty);

    // @formatter:on

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
        return this.worldGenSettings.seed();
    }

    @Override
    public void setSeed(final long seed) {
        // TODO how?
    }

    @Override
    public boolean areFeaturesEnabled() {
        return this.worldGenSettings.generateFeatures();
    }

    @Override
    public void setFeaturesEnabled(final boolean state) {
        this.settings.setGenerateFeatures(state);
        // TODO how?
    }

    @Override
    public void setHardcore(final boolean state) {
        this.settings.setHardcore(state);
        // TODO how?
    }

    @Override
    public void setCommandsEnabled(final boolean state) {
        this.settings.setAllowCommands(state);
        // TODO how?
    }

    @Override
    public void setDifficulty(final org.spongepowered.api.world.difficulty.Difficulty difficulty) {
        this.shadow$setDifficulty((Difficulty) (Object) difficulty);
    }

    @Override
    public DataContainer getGeneratorSettings() {
        // TODO how?
        // TODO Minecraft 1.14 - This may not be correct...
        if (this.legacyCustomOptions != null) {
            try {
                return DataContainer.createNew().set(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS, DataFormats.JSON.get().read(this.legacyCustomOptions));
            } catch (final JsonParseException | IOException ignored) {
                return DataContainer.createNew();
            }
        } else {
            return DataContainer.createNew().set(Constants.Sponge.World.WORLD_CUSTOM_SETTINGS,
                NBTTranslator.getInstance().translateFrom(this.shadow$getGeneratorOptions()));
        }
    }

    @Override
    public void setGeneratorSettings(final DataContainer generatorSettings) {
        // TODO how?
        this.shadow$setGeneratorOptions(NBTTranslator.getInstance().translate(generatorSettings));
    }

    @Override
    public Optional<UUID> getWanderTraderUniqueId() {
        return Optional.ofNullable(this.wanderingTraderId);
    }

}
