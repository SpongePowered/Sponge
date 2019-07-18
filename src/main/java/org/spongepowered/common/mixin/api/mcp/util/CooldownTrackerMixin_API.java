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
package org.spongepowered.common.mixin.api.mcp.util;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.Item;
import net.minecraft.util.CooldownTracker;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.util.CooldownTracker$CooldownBridge;
import org.spongepowered.common.bridge.util.CooldownTrackerBridge;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@Mixin(CooldownTracker.class)
public abstract class CooldownTrackerMixin_API implements org.spongepowered.api.entity.living.player.CooldownTracker {

    @Shadow @Final private Map<Item, ?> cooldowns;
    @Shadow private int ticks;

    @Shadow public abstract boolean hasCooldown(Item itemIn);
    @Shadow public abstract float getCooldown(Item itemIn, float partialTicks);
    @Shadow public abstract void setCooldown(final Item item, final int ticks);

    @Override
    public boolean hasCooldown(final ItemType type) {
        checkNotNull(type, "Item type cannot be null!");
        return hasCooldown((Item) type);
    }

    @Override
    public OptionalInt getCooldown(final ItemType type) {
        checkNotNull(type, "Item type cannot be null!");

        final CooldownTracker$CooldownBridge cooldown = (CooldownTracker$CooldownBridge) this.cooldowns.get((Item) type);

        if (cooldown != null) {
            final int remainingCooldown = cooldown.bridge$getExpireTicks() - this.ticks;
            if (remainingCooldown > 0) {
                return OptionalInt.of(remainingCooldown);
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public boolean setCooldown(final ItemType type, final int ticks) {
        checkNotNull(type, "Item type cannot be null!");
        this.setCooldown((Item) type, ticks);
        return ((CooldownTrackerBridge) this).bridge$getSetCooldownResult();
    }


    @Override
    public boolean resetCooldown(final ItemType type) {
        return setCooldown(type, 0);
    }

    @Override
    public OptionalDouble getFractionRemaining(final ItemType type) {
        checkNotNull(type, "Item type cannot be null!");
        final float cooldown = getCooldown((Item) type, 0);

        if (cooldown > 0.0F) {
            return OptionalDouble.of(cooldown);
        }
        return OptionalDouble.empty();
    }


}
