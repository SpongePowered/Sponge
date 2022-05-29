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
package org.spongepowered.vanilla.client.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.vanilla.client.gui.widget.MetadataPanel;
import org.spongepowered.vanilla.client.gui.widget.list.PluginSelectionList;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class PluginScreen extends Screen {

    private final Screen previousScreen;
    private final List<PluginMetadata> metadata;
    private PluginSelectionList selectionList;
    private MetadataPanel contentPanel;
    private EditBox searchField;

    public PluginScreen(final Screen previousScreen) {
        super(Component.literal("Plugins"));
        this.previousScreen = previousScreen;
        this.metadata = new ObjectArrayList<>();
        final Collection<PluginContainer> plugins = Launch.instance().pluginManager().plugins();
        this.metadata.addAll(plugins.stream().map(PluginContainer::metadata).collect(Collectors.toList()));
    }

    @Override
    protected void init() {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        final int listHeight = this.height - 122;
        this.selectionList = new PluginSelectionList(this, 4, 58, 175, listHeight, 26);
        this.contentPanel = new MetadataPanel(this.minecraft, this, this.width - this.selectionList.getWidth() - 12, listHeight, 58,
            this.selectionList.getRight() + 4);

        // Add plugin list
        this.selectionList.setSelectConsumer(e -> this.contentPanel.setMetadata(e == null ? null : e.metadata));
        this.generateEntries(
            Launch.instance().pluginManager().plugins().stream().map(PluginContainer::metadata).collect(Collectors.toList()));

        // Add search text field
        this.searchField = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, Component.translatable(I18n.get("itemGroup.search")));
        this.searchField.setResponder(value -> {
            this.selectionList.setFilterSupplier(() -> {
                // Filter based on ID/Name
                final List<PluginSelectionList.Entry> filteredList = this.selectionList.children().stream()
                    .filter(entry -> entry.metadata.name().orElse("").toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT))
                        || entry.metadata.id().toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());

                // If the current selection doesn't exist, then select what we can at the top of the filtered list
                if (!filteredList.contains(this.selectionList.getSelected())) {
                    this.selectionList.setSelected(filteredList.stream().findFirst().orElse(null));
                }

                return filteredList;
            });
        });

        // Add controls
        this.addRenderableWidget(this.selectionList);
        this.addRenderableWidget(this.contentPanel);
        this.addRenderableWidget(this.searchField);

        // Add the 'Done' button
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 40, 100, 20, Component.translatable(I18n.get("gui.done")),
            (p_214323_1_) -> Minecraft.getInstance().setScreen(this.previousScreen)));
    }

    @Override
    public void render(final PoseStack stack, final int p_render_1_, final int p_render_2_, final float p_render_3_) {
        this.renderBackground(stack);
        super.render(stack, p_render_1_, p_render_2_, p_render_3_); // render the widgets
        Screen.drawCenteredString(stack, this.font, this.title.getString(), this.width / 2, 8, 16777215);
    }

    private void generateEntries(final List<PluginMetadata> metadatas) {
        if (this.selectionList == null) {
            return;
        }

        this.selectionList.children().clear();
        this.selectionList.children().addAll(metadatas.stream()
            .map(metadata -> new PluginSelectionList.Entry(this.selectionList, metadata))
            .collect(Collectors.toList()));
        this.selectionList.setSelected(this.selectionList.children().stream().findFirst().orElse(null));
    }
}
