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
package org.spongepowered.common.mixin.tracker.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.entity.EntityTypeBridge;
import org.spongepowered.common.util.Constants;

@Mixin(Entity.class)
public abstract class EntityMixin_Tracker implements TrackableBridge {

    // @formatter:off
    @Shadow @Final private EntityType<?> type;
    // @formatter:on

    @Shadow public World world;

    @Shadow public boolean removed;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$refreshTrackerStates(final EntityType<?> entityType, final net.minecraft.world.World world, final CallbackInfo ci) {
        this.bridge$refreshTrackerStates();

        final EntityTypeBridge entityTypeBridge = (EntityTypeBridge) entityType;
        if (!entityTypeBridge.bridge$checkedDamageEntity()) {
            try {
                final String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? Constants.Entity.ATTACK_ENTITY_FROM_MAPPING : Constants.Entity.ATTACK_ENTITY_FROM_OBFUSCATED;
                final Class<?>[] argTypes = {DamageSource.class, float.class};
                final Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
                if (!(clazz.equals(LivingEntity.class) || clazz.equals(PlayerEntity.class) || clazz.equals(ServerPlayerEntity.class))) {
                    entityTypeBridge.bridge$setOverridesDamageEntity(true);
                }
            } catch (final Throwable ex) {
                // In some rare cases, we just want to ignore class errors or
                // reflection errors and can "Safely" ignore our tracking because the alternative
                // is to silently ignore the mod's custom handling if it's there.
                entityTypeBridge.bridge$setOverridesDamageEntity(true);
            } finally {
                entityTypeBridge.bridge$setCheckedDamageEntity(true);
            }
        }
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return ((TrackableBridge) this.type).bridge$allowsBlockBulkCaptures();
    }

    @Override
    public void bridge$setAllowsBlockBulkCaptures(boolean allowsBlockBulkCaptures) {
        ((TrackableBridge) this.type).bridge$setAllowsBlockBulkCaptures(allowsBlockBulkCaptures);
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return ((TrackableBridge) this.type).bridge$allowsBlockEventCreation();
    }

    @Override
    public void bridge$setAllowsBlockEventCreation(boolean allowsBlockEventCreation) {
        ((TrackableBridge) this.type).bridge$setAllowsBlockEventCreation(allowsBlockEventCreation);
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return ((TrackableBridge) this.type).bridge$allowsEntityBulkCaptures();
    }

    @Override
    public void bridge$setAllowsEntityBulkCaptures(boolean allowsEntityBulkCaptures) {
        ((TrackableBridge) this.type).bridge$setAllowsEntityBulkCaptures(allowsEntityBulkCaptures);
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return ((TrackableBridge) this.type).bridge$allowsEntityEventCreation();
    }

    @Override
    public void bridge$setAllowsEntityEventCreation(boolean allowsEntityEventCreation) {
        ((TrackableBridge) this.type).bridge$setAllowsEntityEventCreation(allowsEntityEventCreation);
    }

    @Override
    public void bridge$refreshTrackerStates() {
        ((TrackableBridge) this.type).bridge$refreshTrackerStates();
    }
}
