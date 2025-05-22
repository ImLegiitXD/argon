package dev.lvstrng.argon.module.modules.player;

import dev.lvstrng.argon.event.events.PacketSendListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.module.setting.BooleanSetting;
import dev.lvstrng.argon.utils.EncryptedString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.*;
import net.minecraft.network.packet.c2s.play.*;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public final class Blink extends Module implements PacketSendListener {
    private final Queue<Packet<?>> packets = new LinkedBlockingQueue<>();
    private boolean disableLogger = false;

    private final BooleanSetting c0fValue = new BooleanSetting(EncryptedString.of("CancelTransactions"), true)
            .setDescription(EncryptedString.of("pico"));
    private final BooleanSetting c00Value = new BooleanSetting(EncryptedString.of("CancelKeepAlive"), true)
            .setDescription(EncryptedString.of("pito"));

    public Blink() {
        super(EncryptedString.of("Blink"),
                EncryptedString.of("Stop your packets and sends it when you disable the module."),
                -1,
                Category.PLAYER);
        addSettings(c0fValue, c00Value);
    }

    @Override
    public void onEnable() {
        eventManager.add(PacketSendListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(PacketSendListener.class, this);

        if (mc.player == null) return;

        blink();
        super.onDisable();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.packet;

        if (mc.player == null || disableLogger) return;

        if (packet instanceof PlayerMoveC2SPacket)
            event.cancel();
        if (packet instanceof PositionAndOnGround ||
                packet instanceof Full ||
                packet instanceof PlayerInteractBlockC2SPacket ||
                packet instanceof HandSwingC2SPacket ||
                packet instanceof ClientCommandC2SPacket ||
                packet instanceof PlayerActionC2SPacket ||
                packet instanceof PlayerInteractEntityC2SPacket ||
                (c0fValue.getValue() && packet instanceof CommonPongC2SPacket)) {

            event.cancel();
            packets.add(packet);
        }
    }

    private void blink() {
        if (mc.getNetworkHandler() == null) return;
        try {
            disableLogger = true;
            while (!packets.isEmpty()) {
                mc.getNetworkHandler().sendPacket(packets.poll());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disableLogger = false;
        }
    }
}
