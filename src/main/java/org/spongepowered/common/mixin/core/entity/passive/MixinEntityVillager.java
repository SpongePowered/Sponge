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
package org.spongepowered.common.mixin.core.entity.passive;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.api.item.merchant.VillagerRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCareerData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeEntityMeta;
import org.spongepowered.common.interfaces.entity.IMixinVillager;
import org.spongepowered.common.item.inventory.adapter.impl.MinecraftInventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;
import org.spongepowered.common.mixin.core.entity.MixinEntityAgeable;
import org.spongepowered.common.registry.SpongeVillagerRegistry;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(EntityVillager.class)
@Implements({@Interface(iface = Villager.class, prefix = "villager$"), @Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$")})
public abstract class MixinEntityVillager extends MixinEntityAgeable implements Villager, IMixinVillager, CarriedInventory<Villager> {

    @Shadow private boolean isPlaying; // isPlaying
    @Shadow private int careerId; // careerId
    @Shadow private int careerLevel; // careerLevel
    @Shadow @Nullable private MerchantRecipeList buyingList; // buyingList
    @Shadow @Final private InventoryBasic villagerInventory; // villagerInventory

    @Shadow public abstract void setProfession(int professionId); // setProfession
    @Shadow public abstract void setCustomer(EntityPlayer player);
    @Shadow public abstract boolean shadow$isTrading(); // isTrading
    @Shadow @Nullable public abstract EntityPlayer shadow$getCustomer(); // getCustomer
    @Shadow public abstract MerchantRecipeList getRecipes(EntityPlayer player);

    private Fabric<IInventory> fabric;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    private Profession profession;

    @Inject(method = "setProfession(I)V", at = @At("RETURN"))
    public void onSetProfession(int professionId, CallbackInfo ci) {
        this.profession = EntityUtil.validateProfession(professionId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this.villagerInventory);
        this.slots = new SlotCollection.Builder().add(8).build();
        this.lens = new OrderedInventoryLensImpl(0, 8, 1, this.slots);
    }

    public SlotProvider<IInventory, ItemStack> inventory$getSlotProvider() {
        return this.slots;
    }

    public Lens<IInventory, ItemStack> inventory$getRootLens() {
        return this.lens;
    }

    public Fabric<IInventory> inventory$getInventory() {
        return this.fabric;
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    @Intrinsic
    public boolean villager$isTrading() {
        return this.shadow$isTrading();
    }

    @Override
    public Career getCareer() {
        List<Career> careers = (List<Career>) this.profession.getCareers();
        if (this.careerId == 0 || this.careerId > careers.size()) {
            this.careerId = new Random().nextInt(careers.size()) + 1;
        }
        this.getRecipes(null);
        return careers.get(this.careerId - 1);
    }

    @Override
    public Profession getProfession() {
        return this.profession;
    }

    @Override
    public void setCareer(Career career) {
        setProfession(((SpongeEntityMeta) career.getProfession()).type);
        this.buyingList = null;
        this.careerId = ((SpongeCareer) career).type + 1;
        this.careerLevel = 1;
        this.getRecipes(null);
    }

    @Override
    public Optional<Humanoid> getCustomer() {
        return Optional.ofNullable((Humanoid) this.shadow$getCustomer());
    }

    /**
     * @author gabizou - January 13th, 2016
     * @reason This overwrites the current method using the multi-dimension array with
     * our {@link VillagerRegistry} to handle career levels and registrations
     * for {@link TradeOfferGenerator}s. Note that this takes over entirely
     * whatever vanilla does, but this allows for maximum customization for
     * plugins to handle gracefully.
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public void populateBuyingList() { // populateBuyingList
        // Sponge
        List<Career> careers = (List<Career>) this.profession.getCareers();

        // Sponge start - throw an informative exception if we somehow have no careeres
        if (careers.size() == 0) {
            throw new IllegalStateException(String.format("Villager '%s' with profession '%s' has no careers!", this, this.profession));
        }
        // Sponge end

        // EntityVillager.ITradeList[][][] aentityvillager$itradelist = DEFAULT_TRADE_LIST_MAP[this.getProfession()];

        if (this.careerId != 0 && this.careerLevel != 0) {
            ++this.careerLevel;
        } else {
            // Sponge change aentityvillager$itradelist to use this.profession.getCareers()
            this.careerId = this.rand.nextInt(careers.size()) + 1;
            this.careerLevel = 1;
        }

        if (this.buyingList == null) {
            this.buyingList = new MerchantRecipeList();
        }

        // Sponge start - use our own registry stuffs
        checkState(this.careerId <= careers.size(), "The villager career id is out of bounds fo the available Careers! Found: " + this.careerId
                                                    + " when the current maximum is: " + careers.size());
        final Career careerLevel = careers.get(this.careerId - 1);
        SpongeVillagerRegistry.getInstance().populateOffers(this, (List<TradeOffer>) (List<?>) this.buyingList, careerLevel, this.careerLevel, this.rand);
        // Sponge end
    }

    @Override
    public void setCustomer(@Nullable Humanoid humanoid) {
        this.setCustomer((EntityPlayer) humanoid);
    }

    // Data delegated methods


    @Override
    public CareerData getCareerData() {
        return new SpongeCareerData(getCareer());
    }

    @Override
    public Value<Career> career() {
        return new SpongeValue<>(Keys.CAREER, DataConstants.Catalog.CAREER_DEFAULT, getCareer());
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getCareerData());
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return this;
    }

    @Override
    public Optional<Villager> getCarrier() {
        return Optional.of(this);
    }
}
