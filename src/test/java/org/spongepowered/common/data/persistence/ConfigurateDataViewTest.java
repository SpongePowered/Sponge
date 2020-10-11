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
package org.spongepowered.common.data.persistence;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.math.vector.Vector3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Disabled("Can't run these tests without access to a ResourceKey implementation")
public class ConfigurateDataViewTest {
    private static final HoconDataFormat HOCON = new HoconDataFormat(null);

    @Test
    void testNodeToData() {
        ConfigurationNode node = ConfigurationNode.root();
        node.getNode("foo","int").setValue(1);
        node.getNode("foo", "double").setValue(10.0D);
        node.getNode("foo", "long").setValue(Long.MAX_VALUE);
        List<String> stringList = Lists.newArrayList();
        for (int i = 0; i < 100; i ++) {
            stringList.add("String" + i);
        }
        node.getNode("foo", "stringList").setValue(stringList);
        List<SimpleData> dataList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            dataList.add(new SimpleData(i, 10.0 + i, "String" + i, Collections.emptyList()));
        }
        node.getNode("foo", "nested", "Data").setValue(dataList);

        DataContainer manual = DataContainer.createNew();
        manual.set(DataQuery.of("foo", "int"), 1)
                .set(DataQuery.of("foo", "double"), 10.0D)
                .set(DataQuery.of("foo", "long"), Long.MAX_VALUE)
                .set(DataQuery.of("foo", "stringList"), stringList)
                .set(DataQuery.of("foo", "nested", "Data"), dataList);

        DataView container = ConfigurateTranslator.instance().translate(node);
        assertEquals(manual, container);
        ConfigurateTranslator.instance().translate(container);
        //assertEquals(node, translated); // TODO Test is broken, depends on quite a bit of init
    }

    @Test
    void testEmptyNodeToData() {
        final ConfigurationNode source = ConfigurationNode.root();
        final DataContainer container = ConfigurateTranslator.instance().translate(source);
        assertTrue(container.isEmpty());
    }

    @Test
    void testEmptyDataToNode() {
        final DataContainer source = DataContainer.createNew();
        final ConfigurationNode destination = ConfigurateTranslator.instance().translate(source);
        assertTrue(destination.isEmpty());
    }

    @Test
    void testTypesAreCoerced() {
        final DataContainer source = DataContainer.createNew();
        source.set(DataQuery.of("i'm a short"), (short) 5);

        final ConfigurationNode destination = ConfigurationNode.root(ConfigurationOptions.defaults()
                .withNativeTypes(Collections.singleton(Integer.class)));
        ConfigurateTranslator.instance().translateDataToNode(destination, source);

        assertEquals(5, destination.getNode("i'm a short").getValue());
    }

    @Test
    void testColor() throws IOException {
        Color color = Color.ofRgb(0x66, 0xCC, 0xFF);
        DataContainer original = color.toContainer();

        String serialized = HOCON.write(original);
        DataContainer ret = HOCON.read(serialized);

        assertEquals(original, ret);
    }

    @Test
    void testFireworkEffectData() throws IOException {
        Color color = Color.ofRgb(0x66, 0xCC, 0xFF);
        FireworkEffect fe = new SpongeFireworkEffectBuilder()
                .colors(color, color, color)
                //.shape(new SpongeFireworkEffect("ball", "Ball"))
                .build();
        DataContainer container = fe.toContainer();

        String s = HOCON.write(container);
        DataContainer dc = HOCON.read(s);

        assertEquals(container, dc);
    }

    @Test
    void testRespawnLocationData() throws IOException {
        Map<ResourceKey, RespawnLocation> m = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            final ResourceKey key = ResourceKey.sponge("overworld" + i);
            RespawnLocation loc = RespawnLocation.builder().world(key).position(Vector3d.ZERO).build();
            m.put(key, loc);
        }
        DataContainer container = DataContainer.createNew().set(DataQuery.of("respawn_locations"), m);

        ConfigurationNode node = ConfigurateTranslator.instance().translate(container);


        DataContainer dc = ConfigurateTranslator.instance().translate(node);

        assertEquals(container, dc);
    }

    @Test
    void testNumber() throws IOException {
        DataContainer container = DataContainer.createNew().set(DataQuery.of("double"), 1.0);

        ConfigurationNode node = ConfigurateTranslator.instance().translate(container);
        assertEquals(1.0, node.getNode("double").getValue());

        DataContainer dc = ConfigurateTranslator.instance().translate(node);

        assertEquals(container, dc);
    }

    @Test
    void testMapInsideList() throws IOException {
        JsonDataFormat json = new JsonDataFormat(ResourceKey.sponge("json"));

        ConfigurationNode node = CommentedConfigurationNode.root();
        Map<String, String> map = Collections.singletonMap("mkey", "mvalue");
        List<Object> list = Arrays.asList("lelement", map);

        node.getNode("foo").setValue("bar");
        node.getNode("l").setValue(list);

        DataContainer jc = json.read("{\"foo\":\"bar\",\"l\":[\"lelement\",{\"mkey\":\"mvalue\"}]}");
        DataContainer hc = ConfigurateTranslator.instance().translate(node);

        assertEquals(jc.getMap(DataQuery.of()), hc.getMap(DataQuery.of()));
    }

    @Test
    void testNullRootKey() {
        assertThrows(IllegalArgumentException.class, () ->
                ConfigurateTranslator.instance().translate(ConfigurationNode.root().setValue("bar")));
    }

    @Test
    void testNullMapKey() {
        ConfigurationNode node = CommentedConfigurationNode.root();
        Map<String, String> map = Collections.singletonMap(null, "v");
        List<Object> list = Arrays.asList("e", map);

        node.getNode("foo").setValue("bar");
        node.getNode("l").setValue(list);

        assertThrows(IllegalArgumentException.class, () -> ConfigurateTranslator.instance().translate(node));
    }

    @Test
    void testNonRootNodeToData() {
        final ConfigurationNode root = CommentedConfigurationNode.root(n -> {
            n.getNode("test").act(c -> {
                c.getNode("child").setValue("hello");
                c.getNode("other").setValue("world");
            });
        });

        final DataView view = ConfigurateTranslator.instance().translate(root.getNode("test"));

        assertEquals("hello", view.getString(DataQuery.of("child")).get());
        assertEquals("world", view.getString(DataQuery.of("other")).get());

        ConfigurateTranslator.instance().translateDataToNode(root.getNode("test2"), view);
        assertEquals(root.getNode("test").getValue(), root.getNode("test2").getValue());
    }

}
