package dev.lvstrng.argon.event.events;

import dev.lvstrng.argon.event.CancellableEvent;
import dev.lvstrng.argon.event.Listener;
import lombok.Getter;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;

public interface Render3DListener extends Listener {
    void onRender3D(Render3DEvent event);

    @Getter
    class Render3DEvent extends CancellableEvent<Render3DListener> implements Listener {
        MatrixStack matrices;
        Frustum frustum;
        RenderTickCounter renderTickCounter;
        Camera camera;

        public Render3DEvent(MatrixStack matrix4f, Frustum frustum, Camera camera, RenderTickCounter renderTickCounter) {
            matrices = matrix4f;
            this.renderTickCounter = renderTickCounter;
            this.frustum = frustum;
            this.camera = camera;
        }

        @Override
        public void fire(ArrayList<Render3DListener> listeners) {
            listeners.forEach(e -> e.onRender3D(this));
        }

        @Override
        public Class<Render3DListener> getListenerType() {
            return Render3DListener.class;
        }
    }
}
