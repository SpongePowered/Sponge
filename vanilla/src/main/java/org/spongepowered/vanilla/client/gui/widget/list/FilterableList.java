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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.GroupNarration;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.vanilla.util.Bounds;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class FilterableList<P extends FilterableList<P, E>, E extends FilterableList.Entry<P, E>> extends AbstractSelectionList<E> {

    private static final Component USAGE_NARRATION = Component.translatable("narration.selection.usage");

    private final Screen screen;
    private Supplier<List<E>> filterSupplier;
    private Consumer<E> selectConsumer;
    protected final Font fontRenderer;
    protected E currentHoveredEntry;

    public FilterableList(final Screen screen, final int x, final int y, final int width, final int height, final int entryHeight) {
        super(Minecraft.getInstance(), width, height, y, entryHeight);
        // TODO height/screen.height?
        this.screen = screen;
        this.setX(x);
        this.fontRenderer = Minecraft.getInstance().font;
    }

    public Screen getScreen() {
        return this.screen;
    }

    public int getBottom() {
        return this.getY() + this.getHeight();
    }

    public E getCurrentHoveredEntry() {
        return this.currentHoveredEntry;
    }

    public P setCurrentHoveredEntry(final E currentHoveredEntry) {
        this.currentHoveredEntry = currentHoveredEntry;
        return (P) this;
    }

    @SafeVarargs
    public final P addEntries(final E... entries) {
        this.addEntries(Arrays.asList(entries));
        return (P) this;
    }

    public P addEntries(final List<E> entries) {
        entries.forEach(this::addEntry);
        return (P) this;
    }

    public Consumer<E> getSelectConsumer() {
        return this.selectConsumer;
    }

    public P setSelectConsumer(final Consumer<E> selectConsumer) {
        this.selectConsumer = selectConsumer;
        return (P) this;
    }

    public Supplier<List<E>> getFilterSupplier() {
        return this.filterSupplier;
    }

    public P setFilterSupplier(final Supplier<List<E>> filterSupplier) {
        this.filterSupplier = filterSupplier;
        return (P) this;
    }
//
//    public int getRowHeight() {
//        return this.itemHeight;
//    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    public int getRowLeft() {
        return this.getX() + 4;
    }

    @Override
    public void setSelected(@Nullable final E entry) {
        if (this.selectConsumer != null) {
            this.selectConsumer.accept(entry);
        }

        super.setSelected(entry);
    }


    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean repeatedClick) {
        final var mouseX = event.x();
        final var mouseY = event.y();
        final var button = event.button();
        this.updateScrolling(event);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            final E e = this.getEntryAtPosition(mouseX, mouseY);
            if (e != null) {
                if (e.mouseClicked(event, repeatedClick)) {
                    this.setFocused(e);
                    this.setDragging(true);
                    return true;
                }
            } else if (button == 0) {
                final var newEvent = new MouseButtonEvent(
                    (mouseX - (double) (this.getX() + this.width / 2 - this.getRowWidth() / 2)),
                    (mouseY - (double) this.getY()) + (int) this.scrollAmount() - 4,
                    event.buttonInfo());
                this.onClick(newEvent, repeatedClick);
                return true;
            }

            return true;
        }
    }

    // TODO: renderListItems was removed in pre-1 — rendering now handled by extractContent on entries
    // See .docs/plugin-gui-rewrite-plan.md

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationConsumer) {
        final @org.checkerframework.checker.nullness.qual.Nullable E hovered = this.getCurrentHoveredEntry();
        if (hovered != null) {
            GroupNarration.LIST.narratePosition(narrationConsumer, this.children(), hovered);
            hovered.updateNarration(narrationConsumer);
        } else {
            final E selected = this.getSelected();
            if (selected != null) {
                GroupNarration.LIST.narratePosition(narrationConsumer.nest(), this.children(), selected);
                selected.updateNarration(narrationConsumer);
            }
        }

        if (this.isFocused()) {
            narrationConsumer.add(NarratedElementType.USAGE, FilterableList.USAGE_NARRATION);
        }
    }

    public static abstract class Entry<P extends FilterableList<P, E>, E extends org.spongepowered.vanilla.client.gui.widget.list.FilterableList.Entry<P, E>> extends net.minecraft.client.gui.components.AbstractSelectionList.Entry<E> implements NarrationSupplier {

        private final P parentList;

        public Entry(final P parentList) {
            this.parentList = parentList;
        }

        public P getParentList() {
            return this.parentList;
        }

        public abstract Bounds getInteractBounds();

        @Override
        public void extractContent(final GuiGraphicsExtractor graphics, final boolean hovered) {
            // TODO: Implement rendering via GuiGraphicsExtractor (see .docs/plugin-gui-rewrite-plan.md)
        }
    }
}
