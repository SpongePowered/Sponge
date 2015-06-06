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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.entity.CombatHelper.getNewTracker;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.util.FoodStats;
import org.apache.commons.lang3.LocaleUtils;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DisplayNameData;
import org.spongepowered.api.data.manipulator.entity.GameModeData;
import org.spongepowered.api.data.manipulator.entity.JoinData;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Titles;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.Subjectable;
import org.spongepowered.common.interfaces.text.IMixinTitle;
import org.spongepowered.common.scoreboard.SpongeScoreboard;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.SpongeChatType;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends MixinEntityPlayer implements Player, CommandSource, Subjectable, IMixinEntityPlayerMP {

    public int newExperience = 0;
    public int newLevel = 0;
    public int newTotalExperience = 0;
    public boolean keepsLevel = false;

    @Shadow private String translator;
    @Shadow public NetHandlerPlayServer playerNetServerHandler;
    @Shadow public int lastExperience;
    private MessageSink sink = Sponge.getGame().getServer().getBroadcastSink();

    private org.spongepowered.api.scoreboard.Scoreboard spongeScoreboard = ((World) this.worldObj).getScoreboard();

    private net.minecraft.scoreboard.Scoreboard mcScoreboard = this.worldObj.getScoreboard();

    @Inject(method = "func_152339_d", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onRemoveEntity(Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            ((EntityHuman) entityIn).onRemovedFrom((EntityPlayerMP) (Object) this);
        }
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getObjectivesFromCriteria(Lnet/minecraft/scoreboard/IScoreObjectiveCriteria;)Ljava/util/Collection;"))
    public Collection onGetObjectivesFromCriteria(net.minecraft.scoreboard.Scoreboard this$0, IScoreObjectiveCriteria criteria) {
        return this.getWorldScoreboard().getObjectivesFromCriteria(criteria);
    }

    @Override
    public GameProfile getProfile() {
        return (GameProfile) getGameProfile();
    }

    @Override
    public String getName() {
        return getGameProfile().getName();
    }

    @Override
    public boolean isOnline() {
        // TODO This should actually check if the player is online.
        // A plugin may hold a reference to a player who has since disconnected
        return true;
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.of((Player) this);
    }

    public Text getDisplayNameApi() {
        return SpongeTexts.toText(getDisplayName());
    }

    @Override
    public Locale getLocale() {
        return LocaleUtils.toLocale(this.translator);
    }

    @Override
    public void sendMessage(Text... messages) {
        sendMessage(ChatTypes.CHAT, messages);
    }

    @Override
    public void sendMessage(Iterable<Text> messages) {
        sendMessage(ChatTypes.CHAT, messages);
    }

    @Override
    public void sendMessage(ChatType type, Text... messages) {
        for (Text text : messages) {
            if (type == ChatTypes.ACTION_BAR) {
                text = SpongeTexts.fixActionBarFormatting(text);
            }

            this.playerNetServerHandler.sendPacket(new S02PacketChat(SpongeTexts.toComponent(text, getLocale()),
                    ((SpongeChatType) type).getByteId()));
        }
    }

    @Override
    public void sendMessage(ChatType type, Iterable<Text> messages) {
        for (Text text : messages) {
            if (type == ChatTypes.ACTION_BAR) {
                text = SpongeTexts.fixActionBarFormatting(text);
            }

            this.playerNetServerHandler.sendPacket(new S02PacketChat(SpongeTexts.toComponent(text, getLocale()),
                    ((SpongeChatType) type).getByteId()));
        }
    }

    @Override
    public void setMessageSink(MessageSink sink) {
        Preconditions.checkNotNull(sink, "sink");
        this.sink = sink;
    }

    @Override
    public MessageSink getMessageSink() {
        return this.sink;
    }

    @Override
    public void sendTitle(Title title) {
        ((IMixinTitle) title).send((EntityPlayerMP) (Object) this);
    }

    @Override
    public void resetTitle() {
        sendTitle(Titles.RESET);
    }

    @Override
    public void clearTitle() {
        sendTitle(Titles.CLEAR);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            double dx = this.posX - position.getX();
            double dy = this.posY - position.getY();
            double dz = this.posZ - position.getZ();

            if (dx * dx + dy * dy + dz * dz < radius * radius) {
                for (Packet packet : packets) {
                    this.playerNetServerHandler.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public PlayerConnection getConnection() {
        return (PlayerConnection) this.playerNetServerHandler;
    }

    public void setBedLocation(@Nullable Location location) {
        this.spawnChunk = location != null ? VecHelper.toBlockPos(location.getPosition()) : null;
    }

    // this needs to be overridden from EntityPlayer so we can force a resend of the experience level
    @Override
    public void setLevel(int level) {
        super.experienceLevel = level;
        this.lastExperience = -1;
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public String getCustomName() {
        return this.getGameProfile().getName();
    }

    @Override
    public void setCustomName(String name) {
        throw new UnsupportedOperationException("Cannot set the custom name of a player");
    }

    @Override
    public boolean isCustomNameVisible() {
        return true;
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        if (!visible) {
            throw new UnsupportedOperationException("Cannot hide the name of a player");
        }
    }

    @Override
    public String getIdentifier() {
        return getUniqueID().toString();
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.FALSE;
    }

    @Override
    public void reset() {
        float experience = 0;
        boolean keepInventory = this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory");

        if (this.keepsLevel || keepInventory) {
            experience = this.experience;
            this.newTotalExperience = this.experienceTotal;
            this.newLevel = this.experienceLevel;
        }

        this.clearActivePotions();
        this._combatTracker = getNewTracker(this);
        this.deathTime = 0;
        this.experience = 0;
        this.experienceLevel = this.newLevel;
        this.experienceTotal = this.newTotalExperience;
        this.fire = 0;
        this.fallDistance = 0;
        this.foodStats = new FoodStats();
        this.potionsNeedUpdate = true;
        this.openContainer = this.inventoryContainer;
        this.attackingPlayer = null;
        this.entityLivingToAttack = null;
        this.lastExperience = -1;
        this.setHealth(this.getMaxHealth());

        if (this.keepsLevel || keepInventory) {
            this.experience = experience;
        } else {
            this.addExperience(this.newExperience);
        }

        this.keepsLevel = false;
    }

    @Override
    public JoinData getJoinData() {
        return getData(JoinData.class).get();
    }

    @Override
    public DisplayNameData getDisplayNameData() {
        return getData(DisplayNameData.class).get();
    }

    @Override
    public GameModeData getGameModeData() {
        return getData(GameModeData.class).get();
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(of("JoinData"), getJoinData());
    }

    @Override
    public boolean isViewingInventory() {
        return this.openContainer != null;
    }

    @Override
    public void setScoreboard(org.spongepowered.api.scoreboard.Scoreboard scoreboard) {
        if (scoreboard == null) {
            scoreboard = ((World) this.worldObj).getScoreboard();
        }
        ((IMixinServerScoreboard) this.mcScoreboard).removePlayer((EntityPlayerMP) (Object) this);
        this.spongeScoreboard = scoreboard;
        this.mcScoreboard = ((SpongeScoreboard) scoreboard).getPlayerScoreboard();
        ((IMixinServerScoreboard) this.mcScoreboard).addPlayer((EntityPlayerMP) (Object) this);
    }

    @Override
    public net.minecraft.scoreboard.Scoreboard getWorldScoreboard() {
        return this.mcScoreboard;
    }

    public org.spongepowered.api.scoreboard.Scoreboard getScoreboard() {
        return this.spongeScoreboard;
    }
}
