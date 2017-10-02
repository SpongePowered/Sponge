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
package org.spongepowered.common.registry.type.boss;

import net.minecraft.world.BossInfo;
import org.spongepowered.api.boss.BossBarOverlay;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Locale;

@RegisterCatalog(BossBarOverlays.class)
public final class BossBarOverlayRegistryModule extends AbstractCatalogRegistryModule<BossBarOverlay> {

    @Override
    public void registerDefaults() {
        this.map.put("progress", (BossBarOverlay) (Object) BossInfo.Overlay.PROGRESS);
        this.map.put("notched_6", (BossBarOverlay) (Object) BossInfo.Overlay.NOTCHED_6);
        this.map.put("notched_10", (BossBarOverlay) (Object) BossInfo.Overlay.NOTCHED_10);
        this.map.put("notched_12", (BossBarOverlay) (Object) BossInfo.Overlay.NOTCHED_12);
        this.map.put("notched_20", (BossBarOverlay) (Object) BossInfo.Overlay.NOTCHED_20);
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (BossInfo.Overlay overlay : BossInfo.Overlay.values()) {
            String name = overlay.name().toLowerCase(Locale.ENGLISH);
            if (!this.map.containsKey(name)) {
                this.map.put(name, (BossBarOverlay) (Object) overlay);
            }
        }
    }

}
