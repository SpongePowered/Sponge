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
package org.spongepowered.common.mixin.core.entity.player;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.MoreObjects;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.world.WorldLoader;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = SpongeUser.class, remap = false)
public abstract class MixinSpongeUser implements User, IMixinSubject {

    @Shadow @Final private com.mojang.authlib.GameProfile profile;

    @Shadow private double posX;
    @Shadow private double posY;
    @Shadow private double posZ;
    @Shadow private DimensionType dimensionType;
    @Shadow private float rotationPitch;
    @Shadow private float rotationYaw;

    @Shadow protected abstract void markDirty();

    @Shadow protected abstract SpongeUser loadEnderInventory();

    @Shadow private InventoryEnderChest enderChest;

    @Override
    public GameProfile getProfile() {
        return (GameProfile) this.profile;
    }

    @Override
    public boolean isOnline() {
        return this.getPlayer().isPresent();
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.ofNullable((Player) SpongeImpl.getServer().getPlayerList().getPlayerByUUID(this.profile.getId()));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<CommandSource> getCommandSource() {
        return (Optional) getPlayer();
    }

    @Override
    public boolean validateRawData(DataView container) {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public Vector3d getPosition() {
        return new Vector3d(this.posX, this.posY, this.posZ);
    }

    @Override
    public Optional<UUID> getWorldUniqueId() {
        final WorldLoader loader = ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader();

        WorldServer world = loader.getWorld(this.dimensionType).orElse(null);
        if (world == null) {
            world = loader.getWorld(this.dimensionType.getSuffix()).orElse(null);
        }

        UUID uniqueId = null;
        if (world != null) {
            uniqueId = ((World) world).getUniqueId();
        }

        return Optional.ofNullable(uniqueId);
    }

    @Override
    public boolean setLocation(Vector3d position, UUID world) {
        final WorldLoader loader = ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader();
        final WorldInfo info = loader.getWorldInfo(world).orElseThrow(() -> new IllegalArgumentException(
            "Invalid World: No world found for UUID"));
        if (((IMixinWorldInfo) info).getDimensionType() == null) {
            throw new IllegalArgumentException("Invalid World: Not initialized and has no Dimension registration.");
        }
        this.dimensionType = ((IMixinWorldInfo) info).getDimensionType();
        this.posX = position.getX();
        this.posY = position.getY();
        this.posZ = position.getZ();
        this.markDirty();
        return true;
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(this.rotationPitch, this.rotationYaw, 0);
    }

    @Override
    public void setRotation(Vector3d rotation) {
        checkNotNull(rotation, "Rotation was null!");
        this.markDirty();
        this.rotationPitch = ((float) rotation.getX()) % 360.0F;
        this.rotationYaw = ((float) rotation.getY()) % 360.0F;
    }

    @Override
    public Inventory getEnderChestInventory() {
        this.loadEnderInventory();
        return ((Inventory) this.enderChest);
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.FALSE;
    }

    @Override
    public String getIdentifier() {
        return this.profile.getId().toString();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("isOnline", this.isOnline())
                .add("profile", this.getProfile())
                .toString();
    }
}
