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
package org.spongepowered.common.mixin.tracker.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.BasicEntityContext;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Tracker extends PlayerEntity {

    private ServerPlayerEntityMixin_Tracker(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
        // Shadowed
    }

    @Shadow public abstract ServerWorld shadow$getServerWorld();

    @Shadow public ServerPlayNetHandler connection;

    /**
     * @author Aaron1101 August 11th, 2018
     * @author gabizou - February 10th, 2020 - Minecraft 1.14.3
     *
     * @reason Wrap the method in a try-with-resources for a EntityPhase.State.PLAYER_WAKE_UP
     */
    @Overwrite
    public void wakeUpPlayer(final boolean immediately, final boolean updateWorldFlag, final boolean setSpawn) {
        // Sponge start - enter phase
        try (final BasicEntityContext basicEntityContext = EntityPhase.State.PLAYER_WAKE_UP.createPhaseContext(PhaseTracker.SERVER)
                .source(this)) {
            basicEntityContext.buildAndSwitch();
            // Sponge end

            if (this.isSleeping()) {
                this.shadow$getServerWorld().getChunkProvider().sendToTrackingAndSelf(this, new SAnimateHandPacket(this, 2));
            }

            super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);
            if (this.connection != null) {
                this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            }
        } // Sponge - add bracket to close 'try' block
    }
}
