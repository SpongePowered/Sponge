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
package org.spongepowered.common.entity.context;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.entity.Entity;

/**
 * Manages contextual entity data.
 */
public final class ContextManager {

    private static final Table<Entity, ContextViewer, DataContext> table = Tables.newCustomTable(
            new MapMaker().weakKeys().weakValues().makeMap(),
            () -> new MapMaker().weakKeys().weakValues().makeMap()
    );

    private ContextManager() {
    }

    /**
     * Gets a {@link DataContext} for a {@link ContextViewer} of
     * a {@link DataContextual}.
     *
     * @param contextual The contextual
     * @param viewer The viewer
     * @return The context
     */
    public static DataContext get(Entity contextual, ContextViewer viewer) {
        checkNotNull(contextual, "contextual");
        checkNotNull(viewer, "viewer");

        DataContext context = table.get(contextual, viewer);
        if (context == null) {
            context = new SpongeEntityContext(contextual, viewer);
            table.put(contextual, viewer, context);
        }

        return context;
    }

    /**
     * Tests if the {@link DataContextual} has a {@link DataContext} for
     * the {@link ContextViewer}.
     *
     * @param contextual The contextual
     * @param viewer The viewer
     * @return {@code true} if there is a context
     */
    public static boolean contains(Entity contextual, ContextViewer viewer) {
        checkNotNull(contextual, "contextual");
        checkNotNull(viewer, "viewer");

        return table.contains(contextual, viewer);
    }

    /**
     * Remove a {@link DataContextual}.
     *
     * @param contextual The contextual
     */
    public static void remove(Entity contextual) {
        checkNotNull(contextual, "contextual");

        table.rowMap().remove(contextual);
    }

    /**
     * Remove a {@link ContextViewer} from a {@link DataContextual}.
     *
     * @param contextual The contextual
     * @param viewer The viewer
     */
    public static void remove(Entity contextual, ContextViewer viewer) {
        checkNotNull(contextual, "contextual");
        checkNotNull(viewer, "viewer");

        table.remove(contextual, viewer);
    }

    /**
     * Remove a viewer from all {@link DataContextual}s.
     *
     * @param viewer The viewer
     */
    public static void remove(ContextViewer viewer) {
        checkNotNull(viewer, "viewer");

        table.columnMap().remove(viewer);
    }

}
