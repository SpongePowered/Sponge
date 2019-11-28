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
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.bridge.packet.DataParameterBridge;

import java.util.Map;
import java.util.Optional;

@Mixin(EntityDataManager.class)
public abstract class EntityDataManagerMixin {

    // This overrides the setter for the entries of the
    // data manager to use a "faster" map.
    @SuppressWarnings("unused")
    @Shadow @Final @Mutable private Map < Integer, EntityDataManager.DataEntry<? >> entries = new Int2ObjectOpenHashMap<>();

    // The rest is actually used in the overwrite below.
    @Shadow @Final private Entity entity;
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Overwrite
    public <T> void set(final DataParameter<T> key, T value) {
        final EntityDataManager.DataEntry<T> dataentry = this.<T>getEntry(key);

        // Sponge Start - set up the current value, so we don't have to retrieve it multiple times
        final T currentValue = dataentry.func_187206_b();
        final T incomingValue = value;
        if (ObjectUtils.notEqual(value, currentValue)) { // Sponge - change dataentry.getValue() to use local variable
            // Sponge Start - retrieve the associated key, if available
            // Client side can have an entity, because reasons.......
            // Really silly reasons......
            // I don't know, ask Grum....
            if (this.entity != null && this.entity.field_70170_p != null && !this.entity.field_70170_p.field_72995_K) { // We only want to spam the server world ;)
                final Optional<DataParameterConverter<T>> converter = ((DataParameterBridge) key).bridge$getDataConverter();
                // At this point it is changing
                if (converter.isPresent()) {
                    // Ok, we have a key ready to use the converter
                    final Optional<DataTransactionResult> optional = converter.get().createTransaction(this.entity, currentValue, value);
                    if (optional.isPresent()) {
                        // Only need to make a transaction if there are actual changes necessary.
                        final DataTransactionResult transaction = optional.get();
                        final ChangeDataHolderEvent.ValueChange
                            event =
                            SpongeEventFactory.createChangeDataHolderEventValueChange(Sponge.getCauseStackManager().getCurrentCause(), transaction,
                                (DataHolder) this.entity);
                        Sponge.getEventManager().post(event);
                        if (event.isCancelled()) {
                            //If the event is cancelled, well, don't change the underlying value.
                            return;
                        }
                        try {
                            value = converter.get().getValueFromEvent(currentValue, event.getEndResult().getSuccessfulData());
                        } catch (Exception e) {
                            // Worst case scenario, we don't want to cause an issue, so we just set the value
                            value = incomingValue;
                        }
                    }
                }
            }
            // Sponge End
            dataentry.func_187210_a(value);
            this.entity.func_184206_a(key);
            dataentry.func_187208_a(true);
            this.dirty = true;
        }
    }
}
