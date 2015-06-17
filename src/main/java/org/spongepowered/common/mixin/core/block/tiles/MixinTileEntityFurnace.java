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

package org.spongepowered.common.mixin.core.block.tiles;

import static org.spongepowered.api.data.DataQuery.of;

import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.tileentity.FurnaceData;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.tileentity.FurnaceConsumeFuelEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackBuilder;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;

@NonnullByDefault
@Mixin(net.minecraft.tileentity.TileEntityFurnace.class)
public abstract class MixinTileEntityFurnace extends MixinTileEntityLockable implements Furnace {

    @Shadow private String furnaceCustomName;
    @Shadow private int currentItemBurnTime;
    @Shadow private int furnaceBurnTime;
    @Shadow private net.minecraft.item.ItemStack[] furnaceItemStacks;

    @Override
    public TileEntityType getType() {
        return TileEntityTypes.FURNACE;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(of("BurnTime"), this.getField(0));
        container.set(of("CookTime"), this.getField(3) - this.getField(2));
        container.set(of("CookTimeTotal"), this.getField(3));
        if (this.furnaceCustomName != null) {
            container.set(of("CustomName"), this.furnaceCustomName);
        }
        return container;
    }

    /**
     * A method to automatically fire FurnaceConsumeFuelEvent
     *
     * @param ci  A callback info needed to Inject the method
     */
    @Inject(method = "update",at = @At(
            target = "Lnet/minecraft/tileentity/TileEntityFurnace;getItemBurnTime(Lnet/minecraft/item/ItemStack;)I", value ="INVOKE_ASSIGN"))
    public void callFurnaceConsumeFuelEvent(CallbackInfo ci)
    {
        Furnace furnace=(Furnace) (Object) this;
        ItemStack fuelStack=(ItemStack)(Object)this.furnaceItemStacks[1];
        ItemStack burnedItem=fuelStack;
        burnedItem.setQuantity(1);
        ItemStack remainingFuel=fuelStack;
        remainingFuel.setQuantity(remainingFuel.getQuantity()-1);//remove one for present the event like already done
        if (remainingFuel.getQuantity()<=0) {
            remainingFuel = null;
        }
        int itemBurnTime = TileEntityFurnace.getItemBurnTime((net.minecraft.item.ItemStack) fuelStack);
        if (itemBurnTime>0){
            final FurnaceConsumeFuelEvent event = SpongeEventFactory.createFurnaceConsumeFuel(Sponge.getGame(), furnace,
                    (FurnaceData)  getData(FurnaceData.class)
                    ,burnedItem, remainingFuel,
                    null, furnace.getInventory(), furnace.getBlock());
            if (!Sponge.getGame().getEventManager().post(event)) { //if not cancelled
                this.furnaceBurnTime=this.currentItemBurnTime= TileEntityFurnace.getItemBurnTime((net.minecraft.item.ItemStack) event.getBurnedItem());
                if(event.getRemainingFuel().isPresent()) {
                    remainingFuel= event.getRemainingFuel().get();
                    remainingFuel.setQuantity(remainingFuel.getQuantity()+1); //add one because The mojang class will decrement this
                }
                else {
                    ItemStackBuilder itemStackBuilder= Sponge.getGame().getRegistry().getItemBuilder();
                    remainingFuel= itemStackBuilder.itemType(ItemTypes.SPONGE).quantity(1).build(); //set  a arbitrary itemStack with quantity=1,
                                                                                                    // the Mojang class will delete it
                }
                this.furnaceItemStacks[1] = (net.minecraft.item.ItemStack) remainingFuel;
                return;
            } else {
                this.furnaceBurnTime=this.currentItemBurnTime=0;
                return;
            }
        }
        this.furnaceBurnTime=this.currentItemBurnTime= itemBurnTime;
        return;
    }

}
