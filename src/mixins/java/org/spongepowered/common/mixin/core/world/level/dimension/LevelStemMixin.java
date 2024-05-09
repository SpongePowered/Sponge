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
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.dimension.LevelStem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.data.holder.SpongeDataHolder;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.math.vector.Vector3i;

@Mixin(LevelStem.class)
public abstract class LevelStemMixin implements LevelStemBridge, SpongeDataHolder {

    // @formatter:off
    @Shadow @Final @org.spongepowered.asm.mixin.Mutable public static Codec<LevelStem> CODEC;
    // @formatter:on

    @Nullable private GameType impl$gameMode;
    @Nullable private Difficulty impl$difficulty;
    @Nullable private SerializationBehavior impl$serializationBehavior;
    @Nullable private Component impl$displayName;
    @Nullable private Integer impl$viewDistance;
    @Nullable private Vector3i impl$spawnPosition;
    @Nullable private Boolean impl$hardcore, impl$pvp, impl$allowCommands;
    @Nullable private Long impl$seed;
    private boolean impl$loadOnStartup = true;
    private boolean impl$performsSpawnLogic = false;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void impl$useTemplateCodec(final CallbackInfo ci) {
        LevelStemMixin.CODEC = SpongeWorldTemplate.DIRECT_CODEC;
    }

    @Override
    public Component bridge$displayName() {
        return this.impl$displayName;
    }

    @Override
    public GameType bridge$gameMode() {
        return this.impl$gameMode;
    }

    @Override
    public Difficulty bridge$difficulty() {
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
    public @Nullable Boolean bridge$allowCommands() {
        return this.impl$allowCommands;
    }

    @Override
    public @Nullable Boolean bridge$pvp() {
        return this.impl$pvp;
    }

    @Override
    public @Nullable Long bridge$seed() {
        return this.impl$seed;
    }

    @Override
    public LevelStem bridge$decorateData(final SpongeWorldTemplate.SpongeDataSection data) {
        this.impl$displayName = data.displayName();
        this.impl$gameMode = data.gameMode();
        this.impl$difficulty = data.difficulty();
        this.impl$serializationBehavior = data.serializationBehavior();
        this.impl$viewDistance = data.viewDistance();
        this.impl$spawnPosition = data.spawnPosition();
        this.impl$loadOnStartup = data.loadOnStartup() == null || data.loadOnStartup();
        this.impl$performsSpawnLogic = data.performsSpawnLogic() != null && data.performsSpawnLogic();
        this.impl$hardcore = data.hardcore();
        this.impl$allowCommands = data.commands();
        this.impl$pvp = data.pvp();
        this.impl$seed = data.seed();
        return (LevelStem) (Object) this;
    }

    @Override
    public LevelStem bridge$decorateData(final DataManipulator data) {
        this.impl$displayName = data.getOrNull(Keys.DISPLAY_NAME);
        this.impl$gameMode = (GameType) (Object) data.get(Keys.GAME_MODE).orElse(null);
        this.impl$difficulty = (Difficulty) (Object) data.get(Keys.WORLD_DIFFICULTY).orElse(null);
        this.impl$serializationBehavior = data.getOrElse(Keys.SERIALIZATION_BEHAVIOR, SerializationBehavior.AUTOMATIC);
        this.impl$viewDistance = data.getOrNull(Keys.VIEW_DISTANCE);
        this.impl$spawnPosition = data.getOrNull(Keys.SPAWN_POSITION);
        this.impl$loadOnStartup = data.getOrElse(Keys.IS_LOAD_ON_STARTUP, true);
        this.impl$performsSpawnLogic = data.getOrElse(Keys.PERFORM_SPAWN_LOGIC, false);
        this.impl$hardcore = data.getOrNull(Keys.HARDCORE);
        this.impl$allowCommands = data.getOrNull(Keys.COMMANDS);
        this.impl$pvp = data.getOrNull(Keys.PVP);
        this.impl$seed = data.getOrNull(Keys.SEED);
        return (LevelStem) (Object) this;
    }

    @Override
    public SpongeWorldTemplate.SpongeDataSection bridge$createData() {
        return new SpongeWorldTemplate.SpongeDataSection(
            this.impl$displayName,
            this.impl$gameMode,
            this.impl$difficulty,
            this.impl$serializationBehavior,
            this.impl$viewDistance,
            this.impl$spawnPosition,
            this.impl$loadOnStartup,
            this.impl$performsSpawnLogic,
            this.impl$hardcore,
            this.impl$allowCommands,
            this.impl$pvp,
            this.impl$seed
        );
    }
}
