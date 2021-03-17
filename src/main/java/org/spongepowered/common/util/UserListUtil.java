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

import java.io.IOException;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;
import org.spongepowered.common.accessor.server.players.StoredUserEntryAccessor;
import org.spongepowered.common.accessor.server.players.StoredUserListAccessor;

public final class UserListUtil {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <K, V extends StoredUserEntry<K>> V addEntry(final StoredUserList<K, V> list, final StoredUserEntry entry) {
        final V prev = ((StoredUserListAccessor<K, V>) list).accessor$map().put(((StoredUserListAccessor<K, V>) list).invoker$getKeyForUser(((StoredUserEntryAccessor<K>) entry).accessor$user()), (V) entry);

        try {
            list.save();
        } catch (final IOException e) {
            StoredUserListAccessor.accessor$LOGGER().warn("Could not save the list after adding a user.", e);
        }
        return prev;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V extends StoredUserEntry<K>> V removeEntry(final StoredUserList<K, V> list, final Object object) {
        final V prev = ((StoredUserListAccessor<K, V>) list).accessor$map().remove(((StoredUserListAccessor) list).invoker$getKeyForUser(object));

        try {
            list.save();
        } catch (final IOException e) {
            StoredUserListAccessor.accessor$LOGGER().warn("Could not save the list after removing a user.", e);
        }
        return prev;
    }

    private UserListUtil() {
    }
}
