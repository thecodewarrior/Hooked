package com.thecodewarrior.hooks.render.model.hook;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPig;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.thecodewarrior.hooks.render.model.IHookModel;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookUtil;

public class PigHookModel extends ModelBase implements IHookModel
{
	double size = .75;
	boolean whole = false;
	ModelRenderer head = new ModelRenderer(this, 64, 32);
	ResourceLocation tex = new ResourceLocation("textures/entity/pig/pig.png");
	@Override
	public void constructTexturesFor(String hookName)
	{
	}

	@Override
	public void processConfig(Map<String, String> data)
	{
		whole = true;//HookUtil.parseWithDefault(0, data.get("hook.whole")) < 0;
		size = (whole ? 1 : 0.07)*HookUtil.parseWithDefault(size, data.get("hook.size"));
		this.head = new ModelRenderer(this, 64, 32);
		this.head.addBox(-4, -4, 0, 8, 8, 8, 0);
        this.head.setRotationPoint(0, 0, 0);
        this.head.setTextureOffset(16, 16).addBox(-2, 0, -1, 4, 3, 1, 0);
		this.head.rotateAngleX = (float)Math.PI;
		pig = new EntityPig(null);
		pigModel = new ModelPig();
	}

	private EntityPig pig;
	private ModelQuadruped pigModel;
	@Override
	public void draw(ActiveHook hook)
	{
		
		
		GL11.glRotated(90, 0, 1, 0);
		GL11.glRotated(90, 1, 0, 0);

		Minecraft mc = Minecraft.getMinecraft();		
		
		GL11.glScaled(size, size, size);
		
		pig.worldObj = mc.theWorld;
		mc.getTextureManager().bindTexture(tex);
		if(whole) {
			GL11.glTranslated(0, -0.5, -0.5);
			RenderManager.instance.renderEntityWithPosYaw(pig, 0, 0, 0, 0, 1);
		} else {
			head.render(1);
		}
	}

	@Override
	public double getLoopCenterPointDistance(ActiveHook hook)
	{
		return whole ? 0.75 : size*8;
	}
	
}
