package com.davidprogr.shadowclient.feature.utility;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;
import com.davidprogr.shadowclient.feature.setting.KeybindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Legit-allowed. Sorts inventory by item type on keybind press. */
public class InventorySorterFeature extends Feature {
    public final BooleanSetting hotbarSort = addSetting(new BooleanSetting("Hotbar","Include hotbar slots",false));

    public InventorySorterFeature() { super("InvSorter","Sorts your inventory by category", Category.UTILITY, true); }

    public void sort() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        PlayerInventory inv = mc.player.getInventory();
        int start = hotbarSort.getValue() ? 0 : 9;
        int end   = 36;

        List<ItemStack> items = new ArrayList<>();
        for (int i = start; i < end; i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty()) items.add(s.copy());
            else               items.add(ItemStack.EMPTY);
        }

        items.sort((a, b) -> {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            return a.getItem().toString().compareTo(b.getItem().toString());
        });

        for (int i = 0; i < items.size(); i++) {
            inv.setStack(start + i, items.get(i));
        }
    }
}
