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

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Dimension;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.DimensionBridge;
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.common.world.server.SpongeWorldTemplate;

import java.util.function.Function;

@Mixin(Dimension.class)
public abstract class DimensionMixin implements DimensionBridge {

    private ResourceLocation impl$gameMode = (ResourceLocation) (Object) BootstrapProperties.gameMode.location();
    private ResourceLocation impl$difficulty = (ResourceLocation) (Object) BootstrapProperties.difficulty.location();
    private SerializationBehavior impl$serializationBehavior = SerializationBehavior.AUTOMATIC;
    @Nullable private Component impl$displayName = null;
    private Integer impl$viewDistance = BootstrapProperties.viewDistance;

    private boolean impl$enabled = true, impl$loadOnStartup = true, impl$keepSpawnLoaded = true, impl$generateSpawnOnLoad = false,
            impl$hardcore = BootstrapProperties.hardcore, impl$commands = true, impl$pvp = BootstrapProperties.pvp;

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec"))
    private static <T> Codec<Dimension> impl$useTemplateCodec(final Function<RecordCodecBuilder.Instance<Dimension>, ?
                extends App<RecordCodecBuilder.Mu<Dimension>, Dimension>> func) {
        return SpongeWorldTemplate.DIRECT_CODEC;
    }

    @Override
    public @Nullable Component bridge$displayName() {
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
    public @Nullable Integer bridge$viewDistance() {
        return this.impl$viewDistance;
    }

    @Override
    public boolean bridge$enabled() {
        return this.impl$enabled;
    }

    @Override
    public boolean bridge$loadOnStartup() {
        return this.impl$loadOnStartup;
    }

    @Override
    public boolean bridge$keepSpawnLoaded() {
        return this.impl$keepSpawnLoaded;
    }

    @Override
    public boolean bridge$generateSpawnOnLoad() {
        return this.impl$generateSpawnOnLoad;
    }

    @Override
    public boolean bridge$hardcore() {
        return this.impl$hardcore;
    }

    @Override
    public boolean bridge$commands() {
        return this.impl$commands;
    }

    @Override
    public boolean bridge$pvp() {
        return this.impl$pvp;
    }

    @Override
    public void bridge$setSpongeData(final SpongeWorldTemplate.SpongeDataSection spongeData) {
        this.impl$gameMode = spongeData.gameMode == null ? (ResourceLocation) (Object) BootstrapProperties.gameMode.location() : spongeData.gameMode;
        this.impl$difficulty = spongeData.difficulty == null ? (ResourceLocation) (Object) BootstrapProperties.difficulty.location() : spongeData.difficulty;
        this.impl$serializationBehavior = spongeData.serializationBehavior == null ? SerializationBehavior.AUTOMATIC : spongeData.serializationBehavior;
        this.impl$displayName = spongeData.displayName;
        this.impl$enabled = spongeData.enabled == null || spongeData.enabled;
        this.impl$loadOnStartup = spongeData.enabled == null || spongeData.enabled;
        this.impl$keepSpawnLoaded = spongeData.keepSpawnLoaded != null && spongeData.keepSpawnLoaded;
        this.impl$generateSpawnOnLoad = spongeData.generateSpawnOnLoad != null && spongeData.generateSpawnOnLoad;
        this.impl$hardcore = spongeData.hardcore == null ? BootstrapProperties.hardcore : spongeData.hardcore;
        this.impl$commands = spongeData.commands == null || spongeData.commands;
        this.impl$pvp = spongeData.pvp == null || spongeData.pvp;
        this.impl$viewDistance = spongeData.viewDistance == null ? BootstrapProperties.viewDistance : spongeData.viewDistance;
    }
}
