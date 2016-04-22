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
package org.spongepowered.common.gui.window;

import static io.netty.buffer.Unpooled.buffer;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.gui.window.VillagerTradeWindow;
import org.spongepowered.api.item.merchant.Merchant;
import org.spongepowered.common.data.processor.data.entity.TradeOfferDataProcessor;

import java.util.Optional;

public class SpongeVillagerTradeWindow extends AbstractSpongeContainerWindow implements VillagerTradeWindow {

    private IMerchant merchant;
    private TradeOfferData tradeData;
    private DummyMerchant dummyMerchant;

    @Override
    protected IInteractionObject provideInteractionObject() {
        return null; // Unused
    }

    @Override
    protected boolean show() {
        this.player.displayVillagerTradeGui(this.merchant != null ? this.merchant
                : (this.dummyMerchant = new DummyMerchant(this.player, this.tradeData)));
        if (this.merchant != null) {
            this.merchant.setCustomer(this.player);
        }
        return this.player.openContainer != this.player.inventoryContainer;
    }

    @Override
    public Optional<Merchant> getMerchant() {
        return Optional.ofNullable((Merchant) this.merchant);
    }

    @Override
    public void setMerchant(Merchant merchant) {
        this.merchant = (IMerchant) merchant;
    }

    @Override
    public void onClientClose(Packet<INetHandlerPlayServer> packet) {
        this.dummyMerchant = null;
        super.onClientClose(packet);
    }

    @Override
    public void setVirtualTradeData(TradeOfferData tradeOfferData) {
        this.tradeData = tradeOfferData;
        if (this.dummyMerchant != null) {
            this.dummyMerchant.setTradeData(tradeOfferData);
            MerchantRecipeList recipes = this.dummyMerchant.getRecipes(this.player);
            // See displayVillagerTradeGui
            PacketBuffer buffer = new PacketBuffer(buffer());
            buffer.writeInt(this.player.openContainer.windowId);
            if (recipes != null) {
                recipes.writeToBuf(buffer);
            } else {
                buffer.writeByte(0);
            }
            this.player.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|TrList", buffer));
        }
    }

    private static class DummyMerchant implements IMerchant {

        private EntityPlayer customer;
        private MerchantRecipeList recipes;

        public DummyMerchant(EntityPlayer player, TradeOfferData tradeData) {
            this.customer = player;
            setTradeData(tradeData);
        }

        public void setTradeData(TradeOfferData tradeOfferData) {
            this.recipes = tradeOfferData == null ? null : TradeOfferDataProcessor.toMerchantRecipeList(tradeOfferData.asList());
        }

        @Override
        public void setCustomer(EntityPlayer p_70932_1_) {
            this.customer = p_70932_1_;
        }

        @Override
        public EntityPlayer getCustomer() {
            return this.customer;
        }

        @Override
        public MerchantRecipeList getRecipes(EntityPlayer p_70934_1_) {
            if (p_70934_1_ == this.customer) {
                return this.recipes;
            }
            return null;
        }

        @Override
        public void useRecipe(MerchantRecipe recipe) {
            recipe.incrementToolUses();
        }

        @Override
        public void verifySellingItem(ItemStack stack) {
        }

        @Override
        public IChatComponent getDisplayName() {
            return new ChatComponentText("Merchant");
        }

    }

    public static class Builder extends SpongeWindowBuilder<VillagerTradeWindow, VillagerTradeWindow.Builder> implements VillagerTradeWindow.Builder {

        @Override
        public VillagerTradeWindow build() {
            return new SpongeVillagerTradeWindow();
        }
    }

}
