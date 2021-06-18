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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.LockCode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.api.block.entity.carrier.NameableCarrierBlockEntity;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.LockCodeAccessor;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(BaseContainerBlockEntity.class)
public abstract class BaseContainerBlockEntityMixin_API extends BlockEntityMixin_API implements NameableCarrierBlockEntity {

    // @formatter:off
    @Shadow @Nullable private LockCode lockKey;
    @Nullable @Shadow private Component name; // Not really nullable, but it should be... - Roquette
    // @formatter:off

    @Override
    public DataContainer toContainer() {
        final DataContainer container = super.toContainer();
        if (this.lockKey != null) {
            container.set(Constants.TileEntity.LOCK_CODE, ((LockCodeAccessor) this.lockKey).accessor$key());
        }
        final List<DataView> items = Lists.newArrayList();
        for (int i = 0; i < ((Container) this).getContainerSize(); i++) {
            final ItemStack stack = ((Container) this).getItem(i);
            if (!stack.isEmpty()) {
                // todo make a helper object for this
                final DataContainer stackView = DataContainer.createNew()
                    .set(Queries.CONTENT_VERSION, 1)
                    .set(Constants.TileEntity.SLOT, i)
                    .set(Constants.TileEntity.SLOT_ITEM, ((org.spongepowered.api.item.inventory.ItemStack) (Object) stack).toContainer());
                items.add(stackView);
            }
        }
        if (this.name != null) {
            container.set(Constants.TileEntity.LOCKABLE_CONTAINER_CUSTOM_NAME, this.name);
        }
        container.set(Constants.TileEntity.ITEM_CONTENTS, items);
        return container;
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.displayName().asImmutable());

        this.lockToken().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
