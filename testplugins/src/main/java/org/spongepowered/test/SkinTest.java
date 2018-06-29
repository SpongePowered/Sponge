package org.spongepowered.test;

import com.google.common.util.concurrent.Futures;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "skin-test", name = "Skin Test", description = "Use '/skin' to change your skin")
public class SkinTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Change the skin of a player"))
                .arguments(GenericArguments.playerOrSource(Text.of("target")), GenericArguments.string(Text.of("skinPlayer")), GenericArguments.bool(Text.of("updateGameProfile")))
                .executor((src, args) -> {
                    Player target = args.<Player>getOne(Text.of("target")).get();
                    String skinPlayer = args.<String>getOne(Text.of("skinPlayer")).get();
                    boolean updateGameProfile = args.<Boolean>getOne(Text.of("updateGameProfile")).get();

                    GameProfile skinProfile = Futures.getUnchecked(Sponge.getServer().getGameProfileManager().get(skinPlayer));
                    skinProfile = Futures.getUnchecked(Sponge.getServer().getGameProfileManager().fill(skinProfile, true));


                    ProfileProperty skinTexture = skinProfile.getPropertyMap().get(ProfileProperty.TEXTURES).iterator().next();

                    SkinData data = target.get(SkinData.class).get();
                    data.set(Keys.SKIN, skinTexture);
                    data.set(Keys.UPDATE_GAME_PROFILE, updateGameProfile);

                    target.offer(data);

                    target.sendMessage(Text.of(TextColors.GREEN, "Changed skin!"));
                    return CommandResult.success();
                }).build(), "skin");
    }

}
