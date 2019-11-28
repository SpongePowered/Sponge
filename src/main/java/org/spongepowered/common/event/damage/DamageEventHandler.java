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
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
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

    public static Optional<DamageFunction> createHardHatModifier(final EntityLivingBase entityLivingBase,
            final DamageSource damageSource) {
        if ((damageSource instanceof FallingBlockDamageSource) && !entityLivingBase.func_184582_a(EntityEquipmentSlot.HEAD).func_190926_b()) {
            // TODO: direct cause creation: bad bad bad
            final DamageModifier modifier = DamageModifier.builder()
                .cause(
                    Cause.of(EventContext.empty(), ((ItemStack) entityLivingBase.func_184582_a(EntityEquipmentSlot.HEAD)).createSnapshot()))
                .type(DamageModifierTypes.HARD_HAT)
                .build();
            return Optional.of(new DamageFunction(modifier, HARD_HAT_FUNCTION));
        }
        return Optional.empty();
    }

    private static double damageToHandle;

    public static Optional<List<DamageFunction>> createArmorModifiers(final EntityLivingBase entityLivingBase,
            final DamageSource damageSource, double damage) {
        if (!damageSource.func_76363_c()) {
            damage *= 25;
            final net.minecraft.item.ItemStack[] inventory = Iterables.toArray(entityLivingBase.func_184193_aE(), net.minecraft.item.ItemStack.class);
            final List<DamageFunction> modifiers = new ArrayList<>();
            final List<DamageObject> damageObjects = new ArrayList<>();

            for (int index = 0; index < inventory.length; index++) {
                final net.minecraft.item.ItemStack itemStack = inventory[index];
                if (itemStack.func_190926_b()) {
                    continue;
                }
                final Item item = itemStack.func_77973_b();
                if (item instanceof ItemArmor) {
                    final ItemArmor armor = (ItemArmor) item;
                    final double reduction = armor.field_77879_b / 25D;
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
    public static void acceptArmorModifier(final EntityLivingBase entity, final DamageSource damageSource, final DamageModifier modifier, double damage) {
        final Optional<DamageObject> property = modifier.getCause().first(DamageObject.class);
        final Iterable<net.minecraft.item.ItemStack> inventory = entity.func_184193_aE();
        if (property.isPresent()) {
            damage = Math.abs(damage) * 25;
            final net.minecraft.item.ItemStack stack = Iterables.get(inventory, property.get().slot);
            if (stack.func_190926_b()) {
                throw new IllegalStateException("Invalid slot position " + property.get().slot);
            }

            final int itemDamage = (int) (damage / 25D < 1 ? 1 : damage / 25D);
            stack.func_77972_a(itemDamage, entity);
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

    public static Optional<DamageFunction> createResistanceModifier(final EntityLivingBase entityLivingBase, final DamageSource damageSource) {
        if (!damageSource.func_151517_h() && entityLivingBase.func_70644_a(MobEffects.field_76429_m) && damageSource != DamageSource.field_76380_i) {
            final PotionEffect effect = ((PotionEffect) entityLivingBase.func_70660_b(MobEffects.field_76429_m));
            // TODO: direct cause creation: bad bad bad
            return Optional.of(new DamageFunction(DamageModifier.builder()
                                               .cause(Cause.of(EventContext.empty(), effect))
                                               .type(DamageModifierTypes.DEFENSIVE_POTION_EFFECT)
                                               .build(), createResistanceFunction(effect.getAmplifier())));
        }
        return Optional.empty();
    }

    private static double enchantmentDamageTracked;

    public static Optional<List<DamageFunction>> createEnchantmentModifiers(final EntityLivingBase entityLivingBase, final DamageSource damageSource) {
        if (!damageSource.func_151517_h()) {
            final Iterable<net.minecraft.item.ItemStack> inventory = entityLivingBase.func_184193_aE();
            if (EnchantmentHelper.func_77508_a(Lists.newArrayList(entityLivingBase.func_184193_aE()), damageSource) == 0) {
                return Optional.empty();
            }
            final List<DamageFunction> modifiers = new ArrayList<>();
            boolean first = true;
            int totalModifier = 0;
            for (final net.minecraft.item.ItemStack itemStack : inventory) {
                if (itemStack.func_190926_b()) {
                    continue;
                }
                final Multimap<Enchantment, Short> enchantments = LinkedHashMultimap.create();
                final NBTTagList enchantmentList = itemStack.func_77986_q();
                if (enchantmentList == null) {
                    continue;
                }

                for (int i = 0; i < enchantmentList.func_74745_c(); ++i) {
                    final short enchantmentId = enchantmentList.func_150305_b(i).func_74765_d(Constants.Item.ITEM_ENCHANTMENT_ID);
                    final short level = enchantmentList.func_150305_b(i).func_74765_d(Constants.Item.ITEM_ENCHANTMENT_LEVEL);

                    if (Enchantment.func_185262_c(enchantmentId) != null) {
                        // Ok, we have an enchantment!
                        final Enchantment enchantment = Enchantment.func_185262_c(enchantmentId);
                        final int temp = enchantment.func_77318_a(level, damageSource);
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
                        modifierTemp += enchantment.getKey().func_77318_a(level, damageSource);
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

    public static Optional<DamageFunction> createAbsorptionModifier(final EntityLivingBase entityLivingBase,
                                                                                                             final DamageSource damageSource) {
        final float absorptionAmount = entityLivingBase.func_110139_bj();
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

    public static Location<World> findFirstMatchingBlock(final Entity entity, final AxisAlignedBB bb, final Predicate<IBlockState> predicate) {
        final int i = MathHelper.func_76128_c(bb.field_72340_a);
        final int j = MathHelper.func_76128_c(bb.field_72336_d + 1.0D);
        final int k = MathHelper.func_76128_c(bb.field_72338_b);
        final int l = MathHelper.func_76128_c(bb.field_72337_e + 1.0D);
        final int i1 = MathHelper.func_76128_c(bb.field_72339_c);
        final int j1 = MathHelper.func_76128_c(bb.field_72334_f + 1.0D);
        final ChunkProviderBridge spongeChunkProvider = (ChunkProviderBridge) entity.field_70170_p.func_72863_F();
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    final BlockPos blockPos = new BlockPos(k1, l1, i2);
                    final Chunk chunk = spongeChunkProvider.bridge$getLoadedChunkWithoutMarkingActive(blockPos.func_177958_n() >> 4, blockPos.func_177952_p() >> 4);
                    if (chunk == null) {
                        continue;
                    }
                    if (predicate.test(chunk.func_177435_g(blockPos))) {
                        return new Location<>((World) entity.field_70170_p, k1, l1, i2);
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
        if (damageSource instanceof EntityDamageSourceIndirect) {
            final net.minecraft.entity.Entity source = damageSource.func_76346_g();
            if (!(source instanceof EntityPlayer) && source instanceof OwnershipTrackedBridge) {
                final OwnershipTrackedBridge ownerBridge = (OwnershipTrackedBridge) source;
                ownerBridge.tracked$getNotifierReference().ifPresent(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
                ownerBridge.tracked$getOwnerReference().ifPresent(owner -> frame.addContext(EventContextKeys.OWNER, owner));
            }
        } else if (damageSource instanceof EntityDamageSource) {
            final net.minecraft.entity.Entity source = damageSource.func_76346_g();
            if (!(source instanceof EntityPlayer) && source instanceof OwnershipTrackedBridge) {
                final OwnershipTrackedBridge ownerBridge = (OwnershipTrackedBridge) source;
                ownerBridge.tracked$getNotifierReference().ifPresent(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
                ownerBridge.tracked$getOwnerReference().ifPresent(creator -> frame.addContext(EventContextKeys.CREATOR, creator));
            }
        } else if (damageSource instanceof BlockDamageSource) {
            final Location<org.spongepowered.api.world.World> location = ((BlockDamageSource) damageSource).getLocation();
            final BlockPos blockPos = VecHelper.toBlockPos(location);
            final ChunkBridge mixinChunk = (ChunkBridge) ((net.minecraft.world.World) location.getExtent()).func_175726_f(blockPos);
            mixinChunk.bridge$getBlockNotifier(blockPos).ifPresent(notifier -> frame.addContext(EventContextKeys.NOTIFIER, notifier));
            mixinChunk.bridge$getBlockOwner(blockPos).ifPresent(owner -> frame.addContext(EventContextKeys.CREATOR, owner));
        }
        frame.pushCause(damageSource);
    }

    public static List<DamageFunction> createAttackEnchantmentFunction(
            final net.minecraft.item.ItemStack heldItem, final EnumCreatureAttribute creatureAttribute, final float attackStrength) {
        final Multimap<Enchantment, Integer> enchantments = LinkedHashMultimap.create();
        final List<DamageFunction> damageModifierFunctions = new ArrayList<>();
        if (!heldItem.func_190926_b()) {
            final NBTTagList nbttaglist = heldItem.func_77986_q();
            if (nbttaglist.func_82582_d()) {
                return ImmutableList.of();
            }

            for (int i = 0; i < nbttaglist.func_74745_c(); ++i) {
                final int j = nbttaglist.func_150305_b(i).func_74765_d("id");
                final int enchantmentLevel = nbttaglist.func_150305_b(i).func_74765_d("lvl");

                final Enchantment enchantment = Enchantment.func_185262_c(j);
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
                        totalDamage += (double) enchantment.getKey().func_152376_a(level, creatureAttribute) * attackStrength;
                    }
                    return totalDamage;
                };
                damageModifierFunctions.add(new DamageFunction(enchantmentModifier, enchantmentFunction));
            }
        }

        return damageModifierFunctions;
    }

    public static DamageFunction provideCriticalAttackTuple(final EntityPlayer player) {
        // TODO: direct cause creation: bad bad bad
        final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(EventContext.empty(), player))
                .type(DamageModifierTypes.CRITICAL_HIT)
                .build();
        final DoubleUnaryOperator function = (damage) -> damage * .5F;
        return new DamageFunction(modifier, function);
    }

    public static DamageFunction provideCooldownAttackStrengthFunction(final EntityPlayer player,
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

    public static Optional<DamageFunction> createShieldFunction(final EntityLivingBase entity, final DamageSource source, final float amount) {
        if (entity.func_184585_cz() && amount > 0.0 && ((EntityLivingBaseAccessor) entity).accessor$canBlockDamageSource(source)) {
            // TODO: direct cause creation: bad bad bad
            final DamageModifier modifier = DamageModifier.builder()
                    .cause(Cause.of(EventContext.empty(), entity, ((ItemStack) entity.func_184607_cu()).createSnapshot()))
                    .type(DamageModifierTypes.SHIELD)
                    .build();
            return Optional.of(new DamageFunction(modifier, (damage) -> -damage));
        }
        return Optional.empty();
    }
}
