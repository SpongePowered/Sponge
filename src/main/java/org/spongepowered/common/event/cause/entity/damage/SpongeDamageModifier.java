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
package org.spongepowered.common.event.cause.entity.damage;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageStepType;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public record SpongeDamageModifier(DamageStepType type, Optional<Consumer<CauseStackManager.StackFrame>> frameModifier, Optional<Function> damageFunction) implements DamageModifier {

    public static class Builder implements DamageModifier.Builder {
        private @Nullable DamageStepType type;
        private @Nullable Consumer<CauseStackManager.StackFrame> frame;
        private Function function;

        public Builder() {
            this.reset();
        }

        @Override
        public DamageModifier.Builder reset() {
            this.type = null;
            this.frame = null;
            this.function = (step, damage) -> damage;
            return this;
        }

        @Override
        public DamageModifier.Builder type(DamageStepType type) {
            this.type = Objects.requireNonNull(type, "type");
            return this;
        }

        @Override
        public DamageModifier.Builder frameModifier(Consumer<CauseStackManager.StackFrame> frame) {
            this.frame = Objects.requireNonNull(frame, "frame");
            return this;
        }

        @Override
        public DamageModifier.Builder damageFunction(Function function) {
            this.function = Objects.requireNonNull(function, "function");
            return this;
        }

        @Override
        public DamageModifier build() {
            if (this.type == null) {
                throw new IllegalStateException("type must be set");
            }
            return new SpongeDamageModifier(this.type, Optional.ofNullable(this.frame), Optional.ofNullable(this.function));
        }
    }
}
