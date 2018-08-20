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
package org.spongepowered.common.registry.type.block;

import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeSkullType;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

@RegisterCatalog(SkullTypes.class)
public final class SkullTypeRegistryModule
        extends AbstractPrefixAlternateCatalogTypeRegistryModule<SkullType>
        implements CatalogRegistryModule<SkullType> {

    public SkullTypeRegistryModule() {
        super("minecraft");
    }
    @Override
    public void registerDefaults() {
        register(new SpongeSkullType((byte) 0, "minecraft:skeleton", "skeleton", new SpongeTranslation("item.skull.skeleton.name")));
        register(new SpongeSkullType((byte) 1, "minecraft:wither_skeleton", "wither_skeleton", new SpongeTranslation("item.skull.wither.name")));
        register(new SpongeSkullType((byte) 2, "minecraft:zombie", "zombie", new SpongeTranslation("item.skull.zombie.name")));
        register(new SpongeSkullType((byte) 3, "minecraft:player", "player", new SpongeTranslation("item.skull.char.name")));
        register(new SpongeSkullType((byte) 4,"minecraft:creeper", "creeper", new SpongeTranslation("item.skull.creeper.name")));
        register(new SpongeSkullType((byte) 5, "minecraft:ender_dragon", "ender_dragon", new SpongeTranslation("item.skull.dragon.name")));
    }


}
