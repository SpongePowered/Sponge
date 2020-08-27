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
package org.spongepowered.common.mixin.tracker.entity.player;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.network.play.server.SCombatPacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.BasicEntityContext;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Tracker extends PlayerEntityMixin_Tracker implements PlayerEntityBridge {

    // @formatter:off
    @Shadow @Final public MinecraftServer server;
    @Shadow public ServerPlayNetHandler connection;

    @Shadow public abstract void shadow$takeStat(Stat<?> stat);
    @Shadow public abstract ServerWorld shadow$getServerWorld();
    @Shadow public abstract boolean shadow$isSpectator();
    // @formatter:on

    @Nullable
    private Boolean tracker$keepInventory = null;

    /**
     * @author blood - May 12th, 2016
     * @author gabizou - June 3rd, 2016
     * @author gabizou - February 22nd, 2020 - Minecraft 1.14.3
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Override
    @Overwrite
    public void onDeath(final DamageSource cause) {
        // Sponge start
        final boolean isServerThread = PhaseTracker.SERVER.onSidedThread();
        final Optional<DestructEntityEvent.Death> optEvent = SpongeCommonEventFactory.callDestructEntityEventDeath((ServerPlayerEntity) (Object) this, cause, isServerThread);
        if (optEvent.map(Cancellable::isCancelled).orElse(true)) {
            return;
        }
        final DestructEntityEvent.Death event = optEvent.get();

        // Double check that the PhaseTracker is already capturing the Death phase
        final boolean tracksEntityDeaths;
        if (isServerThread) {
            tracksEntityDeaths = PhaseTracker.getInstance().getCurrentState().tracksEntityDeaths();
        } else {
            tracksEntityDeaths = false;
        }
        try (final PhaseContext<?> context = this.tracker$createDeathContext(cause, tracksEntityDeaths)) {
            if (context != null) {
                context.buildAndSwitch();
            }
            // Sponge end
            final boolean flag = this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
            if (flag) {
                ITextComponent itextcomponent = this.shadow$getCombatTracker().getDeathMessage();
                this.connection.sendPacket(new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, itextcomponent), (p_212356_2_) -> {
                    if (!p_212356_2_.isSuccess()) {
                        int i = 256;
                        String s = itextcomponent.getStringTruncated(256);
                        ITextComponent itextcomponent1 = new TranslationTextComponent("death.attack.message_too_long", (new StringTextComponent(s)).applyTextStyle(TextFormatting.YELLOW));
                        ITextComponent itextcomponent2 = (new TranslationTextComponent("death.attack.even_more_magic", this.shadow$getDisplayName())).applyTextStyle((p_212357_1_) -> {
                            p_212357_1_.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));
                        });
                        this.connection.sendPacket(new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, itextcomponent2));
                    }

                });
                final Team team = this.getTeam();
                if (team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS) {
                    if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS) {
                        this.server.getPlayerList().sendMessageToAllTeamMembers((ServerPlayerEntity) (Object) this, itextcomponent);
                    } else if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM) {
                        this.server.getPlayerList().sendMessageToTeamOrAllPlayers((ServerPlayerEntity) (Object) this, itextcomponent);
                    }
                } else {
                    this.server.getPlayerList().sendMessage(itextcomponent);
                }
            } else {
                this.connection.sendPacket(new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED));
            }

            this.shadow$spawnShoulderEntities();

            // Ignore keepInventory GameRule instead use keepInventory from Event
            if (!event.getKeepInventory() && !this.shadow$isSpectator()) {
                this.shadow$destroyVanishingCursedItems();
                this.inventory.dropAllItems();
            }
            // Sponge Stop

            this.shadow$getWorldScoreboard().forAllObjectives(ScoreCriteria.DEATH_COUNT, this.shadow$getScoreboardName(), Score::incrementScore);
            final LivingEntity livingentity = this.shadow$getAttackingEntity();
            if (livingentity != null) {
                this.shadow$addStat(Stats.ENTITY_KILLED_BY.get(livingentity.getType()));
                livingentity.awardKillScore((ServerPlayerEntity) (Object) this, this.scoreValue, cause);
                if (!this.world.isRemote && livingentity instanceof WitherEntity) {
                    boolean flag1 = false;
                    if (this.world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)) {
                        BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
                        BlockState blockstate = Blocks.WITHER_ROSE.getDefaultState();
                        if (this.world.getBlockState(blockpos).isAir() && blockstate.isValidPosition(this.world, blockpos)) {
                            this.world.setBlockState(blockpos, blockstate, 3);
                            flag1 = true;
                        }
                    }

                    if (!flag1) {
                        ItemEntity itementity = new ItemEntity(this.world, this.posX, this.posY, this.posZ, new ItemStack(Items.WITHER_ROSE));
                        this.world.addEntity(itementity);
                    }
                }
            }

            this.shadow$addStat(Stats.DEATHS);
            this.shadow$takeStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
            this.shadow$takeStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            this.shadow$extinguish();
            this.shadow$setFlag(0, false);
            this.shadow$getCombatTracker().reset();

            this.tracker$keepInventory = event.getKeepInventory();
        } // Sponge - brackets
    }

    @Nullable
    private PhaseContext<?> tracker$createDeathContext(final DamageSource cause, final boolean tracksEntityDeaths) {
        return !tracksEntityDeaths
                ? EntityPhase.State.DEATH.createPhaseContext(PhaseTracker.SERVER)
                .source(this)
                .setDamageSource((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) cause)
                : null;
    }


    @Redirect(method = "copyFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
    private boolean tracker$useKeepFromBridge(final GameRules gameRules, final GameRules.RuleKey<?> key,
            final ServerPlayerEntity corpse, final boolean keepEverything) {
        final boolean keep = ((PlayerEntityBridge) corpse).bridge$keepInventory(); // Override Keep Inventory GameRule?
        if (!keep) {
            // Copy corpse inventory to respawned player
            this.inventory.copyInventory(corpse.inventory);
            // Clear corpse so that mods do not copy from it again
            corpse.inventory.clear();
        }
        return keep;
    }


    @Override
    public boolean bridge$keepInventory() {
        if (this.tracker$keepInventory == null) {
            return this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
        }
        return this.tracker$keepInventory;
    }

    @Override
    protected int tracker$modifyExperiencePointsOnDeath(LivingEntity entity, PlayerEntity attackingPlayer) {
        if (this.tracker$keepInventory != null && this.tracker$keepInventory) {
            return 0;
        }
        return super.tracker$modifyExperiencePointsOnDeath(entity, attackingPlayer);
    }
}
