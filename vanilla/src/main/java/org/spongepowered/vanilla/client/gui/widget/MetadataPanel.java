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
package org.spongepowered.vanilla.client.gui.widget;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.vanilla.client.gui.screen.PluginScreen;
import org.spongepowered.vanilla.util.Bounds;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class MetadataPanel extends ScrollPanel {

    static final Pattern URL_PATTERN = Pattern.compile(
        //         schema                          ipv4            OR        namespace                 port     path         ends
        //   |-----------------|        |-------------------------|  |-------------------------|    |---------| |--|   |---------------|
        "((?:[a-z0-9]{2,}://)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|(?:[-\\w_]+\\.[a-z]{2,}?))(?::[0-9]{1,5})?.*?(?=[!\"\u00A7 \n]|$))",
        Pattern.CASE_INSENSITIVE);
    static final int INDENT_SIZE = 12;
    private final Minecraft minecraft;
    private final PluginScreen screen;
    private final int lineHeight;
    private final List<Category> categories = new ObjectArrayList<>();
    private final List<Category> resizedCategories = new ObjectArrayList<>();
    private int maxKeyWidth;

    public MetadataPanel(final Minecraft minecraft, final PluginScreen screen, final int width, final int height, final int top, final int left) {
        super(minecraft, width, height, top, left);
        this.minecraft = minecraft;
        this.screen = screen;
        this.lineHeight = this.minecraft.fontRenderer.FONT_HEIGHT + 1;
    }

    public void setMetadata(@Nullable final PluginMetadata metadata) {
        this.categories.clear();
        this.maxKeyWidth = 0;

        if (metadata == null) {
            this.resizedCategories.clear();
            return;
        }

        // Details
        this.categories.add(new Category("Details")
            .addEntry(new Entry("ID", metadata.getId())).addEntry(new Entry("Name", metadata.getName().orElse(null)))
            .addEntry(new Entry("Version", metadata.getVersion())).addEntry(new Entry("Entry", metadata.getMainClass()))
            .addEntry(new Entry("Description", metadata.getDescription().orElse(null))));

        // Contributors
        this.categories.add(new Category("Contributors",
            metadata.getContributors().stream().map(c -> new Entry(c.getName(), c.getDescription().orElse(""))).collect(Collectors.toList())));

        // Dependencies
        this.categories.add(new Category("Dependencies", metadata.getDependencies().stream()
            .map(d -> Lists.newArrayList(
                new Entry(d.getId(), ""),
                new Entry("Version", d.getVersion(), 1),
                new Entry("Optional", String.valueOf(d.isOptional()), 1),
                new Entry("Load Order", d.getLoadOrder().name(), 1)))
            .flatMap(List::stream)
            .collect(Collectors.toList())));

        // Resources
        this.categories.add(
            new Category("Resources")
                .addEntry(new Entry("Homepage", metadata.getLinks().getHomepage().map(URL::toString).orElse(null)))
                .addEntry(new Entry("Issues", metadata.getLinks().getIssues().map(URL::toString).orElse(null)))
                .addEntry(new Entry("Source", metadata.getLinks().getSource().map(URL::toString).orElse(null))));

        // Other
        this.categories.add(new Category("Other",
            metadata.getExtraMetadata().entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue().toString())).collect(Collectors.toList())));

        this.categories.stream().flatMap(c -> c.getEntries().stream()).forEach(e -> {
            final int width = e.key == null ? 0 : this.minecraft.fontRenderer.getStringWidth(e.key.getFormattedText());
            this.maxKeyWidth = Math.max(this.maxKeyWidth, width + (e.level * MetadataPanel.INDENT_SIZE));
        });

        this.resizeContent();
    }

    private void resizeContent() {
        this.resizedCategories.clear();
        this.resizedCategories.addAll(this.categories);

        for (final Category category : this.resizedCategories) {
            final List<Entry> newEntries = new ArrayList<>();
            for (final Entry entry : category.getEntries()) {
                if (entry.rawValue == null) {
                    continue;
                }

                final int levelOffset = entry.level * MetadataPanel.INDENT_SIZE;
                final int baseX = 4;
                final int keyX = baseX + MetadataPanel.INDENT_SIZE + levelOffset;
                final int separatorX = keyX + this.maxKeyWidth + 4 - levelOffset;
                final int valueX = separatorX + this.minecraft.fontRenderer.getStringWidth(":") + 4;
                final int maxWidth = this.width - valueX - 8;
                if (maxWidth >= 0) {
                    final List<String> lines = Arrays.asList(efficientWrapper(entry.rawValue, maxWidth).split("\n"));
                    newEntries.add(new Entry(entry.rawKey, lines.get(0), entry.level, entry.rawValue));
                    newEntries.addAll(lines.stream().skip(1).map(l -> new Entry(null, l, entry.level, entry.rawValue)).collect(Collectors.toList()));
                }
            }

            category.setEntries(newEntries);
        }
    }

    private int getTotalLineCount() {
        final int categoryCount = this.resizedCategories.size();
        int entryCount = 0;
        for (final Category category : this.resizedCategories) {
            entryCount += category.getEntries().size();
        }
        return (entryCount + categoryCount + categoryCount);
    }

    private List<Entry> getAllEntries() {
        final List<Entry> entries = new ObjectArrayList<>();
        for (final Category category : this.resizedCategories) {
            entries.addAll(category.entries);
        }
        return entries;
    }

    private String efficientWrapper(final String value, final int width) {
        final List<String> lines = new ObjectArrayList<>();
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);

            final String lineCandidate = builder.toString() + c;
            if (this.minecraft.fontRenderer.getStringWidth(lineCandidate) > width) {
                // Add the line with a trailing new line
                lines.add(builder.append("\n").toString());
                // Clear the builder for a new line
                builder.delete(0, builder.length());
            }
            // Add the character
            builder.append(c);

            if (i + 1 >= value.length()) {
                lines.add(builder.toString());
            }
        }

        return String.join("", lines);
    }

    @Override
    public int getContentHeight() {
        int height = this.getTotalLineCount() * this.lineHeight;
        if (height < this.bottom - this.top - 8) {
            height = this.bottom - this.top - 8;
        }
        return height;
    }

    @Override
    protected int getScrollAmount() {
        return this.lineHeight * 3;
    }

    @Override
    protected void drawPanel(final int entryRight, int relativeY, final Tessellator tess, final int mouseX, final int mouseY) {
        final int baseX = this.left + 4;

        if (this.resizedCategories.isEmpty()) {
            final FontRenderer font = this.minecraft.fontRenderer;
            final String noResults = "No data...";
            final int noResultsWidth = font.getStringWidth(noResults);

            font.drawString(noResults, ((float) this.width / 2) + this.left - ((float) noResultsWidth / 2), this.top + 10,
                    TextFormatting.GRAY.getColor());

            return;
        }

        // Iterate and draw categories
        for (final Category category : this.resizedCategories) {
            // Skip empty categories
            if (category.getEntries().size() == 0) {
                continue;
            }

            // Draw category name
            this.minecraft.fontRenderer.drawString(category.name.getFormattedText(), baseX, relativeY, 0xFFFFFF);
            relativeY += this.lineHeight;

            // Iterate and draw entries
            for (final Entry entry : category.getEntries()) {
                if (entry.value == null) {
                    continue;
                }

                final int levelOffset = entry.level * MetadataPanel.INDENT_SIZE;
                final int keyX = baseX + MetadataPanel.INDENT_SIZE + levelOffset;
                final int separatorX = keyX + this.maxKeyWidth + 4 - levelOffset;
                final int valueX = separatorX + this.minecraft.fontRenderer.getStringWidth(":") + 4;

                // Only draw key and separator if there is any key present
                if (entry.key != null) {
                    this.minecraft.fontRenderer.drawString(entry.key.getFormattedText(), keyX, relativeY, 0xFFFFFF);

                    if (entry.rawValue != null && !entry.rawValue.isEmpty()) {
                        this.minecraft.fontRenderer.drawString(":", separatorX, relativeY, 0xFFFFFF);
                    }
                }

                // Draw the value, and update the value bounds if needed
                this.minecraft.fontRenderer.drawString(entry.value.getFormattedText(), valueX, relativeY, 0xFFFFFF);
                if (entry.value.getStyle().getClickEvent() != null) {
                    entry.valueBounds =
                        new Bounds(valueX, relativeY + 1, valueX + this.minecraft.fontRenderer.getStringWidth(entry.value.getFormattedText()),
                            relativeY + this.lineHeight);
                }
                relativeY += this.lineHeight;
            }

            relativeY += this.lineHeight;
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        // Find an entry match for where we clicked
        final Entry entry =
            this.getAllEntries().stream()
                .filter(e -> e.valueBounds != null && e.valueBounds.isInBounds((int) mouseX, (int) mouseY))
                .findFirst().orElse(null);

        if (entry == null) {
            return false;
        }

        final ITextComponent component = entry.value;
        if (component != null) {
            this.screen.handleComponentClicked(component);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawBackground() {
    }

    /*
     * Credit: MinecraftForge
     * Changes: Set ichat to link if ichat is null
     */
    public static ITextComponent newChatWithLinks(final String string, final boolean allowMissingHeader) {
        // Includes ipv4 and domain pattern
        // Matches an ip (xx.xxx.xx.xxx) or a domain (something.com) with or
        // without a protocol or path.
        ITextComponent ichat = null;
        final Matcher matcher = URL_PATTERN.matcher(string);
        int lastEnd = 0;

        // Find all urls
        while (matcher.find()) {
            final int start = matcher.start();
            final int end = matcher.end();

            // Append the previous left overs.
            final String part = string.substring(lastEnd, start);
            if (part.length() > 0) {
                if (ichat == null) {
                    ichat = new StringTextComponent(part);
                } else {
                    ichat.appendText(part);
                }
            }
            lastEnd = end;
            String url = string.substring(start, end);
            final ITextComponent link = new StringTextComponent(url);

            try {
                // Add schema so client doesn't crash.
                if ((new URI(url)).getScheme() == null) {
                    if (!allowMissingHeader) {
                        if (ichat == null) {
                            ichat = new StringTextComponent(url);
                        } else {
                            ichat.appendText(url);
                        }
                        continue;
                    }
                    url = "http://" + url;
                }
            } catch (final URISyntaxException e) {
                // Bad syntax bail out!
                if (ichat == null) {
                    ichat = new StringTextComponent(url);
                } else {
                    ichat.appendText(url);
                }
                continue;
            }

            // Set the click event and append the link.
            final ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            link.getStyle().setClickEvent(click);
            link.getStyle().setUnderlined(true);
            link.getStyle().setColor(TextFormatting.BLUE);
            ichat = ichat == null ? link : ichat.appendSibling(link);
        }

        // Append the rest of the message.
        final String end = string.substring(lastEnd);
        if (ichat == null) {
            ichat = new StringTextComponent(end);
        } else if (end.length() > 0) {
            ichat.appendText(string.substring(lastEnd));
        }
        return ichat;
    }

    private static final class Category {

        protected final ITextComponent name;
        protected final String rawName;
        private final List<Entry> entries = new ArrayList<>();

        public Category(final String name) {
            this.name = new StringTextComponent(name)
                .applyTextStyle(TextFormatting.BOLD)
                .applyTextStyle(TextFormatting.UNDERLINE);
            this.rawName = name;
        }

        public Category(final String name, final List<Entry> entries) {
            this(name);
            this.entries.addAll(entries);
        }

        public Category addEntry(final Entry entry) {
            this.entries.add(entry);
            return this;
        }

        public Category setEntries(final List<Entry> entries) {
            this.entries.clear();
            this.entries.addAll(entries);
            return this;
        }

        public Category clearEntries() {
            this.entries.clear();
            return this;
        }

        public List<Entry> getEntries() {
            return Collections.unmodifiableList(this.entries);
        }
    }

    private static final class Entry {

        protected final int level;
        @Nullable protected final String rawKey;
        @Nullable protected final String rawValue;
        @Nullable protected ITextComponent key;
        @Nullable protected ITextComponent value;
        @Nullable protected Bounds valueBounds;

        public Entry(@Nullable final String key, @Nullable final String value) {
            this(key, value, 0, null);
        }

        public Entry(@Nullable final String key, @Nullable final String value, final int level) {
            this(key, value, level, null);
        }

        public Entry(@Nullable final String key, @Nullable final String value, final int level, @Nullable final String originalValue) {
            if (key != null) {
                this.key = new StringTextComponent(key);
            }
            this.rawKey = key;

            if (value != null) {
                this.value = new StringTextComponent(value).applyTextStyle(TextFormatting.GRAY);

                // Account for text components that were split to new lines
                if (originalValue != null) {
                    final ITextComponent linkComponent = newChatWithLinks(originalValue, false);
                    if (linkComponent.getStyle().getClickEvent() != null) {
                        this.value.setStyle(linkComponent.getStyle());
                    }
                }
            }
            this.rawValue = value;

            this.level = level;
        }
    }
}
