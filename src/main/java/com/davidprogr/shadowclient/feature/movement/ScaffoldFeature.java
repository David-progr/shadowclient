package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Shadow-only. Places blocks below player while walking. */
public class ScaffoldFeature extends Feature {
    public final ModeSetting    mode     = addSetting(new ModeSetting("Mode","Scaffold style",new String[]{"Normal","Tower","Safe"},"Normal"));
    public final BooleanSetting rotate   = addSetting(new BooleanSetting("Rotate","Look at placement spot",true));
    public final BooleanSetting sprint   = addSetting(new BooleanSetting("Sprint","Auto-sprint while active",true));
    public final SliderSetting  delay    = addSetting(new SliderSetting("Delay","Ticks between places",1,0,5,1));

    private int delayTimer = 0;

    public ScaffoldFeature() { super("Scaffold","Automatically bridges under your feet", Category.MOVEMENT, false); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (delayTimer-- > 0) return;
        delayTimer = delay.getValue().intValue();

        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        // Find a block item in hotbar
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack st = mc.player.getInventory().getStack(i);
            if (!st.isEmpty() && st.getItem() instanceof BlockItem bi) {
                Block b = bi.getBlock();
                if (b != Blocks.AIR) { slot = i; break; }
            }
        }
        if (slot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        // Place on top face of block below-below
        BlockPos target = below.down();
        if (mc.world.getBlockState(target).isAir()) return;

        if (rotate.getValue()) {
            mc.player.setPitch(80f);
        }

        Vec3d hitVec = Vec3d.ofCenter(target).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, target, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;

        if (sprint.getValue()) mc.player.setSprinting(true);
    }
}
