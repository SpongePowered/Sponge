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

import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.OcelotData;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.animal.Ocelot;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeOcelotData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSittingData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeEntityConstants;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.List;
import java.util.Random;

@Mixin(EntityOcelot.class)
public abstract class MixinEntityOcelot extends MixinEntityTameable implements Ocelot {

    @Shadow
    public abstract int getTameSkin();

    @Redirect(method = "processInteract", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0, remap = false))
    public int onTame(Random rand, int bound, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        int random = rand.nextInt(bound);
        if (random == 0) {
            stack.setCount(stack.getCount() + 1);
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().pushCause(player);
                if (!SpongeImpl.postEvent(SpongeEventFactory.createTameEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), this))) {
                    stack.setCount(stack.getCount() - 1);
                    return random;
                }
            }
        }
        return 1;
    }

    @Inject(method = "setupTamedAI", at = @At(value = "HEAD"), cancellable = true)
    public void onSetupTamedAi(CallbackInfo ci) {
        if (this.world.isRemote) {
            // Because ocelot AI tasks are added on the client, for whatever reason
            ci.cancel();
        }
    }

    // Data delegated methods

    @Override
    public OcelotData getOcelotData() {
        return new SpongeOcelotData(SpongeEntityConstants.OCELOT_IDMAP.get(this.getTameSkin()));
    }

    @Override
    public Value<OcelotType> variant() {
        return new SpongeValue<>(Keys.OCELOT_TYPE, DataConstants.Ocelot.DEFAULT_TYPE, SpongeEntityConstants.OCELOT_IDMAP.get(this.getTameSkin()));
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(new SpongeSittingData(this.shadow$isSitting()));
        manipulators.add(getOcelotData());
    }

    @Override
    public Translation getTranslation() {
        if (shadow$isTamed()) {
            return new SpongeTranslation("entity.Cat.name");
        }
        return super.getTranslation();
    }

}
