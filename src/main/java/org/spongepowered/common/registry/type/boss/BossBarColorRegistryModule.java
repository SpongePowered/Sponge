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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.world.BossInfo;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class BossBarColorRegistryModule implements CatalogRegistryModule<BossBarColor> {

    @RegisterCatalog(BossBarColors.class)
    public final Map<String, BossBarColor> colorMap = Maps.newHashMap();

    @Override
    public void registerDefaults() {
        this.colorMap.put("pink", (BossBarColor) (Object) BossInfo.Color.PINK);
        this.colorMap.put("blue", (BossBarColor) (Object) BossInfo.Color.BLUE);
        this.colorMap.put("red", (BossBarColor) (Object) BossInfo.Color.RED);
        this.colorMap.put("green", (BossBarColor) (Object) BossInfo.Color.GREEN);
        this.colorMap.put("yellow", (BossBarColor) (Object) BossInfo.Color.YELLOW);
        this.colorMap.put("purple", (BossBarColor) (Object) BossInfo.Color.PURPLE);
        this.colorMap.put("white", (BossBarColor) (Object) BossInfo.Color.WHITE);
    }

    @Override
    public Optional<BossBarColor> getById(String id) {
        return Optional.ofNullable(this.colorMap.get(checkNotNull(id, "id").toLowerCase()));
    }

    @Override
    public Collection<BossBarColor> getAll() {
        return ImmutableSet.copyOf((BossBarColor[]) (Object[]) BossInfo.Color.values());
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (BossInfo.Color color : BossInfo.Color.values()) {
            if (!this.colorMap.containsKey(color.name().toLowerCase())) {
                this.colorMap.put(color.name().toLowerCase(), (BossBarColor) (Object) color);
            }
        }
    }

}
