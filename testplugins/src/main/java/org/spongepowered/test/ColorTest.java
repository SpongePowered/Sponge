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

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;

@Plugin(id = "color_test", name = "Color Test", description = "Right click to get an items color. Left click to set the color to orange.")
public class ColorTest {

    @Listener
    public void onPrimaryClick(InteractItemEvent.Primary event, @First Player player) {
        player.getItemInHand(HandTypes.MAIN_HAND).ifPresent(item -> {
            String id = item.getType().getId();
            if (item.supports(Keys.COLOR)) {
                Color color = item.get(Keys.COLOR).get();
                player.sendMessage(Text.of(id, "'s color is ", String.format("0x%06X", color.getRgb()), " ", color.toVector3i()));
            } else if (item.supports(Keys.DYE_COLOR)) {
                DyeColor dyeColor = item.get(Keys.DYE_COLOR).get();
                player.sendMessage(Text.of(id, "'s dye color is ", dyeColor.getId(), " (", String.format("0x%06X", dyeColor.getColor().getRgb()), "),", dyeColor.getColor().toVector3i()));
            } else {
                player.sendMessage(Text.of(id, " does not support color or dye color."));
            }
        });
    }

    private static double radians;
    private static final double increment = Math.PI / 7, shift = 2 * Math.PI / 3;
    private static final Vector3d shifts = Vector3d.from(2 * shift, shift, 0), offset = Vector3d.from(127.5);

    @Listener
    public void onSecondaryClick(InteractItemEvent.Secondary event, @First Player player) {
        player.getItemInHand(HandTypes.MAIN_HAND).filter(item -> item.supports(Keys.COLOR)).ifPresent(item -> {
            Vector3d vec = Vector3d.from(radians += increment).add(shifts);
            item.offer(Keys.COLOR, Color.of(Vector3d.from(Math.sin(vec.getX()), Math.sin(vec.getY()), Math.sin(vec.getZ())).mul(offset).add(offset).round().toInt()));
            player.setItemInHand(HandTypes.MAIN_HAND, item);
        });
    }

}
