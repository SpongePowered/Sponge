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
package org.spongepowered.common.event.damage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;

public class DamageEventHandler {

    public static final DoubleUnaryOperator HARD_HAT_FUNCTION = damage -> -(damage - (damage * 0.75F));
    public static final DoubleUnaryOperator BLOCKING_FUNCTION = damage -> -(damage - ((1.0F + damage) * 0.5F));

    public static DoubleUnaryOperator createResistanceFunction(final int resistanceAmplifier) {
        final int base = (resistanceAmplifier + 1) * 5;
        final int modifier = 25 - base;
        return damage -> -(damage - ((damage * modifier) / 25.0F));
    }

    public static Optional<DamageFunction> createHardHatModifier(final LivingEntity entityLivingBase,
            final DamageSource damageSource) {
        if ((damageSource instanceof FallingBlockDamageSource) && !entityLivingBase.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
            // TODO: direct cause creation: bad bad bad
            final DamageModifier modifier = DamageModifier.builder()
                .cause(
                    Cause.of(EventContext.empty(), ((ItemStack) entityLivingBase.getItemStackFromSlot(EquipmentSlotType.HEAD)).createSnapshot()))
                .type(DamageModifierTypes.HARD_HAT)
                .build();
            return Optional.of(new DamageFunction(modifier, HARD_HAT_FUNCTION));
        }
        return Optional.empty();
    }

    private static double damageToHandle;

    public static Optional<List<DamageFunction>> createArmorModifiers(final LivingEntity entityLivingBase,
            final DamageSource damageSource, double damage) {
        if (!damageSource.isUnblockable()) {
            damage *= 25;
            final net.minecraft.item.ItemStack[] inventory = Iterables.toArray(entityLivingBase.getArmorInventoryList(), net.minecraft.item.ItemStack.class);
            final List<DamageFunction> modifiers = new ArrayList<>();
            final List<DamageObject> damageObjects = new ArrayList<>();

            for (int index = 0; index < inventory.length; index++) {
                final net.minecraft.item.ItemStack itemStack = inventory[index];
                if (itemStack.isEmpty()) {
                    continue;
                }
                final Item item = itemStack.getItem();
                if (item instanceof ArmorItem) {
                    final ArmorItem armor = (ArmorItem) item;
                    final double reduction = armor.damageReduceAmount / 25D;
                    final DamageObject object = new DamageObject();
                    object.slot = index;
                    object.ratio = reduction;
                    damageObjects.add(object);
                }
            }

            boolean first = true;
            double ratio = 0;

            for (final DamageObject prop : damageObjects) {
                final EquipmentType type = resolveEquipment(prop.slot);

                final DamageObject object = new DamageObject();
                object.ratio = ratio;
                if (first) {
                    object.previousDamage = damage;
                    object.augment = true;
                }
                final DoubleUnaryOperator function = incomingDamage -> {
                    incomingDamage *= 25;
                    if (object.augment) {
                        // This is the damage that needs to be archived for the "first" armor modifier
                        // function since the armor modifiers work based on the initial damage and not as
                        // a chain one after another.
                        damageToHandle = incomingDamage;
                    }
                    final double functionDamage = damageToHandle;
                    object.previousDamage = functionDamage;
                    object.ratio = prop.ratio;
                    object.ratio += prop.ratio;
                    return - ((functionDamage * prop.ratio) / 25);
                };
                ratio += prop.ratio;

                // TODO: direct cause creation: bad bad bad
                final DamageModifier modifier = DamageModifier.builder()
                    .cause(Cause.of(EventContext.empty(), ((org.spongepowered.api.item.inventory.ItemStack) inventory[prop.slot]).createSnapshot(),
                                    prop, // We need this property to refer to the slot.
                                    object)) // We need this object later on.
                    .type(DamageModifierTypes.ARMOR)
                    .build();
                modifiers.add(new DamageFunction(modifier, function));
                first = false;
            }
            if (!modifiers.isEmpty()) {
                return Optional.of(modifiers);
            }
        }
        return Optional.empty();
    }

    /**
     * Only used in Vanilla. The Forge version is much different.
     * Basically, this accepts the various "objects" needed to work for an armor piece to be "damaged".
     *
     * This is also where we can likely throw a damage item event.
     *
     * @param entity
     * @param damageSource
     * @param modifier
     * @param damage
     */
    public static void acceptArmorModifier(final LivingEntity entity, final DamageSource damageSource, final DamageModifier modifier, double damage) {
        final Optional<DamageObject> property = modifier.getCause().first(DamageObject.class);
        final Iterable<net.minecraft.item.ItemStack> inventory = entity.getArmorInventoryList();
        if (property.isPresent()) {
            damage = Math.abs(damage) * 25;
            final net.minecraft.item.ItemStack stack = Iterables.get(inventory, property.get().slot);
            if (stack.isEmpty()) {
                throw new IllegalStateException("Invalid slot position " + property.get().slot);
            }

            final int itemDamage = (int) (damage / 25D < 1 ? 1 : damage / 25D);
            stack.damageItem(itemDamage, entity);
        }
    }

    public static EquipmentType resolveEquipment(final int slot) {
        if (slot == 0) {
            return EquipmentTypes.BOOTS;
        } else if (slot == 1) {
            return EquipmentTypes.LEGGINGS;
        } else if (slot == 2) {
            return EquipmentTypes.CHESTPLATE;
        } else if (slot == 3) {
            return EquipmentTypes.HEADWEAR;
        } else {
            return EquipmentTypes.WORN;
        }
    }

    public static Optional<DamageFunction> createResistanceModifier(final LivingEntity entityLivingBase, final DamageSource damageSource) {
        if (!damageSource.isDamageAbsolute() && entityLivingBase.isPotionActive(Effects.RESISTANCE) && damageSource != DamageSource.OUT_OF_WORLD) {
            final PotionEffect effect = ((PotionEffect) entityLivingBase.getActivePotionEffect(Effects.RESISTANCE));
            // TODO: direct cause creation: bad bad bad
            return Optional.of(new DamageFunction(DamageModifier.builder()
                                               .cause(Cause.of(EventContext.empty(), effect))
                                               .type(DamageModifierTypes.DEFENSIVE_POTION_EFFECT)
                                               .build(), createResistanceFunction(effect.getAmplifier())));
        }
        return Optional.empty();
    }

    private static double enchantmentDamageTracked;

    public static Optional<List<DamageFunction>> createEnchantmentModifiers(final LivingEntity entityLivingBase, final DamageSource damageSource) {
        if (!damageSource.isDamageAbsolute()) {
            final Iterable<net.minecraft.item.ItemStack> inventory = entityLivingBase.getArmorInventoryList();
            if (EnchantmentHelper.getEnchantmentModifierDamage(Lists.newArrayList(entityLivingBase.getArmorInventoryList()), damageSource) == 0) {
                return Optional.empty();
            }
            final List<DamageFunction> modifiers = new ArrayList<>();
            boolean first = true;
            int totalModifier = 0;
            for (final net.minecraft.item.ItemStack itemStack : inventory) {
                if (itemStack.isEmpty()) {
                    continue;
                }
                final Multimap<Enchantment, Short> enchantments = LinkedHashMultimap.create();
                final ListNBT enchantmentList = itemStack.getEnchantmentTagList();
                if (enchantmentList == null) {
                    continue;
                }

                for (int i = 0; i < enchantmentList.tagCount(); ++i) {
                    final short enchantmentId = enchantmentList.getCompound(i).getShort(Constants.Item.ITEM_ENCHANTMENT_ID);
                    final short level = enchantmentList.getCompound(i).getShort(Constants.Item.ITEM_ENCHANTMENT_LEVEL);

                    if (Enchantment.getEnchantmentByID(enchantmentId) != null) {
                        // Ok, we have an enchantment!
                        final Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentId);
                        final int temp = enchantment.calcModifierDamage(level, damageSource);
                        if (temp != 0) {
                            enchantments.put(enchantment, level);
                        }
                    }
                }
                final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack);

                for (final Map.Entry<Enchantment, Collection<Short>> enchantment : enchantments.asMap().entrySet()) {
                    final DamageObject object = new DamageObject();
                    int modifierTemp = 0;
                    for (final short level : enchantment.getValue()) {
                        modifierTemp += enchantment.getKey().calcModifierDamage(level, damageSource);
                    }
                    final int modifier = modifierTemp;
                    object.previousDamage = totalModifier;
                    if (object.previousDamage > 25) {
                        object.previousDamage = 25;
                    }
                    totalModifier += modifier;
                    object.augment = first;
                    object.ratio = modifier;
                    final DoubleUnaryOperator enchantmentFunction = damageIn -> {
                        if (object.augment) {
                            enchantmentDamageTracked = damageIn;
                        }
                        if (damageIn <= 0) {
                            return 0D;
                        }
                        final double actualDamage = enchantmentDamageTracked;
                        if (object.previousDamage > 25) {
                            return 0D;
                        }
                        double modifierDamage = actualDamage;
                        final double magicModifier;
                        if (modifier > 0 && modifier <= 20) {
                            final int j = 25 - modifier;
                            magicModifier = modifierDamage * j;
                            modifierDamage = magicModifier / 25.0F;
                        }
                        return -Math.max(actualDamage - modifierDamage, 0.0D);
                    };
                    if (first) {
                        first = false;
                    }

                    // TODO: direct cause creation: bad bad bad
                    final DamageModifier enchantmentModifier = DamageModifier.builder()
                        .cause(Cause.of(EventContext.empty(), enchantment, snapshot, entityLivingBase))
                        .type(DamageModifierTypes.ARMOR_ENCHANTMENT)
                        .build();
                    modifiers.add(new DamageFunction(enchantmentModifier, enchantmentFunction));
                }
                if (!modifiers.isEmpty()) {
                    return Optional.of(modifiers);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<DamageFunction> createAbsorptionModifier(final LivingEntity entityLivingBase,
                                                                                                             final DamageSource damageSource) {
        final float absorptionAmount = entityLivingBase.getAbsorptionAmount();
        if (absorptionAmount > 0) {
            final DoubleUnaryOperator function = damage ->
                -(Math.max(damage - Math.max(damage - absorptionAmount, 0.0F), 0.0F));
                // TODO: direct cause creation: bad bad bad
            final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(EventContext.empty(), entityLivingBase))
                .type(DamageModifierTypes.ABSORPTION)
                .build();
            return Optional.of(new DamageFunction(modifier, function));
        }
        return Optional.empty();
    }

    public static Location<World> findFirstMatchingBlock(final Entity entity, final AxisAlignedBB bb, final Predicate<BlockState> predicate) {
        final int i = MathHelper.floor(bb.minX);
        final int j = MathHelper.floor(bb.maxX + 1.0D);
        final int k = MathHelper.floor(bb.minY);
        final int l = MathHelper.floor(bb.maxY + 1.0D);
        final int i1 = MathHelper.floor(bb.minZ);
        final int j1 = MathHelper.floor(bb.maxZ + 1.0D);
        final ChunkProviderBridge spongeChunkProvider = (ChunkProviderBridge) entity.world.getChunkProvider();
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    final BlockPos blockPos = new BlockPos(k1, l1, i2);
                    final Chunk chunk = spongeChunkProvider.bridge$getLoadedChunkWithoutMarkingActive(blockPos.getX() >> 4, blockPos.getZ() >> 4);
                    if (chunk == null) {
                        continue;
                    }
                    if (predicate.test(chunk.getBlockState(blockPos))) {
                        return new Location<>((World) entity.world, k1, l1, i2);
                    }
                }
            }
        }

        // Entity is source of fire
        return ((org.spongepowered.api.entity.Entity) entity).getLocation();
    }

    /**
     * This applies various contexts based on the type of {@link DamageSource}, whether
     * it's provided by sponge or vanilla. This is not stack neutral, which is why it requires
     * a {@link CauseStackManager.StackFrame} reference to push onto the stack.
     * @param damageSource
     * @param frame
     */
    public static void generateCauseFor(final DamageSource damageSource, final CauseStackManager.StackFrame frame) {
        if (damageSource instanceof IndirectEntityDamageSource) {
            final net.minecraft.entity.Entity source = damageSource.getTrueSource();
            if (!(source instanceof PlayerEntity) && source instanceof OwnershipTrackedBridge) {
                final OwnershipTrackedBridge ownerBridge = (OwnershipTrackedBridge) source;
                ownerBridge.tracked$getNotifierReference().ifPresent(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
                ownerBridge.tracked$getOwnerReference().ifPresent(owner -> frame.addContext(EventContextKeys.OWNER, owner));
            }
        } else if (damageSource instanceof EntityDamageSource) {
            final net.minecraft.entity.Entity source = damageSource.getTrueSource();
            if (!(source instanceof PlayerEntity) && source instanceof OwnershipTrackedBridge) {
                final OwnershipTrackedBridge ownerBridge = (OwnershipTrackedBridge) source;
                ownerBridge.tracked$getNotifierReference().ifPresent(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
                ownerBridge.tracked$getOwnerReference().ifPresent(creator -> frame.addContext(EventContextKeys.CREATOR, creator));
            }
        } else if (damageSource instanceof BlockDamageSource) {
            final Location<org.spongepowered.api.world.World> location = ((BlockDamageSource) damageSource).getLocation();
            final BlockPos blockPos = VecHelper.toBlockPos(location);
            final ChunkBridge mixinChunk = (ChunkBridge) ((net.minecraft.world.World) location.getExtent()).getChunkAt(blockPos);
            mixinChunk.bridge$getBlockNotifier(blockPos).ifPresent(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
            mixinChunk.bridge$getBlockOwner(blockPos).ifPresent(owner -> frame.addContext(EventContextKeys.CREATOR, owner));
        }
        frame.pushCause(damageSource);
    }

    public static List<DamageFunction> createAttackEnchantmentFunction(
            final net.minecraft.item.ItemStack heldItem, final EnumCreatureAttribute creatureAttribute, final float attackStrength) {
        final Multimap<Enchantment, Integer> enchantments = LinkedHashMultimap.create();
        final List<DamageFunction> damageModifierFunctions = new ArrayList<>();
        if (!heldItem.isEmpty()) {
            final ListNBT nbttaglist = heldItem.getEnchantmentTagList();
            if (nbttaglist.isEmpty()) {
                return ImmutableList.of();
            }

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                final int j = nbttaglist.getCompound(i).getShort("id");
                final int enchantmentLevel = nbttaglist.getCompound(i).getShort("lvl");

                final Enchantment enchantment = Enchantment.getEnchantmentByID(j);
                if (enchantment != null) {
                    enchantments.put(enchantment, enchantmentLevel);
                }
            }
            if (enchantments.isEmpty()) {
                return ImmutableList.of();
            }
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(heldItem);

            for (final Map.Entry<Enchantment, Collection<Integer>> enchantment : enchantments.asMap().entrySet()) {
                final DamageModifier enchantmentModifier = DamageModifier.builder()
                        .type(DamageModifierTypes.WEAPON_ENCHANTMENT)
                        .cause(Cause.of(EventContext.empty(), snapshot, enchantment))
                        .build();
                final DoubleUnaryOperator enchantmentFunction = (damage) -> {
                    double totalDamage = 0;
                    for (final int level : enchantment.getValue()) {
                        totalDamage += (double) enchantment.getKey().calcDamageByCreature(level, creatureAttribute) * attackStrength;
                    }
                    return totalDamage;
                };
                damageModifierFunctions.add(new DamageFunction(enchantmentModifier, enchantmentFunction));
            }
        }

        return damageModifierFunctions;
    }

    public static DamageFunction provideCriticalAttackTuple(final PlayerEntity player) {
        // TODO: direct cause creation: bad bad bad
        final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(EventContext.empty(), player))
                .type(DamageModifierTypes.CRITICAL_HIT)
                .build();
        final DoubleUnaryOperator function = (damage) -> damage * .5F;
        return new DamageFunction(modifier, function);
    }

    public static DamageFunction provideCooldownAttackStrengthFunction(final PlayerEntity player,
            final float attackStrength) {
        // TODO: direct cause creation: bad bad bad
        final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(EventContext.empty(), player))
                .type(DamageModifierTypes.ATTACK_COOLDOWN)
                .build();
        // The formula is as follows:
        // Since damage needs to be "multiplied", this needs to basically add negative damage but re-add the "reduced" damage.
        final DoubleUnaryOperator function = (damage) -> - damage + (damage * (0.2F + attackStrength * attackStrength * 0.8F));
        return new DamageFunction(modifier, function);
    }

    public static Optional<DamageFunction> createShieldFunction(final LivingEntity entity, final DamageSource source, final float amount) {
        if (entity.isActiveItemStackBlocking() && amount > 0.0 && ((EntityLivingBaseAccessor) entity).accessor$canBlockDamageSource(source)) {
            // TODO: direct cause creation: bad bad bad
            final DamageModifier modifier = DamageModifier.builder()
                    .cause(Cause.of(EventContext.empty(), entity, ((ItemStack) entity.getActiveItemStack()).createSnapshot()))
                    .type(DamageModifierTypes.SHIELD)
                    .build();
            return Optional.of(new DamageFunction(modifier, (damage) -> -damage));
        }
        return Optional.empty();
    }
}
