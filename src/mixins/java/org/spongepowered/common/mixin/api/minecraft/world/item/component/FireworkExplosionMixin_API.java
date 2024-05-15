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
package org.spongepowered.common.mixin.api.minecraft.world.item.component;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.component.FireworkExplosion;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

import java.util.List;

@Mixin(FireworkExplosion.class)
public abstract class FireworkExplosionMixin_API implements FireworkEffect {

    // @formatter:off
    @Shadow @Final private boolean hasTwinkle;
    @Shadow @Final private boolean hasTrail;
    @Shadow @Final private IntList colors;
    @Shadow @Final private IntList fadeColors;
    @Shadow @Final private FireworkExplosion.Shape shape;
    // @formatter:on

    @Override
    public boolean flickers() {
        return this.hasTwinkle;
    }

    @Override
    public boolean hasTrail() {
        return this.hasTrail;
    }

    @Override
    public List<Color> colors() {
        return this.colors.intStream().mapToObj(Color::ofRgb).toList();
    }

    @Override
    public List<Color> fadeColors() {
        return this.fadeColors.intStream().mapToObj(Color::ofRgb).toList();
    }

    @Override
    public FireworkShape shape() {
        return (FireworkShape) (Object) this.shape;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final ResourceKey resourceKey = Sponge.game().registry(RegistryTypes.FIREWORK_SHAPE).valueKey((FireworkShape) (Object) this.shape);

        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.Item.Fireworks.FIREWORK_SHAPE, resourceKey)
                .set(Constants.Item.Fireworks.FIREWORK_COLORS, this.colors)
                .set(Constants.Item.Fireworks.FIREWORK_FADE_COLORS, this.fadeColors())
                .set(Constants.Item.Fireworks.FIREWORK_TRAILS, this.hasTrail)
                .set(Constants.Item.Fireworks.FIREWORK_FLICKERS, this.hasTwinkle);
    }

}
