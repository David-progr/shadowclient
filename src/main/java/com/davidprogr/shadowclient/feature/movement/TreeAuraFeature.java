package com.davidprogr.shadowclient.feature.movement;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/** Shadow-only. Automatically chops nearby trees. */
public class TreeAuraFeature extends Feature {

    public final SliderSetting range  = addSetting(new SliderSetting("Range","Chop range",4.5,1.0,6.0,0.5,"b"));
    public final BooleanSetting replant = addSetting(new BooleanSetting("Replant","Replant saplings",false));

    private int cooldown = 0;

    private static final Set<Block> LOGS = Set.of(
        Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
        Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG,
        Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_OAK_LOG
    );

    public TreeAuraFeature() { super("TreeAura","Auto-chops nearby trees", Category.MOVEMENT, false); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (cooldown-- > 0) return;

        BlockPos origin = mc.player.getBlockPos();
        int r = range.getValue().intValue();

        for (int x = -r; x <= r; x++)
        for (int y = 0;  y <= r*2; y++)
        for (int z = -r; z <= r; z++) {
            BlockPos pos = origin.add(x, y, z);
            if (!LOGS.contains(mc.world.getBlockState(pos).getBlock())) continue;
            mc.interactionManager.attackBlock(pos, net.minecraft.util.math.Direction.UP);
            cooldown = 5;
            return;
        }
    }
}
