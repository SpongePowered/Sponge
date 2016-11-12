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
import org.spongepowered.common.item.inventory.adapter.impl.slots.OutputSlotAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.Lens;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.OrderedInventoryLensImpl;
import org.spongepowered.common.item.inventory.lens.impl.fabric.DefaultInventoryFabric;
import org.spongepowered.common.item.inventory.lens.impl.slots.OutputSlotLensImpl;
import org.spongepowered.common.mixin.core.entity.MixinEntityAgeable;
import org.spongepowered.common.registry.SpongeVillagerRegistry;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(EntityVillager.class)
@Implements({@Interface(iface = Villager.class, prefix = "villager$"), @Interface(iface = MinecraftInventoryAdapter.class, prefix = "inventory$")})
public abstract class MixinEntityVillager extends MixinEntityAgeable implements Villager, IMixinVillager, CarriedInventory {

    @Shadow private boolean fld_000361_bB; // isPlaying
    @Shadow private int fld_000370_bJ; // careerId
    @Shadow private int fld_000371_bK; // careerLevel
    @Shadow @Nullable private MerchantRecipeList fld_000364_bD; // buyingList
    @Shadow @Final private InventoryBasic fld_000374_bN; // villagerInventory

    @Shadow public abstract void mth_000379_g(int professionId); // setProfession
    @Shadow public abstract void setCustomer(EntityPlayer player);
    @Shadow public abstract boolean mth_000386_dk(); // isTrading
    @Shadow public abstract EntityPlayer mth_000385_s_(); // getCustomer
    @Shadow public abstract MerchantRecipeList getRecipes(EntityPlayer player);

    private Fabric<IInventory> fabric;
    private SlotCollection slots;
    private Lens<IInventory, ItemStack> lens;

    private Profession profession;

    @Inject(method = "mth_000379_g(I)V", at = @At("RETURN"))
    public void onSetProfession(int professionId, CallbackInfo ci) {
        this.profession = EntityUtil.validateProfession(professionId);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(CallbackInfo ci) {
        this.fabric = new DefaultInventoryFabric(this.fld_000374_bN);
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
        return this.fld_000361_bB;
    }

    @Override
    public void setPlaying(boolean playing) {
        this.fld_000361_bB = playing;
    }

    @Intrinsic
    public boolean villager$isTrading() {
        return this.mth_000386_dk();
    }

    @Override
    public Career getCareer() {
        List<Career> careers = (List<Career>) this.profession.getCareers();
        if (this.fld_000370_bJ == 0 || this.fld_000370_bJ > careers.size()) {
            this.fld_000370_bJ = new Random().nextInt(careers.size()) + 1;
        }
        this.getRecipes(null);
        return careers.get(this.fld_000370_bJ - 1);
    }

    @Override
    public Profession getProfession() {
        return this.profession;
    }

    @Override
    public void setCareer(Career career) {
        mth_000379_g(((SpongeEntityMeta) career.getProfession()).type);
        this.fld_000364_bD = null;
        this.fld_000370_bJ = ((SpongeCareer) career).type + 1;
        this.fld_000371_bK = 1;
        this.getRecipes(null);
    }

    @Override
    public Optional<Humanoid> getCustomer() {
        return Optional.ofNullable((Humanoid) this.mth_000385_s_());
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
    public void mth_000389_dt() { // populateBuyingList
        // Sponge
        List<Career> careers = (List<Career>) this.profession.getCareers();

        // EntityVillager.ITradeList[][][] aentityvillager$itradelist = DEFAULT_TRADE_LIST_MAP[this.getProfession()];

        if (this.fld_000370_bJ != 0 && this.fld_000371_bK != 0) {
            ++this.fld_000371_bK;
        } else {
            // Sponge change aentityvillager$itradelist to use this.profession.getCareers()
            this.fld_000370_bJ = this.rand.nextInt(careers.size()) + 1;
            this.fld_000371_bK = 1;
        }

        if (this.fld_000364_bD == null) {
            this.fld_000364_bD = new MerchantRecipeList();
        }

        // Sponge start - use our own registry stuffs
        checkState(this.fld_000370_bJ <= careers.size(), "The villager career id is out of bounds fo the available Careers! Found: " + this.fld_000370_bJ
                                                    + " when the current maximum is: " + careers.size());
        final Career fld_000370_bJ = careers.get(this.fld_000370_bJ - 1);
        SpongeVillagerRegistry.getInstance().populateOffers(this, (List<TradeOffer>) (List<?>) this.fld_000364_bD, fld_000370_bJ, this.fld_000371_bK, this.rand);
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
        return (CarriedInventory<? extends Carrier>) this;
    }
}
