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
package org.spongepowered.common.entity.living.human;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerListItem.AddPlayerData;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseAccessor;
import org.spongepowered.common.mixin.core.entity.player.EntityPlayerAccessor;
import org.spongepowered.common.mixin.core.network.play.server.SPacketPlayerListItemAccessor;
import org.spongepowered.common.mixin.core.network.play.server.SPacketSpawnPlayerAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/*
 * Notes
 *
 * The label above the player's head is always visible unless the human is in
 * a team with invisible labels set to true. Could we leverage this at all?
 *
 * Hostile mobs don't attack the human, should this be default behaviour?
 */
public class EntityHuman extends EntityCreature implements TeamMember, IRangedAttackMob {

    // According to http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape
    // you can access this data once per minute, lets cache for 2 minutes
    private static final LoadingCache<UUID, PropertyMap> PROPERTIES_CACHE = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, PropertyMap>() {

                @Override
                public PropertyMap load(final UUID uuid) throws Exception {
                    return SpongeImpl.getServer().func_147130_as().fillProfileProperties(new GameProfile(uuid, ""), true)
                            .getProperties();
                }
            });

    // A queue of packets waiting to send to players tracking this human
    private final Map<UUID, List<Packet<?>[]>> playerPacketMap = Maps.newHashMap();

    private GameProfile fakeProfile;
    @Nullable private UUID skinUuid;
    private boolean aiDisabled = false, leftHanded = false;

    public EntityHuman(final World worldIn) {
        super(worldIn);
        this.fakeProfile = new GameProfile(this.field_96093_i, "");
        this.func_70105_a(0.6F, 1.8F);
        this.func_98053_h(true);
    }

    @Override
    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e).func_111128_a(1.0D);
    }

    @Override
    protected void func_70088_a() {
        // EntityLivingBase
        this.field_70180_af.func_187214_a(EntityLivingBaseAccessor.accessor$getHandStatesParameter(), Byte.valueOf((byte)0));
        this.field_70180_af.func_187214_a(EntityLivingBaseAccessor.accessor$getPotionEffectsParameter(), Integer.valueOf(0));
        this.field_70180_af.func_187214_a(EntityLivingBaseAccessor.accessor$getHideParticlesParameter(), Boolean.valueOf(false));
        this.field_70180_af.func_187214_a(EntityLivingBaseAccessor.accessor$getArrowCountInEntityParameter(), Integer.valueOf(0));
        this.field_70180_af.func_187214_a(EntityLivingBaseAccessor.accessor$getHealthParameter(), Float.valueOf(1.0F));
        // EntityPlayer
        this.field_70180_af.func_187214_a(EntityPlayerAccessor.accessor$getAbsorptionParameter(), Float.valueOf(0.0F));
        this.field_70180_af.func_187214_a(EntityPlayerAccessor.accessor$getPlayerScoreParameter(), Integer.valueOf(0));
        this.field_70180_af.func_187214_a(EntityPlayerAccessor.accessor$getPlayerModelFlagParameter(), Byte.valueOf((byte)0));
        this.field_70180_af.func_187214_a(EntityPlayerAccessor.accessor$getMainHandParameter(), Byte.valueOf((byte)1));
    }

    @Override
    public boolean func_184638_cS() {
        return this.leftHanded;
    }

    @Override
    public boolean func_175446_cd() {
        return this.aiDisabled;
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.fakeProfile.getName());
    }

    @Override
    public Team func_96124_cp() {
        return this.field_70170_p.func_96441_U().func_96509_i(this.fakeProfile.getName());
    }

    @Override
    public void func_96094_a(String name) {
        if (name.length() > 16) {
            // Vanilla restriction
            name = name.substring(0, 16);
        }
        if (this.func_95999_t().equals(name)) {
            return;
        }
        super.func_96094_a(name);
        this.renameProfile(name);
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
    }

    @Override
    public void func_70037_a(final NBTTagCompound tagCompund) {
        super.func_70037_a(tagCompund);
        final String skinUuidString = ((DataCompoundHolder) this).data$getSpongeCompound().func_74779_i("skinUuid");
        if (!skinUuidString.isEmpty()) {
            this.updateFakeProfileWithSkin(UUID.fromString(skinUuidString));
        }
    }

    @Override
    public void func_70014_b(final NBTTagCompound tagCompound) {
        super.func_70014_b(tagCompound);
        final NBTTagCompound spongeData = ((DataCompoundHolder) this).data$getSpongeCompound();
        if (this.skinUuid != null) {
            spongeData.func_74778_a("skinUuid", this.skinUuid.toString());
        } else {
            spongeData.func_82580_o("skinUuid");
        }
    }

    @Override
    public void func_70636_d() {
        super.func_70636_d();
        this.func_82168_bl();
    }

    @Override
    public void func_94061_f(final boolean disable) {
        this.aiDisabled = disable;
    }

    @Override
    public void func_184641_n(final boolean leftHanded) {
        this.leftHanded = leftHanded;
    }

    @Override
    public int func_82145_z() {
        return 80;
    }

    @Override
    protected SoundEvent func_184184_Z() {
        return SoundEvents.field_187808_ef;
    }

    @Override
    protected SoundEvent func_184181_aa() {
        return SoundEvents.field_187806_ee;
    }

    @Override
    public int func_82147_ab() {
        return 10;
    }

    @Override
    public void func_70645_a(@Nullable final DamageSource cause) {
        super.func_70645_a(cause);
        this.func_70105_a(0.2F, 0.2F);
        this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
        this.field_70181_x = 0.1D;
        if (cause != null) {
            this.field_70159_w = -MathHelper.func_76134_b((this.field_70739_aP + this.field_70177_z) * (float) Math.PI / 180.0F) * 0.1F;
            this.field_70179_y = -MathHelper.func_76126_a((this.field_70739_aP + this.field_70177_z) * (float) Math.PI / 180.0F) * 0.1F;
        } else {
            this.field_70159_w = this.field_70179_y = 0.0D;
        }
    }

    @Override
    protected SoundEvent func_184601_bQ(final DamageSource source) {
        return SoundEvents.field_187800_eb;
    }

    @Override
    protected SoundEvent func_184615_bR() {
        return SoundEvents.field_187798_ea;
    }

    @Override
    public double func_70033_W() {
        return -0.35D;
    }

    @Override
    public float func_70689_ay() {
        return (float) this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111126_e();
    }

    @Override
    protected SoundEvent func_184588_d(final int height) {
        return height > 4 ? SoundEvents.field_187736_dY : SoundEvents.field_187804_ed;
    }
    @Override
    public float func_70047_e() {
        return 1.62f;
    }

    @Override
    public float func_110139_bj() {
        return this.func_184212_Q().func_187225_a(EntityPlayerAccessor.accessor$getAbsorptionParameter());
    }

    @Override
    public void func_110149_m(float amount) {
        if (amount < 0.0F) {
            amount = 0.0F;
        }
        this.func_184212_Q().func_187227_b(EntityPlayerAccessor.accessor$getAbsorptionParameter(), amount);
    }

    @Override
    protected float func_110146_f(final float p_110146_1_, final float p_110146_2_) {
        final float retValue = super.func_110146_f(p_110146_1_, p_110146_2_);
        // Make the body rotation follow head rotation
        this.field_70177_z = this.field_70759_as;
        return retValue;
    }

    @Override
    public boolean func_70652_k(final Entity entityIn) {
        super.func_70652_k(entityIn);
        this.func_184609_a(EnumHand.MAIN_HAND);
        float f = (float) this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111126_e();
        int i = 0;

        if (entityIn instanceof EntityLivingBase) {
            f += EnchantmentHelper.func_152377_a(this.func_184586_b(EnumHand.MAIN_HAND), ((EntityLivingBase) entityIn).func_70668_bt());
            i += EnchantmentHelper.func_77501_a(this);
        }

        final boolean flag = entityIn.func_70097_a(DamageSource.func_76358_a(this), f);

        if (flag) {
            if (i > 0) {
                entityIn.func_70024_g(-MathHelper.func_76126_a(this.field_70177_z * (float) Math.PI / 180.0F) * i * 0.5F, 0.1D,
                        MathHelper.func_76134_b(this.field_70177_z * (float) Math.PI / 180.0F) * i * 0.5F);
                this.field_70159_w *= 0.6D;
                this.field_70179_y *= 0.6D;
            }

            final int j = EnchantmentHelper.func_90036_a(this);

            if (j > 0) {
                entityIn.func_70015_d(j * 4);
            }

            this.func_174815_a(this, entityIn);
        }

        return flag;
    }

    private void renameProfile(final String newName) {
        final PropertyMap props = this.fakeProfile.getProperties();
        this.fakeProfile = new GameProfile(this.fakeProfile.getId(), newName);
        this.fakeProfile.getProperties().putAll(props);
    }

    private boolean updateFakeProfileWithSkin(final UUID skin) {
        final PropertyMap properties = PROPERTIES_CACHE.getUnchecked(skin);
        if (properties.isEmpty()) {
            return false;
        }
        this.fakeProfile.getProperties().replaceValues("textures", properties.get("textures"));
        this.skinUuid = skin;
        return true;
    }

    public void removeFromTabListDelayed(@Nullable final EntityPlayerMP player, final SPacketPlayerListItem removePacket) {
        final int delay = SpongeImpl.getGlobalConfigAdapter().getConfig().getEntity().getHumanPlayerListRemoveDelay();
        final Runnable removeTask = () -> this.pushPackets(player, removePacket);
        if (delay == 0) {
            removeTask.run();
        } else {
            SpongeImpl.getGame().getScheduler().createTaskBuilder()
                    .execute(removeTask)
                    .delayTicks(delay)
                    .submit(SpongeImpl.getPlugin());
        }
    }

    public boolean setSkinUuid(final UUID skin) {
        if (!SpongeImpl.getServer().func_71266_T()) {
            // Skins only work when online-mode = true
            return false;
        }
        if (skin.equals(this.skinUuid)) {
            return true;
        }
        if (!updateFakeProfileWithSkin(skin)) {
            return false;
        }
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
        return true;
    }

    @Nullable
    public UUID getSkinUuid() {
        return this.skinUuid;
    }

    public DataTransactionResult removeSkin() {
        if (this.skinUuid == null) {
            return DataTransactionResult.successNoData();
        }
        this.fakeProfile.getProperties().removeAll("textures");
        final ImmutableValue<?> oldValue = new ImmutableSpongeValue<>(Keys.SKIN_UNIQUE_ID, this.skinUuid);
        this.skinUuid = null;
        if (this.isAliveAndInWorld()) {
            this.respawnOnClient();
        }
        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).replace(oldValue).build();
    }

    private boolean isAliveAndInWorld() {
        return this.field_70170_p.func_73045_a(this.func_145782_y()) == this && !this.field_70128_L;
    }

    private void respawnOnClient() {
        this.pushPackets(new SPacketDestroyEntities(this.func_145782_y()), this.createPlayerListPacket(SPacketPlayerListItem.Action.ADD_PLAYER));
        this.pushPackets(this.createSpawnPacket());
        removeFromTabListDelayed(null, this.createPlayerListPacket(SPacketPlayerListItem.Action.REMOVE_PLAYER));
    }

    /**
     * Can the fake profile be removed from the tab list immediately (i.e. as
     * soon as the human has spawned).
     *
     * @return Whether it can be removed with 0 ticks delay
     */
    public boolean canRemoveFromListImmediately() {
        return !this.fakeProfile.getProperties().containsKey("textures");
    }

    /**
     * Called when a player stops tracking this human.
     *
     * Removes the player from the packet queue and sends them a REMOVE_PLAYER
     * tab list packet to make sure the human is not on it.
     *
     * @param player The player that has stopped tracking this human
     */
    public void onRemovedFrom(final EntityPlayerMP player) {
        this.playerPacketMap.remove(player.func_110124_au());
        player.field_71135_a.func_147359_a(this.createPlayerListPacket(SPacketPlayerListItem.Action.REMOVE_PLAYER));
    }

    /**
     * Creates a {@link SPacketSpawnPlayer} packet.
     *
     * Copied directly from the constructor of the packet, because that can't be
     * used as we're not an EntityPlayer.
     *
     * @return A new spawn packet
     */
    @SuppressWarnings("ConstantConditions")
    public SPacketSpawnPlayer createSpawnPacket() {
        final SPacketSpawnPlayer packet = new SPacketSpawnPlayer();
        final SPacketSpawnPlayerAccessor accessor = (SPacketSpawnPlayerAccessor) packet;
        accessor.accessor$setentityId(this.func_145782_y());
        accessor.accessor$setuniqueId(this.fakeProfile.getId());
        accessor.accessor$setx(this.field_70165_t);
        accessor.accessor$sety(this.field_70163_u);
        accessor.accessor$setZ(this.field_70161_v);
        accessor.accessor$setYaw((byte) ((int) (this.field_70177_z * 256.0F / 360.0F)));
        accessor.accessor$setPitch((byte) ((int) (this.field_70125_A * 256.0F / 360.0F)));
        accessor.accessor$setWatcher(this.func_184212_Q());
        return packet;
    }

    /**
     * Creates a {@link SPacketPlayerListItem} packet with the given action.
     *
     * @param action The action to apply on the tab list
     * @return A new tab list packet
     */
    @SuppressWarnings("ConstantConditions")
    public SPacketPlayerListItem createPlayerListPacket(final SPacketPlayerListItem.Action action) {
        final SPacketPlayerListItem packet = new SPacketPlayerListItem(action);
        ((SPacketPlayerListItemAccessor) packet).accessor$getPlayerDatas()
            .add(packet.new AddPlayerData(this.fakeProfile, 0, GameType.NOT_SET, this.func_145748_c_()));
        return packet;
    }

    /**
     * Push the given packets to all players tracking this human.
     *
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(final Packet<?>... packets) {
        this.pushPackets(null, packets); // null = all players
    }

    /**
     * Push the given packets to the given player (who must be tracking this
     * human).
     *
     * @param player The player tracking this human
     * @param packets All packets to send in a single tick
     */
    public void pushPackets(@Nullable final EntityPlayerMP player, final Packet<?>... packets) {
        if (player == null) {
            List<Packet<?>[]> queue = this.playerPacketMap.get(null);
            if (queue == null) {
                queue = new ArrayList<>();
                this.playerPacketMap.put(null, queue);
            }
            queue.add(packets);
        } else {
            List<Packet<?>[]> queue = this.playerPacketMap.get(player.func_110124_au());
            if (queue == null) {
                queue = new ArrayList<>();
                this.playerPacketMap.put(player.func_110124_au(), queue);
            }
            queue.add(packets);
        }
    }

    /**
     * (Internal) Pops the packets off the queue for the given player.
     *
     * @param player The player to get packets for (or null for all players)
     * @return An array of packets to send in a single tick
     */
    public Packet<?>[] popQueuedPackets(@Nullable final EntityPlayerMP player) {
        final List<Packet<?>[]> queue = this.playerPacketMap.get(player == null ? null : player.func_110124_au());
        return queue == null || queue.isEmpty() ? null : queue.remove(0);
    }

    @Override
    public void func_82196_d(final EntityLivingBase target, final float distanceFactor) {
        // Borrowed from Skeleton
        // TODO Figure out how to API this out
        final EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.field_70170_p, this);
        final double d0 = target.field_70165_t - this.field_70165_t;
        final double d1 = target.func_174813_aQ().field_72338_b + target.field_70131_O / 3.0F - entitytippedarrow.field_70163_u;
        final double d2 = target.field_70161_v - this.field_70161_v;
        final double d3 = MathHelper.func_76133_a(d0 * d0 + d2 * d2);
        entitytippedarrow.func_70186_c(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, 14 - this.field_70170_p.func_175659_aa().func_151525_a() * 4);
        // These names are wrong
        final int i = EnchantmentHelper.func_185284_a(Enchantments.field_185310_v, this);
        final int j = EnchantmentHelper.func_185284_a(Enchantments.field_185311_w, this);
        entitytippedarrow.func_70239_b(distanceFactor * 2.0F + this.field_70146_Z.nextGaussian() * 0.25D + this.field_70170_p.func_175659_aa().func_151525_a() * 0.11F);

        if (i > 0) {
            entitytippedarrow.func_70239_b(entitytippedarrow.func_70242_d() + i * 0.5D + 0.5D);
        }

        if (j > 0) {
            entitytippedarrow.func_70240_a(j);
        }

        final ItemStack itemstack = this.func_184586_b(EnumHand.OFF_HAND);

        if (itemstack.func_77973_b() == Items.field_185167_i) {
            entitytippedarrow.func_184555_a(itemstack);
        }

        this.func_184185_a(SoundEvents.field_187737_v, 1.0F, 1.0F / (this.func_70681_au().nextFloat() * 0.4F + 0.8F));
        this.field_70170_p.func_72838_d(entitytippedarrow);
    }

    @Override
    public void func_184724_a(final boolean var1) {
        // TODO 1.12-pre2 Can we support this
    }
}
