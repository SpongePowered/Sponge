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
package org.spongepowered.common.event;


import org.spongepowered.api.Game;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;

/**
 * Utility that fires events that normally Forge fires at (in spots). Typically
 * our penultimate goal is to not remove spots where events occur but sometimes
 * it happens (in @Overwrites typically). Normally events that are in Forge are
 * called themselves in SpongeVanilla but when it can't really occur, we fix
 * this issue with Sponge by overwriting this class
 */
public class SpongeImplEventFactory {

    public static LoadWorldEvent createLoadWorldEvent(Game game, World world) {
        return SpongeEventFactory.createLoadWorldEvent(Cause.of(Sponge.getGame().getServer()), game, world);
    }

    public static ClientConnectionEvent.Join createClientConnectionEventJoin(RemoteConnection connection, Transform<World> fromTransform, Game game, Text message, Text originalMessage, MessageSink originalSink, GameProfile profile, MessageSink sink, Player targetEntity, Transform<World> toTransform) {
        return SpongeEventFactory.createClientConnectionEventJoin(connection, fromTransform, game, message, originalMessage, originalSink, profile, sink, targetEntity, toTransform);
    }

    public static RespawnPlayerEvent createRespawnPlayerEvent(boolean bedSpawn, Transform<World> fromTransform, Game game, Player targetEntity, Transform<World> toTransform) {
        return SpongeEventFactory.createRespawnPlayerEvent(bedSpawn, fromTransform, game, targetEntity, toTransform);
    }

    public static ClientConnectionEvent.Disconnect createClientConnectionEventDisconnect(RemoteConnection connection, Game game, Text message, Text originalMessage, MessageSink originalSink, GameProfile profile, MessageSink sink, Player targetEntity) {
        return SpongeEventFactory.createClientConnectionEventDisconnect(connection, game, message, originalMessage, originalSink, profile, sink, targetEntity);
    }

}
