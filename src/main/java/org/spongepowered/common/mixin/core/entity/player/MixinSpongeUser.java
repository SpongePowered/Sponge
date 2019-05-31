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
import org.spongepowered.common.entity.player.IUserOrEntity;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.world.WorldManager;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = SpongeUser.class, remap = false)
public abstract class MixinSpongeUser implements User, IMixinSubject, IUserOrEntity {

    @Shadow @Final private com.mojang.authlib.GameProfile profile;

    @Shadow private double posX;
    @Shadow private double posY;
    @Shadow private double posZ;
    @Shadow private int dimension;
    @Shadow private float rotationPitch;
    @Shadow private float rotationYaw;
    @Shadow private boolean invulnerable;
    @Shadow private boolean isVanished;

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
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            return playerOpt.get().getPosition();
        }
        return new Vector3d(this.posX, this.posY, this.posZ);
    }

    @Override
    public Optional<UUID> getWorldUniqueId() {
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            return playerOpt.get().getWorldUniqueId();
        }
        Optional<String> folder = WorldManager.getWorldFolderByDimensionId(this.dimension);
        return folder.map(WorldManager::getWorldProperties).flatMap(e -> e.map(WorldProperties::getUniqueId));
    }

    @Override
    public boolean setLocation(Vector3d position, UUID world) {
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            return playerOpt.get().setLocation(position, world);
        }
        WorldProperties prop = WorldManager.getWorldProperties(world).orElseThrow(() -> new IllegalArgumentException("Invalid World: No world found for UUID"));
        Integer dimensionId = ((IMixinWorldInfo) prop).getDimensionId();
        if (dimensionId == null) {
            throw new IllegalArgumentException("Invalid World: missing dimensionID)");
        }
        this.dimension = dimensionId;
        this.posX = position.getX();
        this.posY = position.getY();
        this.posZ = position.getZ();
        this.markDirty();
        return true;
    }

    @Override
    public Vector3d getRotation() {
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            return playerOpt.get().getRotation();
        }
        return new Vector3d(this.rotationPitch, this.rotationYaw, 0);
    }

    @Override
    public void setRotation(Vector3d rotation) {
        checkNotNull(rotation, "Rotation was null!");
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            playerOpt.get().setRotation(rotation);
            return;
        }
        this.markDirty();
        this.rotationPitch = ((float) rotation.getX()) % 360.0F;
        this.rotationYaw = ((float) rotation.getY()) % 360.0F;
    }

    @Override
    public Inventory getEnderChestInventory() {
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            return playerOpt.get().getEnderChestInventory();
        }
        this.loadEnderInventory();
        return ((Inventory) this.enderChest);
    }

    @Override
    public void setInvulnerable(boolean value) {
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            ((IMixinEntity) playerOpt.get()).setInvulnerable(value);
            return;
        }
        this.invulnerable = value;
        this.markDirty();
    }

    @Override
    public boolean getIsInvulnerable() {
        return this.invulnerable;
    }

    @Override
    public void setVanished(boolean vanished) {
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            ((IMixinEntity) playerOpt.get()).setVanished(vanished);
            return;
        }
        this.isVanished = vanished;
        this.markDirty();
    }

    @Override
    public boolean isVanished() {
        Optional<Player> playerOpt = getPlayer();
        if (playerOpt.isPresent()) {
            return ((IMixinEntity) playerOpt.get()).isVanished();
        }
        return this.isVanished;
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
