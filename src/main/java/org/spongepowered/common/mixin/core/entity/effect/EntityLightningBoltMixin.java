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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.NBTTagCompound;
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

import java.util.List;

@Mixin(EntityLightningBolt.class)
public abstract class EntityLightningBoltMixin extends EntityMixin {

    private final List<Entity> struckEntities = Lists.newArrayList();
    private final List<Transaction<BlockSnapshot>> struckBlocks = Lists.newArrayList();
    private boolean effect = false;

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"))
    private boolean spongeImpl$throwEventForChangingBlocks(final net.minecraft.world.World world, final BlockPos pos, final IBlockState blockState) {
        return spongeImpl$strikeBlockAndAddSnapshot(world, pos, blockState);
    }

    @Redirect(method = "onUpdate()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"))
    private boolean spongeImpl$throwEventForChangingBlockDuringUpdate(final net.minecraft.world.World world, final BlockPos pos, final IBlockState blockState) {
        return spongeImpl$strikeBlockAndAddSnapshot(world, pos, blockState);
    }

    private boolean spongeImpl$strikeBlockAndAddSnapshot(final net.minecraft.world.World world, final BlockPos pos, final IBlockState blockState) {
        if (!this.effect && ((World) world).containsBlock(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p())) {
            final Vector3i pos3i = VecHelper.toVector3i(pos);
            final Transaction<BlockSnapshot> transaction = new Transaction<>(
                SpongeBlockSnapshotBuilder.pooled()
                    .blockState(world.func_180495_p(pos))
                    .world(((World) world).getProperties())
                    .position(pos3i)
                    .build(),
                SpongeBlockSnapshotBuilder.pooled()
                    .blockState(blockState)
                    .world(((World) world).getProperties())
                    .position(pos3i)
                    .build());
            if (!this.struckBlocks.contains(transaction)) {
                this.struckBlocks.add(transaction);
            }
            return true;
        }
        return false;
    }

    @Redirect(method = "onUpdate()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;onStruckByLightning(Lnet/minecraft/entity/effect/EntityLightningBolt;)V"))
    private void spongeImpl$AddEntityToListForEvent(final net.minecraft.entity.Entity mcEntity, final EntityLightningBolt lightningBolt) {
        if (!this.effect) {
            final Entity entity = (Entity) mcEntity;
            if (!this.struckEntities.contains(entity)) {
                this.struckEntities.add(entity);
            }
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/EntityLightningBolt;setDead()V"))
    private void spongeImpl$ThrowEventAndProcess(final CallbackInfo ci) {
        if (this.isDead || this.world.field_72995_K) {
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            final LightningEvent.Strike
                strike =
                SpongeEventFactory
                    .createLightningEventStrike(frame.getCurrentCause(), this.struckEntities, this.struckBlocks);
            Sponge.getEventManager().post(strike);

            if (!strike.isCancelled()) {
                for (final Transaction<BlockSnapshot> bt : strike.getTransactions()) {
                    if (bt.isValid()) {
                        final BlockSnapshot bs = bt.getFinal();
                        this.world.func_175656_a(VecHelper.toBlockPos(bs.getLocation().get()), ((IBlockState) bs.getState()));
                    }
                }
                for (final Entity e : strike.getEntities()) {
                    ((net.minecraft.entity.Entity) e).func_70077_a((EntityLightningBolt) (Object) this);
                }
                SpongeImpl.postEvent(SpongeEventFactory.createLightningEventPost(frame.getCurrentCause()));
            }
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.func_74764_b(Constants.Entity.LIGHTNING_EFFECT)) {
            this.effect = compound.func_74767_n(Constants.Entity.LIGHTNING_EFFECT);
        }
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        compound.func_74757_a(Constants.Entity.LIGHTNING_EFFECT, this.effect);
    }

}
