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
package org.spongepowered.server.mixin.core.server.management;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.ForgeITeleporterBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.NetworkUtil;

@Mixin(value = PlayerList.class, priority = 1001)
public abstract class PlayerListMixin_Vanilla {

    @Shadow @Final private MinecraftServer server;

    /**
     * @author gabizou - June 18th, 2019 - 1.12.2
     * @reason Use the common initialize connection with events
     * since Forge changes this method's signature to add the
     * net handler play server....
     */
    @Overwrite
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP playerIn) {
        NetworkUtil.initializeConnectionToPlayer((PlayerList) (Object) this, netManager, playerIn, null);
    }

    /**
     * @author Zidane
     * @reason Re-route to the common hook
     */
    @Overwrite
    public void changePlayerDimension(EntityPlayerMP player, int dimensionIn) {
        final WorldServer world = this.server.getWorld(dimensionIn);
        EntityUtil.transferPlayerToWorld(player, null, world, (ForgeITeleporterBridge) world.getDefaultTeleporter());
    }

    /**
     * @author Zidane
     * @reason Re-route to the common hook
     */
    @Overwrite
    public void transferEntityToWorld(Entity entityIn, int lastDimension, WorldServer oldWorldIn, WorldServer toWorldIn) {
        EntityUtil.transferEntityToWorld(entityIn, null, toWorldIn, (ForgeITeleporterBridge) toWorldIn.getDefaultTeleporter(), false);
    }
}
