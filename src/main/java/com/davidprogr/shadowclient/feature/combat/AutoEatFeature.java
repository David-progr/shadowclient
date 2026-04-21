package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/** Legit-allowed. Uses 1.21.4 DataComponent food API. */
public class AutoEatFeature extends Feature {

    public final SliderSetting  threshold = addSetting(new SliderSetting("Threshold","Eat when hunger below",16,1,19,1));
    public final BooleanSetting bestFood  = addSetting(new BooleanSetting("Best Food","Pick highest nutrition",true));

    public AutoEatFeature() {
        super("AutoEat","Automatically eats food when hungry", Category.COMBAT, true);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHungerManager().getFoodLevel() >= threshold.getValue().intValue()) return;

        int bestSlot = -1, bestNut = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            // 1.21.4: food is a DataComponent, not a method on Item
            FoodComponent food = stack.get(DataComponentTypes.FOOD);
            if (food == null) continue;
            if (!bestFood.getValue() || food.nutrition() > bestNut) {
                bestNut  = food.nutrition();
                bestSlot = i;
                if (!bestFood.getValue()) break;
            }
        }
        if (bestSlot == -1) return;
        mc.player.getInventory().selectedSlot = bestSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
}
