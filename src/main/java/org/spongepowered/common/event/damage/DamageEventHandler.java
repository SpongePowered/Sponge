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
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
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
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class DamageEventHandler {

    public static final DoubleUnaryOperator HARD_HAT_FUNCTION = damage -> -(damage - (damage * 0.75F));
    public static final DoubleUnaryOperator BLOCKING_FUNCTION = damage -> -(damage - ((1.0F + damage) * 0.5F));

    public static DoubleUnaryOperator createResistanceFunction(int resistanceAmplifier) {
        final int base = (resistanceAmplifier + 1) * 5;
        final int modifier = 25 - base;
        return damage -> -(damage - ((damage * modifier) / 25.0F));
    }

    public static Optional<DamageFunction> createHardHatModifier(EntityLivingBase entityLivingBase,
            DamageSource damageSource) {
        if ((damageSource instanceof FallingBlockDamageSource) && !entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
            // TODO: direct cause creation: bad bad bad
            DamageModifier modifier = DamageModifier.builder()
                .cause(
                    Cause.of(EventContext.empty(), ((ItemStack) entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD)).createSnapshot()))
                .type(DamageModifierTypes.HARD_HAT)
                .build();
            return Optional.of(new DamageFunction(modifier, HARD_HAT_FUNCTION));
        }
        return Optional.empty();
    }

    private static double damageToHandle;

    public static Optional<List<DamageFunction>> createArmorModifiers(EntityLivingBase entityLivingBase,
            DamageSource damageSource, double damage) {
        if (!damageSource.isDamageAbsolute()) {
            damage *= 25;
            net.minecraft.item.ItemStack[] inventory = Iterables.toArray(entityLivingBase.getArmorInventoryList(), net.minecraft.item.ItemStack.class);
            List<DamageFunction> modifiers = new ArrayList<>();
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
                DoubleUnaryOperator function = incomingDamage -> {
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

                // TODO: direct cause creation: bad bad bad
                DamageModifier modifier = DamageModifier.builder()
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

    public static Optional<DamageFunction> createResistanceModifier(EntityLivingBase entityLivingBase, DamageSource damageSource) {
        if (!damageSource.isDamageAbsolute() && entityLivingBase.isPotionActive(MobEffects.RESISTANCE) && damageSource != DamageSource.OUT_OF_WORLD) {
            PotionEffect effect = ((PotionEffect) entityLivingBase.getActivePotionEffect(MobEffects.RESISTANCE));
            // TODO: direct cause creation: bad bad bad
            return Optional.of(new DamageFunction(DamageModifier.builder()
                                               .cause(Cause.of(EventContext.empty(), effect))
                                               .type(DamageModifierTypes.DEFENSIVE_POTION_EFFECT)
                                               .build(), createResistanceFunction(effect.getAmplifier())));
        }
        return Optional.empty();
    }

    private static double enchantmentDamageTracked;

    public static Optional<List<DamageFunction>> createEnchantmentModifiers(EntityLivingBase entityLivingBase, DamageSource damageSource) {
        Iterable<net.minecraft.item.ItemStack> inventory = entityLivingBase.getArmorInventoryList();
        if (EnchantmentHelper.getEnchantmentModifierDamage(Lists.newArrayList(entityLivingBase.getArmorInventoryList()), damageSource) == 0) {
            return Optional.empty();
        }
        List<DamageFunction> modifiers = new ArrayList<>();
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
                        DoubleUnaryOperator enchantmentFunction = damageIn -> {
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
                                magicModifier = modifierDamage * j;
                                modifierDamage = magicModifier / 25.0F;
                            }
                            return - Math.max(actualDamage - modifierDamage, 0.0D);
                        };
                        if (first) {
                            first = false;
                        }

                        // TODO: direct cause creation: bad bad bad
                        DamageModifier enchantmentModifier = DamageModifier.builder()
                            .cause(Cause.of(EventContext.empty(), enchantment, snapshot, entityLivingBase))
                            .type(DamageModifierTypes.ARMOR_ENCHANTMENT)
                            .build();
                        modifiers.add(new DamageFunction(enchantmentModifier, enchantmentFunction));
                    }
                }
            }
        }
        if (!modifiers.isEmpty()) {
            return Optional.of(modifiers);
        }
        return Optional.empty();
    }

    public static Optional<DamageFunction> createAbsorptionModifier(EntityLivingBase entityLivingBase,
                                                                                                             DamageSource damageSource) {
        final float absorptionAmount = entityLivingBase.getAbsorptionAmount();
        if (absorptionAmount > 0) {
            DoubleUnaryOperator function = damage ->
                -(Math.max(damage - Math.max(damage - absorptionAmount, 0.0F), 0.0F));
                // TODO: direct cause creation: bad bad bad
            DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(EventContext.empty(), entityLivingBase))
                .type(DamageModifierTypes.ABSORPTION)
                .build();
            return Optional.of(new DamageFunction(modifier, function));
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
        final IMixinChunkProviderServer spongeChunkProvider = (IMixinChunkProviderServer) entity.world.getChunkProvider();
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = k; l1 < l; ++l1) {
                for (int i2 = i1; i2 < j1; ++i2) {
                    BlockPos blockPos = new BlockPos(k1, l1, i2);
                    final Chunk chunk = spongeChunkProvider.getLoadedChunkWithoutMarkingActive(blockPos.getX() >> 4, blockPos.getZ() >> 4);
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

    public static void generateCauseFor(DamageSource damageSource) {
        if (damageSource instanceof EntityDamageSourceIndirect) {
            net.minecraft.entity.Entity source = damageSource.getTrueSource();
            if (!(source instanceof EntityPlayer) && source != null) {
                final IMixinEntity mixinEntity = EntityUtil.toMixin(source);
                mixinEntity.getNotifierUser().ifPresent(notifier -> Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier));
                mixinEntity.getCreatorUser().ifPresent(owner -> Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, owner));
            }
        } else if (damageSource instanceof EntityDamageSource) {
            net.minecraft.entity.Entity source = damageSource.getTrueSource();
            if (!(source instanceof EntityPlayer) && source != null) {
                final IMixinEntity mixinEntity = EntityUtil.toMixin(source);
                // TODO only have a UUID, want a user
                mixinEntity.getNotifierUser().ifPresent(notifier -> Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier));
                mixinEntity.getCreatorUser().ifPresent(creator -> Sponge.getCauseStackManager().addContext(EventContextKeys.CREATOR, creator));
            }
        } else if (damageSource instanceof BlockDamageSource) {
            Location<org.spongepowered.api.world.World> location = ((BlockDamageSource) damageSource).getLocation();
            BlockPos blockPos = ((IMixinLocation) (Object) location).getBlockPos();
            final IMixinChunk mixinChunk = (IMixinChunk) ((net.minecraft.world.World) location.getExtent()).getChunkFromBlockCoords(blockPos);
            mixinChunk.getBlockNotifier(blockPos).ifPresent(notifier -> Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, notifier));
            mixinChunk.getBlockOwner(blockPos).ifPresent(owner -> Sponge.getCauseStackManager().addContext(EventContextKeys.CREATOR, owner));
        }
        Sponge.getCauseStackManager().pushCause(damageSource);
    }

    public static List<DamageFunction> createAttackEnchantmentFunction(
            net.minecraft.item.ItemStack heldItem, EnumCreatureAttribute creatureAttribute, float attackStrength) {
        final List<DamageFunction> damageModifierFunctions = new ArrayList<>();
        if (!heldItem.isEmpty()) {
            Supplier<ItemStackSnapshot> supplier = new Supplier<ItemStackSnapshot>() {
                private ItemStackSnapshot snapshot;
                @Override
                public ItemStackSnapshot get() {
                    if (this.snapshot == null) {
                        this.snapshot = ItemStackUtil.snapshotOf(heldItem);
                    }
                    return this.snapshot;
                }
            };
            NBTTagList nbttaglist = heldItem.getEnchantmentTagList();

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                int j = nbttaglist.getCompoundTagAt(i).getShort("id");
                int enchantmentLevel = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                    final Enchantment enchantment = Enchantment.getEnchantmentByID(j);
                    if (enchantment != null) {
                        final DamageModifier enchantmentModifier = DamageModifier.builder()
                            .type(DamageModifierTypes.WEAPON_ENCHANTMENT)
                            .cause(Cause.of(EventContext.empty(), supplier.get(), enchantment))
                            .build();
                        DoubleUnaryOperator enchantmentFunction = (damage) ->
                                (double) enchantment.calcDamageByCreature(enchantmentLevel, creatureAttribute) * attackStrength;
                        damageModifierFunctions.add(new DamageFunction(enchantmentModifier, enchantmentFunction));

                }
            }
        }

        return damageModifierFunctions;
    }

    public static DamageFunction provideCriticalAttackTuple(EntityPlayer player) {
        // TODO: direct cause creation: bad bad bad
        final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(EventContext.empty(), player))
                .type(DamageModifierTypes.CRITICAL_HIT)
                .build();
        DoubleUnaryOperator function = (damage) -> damage * 1.5F;
        return new DamageFunction(modifier, function);
    }

    public static DamageFunction provideCooldownAttackStrengthFunction(EntityPlayer player,
            float attackStrength) {
        // TODO: direct cause creation: bad bad bad
        final DamageModifier modifier = DamageModifier.builder()
                .cause(Cause.of(EventContext.empty(), player))
                .type(DamageModifierTypes.ATTACK_COOLDOWN)
                .build();
        // The formula is as follows:
        // Since damage needs to be "multiplied", this needs to basically add negative damage but re-add the "reduced" damage.
        DoubleUnaryOperator function = (damage) -> - damage + (damage * (0.2F + attackStrength * attackStrength * 0.8F));
        return new DamageFunction(modifier, function);
    }

    public static Optional<DamageFunction> createShieldFunction(EntityLivingBase entity, DamageSource source, float amount) {
        if (entity.isActiveItemStackBlocking() && amount > 0.0 && entity.canBlockDamageSource(source)) {
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
