package com.thecodewarrior.hooks.render.model.hook;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.render.model.IHookModel;
import com.thecodewarrior.hooks.util.ActiveHook;

public class SkullHookModel extends ModelBase implements IHookModel
{
	private ResourceLocation tex;
	
	@Override
	public void constructTexturesFor(String hookName)
	{
		tex = new ResourceLocation(HookMod.MODID + ":textures/hooks/" + hookName + "/hook.png");
	}
	
	private int textureWidth = 64;
	private int textureHeight = 32;
	private int distance = 7;
	private double scale = 0.07;
	
	
	private ModelRenderer headTop;
	private ModelRenderer headBottom;
	
	@Override
	public void processConfig(Map<String, String> data)
	{
        this.headTop = new ModelRenderer(this, textureWidth, textureHeight);
        this.headTop.addBox(-4.0F, -6.0F, -8.0F, 8, 6, 8, 0.0F);
        this.headTop.setRotationPoint(0.0F, 0.0F, 0.0F);
        
        this.headBottom = new ModelRenderer(this, textureWidth, textureHeight);
        this.headBottom.setTextureOffset(0, 14);
        this.headBottom.addBox(-4.0F, 0.0F, -8.0F, 8, 2, 8, 0.0F);
        this.headBottom.setRotationPoint(0.0F, 0.0F, 0.0F);
	}
	
	@Override
	public void draw(ActiveHook hook)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		mc.renderEngine.bindTexture(tex);
		distance = 5;
		GL11.glPushMatrix();
		GL11.glScaled(scale, scale, scale);
		GL11.glTranslated(0, distance, 0);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glRotated(-90, 1, 0, 0);
//        model.skeletonHead.render(1);
		GL11.glRotated(-22.5, 1, 0, 0);
		headTop.render(1);
		GL11.glRotated(45, 1, 0, 0);
		headBottom.render(1);
		GL11.glPopMatrix();
	}
	
	@Override
	public double getLoopCenterPointDistance(ActiveHook hook)
	{
		// TODO Auto-generated method stub
		return distance * scale;
	}
	
}
