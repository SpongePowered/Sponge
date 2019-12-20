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
package org.spongepowered.common.registry.supplier;

import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class VanillaBannerPatternSupplier {

    private VanillaBannerPatternSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(BannerPatternShape.class, "base", () -> (BannerPatternShape) (Object) BannerPattern.BASE)
            .registerSupplier(BannerPatternShape.class, "square_bottom_left", () -> (BannerPatternShape) (Object) BannerPattern.SQUARE_BOTTOM_LEFT)
            .registerSupplier(BannerPatternShape.class, "square_bottom_right", () -> (BannerPatternShape) (Object) BannerPattern.SQUARE_BOTTOM_RIGHT)
            .registerSupplier(BannerPatternShape.class, "square_top_left", () -> (BannerPatternShape) (Object) BannerPattern.SQUARE_TOP_LEFT)
            .registerSupplier(BannerPatternShape.class, "square_top_right", () -> (BannerPatternShape) (Object) BannerPattern.SQUARE_TOP_RIGHT)
            .registerSupplier(BannerPatternShape.class, "stripe_bottom", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_BOTTOM)
            .registerSupplier(BannerPatternShape.class, "stripe_top", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_TOP)
            .registerSupplier(BannerPatternShape.class, "stripe_left", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_LEFT)
            .registerSupplier(BannerPatternShape.class, "stripe_right", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_RIGHT)
            .registerSupplier(BannerPatternShape.class, "stripe_center", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_CENTER)
            .registerSupplier(BannerPatternShape.class, "stripe_middle", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_MIDDLE)
            .registerSupplier(BannerPatternShape.class, "stripe_downright", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_DOWNRIGHT)
            .registerSupplier(BannerPatternShape.class, "stripe_downleft", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_DOWNLEFT)
            .registerSupplier(BannerPatternShape.class, "stripe_small", () -> (BannerPatternShape) (Object) BannerPattern.STRIPE_SMALL)
            .registerSupplier(BannerPatternShape.class, "cross", () -> (BannerPatternShape) (Object) BannerPattern.CROSS)
            .registerSupplier(BannerPatternShape.class, "straight_cross", () -> (BannerPatternShape) (Object) BannerPattern.STRAIGHT_CROSS)
            .registerSupplier(BannerPatternShape.class, "triangle_bottom", () -> (BannerPatternShape) (Object) BannerPattern.TRIANGLE_BOTTOM)
            .registerSupplier(BannerPatternShape.class, "triangle_top", () -> (BannerPatternShape) (Object) BannerPattern.TRIANGLE_TOP)
            .registerSupplier(BannerPatternShape.class, "triangles_bottom", () -> (BannerPatternShape) (Object) BannerPattern.TRIANGLES_BOTTOM)
            .registerSupplier(BannerPatternShape.class, "triangles_top", () -> (BannerPatternShape) (Object) BannerPattern.TRIANGLES_TOP)
            .registerSupplier(BannerPatternShape.class, "diagonal_left", () -> (BannerPatternShape) (Object) BannerPattern.DIAGONAL_LEFT)
            .registerSupplier(BannerPatternShape.class, "diagonal_right", () -> (BannerPatternShape) (Object) BannerPattern.DIAGONAL_RIGHT)
            .registerSupplier(BannerPatternShape.class, "diagonal_left_mirror", () -> (BannerPatternShape) (Object) BannerPattern.DIAGONAL_LEFT_MIRROR)
            .registerSupplier(BannerPatternShape.class, "diagonal_right_mirror", () -> (BannerPatternShape) (Object) BannerPattern.DIAGONAL_RIGHT_MIRROR)
            .registerSupplier(BannerPatternShape.class, "circle_middle", () -> (BannerPatternShape) (Object) BannerPattern.CIRCLE_MIDDLE)
            .registerSupplier(BannerPatternShape.class, "rhombus_middle", () -> (BannerPatternShape) (Object) BannerPattern.RHOMBUS_MIDDLE)
            .registerSupplier(BannerPatternShape.class, "half_vertical", () -> (BannerPatternShape) (Object) BannerPattern.HALF_VERTICAL)
            .registerSupplier(BannerPatternShape.class, "half_horizontal", () -> (BannerPatternShape) (Object) BannerPattern.HALF_HORIZONTAL)
            .registerSupplier(BannerPatternShape.class, "half_vertical_mirror", () -> (BannerPatternShape) (Object) BannerPattern.HALF_VERTICAL_MIRROR)
            .registerSupplier(BannerPatternShape.class, "half_horizontal_mirror", () -> (BannerPatternShape) (Object) BannerPattern.HALF_HORIZONTAL_MIRROR)
            .registerSupplier(BannerPatternShape.class, "border", () -> (BannerPatternShape) (Object) BannerPattern.BORDER)
            .registerSupplier(BannerPatternShape.class, "curly_border", () -> (BannerPatternShape) (Object) BannerPattern.CURLY_BORDER)
            .registerSupplier(BannerPatternShape.class, "gradient", () -> (BannerPatternShape) (Object) BannerPattern.GRADIENT)
            .registerSupplier(BannerPatternShape.class, "gradient_up", () -> (BannerPatternShape) (Object) BannerPattern.GRADIENT_UP)
            .registerSupplier(BannerPatternShape.class, "bricks", () -> (BannerPatternShape) (Object) BannerPattern.BRICKS)
            .registerSupplier(BannerPatternShape.class, "globe", () -> (BannerPatternShape) (Object) BannerPattern.GLOBE)
            .registerSupplier(BannerPatternShape.class, "creeper", () -> (BannerPatternShape) (Object) BannerPattern.CREEPER)
            .registerSupplier(BannerPatternShape.class, "skull", () -> (BannerPatternShape) (Object) BannerPattern.SKULL)
            .registerSupplier(BannerPatternShape.class, "flower", () -> (BannerPatternShape) (Object) BannerPattern.FLOWER)
            .registerSupplier(BannerPatternShape.class, "mojang", () -> (BannerPatternShape) (Object) BannerPattern.MOJANG)
        ;
    }
}
