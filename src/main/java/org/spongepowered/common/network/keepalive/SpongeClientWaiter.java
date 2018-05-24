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
package org.spongepowered.common.network.keepalive;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeImpl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpongeClientWaiter {

    public static final SpongeClientWaiter INSTANCE = new SpongeClientWaiter();

    private Map<UUID, Deque<CallbackData>> callbacks = new HashMap<>();

    // Normally, only waiting for one confirmation packet is enough,
    // since the player entity should be rendered in each call
    // to Minecraft#runGameLoop.

    // However, this will not be sufficient if the player's
    // chunk has not yet been loaded on the client, such as
    // when first logging in. Since an entity will not
    // be rendered until its chunk has loaded, there is a chance
    // that the first render of the player and the restoration
    // of the tab list will occur during the same call to
    // Minecraft#runGameLoop.
    //
    // To ensure that the player entity has always rendered
    // at least once, we perform two confirm round-trips.
    // We know that the chunk data will be send during the same
    // server tick as the login, so performing two round trips
    // ensures that the chunk data has been loaded for a full
    // run of Minecraft#runGameLoop on the client.
    private static final int NUM_CONFIRM_PACKETS = 2;

    private static class CallbackData {

        Runnable callback;
        int packetConfirmCount;

        public CallbackData(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public String toString() {
            return "CallbackData{" +
                    "callback=" + callback +
                    ", packetConfirmCount=" + packetConfirmCount +
                    '}';
        }
    }

    private SpongeClientWaiter() {

    }

    /**
     * Runs the specified {@link Runnable} after the client completes
     * at least one render tick.
     *
     * When modifying player skins, we need to modify the player's tab list,
     * which determines which skin is rendered for a player. However, when a client-side
     * player entity is rendered for the first time, its skin is retrieved from the tab
     * list and cached. This means that when we restore the player's tab list to its original
     * state (removing the modified tab list entry, and possible adding the original)
     * , we need to ensure that the target entity has already been rendered on the client.
     *
     * Previously, this was accomplished by waiting several after spawning the client-side entity
     * before restoring the tab list. However, this was only a partial solution. Due to network
     * latency, or unlucky client-side thread scheduling, the client could end up in a state like this:
     *
     * Minecraft.scheduledTasks = [SPacketPlayerListItem.Action.ADD_PLAYER, SPacketSpawnPlayer, SPacketPlayerListItem.Action.REMOVE_PLAYER]
     *
     * In this state, the tab list modification would occur immediately after the client
     * spawned the entity. All scheduled tasks in the queue are processed at the beginning of
     * Minecraft#runGameLoop, meaning that no render tick would occur between the spawning
     * of the player and the restoration of the tab list.
     *
     * Increasing the server-side delay doesn't prevent this from happening,
     * though it does make it less likely. We have no control over the timing
     * of how the client Netty thread enqueues packet handlers.
     *
     * To guarantee that a client render tick has occured, we make ues of
     * SPacketConfirmTransaction
     *
     * <p>When the client receives a SPacketConfirmTransaction, it sends a CPacketConfirmTransaction
     * on the main thread (through a scheduled task). The client processes scheduled tasks
     * in 'batches' - at the beginning of Minecraft#runGameLoop, the list of scheduled
     * tasks is drained from a 'syncrhonized' block. This means that once the client
     * has started processing scheduled tasks (such as packet handlers), no new
     * scheduled tasks from Netty threads can be added until the next invocation of
     * runGameLoop.
     *
     * This means that, once the server receives the CPacketConfirmTransaction, any
     * packets the server sends in response are guaranteed to not be handled
     * in the same 'batch' of scheduled tasks.
     *
     * Instead, the earliest that the server's response can be processed is during
     * the next invocation of Minecraft#runGameLoop. This means that the client
     * is guaranteed to have performed a render tick - rendering occurs after
     * processing scheduled tasks, which means that rendering will always
     * occur between 'batches' of scheduled tasks.</p>
     *
     * This becomes important when modifying player skins, which is done through
     * the tab list. The client caches the GameProfile it retrieves from its local
     * tab list, but only during rendering. If we wish to briefly add a tab list entry
     * (for example, to faciliate skin changing), we must ensure that at least
     * one render tick has occured before we remove the tab list entry again.
     * Otherwise, the client will fail to cache the skin from its tab list, resulting
     * in the skin not being applied.
     *
     * @param runnable
     * @param player
     */
    public void waitForRenderTick(Runnable runnable, EntityPlayerMP player) {
        Deque<CallbackData> data = this.callbacks.computeIfAbsent(player.getUniqueID(), k -> new ArrayDeque<>());
        data.addLast(new CallbackData(runnable));
        this.sendConfirmPacket(player);
    }

    private void sendConfirmPacket(EntityPlayerMP player) {
        player.connection.sendPacket(new SPacketConfirmTransaction(0, (short) -1, false));
    }

    public boolean onClientConfirm(CPacketConfirmTransaction packet, EntityPlayerMP player) {
        if (packet.getUid() == -1) {
            Deque<CallbackData> callbacks = this.callbacks.get(player.getUniqueID());
            if (callbacks == null) {
                SpongeImpl.getLogger().warn(String.format("Recieved unexpected CPacketConfirmTransaction player %s: %s", player, packet.getWindowId()));
                return false;
            }
            if (++callbacks.getFirst().packetConfirmCount < NUM_CONFIRM_PACKETS) {
                this.sendConfirmPacket(player);
            } else {
                callbacks.removeFirst().callback.run();
            }
            return true;
        }
        return false;
    }

    public void onClientPacketPlayer(CPacketPlayer packet, EntityPlayerMP player) {
        /*
        Deque<CallbackData> callbacks = this.callbacks.get(player.getUniqueID());
        if (callbacks != null) {
            CallbackData data = callbacks.peekFirst();
            if (data != null && data.receivedPacketConfirm) {
                callbacks.removeFirst().callback.run();
            }
        }*/
    }

}
