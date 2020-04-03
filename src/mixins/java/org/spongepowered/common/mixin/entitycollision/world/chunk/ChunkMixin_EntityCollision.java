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
package org.spongepowered.common.mixin.entitycollision.world.chunk;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.bridge.entitycollision.CollisionCapabilityBridge;

import java.util.List;
import java.util.function.Predicate;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin_EntityCollision {

    @Shadow public abstract World shadow$getWorld();

    @Inject(method = "getEntitiesWithinAABBForEntity",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false), cancellable = true)
    private void collisionsImpl$checkForCollisionRules(@Nullable Entity entity, AxisAlignedBB bb, List<Entity> entities, Predicate<? super Entity> filter,
            CallbackInfo ci) {
        // ignore players and entities with parts (ex. EnderDragon)
        if (this.shadow$getWorld().isRemote() || entities == null || entity instanceof PlayerEntity || entity instanceof EnderDragonEntity) {
            return;
        }
        // Run hook in LivingEntity to support maxEntityCramming
        if (entity instanceof LivingEntity && ((CollisionCapabilityBridge) entity).collision$isRunningCollideWithNearby()) {
            return;
        }

        if (!this.entityCollision$allowEntityCollision(entities)) {
            ci.cancel();
        }
    }

    @Inject(method = "getEntitiesOfTypeWithinAABB",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false), cancellable = true)
    private <T extends Entity> void collisionsImpl$checkForCollisionRules(Class<? extends T> entityClass, AxisAlignedBB bb,
            List<T> entities, Predicate<? super T> filter, CallbackInfo ci) {
        // ignore player checks
        // ignore item check (ex. Hoppers)
        if (this.shadow$getWorld().isRemote() || PlayerEntity.class.isAssignableFrom(entityClass) || ItemEntity.class == entityClass) {
            return;
        }

        if (!this.entityCollision$allowEntityCollision(entities)) {
            ci.cancel();
        }
    }

    private <T extends Entity> boolean entityCollision$allowEntityCollision(List<T> entities) {
        if (((WorldBridge) this.shadow$getWorld()).bridge$isFake()) {
            return true;
        }

        if (PhaseTracker.getInstance().getCurrentState().ignoresEntityCollisions()) {
            // allow explosions
            return true;
        }

        final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getCurrentContext();
        final Object source = phaseContext.getSource();
        if (source == null) {
            return true;
        }

        CollisionCapabilityBridge collisionBridge = null;

        if (source instanceof LocatableBlock) {
            final LocatableBlock locatable = (LocatableBlock) source;
            final BlockType blockType = locatable.getLocation().getBlock().getType();
            collisionBridge = (CollisionCapabilityBridge) blockType;
        } else if (source instanceof CollisionCapabilityBridge) {
            collisionBridge = (CollisionCapabilityBridge) source;
        }

        if (collisionBridge == null) {
            return true;
        }

        if (collisionBridge.collision$requiresCollisionsCacheRefresh()) {
            collisionBridge.collision$initializeCollisionState(this.shadow$getWorld());
            collisionBridge.collision$requiresCollisionsCacheRefresh(false);
        }

        return !((collisionBridge.collision$getMaxCollisions() >= 0) && (entities.size() >= collisionBridge.collision$getMaxCollisions()));
    }
}
