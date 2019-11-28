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
package org.spongepowered.common.event.tracking.phase.packet;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.asm.util.PrettyPrinter;

import javax.annotation.Nullable;
import net.minecraft.inventory.container.Container;

public class BasicPacketContext extends PacketContext<BasicPacketContext> {

    @Nullable private Container container;

    public BasicPacketContext(PacketState<? extends BasicPacketContext> state) {
        super(state);
    }

    public BasicPacketContext openContainer(Container openContainer) {
        this.container = openContainer;
        return this;
    }

    public Container getOpenContainer() {
        return checkNotNull(this.container, "Open Container was null!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasCaptures() {
        if (this.state == PacketPhase.General.RESOURCE_PACK) {
            return true;
        }
        if (this.state == PacketPhase.General.CLOSE_WINDOW) {
            return true;
        }

        return super.hasCaptures();
    }

    @Override
    public PrettyPrinter printCustom(PrettyPrinter printer, int indent) {
        String s = String.format("%1$"+indent+"s", "");
        return super.printCustom(printer, indent)
            .add(s + "- %s: %s", "OpenContainer", this.container)
            ;
    }

    @Override
    protected void reset() {
        super.reset();
        this.container = null;
    }
}
