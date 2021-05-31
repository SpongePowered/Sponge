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
package org.spongepowered.common.mixin.api.mcp.world.entity.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.mixin.api.mcp.world.entity.LivingEntityMixin_API;

@Mixin(net.minecraft.world.entity.player.Player.class)
public abstract class PlayerMixin_API extends LivingEntityMixin_API implements Player {

    // @formatter:off
    @Shadow public AbstractContainerMenu containerMenu;
    @Shadow @Final public Abilities abilities;
    @Shadow @Final public Inventory inventory;
    @Shadow public abstract ItemCooldowns shadow$getCooldowns();
    @Shadow public abstract Component shadow$getDisplayName();
    @Shadow public abstract Component shadow$getName();
    // @formatter:on

    public final boolean impl$isFake = ((PlatformEntityBridge) this).bridge$isFakePlayer();

    @Override
    public String name() {
        return this.shadow$getName().getString();
    }

}
