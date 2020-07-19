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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.TrackableBridge;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin_Tracker implements TrackableBridge {

    // @formatter:off
    @Shadow @Final private EntityType<?> type;
    @Shadow public World world;
    @Shadow public boolean removed;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow @Final protected Random rand;

    @Shadow public abstract void shadow$extinguish();
    @Shadow protected abstract void shadow$setFlag(int flag, boolean set);
    @Shadow @Nullable public abstract Team getTeam();
    @Shadow public abstract void shadow$setPosition(double x, double y, double z);
    @Shadow public abstract void shadow$setMotion(Vec3d motionIn);
    @Shadow public abstract void shadow$setMotion(double x, double y, double z);
    @Shadow public abstract float getEyeHeight();
    @Shadow public abstract UUID getUniqueID();

    private boolean tracker$trackedInWorld = false;
    @Nullable private Cause tracker$destructCause;
    //@formatter:on


//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void tracker$refreshTrackerStates(final EntityType<?> entityType, final net.minecraft.world.World world, final CallbackInfo ci) {
//        this.bridge$refreshTrackerStates();
//
//        final EntityTypeBridge entityTypeBridge = (EntityTypeBridge) entityType;
//        if (!entityTypeBridge.bridge$checkedDamageEntity()) {
//            try {
//                final String mapping = Launcher.getInstance().isDeveloperEnvironment() ? Constants.Entity.ATTACK_ENTITY_FROM_MAPPING : Constants.Entity.ATTACK_ENTITY_FROM_OBFUSCATED;
//                final Class<?>[] argTypes = {DamageSource.class, float.class};
//                final Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
//                if (!(clazz.equals(LivingEntity.class) || clazz.equals(PlayerEntity.class) || clazz.equals(ServerPlayerEntity.class))) {
//                    entityTypeBridge.bridge$setOverridesDamageEntity(true);
//                }
//            } catch (final Throwable ex) {
//                // In some rare cases, we just want to ignore class errors or
//                // reflection errors and can "Safely" ignore our tracking because the alternative
//                // is to silently ignore the mod's custom handling if it's there.
//                entityTypeBridge.bridge$setOverridesDamageEntity(true);
//            } finally {
//                entityTypeBridge.bridge$setCheckedDamageEntity(true);
//            }
//        }
//    }


    @Override
    public boolean bridge$isWorldTracked() {
        return this.tracker$trackedInWorld;
    }

    @Override
    public void bridge$setWorldTracked(final boolean tracked) {
        this.tracker$trackedInWorld = tracked;
        // Since this is called during removeEntity from world, we can
        // post the removal event here, basically.
        if (!tracked && this.tracker$destructCause != null) {
            final Audience originalChannel = Audience.empty();
            SpongeCommon.postEvent(SpongeEventFactory.createDestructEntityEvent(
                this.tracker$destructCause,
                originalChannel,
                Optional.of(originalChannel),
                TextComponent.empty(),
                TextComponent.empty(),
                (org.spongepowered.api.entity.Entity) this,
                false
            ));

            this.tracker$destructCause = null;
        }
    }

    @Override
    public boolean bridge$shouldTick() {
//        final ChunkBridge chunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
//        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
//        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
//        return chunk == null || chunk.bridge$isActive();
        return true;
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return ((TrackableBridge) this.type).bridge$allowsBlockBulkCaptures();
    }

    @Override
    public void bridge$setAllowsBlockBulkCaptures(final boolean allowsBlockBulkCaptures) {
        ((TrackableBridge) this.type).bridge$setAllowsBlockBulkCaptures(allowsBlockBulkCaptures);
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return ((TrackableBridge) this.type).bridge$allowsBlockEventCreation();
    }

    @Override
    public void bridge$setAllowsBlockEventCreation(final boolean allowsBlockEventCreation) {
        ((TrackableBridge) this.type).bridge$setAllowsBlockEventCreation(allowsBlockEventCreation);
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return ((TrackableBridge) this.type).bridge$allowsEntityBulkCaptures();
    }

    @Override
    public void bridge$setAllowsEntityBulkCaptures(final boolean allowsEntityBulkCaptures) {
        ((TrackableBridge) this.type).bridge$setAllowsEntityBulkCaptures(allowsEntityBulkCaptures);
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return ((TrackableBridge) this.type).bridge$allowsEntityEventCreation();
    }

    @Override
    public void bridge$setAllowsEntityEventCreation(final boolean allowsEntityEventCreation) {
        ((TrackableBridge) this.type).bridge$setAllowsEntityEventCreation(allowsEntityEventCreation);
    }

    @Override
    public void bridge$refreshTrackerStates() {
        ((TrackableBridge) this.type).bridge$refreshTrackerStates();
    }
}
