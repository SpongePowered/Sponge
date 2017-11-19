package org.spongepowered.test;

import static org.spongepowered.api.data.DataTransactionResult.DataCategory.REPLACED;
import static org.spongepowered.api.data.DataTransactionResult.DataCategory.SUCCESSFUL;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.data.GetKey;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.Optional;

@Plugin(id = "food-change-test", authors = "Aaron1011")
public class FoodChangeTest {

    @Listener(order = Order.EARLY)
    public void simpleListener(ChangeDataHolderEvent.ValueChange event) {
        Optional<ImmutableValue<?>> oldFood = event.getChanges().get(DataTransactionResult.DataCategory.REPLACED, Keys.FOOD_LEVEL);
        Optional<ImmutableValue<?>> newFood = event.getChanges().get(DataTransactionResult.DataCategory.SUCCESSFUL, Keys.FOOD_LEVEL);

        if (!oldFood.isPresent() || !newFood.isPresent()) {
            return;
        }

        MessageChannel.TO_ALL.send(Text.of(String.format("Simple listener: %s %s", oldFood.get().get(), newFood.get().get())));
    }

    @Listener
    public void getKeyListener(ChangeDataHolderEvent.ValueChange event,
            @GetKey(value = "FOOD_LEVEL", from = REPLACED) int oldFood,
            @GetKey(value = "FOOD_LEVEL", from = SUCCESSFUL) MutableBoundedValue<Integer> newFood,
            @First(tag = "a") Player player,
            @GetKey(value = "DISPLAY_NAME", tag = "a") Text name) {

        MessageChannel.TO_ALL.send(Text.of(String.format("GetKey listener: %s %s from player ", oldFood, newFood.get())).concat(name));

        event.setChanges(DataTransactionResult.builder().from(event.getChanges()).absorbResult(DataTransactionResult.successResult(newFood.asImmutable().with(newFood.getMaxValue()))).build());
    }

}
