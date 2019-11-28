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
package org.spongepowered.common.mixin.core.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIDoorInteract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.GrieferBridge;

@Mixin(EntityAIBreakDoor.class)
public abstract class EntityAIBreakDoorMixin extends EntityAIDoorInteract {

    public EntityAIBreakDoorMixin(final EntityLiving entityLiving) {
        super(entityLiving);
    }


    /**
     * @author gabizou - April 13th, 2018
     * @reason - Due to Forge's changes, there's no clear redirect or injection
     * point where Sponge can add the griefer checks. The original redirect aimed
     * at the gamerule check, but this can suffice for now.
     */
    @Redirect(
        method = "shouldExecute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/ai/EntityAIDoorInteract;shouldExecute()Z"
        )
    )
    private boolean spongeShouldExecuteForGriefer(final EntityAIDoorInteract entityAIDoorInteract) {
        return super.func_75250_a() && ((GrieferBridge) this.field_75356_a).bridge$CanGrief();

    }
}
