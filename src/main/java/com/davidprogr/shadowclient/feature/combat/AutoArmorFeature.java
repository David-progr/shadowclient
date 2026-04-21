package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoArmor — Shadow only.
 * Uses DataComponentTypes.EQUIPPABLE to identify armor slot,
 * and ATTRIBUTE_MODIFIERS to compare armor defense values.
 */
public class AutoArmorFeature extends Feature {

    public final SliderSetting delay = addSetting(new SliderSetting("Delay","Ticks between checks",20,5,60,1));

    private int ticker = 0;

    public AutoArmorFeature() {
        super("AutoArmor","Equips the best available armor", Category.COMBAT, false);
    }

    @Override
    public void onTick() {
        if (++ticker < delay.getValue().intValue()) return;
        ticker = 0;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.currentScreenHandler == null) return;

        // Armor slots: FEET=0, LEGS=1, CHEST=2, HEAD=3
        EquipmentSlot[] armorSlots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

        for (int slotIdx = 0; slotIdx < armorSlots.length; slotIdx++) {
            EquipmentSlot equipSlot = armorSlots[slotIdx];
            ItemStack worn    = mc.player.getEquippedStack(equipSlot);
            int       bestSlot = -1;
            double    bestProt = getDefense(worn, equipSlot);

            for (int i = 0; i < 36; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (s.isEmpty()) continue;

                // Check if this item is equippable in the target slot
                EquippableComponent equippable = s.get(DataComponentTypes.EQUIPPABLE);
                if (equippable == null) continue;
                if (equippable.slot() != equipSlot) continue;

                double prot = getDefense(s, equipSlot);
                if (prot > bestProt) { bestProt = prot; bestSlot = i; }
            }

            if (bestSlot != -1) {
                int networkSlot = bestSlot < 9 ? bestSlot + 36 : bestSlot;
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId, networkSlot, 0,
                        SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }

    /**
     * Computes the armor defense value for a given slot using attribute modifiers.
     */
    private double getDefense(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return 0;
        AttributeModifiersComponent attrs = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attrs == null) return 0;
        double[] total = {0};
        attrs.applyModifiers(slot, (attr, modifier) -> {
            if (attr.matches(EntityAttributes.ARMOR)) {
                total[0] += modifier.value();
            }
        });
        return total[0];
    }
}
