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
package org.spongepowered.common.mixin.core.server;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import java.util.UUID;

@Mixin(ServerConfigurationManager.class)
public class MixinServerConfigurationManager {

    @Shadow public Map<UUID, EntityPlayerMP> uuidToPlayerMap;

    private Scoreboard scoreboard;

    @Inject(method = "recreatePlayerEntity", at = @At("HEAD"))
    public void onRecreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd, CallbackInfoReturnable<EntityPlayerMP> cir) {
        this.scoreboard = ((Player) playerIn).getScoreboard();
    }

    @Inject(method = "recreatePlayerEntity", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRecreatePlayerEntityReturn(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd, CallbackInfoReturnable<EntityPlayerMP> cir, World world, BlockPos blockpos, boolean flag1, Object object, EntityPlayerMP entityplayermp1, WorldServer worldserver, BlockPos blockpos1 ) {
        ((Player) entityplayermp1).setScoreboard(this.scoreboard);
        this.scoreboard = null;
    }

    @Surrogate
    public void onRecreatePlayerEntityReturn(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd, CallbackInfoReturnable<EntityPlayerMP> cir) {
        ((Player) this.uuidToPlayerMap.get(playerIn.getUniqueID())).setScoreboard(this.scoreboard);
        this.scoreboard = null;
    }

}
