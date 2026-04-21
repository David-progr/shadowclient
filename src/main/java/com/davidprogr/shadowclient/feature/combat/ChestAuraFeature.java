package com.davidprogr.shadowclient.feature.combat;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Shadow-only. Opens nearby chests and loots them. */
public class ChestAuraFeature extends Feature {

    public final SliderSetting range = addSetting(new SliderSetting("Range","Chest open range",4.0,1.0,6.0,0.5,"b"));
    public final BooleanSetting autoClose = addSetting(new BooleanSetting("Auto Close","Close GUI after looting",true));

    private int cooldown = 0;

    public ChestAuraFeature() {
        super("ChestAura", "Automatically opens and loots nearby chests", Category.COMBAT, false);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (cooldown-- > 0) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.getValue().intValue();

        for (int x = -r; x <= r; x++)
        for (int y = -r; y <= r; y++)
        for (int z = -r; z <= r; z++) {
            BlockPos pos = playerPos.add(x, y, z);
            var state = mc.world.getBlockState(pos);
            if (state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.TRAPPED_CHEST
             || state.getBlock() == Blocks.BARREL || state.getBlock() == Blocks.SHULKER_BOX) {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
                cooldown = 20;
                return;
            }
        }
    }
}
