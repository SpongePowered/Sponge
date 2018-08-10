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
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.util.VecHelper;

import javax.annotation.Nullable;

@Mixin(value = Merchant.class)
@Implements(@Interface(iface = IMerchant.class, prefix = "imerchant$"))
public interface MixinMerchant extends Merchant {

    default void imerchant$setCustomer(EntityPlayer player) {
        this.setCustomer((Humanoid) player);
    }

    default EntityPlayer imerchant$getCustomer() {
        return this.getCustomer().map(EntityUtil.HUMANOID_TO_PLAYER).orElse(null);
    }


    @Nullable
    default MerchantRecipeList imerchant$getRecipes(EntityPlayer player) {
        final MerchantRecipeList merchantRecipes = new MerchantRecipeList();
        for (TradeOffer tradeOffer : getTradeOfferData().tradeOffers()) {
            merchantRecipes.add((MerchantRecipe) tradeOffer);
        }
        return merchantRecipes;
    }

    default void imerchant$useRecipe(MerchantRecipe recipe) {

    }

    default void imerchant$verifySellingItem(ItemStack stack) {

    }

    default ITextComponent imerchant$getDisplayName() {
        return new TextComponentString("nitwit");
    }

    default World imerchant$getWorld() {
        return ((World) getLocation().getExtent());
    }

    default BlockPos imerchant$getPos() {
        return VecHelper.toBlockPos(getLocation());
    }
}
