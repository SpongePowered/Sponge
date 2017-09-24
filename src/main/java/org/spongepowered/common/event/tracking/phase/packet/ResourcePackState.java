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
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.living.humanoid.player.ResourcePackStatusEvent;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;

public class ResourcePackState extends BasicPacketState {

    @Override
    public void unwind(BasicPacketContext phaseContext) {
        final EntityPlayerMP player = phaseContext.getPacketPlayer();

        final NetHandlerPlayServer connection = player.connection;
        final IMixinNetHandlerPlayServer mixinHandler = (IMixinNetHandlerPlayServer) connection;
        final CPacketResourcePackStatus resource = phaseContext.getPacket();
        final ResourcePackStatusEvent.ResourcePackStatus status;
        ResourcePack pack = ((IMixinPacketResourcePackSend) mixinHandler.getPendingResourcePackQueue().peek()).getResourcePack();
        switch (resource.action) {
            case ACCEPTED:
                status = ResourcePackStatusEvent.ResourcePackStatus.ACCEPTED;
                break;
            case DECLINED:
                status = ResourcePackStatusEvent.ResourcePackStatus.DECLINED;
                break;
            case SUCCESSFULLY_LOADED:
                status = ResourcePackStatusEvent.ResourcePackStatus.SUCCESSFULLY_LOADED;
                break;
            case FAILED_DOWNLOAD:
                status = ResourcePackStatusEvent.ResourcePackStatus.FAILED;
                break;
            default:
                throw new AssertionError();
        }
        SpongeImpl.postEvent(
            SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack, (Player) player, status));
        if (status.wasSuccessful().isPresent()) {
            mixinHandler.getPendingResourcePackQueue().remove();

            if (!mixinHandler.getPendingResourcePackQueue().isEmpty()) {

                while (mixinHandler.getPendingResourcePackQueue().size() > 1) {
                    // Fire events so other plugins know what happened to their
                    // resource packs.
                    pack = ((IMixinPacketResourcePackSend) mixinHandler.getPendingResourcePackQueue().remove()).getResourcePack();
                    if (status == ResourcePackStatusEvent.ResourcePackStatus.DECLINED) {
                        SpongeImpl.postEvent(SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack,
                            (Player) player,
                            ResourcePackStatusEvent.ResourcePackStatus.DECLINED));
                    } else {
                        // Say it was successful even if it wasn't. Minecraft
                        // makes no guarantees, and I don't want to change the
                        // API.
                        // In addition, I would assume this would result in the
                        // expected behavior from plugins.
                        SpongeImpl.postEvent(SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack,
                            (Player) player,
                            ResourcePackStatusEvent.ResourcePackStatus.ACCEPTED));
                        SpongeImpl.postEvent(SpongeEventFactory.createResourcePackStatusEvent(Sponge.getCauseStackManager().getCurrentCause(), pack,
                            (Player) player,
                            ResourcePackStatusEvent.ResourcePackStatus.SUCCESSFULLY_LOADED));
                    }
                }
                if (connection.getNetworkManager().isChannelOpen()) {
                    connection.sendPacket(mixinHandler.getPendingResourcePackQueue().element());
                }
            }
        }
    }
}
