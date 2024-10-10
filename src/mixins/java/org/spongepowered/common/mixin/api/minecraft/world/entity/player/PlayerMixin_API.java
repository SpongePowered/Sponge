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
package org.spongepowered.common.mixin.api.minecraft.world.entity.player;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.effect.ViewerBridge;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.effect.SpongeViewer;
import org.spongepowered.common.effect.util.ViewerPacketUtil;
import org.spongepowered.common.mixin.api.minecraft.world.entity.LivingEntityMixin_API;
import org.spongepowered.common.util.BookUtil;

import java.util.Objects;
import java.util.Set;

@Mixin(net.minecraft.world.entity.player.Player.class)
@Implements(@Interface(iface=Identified.class, prefix = "identified$", remap = Remap.NONE))
public abstract class PlayerMixin_API extends LivingEntityMixin_API implements Player, SpongeViewer {

    // @formatter:off
    @Shadow public AbstractContainerMenu containerMenu;
    @Shadow public abstract ItemCooldowns shadow$getCooldowns();
    @Shadow public abstract Component shadow$getName();
    @Shadow public abstract GameProfile shadow$getGameProfile();
    // @formatter:on

    @Shadow public abstract Component getName();

    public final boolean impl$isFake = ((PlatformEntityBridge) this).bridge$isFakePlayer();

    @Override
    public String name() {
        return this.shadow$getName().getString();
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.requireValue(Keys.AFFECTS_SPAWNING).asImmutable());
        values.add(this.requireValue(Keys.CAN_FLY).asImmutable());
        values.add(this.requireValue(Keys.DOMINANT_HAND).asImmutable());
        values.add(this.requireValue(Keys.EXHAUSTION).asImmutable());
        values.add(this.requireValue(Keys.EXPERIENCE).asImmutable());
        values.add(this.requireValue(Keys.EXPERIENCE_FROM_START_OF_LEVEL).asImmutable());
        values.add(this.requireValue(Keys.EXPERIENCE_LEVEL).asImmutable());
        values.add(this.requireValue(Keys.EXPERIENCE_SINCE_LEVEL).asImmutable());
        values.add(this.requireValue(Keys.FLYING_SPEED).asImmutable());
        values.add(this.requireValue(Keys.FOOD_LEVEL).asImmutable());
        values.add(this.requireValue(Keys.IS_FLYING).asImmutable());
        values.add(this.requireValue(Keys.IS_SLEEPING).asImmutable());
        values.add(this.requireValue(Keys.IS_SLEEPING_IGNORED).asImmutable());
        values.add(this.requireValue(Keys.MAX_EXHAUSTION).asImmutable());
        values.add(this.requireValue(Keys.MAX_FOOD_LEVEL).asImmutable());
        values.add(this.requireValue(Keys.MAX_SATURATION).asImmutable());
        values.add(this.requireValue(Keys.SATURATION).asImmutable());
        values.add(this.requireValue(Keys.SLEEP_TIMER).asImmutable());
        values.add(this.requireValue(Keys.WALKING_SPEED).asImmutable());

        this.getValue(Keys.FIRST_DATE_JOINED).map(Value::asImmutable).ifPresent(values::add);
        this.getValue(Keys.LAST_DATE_PLAYED).map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

    public Identity identified$identity() {
        return this.profile();
    }

    // Viewer

    @Override
    public void playMusicDisc(final int x, final int y, final int z, final MusicDisc musicDisc) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.playMusicDisc(x, y, z, musicDisc, this.shadow$level().registryAccess()));
    }

    @Override
    public void resetBlockChange(final int x, final int y, final int z) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.blockUpdate(x, y, z, this.world()));
    }

    @Override
    public void sendBlockProgress(final int x, final int y, final int z, final double progress) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.blockProgress(x, y, z, progress, this.world().engine()));
    }

    @Override
    public void resetBlockProgress(final int x, final int y, final int z) {
        ViewerPacketUtil.resetBlockProgress(x, y, z, this.world().engine()).ifPresent(((ViewerBridge) this)::bridge$sendToViewer);
    }

    // Audience

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.playSound(sound, this.shadow$level().random, x, y, z));
    }

    @Override
    public void playSound(final Sound sound) {
        this.playSound(sound, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ());
    }

    @Override
    public void playSound(final Sound sound, final Sound.Emitter emitter) {
        Objects.requireNonNull(emitter, "emitter");
        if (emitter == Sound.Emitter.self()) {
            ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.playSound(sound, this));
        } else if (emitter instanceof final Entity entityEmitter) {
            ((ViewerBridge) this).bridge$sendToViewer(ViewerPacketUtil.playSound(sound, entityEmitter));
        } else {
            throw new IllegalArgumentException("Specified emitter '" + emitter + "' is not a Sponge Entity or Emitter.self(), was of type '" + emitter.getClass() + "'");
        }
    }

    @Override
    public void openBook(final Book book) {
        ((ViewerBridge) this).bridge$sendToViewer(BookUtil.createFakeBookViewPacket(this, Objects.requireNonNull(book, "book")));
    }

    // BossBarViewer

    @Override
    public Iterable<? extends BossBar> activeBossBars() {
        return ((PlayerBridge) this).bridge$getActiveBossBars();
    }
}
