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
package org.spongepowered.vanilla.mixin.core.server.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.network.packet.ChangeViewerEnvironmentPacket;
import org.spongepowered.common.network.packet.SpongePacketHandler;
import org.spongepowered.common.world.portal.PortalLogic;
import org.spongepowered.vanilla.mixin.core.world.entity.EntityMixin_Vanilla;

import javax.annotation.Nullable;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public abstract class ServerPlayerMixin_Vanilla extends EntityMixin_Vanilla implements ServerPlayerBridge {

    @Override
    public void bridge$sendViewerEnvironment(final DimensionType dimensionType) {
        if (this.bridge$getClientType() == ClientType.SPONGE_VANILLA) {
            SpongePacketHandler.getChannel().sendTo((ServerPlayer) this, new ChangeViewerEnvironmentPacket(dimensionType));
        }
    }

    /**
     * @author dualspiral - 18th December 2020 - 1.16.4
     * @reason Redirects the vanilla changeDimension method to our own
     *         to support our event and other logic (see
     *         ServerPlayerEntityMixin on the common mixin sourceset for
     *         details).
     *
     *         This method does not explicitly exist on SeverPlayerEntity
     *         on Forge, it is an overridden method in Vanilla so needs doing
     *         here as well as in EntityMixin_Vanilla.
     *
     *         This will get called on the nether dimension changes, as the
     *         end portal teleport call itself has been redirected to provide
     *         the correct type.
     */
    @Overwrite
    @Nullable
    public net.minecraft.world.entity.Entity changeDimension(final ServerLevel target) {
        return this.bridge$changeDimension(target, (PortalLogic) target.getPortalForcer());
    }

}
