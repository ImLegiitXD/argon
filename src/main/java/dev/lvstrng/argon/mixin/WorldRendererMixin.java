package dev.lvstrng.argon.mixin;

import dev.lvstrng.argon.Argon;
import dev.lvstrng.argon.event.EventManager;
import dev.lvstrng.argon.event.events.Render3DListener;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.util.math.MatrixStack;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Shadow
	private Frustum frustum;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
	private void onRender(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
		if (Argon.INSTANCE.moduleManager != null) {
			GL11.glEnable(GL11.GL_LINE_SMOOTH);

			RenderSystem.getModelViewStack().pushMatrix().mul(matrix4f);

			MatrixStack matrixStack = new MatrixStack();
			Render3DListener.Render3DEvent renderEvent = new Render3DListener.Render3DEvent(matrixStack, frustum, camera, tickCounter);
			EventManager.fire(renderEvent);

			RenderSystem.getModelViewStack().popMatrix();

			GL11.glDisable(GL11.GL_LINE_SMOOTH);
		}
	}
}