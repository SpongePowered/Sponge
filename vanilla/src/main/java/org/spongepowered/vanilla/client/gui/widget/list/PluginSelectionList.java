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
package org.spongepowered.vanilla.client.gui.widget.list;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.vanilla.util.Bounds;

public final class PluginSelectionList extends FilterableList<PluginSelectionList, PluginSelectionList.Entry> {

    public PluginSelectionList(final Screen screen, final int x, final int y, final int width, final int height, final int entryHeight) {
        super(screen, x, y, width, height, entryHeight);
    }

    public static final class Entry extends FilterableList.Entry<PluginSelectionList, org.spongepowered.vanilla.client.gui.widget.list.PluginSelectionList.Entry> {

        public final PluginMetadata metadata;
        private final PluginSelectionList list;

        public Entry(final PluginSelectionList list, final PluginMetadata metadata) {
            super(list);
            this.list = list;
            this.metadata = metadata;
        }

        @Override
        public Bounds getInteractBounds() {
            return new Bounds(0, this.getParentList().getRowWidth(), 0, this.getParentList().scrollerHeight());
        }

        @Override
        public void extractContent(final GuiGraphicsExtractor graphics, final boolean hovered) {
            // TODO: Implement rendering via GuiGraphicsExtractor (see .docs/plugin-gui-rewrite-plan.md)
            // Should draw: plugin name (or ID), and ID below if name is present
        }

        @Override
        public boolean mouseClicked(final MouseButtonEvent event, final boolean repeated) {
            this.getParentList().setSelected(this);
            return true;
        }

        @Override
        public void updateNarration(final NarrationElementOutput var1) {
            var1.add(NarratedElementType.TITLE, this.metadata.name().orElse(this.metadata.id()));
        }
    }
}
