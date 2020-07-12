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

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.vanilla.util.Bounds;

public final class PluginSelectionList extends FilterableList<PluginSelectionList, PluginSelectionList.Entry> {

    public PluginSelectionList(final Screen screen, final int x, final int y, final int width, final int height, final int entryHeight) {
        super(screen, x, y, width, height, entryHeight);
    }

    public static final class Entry extends FilterableList.Entry<PluginSelectionList, Entry> {

        public final PluginMetadata metadata;
        private final PluginSelectionList list;

        public Entry(final PluginSelectionList list, final PluginMetadata metadata) {
            super(list);
            this.list = list;
            this.metadata = metadata;
        }

        @Override
        public Bounds getInteractBounds() {
            return new Bounds(0, this.getParentList().getRowWidth(), 0, this.getParentList().getRowHeight());
        }

        @Override
        public void render(final int p_render_1_, final int renderY, final int renderX, final int p_render_4_, final int p_render_5_,
            final int mouseX, final int mouseY, final boolean p_render_8_, final float p_render_9_) {
            final Screen screen = this.getParentList().getScreen();
            // Draw the name, or ID if name is not present
            screen.drawString(this.list.fontRenderer, this.metadata.getName().orElse(this.metadata.getId()), renderX + 2, renderY + 1, 16777215);

            // Draw the ID if the name is present
            if (this.metadata.getName().isPresent()) {
                screen.drawString(this.list.fontRenderer, this.metadata.getId(), renderX + 2, renderY + 12, 8421504);
            }
        }

        @Override
        public boolean mouseClicked(final double p_mouseClicked_1_, final double p_mouseClicked_3_, final int p_mouseClicked_5_) {
            this.getParentList().setSelected(this);
            return true;
        }
    }
}
