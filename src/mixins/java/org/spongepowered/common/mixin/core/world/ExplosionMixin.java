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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.ExplosionBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Mixin(net.minecraft.world.Explosion.class)
public abstract class ExplosionMixin implements ExplosionBridge {

    @Shadow @Final private List<BlockPos> affectedBlockPositions;
    @Shadow @Final private Map<PlayerEntity, Vec3d> playerKnockbackMap;
    @Shadow @Final private Random random;
    @Shadow @Final private boolean causesFire;
    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private Entity exploder;
    @Shadow @Final private float size;
    @Shadow @Final private net.minecraft.world.Explosion.Mode mode;

    @Shadow public static float shadow$getBlockDensity(Vec3d p_222259_0_, Entity p_222259_1_) {
        throw new UnsupportedOperationException("shadows");
    }
    @Shadow public abstract DamageSource shadow$getDamageSource();
    @Shadow private static void shadow$func_229976_a_(ObjectArrayList<Pair<ItemStack, BlockPos>> p_229976_0_, ItemStack p_229976_1_,
            BlockPos p_229976_2_) {
        throw new UnsupportedOperationException("shadows");
    }

    private boolean impl$shouldBreakBlocks;
    private boolean impl$shouldDamageEntities;
    private int impl$resolution;
    private float impl$randomness;
    private double impl$knockback;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/Explosion$Mode;)V", at = @At("RETURN"))
    private void onConstructed(World worldIn, Entity exploderIn, double xIn, double yIn, double zIn, float sizeIn, boolean causesFireIn,
            net.minecraft.world.Explosion.Mode modeIn, CallbackInfo ci) {
        // In Vanilla and Forge, 'damagesTerrain' controls both smoke particles and block damage
        // Sponge-created explosions will explicitly set 'impl$shouldBreakBlocks' to its proper value
        this.impl$shouldBreakBlocks = this.mode == net.minecraft.world.Explosion.Mode.BREAK || this.mode == net.minecraft.world.Explosion.Mode.DESTROY;
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
    public void doExplosionA() {

        // Sponge Start - Do not run calculation logic on client thread
        if (this.world.isRemote) {
            return;
        }
        // Sponge End

        // Sponge Start - If the explosion should not break blocks, don't bother calculating it on server thread
        if (this.impl$shouldBreakBlocks) {
            Set<BlockPos> set = Sets.newHashSet();
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
                                BlockState blockstate = this.world.getBlockState(blockpos);
                                IFluidState ifluidstate = this.world.getFluidState(blockpos);
                                if (!blockstate.isAir() || !ifluidstate.isEmpty()) {
                                    float f2 = Math.max(blockstate.getBlock().getExplosionResistance(), ifluidstate.getExplosionResistance());
                                    if (this.exploder != null) {
                                        f2 = this.exploder.getExplosionResistance((net.minecraft.world.Explosion) (Object) this, this.world,
                                                blockpos, blockstate, ifluidstate, f2);
                                    }

                                    f -= (f2 + 0.3F) * 0.3F;
                                }

                                if (f > 0.0F && (this.exploder == null || this.exploder.canExplosionDestroyBlock((net.minecraft.world.Explosion)
                                                (Object) this, this.world, blockpos, blockstate, f))) {
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

            this.affectedBlockPositions.addAll(set);
        }
        // Sponge End

        float f3 = this.size * 2.0F;
        int k1 = MathHelper.floor(this.x - (double) f3 - 1.0D);
        int l1 = MathHelper.floor(this.x + (double) f3 + 1.0D);
        int i2 = MathHelper.floor(this.y - (double) f3 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double) f3 + 1.0D);
        int j2 = MathHelper.floor(this.z - (double) f3 - 1.0D);
        int j1 = MathHelper.floor(this.z + (double) f3 + 1.0D);

        // Sponge Start - Only query for entities if we're to damage them
        List<Entity> list = this.impl$shouldDamageEntities ? this.world.getEntitiesWithinAABBExcludingEntity(this.exploder,
                new AxisAlignedBB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1)) : Collections.emptyList();
        // Sponge End

        if (ShouldFire.EXPLOSION_EVENT_DETONATE) {
            final List<ServerLocation> blockPositions = new ArrayList<>(this.affectedBlockPositions.size());
            final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(list.size());
            for (final BlockPos pos : this.affectedBlockPositions) {
                blockPositions
                        .add(ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) this.world, pos.getX(), pos.getY(), pos.getZ()));
            }
            for (final Entity entity : list) {
                // Make sure to check the entity is immune first.
                if (!entity.isImmuneToExplosions()) {
                    entities.add((org.spongepowered.api.entity.Entity) entity);
                }
            }
            final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();
            final ExplosionEvent.Detonate detonate = SpongeEventFactory.createExplosionEventDetonate(cause, blockPositions, entities,
                    (Explosion) this, (org.spongepowered.api.world.server.ServerWorld) this.world);
            SpongeCommon.postEvent(detonate);
            // Clear the positions so that they can be pulled from the event
            this.affectedBlockPositions.clear();
            if (detonate.isCancelled()) {
                return;
            }
            if (this.impl$shouldBreakBlocks) {
                for (final ServerLocation worldLocation : detonate.getAffectedLocations()) {
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

        Vec3d vec3d = new Vec3d(this.x, this.y, this.z);

        for(int k2 = 0; k2 < list.size(); ++k2) {
            Entity entity = list.get(k2);
            if (!entity.isImmuneToExplosions()) {
                double d12 = (double)(MathHelper.sqrt(entity.getDistanceSq(vec3d)) / f3);
                if (d12 <= 1.0D) {
                    double d5 = entity.getPosX() - this.x;
                    double d7 = entity.getPosYEye() - this.y;
                    double d9 = entity.getPosZ() - this.z;
                    double d13 = (double)MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = (double)shadow$getBlockDensity(vec3d, entity);
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(this.shadow$getDamageSource(), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f3 + 1.0D)));
                        double d11 = d10;
                        if (entity instanceof LivingEntity) {
                            d11 = ProtectionEnchantment.getBlastDamageReduction((LivingEntity)entity, d10);
                        }

                        // Sponge Start - Honor our knockback value from event
                        entity.setMotion(entity.getMotion().add(d5 * d11 * this.impl$knockback, d7 * d11 * this.impl$knockback, d9 * d11 * this.impl$knockback));
                        if (entity instanceof PlayerEntity) {
                            PlayerEntity playerentity = (PlayerEntity)entity;
                            if (!playerentity.isSpectator() && (!playerentity.isCreative() || !playerentity.abilities.isFlying)) {
                                this.playerKnockbackMap.put(playerentity, new Vec3d(d5 * d10 * this.impl$knockback,
                                        d7 * d10 * this.impl$knockback, d9 * d10 * this.impl$knockback));
                            }
                        }
                        // Sponge End
                    }
                }
            }
        }
    }

    /**
     * @author gabziou
     * @author zidane
     * @reason Run explosion logic through tracking
     */
    @Overwrite
    public void doExplosionB(boolean spawnParticles) {
        if (this.world.isRemote) {
            this.world.playSound(this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean flag = this.mode != net.minecraft.world.Explosion.Mode.NONE;
        if (spawnParticles) {
            if (!(this.size < 2.0F) && (flag || this.impl$shouldBreakBlocks)) {
                // Sponge Start - Use WorldServer methods since we prune the explosion packets
                // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
                // hundreds of explosions at once
                if (this.world instanceof ServerWorld) {
                    ((ServerWorld) this.world).spawnParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
                } else {
                    this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
                }
                // Sponge End
            } else {
                // Sponge Start - Use WorldServer methods since we prune the explosion packets
                // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
                // hundreds of explosions at once
                if (this.world instanceof ServerWorld) {
                    ((ServerWorld) this.world).spawnParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
                } else {
                    this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
                }
                // Sponge End
            }
        }

        if (flag) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
            Collections.shuffle(this.affectedBlockPositions, this.world.rand);

            for(BlockPos blockpos : this.affectedBlockPositions) {
                BlockState blockstate = this.world.getBlockState(blockpos);
                Block block = blockstate.getBlock();
                if (!blockstate.isAir()) {
                    BlockPos blockpos1 = blockpos.toImmutable();
                    this.world.getProfiler().startSection("explosion_blocks");
                    if (block.canDropFromExplosion((net.minecraft.world.Explosion) (Object) this) && this.world instanceof ServerWorld) {
                        TileEntity tileentity = block.hasTileEntity() ? this.world.getTileEntity(blockpos) : null;
                        LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.world)).withRandom(this.world.rand).withParameter(
                                LootParameters.POSITION, blockpos).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, tileentity).withNullableParameter(LootParameters.THIS_ENTITY, this.exploder);
                        if (this.mode == net.minecraft.world.Explosion.Mode.DESTROY) {
                            lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.size);
                        }

                        blockstate.getDrops(lootcontext$builder).forEach((p_229977_2_) -> {
                            shadow$func_229976_a_(objectarraylist, p_229977_2_, blockpos1);
                        });
                    }

                    this.world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3);
                    block.onExplosionDestroy(this.world, blockpos, (net.minecraft.world.Explosion) (Object) this);
                    this.world.getProfiler().endSection();
                }
            }

            for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
                Block.spawnAsEntity(this.world, pair.getSecond(), pair.getFirst());
            }
        }

        if (this.causesFire) {
            for(BlockPos blockpos2 : this.affectedBlockPositions) {
                if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockpos2).isAir() && this.world.getBlockState(blockpos2.down()).isOpaqueCube(this.world, blockpos2.down())) {
                    this.world.setBlockState(blockpos2, Blocks.FIRE.getDefaultState());
                }
            }
        }

    }

//
//    /**
//     * @author gabizou - March 26th, 2017
//     * @reason Since forge will attempt to call the normalized method for modded blocks,
//     * we must artificially capture the block position for any block drops or changes during the
//     * explosion phase.
//     *
//     * Does the second part of the explosion (sound, particles, drop spawn)
//     */
//    @Overwrite
//    public void doExplosionB(final boolean spawnParticles) {
//        this.world.playSound((PlayerEntity) null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE,
//            SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
//
//        final boolean flag = this.mode != net.minecraft.world.Explosion.Mode.NONE;
//        if (this.size >= 2.0F && (flag || this.impl$shouldBreakBlocks)) {
//            // Sponge Start - Use WorldServer methods since we prune the explosion packets
//            // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
//            // hundreds of explosions at once
//            if (this.world instanceof ServerWorld) {
//                ((ServerWorld) this.world).spawnParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
//            } else {
//                // Sponge End
//                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
//            } // Sponge - brackets.
//        } else {
//            // Sponge Start - Use WorldServer methods since we prune the explosion packets
//            // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
//            // hundreds of explosions at once
//            if (this.world instanceof ServerWorld) {
//                ((ServerWorld) this.world).spawnParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
//            } else { // Sponge end
//                this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
//            } // Sponge - brackets.
//        }
//        // Sponge Start - set up some variables for more fasts
//        @Nullable final PhaseContext<?> context = !((WorldBridge) this.world).bridge$isFake() ? PhaseTracker.getInstance().getPhaseContext() : null;
//        final boolean hasCapturePos = context != null && context.state.requiresBlockPosTracking();
//        // Sponge end
//
//        if (this.impl$shouldBreakBlocks) { // Sponge - use 'impl$shouldBreakBlocks' instead of 'damagesTerrain'
//            for (final BlockPos blockpos : this.affectedBlockPositions) {
//                final BlockState iblockstate = this.world.getBlockState(blockpos);
//                final Block block = iblockstate.getBlock();
//
//                if (spawnParticles) {
//                    final double d0 = (double) ((float) blockpos.getX() + this.world.rand.nextFloat());
//                    final double d1 = (double) ((float) blockpos.getY() + this.world.rand.nextFloat());
//                    final double d2 = (double) ((float) blockpos.getZ() + this.world.rand.nextFloat());
//                    double d3 = d0 - this.x;
//                    double d4 = d1 - this.y;
//                    double d5 = d2 - this.z;
//                    final double d6 = (double) MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
//                    d3 = d3 / d6;
//                    d4 = d4 / d6;
//                    d5 = d5 / d6;
//                    double d7 = 0.5D / (d6 / (double) this.size + 0.1D);
//                    d7 = d7 * (double) (this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F);
//                    d3 = d3 * d7;
//                    d4 = d4 * d7;
//                    d5 = d5 * d7;
//                    this.world.addParticle(ParticleTypes.POOF, (d0 + this.x) / 2.0D, (d1 + this.y) / 2.0D, (d2 + this.z) / 2.0D, d3, d4, d5);
//                    this.world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
//                }
//
//                if (iblockstate.isAir()) {
//                    if (block.canDropFromExplosion((net.minecraft.world.Explosion) (Object) this)) {
//                        // Sponge Start - Track the block position being destroyed
//                        // We need to capture this block position if necessary
//                        try (final CaptureBlockPos pos = hasCapturePos ? context.getCaptureBlockPos() : null) {
//                            if (pos != null) {
//                                pos.setPos(blockpos);
//                            }
//                            // Sponge End
//                            TileEntity tileentity = block.hasTileEntity() ? this.world.getTileEntity(blockpos) : null;
//                            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.world)).withRandom(this.world.rand).withParameter(LootParameters.POSITION, blockpos).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, tileentity);
//                            if (this.mode == net.minecraft.world.Explosion.Mode.DESTROY) {
//                                lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.size);
//                            }
//
//                            Block.spawnDrops(iblockstate, lootcontext$builder);
//                        } // Sponge - brackets
//                    }
//
//                    // Sponge Start - Track the block position being destroyed
//                    // We need to capture this block position if necessary
//                    try (final CaptureBlockPos pos = hasCapturePos ? context.getCaptureBlockPos() : null) {
//                        if (pos != null) {
//                            pos.setPos(blockpos);
//                        }
//                        // this.world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 3); // Vanilla
//                        // block.onExplosionDestroy(this.world, blockpos, this); // Vanilla
//                        // block.onBlockExploded(this.world, blockpos, this); // Forge
//                        // Sponge - Use our universal hook.
//                        SpongeImplHooks.blockExploded(block, this.world, blockpos, (net.minecraft.world.Explosion) (Object) this);
//                    }
//                    // Sponge End
//                }
//            }
//        }
//
//        if (this.causesFire) {
//            for (final BlockPos blockpos1 : this.affectedBlockPositions) {
//                if (this.world.getBlockState(blockpos1).isAir() && this.world.getBlockState(blockpos1.down()).isOpaqueCube(this.world, blockpos1.down()) && this.random.nextInt(3) == 0) {
//                    // Sponge Start - Track the block position being destroyed
//                    try (final CaptureBlockPos pos = hasCapturePos ? context.getCaptureBlockPos() : null) {
//                        if (pos != null) {
//                            pos.setPos(blockpos1);
//                        }
//                        // Sponge End
//                        this.world.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
//                    } // Sponge - brackets
//                }
//            }
//        }
//    }

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
    public void bridge$setResolution(int resolution) {
        this.impl$resolution = resolution;
    }

    @Override
    public int bridge$getResolution() {
        return this.impl$resolution;
    }

    @Override
    public void bridge$setRandomness(float randomness) {
        this.impl$randomness = randomness;
    }

    @Override
    public float bridge$getRandomness() {
        return this.impl$randomness;
    }

    @Override
    public void bridge$setKnockback(double knockback) {
        this.impl$knockback = knockback;
    }

    @Override
    public double bridge$getKnockback() {
        return this.impl$knockback;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("causesFire", this.causesFire)
                .add("mode", this.mode)
                .add("world", ((org.spongepowered.api.world.server.ServerWorld) this.world).getKey())
                .add("x", this.x)
                .add("y", this.y)
                .add("z", this.z)
                .add("exploder", this.exploder)
                .add("size", this.size)
                .add("resolution", this.impl$resolution)
                .add("randomness", this.impl$randomness)
                .add("knockback", this.impl$knockback)
                .toString();
    }
}
