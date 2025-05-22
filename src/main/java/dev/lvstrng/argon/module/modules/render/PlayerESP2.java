package dev.lvstrng.argon.module.modules.render;

import dev.lvstrng.argon.event.events.Render3DListener;
import dev.lvstrng.argon.module.Category;
import dev.lvstrng.argon.module.Module;
import dev.lvstrng.argon.utils.EncryptedString;
import dev.lvstrng.argon.utils.RenderUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Box;

import java.awt.*;

public final class PlayerESP2 extends Module implements Render3DListener {

    public PlayerESP2() {
        super(EncryptedString.of("Player ESP 2"),
                EncryptedString.of("Renders players through walls"),
                -1,
                Category.RENDER);
    }

    @Override
    public void onEnable() {
        eventManager.add(Render3DListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(Render3DListener.class, this);
        super.onDisable();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (AbstractClientPlayerEntity entity : mc.world.getPlayers()) {
            if (entity != mc.player) {
                Box box = entity.getBoundingBox();
              /*  RenderUtils.renderFilledBox(
                        event.getMatrices(),
                        (float) box.minX,
                        (float) box.minY,
                        (float) box.minZ,
                        (float) box.maxX,
                        (float) box.maxY,
                        (float) box.maxZ,
                        Color.WHITE
                ); */

                RenderUtils.draw3DBox(event.getMatrices(), event.getCamera(), entity.getBoundingBox(),
                        Color.WHITE, 2f);
            }
        }
    }
}