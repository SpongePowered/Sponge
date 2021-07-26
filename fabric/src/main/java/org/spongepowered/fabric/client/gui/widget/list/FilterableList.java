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
package org.spongepowered.fabric.client.gui.widget.list;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.common.accessor.client.gui.components.AbstractSelectionListAccessor;
import org.spongepowered.fabric.util.Bounds;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class FilterableList<P extends FilterableList<P, E>, E extends FilterableList.Entry<P, E>> extends AbstractSelectionList<E> {

    private final Screen screen;
    private Supplier<List<E>> filterSupplier;
    private Consumer<E> selectConsumer;
    protected final Font fontRenderer;
    protected E currentHoveredEntry;

    public FilterableList(final Screen screen, final int x, final int y, final int width, final int height, final int entryHeight) {
        super(Minecraft.getInstance(), width, screen.height, y, y + height, entryHeight);
        this.screen = screen;
        this.x0 = x;
        this.x1 = x + width;
        this.fontRenderer = Minecraft.getInstance().font;
    }

    public Screen getScreen() {
        return this.screen;
    }

    public int getX() {
        return this.x0;
    }

    public P setX(final int x) {
        this.x0 = x;
        return (P) this;
    }

    public int getY() {
        return this.y0;
    }

    public P setY(final int y) {
        this.y0 = y;
        return (P) this;
    }

    public P setPosition(final int x, final int y) {
        this.x0 = x;
        this.y0 = y;
        return (P) this;
    }

    public int getWidth() {
        return this.width;
    }

    public P setWidth(final int width) {
        this.width = width;
        return (P) this;
    }

    public int getHeight() {
        return this.height;
    }

    public P setHeight(final int height) {
        this.height = height;
        return (P) this;
    }

    public P setSize(final int width, final int height) {
        this.width = width;
        this.height = height;
        return (P) this;
    }

    public P setBounds(final int x, final int y, final int width, final int height) {
        this.x0 = x;
        this.y0 = y;
        this.width = width;
        this.height = height;
        return (P) this;
    }

    public int getRight() {
        return this.x0 + this.width;
    }

    public int getBottom() {
        return this.y0 + this.headerHeight;
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

    // Because private
    private int getRowBottom(final int p_getRowBottom_1_) {
        return this.getRowTop(p_getRowBottom_1_) + this.itemHeight;
    }

    public int getRowHeight() {
        return this.itemHeight;
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    public int getRowLeft() {
        return this.x0 + 4;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    @Override
    public void setSelected(@Nullable final E entry) {
        if (this.selectConsumer != null) {
            this.selectConsumer.accept(entry);
        }

        super.setSelected(entry);
    }

    @Override
    public void render(final PoseStack stack, final int p_render_1_, final int p_render_2_, final float p_render_3_) {
        super.render(stack, p_render_1_, p_render_2_, p_render_3_);
    }

    @Override
    public boolean mouseClicked(final double p_mouseClicked_1_, final double p_mouseClicked_3_, final int p_mouseClicked_5_) {
        this.updateScrollingState(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        if (!this.isMouseOver(p_mouseClicked_1_, p_mouseClicked_3_)) {
            return false;
        } else {
            final E e = this.getEntryAtPosition(p_mouseClicked_1_, p_mouseClicked_3_);
            if (e != null) {
                if (e.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
                    this.setFocused(e);
                    this.setDragging(true);
                    return true;
                }
            } else if (p_mouseClicked_5_ == 0) {
                this.clickedHeader((int) (p_mouseClicked_1_ - (double) (this.x0 + this.width / 2 - this.getRowWidth() / 2)),
                    (int) (p_mouseClicked_3_ - (double) this.y0) + (int) this.getScrollAmount() - 4);
                return true;
            }

            return true;
        }
    }

    @Override
    protected void renderList(final PoseStack stack, final int renderX, final int renderY, final int p_renderList_3_, final int p_renderList_4_,
            final float p_renderList_5_) {
        // Most of this is based on AbstractList::renderList logic
        final List<E> filteredList = this.filterSupplier == null ? new ObjectArrayList<>(this.children()) : this.filterSupplier.get();
        final int itemCount = filteredList.size();
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();

        if (filteredList.isEmpty()) {
            final Font font = this.minecraft.font;
            final String noResults = "No results...";
            final int noResultsWidth = font.width(noResults);

            font.draw(stack, noResults, (this.width / 2) + this.x0 - (noResultsWidth / 2), this.y0 + 10, ChatFormatting.GRAY.getColor());

            return;
        }

        for (int i = 0; i < itemCount; ++i) {
            final int rowTop = this.getRowTop(i);
            final int rowBottom = this.getRowBottom(i);
            if (rowBottom >= this.y0 && rowTop <= this.y1) {
                final int yStart = renderY + i * this.itemHeight + this.headerHeight;
                final int yEnd = this.itemHeight - 4;

                final int rowWidth = this.getRowWidth();

                if (((AbstractSelectionListAccessor) this).accessor$renderSelection() && Objects.equals(this.getSelected(), filteredList.get(i))) {
                    final int xSelectStart = this.x0 + this.width / 2 - rowWidth / 2 - 2;
                    final int xSelectEnd = this.x0 + this.width / 2 + rowWidth / 2 - 4;
                    GlStateManager._disableTexture();
                    final float f = this.isFocused() ? 1.0F : 0.5F;
                    GlStateManager._color4f(f, f, f, 1.0F);
                    bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex(xSelectStart, yStart + yEnd + 2, 0.0D).endVertex();
                    bufferbuilder.vertex(xSelectEnd, yStart + yEnd + 2, 0.0D).endVertex();
                    bufferbuilder.vertex(xSelectEnd, yStart - 2, 0.0D).endVertex();
                    bufferbuilder.vertex(xSelectStart, yStart - 2, 0.0D).endVertex();
                    tessellator.end();
                    GlStateManager._color4f(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferbuilder.begin(7, DefaultVertexFormat.POSITION);
                    bufferbuilder.vertex(xSelectStart + 1, yStart + yEnd + 1, 0.0D).endVertex();
                    bufferbuilder.vertex(xSelectEnd - 1, yStart + yEnd + 1, 0.0D).endVertex();
                    bufferbuilder.vertex(xSelectEnd - 1, yStart - 1, 0.0D).endVertex();
                    bufferbuilder.vertex(xSelectStart + 1, yStart - 1, 0.0D).endVertex();
                    tessellator.end();
                    GlStateManager._enableTexture();
                }

                final E entry = filteredList.get(i);
                entry.render(stack, i, rowTop, this.getRowLeft(), rowWidth, yEnd, p_renderList_3_, p_renderList_4_, false, p_renderList_5_);
            }
        }
    }

    public static abstract class Entry<P extends FilterableList<P, E>, E extends Entry<P, E>> extends AbstractSelectionList.Entry<E> {

        private final P parentList;

        public Entry(final P parentList) {
            this.parentList = parentList;
        }

        public P getParentList() {
            return this.parentList;
        }

        public abstract Bounds getInteractBounds();

        @SuppressWarnings("unchecked")
        @Override
        public void render(final PoseStack stack, final int p_render_1_, final int renderY, final int renderX, final int p_render_4_,
                final int p_render_5_, final int mouseX, final int mouseY, final boolean p_render_8_,
            final float p_render_9_) {
            if (this.getInteractBounds().isInBounds(mouseX, mouseY, renderX, renderY)) {
                this.parentList.currentHoveredEntry = (E) this;
            } else if (this.parentList.getCurrentHoveredEntry() == this) {
                this.parentList.currentHoveredEntry = null;
            }
        }
    }
}
