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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.data.holder.SpongeDataHolder;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.math.vector.Vector3i;

@Mixin(LevelStem.class)
public abstract class LevelStemMixin implements LevelStemBridge, ResourceKeyBridge, SpongeDataHolder {

    // @formatter:off

    @Shadow @Final @org.spongepowered.asm.mixin.Mutable public static Codec<LevelStem> CODEC;

    // @formatter:on

    private ResourceKey impl$key;
    private ResourceLocation impl$gameMode;
    @Nullable private ResourceLocation impl$difficulty;
    private SerializationBehavior impl$serializationBehavior = null;
    @Nullable private Component impl$displayName = null;
    private Integer impl$viewDistance = null;
    @Nullable private Vector3i impl$spawnPosition;
    @Nullable private Boolean impl$hardcore, impl$pvp, impl$commands;

    private boolean impl$loadOnStartup = true;
    private boolean impl$performsSpawnLogic = false;

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
    public Component bridge$displayName() {
        return this.impl$displayName;
    }

    @Override
    public ResourceLocation bridge$gameMode() {
        return this.impl$gameMode;
    }

    @Override
    public ResourceLocation bridge$difficulty() {
        return this.impl$difficulty;
    }

    @Override
    public SerializationBehavior bridge$serializationBehavior() {
        return this.impl$serializationBehavior;
    }

    @Override
    public Integer bridge$viewDistance() {
        return this.impl$viewDistance;
    }

    @Override
    public @Nullable Vector3i bridge$spawnPosition() {
        return this.impl$spawnPosition;
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
    public @Nullable Boolean bridge$hardcore() {
        return this.impl$hardcore;
    }

    @Override
    public @Nullable Boolean bridge$commands() {
        return this.impl$commands;
    }

    @Override
    public @Nullable Boolean bridge$pvp() {
        return this.impl$pvp;
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
    public LevelStem bridge$decorateData(final DataManipulator data) {
        // TODO decorate
        // TODO worldgensettings copy?

        return (LevelStem) (Object) this;
    }

    @Override
    public SpongeWorldTemplate.SpongeDataSection bridge$createData() {
        return new SpongeWorldTemplate.SpongeDataSection(this.bridge$displayName(),
            this.bridge$gameMode(), this.bridge$difficulty(),
            this.bridge$serializationBehavior(),
            this.bridge$viewDistance(), this.bridge$spawnPosition(),
            this.bridge$loadOnStartup(), this.bridge$performsSpawnLogic(),
            this.bridge$hardcore(), this.bridge$commands(),
            this.bridge$pvp());
    }
}
