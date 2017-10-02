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
package org.spongepowered.common.registry.type.entity;

import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeBodyPart;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@RegisterCatalog(BodyParts.class)
public class BodyPartRegistryModule extends AbstractCatalogRegistryModule<BodyPart> {

    @Override
    public void registerDefaults() {
        this.map.put("head", new SpongeBodyPart("HEAD"));
        this.map.put("chest", new SpongeBodyPart("CHEST"));
        this.map.put("left_arm", new SpongeBodyPart("LEFT_ARM"));
        this.map.put("right_arm", new SpongeBodyPart("RIGHT_ARM"));
        this.map.put("left_leg", new SpongeBodyPart("LEFT_LEG"));
        this.map.put("right_leg", new SpongeBodyPart("RIGHT_LEG"));
    }
}
