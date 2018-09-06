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

import net.minecraft.inventory.ClickType;

@SuppressWarnings("WeakerAccess")
public final class PacketConstants {

    // Inventory static fields
    public final static int MAGIC_CLICK_OUTSIDE_SURVIVAL = -999;
    public final static int MAGIC_CLICK_OUTSIDE_CREATIVE = -1;
    // Flag masks
    public final static int MASK_NONE              = 0x00000;
    public final static int MASK_OUTSIDE           = 0x30000;
    public final static int MASK_MODE              = 0x0FE00;
    public final static int MASK_DRAGDATA          = 0x001F8;
    public final static int MASK_BUTTON            = 0x00007;
    public final static int MASK_NORMAL            = MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
    public final static int MASK_DRAG              = MASK_OUTSIDE | MASK_NORMAL;
    // Mask presets
    public final static int MASK_ALL               = MASK_OUTSIDE | MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
    // Click location semaphore flags
    public final static int CLICK_INSIDE_WINDOW    = 0x01 << 16; // << 0
    public final static int CLICK_OUTSIDE_WINDOW   = 0x01 << 16 << 1;
    public final static int CLICK_ANYWHERE         = CLICK_INSIDE_WINDOW | CLICK_OUTSIDE_WINDOW;
    // Modes flags
    public final static int MODE_CLICK             = 0x01 << 9 << ClickType.PICKUP.ordinal();
    public final static int MODE_SHIFT_CLICK       = 0x01 << 9 << ClickType.QUICK_MOVE.ordinal();
    public final static int MODE_HOTBAR            = 0x01 << 9 << ClickType.SWAP.ordinal();
    public final static int MODE_PICKBLOCK         = 0x01 << 9 << ClickType.CLONE.ordinal();
    public final static int MODE_DROP              = 0x01 << 9 << ClickType.THROW.ordinal();
    public final static int MODE_DRAG              = 0x01 << 9 << ClickType.QUICK_CRAFT.ordinal();
    public final static int MODE_DOUBLE_CLICK      = 0x01 << 9 << ClickType.PICKUP_ALL.ordinal();
    // Drag mode flags, bitmasked from button and only set if MODE_DRAG
    public final static int DRAG_MODE_PRIMARY_BUTTON = 0x01 << 6; // << 0
    public final static int DRAG_MODE_SECONDARY_BUTTON = 0x01 << 6 << 1;
    public final static int DRAG_MODE_MIDDLE_BUTTON = 0x01 << 6 << 2;
    public final static int DRAG_MODE_ANY          = DRAG_MODE_PRIMARY_BUTTON | DRAG_MODE_SECONDARY_BUTTON | DRAG_MODE_MIDDLE_BUTTON;
    // Drag status flags, bitmasked from button and only set if MODE_DRAG
    public final static int DRAG_STATUS_STARTED    = 0x01 << 3; // << 0;
    public final static int DRAG_STATUS_ADD_SLOT   = 0x01 << 3 << 1;
    public final static int DRAG_STATUS_STOPPED    = 0x01 << 3 << 2;
    // Buttons flags, only set if *not* MODE_DRAG
    public final static int BUTTON_PRIMARY         = 0x01 /* << 0 */; // << 0
    public final static int BUTTON_SECONDARY       = 0x01 /* << 0 */ << 1;
    public final static int BUTTON_MIDDLE          = 0x01 /* << 0 */ << 2;
    // Only use these with data from the actual packet. DO NOT
    // use them as enum constant values (the 'stateId')
    public final static int PACKET_BUTTON_PRIMARY_ID = 0;
    public final static int PACKET_BUTTON_SECONDARY_ID = 0;
    public final static int PACKET_BUTTON_MIDDLE_ID = 0;
}
