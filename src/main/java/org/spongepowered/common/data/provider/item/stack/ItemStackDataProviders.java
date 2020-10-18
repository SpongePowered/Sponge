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
package org.spongepowered.common.data.provider.item.stack;

import org.spongepowered.common.data.provider.DataProviderRegistratorBuilder;
import org.spongepowered.common.util.Constants;

public final class ItemStackDataProviders extends DataProviderRegistratorBuilder {

    public ItemStackDataProviders() {
        super(Constants.Sponge.Entity.DataRegistration.ITEMSTACK);
    }

    @Override
    public void registerProviders() {
        AbstractBannerItemStackData.register(this.registrator);
        ArmorItemStackData.register(this.registrator);
        BlockItemStackData.register(this.registrator);
        BlockTypeItemStackData.register(this.registrator);
        BookItemStackData.register(this.registrator);
        BookPagesItemStackData.register(this.registrator);
        FireworkItemStackData.register(this.registrator);
        HideFlagsItemStackData.register(this.registrator);
        IDyeableArmorItemStackData.register(this.registrator);
        ItemStackData.register(this.registrator);
        MusicDiscItemStackData.register(this.registrator);
        PotionItemStackData.register(this.registrator);
        ShieldItemStackData.register(this.registrator);
        SignItemStackData.register(this.registrator);
        SkullItemStackData.register(this.registrator);
        TieredItemStackData.register(this.registrator);
        ToolItemStackData.register(this.registrator);
        WoolItemStackData.register(this.registrator);
    }
}
