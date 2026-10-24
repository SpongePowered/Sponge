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
package org.spongepowered.vanilla.test.gametest;

import net.minecraft.client.Minecraft;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.vanilla.client.gui.screen.PluginScreen;
import org.spongepowered.vanilla.client.gui.widget.list.PluginSelectionList;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/** The title screen's plugin list, built on the client thread the way {@code Minecraft.setScreen} builds it. */
@GameTestGroup("plugin_screen")
public final class PluginScreenGameTests {

    // The screen is built but not shown: a shown screen pauses the integrated server, and with it this test.
    // init() still runs everything that broke: the list's geometry, and the metadata panel wrapping the first plugin.
    @GameTest(client = true, maxTicks = 40)
    void listsPluginsAndSelectsOnClick(final GameTestHelper helper) {
        final Minecraft mc = Minecraft.getInstance();
        final List<String> problems = new CopyOnWriteArrayList<>();
        final CompletableFuture<Void> built = mc.submit(() -> {
            final PluginScreen screen = new PluginScreen(null);
            screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
            final PluginSelectionList list = screen.children().stream()
                .filter(PluginSelectionList.class::isInstance).map(PluginSelectionList.class::cast)
                .findFirst().orElseThrow(() -> new IllegalStateException("no plugin list on the screen"));
            final int plugins = Launch.instance().pluginManager().plugins().size();
            PluginScreenGameTests.check(problems, list.children().size() == plugins,
                "list has " + list.children().size() + " entries for " + plugins + " plugins");
            // A stale getBottom() override once made the list zero-height, so nothing rendered or took clicks
            PluginScreenGameTests.check(problems, list.getBottom() == list.getY() + list.getHeight(),
                "list bottom is " + list.getBottom() + " for y " + list.getY() + " and height " + list.getHeight());
            final int rowX = list.getX() + list.getWidth() / 2;
            final int secondRowY = list.getY() + 4 + list.getRowHeight() + 2;
            PluginScreenGameTests.check(problems, list.isMouseOver(rowX, secondRowY), "the second row is inside the list");
            if (plugins > 1) {
                list.mouseClicked(rowX, secondRowY, 0);
                PluginScreenGameTests.check(problems, list.getSelected() == list.children().get(1), "clicking the second row selects it");
            }
        });
        helper.startSequence()
            .thenWaitUntil(() -> helper.assertTrue(built.isDone(), "screen built on the client thread"))
            .thenExecute(built::join)
            .thenExecute(() -> helper.assertTrue(problems.isEmpty(), String.join("; ", problems)))
            .thenSucceed();
    }

    private static void check(final List<String> problems, final boolean ok, final String what) {
        if (!ok) {
            problems.add(what);
        }
    }
}
