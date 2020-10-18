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
package org.spongepowered.common.mixin.core.tileentity;

import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

@Mixin(net.minecraft.tileentity.TileEntity.class)
public abstract class TileEntityMixin implements TileEntityBridge, DataCompoundHolder, TimingBridge {

    //@formatter:off
    @Shadow @Final private TileEntityType<?> type;
    @Shadow @Nullable private BlockState cachedBlockState;
    @Shadow protected net.minecraft.world.World world;
    @Shadow protected BlockPos pos;
    @Nullable private Timing impl$timing;

    @Shadow public abstract BlockPos shadow$getPos();
    @Shadow public abstract BlockState shadow$getBlockState();
    @Shadow public abstract void shadow$markDirty();
    //@formatter:on

    @Override
    public Timing bridge$getTimingsHandler() {
        if (this.impl$timing == null) {
            this.impl$timing = SpongeTimings.getTileEntityTiming((BlockEntity) this);
        }
        return this.impl$timing;
    }

    // When changing custom data it is serialized on to this.
    // On writeInternal the SpongeData tag is added to the new CompoundNBT accordingly
    // In a Forge environment the ForgeData tag is managed by forge
    // Structure: tileNbt - ForgeData - SpongeData - customdata
    private CompoundNBT impl$nbt;

    // TODO overrides for ForgeData
    // @Shadow private CompoundNBT customTileData;
    // @Override CompoundNBT data$getForgeData()
    // @Override CompoundNBT data$getForgeData()
    // @Override CompoundNBT data$hasForgeData()
    // @Override CompoundNBT cleanEmptySpongeData()

    @Override
    public CompoundNBT data$getCompound() {
        return this.impl$nbt;
    }

    @Override
    public void data$setCompound(CompoundNBT nbt) {
        this.impl$nbt = nbt;
    }

    @Override
    public NBTDataType data$getNbtDataType() {
        return NBTDataTypes.BLOCK_ENTITY;
    }

    @Inject(method = "writeInternal", at = @At("RETURN"))
    private void impl$WriteSpongeDataToCompound(final CompoundNBT compound, final CallbackInfoReturnable<CompoundNBT> ci) {
        if (this.data$hasSpongeData()) {
            final CompoundNBT forgeCompound = compound.getCompound(Constants.Forge.FORGE_DATA);
            // If we are in Forge data is already present
            if (forgeCompound != this.data$getForgeData()) {
                if (forgeCompound.isEmpty()) { // In vanilla this should be an new detached empty compound
                    compound.put(Constants.Forge.FORGE_DATA, forgeCompound);
                }
                // Get our nbt data and write it to the compound
                forgeCompound.put(Constants.Sponge.SPONGE_DATA, this.data$getSpongeData());
            }
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(final CompoundNBT compound, final CallbackInfo ci) {
        // If we are in Forge data is already present
        this.data$setCompound(compound); // For vanilla we set the incoming nbt
        if (this.data$hasSpongeData()) {
            // Deserialize our data...
            CustomDataHolderBridge.syncTagToCustom(this);
            this.data$setCompound(null);; // For vanilla this will be recreated empty in the next call - for Forge it reuses the existing compound instead
            // ReSync our data (includes failed data)
            CustomDataHolderBridge.syncCustomToTag(this);
        } else {
            this.data$setCompound(null); // No data? No need to keep the nbt
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                // Double check some mods are registering their tile entities and doing some "interesting"
                // things with doing a to string on a tile entity not actually functionally valid in the game "yet".
            .add("tileType", ((BlockEntityType) this.type).getKey())
            .add("world", this.world)
            .add("pos", this.pos)
            .add("blockMetadata", this.cachedBlockState)
            .toString();
    }

    protected MoreObjects.ToStringHelper getPrettyPrinterStringHelper() {
        return MoreObjects.toStringHelper(this)
            .add("type", ((BlockEntityType) this.type).getKey())
            .add("world", ((ServerWorld) this.world).getKey())
            .add("pos", this.pos);
    }

    @Override
    public String bridge$getPrettyPrinterString() {
        return this.getPrettyPrinterStringHelper().toString();
    }
}
