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

import com.google.common.collect.Maps;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class SpongeVillagerTradeWindow extends AbstractSpongeContainerWindow implements VillagerTradeWindow {

    private IMerchant merchant;
    private TradeOfferData tradeData;
    // private DummyMerchant dummyMerchant;
    private final Map<EntityPlayerMP, DummyMerchant> dummyMerchants = Maps.newHashMap();

    @Override
    protected IInteractionObject provideInteractionObject() {
        return null; // Unused
    }

    @Override
    protected boolean isVirtual() {
        return this.merchant == null;
    }

    @Override
    protected boolean show(EntityPlayerMP player) {
        IMerchant merchant = this.merchant;
        if (merchant == null) {
            this.dummyMerchants.put(player, (DummyMerchant) (merchant = new DummyMerchant(player, this.tradeData)));
        }
        player.displayVillagerTradeGui(merchant);
        if (this.merchant != null) {
            this.merchant.setCustomer(player);
        }
        return player.openContainer != player.inventoryContainer;
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
    protected void onClosed(EntityPlayerMP player) {
        this.dummyMerchants.remove(player);
        super.onClosed(player);
    }

    @Override
    public void setVirtualTradeData(TradeOfferData tradeOfferData) {
        this.tradeData = tradeOfferData;
        for (Entry<EntityPlayerMP, DummyMerchant> entry : this.dummyMerchants.entrySet()) {
            sendDummyData(entry.getKey(), entry.getValue(), tradeOfferData);
        }
    }

    private void sendDummyData(EntityPlayerMP player, DummyMerchant merchant, TradeOfferData tradeOfferData) {
        merchant.setTradeData(tradeOfferData);
        MerchantRecipeList recipes = merchant.getRecipes(player);
        // See displayVillagerTradeGui
        PacketBuffer buffer = new PacketBuffer(buffer());
        buffer.writeInt(player.openContainer.windowId);
        if (recipes != null) {
            recipes.writeToBuf(buffer);
        } else {
            buffer.writeByte(0);
        }
        player.playerNetServerHandler.sendPacket(new S3FPacketCustomPayload("MC|TrList", buffer));
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
        public void setCustomer(EntityPlayer player) {
            this.customer = player;
        }

        @Override
        public EntityPlayer getCustomer() {
            return this.customer;
        }

        @Override
        public MerchantRecipeList getRecipes(EntityPlayer player) {
            if (player == this.customer) {
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
