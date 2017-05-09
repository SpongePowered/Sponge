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
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpirableData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.weather.Lightning;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpirableData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.interfaces.entity.IMixinEntityLightningBolt;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.util.VecHelper;

import java.util.List;

@Mixin(EntityLightningBolt.class)
public abstract class MixinEntityLightningBolt extends MixinEntityWeatherEffect implements Lightning, IMixinEntityLightningBolt {

    public Cause cause = Cause.source(this).build();

    private final List<Entity> struckEntities = Lists.newArrayList();
    private final List<Transaction<BlockSnapshot>> struckBlocks = Lists.newArrayList();
    private boolean effect = false;

    @Shadow private int lightningState;

    @Override
    public boolean isEffect() {
        return this.effect;
    }

    @Override
    public void setEffect(boolean effect) {
        this.effect = effect;
        if (effect) {
            this.struckBlocks.clear();
            this.struckEntities.clear();
        }
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"))
    public boolean onStrikeBlockInit(net.minecraft.world.World world, BlockPos pos, IBlockState blockState) {
        return onStrikeBlock(world, pos, blockState);
    }

    @Redirect(method = "onUpdate()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"))
    public boolean onStrikeBlockUpdate(net.minecraft.world.World world, BlockPos pos, IBlockState blockState) {
        return onStrikeBlock(world, pos, blockState);
    }

    private boolean onStrikeBlock(net.minecraft.world.World world, BlockPos pos, IBlockState blockState) {
        if (!this.effect && ((World) world).containsBlock(pos.getX(), pos.getY(), pos.getZ())) {
            Vector3i pos3i = VecHelper.toVector3i(pos);
            Transaction<BlockSnapshot> transaction = new Transaction<BlockSnapshot>(new SpongeBlockSnapshotBuilder()
                    .blockState((BlockState) world.getBlockState(pos)).world(((World) world).getProperties()).position(pos3i).build(),
                    new SpongeBlockSnapshotBuilder().blockState((BlockState) blockState).world(((World) world).getProperties()).position(pos3i)
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
    public void onStrikeEntity(net.minecraft.entity.Entity mcEntity, EntityLightningBolt lightningBolt) {
        if (!this.effect) {
            Entity entity = (Entity) mcEntity;
            if (!this.struckEntities.contains(entity)) {
                this.struckEntities.add(entity);
            }
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/EntityLightningBolt;setDead()V"))
    public void onLivingTimeExpired(CallbackInfo ci) {
        if (this.isDead) {
            return;
        }
        World world = (World) this.world;
        LightningEvent.Strike strike = SpongeEventFactory.createLightningEventStrike(this.cause, this.struckEntities, this.struckBlocks);
        Sponge.getEventManager().post(strike);

        if (!strike.isCancelled()) {
            for (Transaction<BlockSnapshot> bt : strike.getTransactions()) {
                if (bt.isValid()) {
                    BlockSnapshot bs = bt.getFinal();
                    ((WorldServer) world).setBlockState(((IMixinLocation) (Object) bs.getLocation().get()).getBlockPos(),
                            ((IBlockState) bs.getState()));
                }
            }
            for (Entity e : strike.getEntities()) {
                ((net.minecraft.entity.Entity) e).onStruckByLightning((EntityLightningBolt) (Object) this);
            }
            SpongeImpl.postEvent(SpongeEventFactory.createLightningEventPost(this.cause));
        }
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey("effect")) {
            this.effect = compound.getBoolean("effect");
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setBoolean("effect", this.effect);
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public void setCause(Cause cause) {
        this.cause = cause;
    }

    // Data delegated methods

    @Override
    public ExpirableData getExpiringData() {
        return new SpongeExpirableData(this.lightningState, 2);
    }

    @Override
    public MutableBoundedValue<Integer> expireTicks() {
        return SpongeValueFactory.boundedBuilder(Keys.EXPIRATION_TICKS)
                .minimum((int) Short.MIN_VALUE)
                .maximum(2)
                .defaultValue(2)
                .actualValue(this.lightningState)
                .build();
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getExpiringData());
    }
}
