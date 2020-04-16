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
package org.spongepowered.server.launch.transformer.at;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

import org.checkerframework.checker.nullness.qual.Nullable;


enum AccessModifier {
    REMOVE_FINAL(0, true, null),
    PROTECTED_REMOVE_FINAL(ACC_PROTECTED, true, null),
    PROTECTED(ACC_PROTECTED, false, PROTECTED_REMOVE_FINAL),
    PUBLIC_REMOVE_FINAL(ACC_PUBLIC, true, null),
    PUBLIC(ACC_PUBLIC, false, PUBLIC_REMOVE_FINAL);

    private final int flag;
    private final boolean removeFinal;
    private final AccessModifier finalVariant;

    AccessModifier(int flag, boolean removeFinal, @Nullable AccessModifier finalVariant) {
        this.flag = flag;
        this.removeFinal = removeFinal;
        this.finalVariant = finalVariant != null ? finalVariant : this;
    }

    boolean hasAccessChange() {
        return this.flag != 0;
    }

    AccessModifier removeFinal() {
        return this.finalVariant;
    }

    AccessModifier merge(@Nullable AccessModifier modifier) {
        if (modifier == null || this == modifier) {
            return this;
        }

        if (this.flag == modifier.flag) {
            return this.removeFinal ? modifier.removeFinal() : modifier;
        }

        AccessModifier base = this.compareTo(modifier) > 0 ? this : modifier;
        return this.removeFinal ? base.removeFinal() : base;
    }

    int apply(int access) {
        if (hasAccessChange()) {
            // Don't allow lowering the access
            if (this.flag != ACC_PROTECTED || (access & ACC_PUBLIC) == 0) {
                // First remove the old access modifier, then add our new one
                access = ((access & ~7) | this.flag);
            }
        }

        if (this.removeFinal) {
            // Remove the final bit
            access &= ~ACC_FINAL;
        }

        return access;
    }

}
