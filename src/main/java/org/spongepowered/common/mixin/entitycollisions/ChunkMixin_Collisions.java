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
package org.spongepowered.common.mixin.entitycollisions;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.CollisionsCapability;

import java.util.List;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public class ChunkMixin_Collisions {

    @Shadow @Final private World world;

    @SuppressWarnings("Guava")
    @Inject(method = "getEntitiesWithinAABBForEntity",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false), cancellable = true)
    private void collisionsImpl$checkForCollisionRules(final Entity entityIn, final AxisAlignedBB aabb, final List<Entity> listToFill,
        final Predicate<? super Entity> predicate, final CallbackInfo ci) {
        // ignore players and entities with parts (ex. EnderDragon)
        if (this.world.isRemote || entityIn == null || entityIn instanceof PlayerEntity || entityIn.getParts() != null) {
            return;
        }
        // Run hook in EntityLivingBase to support maxEntityCramming
        if (entityIn != null && entityIn instanceof LivingEntity && ((CollisionsCapability) entityIn).collision$isRunningCollideWithNearby()) {
            return;
        }

        if (!collisionsImpl$allowEntityCollision(listToFill)) {
            ci.cancel();
        }
    }

    @SuppressWarnings("Guava")
    @Inject(method = "getEntitiesOfTypeWithinAABB",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false), cancellable = true)
    private <T extends Entity> void collisionsImpl$checkForCollisionRules(final Class<? extends T> entityClass, final AxisAlignedBB aabb,
        final List<T> listToFill, final Predicate<? super T> p_177430_4_, final CallbackInfo ci) {
        // ignore player checks
        // ignore item check (ex. Hoppers)
        if (this.world.isRemote || PlayerEntity.class.isAssignableFrom(entityClass) || ItemEntity.class == entityClass) {
            return;
        }

        if (!collisionsImpl$allowEntityCollision(listToFill)) {
            ci.cancel();
        }
    }

    private <T extends Entity> boolean collisionsImpl$allowEntityCollision(final List<T> listToFill) {
        if (this.world instanceof WorldServerBridge) {
            if (PhaseTracker.getInstance().getCurrentState().ignoresEntityCollisions()) {
                // allow explosions
                return true;
            }

            final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getCurrentContext();
            final Object source = phaseContext.getSource();
            if (source == null) {
                return true;
            }

            if (source instanceof LocatableBlock) {
                final LocatableBlock locatable = (LocatableBlock) source;
                final BlockType blockType =locatable.getLocation().getBlockType();
                final CollisionsCapability spongeBlock = (CollisionsCapability) blockType;
                if (spongeBlock.collision$requiresCollisionsCacheRefresh()) {
                    spongeBlock.collision$initializeCollisionState(this.world);
                    spongeBlock.collision$requiresCollisionsCacheRefresh(false);
                }

                return !((spongeBlock.collision$getMaxCollisions() >= 0) && (listToFill.size() >= spongeBlock.collision$getMaxCollisions()));
            } else if (source instanceof CollisionsCapability) {
                final CollisionsCapability spongeEntity = (CollisionsCapability) source;
                if (spongeEntity.collision$requiresCollisionsCacheRefresh()) {
                    spongeEntity.collision$initializeCollisionState(this.world);
                    spongeEntity.collision$requiresCollisionsCacheRefresh(false);
                }

                return !((spongeEntity.collision$getMaxCollisions() >= 0) && (listToFill.size() >= spongeEntity.collision$getMaxCollisions()));
            }

            return true;
        }

        return true;
    }
}
