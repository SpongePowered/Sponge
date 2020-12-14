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

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.Difficulty;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldSettingsBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Mixin(DerivedWorldInfo.class)
public abstract class DerivedWorldInfoMixin implements IServerWorldInfoMixin {

    // @formatter:off
    @Shadow @Final private IServerConfiguration worldData;
    @Shadow @Final private IServerWorldInfo wrapped;
    // @formatter:on

    @Nullable private ResourceKey impl$key;
    private UUID impl$uniqueId = UUID.randomUUID();
    private boolean impl$hasCustomDifficulty = false;
    @Nullable private Difficulty impl$customDifficulty;

    private InheritableConfigHandle<WorldConfig> impl$configAdapter = SpongeGameConfigs.createDetached();
    private boolean impl$modCreated;

    // ResourceKeyBridge

    @Override
    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    // WorldInfoBridge

    @Override
    public UUID bridge$getUniqueId() {
        return this.impl$uniqueId;
    }

    @Override
    public void bridge$setUniqueId(final UUID uniqueId) {
        this.impl$uniqueId = uniqueId;
    }

    @Override
    public boolean bridge$hasCustomDifficulty() {
        return this.impl$hasCustomDifficulty;
    }

    @Override
    public void bridge$forceSetDifficulty(final Difficulty difficulty) {
        this.impl$hasCustomDifficulty = true;
        this.impl$customDifficulty = difficulty;
        this.impl$updateWorldForDifficultyChange(this.bridge$getWorld(), this.worldData.isDifficultyLocked());
    }

    @Override
    public boolean bridge$isModCreated() {
        return this.impl$modCreated;
    }

    @Override
    public void bridge$setModCreated(final boolean state) {
        this.impl$modCreated = state;
    }

    @Override
    public InheritableConfigHandle<WorldConfig> bridge$getConfigAdapter() {
        if (this.impl$configAdapter == null) {
            if (this.bridge$isValid()) {
                this.impl$configAdapter = SpongeGameConfigs.createWorld(null, this.bridge$getKey());
            } else {
                this.impl$configAdapter = SpongeGameConfigs.createDetached();
            }
        }
        return this.impl$configAdapter;
    }

    @Override
    public void bridge$setConfigAdapter(final InheritableConfigHandle<WorldConfig> adapter) {
        this.impl$configAdapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public void bridge$writeTrackedPlayerTable(CompoundNBT spongeDataCompound) {
        // saved on ServerWorldInfo
    }

    @Override
    public int bridge$getIndexForUniqueId(final UUID uniqueId) {
        return ((WorldInfoBridge) this.wrapped).bridge$getIndexForUniqueId(uniqueId);
    }

    @Override
    public Optional<UUID> bridge$getUniqueIdForIndex(final int index) {
        return ((WorldInfoBridge) this.wrapped).bridge$getUniqueIdForIndex(index);
    }

    @Redirect(method = "getDifficulty", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/IServerConfiguration;getDifficulty()Lnet/minecraft/world/Difficulty;"))
    public Difficulty impl$onGetDifficulty(IServerConfiguration iServerConfiguration) {
        if (this.impl$hasCustomDifficulty) {
            return this.impl$customDifficulty;
        }
        return iServerConfiguration.getDifficulty();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$fillInfo(final IServerConfiguration serverConfig, final IServerWorldInfo serverInfo, final CallbackInfo ci) {
        ((WorldSettingsBridge) (Object) serverConfig.getLevelSettings()).bridge$populateInfo(this);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DerivedWorldInfo.class.getSimpleName() + "[", "]")
                .add("key=" + this.impl$key)
                .add("uniqueId=" + this.impl$uniqueId)
                .add("modCreated=" + this.impl$modCreated)
                .add("spawnX=" + this.shadow$getXSpawn())
                .add("spawnY=" + this.shadow$getYSpawn())
                .add("spawnZ=" + this.shadow$getZSpawn())
                .add("gameType=" + this.shadow$getGameType())
                .add("hardcore=" + this.shadow$isHardcore())
                .add("difficulty=" + this.shadow$getDifficulty())
                .toString();
    }

}
