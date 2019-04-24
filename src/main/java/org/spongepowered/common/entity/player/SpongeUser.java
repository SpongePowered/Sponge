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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.nbt.CustomDataNbtUtil;
import org.spongepowered.common.data.type.SpongeEquipmentType;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Implements things that are not implemented by mixins into this class. <p>This
 * class is concrete in order to create instances of User.</p>
 *
 * <p>List of mixins mixing into this class: <ul>
 * <li>MixinSpongeUser</li><li>MixinDataHolder</li><li>MixinSubject</li> </ul>
 *
 * TODO Future note about data: The following data manipulators are always
 * applicable to User: BanData, WhitelistData, JoinData
 */
public class SpongeUser implements ArmorEquipable, Tamer, DataSerializable, Carrier, ISpongeUser {

    public static final Set<SpongeUser> dirtyUsers = ConcurrentHashMap.newKeySet();

    private final User self = (User) this; // convenient access
    private final GameProfile profile;

    private final Map<UUID, RespawnLocation> spawnLocations = Maps.newHashMap();

    private double posX;
    private double posY;
    private double posZ;
    private int dimension;
    private float rotationYaw;
    private float rotationPitch;
    private boolean invulnerable;
    private boolean isVanished;

    private SpongeUserInventory inventory; // lazy load when accessing inventory
    private InventoryEnderChest enderChest; // lazy load when accessing inventory
    private NBTTagCompound nbt = new NBTTagCompound();

    public SpongeUser(GameProfile profile) {
        this.profile = profile;
    }

    private void reset() {
        this.spawnLocations.clear();
    }

    public void readFromNbt(NBTTagCompound compound) {
        this.reset();
        this.nbt = compound;

        // net.minecraft.entity.Entity#readFromNBT

        NBTTagList position = compound.getTagList(NbtDataUtil.ENTITY_POSITION, NbtDataUtil.TAG_DOUBLE);
        //NBTTagList motion = compound.getTagList("Motion", NbtDataUtil.TAG_DOUBLE);
        NBTTagList rotation = compound.getTagList(NbtDataUtil.ENTITY_ROTATION, NbtDataUtil.TAG_FLOAT);
        //this.motionX = motion.getDoubleAt(0);
        //this.motionY = motion.getDoubleAt(1);
        //this.motionZ = motion.getDoubleAt(2);
        //if (Math.abs(this.motionX) > 10.0D) {
        //    this.motionX = 0.0D;
        //}
        //if (Math.abs(this.motionY) > 10.0D) {
        //    this.motionY = 0.0D;
        //}
        //if (Math.abs(this.motionZ) > 10.0D) {
        //    this.motionZ = 0.0D;
        //}
        this.posX = position.getDoubleAt(0);
        this.posY = position.getDoubleAt(1);
        this.posZ = position.getDoubleAt(2);
        //this.lastTickPosX = this.posX;
        //this.lastTickPosY = this.posY;
        //this.lastTickPosZ = this.posZ;
        //this.prevPosX = this.posX;
        //this.prevPosY = this.posY;
        //this.prevPosZ = this.posZ;
        this.rotationYaw = rotation.getFloatAt(0);
        this.rotationPitch = rotation.getFloatAt(1);
        //this.prevRotationYaw = this.rotationYaw;
        //this.prevRotationPitch = this.rotationPitch;
        //this.fallDistance = compound.getFloat("FallDistance");
        //this.fire = compound.getShort("Fire");
        //this.setAir(compound.getShort("Air"));
        //this.onGround = compound.getBoolean("OnGround");

        if (compound.hasKey(NbtDataUtil.ENTITY_DIMENSION)) {
            this.dimension = compound.getInteger(NbtDataUtil.ENTITY_DIMENSION);
        } else {
            this.dimension = 0;
        }

        this.invulnerable = compound.getBoolean("Invulnerable");
        //this.timeUntilPortal = compound.getInteger("PortalCooldown");
        //if (compound.hasUniqueId("UUID")) {
        //    this.entityUniqueID = compound.getUniqueId("UUID");
        //    this.cachedUniqueIdString = this.entityUniqueID.toString();
        //}
        //this.setPosition(this.posX, this.posY, this.posZ);
        //this.setRotation(this.rotationYaw, this.rotationPitch);
        //if (compound.hasKey("CustomName", 8)) {
        //    this.setCustomNameTag(compound.getString("CustomName"));
        //}
        //this.setAlwaysRenderNameTag(compound.getBoolean("CustomNameVisible"));
        //this.cmdResultStats.readStatsFromNBT(compound);
        //this.setSilent(compound.getBoolean("Silent"));
        //this.setNoGravity(compound.getBoolean("NoGravity"));
        //this.setGlowing(compound.getBoolean("Glowing"));
        //updateBlocked = compound.getBoolean("UpdateBlocked"); //Forge
        //if (compound.hasKey("ForgeData")) customEntityData = compound.getCompoundTag("ForgeData"); //Forge
        //if (this.capabilities != null && compound.hasKey("ForgeCaps")) this.capabilities.deserializeNBT(compound.getCompoundTag("ForgeCaps")); //Forge


        // net.minecraft.entity.EntityLivingBase#readEntityFromNBT


        //this.absorptionAmount = compound.getFloat("AbsorptionAmount");
        //if (compound.hasKey("Attributes", 9) && this.world != null && !this.world.isRemote) {
        //    SharedMonsterAttributes.setAttributeModifiers(this.getAttributeMap(), compound.getTagList("Attributes", 10));
        //}
        //if (compound.hasKey("ActiveEffects", 9)) {
        //    NBTTagList nbttaglist = compound.getTagList("ActiveEffects", 10);
        //    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
        //        NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
        //        PotionEffect potioneffect = PotionEffect.readCustomPotionEffectFromNBT(nbttagcompound);
        //        if (potioneffect != null) {
        //            this.activePotionsMap.put(potioneffect.getPotion(), potioneffect);
        //        }
        //    }
        //}
        //if (compound.hasKey("Health", 99)) {
        //    this.setHealth(compound.getFloat("Health"));
        //}
        //this.hurtTime = compound.getShort("HurtTime");
        //this.deathTime = compound.getShort("DeathTime");
        //this.revengeTimer = compound.getInteger("HurtByTimestamp");
        //if (compound.hasKey("Team", 8)) {
        //    String s = compound.getString("Team");
        //    boolean flag = this.world.getScoreboard().addPlayerToTeam(this.getCachedUniqueIdString(), s);
        //    if (!flag) {
        //        LOGGER.warn("Unable to add mob to team \"" + s + "\" (that team probably doesn't exist)");
        //    }
        //}
        //if (compound.getBoolean("FallFlying")) {
        //    this.setFlag(7, true);
        //}


        // net.minecraft.entity.player.EntityPlayer#readEntityFromNBT


        //this.setUniqueId(getUUID(this.gameProfile));
        //NBTTagList nbttaglist = compound.getTagList("Inventory", 10);
        //this.inventory.readFromNBT(nbttaglist);
        //this.inventory.currentItem = compound.getInteger("SelectedItemSlot");
        //this.sleeping = compound.getBoolean("Sleeping");
        //this.sleepTimer = compound.getShort("SleepTimer");
        //this.experience = compound.getFloat("XpP");
        //this.experienceLevel = compound.getInteger("XpLevel");
        //this.experienceTotal = compound.getInteger("XpTotal");
        //this.xpSeed = compound.getInteger("XpSeed");
        //if (this.xpSeed == 0) {
        //    this.xpSeed = this.rand.nextInt();
        //}
        //this.setScore(compound.getInteger("Score"));
        //if (this.sleeping) {
        //    this.bedLocation = new BlockPos(this);
        //    this.wakeUpPlayer(true, true, false);
        //}
        //if (compound.hasKey("SpawnX", 99) && compound.hasKey("SpawnY", 99) && compound.hasKey("SpawnZ", 99)) {
        //    this.spawnPos = new BlockPos(compound.getInteger("SpawnX"), compound.getInteger("SpawnY"), compound.getInteger("SpawnZ"));
        //    this.spawnForced = compound.getBoolean("SpawnForced");
        //}
        //NBTTagList spawnlist = null;
        //spawnlist = compound.getTagList("Spawns", 10);
        //for (int i = 0; i < spawnlist.tagCount(); i++) {
        //    NBTTagCompound spawndata = (NBTTagCompound)spawnlist.getCompoundTagAt(i);
        //    int spawndim = spawndata.getInteger("Dim");
        //    this.spawnChunkMap.put(spawndim, new BlockPos(spawndata.getInteger("SpawnX"), spawndata.getInteger("SpawnY"), spawndata.getInteger("SpawnZ")));
        //    this.spawnForcedMap.put(spawndim, spawndata.getBoolean("SpawnForced"));
        //}
        //this.spawnDimension = compound.getBoolean("HasSpawnDimensionSet") ? compound.getInteger("SpawnDimension") : null;
        //this.foodStats.readNBT(compound);
        //this.capabilities.readCapabilitiesFromNBT(compound);
        //if (compound.hasKey("EnderItems", 9)) {
        //    NBTTagList nbttaglist1 = compound.getTagList("EnderItems", 10);
        //    this.enderChest.loadInventoryFromNBT(nbttaglist1);
        //}
        //if (compound.hasKey("ShoulderEntityLeft", 10)) {
        //    this.setLeftShoulderEntity(compound.getCompoundTag("ShoulderEntityLeft"));
        //}
        //if (compound.hasKey("ShoulderEntityRight", 10)) {
        //    this.setRightShoulderEntity(compound.getCompoundTag("ShoulderEntityRight"));
        //}


        // net.minecraft.entity.player.EntityPlayerMP#readEntityFromNBT


        //if (compound.hasKey("playerGameType", 99)) {
        //    if (this.getServer().getForceGamemode()) {
        //        this.interactionManager.setGameType(this.getServer().getGameType());
        //    } else {
        //        this.interactionManager.setGameType(GameType.getByID(compound.getInteger("playerGameType")));
        //    }
        //}
        //if (compound.hasKey("enteredNetherPosition", 10)) {
        //    NBTTagCompound nbttagcompound = compound.getCompoundTag("enteredNetherPosition");
        //    this.enteredNetherPosition = new Vec3d(nbttagcompound.getDouble("x"), nbttagcompound.getDouble("y"), nbttagcompound.getDouble("z"));
        //}
        //this.seenCredits = compound.getBoolean("seenCredits");
        //if (compound.hasKey("recipeBook", 10)) {
        //    this.recipeBook.read(compound.getCompoundTag("recipeBook"));
        //}


        // org.spongepowered.common.mixin.core.entity.MixinEntity#readFromNbt


        final NBTTagCompound spongeCompound = compound.getCompoundTag(NbtDataUtil.FORGE_DATA).getCompoundTag(NbtDataUtil.SPONGE_DATA);
        CustomDataNbtUtil.readCustomData(spongeCompound, ((DataHolder) this));
        //if (this instanceof IMixinGriefer && ((IMixinGriefer) this).isGriefer() && compound.hasKey(NbtDataUtil.CAN_GRIEF)) {
        //    ((IMixinGriefer) this).setCanGrief(compound.getBoolean(NbtDataUtil.CAN_GRIEF));
        //}


        // org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase#readFromNbt


        //if (compound.hasKey("maxAir")) {
        //    this.maxAir = compound.getInteger("maxAir");
        //}


        // org.spongepowered.common.mixin.core.entity.player.MixinEntityPlayerMP#readFromNbt


        //if (compound.hasKey(NbtDataUtil.HEALTH_SCALE, NbtDataUtil.TAG_DOUBLE)) {
        //    this.healthScaling = true;
        //    this.healthScale = compound.getDouble(NbtDataUtil.HEALTH_SCALE);
        //}

        // extra data

        if (!spongeCompound.isEmpty()) {
            this.isVanished = spongeCompound.getBoolean(NbtDataUtil.VANISH);

            final NBTTagList spawnList = spongeCompound.getTagList(NbtDataUtil.USER_SPAWN_LIST, NbtDataUtil.TAG_COMPOUND);

            for (int i = 0; i < spawnList.tagCount(); i++) {
                final NBTTagCompound spawnCompound = spawnList.getCompoundTagAt(i);

                final UUID uuid = spawnCompound.getUniqueId(NbtDataUtil.UUID);

                if (uuid.getLeastSignificantBits() != 0 && uuid.getMostSignificantBits() != 0) {
                    final double xPos = spawnCompound.getDouble(NbtDataUtil.USER_SPAWN_X);
                    final double yPos = spawnCompound.getDouble(NbtDataUtil.USER_SPAWN_Y);
                    final double zPos = spawnCompound.getDouble(NbtDataUtil.USER_SPAWN_Z);
                    final boolean forced = spawnCompound.getBoolean(NbtDataUtil.USER_SPAWN_FORCED);
                    this.spawnLocations.put(uuid, new RespawnLocation.Builder()
                            .forceSpawn(forced)
                            .position(new Vector3d(xPos, yPos, zPos))
                            .world(uuid)
                            .build());
                }
            }
        }
    }

    private SpongeUser loadInventory() {
        if (this.inventory == null) {
            this.inventory = new SpongeUserInventory(this);
            NBTTagList nbttaglist = this.nbt.getTagList(NbtDataUtil.Minecraft.INVENTORY, 10);
            this.inventory.readFromNBT(nbttaglist);
            this.inventory.currentItem = this.nbt.getInteger(NbtDataUtil.Minecraft.SELECTED_ITEM_SLOT);
        }
        return this;
    }

    private SpongeUser loadEnderInventory() {
        if (this.enderChest == null) {
            this.enderChest = new SpongeUserInventoryEnderchest(this);
            if (this.nbt.hasKey(NbtDataUtil.Minecraft.ENDERCHEST_INVENTORY, 9))
            {
                NBTTagList nbttaglist1 = this.nbt.getTagList(NbtDataUtil.Minecraft.ENDERCHEST_INVENTORY, 10);
                this.enderChest.loadInventoryFromNBT(nbttaglist1);
            }
        }
        return this;
    }

    public void writeToNbt(NBTTagCompound compound) {

        this.loadInventory();
        this.loadEnderInventory();
        compound.setTag(NbtDataUtil.Minecraft.INVENTORY, this.inventory.writeToNBT(new NBTTagList()));
        compound.setTag(NbtDataUtil.Minecraft.ENDERCHEST_INVENTORY, this.enderChest.saveInventoryToNBT());
        compound.setInteger(NbtDataUtil.Minecraft.SELECTED_ITEM_SLOT, this.inventory.currentItem);

        compound.setTag(NbtDataUtil.ENTITY_POSITION, NbtDataUtil.newDoubleNBTList(this.posX, this.posY, this.posZ));
        compound.setInteger(NbtDataUtil.ENTITY_DIMENSION, this.dimension);
        compound.setTag(NbtDataUtil.ENTITY_ROTATION, NbtDataUtil.newFloatNBTList(this.rotationYaw, this.rotationPitch));

        compound.setBoolean("Invulnerable", this.invulnerable);

        final NBTTagCompound forgeCompound = compound.getCompoundTag(NbtDataUtil.FORGE_DATA);
        final NBTTagCompound spongeCompound = forgeCompound.getCompoundTag(NbtDataUtil.SPONGE_DATA);
        spongeCompound.removeTag(NbtDataUtil.USER_SPAWN_LIST);
        spongeCompound.removeTag(NbtDataUtil.VANISH);

        final NBTTagList spawnList = new NBTTagList();
        for (Entry<UUID, RespawnLocation> entry : this.spawnLocations.entrySet()) {
            final RespawnLocation respawn = entry.getValue();

            final NBTTagCompound spawnCompound = new NBTTagCompound();
            spawnCompound.setUniqueId(NbtDataUtil.UUID, entry.getKey());
            spawnCompound.setDouble(NbtDataUtil.USER_SPAWN_X, respawn.getPosition().getX());
            spawnCompound.setDouble(NbtDataUtil.USER_SPAWN_Y, respawn.getPosition().getY());
            spawnCompound.setDouble(NbtDataUtil.USER_SPAWN_Z, respawn.getPosition().getZ());
            spawnCompound.setBoolean(NbtDataUtil.USER_SPAWN_FORCED, false); // No way to know
            spawnList.appendTag(spawnCompound);
        }

        if (!spawnList.isEmpty()) {
            spongeCompound.setTag(NbtDataUtil.USER_SPAWN_LIST, spawnList);
        }
        if (this.isVanished) {
            spongeCompound.setBoolean(NbtDataUtil.VANISH, true);
        }
        if (!spongeCompound.isEmpty()) {
            forgeCompound.setTag(NbtDataUtil.SPONGE_DATA, spongeCompound);
            compound.setTag(NbtDataUtil.FORGE_DATA, forgeCompound);
        }

        CustomDataNbtUtil.writeCustomData(spongeCompound, ((DataHolder) this));
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
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.USER_UUID, this.profile.getId())
                .set(DataQueries.USER_NAME, this.profile.getName())
                .set(DataQueries.USER_SPAWNS, this.spawnLocations);
    }

    @Override
    public boolean canEquip(EquipmentType type) {
        return getForInventory(p -> p.canEquip(type), u -> true); // TODO Inventory API
    }

    @Override
    public boolean canEquip(EquipmentType type, @Nullable ItemStack equipment) {
        return getForInventory(p -> p.canEquip(type, equipment), u -> true); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getEquipped(EquipmentType type) {
        return this.getForInventory(p -> p.getEquipped(type), u -> u.getEquippedItem(type));
    }

    @Override
    public boolean equip(EquipmentType type, @Nullable ItemStack equipment) {
        if (this.canEquip(type, equipment)) {
            this.setForInventory(p -> p.equip(type, equipment), u -> u.setEquippedItem(type, equipment));
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public CarriedInventory<?> getInventory() {
        return this.getForInventory(Player::getInventory, u -> ((CarriedInventory) u.inventory));
    }

    public Inventory getEnderChestInventory() {
        this.loadEnderInventory();
        return ((Inventory) this.enderChest);
    }

    @Override
    public Optional<ItemStack> getItemInHand(HandType handType) {
        if (handType == HandTypes.MAIN_HAND) {
            return this.getForInventory(p -> p.getItemInHand(handType), u -> u.getEquipped(EquipmentTypes.MAIN_HAND));
        } else if (handType == HandTypes.OFF_HAND) {
            return this.getForInventory(p -> p.getItemInHand(handType), u -> u.getEquipped(EquipmentTypes.OFF_HAND));
        }
        throw new IllegalArgumentException("Invalid hand " + handType);
    }

    @Override
    public void setItemInHand(HandType handType, @Nullable ItemStack itemInHand) {
        if (handType == HandTypes.MAIN_HAND) {
            this.setForInventory(p -> p.setItemInHand(handType, itemInHand), u -> u.setEquippedItem(EquipmentTypes.MAIN_HAND, itemInHand));
        } else if (handType == HandTypes.OFF_HAND) {
            this.setForInventory(p -> p.setItemInHand(handType, itemInHand), u -> u.setEquippedItem(EquipmentTypes.OFF_HAND, itemInHand));
        } else {
            throw new IllegalArgumentException("Invalid hand " + handType);
        }
    }

    @Override
    public Map<UUID, RespawnLocation> getBedlocations() {
        Optional<Player> player = this.self.getPlayer();
        if (player.isPresent()) {
            return ((ISpongeUser) player.get()).getBedlocations();
        }
        return this.spawnLocations;
    }

    @Override
    public boolean setBedLocations(Map<UUID, RespawnLocation> value) {
        Optional<Player> player = this.self.getPlayer();
        if (player.isPresent()) {
            return ((ISpongeUser) player.get()).setBedLocations(value);
        }
        this.spawnLocations.clear();
        this.spawnLocations.putAll(value);
        this.markDirty();
        return true;
    }

    @Override
    public ImmutableMap<UUID, RespawnLocation> removeAllBeds() {
        Optional<Player> player = this.self.getPlayer();
        if (player.isPresent()) {
            return ((ISpongeUser) player.get()).removeAllBeds();
        }
        ImmutableMap<UUID, RespawnLocation> locations = ImmutableMap.copyOf(this.spawnLocations);
        this.spawnLocations.clear();
        this.markDirty();
        return locations;
    }

    protected void markDirty() {
        dirtyUsers.add(this);
    }

    public void save() {
        SaveHandler saveHandler = (SaveHandler) WorldManager.getWorldByDimensionId(0).get().getSaveHandler();
        File dataFile = new File(saveHandler.playersDirectory, getUniqueId() + ".dat");
        NBTTagCompound tag;
        if (dataFile.isFile()) {
            try {
                tag = CompressedStreamTools.readCompressed(new FileInputStream(dataFile));
            } catch (IOException ignored) {
                // Nevermind
                tag = new NBTTagCompound();
            }
        } else {
            tag = new NBTTagCompound();
        }
        writeToNbt(tag);
        try {
            CompressedStreamTools.writeCompressed(tag, new FileOutputStream(dataFile));
            dirtyUsers.remove(this);
        } catch (IOException e) {
            SpongeImpl.getLogger().warn("Failed to save user file [{}]!", dataFile, e);
        }
    }

    // Helpers for UserInventory

    private <T> T getForInventory(Function<Player, T> playerFunction, Function<SpongeUser, T> userFunction) {
        if (this.self.getPlayer().isPresent()) {
            return playerFunction.apply(this.self.getPlayer().get());
        }
        return userFunction.apply(this.loadInventory()); // Load Inventory if not yet loaded
    }

    private void setForInventory(Consumer<Player> playerFunction, Consumer<SpongeUser> userFunction) {
        if (this.self.getPlayer().isPresent()) {
            playerFunction.accept(this.self.getPlayer().get());
            return;
        }
        userFunction.accept(this.loadInventory()); // Load Inventory if not yet loaded
    }


    // Helpers for Equipment:

    private Optional<ItemStack> getEquippedItem(EquipmentType type) {
        if (type instanceof SpongeEquipmentType) {
            EntityEquipmentSlot[] slots = ((SpongeEquipmentType) type).getSlots();
            if (slots.length == 1) {
                net.minecraft.item.ItemStack nmsItem = this.getItemStackFromSlot(slots[0]);
                if (!nmsItem.isEmpty()) {
                    return Optional.of(ItemStackUtil.fromNative(nmsItem));
                }
            }
        }
        return Optional.empty();
    }

    private void setEquippedItem(EquipmentType type, @Nullable ItemStack item) {
        if (type instanceof SpongeEquipmentType) {
            EntityEquipmentSlot[] slots = ((SpongeEquipmentType) type).getSlots();
            for (EntityEquipmentSlot slot : slots) {
                this.setItemStackToSlot(slot, ItemStackUtil.toNative(item));
                // TODO check canequip
                return;
            }
        }
    }

    private net.minecraft.item.ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            return this.inventory.getCurrentItem();
        } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
            return this.inventory.offHandInventory.get(0);
        } else {
            return slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? this.inventory.armorInventory.get(slotIn.getIndex()) :
                    net.minecraft.item.ItemStack.EMPTY;
        }
    }

    private void setItemStackToSlot(EntityEquipmentSlot slotIn, net.minecraft.item.ItemStack stack) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            this.inventory.mainInventory.set(this.inventory.currentItem, stack);
        } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
            this.inventory.offHandInventory.set(0, stack);
        } else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            this.inventory.armorInventory.set(slotIn.getIndex(), stack);
        }
    }

}
