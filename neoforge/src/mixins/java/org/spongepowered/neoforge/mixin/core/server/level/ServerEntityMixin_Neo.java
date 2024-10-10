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
package org.spongepowered.neoforge.mixin.core.server.level;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.entity.living.human.HumanEntity;

import java.util.EnumSet;
import java.util.List;

@Mixin(ServerEntity.class)
public class ServerEntityMixin_Neo {
    @Shadow @Final private Entity entity;

    /**
     * @author gabizou
     * @reason Because the entity spawn packet is just a lone packet, we have to actually
     * do some hackery to create the player list packet first, then the spawn packet,
     * then perform the remove packet.
     */
    @Redirect(
        method = "sendPairingData",
        at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/bundle/PacketAndPayloadAcceptor;accept(Lnet/minecraft/network/protocol/Packet;)Lnet/neoforged/neoforge/network/bundle/PacketAndPayloadAcceptor;", ordinal = 0)
    )
    public PacketAndPayloadAcceptor impl$sendHumanSpawnPacket(PacketAndPayloadAcceptor consumer, Packet spawnPacket) {
        if (!(this.entity instanceof final HumanEntity human)) {
            return consumer.accept(spawnPacket);
        }
        // Adds the GameProfile to the client
        consumer.accept(human.createPlayerListPacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)));
        // Actually spawn the human (a player)
        consumer.accept(spawnPacket);
        // Remove from the player map
        final ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(List.of(human.getUUID()));
        if (human.canRemoveFromListImmediately()) {
            consumer.accept(removePacket);
        } else {
            // Human is a Player entity on the client and needs to tick once for the skin to render
            human.removeFromTabListDelayed(null, removePacket);
        }
        return consumer;
    }
}
