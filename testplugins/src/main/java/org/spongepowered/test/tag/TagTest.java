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
package org.spongepowered.test.tag;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagRegistration;
import org.spongepowered.api.tag.TagTypes;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("tagtest")
public class TagTest {

    @Inject
    public PluginContainer pluginContainer;

    @Inject
    public Logger logger;

    @Listener
    public void registerTags(RegisterDataPackValueEvent<@NonNull TagRegistration> event) {
        logger.info("Adding GRASS to the wool tag.");

        TagRegistration tagRegistration = Tag.builder()
                .key(ResourceKey.of(pluginContainer, "wool"))
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.GRASS.get())
                .build();

        event.register(tagRegistration);

        TagRegistration woolLog = Tag.builder()
                .key(ResourceKey.minecraft("wool"))
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.OAK_LOG.get())
                .build();

        event.register(woolLog);

        TagRegistration woolGrass = Tag.builder()
                .key(ResourceKey.minecraft("wool"))
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.GRASS_BLOCK.get())
                .build();

        event.register(woolGrass);

        TagRegistration underwaterDiamond = Tag.builder()
                .key(ResourceKey.minecraft("underwater_bonemeals"))
                .type(TagTypes.BLOCK_TYPE.get())
                .addValue(BlockTypes.DIAMOND_BLOCK.get())
                .build();

        event.register(underwaterDiamond);
    }
}
