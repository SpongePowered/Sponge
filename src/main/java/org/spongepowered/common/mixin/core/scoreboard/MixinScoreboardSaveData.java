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
package org.spongepowered.common.mixin.core.scoreboard;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.world.WorldSavedData;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Texts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.interfaces.IMixinScore;
import org.spongepowered.common.interfaces.IMixinScoreObjective;
import org.spongepowered.common.interfaces.IMixinScoreboard;
import org.spongepowered.common.interfaces.IMixinScoreboardSaveData;
import org.spongepowered.common.scoreboard.SpongeScore;
import org.spongepowered.common.scoreboard.SpongeScoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mixin(ScoreboardSaveData.class)
public abstract class MixinScoreboardSaveData extends WorldSavedData implements IMixinScoreboardSaveData {

    private Scoreboard spongeScoreboard;

    private static final String SPONGE_SCORE_UUID_LEAST = "SpongeScoreUUIDLeast";
    private static final String SPONGE_SCORE_UUID_MOST = "SpongeScoreUUIDMost";

    public Map<UUID, SpongeScore> scoreMap = new HashMap<UUID, SpongeScore>();

    private net.minecraft.scoreboard.Score lastScore = null;

    public MixinScoreboardSaveData(String name) {
        super(name);
        this.scoreMap = new HashMap<UUID, SpongeScore>();
    }

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    public void onInit(String name, CallbackInfo ci) {
        this.scoreMap = Maps.newHashMap();
    }

    @Override
    public void setSpongeScoreboard(Scoreboard scoreboard) {
        this.spongeScoreboard = scoreboard;
    }

    @Inject(method = "setScoreboard", at = @At("HEAD"))
    public void onSetScoreboard(net.minecraft.scoreboard.Scoreboard scoreboard, CallbackInfo ci) {
        ((IMixinScoreboard) scoreboard).setSpongeScoreboard((SpongeScoreboard) this.spongeScoreboard);
        ((SpongeScoreboard) this.spongeScoreboard).getScoreboards().add(scoreboard);
    }

    @Inject(method = "readScores", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getValueFromObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)Lnet/minecraft/scoreboard/Score;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onAfterSetLocked(NBTTagList nbt, CallbackInfo ci, int i, NBTTagCompound nbttagcompound, ScoreObjective objective) {
        if (nbttagcompound.hasKey(SPONGE_SCORE_UUID_LEAST)) {
            UUID uuid = new UUID(nbttagcompound.getLong(SPONGE_SCORE_UUID_MOST), nbttagcompound.getLong(SPONGE_SCORE_UUID_LEAST));
            SpongeScore spongeScore = this.scoreMap.get(uuid);
            if (spongeScore == null){
                spongeScore = new SpongeScore(Texts.fromLegacy(nbttagcompound.getString("Name")));
                this.scoreMap.put(uuid, spongeScore);
            }
            ((IMixinScoreObjective) objective).getSpongeObjective().addScore(spongeScore);
            this.lastScore = spongeScore.getScore(objective);
        }
    }

    @Redirect(method = "readScores", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getValueFromObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)Lnet/minecraft/scoreboard/Score;"))
    public net.minecraft.scoreboard.Score onGetValueFromObjective(net.minecraft.scoreboard.Scoreboard scoreboard, String name, ScoreObjective objective) {
        if (this.lastScore != null) {
            net.minecraft.scoreboard.Score score = this.lastScore;
            this.lastScore = null;
            return score;
        }
        return scoreboard.getValueFromObjective(name, objective);
    }

    @Inject(method = "scoresToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Score;isLocked()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onScoresToNbt(CallbackInfoReturnable<NBTTagList> cir, NBTTagList nbttaglist, Collection collection, Iterator iterator, net.minecraft.scoreboard.Score score, NBTTagCompound nbttagcompound) {
        SpongeScore spongeScore = ((IMixinScore) score).getSpongeScore();
        if (spongeScore.getObjectives().size() > 1) {
            nbttagcompound.setLong(SPONGE_SCORE_UUID_MOST, spongeScore.getUUID().getMostSignificantBits());
            nbttagcompound.setLong(SPONGE_SCORE_UUID_LEAST, spongeScore.getUUID().getLeastSignificantBits());
        }
    }

}
