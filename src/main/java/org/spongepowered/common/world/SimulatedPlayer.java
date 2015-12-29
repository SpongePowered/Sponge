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
package org.spongepowered.common.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import org.spongepowered.common.SpongeImpl;

import java.util.Set;

// Based on Forge's FakePlayer
public class SimulatedPlayer extends EntityPlayerMP implements FakePlayer {

    public static class FakeNetHandler extends NetHandlerPlayServer {

        private static final NetworkManager fakeNetworkManager = new NetworkManager(EnumPacketDirection.CLIENTBOUND);

        public FakeNetHandler(EntityPlayerMP player) {
            super(player.mcServer, fakeNetworkManager, player);
        }

        @Override
        public void update() {
        }

        @Override
        public void disconnect(String reason) {
        }

        @Override
        public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, @SuppressWarnings("rawtypes") Set relativeSet) {
        }

        @Override
        public void onDisconnect(ITextComponent reason) {
        }

        @Override
        public void sendPacket(Packet<?> packetIn) {
        }
    }

    public SimulatedPlayer(WorldServer world, GameProfile profile) {
        super(SpongeImpl.getServer(), world, profile, new PlayerInteractionManager(world));
        this.connection = new FakeNetHandler(this);
    }

    @Override
    public boolean canUseCommand(int i, String s) {
        return false;
    }

    @Override
    public void sendMessage(ITextComponent component) {
    }

    @Override
    public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {
    }

    @Override
    public void addStat(StatBase par1StatBase, int par2) {
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public boolean canAttackPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void onDeath(DamageSource source) {
        return;
    }

    @Override
    public void onUpdate() {
        return;
    }

    @Override
    public Entity changeDimension(int dim) {
        return this;
    }

    @Override
    public void handleClientSettings(CPacketClientSettings packetIn) {
    }
}
