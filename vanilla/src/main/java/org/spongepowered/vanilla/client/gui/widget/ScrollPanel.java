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
package org.spongepowered.vanilla.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.Collections;
import java.util.List;

/**
 * Credit: MinecraftForge
 * Changes: Minor tweaks, fixed scroll limits able to hit negative
 * TODO: Rewrite to use AbstractScrollArea instead of custom scroll logic
 */
public abstract class ScrollPanel extends AbstractContainerEventHandler implements Renderable {

    private final Minecraft client;
    protected final int width;
    protected final int height;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    private boolean scrolling;
    protected float scrollDistance;
    protected final int border = 4;

    private final int barWidth = 6;
    private final int barLeft;

    public ScrollPanel(final Minecraft client, final int width, final int height, final int top, final int left) {
        this.client = client;
        this.width = width;
        this.height = height;
        this.top = top;
        this.left = left;
        this.bottom = height + this.top;
        this.right = width + this.left;
        this.barLeft = this.left + this.width - this.barWidth;
    }

    protected abstract int getContentHeight();

    protected void drawBackground() {
    }

    protected abstract void drawPanel(final GuiGraphicsExtractor graphics, int entryRight, int relativeY, int mouseX, int mouseY);

    protected boolean clickPanel(final double mouseX, final double mouseY, final int button) {
        return false;
    }

    private int getMaxScroll() {
        return this.getContentHeight() - (this.height - this.border);
    }

    private void applyScrollLimits() {
        final int max = Math.max(0, this.getMaxScroll());

        if (this.scrollDistance < 0.0F) {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > max) {
            this.scrollDistance = max;
        }
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scroll, final double scrollY) {
        if (scroll != 0) {
            this.scrollDistance += -scroll * this.getScrollAmount();
            this.applyScrollLimits();
            return true;
        }
        return false;
    }

    protected int getScrollAmount() {
        return 20;
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        return mouseX >= this.left && mouseX <= this.left + this.width && mouseY >= this.top && mouseY <= this.bottom;
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        final var button = event.button();
        final var mouseX = event.x();
        final var mouseY = event.y();
        this.scrolling = button == 0 && mouseX >= this.barLeft && mouseX < this.barLeft + this.barWidth;
        if (this.scrolling) {
            return true;
        }
        final int mouseListY = ((int) mouseY) - this.top - this.getContentHeight() + (int) this.scrollDistance - this.border;
        if (mouseX >= this.left && mouseX <= this.right && mouseListY < 0) {
            return this.clickPanel(mouseX - this.left, mouseY - this.top + (int) this.scrollDistance - this.border, button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (super.mouseReleased(event)) {
            return true;
        }
        final boolean ret = this.scrolling;
        this.scrolling = false;
        return ret;
    }

    private int getBarHeight() {
        int barHeight = (this.height * this.height) / this.getContentHeight();

        if (barHeight < 32) {
            barHeight = 32;
        }

        if (barHeight > this.height - this.border * 2) {
            barHeight = this.height - this.border * 2;
        }

        return barHeight;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double deltaX, final double deltaY) {
        if (this.scrolling) {
            final int maxScroll = this.height - this.getBarHeight();
            final double moved = deltaY / maxScroll;
            this.scrollDistance += this.getMaxScroll() * moved;
            this.applyScrollLimits();
            return true;
        }
        return false;
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics) {
        this.drawBackground();
        // TODO: Rewrite rendering for GuiGraphicsExtractor pipeline
        final int baseY = this.top + this.border - (int) this.scrollDistance;
        this.drawPanel(graphics, this.right, baseY, graphics.mouseX(), graphics.mouseY());
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
}
