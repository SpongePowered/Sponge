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
package org.spongepowered.common.mixin.core.entity.living.villager;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.manipulator.mutable.entity.CareerData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.Professions;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.entity.SpongeCareer;
import org.spongepowered.common.entity.SpongeEntityMeta;
import org.spongepowered.common.interfaces.entity.IMixinVillager;
import org.spongepowered.common.mixin.core.entity.living.MixinEntityAgeable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityVillager.class)
public abstract class MixinEntityVillager extends MixinEntityAgeable implements Villager, IMixinVillager {

    @Shadow private boolean isPlaying;
    @Shadow private EntityPlayer buyingPlayer;
    @Shadow private int careerId;
    @Shadow private int careerLevel;
    @Shadow private MerchantRecipeList buyingList;

    @Shadow public abstract int getProfession();
    @Shadow public abstract void setProfession(int professionId);
    @Shadow public abstract void setCustomer(EntityPlayer player);
    @Shadow(prefix = "shadow$")
    public abstract EntityPlayer shadow$getCustomer();
    @Shadow public abstract MerchantRecipeList getRecipes(EntityPlayer player);

    private Profession profession;
    private Career spongeCareer;

    @SuppressWarnings("unchecked")
    @Inject(method = "setProfession(I)V", at = @At("RETURN"))
    public void onSetProfession(int professionId, CallbackInfo ci) {
        this.profession = ((List<? extends Profession>) Sponge.getGame().getRegistry().getAllOf(Profession.class)).get(professionId);
    }

    @Override
    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    @Override
    @Overwrite
    public boolean isTrading() {
        return this.buyingPlayer != null;
    }

    @Override
    public Career getCareer() {
        // TODO for some reason, there is some form of validation issue, because villager code
        // is the some really bad code....
        // At some point might write an overhaul of Villager for better handling
        // until then... there may be some exceptions thrown at random
        List<Career> careers = Sponge.getSpongeRegistry().getCareers(this.profession);
        if (this.careerId == 0 || this.careerId > careers.size()) {
            this.careerId = new Random().nextInt(careers.size()) + 1;
        }

        this.getRecipes(null);
        if (this.profession == Professions.PRIEST) {
            this.careerId = 1;
        }
        return ((List<Career>) Sponge.getGame().getRegistry().getCareers(this.profession)).get(this.careerId - 1);
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
    public Optional<Human> getCustomer() {
        return Optional.ofNullable((Human) this.shadow$getCustomer());
    }

    @Override
    public void setCustomer(@Nullable Human human) {
        this.setCustomer((EntityPlayer) human);
    }

    @Override
    public TradeOfferData getTradeOfferData() {
        return get(TradeOfferData.class).get();
    }

    @Override
    public CareerData getCareerData() {
        return get(CareerData.class).get();
    }
}
