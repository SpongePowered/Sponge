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
package org.spongepowered.common.entity.player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.UserInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.storage.SaveHandlerAccessor;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.InvulnerableTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.player.BedLocationHolderBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.service.server.permission.SpongeBridgeSubject;
import org.spongepowered.common.service.server.permission.SubjectHelper;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class SpongeUser implements User, DataSerializable, BedLocationHolderBridge, SpongeMutableDataHolder, SpongeBridgeSubject, SubjectBridge,
        DataCompoundHolder, InvulnerableTrackedBridge, VanishableBridge {

    public static final Set<SpongeUser> dirtyUsers = ConcurrentHashMap.newKeySet();
    public static final Set<SpongeUser> initializedUsers = ConcurrentHashMap.newKeySet();

    private final GameProfile profile;
    private final Map<ResourceKey, RespawnLocation> spawnLocations = Maps.newHashMap();

    private ResourceKey worldKey = SpongeWorldManager.VANILLA_OVERWORLD;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean invulnerable;
    private boolean isVanished;
    private boolean isInvisible;
    private boolean isVanishCollide;
    private boolean isVanishTarget;

    @Nullable private SubjectReference subjectReference;
    @Nullable private SpongeUserInventory inventory; // lazy load when accessing inventory
    @Nullable private EnderChestInventory enderChest; // lazy load when accessing inventory
    @Nullable private CompoundNBT compound;
    private boolean isConstructing;

    public SpongeUser(final GameProfile profile) {
        this.profile = profile;
        SubjectHelper.applySubject(this);
    }

    private void reset() {
        this.spawnLocations.clear();
    }

    public boolean isInitialized() {
        return this.compound != null;
    }

    @Override
    public Mutable delegateDataHolder() {
        return this.getDataHolder(true);
    }

    public DataHolder.Mutable getDataHolder(final boolean markDirty) {
        if (this.isOnline()) {
            return this.getPlayer().get();
        }
        if (!this.isInitialized()) {
            this.initialize();
        }
        if (markDirty) {
            this.markDirty();
        }
        return this;
    }

    public void invalidate() {
        this.compound = null;
        this.inventory = null;
        this.enderChest = null;

        ((CustomDataHolderBridge) (Object) this).bridge$getFailedData().clear();
        initializedUsers.remove(this);
    }

    public void initializeIfRequired() {
        if (!this.isInitialized()) {
            this.initialize();
        }
    }

    public void initialize() {
        initializedUsers.add(this);
        this.compound = new CompoundNBT();
        final ServerWorld world = ((SpongeWorldManager) Sponge.getServer().getWorldManager()).getDefaultWorld();
        if (world == null) {
            return;
        }

        // Note: Uses the overworld's player data
        final SaveHandlerAccessor saveHandler = (SaveHandlerAccessor) world.getSaveHandler();
        final File file = new File(saveHandler.accessor$getPlayersDirectory(),
            this.profile.getId().toString() + ".dat");
        if (!file.exists()) {
            return;
        }

        try {
            try (final FileInputStream in = new FileInputStream(file)) {
                this.readFromNBT(CompressedStreamTools.readCompressed(in));
            }
        } catch (final IOException e) {
            SpongeCommon.getLogger().warn("Corrupt user file '{}'!", file, e);
        }
    }

    public void readFromNBT(final CompoundNBT compound) {
        this.reset();
        this.compound = compound;

        if (!compound.contains(Constants.Sponge.World.KEY)) {
            if (compound.contains(Constants.Sponge.World.DIMENSION_ID)) {
                final DimensionType type = DimensionType.getById(compound.getInt(Constants.Sponge.World.DIMENSION_ID));

                if (type != null) {
                    this.worldKey = (ResourceKey) (Object) Registry.DIMENSION_TYPE.getKey(type);
                }
            }
        } else {
            this.worldKey = ResourceKey.resolve(compound.getString(Constants.Sponge.World.KEY));
        }
        final ListNBT position = compound.getList(Constants.Entity.ENTITY_POSITION, Constants.NBT.TAG_DOUBLE);
        final ListNBT rotation = compound.getList(Constants.Entity.ENTITY_ROTATION, Constants.NBT.TAG_FLOAT);
        this.x = position.getDouble(0);
        this.y = position.getDouble(1);
        this.z = position.getDouble(2);
        this.yaw = rotation.getFloat(0);
        this.pitch = rotation.getFloat(1);

        this.invulnerable = compound.getBoolean(Constants.Entity.Player.INVULNERABLE);
        final CompoundNBT spongeCompound = compound.getCompound(Constants.Forge.FORGE_DATA).getCompound(Constants.Sponge.SPONGE_DATA);
        this.isConstructing = true;
        CustomDataHolderBridge.syncTagToCustom(this);
        this.isConstructing = false;

        if (spongeCompound.isEmpty()) {
            return;
        }

        this.isVanished = spongeCompound.getBoolean(Constants.Sponge.Entity.IS_VANISHED);
        this.isInvisible = spongeCompound.getBoolean(Constants.Sponge.Entity.IS_INVISIBLE);
        if (this.isVanished) {
            this.isVanishTarget = spongeCompound.getBoolean(Constants.Sponge.Entity.VANISH_UNTARGETABLE);
            this.isVanishCollide = spongeCompound.getBoolean(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE);
        }

        final ListNBT spawns = spongeCompound.getList(Constants.Sponge.User.USER_SPAWN_LIST, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < spawns.size(); i++) {
            final CompoundNBT spawnCompound = spawns.getCompound(i);

            if (!spawnCompound.contains(Constants.Sponge.World.KEY)) {
                continue;
            }

            final ResourceKey key = ResourceKey.resolve(spawnCompound.getString(Constants.Sponge.World.KEY));
            final double xPos = spawnCompound.getDouble(Constants.Sponge.User.USER_SPAWN_X);
            final double yPos = spawnCompound.getDouble(Constants.Sponge.User.USER_SPAWN_Y);
            final double zPos = spawnCompound.getDouble(Constants.Sponge.User.USER_SPAWN_Z);
            final boolean forced = spawnCompound.getBoolean(Constants.Sponge.User.USER_SPAWN_FORCED);
            this.spawnLocations.put(key, new RespawnLocation.Builder()
                .world(key)
                .position(new Vector3d(xPos, yPos, zPos))
                .forceSpawn(forced)
                .build());
        }
    }

    private UserInventory loadInventory() {
        if (this.inventory == null) {
            if (!this.isInitialized()) {
                this.initialize();
            }
            this.inventory = new SpongeUserInventory(this);
            final ListNBT listNBT = this.compound.getList(Constants.Entity.Player.INVENTORY, 10);
            this.inventory.readFromNBT(listNBT);
            this.inventory.currentItem = this.compound.getInt(Constants.Entity.Player.SELECTED_ITEM_SLOT);
        }
        return (UserInventory) this.inventory;
    }

    private SpongeUser loadEnderInventory() {
        if (this.enderChest == null) {
            if (!this.isInitialized()) {
                this.initialize();
            }
            this.enderChest = new SpongeUserInventoryEnderchest(this);
            if (this.compound.contains(Constants.Entity.Player.ENDERCHEST_INVENTORY, 9)) {
                final ListNBT nbttaglist1 = this.compound.getList(Constants.Entity.Player.ENDERCHEST_INVENTORY, 10);
                this.enderChest.read(nbttaglist1);
            }
        }
        return this;
    }

    public void writeToNbt(final CompoundNBT compound) {

        compound.putString(Constants.Sponge.World.KEY, this.worldKey.getFormatted());

        this.loadInventory();
        this.loadEnderInventory();

        compound.put(Constants.Entity.Player.INVENTORY, this.inventory.writeToNBT(new ListNBT()));
        compound.put(Constants.Entity.Player.ENDERCHEST_INVENTORY, this.enderChest.write());
        compound.putInt(Constants.Entity.Player.SELECTED_ITEM_SLOT, this.inventory.currentItem);

        compound.put(Constants.Entity.ENTITY_POSITION, Constants.NBT.newDoubleNBTList(this.x, this.y, this.z));
        compound.put(Constants.Entity.ENTITY_ROTATION, Constants.NBT.newFloatNBTList(this.yaw, this.pitch));

        compound.putBoolean(Constants.Entity.Player.INVULNERABLE, this.invulnerable);

        final CompoundNBT forgeCompound = compound.getCompound(Constants.Forge.FORGE_DATA);
        final CompoundNBT spongeCompound = forgeCompound.getCompound(Constants.Sponge.SPONGE_DATA);
        spongeCompound.remove(Constants.Sponge.User.USER_SPAWN_LIST);
        spongeCompound.remove(Constants.Sponge.Entity.IS_VANISHED);
        spongeCompound.remove(Constants.Sponge.Entity.IS_INVISIBLE);
        spongeCompound.remove(Constants.Sponge.Entity.VANISH_UNTARGETABLE);
        spongeCompound.remove(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE);

        final ListNBT spawns = new ListNBT();
        for (final Map.Entry<ResourceKey, RespawnLocation> entry : this.spawnLocations.entrySet()) {
            final RespawnLocation respawn = entry.getValue();

            final CompoundNBT spawnCompound = new CompoundNBT();
            spawnCompound.putString(Constants.Sponge.World.KEY, respawn.getWorldKey().getFormatted());
            spawnCompound.putDouble(Constants.Sponge.User.USER_SPAWN_X, respawn.getPosition().getX());
            spawnCompound.putDouble(Constants.Sponge.User.USER_SPAWN_Y, respawn.getPosition().getY());
            spawnCompound.putDouble(Constants.Sponge.User.USER_SPAWN_Z, respawn.getPosition().getZ());
            spawnCompound.putBoolean(Constants.Sponge.User.USER_SPAWN_FORCED, false); // No way to know
            spawns.add(spawnCompound);
        }

        if (!spawns.isEmpty()) {
            spongeCompound.put(Constants.Sponge.User.USER_SPAWN_LIST, spawns);
        }
        if (this.isVanished) {
            spongeCompound.putBoolean(Constants.Sponge.Entity.IS_VANISHED, true);
            spongeCompound.putBoolean(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE, this.isVanishCollide);
            spongeCompound.putBoolean(Constants.Sponge.Entity.VANISH_UNTARGETABLE, this.isVanishTarget);
        }
        if (this.isInvisible) {
            spongeCompound.putBoolean(Constants.Sponge.Entity.IS_INVISIBLE, true);
        }

        forgeCompound.put(Constants.Sponge.SPONGE_DATA, spongeCompound);
        compound.put(Constants.Forge.FORGE_DATA, forgeCompound);
    }

    @Override
    public UUID getUniqueId() {
        return this.profile.getId();
    }

    @Override
    public String getName() {
        return this.profile.getName();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        // TODO More data
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Constants.Entity.Player.UUID, this.profile.getId())
            .set(Constants.Entity.Player.NAME, this.profile.getName())
            .set(Constants.Entity.Player.SPAWNS, this.spawnLocations);
    }

    @Override
    public boolean canEquip(final EquipmentType type) {
        return true;
    }

    @Override
    public boolean canEquip(final EquipmentType type, @Nullable final ItemStack equipment) {
        return true;
    }

    @Override
    public Optional<ItemStack> getEquipped(final EquipmentType type) {
        throw new MissingImplementationException("SpongeUser", "getEquipped");
    }

    @Override
    public boolean equip(final EquipmentType type, @Nullable final ItemStack equipment) {
        if (this.canEquip(type, equipment)) {
            this.loadInventory();
            this.setEquippedItem(type, equipment);
            return true;
        }
        return false;
    }

    @Override
    public UserInventory getInventory() {
        return this.loadInventory();
    }

    @Override
    public EquipmentInventory getEquipment() {
        return this.getInventory().getEquipment();
    }

    @Override
    public ItemStack getItemInHand(final HandType handType) {
        if (handType == HandTypes.MAIN_HAND.get()) {
            this.getEquipped(EquipmentTypes.MAIN_HAND).orElseThrow(IllegalStateException::new);
        } else if (handType == HandTypes.OFF_HAND.get()) {
            this.getEquipped(EquipmentTypes.OFF_HAND).orElseThrow(IllegalStateException::new);
        }
        throw new IllegalArgumentException("Invalid hand " + handType);
    }

    @Override
    public ItemStack getHead() {
        return this.getEquipped(EquipmentTypes.HEAD).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setHead(final ItemStack helmet) {
        this.equip(EquipmentTypes.HEAD, helmet);
    }

    @Override
    public ItemStack getChest() {
        return this.getEquipped(EquipmentTypes.CHEST).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setChest(final ItemStack chestplate) {
        this.equip(EquipmentTypes.CHEST, chestplate);
    }

    @Override
    public ItemStack getLegs() {
        return this.getEquipped(EquipmentTypes.LEGS).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setLegs(final ItemStack leggings) {
        this.equip(EquipmentTypes.LEGS, leggings);
    }

    @Override
    public ItemStack getFeet() {
        return this.getEquipped(EquipmentTypes.FEET).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setFeet(final ItemStack boots) {
        this.equip(EquipmentTypes.FEET, boots);
    }

    @Override
    public void setItemInHand(final HandType handType, @Nullable final ItemStack itemInHand) {
        if (handType == HandTypes.MAIN_HAND.get()) {
            this.setEquippedItem(EquipmentTypes.MAIN_HAND, itemInHand);
        } else if (handType == HandTypes.OFF_HAND.get()) {
            this.setEquippedItem(EquipmentTypes.OFF_HAND, itemInHand);
        } else {
            throw new IllegalArgumentException("Invalid hand " + handType);
        }
    }

    @Override
    public Map<ResourceKey, RespawnLocation> bridge$getBedlocations() {
        final Optional<ServerPlayer> player = this.getPlayer();
        return player
            .map(value -> ((BedLocationHolderBridge) value).bridge$getBedlocations())
            .orElse(this.spawnLocations);
    }

    @Override
    public boolean bridge$setBedLocations(final Map<ResourceKey, RespawnLocation> value) {
        final Optional<ServerPlayer> player = this.getPlayer();
        if (player.isPresent()) {
            return ((BedLocationHolderBridge) player.get()).bridge$setBedLocations(value);
        }
        this.spawnLocations.clear();
        this.spawnLocations.putAll(value);
        this.markDirty();
        return true;
    }

    @Override
    public ImmutableMap<ResourceKey, RespawnLocation> bridge$removeAllBeds() {
        final Optional<ServerPlayer> player = this.getPlayer();
        if (player.isPresent()) {
            return ((BedLocationHolderBridge) player.get()).bridge$removeAllBeds();
        }
        final ImmutableMap<ResourceKey, RespawnLocation> locations = ImmutableMap.copyOf(this.spawnLocations);
        this.spawnLocations.clear();
        this.markDirty();
        return locations;
    }

    public void markDirty() {
        if (this.isConstructing) {
            return;
        }
        if (!this.isInitialized()) {
            SpongeCommon.getLogger()
                    .warn("Unable to mark user data for [{}] as dirty, data is not initialized! Any changes may be lost.",
                            this.profile.getId());
        } else {
            dirtyUsers.add(this);
        }
    }

    public void save() {
        Preconditions.checkState(this.isInitialized(), "User {} is not initialized", this.profile.getId());
        final SaveHandlerAccessor saveHandler = (SaveHandlerAccessor) ((SpongeWorldManager) Sponge.getServer().getWorldManager()).getDefaultWorld();
        final File dataFile = new File(saveHandler.accessor$getPlayersDirectory(), this.getUniqueId() + ".dat");
        CompoundNBT compound;
        try {
            compound = CompressedStreamTools.readCompressed(new FileInputStream(dataFile));
        } catch (final IOException ignored) {
            // Nevermind
            compound = new CompoundNBT();
        }
        this.writeToNbt(compound);
        try (final FileOutputStream out = new FileOutputStream(dataFile)) {
            CompressedStreamTools.writeCompressed(compound, out);
            dirtyUsers.remove(this);
            this.invalidate();
        } catch (final IOException e) {
            SpongeCommon.getLogger().warn("Failed to save user file [{}]!", dataFile, e);
        }
    }

    // Helpers for Equipment:

    private void setEquippedItem(final Supplier<? extends EquipmentType> type, @Nullable final ItemStack item) {
        this.setEquippedItem(type.get(), item);
    }

    private void setEquippedItem(final EquipmentType type, @Nullable final ItemStack item) {
        throw new MissingImplementationException("SpongeUser", "setEquippedItem");
    }

    private net.minecraft.item.ItemStack getItemStackFromSlot(final EquipmentSlotType slotIn) {
        this.loadInventory();
        if (slotIn == EquipmentSlotType.MAINHAND) {
            return this.inventory.getCurrentItem();
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            return this.inventory.offHandInventory.get(0);
        } else {
            return slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR ? this.inventory.armorInventory.get(
                slotIn.getIndex()) :
                net.minecraft.item.ItemStack.EMPTY;
        }
    }

    private void setItemStackToSlot(final EquipmentSlotType slotIn, final net.minecraft.item.ItemStack stack) {
        this.loadInventory();
        if (slotIn == EquipmentSlotType.MAINHAND) {
            this.inventory.mainInventory.set(this.inventory.currentItem, stack);
        } else if (slotIn == EquipmentSlotType.OFFHAND) {
            this.inventory.offHandInventory.set(0, stack);
        } else if (slotIn.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            this.inventory.armorInventory.set(slotIn.getIndex(), stack);
        }
    }

    @Override
    public org.spongepowered.api.profile.GameProfile getProfile() {
        return (org.spongepowered.api.profile.GameProfile) this.profile;
    }

    @Override
    public boolean isOnline() {
        return this.getPlayer().isPresent();
    }

    @Override
    public Optional<ServerPlayer> getPlayer() {
        return Optional.ofNullable((ServerPlayer) SpongeCommon.getServer().getPlayerList().getPlayerByUUID(this.profile.getId()));
    }

    @Override
    public Vector3d getPosition() {
        return this.getPlayer()
                .map(Player::getPosition)
                .orElseGet(() -> new Vector3d(this.x, this.y, this.z));
    }

    @Override
    public ResourceKey getWorldKey() {
        final Optional<ServerPlayer> player = this.getPlayer();
        return player.map(serverPlayer -> serverPlayer.getWorld().getKey()).orElseGet(() -> this.worldKey);

    }

    @Override
    public boolean setLocation(final ResourceKey key, final Vector3d position) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(position);

        final Optional<ServerPlayer> player = this.getPlayer();
        if (player.isPresent()) {
            final Optional<org.spongepowered.api.world.server.ServerWorld> world = Sponge.getServer().getWorldManager().getWorld(key);
            return world.filter(serverWorld -> player.get().setLocation(ServerLocation.of(serverWorld, position))).isPresent();
        }

        this.worldKey = key;
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
        this.markDirty();
        return true;
    }

    @Override
    public Vector3d getRotation() {
        return this.getPlayer()
                .map(Entity::getRotation)
                .orElseGet(() -> new Vector3d(this.pitch, this.yaw, 0));
    }

    @Override
    public void setRotation(final Vector3d rotation) {
        Preconditions.checkNotNull(rotation, "Rotation was null!");
        final Optional<ServerPlayer> playerOpt = this.getPlayer();
        if (playerOpt.isPresent()) {
            playerOpt.get().setRotation(rotation);
            return;
        }
        this.markDirty();
        this.pitch = ((float) rotation.getX()) % 360.0F;
        this.yaw = ((float) rotation.getY()) % 360.0F;
    }

    @Override
    public String getIdentifier() {
        return this.profile.getId().toString();
    }

    @Override
    public Inventory getEnderChestInventory() {
        final Optional<ServerPlayer> playerOpt = this.getPlayer();
        if (playerOpt.isPresent()) {
            return playerOpt.get().getEnderChestInventory();
        }
        this.loadEnderInventory();
        return ((Inventory) this.enderChest);
    }

    @Override
    public void bridge$setSubject(final SubjectReference subj) {
        this.subjectReference = subj;
    }

    @Override
    public Optional<SubjectReference> bridge$resolveReferenceOptional() {
        if (this.subjectReference == null) {
            SubjectHelper.applySubject(this);
        }
        return Optional.ofNullable(this.subjectReference);
    }

    @Override
    public Optional<Subject> bridge$resolveOptional() {
        return this.bridge$resolveReferenceOptional().map(SubjectReference::resolve).map(CompletableFuture::join);
    }

    @Override
    public Subject bridge$resolve() {
        return this.bridge$resolveOptional()
                .orElseThrow(() -> new IllegalStateException("No subject reference present for user " + this));
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.FALSE;
    }


    @Override
    public void bridge$setInvulnerable(final boolean value) {
        final Optional<ServerPlayer> playerOpt = this.getPlayer();
        if (playerOpt.isPresent()) {
            ((InvulnerableTrackedBridge) playerOpt.get()).bridge$setInvulnerable(value);
            return;
        }
        this.invulnerable = value;
        this.markDirty();
    }

    @Override
    public boolean bridge$getIsInvulnerable() {
        return this.invulnerable;
    }

    @Override
    public void bridge$setVanished(final boolean vanished) {
        final Optional<ServerPlayer> playerOpt = this.getPlayer();
        if (playerOpt.isPresent()) {
            ((VanishableBridge) playerOpt.get()).bridge$setVanished(vanished);
            return;
        }
        this.isVanished = vanished;
        this.markDirty();
    }

    @Override
    public boolean bridge$isVanished() {
        return this.getPlayer().map(player -> ((VanishableBridge) player).bridge$isVanished()).orElseGet(() -> this.isVanished);
    }

    @Override
    public boolean bridge$isInvisible() {
        return this.getPlayer().map(player -> ((VanishableBridge) player).bridge$isInvisible()).orElseGet(() -> this.isInvisible);
    }

    @Override
    public void bridge$setInvisible(final boolean invisible) {
        final Optional<ServerPlayer> player = this.getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setInvisible(invisible);
            return;
        }
        this.isInvisible = invisible;
    }

    @Override
    public boolean bridge$isUncollideable() {
        return this.getPlayer().map(player -> ((VanishableBridge) player).bridge$isUncollideable()).orElseGet(() -> this.isVanishCollide);
    }

    @Override
    public void bridge$setUncollideable(final boolean uncollideable) {
        final Optional<ServerPlayer> player = this.getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setUncollideable(uncollideable);
            return;
        }
        this.isVanishCollide = uncollideable;
    }

    @Override
    public boolean bridge$isUntargetable() {
        return this.getPlayer().map(player -> ((VanishableBridge) player).bridge$isUntargetable()).orElseGet(() -> this.isVanishTarget);
    }

    @Override
    public void bridge$setUntargetable(final boolean untargetable) {
        final Optional<ServerPlayer> player = this.getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setUntargetable(untargetable);
            return;
        }
        this.isVanishTarget = untargetable;
    }

    @Override
    public CompoundNBT data$getCompound() {
        return this.compound;
    }

    @Override
    public void data$setCompound(CompoundNBT nbt) {
        this.compound = nbt;
    }

    @Override
    public NBTDataType data$getNbtDataType() {
        return NBTDataTypes.PLAYER;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final SpongeUser other = (SpongeUser) obj;
        return this.profile.getId().equals(other.profile.getId());
    }

    @Override
    public int hashCode() {
        return this.profile.getId().hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("isOnline", this.isOnline())
            .add("profile", this.getProfile())
            .toString();
    }
}
