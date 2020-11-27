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
package org.spongepowered.common.mixin.api.mcp.entity.player;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.CooldownTracker;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.hooks.SpongeImplHooks;
import org.spongepowered.common.mixin.api.mcp.entity.LivingEntityMixin_API;

@Mixin(PlayerEntity.class)
@Implements(@Interface(iface = Player.class, prefix = "api$"))
public abstract class PlayerEntityMixin_API extends LivingEntityMixin_API {

    @Shadow public Container openContainer;
    @Shadow public float experience;
    @Shadow @Final public PlayerAbilities abilities;
    @Shadow @Final public PlayerInventory inventory;
    @Shadow public abstract CooldownTracker shadow$getCooldownTracker();
    @Shadow public abstract ITextComponent shadow$getDisplayName();
    @Shadow public abstract ITextComponent shadow$getName();

    final boolean impl$isFake = SpongeImplHooks.isFakePlayer((PlayerEntity) (Object) this);

    @Intrinsic
    public String api$getName() {
        return this.shadow$getName().getString();
    }

}
