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
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.Types;
import ninja.leaping.configurate.ValueType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMapper.BoundInstance;
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
import java.util.List;
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
    private final boolean isDummy;

    public static <T extends ConfigBase> SpongeConfig<T> newDummyConfig(Type type) {
        return new SpongeConfig<>(type);
    }

    @SuppressWarnings("unchecked")
    private SpongeConfig(Type type) {
        this.type = type;
        this.parent = null;
        this.modId = null;
        this.isDummy = true;

        try {
            this.configMapper = (ObjectMapper.BoundInstance) ObjectMapper.forClass(this.type.type).bindToNew();
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize dummy configuration", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SpongeConfig(Type type, Path path, String modId, SpongeConfig<?> parent, boolean forceSaveOnLoad) {
        this.type = type;
        this.parent = parent;
        this.modId = modId;
        this.isDummy = false;

        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

            this.loader = HoconConfigurationLoader.builder().setPath(path).build();
            this.configMapper = (ObjectMapper.BoundInstance) ObjectMapper.forClass(this.type.type).bindToNew();

            // If load fails, avoid saving as this can mess up world configs.
            if (!load()) {
                return;
            }
            // In order for the removeDuplicates method to function properly, it is extremely
            // important to avoid running save on parent BEFORE children save. Doing so will
            // cause duplicate nodes to not be removed properly as parent would have cleaned up
            // all duplicates prior.
            // To handle the above issue, we only call save for world configs during init.
            if (!forceSaveOnLoad && parent != null && parent.parent != null) {
                saveNow();
            } else if (forceSaveOnLoad) {
                saveNow();
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to initialize configuration", e);
        }
    }

    public T getConfig() {
        return this.configMapper.getInstance();
    }

    public void save() {
        if (this.isDummy) {
            return;
        }
        SpongeImpl.getConfigSaveManager().save(this);
    }

    public boolean saveNow() {
        if (this.isDummy) {
            return false;
        }
        try {
            // save from the mapped object --> node
            CommentedConfigurationNode saveNode = SimpleCommentedConfigurationNode.root(LOADER_OPTIONS);
            this.configMapper.serialize(saveNode.getNode(this.modId));

            // before saving this config, remove any values already declared with the same value on the parent
            if (this.parent != null) {
                removeDuplicates(saveNode);
            }

            // save the data to disk
            this.loader.save(saveNode);

            // In order for the removeDuplicates method to function properly, it is extremely
            // important to avoid running save on parent BEFORE children save. Doing so will
            // cause duplicate nodes to not be removed as parent would have cleaned up
            // all duplicates prior.
            // To handle the above issue, we save AFTER saving child config.
            if (this.parent != null) {
                this.parent.saveNow();
            }
            return true;
        } catch (IOException | ObjectMappingException e) {
            SpongeImpl.getLogger().error("Failed to save configuration", e);
            return false;
        }
    }

    public boolean load() {
        if (this.isDummy) {
            return true;
        }
        if (!SpongeImpl.getConfigSaveManager().flush(this)) {
            // Can't reload
            SpongeImpl.getLogger().error("Failed to load configuration due to error in flushing config");
            return false;
        }

        try {
            // load settings from file
            CommentedConfigurationNode loadedNode = this.loader.load();

            // This is where we can inject versioning.
//            ConfigurationTransformation.versionedBuilder()
//                .addVersion(2, ConfigurationTransformation.builder()
//                    .addAction(path("optimizations",  "faster-thread-checks"), (input, value) -> {
//                    })
//                ).build();

            // store "what's in the file" separately in memory
            this.fileData = loadedNode;

            // make a copy of the file data
            this.data = this.fileData.copy();

            // merge with settings from parent
            if (this.parent != null) {
                this.parent.load();
                this.data.mergeValuesFrom(this.parent.data);
            }

            // populate the config object
            populateInstance();
            return true;
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to load configuration", e);
            return false;
        }
    }

    private void populateInstance() throws ObjectMappingException {
        if (this.isDummy) {
            return;
        }
        this.configMapper.populate(this.data.getNode(this.modId));
    }

    /**
     * Traverses the given {@code root} config node, removing any values which
     * are also present and set to the same value on this configs "parent".
     *
     * @param root The node to process
     */
    private void removeDuplicates(CommentedConfigurationNode root) {
        if (this.isDummy) {
            return;
        }
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
            } else {
                // Fix list bug
                if (parentValue.getValue() == null) {
                    if (node.getValueType() == ValueType.LIST) {
                        final List<?> nodeList = (List<?>) node.getValue();
                        if (nodeList.isEmpty()) {
                            node.setValue(null);
                        }
                        continue;
                    }
                }
                // Fix double bug
                final Double nodeVal = node.getValue(Types::asDouble);
                if (nodeVal != null) {
                    Double parentVal = parentValue.getValue(Types::asDouble);
                    if (parentVal == null && nodeVal.doubleValue() == 0 || (parentVal != null && nodeVal.doubleValue() == parentVal.doubleValue())) {
                        node.setValue(null);
                        continue;
                    }
                }
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

    public <V> CompletableFuture<CommentedConfigurationNode> updateSetting(String key, V value, TypeToken<V> token) {
        return Functional.asyncFailableFuture(() -> {
            CommentedConfigurationNode upd = getSetting(key);
            upd.setValue(token, value);
            populateInstance();
            save();
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
            CommentedConfigurationNode node = getRootNode();
            final String[] split = key.split("\\.");
            return node.getNode((Object[]) split);
        }
    }

    public Type getType() {
        return this.type;
    }

}
