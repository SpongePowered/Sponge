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
package org.spongepowered.common.data.datasync.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.datasync.DataParameterConverter;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;

import java.util.List;
import java.util.Optional;

public class EntityFlagsConverter extends DataParameterConverter<Byte> {

    public EntityFlagsConverter() {
        super(EntityAccessor.accessor$getFlagsParameter());
    }

    @Override
    public Optional<DataTransactionResult> createTransaction(final Entity entity, final Byte currentValue, final Byte value) {
        // TODO - on fire and elytra are not represented in the API, maybe we should?
        final boolean onFire = getFlag(currentValue, ON_FIRE_MASK);
        final boolean isSneaking = getFlag(currentValue, CROUCHED_MASK);
        final boolean sprinting = getFlag(currentValue, SPRINTING_MASK);
        final boolean invisible = getFlag(currentValue, INVISIBLE_MASK);
        final boolean glowing = getFlag(currentValue, GLOWING_MASK);
        final boolean elytra = getFlag(currentValue, FLYING_ELYTRA_MASK);

        final boolean newOnFire = getFlag(value, ON_FIRE_MASK);
        final boolean newIsSneaking = getFlag(value, CROUCHED_MASK);
        final boolean newSprinting = getFlag(value, SPRINTING_MASK);
        final boolean newInvisible = getFlag(value, INVISIBLE_MASK);
        final boolean newGlowing = getFlag(value, GLOWING_MASK);
        final boolean newElytra = getFlag(value, FLYING_ELYTRA_MASK);

        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        boolean changed = false;
        if (isSneaking != newIsSneaking) {
            builder.replace(ImmutableSpongeValue.cachedOf(Keys.IS_SNEAKING, false, isSneaking));
            builder.success(ImmutableSpongeValue.cachedOf(Keys.IS_SNEAKING, false, newIsSneaking));
            changed = true;
        }
        if (sprinting != newSprinting) {
            builder.replace(ImmutableSpongeValue.cachedOf(Keys.IS_SPRINTING, false, sprinting));
            builder.success(ImmutableSpongeValue.cachedOf(Keys.IS_SPRINTING, false, newSprinting));
            changed = true;
        }
        if (invisible != newInvisible) {
            builder.replace(ImmutableSpongeValue.cachedOf(Keys.INVISIBLE, false, invisible));
            builder.success(ImmutableSpongeValue.cachedOf(Keys.INVISIBLE, false, newInvisible));
            changed = true;
        }
        if (glowing != newGlowing) {
            builder.replace(ImmutableSpongeValue.cachedOf(Keys.GLOWING, false, glowing));
            builder.success(ImmutableSpongeValue.cachedOf(Keys.GLOWING, false, newGlowing));
            changed = true;
        }
        if (elytra != newElytra) {
            builder.replace(ImmutableSpongeValue.cachedOf(Keys.IS_ELYTRA_FLYING, false, elytra));
            builder.success(ImmutableSpongeValue.cachedOf(Keys.IS_ELYTRA_FLYING, false, newElytra));
            changed = true;
        }
        builder.result(DataTransactionResult.Type.SUCCESS);
        return changed ? Optional.of(builder.build()) : Optional.empty();
    }

    @Override
    public Byte getValueFromEvent(final Byte originalValue, final List<ImmutableValue<?>> immutableValues) {
        if (immutableValues.isEmpty()) {
            // Short circuit when there are no changes.
            return originalValue;
        }
        final boolean onFire = getFlag(originalValue, ON_FIRE_MASK);
        boolean newIsSneaking = getFlag(originalValue, CROUCHED_MASK);
        boolean newSprinting = getFlag(originalValue, SPRINTING_MASK);
        boolean newInvisible = getFlag(originalValue, INVISIBLE_MASK);
        boolean newGlowing = getFlag(originalValue, GLOWING_MASK);
        boolean newElytra = getFlag(originalValue, FLYING_ELYTRA_MASK);
        for (final ImmutableValue<?> immutableValue : immutableValues) {
            if (immutableValue.getKey() == Keys.IS_SNEAKING) {
                newIsSneaking = ((Boolean) immutableValue.get());
            }
            if (immutableValue.getKey() == Keys.IS_SPRINTING) {
                newSprinting = ((Boolean) immutableValue.get());
            }
            if (immutableValue.getKey() == Keys.INVISIBLE) {
                newInvisible = (Boolean) immutableValue.get();
            }
            if (immutableValue.getKey() == Keys.GLOWING) {
                newGlowing = (Boolean) immutableValue.get();
            }
            if (immutableValue.getKey() == Keys.IS_ELYTRA_FLYING) {
                newElytra = (Boolean) immutableValue.get();
            }
        }
        byte newValue = (byte) (onFire ?  ON_FIRE_MASK : 0);
        newValue = (byte) (newIsSneaking ? newValue | CROUCHED_MASK : newValue & ~CROUCHED_MASK);
        newValue = (byte) (newSprinting ? newValue | SPRINTING_MASK : newValue & ~SPRINTING_MASK);
        newValue = (byte) (newInvisible ? newValue | INVISIBLE_MASK : newValue & ~INVISIBLE_MASK);
        newValue = (byte) (newGlowing ? newValue | GLOWING_MASK : newValue & ~GLOWING_MASK);
        newValue = (byte) (newElytra ? newValue | FLYING_ELYTRA_MASK : newValue & ~FLYING_ELYTRA_MASK);
        return newValue;
    }

    private boolean getFlag(final byte value, final int mask) {
        return (value & mask) != 0;
    }

    public static final int ON_FIRE_MASK        = 0b00000001; // 0x01
    public static final int CROUCHED_MASK       = 0b00000010; // 0x02
    public static final int UNUSED_MASK         = 0b00000100; // 0x04
    public static final int SPRINTING_MASK      = 0b00001000; // 0x08
    public static final int UNUSED_2_MASK       = 0b00010000; // 0x10
    public static final int INVISIBLE_MASK      = 0b00100000; // 0x20
    public static final int GLOWING_MASK        = 0b01000000; // 0x40
    public static final int FLYING_ELYTRA_MASK  = 0b10000000; // 0x80
}
