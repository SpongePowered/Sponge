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
package org.spongepowered.common.test.gametest.tag;

import io.leangen.geantyref.TypeToken;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.datapack.DataPacks;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterDataPackValueEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.BlockTypeTags;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tags registered through {@link RegisterDataPackValueEvent} are loaded by the time the gametests run, as they would
 * be for a plugin on a dedicated server, with vanilla's rules for missing entries: a missing optional entry is
 * skipped, a missing required one leaves its tag out.
 */
@GameTestGroup("plugin_tags")
public final class PluginTagGameTests {

    private static final Tag<BlockType> NEW_TAG = PluginTagGameTests.blockTag("gametest_blocks");
    private static final Tag<BlockType> ORES = PluginTagGameTests.blockTag("gametest_ores");
    private static final Tag<BlockType> ORES_AND_BLOCKS = PluginTagGameTests.blockTag("gametest_ores_and_blocks");
    private static final Tag<BlockType> MERGED = PluginTagGameTests.blockTag("gametest_merged");
    private static final ResourceKey STILL_WORKING = ResourceKey.sponge("gametest_still_working");
    private static final ResourceKey BROKEN_CHILD = ResourceKey.sponge("gametest_broken_child");
    private static final ResourceKey BROKEN_VALUE = ResourceKey.sponge("gametest_broken_value");

    // Gametest classes are created before the server starts, which fires the event
    PluginTagGameTests() {
        SpongeGameTests.listen(new TypeToken<RegisterDataPackValueEvent<TagTemplate<?>>>() {}, Order.DEFAULT, event -> {
            event.register(TagTemplate.builder(DataPacks.BLOCK_TAG)
                .key(PluginTagGameTests.NEW_TAG.key())
                .addValue(BlockTypes.STONE)
                .addChild(BlockTypeTags.BEDS)
                .build());
            event.register(TagTemplate.builder(DataPacks.BLOCK_TAG)
                .key(BlockTypeTags.FLOWER_POTS.key())
                .addValue(BlockTypes.STONE)
                .build());

            final TagTemplate<BlockType> ores = TagTemplate.builder(DataPacks.BLOCK_TAG)
                .key(PluginTagGameTests.ORES.key())
                .addValue(BlockTypes.COAL_ORE)
                .addChild(BlockTypeTags.GOLD_ORES)
                .build();
            event.register(ores);
            event.register(TagTemplate.builder(DataPacks.BLOCK_TAG)
                .key(PluginTagGameTests.ORES_AND_BLOCKS.key())
                .addValue(BlockTypes.COAL_BLOCK)
                .addChild(ores)
                .build());

            event.register(TagTemplate.builder(DataPacks.BLOCK_TAG).key(PluginTagGameTests.MERGED.key()).addValue(BlockTypes.STONE).build());
            event.register(TagTemplate.builder(DataPacks.BLOCK_TAG).key(PluginTagGameTests.MERGED.key()).addValue(BlockTypes.DIRT).build());

            final ResourceKey missing = ResourceKey.of("notrealnamespace", "notrealvalue");
            event.register(TagTemplate.builder(DataPacks.ITEM_TAG)
                .key(PluginTagGameTests.STILL_WORKING)
                .addValue(RegistryKey.of(RegistryTypes.ITEM_TYPE, missing), false)
                .addChild(Tag.of(RegistryTypes.ITEM_TYPE, missing), false)
                .addValue(ItemTypes.REDSTONE)
                .build());
            event.register(TagTemplate.builder(DataPacks.ITEM_TAG)
                .key(PluginTagGameTests.BROKEN_CHILD)
                .addChild(Tag.of(RegistryTypes.ITEM_TYPE, missing), true)
                .build());
            event.register(TagTemplate.builder(DataPacks.ITEM_TAG)
                .key(PluginTagGameTests.BROKEN_VALUE)
                .addValue(RegistryKey.of(RegistryTypes.ITEM_TYPE, missing))
                .build());
        });
    }

    @GameTest
    void newTagHasItsValuesAndChildren(final GameTestHelper helper) {
        PluginTagGameTests.assertTagged(helper, PluginTagGameTests.NEW_TAG, BlockTypes.STONE, BlockTypes.RED_BED);
        helper.succeed();
    }

    @GameTest
    void vanillaTagGainsValuesAndKeepsItsOwn(final GameTestHelper helper) {
        PluginTagGameTests.assertTagged(helper, BlockTypeTags.FLOWER_POTS, BlockTypes.STONE, BlockTypes.POTTED_CACTUS);
        helper.succeed();
    }

    @GameTest
    void childTemplatesAreIncluded(final GameTestHelper helper) {
        PluginTagGameTests.assertTagged(helper, PluginTagGameTests.ORES_AND_BLOCKS, BlockTypes.COAL_BLOCK, BlockTypes.COAL_ORE, BlockTypes.GOLD_ORE);
        helper.succeed();
    }

    // Known issue: each template is written to its key's file in the pack, so the last of two for one tag replaces
    // the other, see the merging TODO in TagDataPackSerializer
    @GameTest(required = false)
    void templatesOfOneTagAreMerged(final GameTestHelper helper) {
        PluginTagGameTests.assertTagged(helper, PluginTagGameTests.MERGED, BlockTypes.STONE, BlockTypes.DIRT);
        helper.succeed();
    }

    @GameTest
    void missingOptionalEntriesAreSkipped(final GameTestHelper helper) {
        PluginTagGameTests.assertTagged(helper, Tag.of(RegistryTypes.ITEM_TYPE, PluginTagGameTests.STILL_WORKING), ItemTypes.REDSTONE);
        helper.succeed();
    }

    @GameTest
    void missingRequiredEntriesLeaveTheTagOut(final GameTestHelper helper) {
        final Set<ResourceKey> tags = RegistryTypes.ITEM_TYPE.get().<ItemType>tags().map(Tag::key).collect(Collectors.toSet());
        GameTestAssertions.assertTrue(helper, tags.contains(PluginTagGameTests.STILL_WORKING), "#" + PluginTagGameTests.STILL_WORKING + " is not loaded");
        GameTestAssertions.assertFalse(helper, tags.contains(PluginTagGameTests.BROKEN_CHILD), "#" + PluginTagGameTests.BROKEN_CHILD + " is loaded without its required child");
        GameTestAssertions.assertFalse(helper, tags.contains(PluginTagGameTests.BROKEN_VALUE), "#" + PluginTagGameTests.BROKEN_VALUE + " is loaded without its required value");
        helper.succeed();
    }

    private static Tag<BlockType> blockTag(final String value) {
        return Tag.of(RegistryTypes.BLOCK_TYPE, ResourceKey.sponge(value));
    }

    @SafeVarargs
    private static <T extends Taggable<T>> void assertTagged(final GameTestHelper helper, final Tag<T> tag,
            final DefaultedRegistryReference<? extends T>... values) {
        for (final DefaultedRegistryReference<? extends T> value : values) {
            GameTestAssertions.assertTrue(helper, value.get().is(tag), value.location() + " is not in #" + tag.key());
        }
    }
}
