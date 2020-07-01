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

import static com.google.common.base.Preconditions.checkNotNull;

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
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
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
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeInternalListeners;
import org.spongepowered.common.accessor.world.storage.SaveHandlerAccessor;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.InvulnerableTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.player.BedLocationHolderBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.data.type.SpongeEquipmentType;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.service.permission.SpongeBridgeSubject;
import org.spongepowered.common.service.permission.SubjectSettingCallback;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

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

import javax.annotation.Nullable;

/**
 * Implements things that are not implemented by mixins into this class. <p>This
 * class is concrete in order to create instances of User.</p>
 *
 * <p>List of mixins mixing into this class: <ul>
 * <li>SpongeUserMixin</li><li>DataHolderMixin_API</li><li>SubjectMixin</li> </ul>
 * <p>
 * TODO Future note about data: The following data manipulators are always
 * applicable to User: BanData, WhitelistData, JoinData
 */
public class SpongeUser implements User, DataSerializable, BedLocationHolderBridge,
        SpongeMutableDataHolder,
        SpongeBridgeSubject, SubjectBridge,
        DataCompoundHolder,
        InvulnerableTrackedBridge, VanishableBridge {

    public static final Set<SpongeUser> dirtyUsers = ConcurrentHashMap.newKeySet();

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
    private boolean isInvisible;
    private boolean isVanishCollide;
    private boolean isVanishTarget;

    @Nullable private SpongeUserInventory inventory; // lazy load when accessing inventory
    @Nullable private EnderChestInventory enderChest; // lazy load when accessing inventory
    @Nullable private CompoundNBT nbt;
    private boolean isConstructing;

    public SpongeUser(final GameProfile profile) {
        this.profile = profile;
        if (SpongeCommon.isInitialized()) {
            SpongeInternalListeners.getInstance().registerExpirableServiceCallback(PermissionService.class, new SubjectSettingCallback(this));
        }
    }

    private void reset() {
        this.spawnLocations.clear();
    }

    public boolean isInitialized() {
        return this.nbt != null;
    }

    public DataHolder getDataHolder(boolean markDirty) {
        if (this.isOnline()) {
            return this.getPlayer().get();
        }
        if (!this.isInitialized()) {
            this.initialize();
        }
        if (markDirty) {
            this.markDirty();
        }
        return (DataHolder) this;
    }

    public void invalidate() {
        this.nbt = null;
        this.inventory = null;
        this.enderChest = null;

        ((CustomDataHolderBridge) this).bridge$getFailedData().clear();
    }

    public void initialize() {
        this.nbt = new CompoundNBT();
        final ServerWorld world = SpongeCommon.getWorldManager().getDefaultWorld();
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
            try (FileInputStream in = new FileInputStream(file)) {
                this.readFromNbt(CompressedStreamTools.readCompressed(in));
            }
        } catch (IOException e) {
            SpongeCommon.getLogger().warn("Corrupt user file {}", file, e);
        }
    }

    public void readFromNbt(final CompoundNBT compound) {
        this.reset();
        this.nbt = compound;

        // net.minecraft.entity.Entity#readFromNBT

        final ListNBT position = compound.getList(Constants.Entity.ENTITY_POSITION, Constants.NBT.TAG_DOUBLE);
        //NBTTagList motion = compound.getTagList("Motion", NbtDataUtil.TAG_DOUBLE);
        final ListNBT rotation = compound.getList(Constants.Entity.ENTITY_ROTATION, Constants.NBT.TAG_FLOAT);
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
        this.posX = position.getDouble(0);
        this.posY = position.getDouble(1);
        this.posZ = position.getDouble(2);
        //this.lastTickPosX = this.posX;
        //this.lastTickPosY = this.posY;
        //this.lastTickPosZ = this.posZ;
        //this.prevPosX = this.posX;
        //this.prevPosY = this.posY;
        //this.prevPosZ = this.posZ;
        this.rotationYaw = rotation.getFloat(0);
        this.rotationPitch = rotation.getFloat(1);
        //this.prevRotationYaw = this.rotationYaw;
        //this.prevRotationPitch = this.rotationPitch;
        //this.fallDistance = compound.getFloat("FallDistance");
        //this.fire = compound.getShort("Fire");
        //this.setAir(compound.getShort("Air"));
        //this.onGround = compound.getBoolean("OnGround");

        if (compound.contains(Constants.Entity.ENTITY_DIMENSION)) {
            this.dimension = compound.getInt(Constants.Entity.ENTITY_DIMENSION);
        } else {
            this.dimension = 0;
        }

        this.invulnerable = compound.getBoolean(Constants.Entity.Player.INVULNERABLE);
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


        // org.spongepowered.common.mixin.core.entity.EntityMixin#readSpongeNBT


        final CompoundNBT spongeCompound = compound.getCompound(Constants.Forge.FORGE_DATA).getCompound(
            Constants.Sponge.SPONGE_DATA);
        this.isConstructing = true;
//        DataUtil.readCustomData(spongeCompound, (DataHolder) this);
        this.isConstructing = false;
        //if (this instanceof GrieferBridge && ((GrieferBridge) this).bridge$isGriefer() && compound.hasKey(NbtDataUtil.CAN_GRIEF)) {
        //    ((GrieferBridge) this).bridge$SetCanGrief(compound.getBoolean(NbtDataUtil.CAN_GRIEF));
        //}


        // org.spongepowered.common.mixin.core.entity.EntityLivingBaseMixin#readSpongeNBT


        //if (compound.hasKey("maxAir")) {
        //    this.maxAir = compound.getInteger("maxAir");
        //}


        // org.spongepowered.common.mixin.core.entity.player.EntityPlayerMPMixin#readSpongeNBT


        //if (compound.hasKey(NbtDataUtil.HEALTH_SCALE, NbtDataUtil.TAG_DOUBLE)) {
        //    this.healthScaling = true;
        //    this.healthScale = compound.getDouble(NbtDataUtil.HEALTH_SCALE);
        //}

        // extra data

        if (!spongeCompound.isEmpty()) {
            this.isVanished = spongeCompound.getBoolean(Constants.Sponge.Entity.IS_VANISHED);
            this.isInvisible = spongeCompound.getBoolean(Constants.Sponge.Entity.IS_INVISIBLE);
            if (this.isVanished) {
                this.isVanishTarget = spongeCompound.getBoolean(Constants.Sponge.Entity.VANISH_UNTARGETABLE);
                this.isVanishCollide = spongeCompound.getBoolean(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE);
            }

            final ListNBT spawnList = spongeCompound.getList(Constants.Sponge.User.USER_SPAWN_LIST,
                Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < spawnList.size(); i++) {
                final CompoundNBT spawnCompound = spawnList.getCompound(i);

                final UUID uuid = spawnCompound.getUniqueId(Constants.UUID);

                if (uuid.getLeastSignificantBits() != 0 && uuid.getMostSignificantBits() != 0) {
                    final double xPos = spawnCompound.getDouble(Constants.Sponge.User.USER_SPAWN_X);
                    final double yPos = spawnCompound.getDouble(Constants.Sponge.User.USER_SPAWN_Y);
                    final double zPos = spawnCompound.getDouble(Constants.Sponge.User.USER_SPAWN_Z);
                    final boolean forced = spawnCompound.getBoolean(Constants.Sponge.User.USER_SPAWN_FORCED);
                    this.spawnLocations.put(uuid, new RespawnLocation.Builder()
                        .forceSpawn(forced)
                        .position(new Vector3d(xPos, yPos, zPos))
                        .world(uuid)
                        .build());
                }
            }
        }
    }

    private UserInventory loadInventory() {
        if (this.inventory == null) {
            if (!this.isInitialized()) {
                this.initialize();
            }
            this.inventory = new SpongeUserInventory(this);
            final ListNBT nbttaglist = this.nbt.getList(Constants.Entity.Player.INVENTORY, 10);
            this.inventory.readFromNBT(nbttaglist);
            this.inventory.currentItem = this.nbt.getInt(Constants.Entity.Player.SELECTED_ITEM_SLOT);
        }
        return (UserInventory) this.inventory;
    }

    private SpongeUser loadEnderInventory() {
        if (this.enderChest == null) {
            if (!this.isInitialized()) {
                this.initialize();
            }
            this.enderChest = new SpongeUserInventoryEnderchest(this);
            if (this.nbt.contains(Constants.Entity.Player.ENDERCHEST_INVENTORY, 9)) {
                final ListNBT nbttaglist1 = this.nbt.getList(Constants.Entity.Player.ENDERCHEST_INVENTORY, 10);
                this.enderChest.read(nbttaglist1);
            }
        }
        return this;
    }

    public void writeToNbt(final CompoundNBT compound) {

        this.loadInventory();
        this.loadEnderInventory();
        compound.put(Constants.Entity.Player.INVENTORY, this.inventory.writeToNBT(new ListNBT()));
        compound.put(Constants.Entity.Player.ENDERCHEST_INVENTORY, this.enderChest.write());
        compound.putInt(Constants.Entity.Player.SELECTED_ITEM_SLOT, this.inventory.currentItem);

        compound.put(Constants.Entity.ENTITY_POSITION, Constants.NBT.newDoubleNBTList(this.posX, this.posY, this.posZ));
        compound.putInt(Constants.Entity.ENTITY_DIMENSION, this.dimension);
        compound.put(Constants.Entity.ENTITY_ROTATION,
            Constants.NBT.newFloatNBTList(this.rotationYaw, this.rotationPitch));

        compound.putBoolean(Constants.Entity.Player.INVULNERABLE, this.invulnerable);

        final CompoundNBT forgeCompound = compound.getCompound(Constants.Forge.FORGE_DATA);
        final CompoundNBT spongeCompound = forgeCompound.getCompound(Constants.Sponge.SPONGE_DATA);
        spongeCompound.remove(Constants.Sponge.User.USER_SPAWN_LIST);
        spongeCompound.remove(Constants.Sponge.Entity.IS_VANISHED);
        spongeCompound.remove(Constants.Sponge.Entity.IS_INVISIBLE);
        spongeCompound.remove(Constants.Sponge.Entity.VANISH_UNTARGETABLE);
        spongeCompound.remove(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE);

        final ListNBT spawnList = new ListNBT();
        for (final Map.Entry<UUID, RespawnLocation> entry : this.spawnLocations.entrySet()) {
            final RespawnLocation respawn = entry.getValue();

            final CompoundNBT spawnCompound = new CompoundNBT();
            spawnCompound.putUniqueId(Constants.UUID, entry.getKey());
            spawnCompound.putDouble(Constants.Sponge.User.USER_SPAWN_X, respawn.getPosition().getX());
            spawnCompound.putDouble(Constants.Sponge.User.USER_SPAWN_Y, respawn.getPosition().getY());
            spawnCompound.putDouble(Constants.Sponge.User.USER_SPAWN_Z, respawn.getPosition().getZ());
            spawnCompound.putBoolean(Constants.Sponge.User.USER_SPAWN_FORCED, false); // No way to know
            spawnList.add(spawnCompound);
        }

        if (!spawnList.isEmpty()) {
            spongeCompound.put(Constants.Sponge.User.USER_SPAWN_LIST, spawnList);
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
        if (type instanceof SpongeEquipmentType) {
            final EquipmentSlotType[] slots = ((SpongeEquipmentType) type).getSlots();
            if (slots.length == 1) {
                final net.minecraft.item.ItemStack nmsItem = this.getItemStackFromSlot(slots[0]);
                if (!nmsItem.isEmpty()) {
                    return Optional.of(ItemStackUtil.fromNative(nmsItem));
                }
            }
        }
        return Optional.empty();
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

    @SuppressWarnings("rawtypes")
    @Override
    public UserInventory getInventory() {
        return this.loadInventory();
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
    public ItemStack getHelmet() {
        return this.getEquipped(EquipmentTypes.HEADWEAR).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        this.equip(EquipmentTypes.HEADWEAR, helmet);
    }

    @Override
    public ItemStack getChestplate() {
        return this.getEquipped(EquipmentTypes.CHESTPLATE).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        this.equip(EquipmentTypes.CHESTPLATE, chestplate);
    }

    @Override
    public ItemStack getLeggings() {
        return this.getEquipped(EquipmentTypes.LEGGINGS).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        this.equip(EquipmentTypes.LEGGINGS, leggings);
    }

    @Override
    public ItemStack getBoots() {
        return this.getEquipped(EquipmentTypes.BOOTS).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setBoots(ItemStack boots) {
        this.equip(EquipmentTypes.BOOTS, boots);
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
    public Map<UUID, RespawnLocation> bridge$getBedlocations() {
        final Optional<ServerPlayer> player = this.getPlayer();
        return player
            .map(value -> ((BedLocationHolderBridge) value).bridge$getBedlocations())
            .orElse(this.spawnLocations);
    }

    @Override
    public boolean bridge$setBedLocations(final Map<UUID, RespawnLocation> value) {
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
    public ImmutableMap<UUID, RespawnLocation> bridge$removeAllBeds() {
        final Optional<ServerPlayer> player = this.getPlayer();
        if (player.isPresent()) {
            return ((BedLocationHolderBridge) player.get()).bridge$removeAllBeds();
        }
        final ImmutableMap<UUID, RespawnLocation> locations = ImmutableMap.copyOf(this.spawnLocations);
        this.spawnLocations.clear();
        this.markDirty();
        return locations;
    }

    public void markDirty() {
        if (this.isConstructing) {
            return;
        }
        dirtyUsers.add(this);
    }

    public void save() {
        Preconditions.checkState(this.isInitialized(), "User {} is not initialized", this.profile.getId());
        final SaveHandlerAccessor saveHandler = (SaveHandlerAccessor) SpongeCommon.getWorldManager().getDefaultWorld();
        final File dataFile = new File(saveHandler.accessor$getPlayersDirectory(), this.getUniqueId() + ".dat");
        CompoundNBT compound;
        try {
            compound = CompressedStreamTools.readCompressed(new FileInputStream(dataFile));
        } catch (IOException ignored) {
            // Nevermind
            compound = new CompoundNBT();
        }
        this.writeToNbt(compound);
        try (final FileOutputStream out = new FileOutputStream(dataFile)) {
            CompressedStreamTools.writeCompressed(compound, out);
            dirtyUsers.remove(this);
            this.invalidate();
        } catch (IOException e) {
            SpongeCommon.getLogger().warn("Failed to save user file [{}]!", dataFile, e);
        }
    }

    // Helpers for Equipment:

    private void setEquippedItem(final Supplier<? extends EquipmentType> type, @Nullable final ItemStack item) {
        this.setEquippedItem(type.get(), item);
    }

    private void setEquippedItem(final EquipmentType type, @Nullable final ItemStack item) {
        if (type instanceof SpongeEquipmentType) {
            final EquipmentSlotType[] slots = ((SpongeEquipmentType) type).getSlots();
            for (final EquipmentSlotType slot : slots) {
                this.setItemStackToSlot(slot, ItemStackUtil.toNative(item));
                // TODO check canequip
                return
                    ;
            }
        }
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
                .orElseGet(() -> new Vector3d(this.posX, this.posY, this.posZ));
    }

    @Override
    public Optional<UUID> getWorldUniqueId() {
        final Optional<ServerPlayer> playerOpt = this.getPlayer();
        if (playerOpt.isPresent()) {
            return playerOpt.map(Player::getWorld).map(World::getUniqueId);
        }
        final DimensionType dimensionType = DimensionType.getById(this.dimension);
        if (dimensionType == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(SpongeCommon.getWorldManager().getDimensionTypeUniqueId(dimensionType));
    }

    @Override
    public boolean setLocation(Vector3d position, UUID worldUniqueId) {
        final Optional<ServerPlayer> playerOpt = this.getPlayer();
        if (playerOpt.isPresent()) {
            Optional<org.spongepowered.api.world.server.ServerWorld> world = SpongeCommon.getWorldManager().getWorld(worldUniqueId);
            return world.filter(serverWorld -> playerOpt.get().setLocation(ServerLocation.of(serverWorld, position))).isPresent();
        }
        final WorldProperties properties =
                SpongeCommon.getWorldManager().getProperties(worldUniqueId).orElseThrow(() -> new IllegalArgumentException(String.format("Unknown "
                        + "World UUID '%s' given when setting location of user!", worldUniqueId)));
        final Integer dimensionId = ((WorldInfoBridge) properties).bridge$getDimensionType().getId();
        this.dimension = dimensionId;
        this.posX = position.getX();
        this.posY = position.getY();
        this.posZ = position.getZ();
        this.markDirty();
        return true;
    }

    @Override
    public Vector3d getRotation() {
        return this.getPlayer()
                .map(Entity::getRotation)
                .orElseGet(() -> new Vector3d(this.rotationPitch, this.rotationYaw, 0));
    }

    @Override
    public void setRotation(final Vector3d rotation) {
        checkNotNull(rotation, "Rotation was null!");
        final Optional<ServerPlayer> playerOpt = this.getPlayer();
        if (playerOpt.isPresent()) {
            playerOpt.get().setRotation(rotation);
            return;
        }
        this.markDirty();
        this.rotationPitch = ((float) rotation.getX()) % 360.0F;
        this.rotationYaw = ((float) rotation.getY()) % 360.0F;
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

    @org.checkerframework.checker.nullness.qual.Nullable
    private SubjectReference impl$subjectReference;

    @Override
    public void bridge$setSubject(final SubjectReference subj) {
        this.impl$subjectReference = subj;
    }

    @Override
    public Optional<SubjectReference> bridge$resolveReferenceOptional() {
        if (this.impl$subjectReference == null) {
            final Optional<PermissionService> serv = SpongeCommon.getGame().getServiceManager().provide(PermissionService.class);
            serv.ifPresent(permissionService -> new SubjectSettingCallback(this).test(permissionService));
        }
        return Optional.ofNullable(this.impl$subjectReference);
    }

    @Override
    public Optional<Subject> bridge$resolveOptional() {
        return bridge$resolveReferenceOptional().map(SubjectReference::resolve).map(CompletableFuture::join);
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
    public boolean data$hasSpongeCompound() {
        if (this.nbt == null) {
            return false;
        }
        return this.nbt.contains(Constants.Forge.FORGE_DATA);
    }

    @Override
    public CompoundNBT data$getSpongeCompound() {
        if (this.nbt == null) {
            return new CompoundNBT();
        }
        CompoundNBT forgeCompound = this.nbt.getCompound(Constants.Forge.FORGE_DATA);
        if (forgeCompound == null) { // TODO this is currently never null
            forgeCompound = new CompoundNBT();
            this.nbt.put(Constants.Forge.FORGE_DATA, forgeCompound);
        }
        return forgeCompound;
    }

    @Override
    public void bridge$setInvulnerable(final boolean value) {
        final Optional<ServerPlayer> playerOpt = ((User) this).getPlayer();
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
        final Optional<ServerPlayer> playerOpt = ((User) this).getPlayer();
        if (playerOpt.isPresent()) {
            ((VanishableBridge) playerOpt.get()).bridge$setVanished(vanished);
            return;
        }
        this.isVanished = vanished;
        this.markDirty();
    }

    @Override
    public boolean bridge$isVanished() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isVanished()).orElseGet(() -> this.isVanished);
    }

    @Override
    public boolean bridge$isInvisible() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isInvisible()).orElseGet(() -> this.isInvisible);
    }

    @Override
    public void bridge$setInvisible(final boolean invisible) {
        final Optional<ServerPlayer> player = ((User) this).getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setInvisible(invisible);
            return;
        }
        this.isInvisible = invisible;
    }

    @Override
    public boolean bridge$isUncollideable() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isUncollideable()).orElseGet(() -> this.isVanishCollide);
    }

    @Override
    public void bridge$setUncollideable(final boolean uncollideable) {
        final Optional<ServerPlayer> player = ((User) this).getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setUncollideable(uncollideable);
            return;
        }
        this.isVanishCollide = uncollideable;
    }

    @Override
    public boolean bridge$isUntargetable() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isUntargetable()).orElseGet(() -> this.isVanishTarget);
    }

    @Override
    public void bridge$setUntargetable(final boolean untargetable) {
        final Optional<ServerPlayer> player = ((User) this).getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setUntargetable(untargetable);
            return;
        }
        this.isVanishTarget = untargetable;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("isOnline", ((User) this).isOnline())
                .add("profile", ((User) this).getProfile())
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
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
}
