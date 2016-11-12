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
package org.spongepowered.common.mixin.api.item.merchant;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.item.merchant.Merchant;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.world.IMixinLocation;

import javax.annotation.Nullable;

@Mixin(value = Merchant.class, remap = false)
public interface MixinMerchant extends Merchant, IMerchant {

    @Override
    default void setCustomer(EntityPlayer player) {
        this.setCustomer((Humanoid) player);
    }

    @Override
    default EntityPlayer mth_000385_s_() {
        return this.getCustomer().map(EntityUtil.HUMANOID_TO_PLAYER).orElse(null);
    }


    @Nullable
    @Override
    default MerchantRecipeList getRecipes(EntityPlayer player) {
        final MerchantRecipeList merchantRecipes = new MerchantRecipeList();
        for (TradeOffer tradeOffer : getTradeOfferData().tradeOffers()) {
            merchantRecipes.add((MerchantRecipe) tradeOffer);
        }
        return merchantRecipes;
    }

    @Override
    default void useRecipe(MerchantRecipe recipe) {

    }

    @Override
    default void verifySellingItem(ItemStack stack) {

    }

    @Override
    default ITextComponent getDisplayName() {
        return new TextComponentString("nitwit");
    }

    @Override
    default World mth_000390_t_() {
        return ((World) getLocation().getExtent());
    }

    @Override
    default BlockPos mth_000391_u_() {
        return ((IMixinLocation) (Object) getLocation()).getBlockPos();
    }
}
