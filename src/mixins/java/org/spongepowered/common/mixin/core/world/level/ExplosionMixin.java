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
package org.spongepowered.common.mixin.core.world.level;

import com.google.common.collect.Sets;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.ExplosionBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(net.minecraft.world.level.Explosion.class)
public abstract class ExplosionMixin implements ExplosionBridge {

    // @formatter:off
    @Shadow @Final private ExplosionDamageCalculator damageCalculator;
    @Shadow @Final private List<BlockPos> toBlow;
    @Shadow @Final private Map<Player, Vec3> hitPlayers;
    @Shadow @Final private boolean fire;
    @Shadow @Final private net.minecraft.world.level.Level level;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private Entity source;
    @Shadow @Final private float radius;
    @Shadow @Final private net.minecraft.world.level.Explosion.BlockInteraction blockInteraction;

    @Shadow public abstract DamageSource shadow$getDamageSource();
    // @formatter:on

    private boolean impl$shouldBreakBlocks;
    private boolean impl$shouldDamageEntities;
    private int impl$resolution;
    private float impl$randomness;
    private double impl$knockback;

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)V",
        at = @At("RETURN")
    )
    private void impl$onConstructed(final Level worldIn, final Entity exploderIn, final double xIn, final double yIn, final double zIn, final float sizeIn, final boolean causesFireIn,
            final net.minecraft.world.level.Explosion.BlockInteraction modeIn, final CallbackInfo ci) {
        // In Vanilla and Forge, 'damagesTerrain' controls both smoke particles and block damage
        // Sponge-created explosions will explicitly set 'impl$shouldBreakBlocks' to its proper value
        this.impl$shouldBreakBlocks = this.blockInteraction == net.minecraft.world.level.Explosion.BlockInteraction.BREAK || this.blockInteraction == net.minecraft.world.level.Explosion.BlockInteraction.DESTROY;
        this.impl$shouldDamageEntities = true;
        this.impl$resolution = 16;
        this.impl$randomness = 1.0F;
        this.impl$knockback = 1.0;
    }

    // (Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;Lnet/minecraft/world/ExplosionContext;DDDFZLnet/minecraft/world/Explosion$Mode;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V
    @Surrogate
    private void impl$onConstructed(final Level worldIn, final Entity exploderIn, final DamageSource damageSourceIn, final ExplosionDamageCalculator explosionContextIn, final double xIn, final double yIn, final double zIn, final float sizeIn, final boolean causesFireIn, final net.minecraft.world.level.Explosion.BlockInteraction modeIn, final CallbackInfo ci) {
        // In Vanilla and Forge, 'damagesTerrain' controls both smoke particles and block damage
        // Sponge-created explosions will explicitly set 'impl$shouldBreakBlocks' to its proper value
        this.impl$shouldBreakBlocks = this.blockInteraction == net.minecraft.world.level.Explosion.BlockInteraction.BREAK || this.blockInteraction == net.minecraft.world.level.Explosion.BlockInteraction.DESTROY;
        this.impl$shouldDamageEntities = true;
        this.impl$resolution = 16;
        this.impl$randomness = 1.0F;
        this.impl$knockback = 1.0;
    }

    /**
     * @author gabizou
     * @author zidane
     * @reason Fire ExplosionEvent.Detonate
     */
    @Overwrite
    public void explode() {

        // Sponge Start - Do not run calculation logic on client thread
        if (this.level.isClientSide) {
            return;
        }
        // Sponge End

        // Sponge Start - If the explosion should not break blocks, don't bother calculating it on server thread
        if (this.impl$shouldBreakBlocks) {
            final Set<BlockPos> set = Sets.newHashSet();
            final int i = 16;

            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int l = 0; l < 16; ++l) {
                        if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                            double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                            double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                            double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                            final double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 = d0 / d3;
                            d1 = d1 / d3;
                            d2 = d2 / d3;
                            float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                            double d4 = this.x;
                            double d6 = this.y;
                            double d8 = this.z;

                            for (final float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                final BlockPos blockpos = new BlockPos(d4, d6, d8);
                                final BlockState blockstate = this.level.getBlockState(blockpos);
                                final FluidState fluidstate = this.level.getFluidState(blockpos);
                                Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance((net.minecraft.world.level.Explosion) (Object) this, this.level, blockpos, blockstate, fluidstate);
                                if (optional.isPresent()) {
                                    f -= (optional.get() + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F && this.damageCalculator.shouldBlockExplode((net.minecraft.world.level.Explosion) (Object) this, this.level, blockpos, blockstate, f)) {
                                    set.add(blockpos);
                                }

                                d4 += d0 * (double) 0.3F;
                                d6 += d1 * (double) 0.3F;
                                d8 += d2 * (double) 0.3F;
                            }
                        }
                    }
                }
            }

            this.toBlow.addAll(set);
        }
        // Sponge End

        final float f3 = this.radius * 2.0F;
        final int k1 = Mth.floor(this.x - (double) f3 - 1.0D);
        final int l1 = Mth.floor(this.x + (double) f3 + 1.0D);
        final int i2 = Mth.floor(this.y - (double) f3 - 1.0D);
        final int i1 = Mth.floor(this.y + (double) f3 + 1.0D);
        final int j2 = Mth.floor(this.z - (double) f3 - 1.0D);
        final int j1 = Mth.floor(this.z + (double) f3 + 1.0D);

        // Sponge Start - Only query for entities if we're to damage them
        final List<Entity> list = this.impl$shouldDamageEntities ? this.level.getEntities(this.source,
                new AABB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1)) : Collections.emptyList();
        // Sponge End

        if (ShouldFire.EXPLOSION_EVENT_DETONATE) {
            final List<ServerLocation> blockPositions = new ArrayList<>(this.toBlow.size());
            final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(list.size());
            for (final BlockPos pos : this.toBlow) {
                blockPositions
                        .add(ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) this.level, pos.getX(), pos.getY(), pos.getZ()));
            }
            for (final Entity entity : list) {
                // Make sure to check the entity is immune first.
                if (!entity.ignoreExplosion()) {
                    entities.add((org.spongepowered.api.entity.Entity) entity);
                }
            }
            final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
            final ExplosionEvent.Detonate detonate = SpongeEventFactory.createExplosionEventDetonate(cause, blockPositions, entities,
                    (Explosion) this, (org.spongepowered.api.world.server.ServerWorld) this.level);
            SpongeCommon.post(detonate);
            // Clear the positions so that they can be pulled from the event
            this.toBlow.clear();
            if (detonate.isCancelled()) {
                return;
            }
            if (this.impl$shouldBreakBlocks) {
                for (final ServerLocation worldLocation : detonate.affectedLocations()) {
                    this.toBlow.add(VecHelper.toBlockPos(worldLocation));
                }
            }
            // Clear the list of entities so they can be pulled from the event.
            list.clear();
            if (this.impl$shouldDamageEntities) {
                for (final org.spongepowered.api.entity.Entity entity : detonate.entities()) {
                    try {
                        list.add((Entity) entity);
                    } catch (final Exception e) {
                        // Do nothing, a plugin tried to use the wrong entity somehow.
                    }
                }
            }
        }
        // Sponge End

        final Vec3 vec3d = new Vec3(this.x, this.y, this.z);

        for (int k2 = 0; k2 < list.size(); ++k2) {
            final Entity entity = list.get(k2);
            if (!entity.ignoreExplosion()) {
                final double d12 = (double)(Mth.sqrt(entity.distanceToSqr(vec3d)) / f3);
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.x;
                    double d7 = entity.getEyeY() - this.y;
                    double d9 = entity.getZ() - this.z;
                    final double d13 = (double)Mth.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        final double d14 = (double) net.minecraft.world.level.Explosion.getSeenPercent(vec3d, entity);
                        final double d10 = (1.0D - d12) * d14;
                        entity.hurt(this.shadow$getDamageSource(), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f3 + 1.0D)));
                        double d11 = d10;
                        if (entity instanceof LivingEntity) {
                            d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity)entity, d10);
                        }

                        // Sponge Start - Honor our knockback value from event
                        entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11 * this.impl$knockback, d7 * d11 * this.impl$knockback, d9 * d11 * this.impl$knockback));
                        if (entity instanceof Player) {
                            final Player playerentity = (Player)entity;
                            if (!playerentity.isSpectator() && (!playerentity.isCreative() || !playerentity.abilities.flying)) {
                                this.hitPlayers.put(playerentity, new Vec3(d5 * d10 * this.impl$knockback,
                                        d7 * d10 * this.impl$knockback, d9 * d10 * this.impl$knockback));
                            }
                        }
                        // Sponge End
                    }
                }
            }
        }
    }

    @Override
    public boolean bridge$getShouldDamageBlocks() {
        return this.impl$shouldBreakBlocks;
    }

    @Override
    public boolean bridge$getShouldDamageEntities() {
        return this.impl$shouldDamageEntities;
    }

    @Override
    public void bridge$setShouldBreakBlocks(final boolean shouldBreakBlocks) {
        this.impl$shouldBreakBlocks = shouldBreakBlocks;
    }

    @Override
    public void bridge$setShouldDamageEntities(final boolean shouldDamageEntities) {
        this.impl$shouldDamageEntities = shouldDamageEntities;
    }

    @Override
    public void bridge$setResolution(final int resolution) {
        this.impl$resolution = resolution;
    }

    @Override
    public int bridge$getResolution() {
        return this.impl$resolution;
    }

    @Override
    public void bridge$setRandomness(final float randomness) {
        this.impl$randomness = randomness;
    }

    @Override
    public float bridge$getRandomness() {
        return this.impl$randomness;
    }

    @Override
    public void bridge$setKnockback(final double knockback) {
        this.impl$knockback = knockback;
    }

    @Override
    public double bridge$getKnockback() {
        return this.impl$knockback;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ExplosionMixin.class.getSimpleName() + "[", "]")
            .add("causesFire=" + this.fire)
            .add("mode=" + this.blockInteraction)
            .add("world=" + this.level)
            .add("x=" + this.x)
            .add("y=" + this.y)
            .add("z=" + this.z)
            .add("exploder=" + this.source)
            .add("size=" + this.radius)
            .add("affectedBlockPositions=" + this.toBlow)
            .add("playerKnockbackMap=" + this.hitPlayers)
            .add("shouldBreakBlocks=" + this.impl$shouldBreakBlocks)
            .add("shouldDamageEntities=" + this.impl$shouldDamageEntities)
            .add("resolution=" + this.impl$resolution)
            .add("randomness=" + this.impl$randomness)
            .add("knockback=" + this.impl$knockback)
            .toString();
    }
}
