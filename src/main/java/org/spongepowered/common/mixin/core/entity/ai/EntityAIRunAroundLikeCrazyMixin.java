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

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIRunAroundLikeCrazy;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.dismount.DismountTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.EntityBridge;

@Mixin(EntityAIRunAroundLikeCrazy.class)
public abstract class EntityAIRunAroundLikeCrazyMixin extends EntityAIBaseMixin {

    @Shadow @Final @Mutable private AbstractHorse horseHost;

    /**
     * @author rexbut - December 16th, 2016
     *
     * @reason - adjusted to support {@link DismountTypes}
     */
    @Overwrite
    public void updateTask() {
        if (this.horseHost.func_70681_au().nextInt(50) == 0) {
            Entity entity = this.horseHost.func_184188_bt().get(0);

            if (entity == null) {
                return;
            }

            if (entity instanceof EntityPlayer) {
                int i = this.horseHost.func_110252_cg();
                int j = this.horseHost.func_190676_dC();

                if (j > 0 && this.horseHost.func_70681_au().nextInt(j) < i) {
                    try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.pushCause(entity);
                        if (SpongeImpl.postEvent(SpongeEventFactory.createTameEntityEvent(frame.getCurrentCause(), (Horse) this.horseHost))) {
                            return;
                        }
                    }
                    this.horseHost.func_110263_g((EntityPlayer)entity);
                    this.horseHost.field_70170_p.func_72960_a(this.horseHost, (byte)7);
                    return;
                }

                this.horseHost.func_110198_t(5);
            }

            // Sponge start - Throw an event before calling entity states
            // this.horseHost.removePassengers(); // Vanilla
            if (((EntityBridge) this.horseHost).bridge$removePassengers(DismountTypes.DERAIL)) {
                // Sponge end
                this.horseHost.func_190687_dF();
                this.horseHost.field_70170_p.func_72960_a(this.horseHost, (byte)6);
            }
        }
    }
}
