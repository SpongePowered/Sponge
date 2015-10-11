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
package org.spongepowered.common.data.builder.manipulator.mutable.item;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeEnchantmentData;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;
import java.util.Optional;

public class ItemEnchantmentDataBuilder implements DataManipulatorBuilder<EnchantmentData, ImmutableEnchantmentData> {

    private final SerializationService serializationService;

    public ItemEnchantmentDataBuilder(SerializationService serializationService) {
        this.serializationService = checkNotNull(serializationService);
    }

    @Override
    public EnchantmentData create() {
        return new SpongeEnchantmentData();
    }

    @Override
    public Optional<EnchantmentData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            if (!((ItemStack) dataHolder).isItemEnchanted()) {
                return Optional.empty();
            } else {
                final List<ItemEnchantment> enchantments = NbtDataUtil.getItemEnchantments((ItemStack) dataHolder);
                return Optional.of(new SpongeEnchantmentData(enchantments));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<EnchantmentData> build(DataView container) throws InvalidDataException {
        checkDataExists(container, Keys.ITEM_ENCHANTMENTS.getQuery());
        final List<ItemEnchantment> enchantments = container.getSerializableList(Keys.ITEM_ENCHANTMENTS.getQuery(),
                                                                                 ItemEnchantment.class,
                                                                                 this.serializationService).get();
        return Optional.of(new SpongeEnchantmentData(enchantments));
    }
}
