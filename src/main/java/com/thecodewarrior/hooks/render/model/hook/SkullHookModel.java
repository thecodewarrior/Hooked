package com.thecodewarrior.hooks.render.model.hook;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.thecodewarrior.hooks.render.model.IHookModel;
import com.thecodewarrior.hooks.util.ActiveHook;

public class SkullHookModel implements IHookModel
{
	ResourceLocation tex = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
	
	
	@Override
	public void constructTexturesFor(String hookName)
	{
		
	}
	
	@Override
	public void processConfig(Map<String, String> data)
	{
		// TODO Auto-generated method stub
		
	}
	
	private ModelSkeletonHead model = new ModelSkeletonHead(0, 0, 64, 32);
	
	@Override
	public void draw(ActiveHook hook)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		mc.renderEngine.bindTexture(tex);
		GL11.glPushMatrix();
		GL11.glScaled(0.07, 0.07, 0.07);
		GL11.glTranslated(-4, 3, 0);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glRotated(-90, 1, 0, 0);
        model.skeletonHead.render(1);
		GL11.glPopMatrix();
	}
	
	@Override
	public double getLoopCenterPointDistance(ActiveHook hook)
	{
		// TODO Auto-generated method stub
		return .5;
	}
	
}
