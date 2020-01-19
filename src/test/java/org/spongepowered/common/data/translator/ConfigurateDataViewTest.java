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
package org.spongepowered.common.data.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.junit.Test;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.mutable.entity.RespawnLocationData;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeRespawnLocationData;
import org.spongepowered.common.data.persistence.ConfigurateTranslator;
import org.spongepowered.common.data.persistence.HoconDataFormat;
import org.spongepowered.common.data.persistence.JsonDataFormat;
import org.spongepowered.common.item.SpongeFireworkEffect;
import org.spongepowered.common.item.SpongeFireworkEffectBuilder;
import org.spongepowered.common.item.SpongeFireworkShape;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfigurateDataViewTest {

    @Test
    public void testNodeToData() {
        ConfigurationNode node = SimpleConfigurationNode.root();
        node.getNode("foo","int").setValue(1);
        node.getNode("foo", "double").setValue(10.0D);
        node.getNode("foo", "long").setValue(Long.MAX_VALUE);
        List<String> stringList = Lists.newArrayList();
        for (int i = 0; i < 100; i ++) {
            stringList.add("String" + i);
        }
        node.getNode("foo", "stringList").setValue(stringList);
        List<SimpleData> dataList = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            dataList.add(new SimpleData(i, 10.0 + i, "String" + i, Collections.<String>emptyList()));
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
        ConfigurationNode translated = ConfigurateTranslator.instance().translate(container);
        // assertEquals(node, translated); // TODO Pending Configurate equals implementation
    }

    @Test
    public void testColor() throws IOException {
        Color color = Color.ofRgb(0x66, 0xCC, 0xFF);
        DataContainer container = color.toContainer();

        HoconDataFormat hocon = new HoconDataFormat("hocon");
        String s = hocon.write(container);
        DataContainer dc = hocon.read(s);

        assertEquals(container, dc);
    }

    @Test
    public void testFireworkEffectData() throws IOException {
        Color color = Color.ofRgb(0x66, 0xCC, 0xFF);
        FireworkEffect fe = new SpongeFireworkEffectBuilder()
                .colors(color, color, color)
                .shape(new SpongeFireworkShape("ball", "Ball"))
                .build();
        DataContainer container = fe.toContainer();

        HoconDataFormat hocon = new HoconDataFormat("hocon");
        String s = hocon.write(container);
        DataContainer dc = hocon.read(s);

        assertEquals(container, dc);
    }

    @Test
    public void testRespawnLocationData() throws IOException {
        Map<UUID, RespawnLocation> m = new HashMap<>();
        for (int i = 0; i < 1; i++) {
            UUID uuid = UUID.randomUUID();
            RespawnLocation loc = RespawnLocation.builder().world(uuid).position(Vector3d.ZERO).build();
            m.put(uuid, loc);
        }
        DataContainer container = DataContainer.createNew().set(DataQuery.of("respawn_locations"), m);

        HoconDataFormat hocon = new HoconDataFormat("hocon");
        String s = hocon.write(container);
        DataContainer dc = hocon.read(s);

        assertEquals(container, dc);
    }

    @Test
    public void testNumber() throws IOException {
        DataContainer container = DataContainer.createNew().set(DataQuery.of("double"), 1.0);

        HoconDataFormat hocon = new HoconDataFormat("hocon");
        String s = hocon.write(container);
        DataContainer dc = hocon.read(s);

        assertEquals(container, dc);
    }

    @Test
    public void testMapInsideList() throws IOException {
        JsonDataFormat json = new JsonDataFormat();

        ConfigurationNode node = SimpleCommentedConfigurationNode.root();
        Map<String, String> map = Collections.singletonMap("mkey", "mvalue");
        List<Object> list = Arrays.asList("lelement", map);

        node.getNode("foo").setValue("bar");
        node.getNode("l").setValue(list);

        DataContainer jc = json.read("{\"foo\":\"bar\",\"l\":[\"lelement\",{\"mkey\":\"mvalue\"}]}");
        DataContainer hc = ConfigurateTranslator.instance().translate(node);

        assertEquals(jc.getMap(DataQuery.of()), hc.getMap(DataQuery.of()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRootKey() {
        ConfigurateTranslator.instance().translate(SimpleConfigurationNode.root().setValue("bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRootValue() {
        ConfigurateTranslator.instance().translate(SimpleConfigurationNode.root().getNode("foo").setValue(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMapKey() {
        ConfigurationNode node = SimpleCommentedConfigurationNode.root();
        Map<String, String> map = Collections.singletonMap(null, "v");
        List<Object> list = Arrays.asList("e", map);

        node.getNode("foo").setValue("bar");
        node.getNode("l").setValue(list);

        ConfigurateTranslator.instance().translate(node);
    }
}
