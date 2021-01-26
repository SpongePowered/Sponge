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
package org.spongepowered.common.mixin.entitycollision.world.level.chunk;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.entitycollision.CollisionCapabilityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@Mixin(net.minecraft.world.level.chunk.LevelChunk.class)
public abstract class LevelChunkMixin_EntityCollision {

    @Shadow public abstract Level shadow$getLevel();

    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/List;Ljava/util/function/Predicate;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false), cancellable = true)
    private void collisionsImpl$checkForCollisionRules(final @Nullable Entity entity,
            final AABB bb,
            final List<Entity> entities,
            final Predicate<? super Entity> filter,
            final CallbackInfo ci) {
        // ignore players and entities with parts (ex. EnderDragon)
        if (this.shadow$getLevel().isClientSide() || entities == null || entity instanceof Player || entity instanceof EnderDragon) {
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

    @Inject(method = "getEntitiesOfClass",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false), cancellable = true)
    private <T extends Entity> void collisionsImpl$checkForCollisionRules(final Class<? extends T> entityClass, final AABB bb,
            final List<T> entities, final Predicate<? super T> filter, final CallbackInfo ci) {
        // ignore player checks
        // ignore item check (ex. Hoppers)
        if (this.shadow$getLevel().isClientSide() || Player.class.isAssignableFrom(entityClass) || ItemEntity.class == entityClass) {
            return;
        }

        if (!this.entityCollision$allowEntityCollision(entities)) {
            ci.cancel();
        }
    }

    private <T extends Entity> boolean entityCollision$allowEntityCollision(final List<T> entities) {
        if (((WorldBridge) this.shadow$getLevel()).bridge$isFake()) {
            return true;
        }

        if (PhaseTracker.getInstance().getPhaseContext().isCollision()) {
            // allow explosions
            return true;
        }

        final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
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
            collisionBridge.collision$initializeCollisionState(this.shadow$getLevel());
            collisionBridge.collision$requiresCollisionsCacheRefresh(false);
        }

        return !((collisionBridge.collision$getMaxCollisions() >= 0) && (entities.size() >= collisionBridge.collision$getMaxCollisions()));
    }
}
