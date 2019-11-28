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
package org.spongepowered.common.util;

import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListEntry;
import org.spongepowered.common.mixin.core.server.management.UserLIstEntryAccessor;
import org.spongepowered.common.mixin.core.server.management.UserListAccessor;

import java.io.IOException;

public class UserListUtils {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addEntry(final UserList list, final UserListEntry entry) {
        ((UserListAccessor) list).accessor$getValues().put(((UserListAccessor) list).accessor$getObjectKey(((UserLIstEntryAccessor) entry).accessor$getValue()), entry);

        try {
            list.func_152678_f();
        }
        catch (IOException ioexception) {
            UserListAccessor.accessor$getLogger().warn("Could not save the list after adding a user.", ioexception);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void removeEntry(final UserList list, final Object object) {
        ((UserListAccessor) list).accessor$getValues().remove(((UserListAccessor) list).accessor$getObjectKey(object));

        try {
            list.func_152678_f();
        }
        catch (IOException ioexception) {
            UserListAccessor.accessor$getLogger().warn("Could not save the list after removing a user.", ioexception);
        }
    }
}
