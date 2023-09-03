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
package org.spongepowered.forge.util;

import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.api.util.Tristate;

public final class TristateUtil {

    public static Tristate fromEventResult(final Event.Result result) {
        switch (result) {
            case DENY:
                return Tristate.FALSE;
            case DEFAULT:
                return Tristate.UNDEFINED;
            case ALLOW:
                return Tristate.TRUE;
            default:
                throw new IllegalArgumentException("Event.Result " + result);
        }
    }

    public static Event.Result toEventResult(final Tristate tristate) {
        switch (tristate) {
            case TRUE:
                return Event.Result.ALLOW;
            case FALSE:
                return Event.Result.DENY;
            case UNDEFINED:
                return Event.Result.DEFAULT;
            default:
                throw new IllegalArgumentException("Tristate " + tristate);
        }
    }

    private TristateUtil() {
    }
}
