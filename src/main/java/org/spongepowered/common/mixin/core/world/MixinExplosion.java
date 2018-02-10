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
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.BlockChangeFlags;
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
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.world.IMixinExplosion;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.Explosion.class)
public abstract class MixinExplosion implements Explosion, IMixinExplosion {

    public Vector3d origin;
    public Vec3d position; // Added for Forge
    private boolean shouldBreakBlocks;
    private boolean shouldDamageEntities;
//    private Cause createdCause;

    @Shadow @Final private List<BlockPos> affectedBlockPositions;
    @Shadow @Final private Map<EntityPlayer, Vec3d> playerKnockbackMap;
    @Shadow @Final private Random random;
    @Shadow public boolean causesFire;
    @Shadow public boolean damagesTerrain;
    @Shadow public net.minecraft.world.World world;
    @Shadow public double x;
    @Shadow public double y;
    @Shadow public double z;
    @Shadow public Entity exploder;
    @Shadow public float size;

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, Entity entity, double originX, double originY,
            double originZ, float radius, boolean isFlaming, boolean isSmoking,
            CallbackInfo ci) {
        this.origin = new Vector3d(this.x, this.y, this.z);
        // In Vanilla and Forge, 'damagesTerrain' controls both smoke particles and block damage
        // Sponge-created explosions will explicitly set 'shouldBreakBlocks' to its proper value
        this.shouldBreakBlocks = this.damagesTerrain;
        this.shouldDamageEntities = true;
    }


    // TODO fix this whereever it was called from?
//    @Override
//    public Cause createCause() {
//        if (this.createdCause != null) {
//            return this.createdCause;
//        }
//        Object source;
//        Object projectileSource = null;
//        Object igniter = null;
//        if (this.exploder == null) {
//            source = getWorld().getBlock(getLocation().getPosition().toInt());
//        } else {
//            source = this.exploder;
//            if (source instanceof Projectile) {
//                projectileSource = ((Projectile) this.exploder).getShooter();
//            }
//
//            // Don't use the exploder itself as igniter
//            igniter = getExplosivePlacedBy();
//            if (this.exploder == igniter) {
//                igniter = null;
//            }
//        }
//
//        final Cause.Builder builder = Cause.source(source);
//        if (projectileSource != null) {
//            if (igniter != null) {
//                builder.named(NamedCause.of("ProjectileSource", projectileSource)).named(NamedCause.of("Igniter", igniter));
//            } else {
//                builder.named(NamedCause.of("ProjectileSource", projectileSource));
//            }
//        } else if (igniter != null) {
//            builder.named(NamedCause.of("Igniter", igniter));
//        }
//        if (PhaseTracker.ENABLED) {
//            final PhaseData phaseData = PhaseTracker.getInstance().getCurrentPhaseData();
//            phaseData.state.getPhase().appendExplosionCause(phaseData);
//        }
//        return this.createdCause = builder.build();
//    }
//
//    @Override
//    public Cause getCreatedCause() {
//        if (this.createdCause == null) {
//            createCause();
//        }
//        return this.createdCause;
//    }

    /**
     * @author gabizou - September 8th, 2016
     * @reason Rewrites to use our own hooks that will patch with forge perfectly well,
     * and allows for maximal capability.
     */
    @Final
    @Overwrite
    public void doExplosionA() {
        // Sponge Start - If the explosion should not break blocks, don't bother calculating it
        if (this.shouldBreakBlocks) {
            // Sponge End
            Set<BlockPos> set = Sets.<BlockPos>newHashSet();
            int i = 16;

            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int l = 0; l < 16; ++l) {
                        if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                            double d0 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                            double d1 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                            double d2 = (double) ((float) l / 15.0F * 2.0F - 1.0F);
                            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            d0 = d0 / d3;
                            d1 = d1 / d3;
                            d2 = d2 / d3;
                            float f = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
                            double d4 = this.x;
                            double d6 = this.y;
                            double d8 = this.z;

                            for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                                BlockPos blockpos = new BlockPos(d4, d6, d8);
                                IBlockState iblockstate = this.world.getBlockState(blockpos);

                                if (iblockstate.getMaterial() != Material.AIR) {
                                    float f2 = this.exploder != null
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
        float f3 = this.size * 2.0F;
        int k1 = MathHelper.floor(this.x - (double) f3 - 1.0D);
        int l1 = MathHelper.floor(this.x + (double) f3 + 1.0D);
        int i2 = MathHelper.floor(this.y - (double) f3 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double) f3 + 1.0D);
        int j2 = MathHelper.floor(this.z - (double) f3 - 1.0D);
        int j1 = MathHelper.floor(this.z + (double) f3 + 1.0D);

        // Sponge Start - Check if this explosion should damage entities
        List<Entity> list = this.shouldDamageEntities
                            ? this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1))
                            : Collections.emptyList();
        // Now we can throw our Detonate Event
        final List<Location<World>> blockPositions = new ArrayList<>(this.affectedBlockPositions.size());
        final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(list.size());
        for (BlockPos pos : this.affectedBlockPositions) {
            blockPositions.add(new Location<>((World) this.world, pos.getX(), pos.getY(), pos.getZ()));
        }
        for (Entity entity : list) {
            entities.add((org.spongepowered.api.entity.Entity) entity);
        }
        ExplosionEvent.Detonate detonate = SpongeEventFactory.createExplosionEventDetonate(Sponge.getCauseStackManager().getCurrentCause(), blockPositions, entities, this, (World) this.world);
        SpongeImpl.postEvent(detonate);
        if (detonate.isCancelled()) {
            this.affectedBlockPositions.clear();
            return;
        }
        this.affectedBlockPositions.clear();
        if (this.shouldBreakBlocks) {
            for (Location<World> worldLocation : detonate.getAffectedLocations()) {
                this.affectedBlockPositions.add(((IMixinLocation) (Object) worldLocation).getBlockPos());
            }
        }
        list.clear();

        if (this.shouldDamageEntities) {
            for (org.spongepowered.api.entity.Entity entity : detonate.getEntities()) {
                try {
                    list.add(EntityUtil.toNative(entity));
                } catch (Exception e) {
                    // Do nothing, a plugin tried to use the wrong entity somehow.
                }
            }
        }
        // Sponge End

        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for (int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);

            if (!entity.isImmuneToExplosions()) {
                double d12 = entity.getDistance(this.x, this.y, this.z) / (double) f3;

                if (d12 <= 1.0D) {
                    double d5 = entity.posX - this.x;
                    double d7 = entity.posY + (double) entity.getEyeHeight() - this.y;
                    double d9 = entity.posZ - this.z;
                    double d13 = (double) MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = (double) this.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(
                                DamageSource.causeExplosionDamage((net.minecraft.world.Explosion) (Object) this), (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        double d11 = 1.0D;

                        if (entity instanceof EntityLivingBase) {
                            d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, d10);
                        }

                        entity.motionX += d5 * d11;
                        entity.motionY += d7 * d11;
                        entity.motionZ += d9 * d11;

                        if (entity instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer) entity;

                            if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying)) {
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
    public void doExplosionB(boolean spawnParticles) {
        this.world.playSound((EntityPlayer) null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE,
            SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.size >= 2.0F) {
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.EXPLOSION_HUGE,
                    this.x, this.y, this.z,
                    1,
                    0, 0, 0,
                    0.1D);
            } else {
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE,
                    this.x, this.y, this.z,
                    1.0D, 0.0D, 0.0D);
            }
        } else {
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
                    this.x, this.y, this.z,
                    1,
                    0, 0, 0,
                    0.1D);
            } else {
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE,
                    this.x, this.y, this.z,
                    1.0D, 0.0D, 0.0D);
            }
        }

        if (this.shouldBreakBlocks) { // Sponge - use 'shouldBreakBlocks' instead of 'damagesTerrain'
            for (BlockPos blockpos : this.affectedBlockPositions) {
                IBlockState iblockstate = this.world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (spawnParticles) {
                    double d0 = (double) ((float) blockpos.getX() + this.world.rand.nextFloat());
                    double d1 = (double) ((float) blockpos.getY() + this.world.rand.nextFloat());
                    double d2 = (double) ((float) blockpos.getZ() + this.world.rand.nextFloat());
                    double d3 = d0 - this.x;
                    double d4 = d1 - this.y;
                    double d5 = d2 - this.z;
                    double d6 = (double) MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double) this.size + 0.1D);
                    d7 = d7 * (double) (this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.x) / 2.0D, (d1 + this.y) / 2.0D,
                        (d2 + this.z) / 2.0D, d3, d4, d5, new int[0]);
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
                }

                if (iblockstate.getMaterial() != Material.AIR) {
                    if (block.canDropFromExplosion((net.minecraft.world.Explosion) (Object) this)) {
                        // Sponge Start - Track the block position being destroyed
                        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
                        final PhaseData peek = phaseTracker.getCurrentPhaseData();
                        // We need to capture this block position if necessary
                        if (peek.state.requiresBlockPosTracking()) {
                            peek.context.getCaptureBlockPos().setPos(blockpos);
                        }
                        block.dropBlockAsItemWithChance(this.world, blockpos, this.world.getBlockState(blockpos), 1.0F / this.size, 0);
                        // And then un-set the captured block position
                        if (peek.state.requiresBlockPosTracking()) {
                            peek.context.getCaptureBlockPos().setPos(null);
                        }
                    }

                    // Sponge Start - Track the block position being destroyed
                    final PhaseTracker phaseTracker = PhaseTracker.getInstance();
                    final PhaseData peek = phaseTracker.getCurrentPhaseData();
                    // We need to capture this block position if necessary
                    if (peek.state.requiresBlockPosTracking()) {
                        peek.context.getCaptureBlockPos().setPos(blockpos);
                    }
                    // Because we need to hook into forge.
                    SpongeImplHooks.blockExploded(block, this.world, blockpos, (net.minecraft.world.Explosion) (Object) this);
                    // And then un-set the captured block position
                    if (peek.state.requiresBlockPosTracking()) {
                        peek.context.getCaptureBlockPos().setPos(null);
                    }
                    // Sponge End
                }
            }
        }

        if (this.causesFire) {
            for (BlockPos blockpos1 : this.affectedBlockPositions) {
                if (this.world.getBlockState(blockpos1).getMaterial() == Material.AIR && this.world.getBlockState(blockpos1.down()).isFullBlock()
                    && this.random.nextInt(3) == 0) {
                    // Sponge Start - Track the block position being destroyed
                    final PhaseTracker phaseTracker = PhaseTracker.getInstance();
                    final PhaseData peek = phaseTracker.getCurrentPhaseData();
                    // We need to capture this block position if necessary
                    if (peek.state.requiresBlockPosTracking()) {
                        peek.context.getCaptureBlockPos().setPos(blockpos1);
                    }
                    phaseTracker.setBlockState((IMixinWorldServer) this.world, blockpos1, Blocks.FIRE.getDefaultState(), BlockChangeFlags.ALL);
                    // And then un-set the captured block position
                    if (peek.state.requiresBlockPosTracking()) {
                        peek.context.getCaptureBlockPos().setPos(null);
                    }
                }
            }
        }
    }


    @Nullable
    private Location<World> location;

    @Override
    public Location<World> getLocation() {
        if (this.location == null) {
            this.location = new Location<World>((World) this.world, this.origin);
        }
        return this.location;
    }

    @Override
    public Optional<Explosive> getSourceExplosive() {
        if (this.exploder instanceof Explosive) {
            return Optional.of((Explosive) this.exploder);
        }

        return Optional.empty();
    }

    @Override
    public float getRadius() {
        return this.size;
    }

    @Override
    public boolean canCauseFire() {
        return this.causesFire;
    }

    @Override
    public boolean shouldPlaySmoke() {
        return this.damagesTerrain;
    }

    @Override
    public boolean shouldBreakBlocks() {
        return this.shouldBreakBlocks;
    }

    @Override
    public boolean shouldDamageEntities() {
        return this.shouldDamageEntities;
    }

    @Override
    public void setShouldBreakBlocks(boolean shouldBreakBlocks) {
        this.shouldBreakBlocks = shouldBreakBlocks;
    }

    @Override
    public void setShouldDamageEntities(boolean shouldDamageEntities) {
        this.shouldDamageEntities = shouldDamageEntities;
    }
}
