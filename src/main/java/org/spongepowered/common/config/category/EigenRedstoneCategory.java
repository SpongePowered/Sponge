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
package org.spongepowered.common.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class EigenRedstoneCategory extends ConfigCategory {

    @Setting(value = "enabled", comment = ""
            + "If 'true', uses theosib's redstone implementation which improves performance.\n"
            + "See https://bugs.mojang.com/browse/MC-11193 and\n "
            + "    https://bugs.mojang.com/browse/MC-81098 for more information.\n"
            + "Note: We cannot guarantee compatibility with mods. Use at your discretion.")
    private boolean isEnabled = false;

    @Setting(value = "vanilla-search",
            comment = "If 'true', restores the vanilla algorithm for propagating redstone wire changes.")
    private boolean vanillaSearch = false;

    @Setting(value = "vanilla-decrement",
            comment = "If 'true', restores the vanilla algorithm for computing wire power levels when powering off.")
    private boolean vanillaDecrement = false;

    public EigenRedstoneCategory() {
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public boolean vanillaSearch() {
        return this.vanillaSearch;
    }

    public boolean vanillaDecrement() {
        return this.vanillaDecrement;
    }

}
