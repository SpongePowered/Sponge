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
package org.spongepowered.common.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.ValueType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import ninja.leaping.configurate.util.ConfigurationNodeWalker;
import org.spongepowered.api.util.Functional;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.type.ConfigBase;
import org.spongepowered.common.config.type.CustomDataConfig;
import org.spongepowered.common.config.type.DimensionConfig;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.util.IpSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import javax.annotation.Nullable;

public class SpongeConfig<T extends ConfigBase> {

    public enum Type {
        CUSTOM_DATA(CustomDataConfig.class),
        TRACKER(TrackerConfig.class),
        GLOBAL(GlobalConfig.class),
        DIMENSION(DimensionConfig.class),
        WORLD(WorldConfig.class);

        final Class<? extends ConfigBase> type;

        Type(Class<? extends ConfigBase> type) {
            this.type = type;
        }
    }

    private static final String HEADER = "1.0\n"
            + "\n"
            + "# If you need help with the configuration or have any questions related to Sponge,\n"
            + "# join us at the IRC or drop by our forums and leave a post.\n"
            + "\n"
            + "# IRC: #sponge @ irc.esper.net ( https://webchat.esper.net/?channel=sponge )\n"
            + "# Forums: https://forums.spongepowered.org/\n";

    private static final ConfigurationOptions LOADER_OPTIONS = ConfigurationOptions.defaults()
            .setHeader(HEADER)
            .setSerializers(TypeSerializers.getDefaultSerializers().newChild()
                    .registerType(TypeToken.of(IpSet.class), new IpSet.IpSetSerializer())
            );

    /**
     * The type of this config instance
     */
    private final Type type;

    /**
     * The parent configuration - values are inherited from this
     */
    @Nullable private final SpongeConfig<?> parent;

    /**
     * The loader (mapped to a file) used to read/write the config to disk
     */
    private HoconConfigurationLoader loader;

    /**
     * A node representation of "whats actually in the file".
     */
    private CommentedConfigurationNode fileData = SimpleCommentedConfigurationNode.root(LOADER_OPTIONS);

    /**
     * A node representation of {@link #fileData}, merged with the data of {@link #parent}.
     */
    private CommentedConfigurationNode data = SimpleCommentedConfigurationNode.root(LOADER_OPTIONS);

    /**
     * The mapper instance used to populate the config instance
     */
    private ObjectMapper<T>.BoundInstance configMapper;

    private final String modId;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SpongeConfig(Type type, Path path, String modId, SpongeConfig<?> parent) {
        this.type = type;
        this.parent = parent;
        this.modId = modId;

        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            this.configMapper = (ObjectMapper.BoundInstance) ObjectMapper.forClass(this.type.type).bindToNew();

            reload();
            saveNow();
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize configuration", e);
        }
    }

    public T getConfig() {
        return this.configMapper.getInstance();
    }

    public void save() {
        SpongeImpl.getConfigSaveManager().save(this);
    }

    public boolean saveNow() {
        try {
            // save from the mapped object --> node
            CommentedConfigurationNode saveNode = SimpleCommentedConfigurationNode.root(LOADER_OPTIONS);
            this.configMapper.serialize(saveNode.getNode(this.modId));

            // before saving this config, remove any values already declared with the same value on the parent
            if (this.parent != null) {
                removeDuplicates(saveNode);
            }

            // merge the values we need to write with the ones already declared in the file
            saveNode.mergeValuesFrom(this.fileData);

            // save the data to disk
            this.loader.save(saveNode);
            return true;
        } catch (IOException | ObjectMappingException e) {
            SpongeImpl.getLogger().error("Failed to save configuration", e);
            return false;
        }
    }

    public void reload() {
        if (!SpongeImpl.getConfigSaveManager().flush(this)) {
            // Can't reload
            SpongeImpl.getLogger().error("Failed to load configuration due to error in flushing config");
            return;
        }

        try {
            // load settings from file
            CommentedConfigurationNode loadedNode = this.loader.load();

            // attempt to strip duplicate settings from the file
            // (this only happens on the first pass of the file - see javadocs below)
            if (cleanupConfig(loadedNode)) {
                this.loader.save(loadedNode);
            }

            // store "what's in the file" separately in memory
            this.fileData = loadedNode;

            // make a copy of the file data
            this.data = this.fileData.copy();

            // merge with settings from parent
            if (this.parent != null) {
                this.data.mergeValuesFrom(this.parent.data);
            }

            // populate the config object
            populateInstance();
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to load configuration", e);
        }
    }

    private void populateInstance() throws ObjectMappingException {
        this.configMapper.populate(this.data.getNode(this.modId));
    }

    /**
     * Performs a cleanup operation on the given configuration node, taking into
     * account the legacy 'config-enabled' setting.
     *
     * See: https://github.com/SpongePowered/SpongeCommon/pull/1957#issuecomment-400761641
     *
     * @param root The node to cleanup
     * @return If the cleanup was able to occur, depending on the state of the 'config-enabled' setting
     */
    private boolean cleanupConfig(CommentedConfigurationNode root) {
        // we can't strip values from the global config, there won't be any duplicates there
        if (this.parent == null) {
            return false;
        }

        ConfigurationNode configEnabled = root.getNode(this.modId, "config-enabled");

        // if the node is missing, don't strip anything from the file
        // (this ensures the migration only happens on the first pass)
        if (configEnabled.isVirtual()) {
            return false;
        }

        boolean enabled = configEnabled.getBoolean(true);

        // remove the enabled property so migration doesn't happen again
        configEnabled.setValue(null);

        if (!enabled) {
            // config wasn't enabled, just clear it
            // (we don't wish to take account for any of the settings defined here)
            root.getNode(this.modId).setValue(null);
        } else {
            // config was enabled, but we want to strip out duplicated values
            // but keep any overrides
            removeDuplicates(root);
        }

        return true;
    }

    /**
     * Traverses the given {@code root} config node, removing any values which
     * are also present and set to the same value on this configs "parent".
     *
     * @param root The node to process
     */
    private void removeDuplicates(CommentedConfigurationNode root) {
        if (this.parent == null) {
            throw new IllegalStateException("parent is null");
        }

        Iterator<ConfigurationNodeWalker.VisitedNode<CommentedConfigurationNode>> it = ConfigurationNodeWalker.DEPTH_FIRST_POST_ORDER.walkWithPath(root);
        while (it.hasNext()) {
            ConfigurationNodeWalker.VisitedNode<CommentedConfigurationNode> next = it.next();
            CommentedConfigurationNode node = next.getNode();

            // remove empty maps
            if (node.hasMapChildren()) {
                if (node.getChildrenMap().isEmpty()) {
                    node.setValue(null);
                }
                continue;
            }

            // ignore list values
            if (node.getParent() != null && node.getParent().getValueType() == ValueType.LIST) {
                continue;
            }

            // if the node already exists in the parent config, remove it
            CommentedConfigurationNode parentValue = this.parent.data.getNode(next.getPath().getArray());
            if (Objects.equals(node.getValue(), parentValue.getValue())) {
                node.setValue(null);
            }
        }
    }

    public CompletableFuture<CommentedConfigurationNode> updateSetting(String key, Object value) {
        return Functional.asyncFailableFuture(() -> {
            CommentedConfigurationNode upd = getSetting(key);
            upd.setValue(value);
            populateInstance();
            saveNow();
            return upd;
        }, ForkJoinPool.commonPool());
    }

    public CommentedConfigurationNode getRootNode() {
        return this.data.getNode(this.modId);
    }

    @Nullable
    public CommentedConfigurationNode getSetting(String key) {
        if (key.equalsIgnoreCase("config-enabled")) {
            return getRootNode().getNode(key);
        } else if (!key.contains(".") || key.indexOf('.') == key.length() - 1) {
            return null;
        } else {
            String category = key.substring(0, key.indexOf('.'));
            String prop = key.substring(key.indexOf('.') + 1);
            return getRootNode().getNode(category, prop);
        }
    }

    public Type getType() {
        return this.type;
    }

}
