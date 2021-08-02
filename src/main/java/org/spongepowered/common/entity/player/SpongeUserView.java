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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.CollectionValue;
import org.spongepowered.api.data.value.MapValue;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ArmorEquipable;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.service.server.permission.BridgeSubject;
import org.spongepowered.common.service.server.permission.SubjectHelper;
import org.spongepowered.math.vector.Vector3d;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Also see SubjectMixin
 */
@DefaultQualifier(NonNull.class)
public abstract class SpongeUserView implements User, BridgeSubject {

    protected final UUID uuid;

    public static User create(final UUID uuid) {
        return new SpongeUserView.Standard(uuid);
    }

    public static User createLoginEventUser(final ServerPlayer serverPlayer) {
        return new SpongeUserView.Login(serverPlayer);
    }

    protected SpongeUserView(final UUID uuid) {
        this.uuid = uuid;
        SubjectHelper.applySubject((SubjectBridge) (Object) this, PermissionService.SUBJECTS_USER);
    }

    @Override
    public <E> DataTransactionResult offer(final Key<? extends Value<E>> key, final E value) {
        return this.dataHolderBackingObject().offer(key, value);
    }

    @Override
    public DataTransactionResult offer(final Value<?> value) {
        return this.dataHolderBackingObject().offer(value);
    }

    @Override
    public <E> DataTransactionResult offerSingle(final Key<? extends CollectionValue<E, ?>> key, final E element) {
        return this.dataHolderBackingObject().offerSingle(key, element);
    }

    @Override
    public <K, V> DataTransactionResult offerSingle(final Key<? extends MapValue<K, V>> key, final K valueKey, final V value) {
        return this.dataHolderBackingObject().offerSingle(key, valueKey, value);
    }

    @Override
    public <K, V> DataTransactionResult offerAll(final Key<? extends MapValue<K, V>> key, final Map<? extends K, ? extends V> map) {
        return this.dataHolderBackingObject().offerAll(key, map);
    }

    @Override
    public DataTransactionResult offerAll(final MapValue<?, ?> value) {
        return this.dataHolderBackingObject().offerAll(value);
    }

    @Override
    public DataTransactionResult offerAll(final CollectionValue<?, ?> value) {
        return this.dataHolderBackingObject().offerAll(value);
    }

    @Override
    public <E> DataTransactionResult offerAll(final Key<? extends CollectionValue<E, ?>> key, final Collection<? extends E> elements) {
        return this.dataHolderBackingObject().offerAll(key, elements);
    }

    @Override
    public <E> DataTransactionResult removeSingle(final Key<? extends CollectionValue<E, ?>> key, final E element) {
        return this.dataHolderBackingObject().removeSingle(key, element);
    }

    @Override
    public <K> DataTransactionResult removeKey(final Key<? extends MapValue<K, ?>> key, final K mapKey) {
        return this.dataHolderBackingObject().removeKey(key, mapKey);
    }

    @Override
    public DataTransactionResult removeAll(final CollectionValue<?, ?> value) {
        return this.dataHolderBackingObject().removeAll(value);
    }

    @Override
    public <E> DataTransactionResult removeAll(final Key<? extends CollectionValue<E, ?>> key, final Collection<? extends E> elements) {
        return this.dataHolderBackingObject().removeAll(key, elements);
    }

    @Override
    public DataTransactionResult removeAll(final MapValue<?, ?> value) {
        return this.dataHolderBackingObject().removeAll(value);
    }

    @Override
    public <K, V> DataTransactionResult removeAll(final Key<? extends MapValue<K, V>> key, final Map<? extends K, ? extends V> map) {
        return this.dataHolderBackingObject().removeAll(key, map);
    }

    @Override
    public <E> DataTransactionResult tryOffer(final Key<? extends Value<E>> key, final E value) {
        return this.dataHolderBackingObject().tryOffer(key, value);
    }

    @Override
    public DataTransactionResult remove(final Key<?> key) {
        return this.dataHolderBackingObject().remove(key);
    }

    @Override
    public DataTransactionResult undo(final DataTransactionResult result) {
        return this.dataHolderBackingObject().undo(result);
    }

    @Override
    public DataTransactionResult copyFrom(final ValueContainer that, final MergeFunction function) {
        return this.dataHolderBackingObject().copyFrom(that, function);
    }

    @Override
    public <E> Optional<E> get(final Key<? extends Value<E>> key) {
        return this.dataHolderBackingObject().get(key);
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        return this.dataHolderBackingObject().getValue(key);
    }

    @Override
    public boolean supports(final Key<?> key) {
        return this.dataHolderBackingObject().supports(key);
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.dataHolderBackingObject().getKeys();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        return this.dataHolderBackingObject().getValues();
    }

    @Override
    public GameProfile profile() {
        return this.backingObject(Player::profile, SpongeUserData::profile);
    }

    @Override
    public String name() {
        return this.backingObject(Nameable::name, SpongeUserData::name);
    }

    @Override
    public boolean isOnline() {
        return this.player().isPresent();
    }

    @Override
    public Optional<ServerPlayer> player() {
        return Sponge.server().player(this.uuid);
    }

    @Override
    public Vector3d position() {
        return this.backingObject(Entity::position, SpongeUserData::position);
    }

    @Override
    public ResourceKey worldKey() {
        return this.backingObject(player -> player.world().key(), SpongeUserData::worldKey);
    }

    @Override
    public boolean setLocation(final ResourceKey world, final Vector3d position) {
        return this.backingObject(player -> player.setLocation(ServerLocation.of(world, position)), user -> user.setLocation(world, position));
    }

    @Override
    public void setRotation(final Vector3d rotation) {
        this.backingObjectConsumer(player -> player.setRotation(rotation), user -> user.setRotation(rotation));
    }

    @Override
    public Vector3d rotation() {
        return this.backingObject(Player::rotation, SpongeUserData::rotation);
    }

    @Override
    public CarriedInventory<? extends Carrier> inventory() {
        return this.backingObject(Player::inventory, SpongeUserData::inventory);
    }

    @Override
    public Inventory enderChestInventory() {
        return this.backingObject(Player::enderChestInventory, SpongeUserData::enderChestInventory);
    }

    @Override
    public ItemStack head() {
        return this.backingObject(ArmorEquipable::head, SpongeUserData::head);
    }

    @Override
    public void setHead(final ItemStack head) {
        this.backingObjectConsumer(player -> player.setHead(head), user -> user.setHead(head));
    }

    @Override
    public ItemStack chest() {
        return this.backingObject(ArmorEquipable::chest, SpongeUserData::chest);
    }

    @Override
    public void setChest(final ItemStack chest) {
        this.backingObjectConsumer(player -> player.setChest(chest), user -> user.setChest(chest));
    }

    @Override
    public ItemStack legs() {
        return this.backingObject(ArmorEquipable::legs, SpongeUserData::legs);
    }

    @Override
    public void setLegs(final ItemStack legs) {
        this.backingObjectConsumer(player -> player.setLegs(legs), user -> user.setLegs(legs));
    }

    @Override
    public ItemStack feet() {
        return this.backingObject(ArmorEquipable::feet, SpongeUserData::feet);
    }

    @Override
    public void setFeet(final ItemStack feet) {
        this.backingObjectConsumer(player -> player.setFeet(feet), user -> user.setFeet(feet));
    }

    @Override
    public ItemStack itemInHand(final HandType handType) {
        return this.backingObject(player -> player.itemInHand(handType), user -> user.itemInHand(handType));
    }

    @Override
    public void setItemInHand(final HandType handType, final ItemStack itemInHand) {
        this.backingObjectConsumer(player -> player.setItemInHand(handType, itemInHand), user -> user.setItemInHand(handType, itemInHand));
    }

    @Override
    public EquipmentInventory equipment() {
        return this.backingObject(Equipable::equipment, SpongeUserData::equipment);
    }

    @Override
    public boolean canEquip(final EquipmentType type) {
        return this.backingObject(player -> player.canEquip(type), user -> user.canEquip(type));
    }

    @Override
    public boolean canEquip(final EquipmentType type, final ItemStack equipment) {
        return this.backingObject(player -> player.canEquip(type, equipment), user -> user.canEquip(type,  equipment));
    }

    @Override
    public Optional<ItemStack> equipped(final EquipmentType type) {
        return this.backingObject(player -> player.equipped(type), user -> user.equipped(type));
    }

    @Override
    public boolean equip(final EquipmentType type, final ItemStack equipment) {
        return this.backingObject(player -> player.equip(type, equipment), user -> user.equip(type, equipment));
    }

    @Override
    public String identifier() {
        return this.uuid.toString();
    }

    @Override
    public UUID uniqueId() {
        return this.uuid;
    }

    private void backingObjectConsumer(final FunctionConsumer<? super ServerPlayer> playerFunc, final FunctionConsumer<? super SpongeUserData> userFunc) {
        this.backingObject(playerFunc, userFunc);
    }

    protected abstract DataHolder.Mutable dataHolderBackingObject();

    private <T> T backingObject(final Function<? super ServerPlayer, T> playerFunc, final Function<? super SpongeUserData, T> userFunc) {
        final DataHolder.Mutable mutable = this.dataHolderBackingObject();
        if (mutable instanceof ServerPlayer) {
            return playerFunc.apply((ServerPlayer) mutable);
        }
        return userFunc.apply((SpongeUserData) mutable);
    }

    @FunctionalInterface
    interface FunctionConsumer<T> extends Function<T, Void>, Consumer<T> {

        @Override
        default Void apply(final T t) {
            this.accept(t);
            return null;
        }

    }

    final static class Standard extends SpongeUserView {

        Standard(final UUID uuid) {
            super(uuid);
        }

        @SuppressWarnings("ConstantConditions")
        protected DataHolder.Mutable dataHolderBackingObject() {
            final @Nullable ServerPlayer serverPlayer = (ServerPlayer) SpongeCommon.server().getPlayerList().getPlayer(this.uuid);
            if (serverPlayer != null) {
                return serverPlayer;
            }
            final SpongeUserData user = ((SpongeServer) SpongeCommon.server()).userManager().userFromCache(this.uuid);
            if (user != null) {
                return user;
            }
            throw new IllegalStateException("Player is not online and user is not loaded - it must be loaded from the UserManager.");
        }
    }

    final static class Login extends SpongeUserView {

        private final WeakReference<ServerPlayer> player;

        Login(final ServerPlayer serverPlayer) {
            super(serverPlayer.uniqueId());
            this.player = new WeakReference<>(serverPlayer);
        }

        @Override
        protected DataHolder.Mutable dataHolderBackingObject() {
            final @Nullable ServerPlayer player = this.player.get();
            if (player == null) {
                throw new IllegalStateException("The Player is no longer available, do not store this object!");
            }
            return player;
        }

    }

}
