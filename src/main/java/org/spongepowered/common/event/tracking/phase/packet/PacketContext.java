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
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.event.tracking.PhaseContext;

@SuppressWarnings("unchecked")
public class PacketContext<P extends PacketContext<P>> extends PhaseContext<P> {

    EntityPlayerMP packetPlayer;
    Packet<?> packet;
    private ItemStackSnapshot cursor;
    private boolean ignoreCreative;
    private ItemStack itemUsed;

    protected PacketContext(PacketState<? extends P> state) {
        super(state);
    }

    public P packet(Packet<?> packet) {
        this.packet = packet;
        return (P) this;
    }

    public P packetPlayer(EntityPlayerMP playerMP) {
        this.packetPlayer = playerMP;
        return (P) this;
    }

    public P cursor(ItemStackSnapshot snapshot) {
        this.cursor = snapshot;
        return (P) this;
    }

    public P ignoreCreative(boolean creative) {
        this.ignoreCreative = creative;
        return (P) this;
    }

    public EntityPlayerMP getPacketPlayer() {
        return packetPlayer;
    }

    public Player getSpongePlayer() {
        return (Player) this.packetPlayer;
    }

    public <K extends Packet<?>> K getPacket() {
        return (K) packet;
    }

    public ItemStackSnapshot getCursor() {
        return cursor;
    }

    public boolean getIgnoringCreative() {
        return ignoreCreative;
    }

    public P itemUsed(ItemStack stack) {
        this.itemUsed = stack;
        return (P) this;
    }

    public ItemStack getItemUsed() {
        return itemUsed;
    }
}
