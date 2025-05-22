package dev.lvstrng.argon.module.modules.combat;

import dev.lvstrng.argon.Argon;
import dev.lvstrng.argon.event.events.TickListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.modules.misc.CobwebEscape;
import dev.lvstrng.argon.utils.EncryptedString;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class SelfTrap extends Module implements TickListener {
    private float originalYaw, originalPitch;
    private int originalSlot = -1;

    public SelfTrap() {
        super(EncryptedString.of("Self Trap"),
                EncryptedString.of("Automatically traps you in cobwebs"),
                -1,
                Category.COMBAT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) return;

        originalYaw = mc.player.getYaw();
        originalPitch = mc.player.getPitch();
        originalSlot = mc.player.getInventory().selectedSlot;

        eventManager.add(TickListener.class, this);
    }

    @Override
    public void onDisable() {
        eventManager.remove(TickListener.class, this);

        if (mc.player != null) {
            mc.player.setYaw(originalYaw);
            mc.player.setPitch(originalPitch);
            if (originalSlot != -1) {
                mc.player.getInventory().selectedSlot = originalSlot;
             }
        }

        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null || mc.interactionManager == null)
            return;

        if (Argon.INSTANCE.getModuleManager().getModule(CobwebEscape.class).isEnabled()) {
            toggle();
            return;
        }

        mc.player.setPitch(90f);

        BlockPos pos = mc.player.getBlockPos();
        boolean isLookingDown = mc.player.getPitch() >= 89.5f;
        boolean noCobweb = !mc.world.getBlockState(pos).isOf(Blocks.COBWEB);
        boolean replaceable = mc.world.getBlockState(pos).isReplaceable();

        if (isLookingDown && noCobweb && replaceable) {
            int cobwebSlot = findCobwebInHotbar();
            if (cobwebSlot != -1) {
                mc.player.getInventory().selectedSlot = cobwebSlot;

                BlockPos clickedPos = pos.down();
                Direction clickSide = Direction.UP;

                Vec3d hitVec = Vec3d.ofCenter(clickedPos).add(0, 0.5, 0);

                BlockHitResult hit = new BlockHitResult(hitVec, clickSide, clickedPos, false);

                ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                if (result.isAccepted()) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }

        if (!mc.player.isFallFlying()) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else {
                mc.player.startFallFlying();
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }
    }

    private int findCobwebInHotbar() {
        for (int i = 0; i < 9; i++) {
            assert mc.player != null;
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.COBWEB) {
                return i;
            }
        }
        return -1;
    }
}
