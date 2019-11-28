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
package org.spongepowered.common.registry;

import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public interface SpongeGameDictionaryEntry extends GameDictionary.Entry {

    ItemStack bridge$createDictionaryStack(int wildcardValue);

    public static final class Specific implements SpongeGameDictionaryEntry {

        private final Item type;
        private final int damage;

        public Specific(Item type, int damage) {
            this.type = Preconditions.checkNotNull(type, "type");
            this.damage = damage;
        }

        @Override
        public ItemType getType() {
            return (ItemType) this.type;
        }

        @Override
        public boolean matches(org.spongepowered.api.item.inventory.ItemStack stack) {
            return this.getType().equals(stack.getType()) && this.damage == ((ItemStack) stack).func_77952_i();
        }

        @Override
        public boolean isSpecific() {
            return true;
        }

        @Override
        public ItemStack bridge$createDictionaryStack(int wildcardValue) {
            return new ItemStack(this.type, 1, this.damage);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.type.hashCode();
            result = prime * result + this.damage;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof SpongeGameDictionaryEntry.Specific)) {
                return false;
            }
            SpongeGameDictionaryEntry.Specific other = (SpongeGameDictionaryEntry.Specific) obj;
            if (!this.type.equals(other.type)) {
                return false;
            }
            if (this.damage != other.damage) {
                return false;
            }
            return true;
        }

        @Override
        public ItemStackSnapshot getTemplate() {
            return ((org.spongepowered.api.item.inventory.ItemStack) 
                    new ItemStack(this.type, 1, this.damage)).createSnapshot();
        }
    }

    public static SpongeGameDictionaryEntry of(ItemStack stack, int wildcardValue) {
        if (stack.func_77952_i() == wildcardValue) {
            return (SpongeGameDictionaryEntry) stack.func_77973_b();
        }
        return new Specific(stack.func_77973_b(), stack.func_77952_i());
    }
}
