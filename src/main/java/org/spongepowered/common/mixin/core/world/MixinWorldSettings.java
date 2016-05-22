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
package org.spongepowered.common.mixin.core.world;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.registry.type.world.GeneratorModifierRegistryModule;

import java.util.Collection;

@NonnullByDefault
@Mixin(WorldSettings.class)
@Implements(@Interface(iface = WorldCreationSettings.class, prefix = "settings$"))
public abstract class MixinWorldSettings implements WorldCreationSettings, IMixinWorldSettings {

    private DimensionType dimensionType;
    private PortalAgentType portalAgentType = PortalAgentTypes.DEFAULT;
    private DataContainer generatorSettings;
    private boolean worldEnabled = true;
    private boolean loadOnStartup = true;
    private boolean keepSpawnLoaded = true;
    private boolean generateSpawnOnLoad = true;
    private boolean pvpEnabled = true;
    private ImmutableCollection<WorldGeneratorModifier> generatorModifiers;
    // MCP's worldName is actually the generatorOptions
    private String actualWorldName;
    // internal use only
    private int dimId;
    private boolean isMod;
    private boolean fromBuilder;

    @Shadow @Final private long seed;
    @Shadow @Final private WorldSettings.GameType theGameType;
    @Shadow @Final private boolean mapFeaturesEnabled;
    @Shadow @Final private boolean hardcoreEnabled;
    @Shadow @Final private WorldType terrainType;
    @Shadow private boolean commandsAllowed;
    @Shadow private boolean bonusChestEnabled;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(long seedIn, WorldSettings.GameType gameType, boolean enableMapFeatures, boolean hardcoreMode, WorldType worldTypeIn,
            CallbackInfo ci) {
        this.actualWorldName = "";
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(WorldInfo info, CallbackInfo ci) {
        this.actualWorldName = info.getWorldName();
    }

    @Intrinsic
    public String settings$getWorldName() {
        return this.actualWorldName;
    }

    @Override
    public void setActualWorldName(String name) {
        this.actualWorldName = name;
    }

    @Override
    public String getActualWorldName() {
        return this.actualWorldName;
    }

    @Override
    public void fromBuilder(boolean builder) {
        this.fromBuilder = builder;
    }

    @Override
    public boolean isFromBuilder() {
        return this.fromBuilder;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public GameMode getGameMode() {
        return (GameMode) (Object) this.theGameType;
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) this.terrainType;
    }

    @Override
    public boolean usesMapFeatures() {
        return this.mapFeaturesEnabled;
    }

    @Override
    public boolean isHardcore() {
        return this.hardcoreEnabled;
    }

    @Override
    public boolean commandsAllowed() {
        return this.commandsAllowed;
    }

    @Override
    public boolean bonusChestEnabled() {
        return this.bonusChestEnabled;
    }

    @Override
    public DimensionType getDimensionType() {
        return this.dimensionType;
    }

    @Override
    public void setDimensionType(DimensionType type) {
        this.dimensionType = type;
    }

    @Override
    public PortalAgentType getPortalAgentType() {
        return this.portalAgentType;
    }

    @Override
    public void setPortalAgentType(PortalAgentType type) {
        this.portalAgentType = type;
    }

    @Override
    public DataContainer getGeneratorSettings() {
        return this.generatorSettings;
    }

    @Override
    public void setGeneratorSettings(DataContainer settings) {
        this.generatorSettings = settings;
    }

    @Override
    public boolean isEnabled() {
        return this.worldEnabled;
    }

    @Override
    public void setEnabled(boolean isWorldEnabled) {
        this.worldEnabled = isWorldEnabled;
    }

    @Override
    public boolean loadOnStartup() {
        return this.loadOnStartup;
    }

    @Override
    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public boolean doesGenerateSpawnOnLoad() {
        return this.generateSpawnOnLoad;
    }

    @Override
    public void setKeepSpawnLoaded(boolean keepSpawnLoaded) {
        this.keepSpawnLoaded = keepSpawnLoaded;
    }

    @Override
    public void setGenerateSpawnOnLoad(boolean generateSpawnOnLoad) {
        this.generateSpawnOnLoad = generateSpawnOnLoad;
    }

    @Override
    public boolean isPVPEnabled() {
        return this.pvpEnabled;
    }

    @Override
    public void setPVPEnabled(boolean enabled) {
        this.pvpEnabled = enabled;
    }

    @Override
    public void setGeneratorModifiers(Collection<WorldGeneratorModifier> modifiers) {
        ImmutableList<WorldGeneratorModifier> defensiveCopy = ImmutableList.copyOf(modifiers);
        GeneratorModifierRegistryModule.getInstance().checkAllRegistered(defensiveCopy);
        this.generatorModifiers = defensiveCopy;
    }

    @Override
    public Collection<WorldGeneratorModifier> getGeneratorModifiers() {
        if (this.generatorModifiers == null) {
            return ImmutableList.of();
        }
        return this.generatorModifiers;
    }

    // Internal use only
    @Override
    public void setDimensionId(int id) {
        this.dimId = id;
    }

    @Override
    public Integer getDimensionId() {
        return this.dimId;
    }

    @Override
    public void setIsMod(boolean flag) {
        this.isMod = flag;
    }

    @Override
    public boolean getIsMod() {
        return this.isMod;
    }
}
