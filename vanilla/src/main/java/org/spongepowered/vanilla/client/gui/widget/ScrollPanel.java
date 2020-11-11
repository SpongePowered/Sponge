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
/*
 * Credit: MinecraftForge
 * Changes: Minor tweaks, fixed scroll limits able to hit negative
 */
package org.spongepowered.vanilla.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

public abstract class ScrollPanel extends FocusableGui implements IRenderable {

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
        this.barLeft = this.left + this.width - barWidth;
    }

    protected abstract int getContentHeight();

    protected void drawBackground() {
    }

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     *
     * @param mouseX
     * @param mouseY
     */
    protected abstract void drawPanel(int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY);

    protected boolean clickPanel(final double mouseX, final double mouseY, final int button) {
        return false;
    }

    private int getMaxScroll() {
        return this.getContentHeight() - (this.height - this.border);
    }

    private void applyScrollLimits() {
        int max = Math.max(0, getMaxScroll());

        if (this.scrollDistance < 0.0F) {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > max) {
            this.scrollDistance = max;
        }
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scroll) {
        if (scroll != 0) {
            this.scrollDistance += -scroll * getScrollAmount();
            applyScrollLimits();
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
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        this.scrolling = button == 0 && mouseX >= barLeft && mouseX < barLeft + barWidth;
        if (this.scrolling) {
            return true;
        }
        final int mouseListY = ((int) mouseY) - this.top - this.getContentHeight() + (int) this.scrollDistance - border;
        if (mouseX >= left && mouseX <= right && mouseListY < 0) {
            return this.clickPanel(mouseX - left, mouseY - this.top + (int) this.scrollDistance - border, button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(final double p_mouseReleased_1_, final double p_mouseReleased_3_, final int p_mouseReleased_5_) {
        if (super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_)) {
            return true;
        }
        final boolean ret = this.scrolling;
        this.scrolling = false;
        return ret;
    }

    private int getBarHeight() {
        int barHeight = (height * height) / this.getContentHeight();

        if (barHeight < 32) {
            barHeight = 32;
        }

        if (barHeight > height - border * 2) {
            barHeight = height - border * 2;
        }

        return barHeight;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.scrolling) {
            final int maxScroll = height - getBarHeight();
            final double moved = deltaY / maxScroll;
            this.scrollDistance += getMaxScroll() * moved;
            applyScrollLimits();
            return true;
        }
        return false;
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawBackground();

        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder worldr = tess.getBuffer();

        final double scale = client.getMainWindow().getGuiScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (left * scale), (int) (client.getMainWindow().getFramebufferHeight() - (bottom * scale)), (int) (width * scale),
            (int) (height * scale));

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        this.client.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        final float texScale = 32.0F;
        worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldr.pos(this.left, this.bottom, 0.0f).tex(this.left / texScale, (this.bottom + (int) this.scrollDistance) / texScale)
            .color(0x20, 0x20, 0x20, 0xFF).endVertex();
        worldr.pos(this.right, this.bottom, 0.0f).tex(this.right / texScale, (this.bottom + (int) this.scrollDistance) / texScale)
            .color(0x20, 0x20, 0x20, 0xFF).endVertex();
        worldr.pos(this.right, this.top, 0.0f).tex(this.right / texScale, (this.top + (int) this.scrollDistance) / texScale)
            .color(0x20, 0x20, 0x20, 0xFF).endVertex();
        worldr.pos(this.left, this.top, 0.0f).tex(this.left / texScale, (this.top + (int) this.scrollDistance) / texScale)
            .color(0x20, 0x20, 0x20, 0xFF).endVertex();
        tess.draw();

        final int baseY = this.top + border - (int) this.scrollDistance;
        this.drawPanel(right, baseY, tess, mouseX, mouseY);

        GlStateManager.disableDepthTest();

        final int extraHeight = (this.getContentHeight() + border) - height;
        if (extraHeight > 0) {
            final int barHeight = getBarHeight();

            int barTop = (int) this.scrollDistance * (height - barHeight) / extraHeight + this.top;
            if (barTop < this.top) {
                barTop = this.top;
            }

            GlStateManager.disableTexture();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(barLeft, this.bottom, 0.0f).tex(0.0f, 1.0f).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, this.bottom, 0.0f).tex(1.0f, 1.0f).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, this.top, 0.0f).tex(1.0f, 0.0f).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            worldr.pos(barLeft, this.top, 0.0f).tex(0.0f, 0.0f).color(0x00, 0x00, 0x00, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(barLeft, barTop + barHeight, 0.0f).tex(0.0f, 1.0f).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, barTop + barHeight, 0.0f).tex(1.0f, 1.0f).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth, barTop, 0.0f).tex(1.0f, 0.0f).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(barLeft, barTop, 0.0f).tex(0.0f, 0.0f).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(barLeft, barTop + barHeight - 1, 0.0f).tex(0.0f, 1.0f).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth - 1, barTop + barHeight - 1, 0.0f).tex(1.0f, 1.0f).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(barLeft + barWidth - 1, barTop, 0.0f).tex(1.0f, 0.0f).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(barLeft, barTop, 0.0f).tex(0.0f, 0.0f).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.draw();
        }

        GlStateManager.enableTexture();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public List<? extends IGuiEventListener> children() {
        return Collections.emptyList();
    }
}
