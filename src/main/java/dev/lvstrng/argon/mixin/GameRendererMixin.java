package dev.lvstrng.argon.mixin;

import dev.lvstrng.argon.Argon;
import dev.lvstrng.argon.event.EventManager;
import dev.lvstrng.argon.event.events.GameRenderListener;
import dev.lvstrng.argon.module.modules.misc.Freecam;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

	@Final
	@Shadow private MinecraftClient client;
	@Final
	@Shadow private Camera camera;

	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 1))
	private void onWorldRender(RenderTickCounter tickCounter, CallbackInfo ci) {
		float tickDelta = tickCounter.getTickDelta(true);

		float fov = computeFov(tickDelta);
		Matrix4f projection = createProjectionMatrix(fov);

		MatrixStack matrices = new MatrixStack();
		EventManager.fire(new GameRenderListener.GameRenderEvent(matrices, tickDelta));
	}

	@Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
	private void onShouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
		if (Argon.INSTANCE.getModuleManager().getModule(Freecam.class).isEnabled()) {
			cir.setReturnValue(false);
		}
	}

	@Unique
	private float computeFov(float tickDelta) {
		float fov = 70.0F;

		if (client.options != null) {
			fov = client.options.getFov().getValue().floatValue();

			// simulates fovMultiplier yeh yeh yeh
			fov *= 1.0F;
		}

		Entity entity = camera.getFocusedEntity();
		if (entity instanceof LivingEntity living && living.isDead()) {
			float deathTime = Math.min(living.deathTime + tickDelta, 20.0F);
			fov /= (1.0F - 500.0F / (deathTime + 500.0F)) * 2.0F + 1.0F;
		}

		CameraSubmersionType submersion = camera.getSubmersionType();
		if (submersion == CameraSubmersionType.LAVA || submersion == CameraSubmersionType.WATER) {
			float scale = client.options.getFovEffectScale().getValue().floatValue();
			fov *= MathHelper.lerp(scale, 1.0F, 0.85714287F);
		}

		return fov;
	}

	@Unique
	private Matrix4f createProjectionMatrix(float fovDegrees) {
		float aspect = (float) client.getWindow().getFramebufferWidth() / client.getWindow().getFramebufferHeight();
		float near = 0.05F;
		float far = client.gameRenderer.getViewDistance() * 4.0F;

		return new Matrix4f().perspective(fovDegrees * ((float) Math.PI / 180F), aspect, near, far);
	}
}
