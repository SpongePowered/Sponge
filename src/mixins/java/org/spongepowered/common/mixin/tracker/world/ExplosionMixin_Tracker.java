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
package org.spongepowered.common.mixin.tracker.world;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.ExplosionBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddBlockLootDropsEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.ExplodeBlockEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.SpawnDestructBlocksEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.WorldBlockChangeCompleteEffect;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.WorldPipeline;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(Explosion.class)
public abstract class ExplosionMixin_Tracker {

    @Shadow @Final private World level;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private Explosion.Mode blockInteraction;
    @Shadow @Final private float radius;
    @Shadow @Final private List<BlockPos> toBlow;

    @Shadow @Final private boolean fire;
    @Shadow @Final private Random random;

    /**
     * @author gabziou
     * @author zidane
     * @reason Run explosion logic through tracking
     */
    @Overwrite
    public void finalizeExplosion(final boolean spawnParticles) {
        // Sponge Start - In Sponge, we no longer call doExplosionB client-side (kills client perf)
        if (this.level.isClientSide) {
            return;
        }
        // Sponge End

        // Sponge Start - Send the sound packet down. We must do this as we do not call doExplosionB client-side
        this.level.playSound(null, this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
        // Sponge End

        final boolean flag = this.blockInteraction != net.minecraft.world.Explosion.Mode.NONE;
        if (spawnParticles) {
            if (!(this.radius < 2.0F) && (flag || ((ExplosionBridge) this).bridge$getShouldDamageBlocks())) {
                // Sponge Start - Use WorldServer methods since we prune the explosion packets
                // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
                // hundreds of explosions at once
                if (this.level instanceof ServerWorld) {
                    ((ServerWorld) this.level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
                } else {
                    this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
                }
                // Sponge End
            } else {
                // Sponge Start - Use WorldServer methods since we prune the explosion packets
                // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
                // hundreds of explosions at once
                if (this.level instanceof ServerWorld) {
                    ((ServerWorld) this.level).sendParticles(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
                } else {
                    this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
                }
                // Sponge End
            }
        }

        if (flag) {
            // Sponge Start - Forward changes through a WorldPipeline to associate side effects
            // Vanilla - uses a list of itemstacks to do a bunch of pre-merging
            // ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
            Collections.shuffle(this.toBlow, this.level.random);

            for (final BlockPos blockpos : this.toBlow) {
                final BlockState blockstate = this.level.getBlockState(blockpos);
                // Block block = blockstate.getBlock(); // Sponge - we don't use this
                if (!blockstate.isAir()) {
                    final BlockPos blockpos1 = blockpos.immutable();
                    this.level.getProfiler().push("explosion_blocks");

                    // Sponge - All of this is forwarded to the effects
                    // if (block.canDropFromExplosion(this) && this.world instanceof ServerWorld) {
                    //     TileEntity tileentity = block.hasTileEntity() ? this.world.getTileEntity(blockpos) : null;
                    //     LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.world)).withRandom(this.world.rand).withParameter(
                    //         LootParameters.POSITION, blockpos).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, tileentity).withNullableParameter(LootParameters.THIS_ENTITY, this.exploder);
                    //     if (this.mode == Explosion.Mode.DESTROY) {
                    //         lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.size);
                    //     }

                    //     blockstate.getDrops(lootcontext$builder).forEach((p_229977_2_) -> {
                    //         func_229976_a_(objectarraylist, p_229977_2_, blockpos1);
                    //     });
                    // }

                    //this.world.setBlock(blockpos, Blocks.AIR.getDefaultState(), 3);
                    //block.onExplosionDestroy(this.world, blockpos, this);

                    final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
                    ((TrackedWorldBridge) this.level).bridge$startBlockChange(blockpos1, Blocks.AIR.defaultBlockState(), 3)
                        .ifPresent(builder -> {
                            final WorldPipeline build = builder
                                .addEffect(AddBlockLootDropsEffect.getInstance())
                                .addEffect(ExplodeBlockEffect.getInstance())
                                .addEffect(SpawnDestructBlocksEffect.getInstance())
                                .addEffect(WorldBlockChangeCompleteEffect.getInstance())
                                .build();
                            build.processEffects(context, blockstate, Blocks.AIR.defaultBlockState(), blockpos1,
                                null,
                                BlockChangeFlagManager.fromNativeInt(3),
                                Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT);
                        });
                    // Sponge End
                    this.level.getProfiler().pop();
                }
            }
            // Sponge Start - This is built into the SpawnDestructBlocksEffect
            // for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
            //    Block.spawnAsEntity(this.world, pair.getSecond(), pair.getFirst());
            // }
            // Sponge End
        }

        if (this.fire) {
            for(final BlockPos blockpos2 : this.toBlow) {
                if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockpos2).isAir() && this.level.getBlockState(blockpos2.below()).isSolidRender(this.level, blockpos2.below())) {
                    this.level.setBlockAndUpdate(blockpos2, AbstractFireBlock.getState(this.level, blockpos2));
                }
            }
        }

    }
}
