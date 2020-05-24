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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.tileentity.BannerTileEntityBridge;
import org.spongepowered.common.data.provider.item.stack.ItemStackShieldBannerPatternsProvider;
import org.spongepowered.common.util.NonNullArrayList;

import java.util.ArrayList;
import java.util.List;

@Mixin(BannerTileEntity.class)
public abstract class BannerTileEntityMixin extends TileEntityMixin implements BannerTileEntityBridge {

    @Shadow private net.minecraft.item.DyeColor baseColor;
    @Shadow private ListNBT patterns;

    private List<BannerPatternLayer> impl$patternLayers = Lists.newArrayList();

    @Inject(method = "read", at = @At("RETURN"))
    private void onSetItemValues(final CallbackInfo ci) {
        this.impl$updatePatterns();
    }

    @Override
    protected void bridge$writeToSpongeCompound(final CompoundNBT compound) {
        super.bridge$writeToSpongeCompound(compound);
    }

    private void impl$markDirtyAndUpdate() {
        this.shadow$markDirty();
        if (this.world != null && !this.world.isRemote) {
            ((ServerWorld) this.world).getChunkProvider().markBlockChanged(this.shadow$getPos());
        }
    }

    private void impl$updatePatterns() {
        this.impl$patternLayers.clear();
        if (this.patterns != null) {
            for (INBT pattern : this.patterns) {
                this.impl$patternLayers.add(ItemStackShieldBannerPatternsProvider.layerFromNbt((CompoundNBT) pattern));
            }
        }
        this.impl$markDirtyAndUpdate();
    }

    @Override
    public List<BannerPatternLayer> bridge$getLayers() {
        return new ArrayList<>(this.impl$patternLayers);
    }

    @Override
    public void bridge$setLayers(final List<BannerPatternLayer> layers) {
        this.impl$patternLayers = new NonNullArrayList<>();
        this.impl$patternLayers.addAll(layers);
        this.patterns = new ListNBT();
        for (final BannerPatternLayer layer : this.impl$patternLayers) {
            this.patterns.add(ItemStackShieldBannerPatternsProvider.layerToNbt(layer));
        }
        this.impl$markDirtyAndUpdate();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public DyeColor bridge$getBaseColor() {
        return (DyeColor) (Object) this.baseColor;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void bridge$setBaseColor(final DyeColor baseColor) {
        checkNotNull(baseColor, "Null DyeColor!");
        try {
            this.baseColor = (net.minecraft.item.DyeColor) (Object) baseColor;
        } catch (final Exception e) {
            this.baseColor = net.minecraft.item.DyeColor.BLACK;
        }
        this.impl$markDirtyAndUpdate();
    }
}
