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
package org.spongepowered.common.mixin.core.network.datasync;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import org.apache.commons.lang3.ObjectUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.interfaces.network.datasync.IMixinEntityDataManager$DataEntry;

import java.util.Map;
import java.util.Optional;

@Mixin(EntityDataManager.class)
public abstract class MixinEntityDataManager {

    @Shadow @Final @Mutable public Map < Integer, EntityDataManager.DataEntry<? >> entries = new Int2ObjectOpenHashMap<>();

    @Shadow @Final protected Entity entity;
    @Shadow private boolean dirty;

    @Shadow protected abstract <T> EntityDataManager.DataEntry<T> getEntry(DataParameter<T> key);

    /**
     * @author gabizou December 27th, 2017
     * @reason Inject ChangeValueEvent for entities by utilizing keys. Keys are registered
     *     based on the entity of which they belong to. This is to make the events more streamlined
     *     with regards to "when" they are being changed.
     * @param key The parameter key
     * @param value The value
     * @param <T> The type of value
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public <T> void set(DataParameter<T> key, T value) {
        EntityDataManager.DataEntry<T> dataentry = this.<T>getEntry(key);

        // Sponge Start - set up the current value, so we don't have to retrieve it multiple times
        final T currentValue = dataentry.getValue();
        if (ObjectUtils.notEqual(value, currentValue)) { // Sponge - change dataentry.getValue() to use local variable
            // Sponge Start - retrieve the associated key, if available
            final IMixinEntityDataManager$DataEntry mixinEntry = (IMixinEntityDataManager$DataEntry) dataentry;
            final Optional<Key<?>> relatedKey = mixinEntry.getRelatedKey();
            // At this point it is changing
            if (relatedKey.isPresent()) {

                // Ok, we have a key ready to use
                final Key<?> spongeKey = relatedKey.get();
                final DataTransactionResult transaction = DataTransactionResult.builder()
                    // Use the entry to soft convert the value if necessary,
                    // sometimes some keys are native to minecraft types and others are not
                    // like DyeColors are native, but health is oging to be a conversion from
                    // float -> double
                    .replace(mixinEntry.createValue(currentValue))
                    .success(mixinEntry.createValue(value))
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();
                final ChangeDataHolderEvent.ValueChange
                    event =
                    SpongeEventFactory.createChangeDataHolderEventValueChange(Sponge.getCauseStackManager().getCurrentCause(), transaction,
                        (DataHolder) this.entity);
                Sponge.getEventManager().post(event);
                if (event.isCancelled()) {
                    //If the event is cancelled, well, don't change the underlying value.
                    return;
                }
                for (ImmutableValue<?> immutableValue : event.getEndResult().getSuccessfulData()) {
                    if (immutableValue.getKey() == spongeKey) {
                        value = mixinEntry.getValueFromEvent(immutableValue);
                    }
                }
            }
            // Sponge End
            dataentry.setValue(value);
            this.entity.notifyDataManagerChange(key);
            dataentry.setDirty(true);
            this.dirty = true;
        }
    }
}
