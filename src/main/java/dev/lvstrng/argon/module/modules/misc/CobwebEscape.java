package dev.lvstrng.argon.module.modules.misc;

import dev.lvstrng.argon.event.events.TickListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.NumberSetting;
import dev.lvstrng.argon.utils.EncryptedString;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public final class CobwebEscape extends Module implements TickListener {
    private final NumberSetting speed = new NumberSetting(EncryptedString.of("Speed"), 2, 100, 20, 1);

    public CobwebEscape() {
        super(EncryptedString.of("Cobweb Escape"),
                EncryptedString.of("vaso femboy."),
                -1,
                Category.MISC);
        addSettings(speed);
    }

    @Override
    public void onEnable() {
        eventManager.add(TickListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(TickListener.class, this);
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        if (mc.player.getInventory().getArmorStack(2).getItem() != Items.ELYTRA) return;

        if (!mc.player.isFallFlying()) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else {
                mc.player.startFallFlying();
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        if (mc.player.isFallFlying()) {
            int rocketSlot = findFireworkSlot();
            if (rocketSlot == -1) return;

            if (mc.player.getInventory().selectedSlot != rocketSlot && rocketSlot != 40) {
                mc.player.getInventory().selectedSlot = rocketSlot;
            }

            if (mc.player.age % speed.getValueInt() == 0) {
                if (isHoldingFireworkInHand(Hand.MAIN_HAND)) {
                    sendFireworkPacket(Hand.MAIN_HAND);
                } else if (isHoldingFireworkInHand(Hand.OFF_HAND)) {
                    sendFireworkPacket(Hand.OFF_HAND);
                }
            }
        }
    }

    private int findFireworkSlot() {
        if (mc.player.getOffHandStack().getItem() == Items.FIREWORK_ROCKET) {
            return 40;
        }

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }

        return -1;
    }

    private boolean isHoldingFireworkInHand(Hand hand) {
        return mc.player.getStackInHand(hand).getItem() == Items.FIREWORK_ROCKET;
    }

    private void sendFireworkPacket(Hand hand) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        PlayerInteractItemC2SPacket packet = new PlayerInteractItemC2SPacket(hand, 0, yaw, pitch);
        mc.getNetworkHandler().sendPacket(packet);
        mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
    }
}
