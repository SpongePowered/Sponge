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
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.meta.SpongePatternLayer;
import org.spongepowered.common.bridge.tileentity.TileEntityBannerBridge;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.NonNullArrayList;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.world.server.ServerWorld;

@NonnullByDefault
@Mixin(BannerTileEntity.class)
public abstract class TileEntityBannerMixin extends TileEntityMixin implements TileEntityBannerBridge {

    @Shadow private net.minecraft.item.DyeColor baseColor;
    @Shadow private ListNBT patterns;

    private List<PatternLayer> impl$patternLayers = Lists.newArrayList();

    @Inject(method = "setItemValues", at = @At("RETURN"))
    private void onSetItemValues(final CallbackInfo ci) {
        impl$updatePatterns();
    }

    @Override
    public void bridge$readFromSpongeCompound(final CompoundNBT compound) {
        super.bridge$readFromSpongeCompound(compound);
        impl$updatePatterns();
    }

    @Override
    protected void bridge$writeToSpongeCompound(final CompoundNBT compound) {
        super.bridge$writeToSpongeCompound(compound);
    }

    private void impl$markDirtyAndUpdate() {
        this.bridge$markDirty();
        if (this.world != null && !this.world.field_72995_K) {
            ((ServerWorld) this.world).func_184164_w().func_180244_a(this.getPos());
        }
    }

    private void impl$updatePatterns() {
        this.impl$patternLayers.clear();
        if (this.patterns != null) {
            final SpongeGameRegistry registry = SpongeImpl.getRegistry();
            for (int i = 0; i < this.patterns.func_74745_c(); i++) {
                final CompoundNBT tagCompound = this.patterns.func_150305_b(i);
                final String patternId = tagCompound.func_74779_i(Constants.TileEntity.Banner.BANNER_PATTERN_ID);
                final int patternColor = tagCompound.func_74762_e(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR);
                this.impl$patternLayers.add(new SpongePatternLayer(
                    registry.getType(BannerPatternShape.class, patternId).get(),
                    registry.getType(DyeColor.class, net.minecraft.item.DyeColor.func_176766_a(patternColor).func_176610_l()).get()));
            }
        }
        this.impl$markDirtyAndUpdate();
    }

    @Override
    public List<PatternLayer> bridge$getLayers() {
        return new ArrayList<>(this.impl$patternLayers);
    }

    @Override
    public void bridge$setLayers(final List<PatternLayer> layers) {
        this.impl$patternLayers = new NonNullArrayList<>();
        this.impl$patternLayers.addAll(layers);
        this.patterns = new ListNBT();
        for (final PatternLayer layer : this.impl$patternLayers) {
            final CompoundNBT compound = new CompoundNBT();
            compound.func_74778_a(Constants.TileEntity.Banner.BANNER_PATTERN_ID, layer.getShape().getName());
            compound.func_74768_a(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR, ((net.minecraft.item.DyeColor) (Object) layer.getColor()).func_176767_b());
            this.patterns.func_74742_a(compound);
        }
        impl$markDirtyAndUpdate();
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
            final net.minecraft.item.DyeColor color = (net.minecraft.item.DyeColor) (Object) baseColor;
            this.baseColor = color;
        } catch (final Exception e) {
            this.baseColor = net.minecraft.item.DyeColor.BLACK;
        }
        impl$markDirtyAndUpdate();
    }
}
