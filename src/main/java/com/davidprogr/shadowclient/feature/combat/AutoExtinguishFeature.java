package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

/** Legit-allowed. Uses water bucket to put out fire. */
public class AutoExtinguishFeature extends Feature {

    public AutoExtinguishFeature() {
        super("AutoExtinguish", "Automatically puts out fire using water bucket", Category.COMBAT, true);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;
        if (!mc.player.isOnFire()) return;

        // Find water bucket in hotbar
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                BlockPos pos = mc.player.getBlockPos();
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
                mc.player.getInventory().selectedSlot = prev;
                return;
            }
        }
    }
}
