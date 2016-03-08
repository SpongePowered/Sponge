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
package org.spongepowered.common.event;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.FallingBlockDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class DamageEventHandler {

    public static final Function<Double, Double> HARD_HAT_FUNCTION = damage -> -(damage - (damage * 0.75F));
    public static final Function<Double, Double> BLOCKING_FUNCTION = damage -> -(damage - ((1.0F + damage) * 0.5F));

    public static Function<Double, Double> createResistanceFunction(int resistanceAmplifier) {
        final int base = (resistanceAmplifier + 1) * 5;
        final int modifier = 25 - base;
        return damage -> -(damage - ((damage.floatValue() * (float) modifier) / 25.0F));
    }


    public static Optional<Tuple<DamageModifier, Function<? super Double, Double>>> createHardHatModifier(EntityLivingBase entityLivingBase,
                                                                                                          DamageSource damageSource) {
        if ((damageSource instanceof FallingBlockDamageSource) && entityLivingBase.getEquipmentInSlot(4) != null) {
            DamageModifier modifier = DamageModifier.builder()
                .cause(
                    Cause.of(NamedCause.of(DamageEntityEvent.HARD_HAT_ARMOR, ((ItemStack) entityLivingBase.getEquipmentInSlot(4)).createSnapshot())))
                .type(DamageModifierTypes.HARD_HAT)
                .build();
            return Optional.of(new Tuple<>(modifier, HARD_HAT_FUNCTION));
        }
        return Optional.empty();
    }

    public static Optional<Tuple<DamageModifier, Function<? super Double, Double>>> createBlockingModifier(EntityLivingBase entityLivingBase,
                                                                                                           DamageSource damageSource) {
        if (!damageSource.isUnblockable() && (entityLivingBase instanceof EntityPlayer && ((EntityPlayer) entityLivingBase).isBlocking())) {
            DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(NamedCause
                                    .of(DamageEntityEvent.BLOCKING,
                                        ((ItemStack) ((EntityPlayer) entityLivingBase).getCurrentEquippedItem()).createSnapshot())))
                .type(DamageModifierTypes.BLOCKING)
                .build();
            return Optional.of(new Tuple<>(modifier, BLOCKING_FUNCTION));
        }
        return Optional.empty();
    }

    private static double damageToHandle;

    public static Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> createArmorModifiers(EntityLivingBase entityLivingBase,
                                                                                                               DamageSource damageSource, double damage) {
        if (!damageSource.isDamageAbsolute()) {
            damage *= 25;
            net.minecraft.item.ItemStack[] inventory = entityLivingBase instanceof EntityPlayer
                                                       ? ((EntityPlayer) entityLivingBase).inventory.armorInventory : entityLivingBase.getInventory();
            List<Tuple<DamageModifier, Function<? super Double, Double>>> modifiers = new ArrayList<>();
            List<DamageObject> damageObjects = new ArrayList<>();
            for (int index = 0; index < inventory.length; index++) {
                net.minecraft.item.ItemStack itemStack = inventory[index];
                if (itemStack == null) {
                    continue;
                }
                Item item = itemStack.getItem();
                if (item instanceof ItemArmor) {
                    ItemArmor armor = (ItemArmor) item;
                    double reduction = armor.damageReduceAmount / 25D;
                    DamageObject object = new DamageObject();
                    object.slot = index;
                    object.ratio = reduction;
                    damageObjects.add(object);
                }
            }
            boolean first = true;
            double ratio = 0;

            for (DamageObject prop : damageObjects) {
                EquipmentType type = resolveEquipment(prop.slot);

                final DamageObject object = new DamageObject();
                object.ratio = ratio;
                if (first) {
                    object.previousDamage = damage;
                    object.augment = true;
                }
                Function<? super Double, Double> function = incomingDamage -> {
                    incomingDamage *= 25;
                    if (object.augment) {
                        // This is the damage that needs to be archived for the "first" armor modifier
                        // function since the armor modifiers work based on the initial damage and not as
                        // a chain one after another.
                        damageToHandle = incomingDamage;
                    }
                    double functionDamage = damageToHandle;
                    object.previousDamage = functionDamage;
                    object.ratio = prop.ratio;
                    object.ratio += prop.ratio;
                    return - ((functionDamage * prop.ratio) / 25);
                };
                ratio += prop.ratio;

                DamageModifier modifier = DamageModifier.builder()
                    .cause(Cause.of(NamedCause.of(DamageEntityEvent.GENERAL_ARMOR + ":" + type.getId(),
                                                  ((org.spongepowered.api.item.inventory.ItemStack) inventory[prop.slot]).createSnapshot()),
                                    NamedCause.of("ArmorProperty", prop), // We need this property to refer to the slot.
                                    NamedCause.of("0xDEADBEEF", object))) // We need this object later on.
                    .type(DamageModifierTypes.ARMOR)
                    .build();
                modifiers.add(new Tuple<>(modifier, function));
                first = false;
            }
            if (modifiers.isEmpty()) {
                return Optional.empty();
            } else {
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
    public static void acceptArmorModifier(EntityLivingBase entity, DamageSource damageSource, DamageModifier modifier, double damage) {
        Optional<DamageObject> property = modifier.getCause().first(DamageObject.class);
        final net.minecraft.item.ItemStack[] inventory = entity instanceof EntityPlayer ? ((EntityPlayer) entity).inventory.armorInventory : entity.getInventory();
        if (property.isPresent()) {
            damage = Math.abs(damage) * 25;
            net.minecraft.item.ItemStack stack = inventory[property.get().slot];
            int itemDamage = (int) (damage / 25D < 1 ? 1 : damage / 25D);
            stack.damageItem(itemDamage, entity);

            if (stack.stackSize <= 0) {
                inventory[property.get().slot] = null;
            }
        }
    }

    public static EquipmentType resolveEquipment(int slot) {
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

    public static Optional<Tuple<DamageModifier, Function<? super Double, Double>>> createResistanceModifier(EntityLivingBase entityLivingBase,
                                                                                                             DamageSource damageSource) {
        if (!damageSource.isDamageAbsolute() && entityLivingBase.isPotionActive(Potion.resistance) && damageSource != DamageSource.outOfWorld) {
            PotionEffect effect = ((PotionEffect) entityLivingBase.getActivePotionEffect(Potion.resistance));
            return Optional.of(new Tuple<>(DamageModifier.builder()
                                               .cause(Cause.of(NamedCause.of(DamageEntityEvent.RESISTANCE, effect)))
                                               .type(DamageModifierTypes.DEFENSIVE_POTION_EFFECT)
                                               .build(), createResistanceFunction(effect.getAmplifier())));
        }
        return Optional.empty();
    }

    private static double enchantmentDamageTracked;
    private static double previousEnchantmentModifier = 0;

    public static Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> createEnchantmentModifiers(EntityLivingBase entityLivingBase, DamageSource damageSource) {
        net.minecraft.item.ItemStack[] inventory = entityLivingBase instanceof EntityPlayer ? ((EntityPlayer) entityLivingBase).inventory.armorInventory : entityLivingBase.getInventory();
        if (EnchantmentHelper.getEnchantmentModifierDamage(inventory, damageSource) == 0) {
            return Optional.empty();
        }
        List<Tuple<DamageModifier, Function<? super Double, Double>>> modifiers = new ArrayList<>();
        boolean first = true;
        int totalModifier = 0;
        for (net.minecraft.item.ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            NBTTagList enchantmentList = itemStack.getEnchantmentTagList();
            if (enchantmentList == null) {
                continue;
            }
            for (int i = 0; i < enchantmentList.tagCount(); ++i) {
                final short enchantmentId = enchantmentList.getCompoundTagAt(i).getShort(NbtDataUtil.ITEM_ENCHANTMENT_ID);
                final short level = enchantmentList.getCompoundTagAt(i).getShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL);

                if (Enchantment.getEnchantmentById(enchantmentId) != null) {
                    // Ok, we have an enchantment!
                    final Enchantment enchantment = Enchantment.getEnchantmentById(enchantmentId);
                    final int temp = enchantment.calcModifierDamage(level, damageSource);
                    if (temp != 0) {
                        ItemStackSnapshot snapshot = ((ItemStack) itemStack).createSnapshot();
                        // Now we can actually consider this as a modifier!

                        final DamageObject object = new DamageObject();
                        int modifier = enchantment.calcModifierDamage(level, damageSource);
                        object.previousDamage = totalModifier;
                        if (object.previousDamage > 25) {
                            object.previousDamage = 25;
                        }
                        totalModifier += modifier;
                        object.augment = first;
                        object.ratio = modifier;
                        Function<? super Double, Double> enchantmentFunction = damageIn -> {
                            if (object.augment) {
                                enchantmentDamageTracked = damageIn;
                            }
                            if (damageIn <= 0) {
                                return 0D;
                            }
                            double actualDamage = enchantmentDamageTracked;
                            if (object.previousDamage > 25) {
                                return 0D;
                            }
                            double modifierDamage = actualDamage;
                            double magicModifier;
                            if (modifier > 0 && modifier <= 20) {
                                int j = 25 - modifier;
                                magicModifier = modifierDamage * (float) j;
                                modifierDamage = magicModifier / 25.0F;
                            }
                            return - Math.max(actualDamage - modifierDamage, 0.0D);
                        };
                        if (first) {
                            first = false;
                        }

                        DamageModifier enchantmentModifier = DamageModifier.builder()
                            .cause(Cause.of(NamedCause.of("ArmorEnchantment", enchantment),
                                            NamedCause.of("ItemStack", snapshot),
                                            NamedCause.source(entityLivingBase)))
                            .type(DamageModifierTypes.ARMOR_ENCHANTMENT)
                            .build();
                        modifiers.add(new Tuple<>(enchantmentModifier, enchantmentFunction));
                    }
                }
            }
        }
        if (!modifiers.isEmpty()) {
            return Optional.of(modifiers);
        } else {
            return Optional.empty();
        }

    }

    public static Optional<Tuple<DamageModifier, Function<? super Double, Double>>> createAbsorptionModifier(EntityLivingBase entityLivingBase,
                                                                                                             DamageSource damageSource) {
        final float absorptionAmount = entityLivingBase.getAbsorptionAmount();
        if (absorptionAmount > 0) {
            Function<? super Double, Double> function = damage ->
                -(Math.max(damage - Math.max(damage - absorptionAmount, 0.0F), 0.0F));
            DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(NamedCause.of(DamageEntityEvent.ABSORPTION, entityLivingBase),
                                NamedCause.of(DamageEntityEvent.CREATOR, entityLivingBase)))
                .type(DamageModifierTypes.ABSORPTION)
                .build();
            return Optional.of(new Tuple<>(modifier, function));
        }
        return Optional.empty();
    }


    public static Location<World> findFirstMatchingBlock(Entity entity, AxisAlignedBB bb, Predicate<Block> predicate) {
        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(bb.minZ);
        int j1 = MathHelper.floor_double(bb.maxZ + 1.0D);
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    BlockPos blockPos = new BlockPos(k1, l1, i2);
                    if (predicate.test(entity.worldObj.getBlockState(blockPos).getBlock())) {
                        return new Location<>((World) entity.worldObj, k1, l1, i2);
                    }
                }
            }
        }
        SpongeImpl.getLogger().error("Could not find a Source block!");
        return ((org.spongepowered.api.entity.Entity) entity).getLocation();
    }

    public static Cause generateCauseFor(DamageSource damageSource) {
        if (damageSource instanceof EntityDamageSourceIndirect) {
            net.minecraft.entity.Entity source = damageSource.getEntity();
            Optional<User> owner = source == null ? Optional.empty() : ((IMixinEntity) source).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            if (owner.isPresent()) {
                return Cause.of(NamedCause.source(damageSource),
                                 NamedCause.of(DamageEntityEvent.CREATOR, owner.get()));
            } else {
                return Cause.of(NamedCause.source(damageSource));
            }
        } else if (damageSource instanceof EntityDamageSource) {
            net.minecraft.entity.Entity source = damageSource.getEntity();
            Optional<User> owner = ((IMixinEntity) source).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            Optional<User> notifier = ((IMixinEntity) source).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
            List<NamedCause> causeObjects = new ArrayList<>();
            causeObjects.add(NamedCause.source(damageSource));
            if (notifier.isPresent()) {
                causeObjects.add(NamedCause.notifier(notifier.get()));
            }
            if (owner.isPresent()) {
                causeObjects.add(NamedCause.of(DamageEntityEvent.CREATOR, owner.get()));
            }
            return Cause.builder().addAll(causeObjects).build();
        } else if (damageSource instanceof BlockDamageSource) {
            List<NamedCause> causeObjects = new ArrayList<>();
            Location<org.spongepowered.api.world.World> location = ((BlockDamageSource) damageSource).getLocation();
            BlockPos blockPos = VecHelper.toBlockPos(location);
            Optional<User> owner = ((IMixinChunk) ((net.minecraft.world.World) location.getExtent())
                .getChunkFromBlockCoords(blockPos)).getBlockOwner(blockPos);
            Optional<User> notifier = ((IMixinChunk) ((net.minecraft.world.World) location.getExtent())
                .getChunkFromBlockCoords(blockPos)).getBlockNotifier(blockPos);
            causeObjects.add(NamedCause.source(damageSource));
            if (notifier.isPresent()) {
                causeObjects.add(NamedCause.notifier(notifier.get()));
            }
            if (owner.isPresent()) {
                causeObjects.add(NamedCause.of(DamageEntityEvent.CREATOR, owner.get()));
            }
            return Cause.builder().addAll(causeObjects).build();
        } else {
            return Cause.of(NamedCause.source(damageSource));
        }
    }
}
