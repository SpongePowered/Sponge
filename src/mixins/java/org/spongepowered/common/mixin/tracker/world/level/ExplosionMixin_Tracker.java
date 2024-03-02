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
package org.spongepowered.common.mixin.tracker.world.level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.level.ExplosionBridge;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddBlockLootDropsEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.ExplodeBlockEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.SpawnDestructBlocksEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.WorldBlockChangeCompleteEffect;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.WorldPipeline;
import org.spongepowered.common.util.Constants;

@Mixin(Explosion.class)
public abstract class ExplosionMixin_Tracker {

    @Shadow @Final private Level level;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private Explosion.BlockInteraction blockInteraction;
    @Shadow @Final private float radius;
    @Shadow @Final private ObjectArrayList<BlockPos> toBlow;

    @Shadow @Final private boolean fire;
    @Shadow @Final private RandomSource random;

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
        this.level.playSound(null, this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
        // Sponge End

        final boolean flag = this.blockInteraction != Explosion.BlockInteraction.KEEP;
        if (spawnParticles) {
            if (!(this.radius < 2.0F) && (flag || ((ExplosionBridge) this).bridge$getShouldDamageBlocks())) {
                // Sponge Start - Use WorldServer methods since we prune the explosion packets
                // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
                // hundreds of explosions at once
                if (this.level instanceof ServerLevel) {
                    ((ServerLevel) this.level).sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
                } else {
                    this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
                }
                // Sponge End
            } else {
                // Sponge Start - Use WorldServer methods since we prune the explosion packets
                // to avoid spamming/lagging the client out when some ~idiot~ decides to explode
                // hundreds of explosions at once
                if (this.level instanceof ServerLevel) {
                    ((ServerLevel) this.level).sendParticles(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1, 0, 0, 0, 0.1D);
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
            Util.shuffle(this.toBlow, this.level.random);

            for (final BlockPos blockpos : this.toBlow) {
                final BlockState blockstate = this.level.getBlockState(blockpos);
                // Block block = blockstate.getBlock(); // Sponge - we don't use this
                if (!blockstate.isAir()) {
                    final BlockPos blockpos1 = blockpos.immutable();
                    this.level.getProfiler().push("explosion_blocks");

                    // Sponge - All of this is forwarded to the effects
                    // if (block.canDropFromExplosion(this) && this.level instanceof ServerLevel) {
                    //     BlockEntity var6 = block.isEntityBlock() ? this.level.getBlockEntity(blockpos) : null;
                    //     LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel)this.level)).withRandom(this.level.rand).withParameter(
                    //         LootParameters.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, var6).withNullableParameter(LootParameters.THIS_ENTITY, this.source);
                    //     if (this.blockInteraction == Explosion.BlockInteraction.DESTROY) {
                    //         lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.radius);
                    //     }

                    //     var3.getDrops(var7).forEach((param2) -> addBlockDrops(var1, param2, var5));
                    // }

                    //this.level.setBlock(blockpos, Blocks.AIR.defaultState(), 3);
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
            // for(Pair<ItemStack, BlockPos> var8 : objectarraylist) {
            //    Block.popResource(this.level, var8.getSecond(), var8.getFirst());
            // }
            // Sponge End
        }

        if (this.fire) {
            for(final BlockPos blockpos2 : this.toBlow) {
                if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockpos2).isAir() && this.level.getBlockState(blockpos2.below()).isSolidRender(this.level, blockpos2.below())) {
                    this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.level, blockpos2));
                }
            }
        }

    }
}
