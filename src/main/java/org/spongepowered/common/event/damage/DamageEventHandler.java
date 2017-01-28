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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import org.spongepowered.api.effect.potion.PotionEffect;
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
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

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
        if ((damageSource instanceof FallingBlockDamageSource) && entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null) {
            DamageModifier modifier = DamageModifier.builder()
                .cause(
                    Cause.of(NamedCause.of(DamageEntityEvent.HARD_HAT_ARMOR, ((ItemStack) entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD)).createSnapshot())))
                .type(DamageModifierTypes.HARD_HAT)
                .build();
            return Optional.of(new Tuple<>(modifier, HARD_HAT_FUNCTION));
        }
        return Optional.empty();
    }

    private static double damageToHandle;

    public static Optional<List<Tuple<DamageModifier, Function<? super Double, Double>>>> createArmorModifiers(EntityLivingBase entityLivingBase,
                                                                                                               DamageSource damageSource, double damage) {
        if (!damageSource.isDamageAbsolute()) {
            damage *= 25;
            net.minecraft.item.ItemStack[] inventory = Iterables.toArray(entityLivingBase.getArmorInventoryList(), net.minecraft.item.ItemStack.class);
            List<Tuple<DamageModifier, Function<? super Double, Double>>> modifiers = new ArrayList<>();
            List<DamageObject> damageObjects = new ArrayList<>();

            for (int index = 0; index < inventory.length; index++) {
                net.minecraft.item.ItemStack itemStack = inventory[index];
                if (itemStack.isEmpty()) {
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
        final Iterable<net.minecraft.item.ItemStack> inventory = entity.getArmorInventoryList();
        if (property.isPresent()) {
            damage = Math.abs(damage) * 25;
            net.minecraft.item.ItemStack stack = Iterables.get(inventory, property.get().slot);
            if (stack.isEmpty()) {
                throw new IllegalStateException("Invalid slot position " + property.get().slot);
            }

            int itemDamage = (int) (damage / 25D < 1 ? 1 : damage / 25D);
            stack.damageItem(itemDamage, entity);
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
        if (!damageSource.isDamageAbsolute() && entityLivingBase.isPotionActive(MobEffects.RESISTANCE) && damageSource != DamageSource.OUT_OF_WORLD) {
            PotionEffect effect = ((PotionEffect) entityLivingBase.getActivePotionEffect(MobEffects.RESISTANCE));
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
        Iterable<net.minecraft.item.ItemStack> inventory = entityLivingBase.getArmorInventoryList();
        if (EnchantmentHelper.getEnchantmentModifierDamage(Lists.newArrayList(entityLivingBase.getArmorInventoryList()), damageSource) == 0) {
            return Optional.empty();
        }
        List<Tuple<DamageModifier, Function<? super Double, Double>>> modifiers = new ArrayList<>();
        boolean first = true;
        int totalModifier = 0;
        for (net.minecraft.item.ItemStack itemStack : inventory) {
            if (itemStack.isEmpty()) {
                continue;
            }
            NBTTagList enchantmentList = itemStack.getEnchantmentTagList();
            if (enchantmentList == null) {
                continue;
            }
            for (int i = 0; i < enchantmentList.tagCount(); ++i) {
                final short enchantmentId = enchantmentList.getCompoundTagAt(i).getShort(NbtDataUtil.ITEM_ENCHANTMENT_ID);
                final short level = enchantmentList.getCompoundTagAt(i).getShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL);

                if (Enchantment.getEnchantmentByID(enchantmentId) != null) {
                    // Ok, we have an enchantment!
                    final Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentId);
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


    public static Location<World> findFirstMatchingBlock(Entity entity, AxisAlignedBB bb, Predicate<IBlockState> predicate) {
        int i = MathHelper.floor(bb.minX);
        int j = MathHelper.floor(bb.maxX + 1.0D);
        int k = MathHelper.floor(bb.minY);
        int l = MathHelper.floor(bb.maxY + 1.0D);
        int i1 = MathHelper.floor(bb.minZ);
        int j1 = MathHelper.floor(bb.maxZ + 1.0D);
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    BlockPos blockPos = new BlockPos(k1, l1, i2);
                    if (predicate.test(entity.world.getBlockState(blockPos))) {
                        return new Location<>((World) entity.world, k1, l1, i2);
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
            List<NamedCause> causeObjects = new ArrayList<>();
            causeObjects.add(NamedCause.source(damageSource));
            if (!(source instanceof EntityPlayer) && source != null) {
                final IMixinEntity mixinEntity = EntityUtil.toMixin(source);
                mixinEntity.getNotifierUser().ifPresent(notifier -> causeObjects.add(NamedCause.notifier(notifier)));
                mixinEntity.getCreatorUser().ifPresent(owner -> causeObjects.add(NamedCause.owner(owner)));

            }
            return Cause.builder().addAll(causeObjects).build();
        } else if (damageSource instanceof EntityDamageSource) {
            net.minecraft.entity.Entity source = damageSource.getEntity();
            List<NamedCause> causeObjects = new ArrayList<>();
            causeObjects.add(NamedCause.source(damageSource));
            if (!(source instanceof EntityPlayer) && source != null) {

                final IMixinEntity mixinEntity = EntityUtil.toMixin(source);
                mixinEntity.getNotifier().ifPresent(notifier -> causeObjects.add(NamedCause.notifier(notifier)));
                mixinEntity.getCreator().ifPresent(creator -> causeObjects.add(NamedCause.of(DamageEntityEvent.CREATOR, creator)));
            }
            return Cause.builder().addAll(causeObjects).build();
        } else if (damageSource instanceof BlockDamageSource) {
            final Cause.Builder builder = Cause.source(damageSource);
            Location<org.spongepowered.api.world.World> location = ((BlockDamageSource) damageSource).getLocation();
            BlockPos blockPos = ((IMixinLocation) (Object) location).getBlockPos();
            final IMixinChunk mixinChunk = (IMixinChunk) ((net.minecraft.world.World) location.getExtent())
                    .getChunkFromBlockCoords(blockPos);
            mixinChunk.getBlockNotifier(blockPos).ifPresent(notifier -> builder.named(NamedCause.notifier(notifier)));
            mixinChunk.getBlockOwner(blockPos).ifPresent(owner -> builder.named(NamedCause.of(DamageEntityEvent.CREATOR, owner)));
            return builder.build();
        } else {
            return Cause.of(NamedCause.source(damageSource));
        }
    }

    public static List<Tuple<DamageModifier, Function<? super Double, Double>>> createAttackEnchamntmentFunction(
            @Nullable net.minecraft.item.ItemStack heldItem, EnumCreatureAttribute creatureAttribute, float attackStrength) {
        final List<Tuple<DamageModifier, Function<? super Double, Double>>> damageModifierFunctions = new ArrayList<>();
        if (heldItem != null) {
            Supplier<ItemStackSnapshot> supplier = new Supplier<ItemStackSnapshot>() {
                private ItemStackSnapshot snapshot;
                @Override
                public ItemStackSnapshot get() {
                    if (this.snapshot == null) {
                        this.snapshot = ItemStackUtil.createSnapshot(heldItem);
                    }
                    return this.snapshot;
                }
            };
            NBTTagList nbttaglist = heldItem.getEnchantmentTagList();

            if (nbttaglist != null) {
                for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                    int j = nbttaglist.getCompoundTagAt(i).getShort("id");
                    int enchantmentLevel = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                    final Enchantment enchantment = Enchantment.getEnchantmentByID(j);
                    if (enchantment != null) {
                        final DamageModifier enchantmentModifier = DamageModifier.builder()
                                .type(DamageModifierTypes.WEAPON_ENCHANTMENT)
                                .cause(Cause.builder()
                                        .named("Weapon", supplier.get())
                                        .named("Enchantment", enchantment)
                                        .build())
                                .build();
                        Function<? super Double, Double> enchantmentFunction = (damage) ->
                                (double) enchantment.calcDamageByCreature(enchantmentLevel, creatureAttribute) * attackStrength;
                        damageModifierFunctions.add(new Tuple<>(enchantmentModifier, enchantmentFunction));
                    }
                }
            }
        }

        return damageModifierFunctions;
    }

    public static Tuple<DamageModifier, Function<? super Double, Double>> provideCriticalAttackTuple(EntityPlayer player) {
        final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.source(player).build())
                .type(DamageModifierTypes.CRITICAL_HIT)
                .build();
        Function<? super Double, Double> function = (damage) -> damage * 1.5F;
        return new Tuple<>(modifier, function);
    }

    public static DamageSource getEntityDamageSource(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }

        if (entity.world instanceof IMixinWorldServer) {
            IMixinWorldServer spongeWorld = (IMixinWorldServer) entity.world;
            final PhaseData peek = spongeWorld.getCauseTracker().getCurrentPhaseData();
            return peek.state.getPhase().createDestructionDamageSource(peek.state, peek.context, entity).orElse(null);
        }
        return null;
    }

    public static Tuple<DamageModifier, Function<? super Double, Double>> provideCooldownAttackStrengthFunction(EntityPlayer player,
            float attackStrength) {
        final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.source(player).build())
                .type(DamageModifierTypes.ATTACK_COOLDOWN)
                .build();
        // The formula is as follows:
        // Since damage needs to be "multiplied", this needs to basically add negative damage but re-add the "reduced" damage.
        Function<? super Double, Double> function = (damage) -> - damage + (damage * (0.2F + attackStrength * attackStrength * 0.8F));
        return new Tuple<>(modifier, function);
    }

    public static Optional<Tuple<DamageModifier, Function<? super Double, Double>>> createShieldFunction(EntityLivingBase entity, DamageSource source, float amount) {
        if (entity.isActiveItemStackBlocking() && amount > 0.0 && entity.canBlockDamageSource(source)) {
            final DamageModifier modifier = DamageModifier.builder()
                    .cause(Cause.source(entity)
                            .named(NamedCause.of(DamageEntityEvent.SHIELD, ((ItemStack) entity.getActiveItemStack()).createSnapshot()))
                            .build())
                    .type(DamageModifierTypes.SHIELD)
                    .build();
            return Optional.of(new Tuple<>(modifier, (damage) -> -damage));
        }
        return Optional.empty();
    }
}
