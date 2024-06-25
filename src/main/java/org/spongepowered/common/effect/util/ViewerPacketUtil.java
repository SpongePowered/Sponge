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
package org.spongepowered.common.effect.util;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Engine;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeEngine;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.network.packet.ChangeViewerEnvironmentPacket;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ViewerPacketUtil {

    public static ChangeViewerEnvironmentPacket changeEnvironment(final WorldType worldType) {
        return new ChangeViewerEnvironmentPacket((DimensionType) (Object) Objects.requireNonNull(worldType, "worldType"));
    }

    public static ClientboundBundlePacket spawnParticles(final ParticleEffect particleEffect, final double x, final double y, final double z) {
        return SpongeParticleHelper.createPacket(particleEffect, x, y, z);
    }

    public static ClientboundSoundEntityPacket playSound(final Sound sound, final Entity entity) {
        Objects.requireNonNull(entity, "entity");
        final Holder<SoundEvent> event = resolveEvent(sound);
        final SoundSource source = SpongeAdventure.asVanilla(sound.source());
        final net.minecraft.world.entity.Entity mcEntity = (net.minecraft.world.entity.Entity) entity;
        final long random = sound.seed().orElseGet(() -> mcEntity.level().getRandom().nextLong());
        return new ClientboundSoundEntityPacket(event, source, mcEntity, sound.volume(), sound.pitch(), random);
    }

    public static ClientboundSoundPacket playSound(final Sound sound, final RandomSource randomSource, final double x, final double y, final double z) {
        final Holder<SoundEvent> event = resolveEvent(sound);
        final SoundSource source = SpongeAdventure.asVanilla(sound.source());
        final long random = sound.seed().orElseGet(randomSource::nextLong);
        return new ClientboundSoundPacket(event, source, x, y, z, sound.volume(), sound.pitch(), random);
    }

    public static ClientboundStopSoundPacket stopSound(final SoundStop stop) {
        Objects.requireNonNull(stop, "stop");
        final @Nullable ResourceLocation sound = SpongeAdventure.asVanillaNullable(stop.sound());
        final @Nullable SoundSource source = SpongeAdventure.asVanillaNullable(stop.source());
        return new ClientboundStopSoundPacket(sound, source);
    }

    public static ClientboundLevelEventPacket playMusicDisc(final int x, final int y, final int z, final MusicDisc musicDisc, final RegistryAccess registryAccess) {
        Objects.requireNonNull(musicDisc, "musicDisc");
        final int songId = registryAccess.registryOrThrow(Registries.JUKEBOX_SONG).getId((JukeboxSong) (Object) musicDisc);
        return new ClientboundLevelEventPacket(1010, new BlockPos(x, y, z), songId, false);
    }

    public static ClientboundLevelEventPacket stopMusicDisc(final int x, final int y, final int z) {
        return new ClientboundLevelEventPacket(1011, new BlockPos(x, y, z), 0, false);
    }

    public static ClientboundBlockUpdatePacket blockUpdate(final int x, final int y, final int z, final BlockState state) {
        Objects.requireNonNull(state, "state");
        return new ClientboundBlockUpdatePacket(new BlockPos(x, y, z), (net.minecraft.world.level.block.state.BlockState) state);
    }

    public static ClientboundBlockUpdatePacket blockUpdate(final int x, final int y, final int z, final World<?, ?> world) {
        return new ClientboundBlockUpdatePacket((BlockGetter) world, new BlockPos(x, y, z));
    }

    public static ClientboundBlockDestructionPacket blockProgress(final int x, final int y, final int z, final double progress, final Engine engine) {
        if (progress < 0 || progress > 1) {
            throw new IllegalArgumentException("Progress must be between 0 and 1");
        }

        final BlockPos pos = new BlockPos(x, y, z);
        final int id = ((SpongeEngine) engine).getBlockDestructionIdCache().getOrCreate(pos);
        final int progressStage = progress == 1 ? 9 : (int) (progress * 10);
        return new ClientboundBlockDestructionPacket(id, pos, progressStage);
    }

    public static Optional<ClientboundBlockDestructionPacket> resetBlockProgress(final int x, final int y, final int z, final Engine engine) {
        final BlockPos pos = new BlockPos(x, y, z);
        return ((SpongeEngine) engine).getBlockDestructionIdCache().get(pos)
                .map(id -> new ClientboundBlockDestructionPacket(id, pos, -1));
    }

    public static ClientboundSetActionBarTextPacket setActionBarText(final Component message) {
        return new ClientboundSetActionBarTextPacket(SpongeAdventure.asVanilla(Objects.requireNonNull(message, "message")));
    }

    public static ClientboundBundlePacket showTitle(final Title title) {
        final Title.Times times = Objects.requireNonNull(title, "title").times();
        final List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
        if (times != null) {
            packets.add(ViewerPacketUtil.setTitlesAnimation(times));
        }
        packets.add(ViewerPacketUtil.setSubtitleText(title.subtitle()));
        packets.add(ViewerPacketUtil.setTitleText(title.title()));
        return new ClientboundBundlePacket(packets);
    }

    public static ClientboundSetTitleTextPacket setTitleText(final Component component) {
        return new ClientboundSetTitleTextPacket(SpongeAdventure.asVanilla(component));
    }

    public static ClientboundSetSubtitleTextPacket setSubtitleText(final Component component) {
        return new ClientboundSetSubtitleTextPacket(SpongeAdventure.asVanilla(component));
    }

    public static ClientboundSetTitlesAnimationPacket setTitlesAnimation(final Title.Times times) {
        return new ClientboundSetTitlesAnimationPacket(
                durationToTicks(times.fadeIn()),
                durationToTicks(times.stay()),
                durationToTicks(times.fadeOut()));
    }

    public static ClientboundClearTitlesPacket clearTitles(final boolean resetTimes) {
        return new ClientboundClearTitlesPacket(resetTimes);
    }

    private static Holder<SoundEvent> resolveEvent(final @NonNull Sound sound) {
        final ResourceLocation soundKey = SpongeAdventure.asVanilla(Objects.requireNonNull(sound, "sound").name());
        final var soundEventRegistry = SpongeCommon.vanillaRegistry(Registries.SOUND_EVENT);
        final SoundEvent event = soundEventRegistry.getOptional(soundKey)
                .orElseGet(() -> SoundEvent.createVariableRangeEvent(soundKey));

        return soundEventRegistry.wrapAsHolder(event);
    }

    private static int durationToTicks(final Duration duration) {
        return (int) (duration.toMillis() / 50L);
    }

    private ViewerPacketUtil() {
    }
}
