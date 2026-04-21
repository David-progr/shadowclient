package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.util.Hand;

/** Legit-allowed. 1.21.4 potion check via DataComponentTypes.POTION_CONTENTS. */
public class AutoPotFeature extends Feature {

    public final SliderSetting  hpThreshold  = addSetting(new SliderSetting("HP Threshold","Pot when HP below",14,1,20,1));
    public final SliderSetting  delay        = addSetting(new SliderSetting("Delay","Ticks between pots",10,5,40,1));
    public final BooleanSetting instantOnly  = addSetting(new BooleanSetting("Instant Only","Only instant-health pots",true));

    private int cooldown = 0;

    public AutoPotFeature() {
        super("AutoPot","Auto-throws healing potions at low HP", Category.COMBAT, true);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (mc.player.getHealth() >= (float)(double) hpThreshold.getValue()) return;

        int potSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof SplashPotionItem)) continue;

            // Check potion contents via DataComponent (1.21.4 API)
            PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents == null) continue;

            if (instantOnly.getValue()) {
                // Compare using RegistryEntry.matches() — StatusEffects.INSTANT_HEALTH is RegistryEntry
                boolean hasInstantHealth = false;
                for (var effect : contents.getEffects()) {
                    if (effect.getEffectType().matches(StatusEffects.INSTANT_HEALTH)) {
                        hasInstantHealth = true;
                        break;
                    }
                }
                if (!hasInstantHealth) continue;
            }
            potSlot = i;
            break;
        }
        if (potSlot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = potSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
        cooldown = delay.getValue().intValue();
    }
}
