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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.ExplosionBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.CaptureBlockPos;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.Explosion.class)
public abstract class ExplosionMixin implements ExplosionBridge {

    private boolean impl$shouldBreakBlocks;
    private boolean impl$shouldDamageEntities;
//    private Cause createdCause;

    @Shadow @Final private List<BlockPos> affectedBlockPositions;
    @Shadow @Final private Map<PlayerEntity, Vec3d> playerKnockbackMap;
    @Shadow @Final private Random random;
    @Shadow @Final private boolean causesFire;
    @Shadow @Final private boolean damagesTerrain;
    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private Entity exploder;
    @Shadow @Final private float size;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(final net.minecraft.world.World world, final Entity entity, final double originX, final double originY,
            final double originZ, final float radius, final boolean isFlaming, final boolean isSmoking,
            final CallbackInfo ci) {
        // In Vanilla and Forge, 'damagesTerrain' controls both smoke particles and block damage
        // Sponge-created explosions will explicitly set 'impl$shouldBreakBlocks' to its proper value
        this.impl$shouldBreakBlocks = this.damagesTerrain;
        this.impl$shouldDamageEntities = true;
    }

    /**
     * @author gabizou - September 8th, 2016
     * @reason Rewrites to use our own hooks that will patch with forge perfectly well,
     * and allows for maximal capability.
     */
    @Final
    @Overwrite
    public void doExplosionA() {
        // Sponge Start - If the explosion should not break blocks, don't bother calculating it
        if (this.impl$shouldBreakBlocks) {
            // Sponge End
            final Set<BlockPos> set = Sets.<BlockPos>newHashSet();
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
                            float f = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
                            double d4 = this.x;
                            double d6 = this.y;
                            double d8 = this.z;

                            for (final float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                final BlockPos blockpos = new BlockPos(d4, d6, d8);
                                final BlockState iblockstate = this.world.getBlockState(blockpos);

                                if (iblockstate.getMaterial() != Material.AIR) {
                                    final float f2 = this.exploder != null
                                               ? this.exploder.getExplosionResistance((net.minecraft.world.Explosion) (Object) this
                                            , this.world, blockpos, iblockstate)
                                               : iblockstate.getBlock().getExplosionResistance((Entity) null);
                                    f -= (f2 + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F && (this.exploder == null || this.exploder
                                        .canExplosionDestroyBlock((net.minecraft.world.Explosion) (Object) this, this.world, blockpos, iblockstate, f))) {
                                    set.add(blockpos);
                                }

                                d4 += d0 * 0.30000001192092896D;
                                d6 += d1 * 0.30000001192092896D;
                                d8 += d2 * 0.30000001192092896D;
                            }
                        }
                    }
                }
            }

            this.affectedBlockPositions.addAll(set);
        } // Sponge - Finish if statement
        final float f3 = this.size * 2.0F;
        final int k1 = MathHelper.floor(this.x - (double) f3 - 1.0D);
        final int l1 = MathHelper.floor(this.x + (double) f3 + 1.0D);
        final int i2 = MathHelper.floor(this.y - (double) f3 - 1.0D);
        final int i1 = MathHelper.floor(this.y + (double) f3 + 1.0D);
        final int j2 = MathHelper.floor(this.z - (double) f3 - 1.0D);
        final int j1 = MathHelper.floor(this.z + (double) f3 + 1.0D);

        // Sponge Start - Check if this explosion should damage entities
        final List<Entity> list = this.impl$shouldDamageEntities
                            ? this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1))
                            : Collections.emptyList();
        // Now we can throw our Detonate Event
        if (ShouldFire.EXPLOSION_EVENT_DETONATE) {
            final List<Location<World>> blockPositions = new ArrayList<>(this.affectedBlockPositions.size());
            final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(list.size());
            for (final BlockPos pos : this.affectedBlockPositions) {
                blockPositions.add(new Location<>((World) this.world, pos.getX(), pos.getY(), pos.getZ()));
            }
            for (final Entity entity : list) {
                // Make sure to check the entity is immune first.
                if (!entity.isImmuneToExplosions()) {
                    entities.add((org.spongepowered.api.entity.Entity) entity);
                }
            }
            final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
            final ExplosionEvent.Detonate detonate =
                SpongeEventFactory.createExplosionEventDetonate(cause, blockPositions, entities, (Explosion) this, (World) this.world);
            SpongeImpl.postEvent(detonate);
            // Clear the positions so that they can be pulled from the event
            this.affectedBlockPositions.clear();
            if (detonate.isCancelled()) {
                return;
            }
            if (this.impl$shouldBreakBlocks) {
                for (final Location<World> worldLocation : detonate.getAffectedLocations()) {
                    this.affectedBlockPositions.add(VecHelper.toBlockPos(worldLocation));
                }
            }
            // Clear the list of entities so they can be pulled from the event.
            list.clear();
            if (this.impl$shouldDamageEntities) {
                for (final org.spongepowered.api.entity.Entity entity : detonate.getEntities()) {
                    try {
                        list.add((Entity) entity);
                    } catch (final Exception e) {
                        // Do nothing, a plugin tried to use the wrong entity somehow.
                    }
                }
            }
        }
        // Sponge End

        final Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (int k2 = 0; k2 < list.size(); ++k2) {
            final Entity entity = list.get(k2);

            if (!entity.isImmuneToExplosions()) {
                final double d12 = entity.func_70011_f(this.x, this.y, this.z) / (double) f3;

                if (d12 <= 1.0D) {
                    double d5 = entity.posX - this.x;
                    double d7 = entity.posY + (double) entity.getEyeHeight() - this.y;
                    double d9 = entity.posZ - this.z;
                    final double d13 = (double) MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        final double d14 = (double) this.world.func_72842_a(vec3d, entity.getBoundingBox());
                        final double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(
                                DamageSource.causeExplosionDamage((net.minecraft.world.Explosion) (Object) this), (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        double d11 = 1.0D;

                        if (entity instanceof LivingEntity) {
                            d11 = ProtectionEnchantment.getBlastDamageReduction((LivingEntity) entity, d10);
                        }

                        entity.field_70159_w += d5 * d11;
                        entity.field_70181_x += d7 * d11;
                        entity.field_70179_y += d9 * d11;

                        if (entity instanceof PlayerEntity) {
                            final PlayerEntity entityplayer = (PlayerEntity) entity;

                            if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.abilities.isFlying)) {
                                this.playerKnockbackMap.put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author gabizou - March 26th, 2017
     * @reason Since forge will attempt to call the normalized method for modded blocks,
     * we must artificially capture the block position for any block drops or changes during the
     * explosion phase.
     *
     * Does the second part of the explosion (sound, particles, drop spawn)
     */
    @Overwrite
    public void doExplosionB(final boolean spawnParticles) {
        this.world.playSound((PlayerEntity) null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.size >= 2.0F && (this.damagesTerrain || this.impl$shouldBreakBlocks)) {
            // Sponge Start - Use WorldServer methods since we prune the explosion packets
            // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
            // hundreds of explosions at once
            if (this.world instanceof ServerWorld) {
                ((ServerWorld) this.world).func_175739_a(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
            } else {
                // Sponge End
                this.world.func_175688_a(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } // Sponge - brackets.
        } else {
            // Sponge Start - Use WorldServer methods since we prune the explosion packets
            // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
            // hundreds of explosions at once
            if (this.world instanceof ServerWorld) {
                ((ServerWorld) this.world).func_175739_a(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
            } else { // Sponge end
                this.world.func_175688_a(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } // Sponge - brackets.
        }
        // Sponge Start - set up some variables for more fasts
        @Nullable final PhaseContext<?> context = !((WorldBridge) this.world).bridge$isFake() ? PhaseTracker.getInstance().getCurrentContext() : null;
        final boolean hasCapturePos = context != null && context.state.requiresBlockPosTracking();
        // Sponge end

        if (this.impl$shouldBreakBlocks) { // Sponge - use 'impl$shouldBreakBlocks' instead of 'damagesTerrain'
            for (final BlockPos blockpos : this.affectedBlockPositions) {
                final BlockState iblockstate = this.world.getBlockState(blockpos);
                final Block block = iblockstate.getBlock();

                if (spawnParticles) {
                    final double d0 = (double) ((float) blockpos.getX() + this.world.rand.nextFloat());
                    final double d1 = (double) ((float) blockpos.getY() + this.world.rand.nextFloat());
                    final double d2 = (double) ((float) blockpos.getZ() + this.world.rand.nextFloat());
                    double d3 = d0 - this.x;
                    double d4 = d1 - this.y;
                    double d5 = d2 - this.z;
                    final double d6 = (double) MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double) this.size + 0.1D);
                    d7 = d7 * (double) (this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    this.world.func_175688_a(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.x) / 2.0D, (d1 + this.y) / 2.0D, (d2 + this.z) / 2.0D, d3, d4, d5, new int[0]);
                    this.world.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
                }

                if (iblockstate.getMaterial() != Material.AIR) {
                    if (block.canDropFromExplosion((net.minecraft.world.Explosion) (Object) this)) {
                        // Sponge Start - Track the block position being destroyed
                        // We need to capture this block position if necessary
                        try (final CaptureBlockPos pos = hasCapturePos ? context.getCaptureBlockPos() : null) {
                            if (pos != null) {
                                pos.setPos(blockpos);
                            }
                            // Sponge End
                            block.func_180653_a(this.world, blockpos, this.world.getBlockState(blockpos), 1.0F / this.size, 0);
                        } // Sponge - brackets
                    }

                    // Sponge Start - Track the block position being destroyed
                    // We need to capture this block position if necessary
                    try (final CaptureBlockPos pos = hasCapturePos ? context.getCaptureBlockPos() : null) {
                        if (pos != null) {
                            pos.setPos(blockpos);
                        }
                        // this.world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3); // Vanilla
                        // block.onExplosionDestroy(this.world, blockpos, this); // Vanilla
                        // block.onBlockExploded(this.world, blockpos, this); // Forge
                        // Sponge - Use our universal hook.
                        SpongeImplHooks.blockExploded(block, this.world, blockpos, (net.minecraft.world.Explosion) (Object) this);
                    }
                    // Sponge End
                }
            }
        }

        if (this.causesFire) {
            for (final BlockPos blockpos1 : this.affectedBlockPositions) {
                if (this.world.getBlockState(blockpos1).getMaterial() == Material.AIR && this.world.getBlockState(blockpos1.down()).func_185913_b() && this.random.nextInt(3) == 0) {
                    // Sponge Start - Track the block position being destroyed
                    try (final CaptureBlockPos pos = hasCapturePos ? context.getCaptureBlockPos() : null) {
                        if (pos != null) {
                            pos.setPos(blockpos1);
                        }
                        // Sponge End
                        this.world.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
                    } // Sponge - brackets
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

    @Nullable
    @Override
    public Entity bridge$getExploder() {
        return this.exploder;
    }

    @Override
    public net.minecraft.world.World bridge$getWorld() {
        return this.world;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("causesFire", this.causesFire)
            .add("damagesTerrain", this.damagesTerrain)
            .add("world", this.world.getWorldInfo() == null ? "null" : this.world.getWorldInfo().getWorldName())
            .add("x", this.x)
            .add("y", this.y)
            .add("z", this.z)
            .add("exploder", this.exploder)
            .add("size", this.size)
            .toString();
    }
}
