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
package org.spongepowered.common.mixin.invalid.api.mcp.entity.player;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SWorldBorderPacket;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.mixin.api.mcp.entity.player.PlayerEntityMixin_API;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
@Implements(@Interface(iface = Player.class, prefix = "player$"))
public abstract class ServerPlayerEntityMixin_API extends PlayerEntityMixin_API implements Player {

    @Shadow @Final public MinecraftServer server;
    @Shadow public ServerPlayNetHandler connection;

    @Nullable private WorldBorder api$worldBorder;

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        final List<IPacket<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            if (position.sub(this.posX, this.posY, this.posZ).lengthSquared() < (long) radius * (long) radius) {
                for (final IPacket<?> packet : packets) {
                    this.connection.sendPacket(packet);
                }
            }
        }
    }


    @Override
    public void setWorldBorder(@Nullable final WorldBorder border, final Cause cause) {
        if (this.api$worldBorder == border) {
            return; //do not fire an event since nothing would have changed
        }
        if (!SpongeImpl.postEvent(SpongeEventFactory.createChangeWorldBorderEventTargetPlayer(cause,
            Optional.ofNullable(this.api$worldBorder), Optional.ofNullable(border), this))) {
            if (this.api$worldBorder != null) { //is the world border about to be unset?
                ((WorldBorderBridge) this.api$worldBorder).accessor$getListeners().remove(
                    ((ServerPlayerEntityBridge) this).bridge$getWorldBorderListener()); //remove the listener, if so
            }
            this.api$worldBorder = border;
            if (this.api$worldBorder != null) {
                ((net.minecraft.world.border.WorldBorder) this.api$worldBorder).addListener(
                    ((ServerPlayerEntityBridge) this).bridge$getWorldBorderListener());
                this.connection.sendPacket(
                    new SWorldBorderPacket((net.minecraft.world.border.WorldBorder) this.api$worldBorder,
                        SWorldBorderPacket.Action.INITIALIZE));
            } else { //unset the border if null
                this.connection.sendPacket(
                    new SWorldBorderPacket(this.world.getWorldBorder(), SWorldBorderPacket.Action.INITIALIZE));
            }
        }
    }

}
