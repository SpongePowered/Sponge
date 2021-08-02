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
package org.spongepowered.common.user;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

// Used to reduce the number of calls to maps.
final class SpongeUserMutableWatchEvent {

    private WatchEvent.Kind<?> kind = null;

    public WatchEvent.Kind<?> get() {
        return this.kind;
    }

    public void set(WatchEvent.Kind<?> kind) {
        if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            // This should never happen, we don't listen to this.
            // However, if it does, treat it as a create, because it
            // infers the existence of the file.
            kind = StandardWatchEventKinds.ENTRY_CREATE;
        }

        if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_DELETE) {
            if (this.kind != null && this.kind != kind) {
                this.kind = null;
            } else {
                this.kind = kind;
            }
        }
    }

}
