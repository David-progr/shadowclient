package com.davidprogr.shadowclient.feature.misc;

import com.davidprogr.shadowclient.feature.Feature;
import com.davidprogr.shadowclient.feature.setting.BooleanSetting;
import com.davidprogr.shadowclient.feature.setting.SliderSetting;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.HoeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoFarmFeature extends Feature {
    private final SliderSetting range = new SliderSetting("Range", "Harvest radius in blocks", 4, 1, 6, 1);
    private final BooleanSetting replant = new BooleanSetting("Replant", "Automatically replant crops", true);
    private int delay = 0;

    public AutoFarmFeature() {
        super("AutoFarm", "Automatically harvest and replant crops", Category.MISC, true);
        addSetting(range);
        addSetting(replant);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (delay-- > 0) return;
        delay = 3;

        double r = range.getValue();
        BlockPos playerPos = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(
            playerPos.add(-(int)r, -1, -(int)r),
            playerPos.add((int)r, 1, (int)r)
        )) {
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > r * r) continue;
            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();

            // Harvest mature crops
            if (isMatureCrop(block, state)) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                mc.interactionManager.attackBlock(pos, Direction.UP);

                // Replant
                if (replant.getValue()) {
                    delay = 5;
                    return;
                }
                delay = 2;
                return;
            }
        }
    }

    private boolean isMatureCrop(Block block, BlockState state) {
        if (block instanceof CropBlock crop) {
            return crop.isMature(state);
        }
        if (block instanceof SweetBerryBushBlock) {
            int age = state.get(SweetBerryBushBlock.AGE);
            return age == 3;
        }
        if (block instanceof CocoaBlock) {
            int age = state.get(CocoaBlock.AGE);
            return age == 2;
        }
        return false;
    }
}
