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
package org.spongepowered.common.mixin.core.entity.effect;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;
import java.util.List;

@Mixin(LightningBoltEntity.class)
public abstract class LightningBoltEntityMixin extends EntityMixin {

    private final List<Entity> impl$struckEntities = Lists.newArrayList();
    private final List<Transaction<BlockSnapshot>> impl$struckBlocks = Lists.newArrayList();
    private boolean impl$effect = false;

    @Redirect(method = "igniteBlocks",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private boolean impl$throwEventForChangingBlocks(final net.minecraft.world.World world, final BlockPos pos, final BlockState blockState) {
        return this.impl$strikeBlockAndAddSnapshot(world, pos, blockState);
    }

    @Redirect(method = "igniteBlocks(I)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private boolean impl$throwEventForChangingBlockDuringUpdate(final net.minecraft.world.World world, final BlockPos pos, final BlockState blockState) {
        return this.impl$strikeBlockAndAddSnapshot(world, pos, blockState);
    }

    private boolean impl$strikeBlockAndAddSnapshot(final net.minecraft.world.World world, final BlockPos pos, final BlockState blockState) {
        if (!this.impl$effect && ((World) world).containsBlock(pos.getX(), pos.getY(), pos.getZ())) {
            final Vector3i pos3i = VecHelper.toVector3i(pos);
            final Transaction<BlockSnapshot> transaction = new Transaction<>(
                SpongeBlockSnapshotBuilder.pooled()
                    .blockState(world.getBlockState(pos))
                    .world(((World) world).getProperties())
                    .position(pos3i)
                    .build(),
                SpongeBlockSnapshotBuilder.pooled()
                    .blockState(blockState)
                    .world(((World) world).getProperties())
                    .position(pos3i)
                    .build());
            if (!this.impl$struckBlocks.contains(transaction)) {
                this.impl$struckBlocks.add(transaction);
            }
            return true;
        }
        return false;
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;onStruckByLightning(Lnet/minecraft/entity/effect/LightningBoltEntity;)V"))
    private void impl$AddEntityToListForEvent(final net.minecraft.entity.Entity mcEntity, final LightningBoltEntity lightningBolt) {
        if (!this.impl$effect) {
            final Entity entity = (Entity) mcEntity;
            if (!this.impl$struckEntities.contains(entity)) {
                this.impl$struckEntities.add(entity);
            }
        }
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/LightningBoltEntity;remove()V"))
    private void impl$ThrowEventAndProcess(final CallbackInfo ci) {
        if (this.removed || this.world.isRemote) {
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            final LightningEvent.Strike
                strike =
                SpongeEventFactory
                    .createLightningEventStrike(frame.getCurrentCause(), this.impl$struckEntities, this.impl$struckBlocks);
            Sponge.getEventManager().post(strike);

            if (!strike.isCancelled()) {
                for (final Transaction<BlockSnapshot> bt : strike.getTransactions()) {
                    if (bt.isValid()) {
                        final BlockSnapshot bs = bt.getFinal();
                        this.world.setBlockState(VecHelper.toBlockPos(bs.getLocation().get()), ((BlockState) bs.getState()));
                    }
                }
                for (final Entity e : strike.getEntities()) {
                    ((net.minecraft.entity.Entity) e).onStruckByLightning((LightningBoltEntity) (Object) this);
                }
                SpongeImpl.postEvent(SpongeEventFactory.createLightningEventPost(frame.getCurrentCause()));
            }
        }
    }

    @Override
    public void impl$readFromSpongeCompound(final CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);
        if (compound.contains(Constants.Entity.LIGHTNING_EFFECT)) {
            this.impl$effect = compound.getBoolean(Constants.Entity.LIGHTNING_EFFECT);
        }
    }

    @Override
    public void impl$writeToSpongeCompound(final CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);
        compound.putBoolean(Constants.Entity.LIGHTNING_EFFECT, this.impl$effect);
    }

}
