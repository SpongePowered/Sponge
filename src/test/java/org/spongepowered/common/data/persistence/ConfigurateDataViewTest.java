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
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
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
        final ConfigurationNode node = BasicConfigurationNode.root();
        node.node("foo","int").raw(1);
        node.node("foo", "double").raw(10.0D);
        node.node("foo", "long").raw(Long.MAX_VALUE);
        final List<String> stringList = Lists.newArrayList();
        for (int i = 0; i < 100; i ++) {
            stringList.add("String" + i);
        }
        node.node("foo", "stringList").raw(stringList);
        final List<SimpleData> dataList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            dataList.add(new SimpleData(i, 10.0 + i, "String" + i, Collections.emptyList()));
        }
        node.node("foo", "nested", "Data").raw(dataList);

        final DataContainer manual = DataContainer.createNew();
        manual.set(DataQuery.of("foo", "int"), 1)
                .set(DataQuery.of("foo", "double"), 10.0D)
                .set(DataQuery.of("foo", "long"), Long.MAX_VALUE)
                .set(DataQuery.of("foo", "stringList"), stringList)
                .set(DataQuery.of("foo", "nested", "Data"), dataList);

        final DataView container = ConfigurateTranslator.instance().translate(node);
        assertEquals(manual, container);
        ConfigurateTranslator.instance().translate(container);
        //assertEquals(node, translated); // TODO Test is broken, depends on quite a bit of init
    }

    @Test
    void testEmptyNodeToData() {
        final ConfigurationNode source = BasicConfigurationNode.root();
        final DataContainer container = ConfigurateTranslator.instance().translate(source);
        assertTrue(container.isEmpty());
    }

    @Test
    void testEmptyDataToNode() {
        final DataContainer source = DataContainer.createNew();
        final ConfigurationNode destination = ConfigurateTranslator.instance().translate(source);
        assertTrue(destination.empty());
    }

    @Test
    void testTypesAreCoerced() {
        final DataContainer source = DataContainer.createNew();
        source.set(DataQuery.of("i'm a short"), (short) 5);

        final ConfigurationNode destination = BasicConfigurationNode.root(ConfigurationOptions.defaults()
                .nativeTypes(Collections.singleton(Integer.class)));
        ConfigurateTranslator.instance().translateDataToNode(destination, source);

        assertEquals(5, destination.node("i'm a short").raw());
    }

    @Test
    void testColor() throws IOException {
        final Color color = Color.ofRgb(0x66, 0xCC, 0xFF);
        final DataContainer original = color.toContainer();

        final String serialized = ConfigurateDataViewTest.HOCON.write(original);
        final DataContainer ret = ConfigurateDataViewTest.HOCON.read(serialized);

        assertEquals(original, ret);
    }

    @Test
    void testFireworkEffectData() throws IOException {
        final Color color = Color.ofRgb(0x66, 0xCC, 0xFF);
        final FireworkEffect fe = new SpongeFireworkEffectBuilder()
                .colors(color, color, color)
                //.shape(new SpongeFireworkEffect("ball", "Ball"))
                .build();
        final DataContainer container = fe.toContainer();

        final String s = ConfigurateDataViewTest.HOCON.write(container);
        final DataContainer dc = ConfigurateDataViewTest.HOCON.read(s);

        assertEquals(container, dc);
    }

    @Test
    void testRespawnLocationData() throws IOException {
        final Map<ResourceKey, RespawnLocation> m = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            final ResourceKey key = ResourceKey.sponge("overworld" + i);
            final RespawnLocation loc = RespawnLocation.builder().world(key).position(Vector3d.ZERO).build();
            m.put(key, loc);
        }
        final DataContainer container = DataContainer.createNew().set(DataQuery.of("respawn_locations"), m);

        final ConfigurationNode node = ConfigurateTranslator.instance().translate(container);


        final DataContainer dc = ConfigurateTranslator.instance().translate(node);

        assertEquals(container, dc);
    }

    @Test
    void testNumber() throws IOException {
        final DataContainer container = DataContainer.createNew().set(DataQuery.of("double"), 1.0);

        final ConfigurationNode node = ConfigurateTranslator.instance().translate(container);
        assertEquals(1.0, node.node("double").raw());

        final DataContainer dc = ConfigurateTranslator.instance().translate(node);

        assertEquals(container, dc);
    }

    @Test
    void testMapInsideList() throws IOException {
        final JsonDataFormat json = new JsonDataFormat(ResourceKey.sponge("json"));

        final ConfigurationNode node = CommentedConfigurationNode.root();
        final Map<String, String> map = Collections.singletonMap("mkey", "mvalue");
        final List<Object> list = Arrays.asList("lelement", map);

        node.node("foo").raw("bar");
        node.node("l").raw(list);

        final DataContainer jc = json.read("{\"foo\":\"bar\",\"l\":[\"lelement\",{\"mkey\":\"mvalue\"}]}");
        final DataContainer hc = ConfigurateTranslator.instance().translate(node);

        assertEquals(jc.getMap(DataQuery.of()), hc.getMap(DataQuery.of()));
    }

    @Test
    void testNullRootKey() {
        assertThrows(IllegalArgumentException.class, () ->
                ConfigurateTranslator.instance().translate(BasicConfigurationNode.root().raw("bar")));
    }

    @Test
    void testNullMapKey() {
        final ConfigurationNode node = CommentedConfigurationNode.root();
        final Map<String, String> map = Collections.singletonMap(null, "v");
        final List<Object> list = Arrays.asList("e", map);

        node.node("foo").raw("bar");
        node.node("l").raw(list);

        assertThrows(IllegalArgumentException.class, () -> ConfigurateTranslator.instance().translate(node));
    }

    @Test
    void testNonRootNodeToData() {
        final ConfigurationNode root = CommentedConfigurationNode.root(n -> {
            n.node("test").act(c -> {
                c.node("child").raw("hello");
                c.node("other").raw("world");
            });
        });

        final DataView view = ConfigurateTranslator.instance().translate(root.node("test"));

        assertEquals("hello", view.getString(DataQuery.of("child")).get());
        assertEquals("world", view.getString(DataQuery.of("other")).get());

        ConfigurateTranslator.instance().translateDataToNode(root.node("test2"), view);
        assertEquals(root.node("test").raw(), root.node("test2").raw());
    }

}
