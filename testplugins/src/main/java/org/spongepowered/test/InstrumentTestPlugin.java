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
package org.spongepowered.test;

import org.spongepowered.api.block.tileentity.Note;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.InstrumentProperty;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@SuppressWarnings("ConstantConditions")
@Plugin(id = "instrument-test")
public class InstrumentTestPlugin {

    private static boolean ENABLED = false;

    @Listener
    public void onUseItem(InteractBlockEvent.Primary.MainHand event, @First Player player) {
        if (!ENABLED || !player.get(Keys.IS_SNEAKING).get()) {
            return;
        }
        final TileEntity tile = player.getWorld().getTileEntity(event.getTargetBlock().getPosition()).orElse(null);
        if (!(tile instanceof Note)) {
            return;
        }
        final ItemStack itemStack = player.getItemInHand(event.getHandType()).orElse(null);
        if (itemStack == null) {
            return;
        }
        final InstrumentProperty instrumentProperty = itemStack.getProperty(InstrumentProperty.class).orElse(null);
        if (instrumentProperty != null) {
            final InstrumentType instrument = instrumentProperty.getValue();
            player.sendMessage(Text.of("Clicked on a note block with instrument: ", instrument.getName()));
        } else {
            player.sendMessage(Text.of("Clicked on a note block."));
        }
    }
}
