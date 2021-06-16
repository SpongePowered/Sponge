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
package org.spongepowered.common.mixin.core.world.level.dimension;

import com.mojang.serialization.Codec;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

@Mixin(LevelStem.class)
public abstract class LevelStemMixin implements LevelStemBridge, ResourceKeyBridge {

    // @formatter:off

    @Shadow @Final @Mutable public static Codec<LevelStem> CODEC;

    // @formatter:on

    private ResourceKey impl$key;
    private ResourceLocation impl$gameMode;
    @Nullable private ResourceLocation impl$difficulty;
    private SerializationBehavior impl$serializationBehavior = null;
    @Nullable private Component impl$displayName = null;
    private Integer impl$viewDistance = null;
    @Nullable private Vector3i impl$spawnPosition;
    @Nullable private Boolean impl$hardcore, impl$pvp, impl$commands;

    private boolean impl$loadOnStartup = true, impl$performsSpawnLogic = false, impl$fromSettings = true;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void impl$useTemplateCodec(final CallbackInfo ci) {
        LevelStemMixin.CODEC = SpongeWorldTemplate.DIRECT_CODEC;
    }

    @Override
    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    @Override
    public Optional<Component> bridge$displayName() {
        return Optional.ofNullable(this.impl$displayName);
    }

    @Override
    public Optional<ResourceLocation> bridge$gameMode() {
        return Optional.ofNullable(this.impl$gameMode);
    }

    @Override
    public Optional<ResourceLocation> bridge$difficulty() {
        return Optional.ofNullable(this.impl$difficulty);
    }

    @Override
    public Optional<SerializationBehavior> bridge$serializationBehavior() {
        return Optional.ofNullable(this.impl$serializationBehavior);
    }

    @Override
    public Optional<Integer> bridge$viewDistance() {
        return Optional.ofNullable(this.impl$viewDistance);
    }

    @Override
    public Optional<Vector3i> bridge$spawnPosition() {
        return Optional.ofNullable(this.impl$spawnPosition);
    }

    @Override
    public boolean bridge$loadOnStartup() {
        return this.impl$loadOnStartup;
    }

    @Override
    public boolean bridge$performsSpawnLogic() {
        return this.impl$performsSpawnLogic;
    }

    @Override
    public Optional<Boolean> bridge$hardcore() {
        return Optional.ofNullable(this.impl$hardcore);
    }

    @Override
    public Optional<Boolean> bridge$commands() {
        return Optional.ofNullable(this.impl$commands);
    }

    @Override
    public Optional<Boolean> bridge$pvp() {
        return Optional.ofNullable(this.impl$pvp);
    }

    @Override
    public boolean bridge$fromSettings() {
        return this.impl$fromSettings;
    }

    @Override
    public void bridge$setFromSettings(final boolean fromSettings) {
        this.impl$fromSettings = fromSettings;
    }

    @Override
    public LevelStem bridge$decorateData(final SpongeWorldTemplate.SpongeDataSection data) {
        this.impl$gameMode = data.gameMode;
        this.impl$difficulty = data.difficulty;
        this.impl$serializationBehavior = data.serializationBehavior;
        this.impl$displayName = data.displayName;
        this.impl$viewDistance = data.viewDistance;
        this.impl$spawnPosition = data.spawnPosition;
        this.impl$loadOnStartup = data.loadOnStartup == null || data.loadOnStartup;
        this.impl$performsSpawnLogic = data.performsSpawnLogic != null && data.performsSpawnLogic;
        this.impl$hardcore = data.hardcore;
        this.impl$commands = data.commands;
        this.impl$pvp = data.pvp;

        return (LevelStem) (Object) this;
    }

    @Override
    public SpongeWorldTemplate.SpongeDataSection bridge$createData() {
        return new SpongeWorldTemplate.SpongeDataSection(this.bridge$displayName().orElse(null),
            this.bridge$gameMode().orElse(null), this.bridge$difficulty().orElse(null),
            this.bridge$serializationBehavior().orElse(null),
            this.bridge$viewDistance().orElse(null), this.bridge$spawnPosition().orElse(null),
            this.bridge$loadOnStartup(), this.bridge$performsSpawnLogic(),
            this.bridge$hardcore().orElse(null), this.bridge$commands().orElse(null),
            this.bridge$pvp().orElse(null));
    }

    @Override
    public void bridge$populateFromTemplate(final SpongeWorldTemplate s) {
        this.impl$key = s.key();
        this.impl$gameMode = s.gameMode == null ? null : (ResourceLocation) (Object) s.gameMode.location();
        this.impl$difficulty = s.difficulty == null ? null : (ResourceLocation) (Object) s.difficulty.location();
        this.impl$serializationBehavior = s.serializationBehavior;
        this.impl$displayName = s.displayName;
        this.impl$viewDistance = s.viewDistance;
        this.impl$spawnPosition = s.spawnPosition;
        this.impl$loadOnStartup = s.loadOnStartup;
        this.impl$performsSpawnLogic = s.performsSpawnLogic;
        this.impl$hardcore = s.hardcore;
        this.impl$commands = s.commands;
        this.impl$pvp = s.pvp;
    }

    @Override
    public void bridge$populateFromLevelData(final PrimaryLevelData levelData) {
        final PrimaryLevelDataBridge levelDataBridge = (PrimaryLevelDataBridge) levelData;
        this.impl$gameMode = (ResourceLocation) (Object) RegistryTypes.GAME_MODE.get().valueKey((GameMode) (Object) levelData.getGameType());
        this.impl$difficulty = (ResourceLocation) (Object) RegistryTypes.DIFFICULTY.get().valueKey((Difficulty) (Object) levelData.getDifficulty());
        this.impl$serializationBehavior = levelDataBridge.bridge$serializationBehavior().orElse(null);
        this.impl$displayName = levelDataBridge.bridge$displayName().orElse(null);
        this.impl$viewDistance = levelDataBridge.bridge$viewDistance().orElse(null);
        this.impl$spawnPosition = new Vector3i(levelData.getXSpawn(), levelData.getYSpawn(), levelData.getZSpawn());
        this.impl$loadOnStartup = levelDataBridge.bridge$loadOnStartup();
        this.impl$performsSpawnLogic = levelDataBridge.bridge$performsSpawnLogic();
        this.impl$hardcore = levelData.isHardcore();
        this.impl$commands = levelData.getAllowCommands();
        this.impl$pvp = levelDataBridge.bridge$pvp().orElse(null);
    }

    @Override
    public SpongeWorldTemplate bridge$asTemplate() {
        return new SpongeWorldTemplate((LevelStem) (Object) this);
    }
}
