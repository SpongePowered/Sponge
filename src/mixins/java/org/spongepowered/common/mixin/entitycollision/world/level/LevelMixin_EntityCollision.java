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
package org.spongepowered.common.mixin.entitycollision.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.entitycollision.CollisionCapabilityBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class LevelMixin_EntityCollision implements AutoCloseable, LevelAccessor, LevelBridge {

    // @formatter:off
    @Shadow public abstract boolean shadow$isClientSide();
    // @formatter:on

    // TODO: this needs to be updated properly/reviewed
    // lambdas in:
    // "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
    @Inject(method = "*(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;Ljava/util/List;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                     remap = false),
            cancellable = true)
    private void collisionsImpl$checkForCollisionRulesA(final Entity entity, final Predicate<? super Entity> filter, final List<Entity> entities, final Entity candidate, final CallbackInfo ci) {
        this.collisionsImpl$commonCollisionCheck(entity, entities, ci);
    }

    // "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
    @Inject(method = "*(Ljava/util/function/Predicate;Ljava/util/List;Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                     remap = false),
            cancellable = true)
    private void collisionsImpl$checkForCollisionRulesA(final Predicate<? super Entity> filter, final List<Entity> entities, final EntityTypeTest<?, ?> entity, final Entity candidate, final CallbackInfo ci) {
        this.collisionsImpl$commonCollisionCheck(entity, entities, ci);
    }

    private void collisionsImpl$commonCollisionCheck(
        final @Nullable @Coerce Object entity,
        final List<Entity> entities,
        final CallbackInfo ci
    ) {
        // ignore players and entities with parts (ex. EnderDragon)
        if (this.shadow$isClientSide() || entities == null) {
            return;
        }
        if (entity instanceof EntityTypeTest<?, ?> test && (Player.class.isAssignableFrom(
            test.getBaseClass()) || ItemEntity.class == test.getBaseClass())) {
            return;
        }
        if (entity instanceof Player || entity instanceof EnderDragon) {
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

    private <T extends Entity> boolean entityCollision$allowEntityCollision(final List<T> entities) {
        if (this.bridge$isFake()) {
            return true;
        }

        final PhaseContext<@NonNull ?> phaseContext = PhaseTracker.getInstance().getPhaseContext();
        if (!phaseContext.allowsEntityCollisionEvents()) {
            // allow explosions
            return true;
        }

        final Object source = phaseContext.getSource();
        if (source == null) {
            return true;
        }

        CollisionCapabilityBridge collisionBridge = null;

        if (source instanceof LocatableBlock) {
            final LocatableBlock locatable = (LocatableBlock) source;
            final BlockType blockType = locatable.location().block().type();
            collisionBridge = (CollisionCapabilityBridge) blockType;
        } else if (source instanceof CollisionCapabilityBridge) {
            collisionBridge = (CollisionCapabilityBridge) source;
        }

        if (collisionBridge == null) {
            return true;
        }

        if (collisionBridge.collision$requiresCollisionsCacheRefresh()) {
            collisionBridge.collision$initializeCollisionState((Level) (Object) this);
            collisionBridge.collision$requiresCollisionsCacheRefresh(false);
        }

        return !((collisionBridge.collision$getMaxCollisions() >= 0) && (entities.size() >= collisionBridge.collision$getMaxCollisions()));
    }
}
