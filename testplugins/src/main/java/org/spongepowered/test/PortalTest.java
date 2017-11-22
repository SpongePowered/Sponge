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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.world.ConstructPortalEvent;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Plugin(id = "portaltest")
public class PortalTest {

    private static final Random RANDOM = new Random();
    /**
     * There is no significance in this UUID. I just created it so I didn't
     * have to go through the effort of setting up custom data.
     */
    private static final UUID CREATOR_UUID = UUID.fromString("7fac20d9-cbd4-48c7-88dc-61a33e22cd74");

    @Listener
    public void onCreatePortal(ConstructPortalEvent e) {
        List<Color> dyeColors = new ArrayList<>();
        for (DyeColor x : Sponge.getRegistry().getAllOf(DyeColor.class)) {
            if (!DyeColors.RED.equals(x)) {
                Color color = x.getColor();
                if (Math.abs(color.getRed() - color.getGreen()) >= 30 || Math.abs(color.getGreen() - color.getBlue()) >= 30
                        || Math.abs(color.getRed() - color.getBlue()) >= 30) {
                    dyeColors.add(color);
                }
            }
        }
        Color randomColor = dyeColors.get(RANDOM.nextInt(dyeColors.size()));
        if (RANDOM.nextInt(5) == 0) {
            e.setCancelled(true);
            randomColor = DyeColors.RED.getColor();
        }
        Firework firework = (Firework) e.getPortalLocation().createEntity(EntityTypes.FIREWORK);
        firework.offer(Keys.FIREWORK_EFFECTS, Collections.singletonList(FireworkEffect.builder().color(randomColor).build()));
        e.getPortalLocation().getExtent().spawnEntity(firework);
        firework.setCreator(CREATOR_UUID);
        firework.detonate();
    }

    /**
     * Because fireworks only explode after a tick, they might move - or,
     * worse, travel through the portal that was just created. This prevents
     * any movement of those fireworks.
     */
    @Listener
    public void preventMove(MoveEntityEvent.Teleport.Portal event, @Getter("getTargetEntity") Firework firework) {
        if (firework.getCreator().map(CREATOR_UUID::equals).orElse(false)) {
            event.setCancelled(true);
        }
    }
}
