package com.thecodewarrior.hooks.render.model.chain;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.thecodewarrior.hooks.HookMod;
import com.thecodewarrior.hooks.render.model.IChainModel;
import com.thecodewarrior.hooks.util.ActiveHook;
import com.thecodewarrior.hooks.util.HookClientUtil;
import com.thecodewarrior.hooks.util.HookUtil;

public class SpineChainModel implements IChainModel
{
	
	ResourceLocation tex;
	
	@Override
	public void constructTexturesFor(String hookName)
	{
		// TODO Auto-generated method stub
		tex = new ResourceLocation(HookMod.MODID + ":textures/hooks/" + hookName + "/chain.png");
	}
	
	@Override
	public void processConfig(Map<String, String> data)
	{
		overlap = HookUtil.parseWithDefault(overlap, data.get("chain.overlap"));
		texHeight = HookUtil.parseWithDefault(texHeight, data.get("chain.tex.height"));
	}
	int texHeight = 16;
	int overlap = 0;
	double sideLen = Math.sqrt(0.5);

	@Override
	public void draw(ActiveHook hook, double length)
	{
		double w = 48;
		
		GL11.glPushMatrix();
//		GL11.glTranslated(0, 2, 0);
		double scale = 0.25;
		GL11.glScaled(scale, scale, scale);
		Minecraft mc = Minecraft.getMinecraft();
		Tessellator t = Tessellator.instance;
		mc.renderEngine.bindTexture(tex);
		
		double l = 2;
		double px = l/(float)texHeight;
		GL11.glPushMatrix();
		for(double d = 0; d+l*scale < length; d += (l-overlap*px)*scale) {
			
			t.startDrawingQuads();

			HookClientUtil.renderFace(
					sideLen, 	0, 		   0, 		16/w, 0,
					0, 		0, 		   sideLen, 	24/w, 0,
					0, 		l, sideLen, 	24/w, 1,
					sideLen, 	l, 0, 		16/w, 1);
			
			HookClientUtil.renderFace(
					sideLen, 	0, 		   	0, 	   16/w,  0,
					0, 		0, 		   	-sideLen, 8/w,  0,
					0, 		l, 	-sideLen, 8/w,  1,
					sideLen, 	l, 	0, 	   16/w,  1);
			
			HookClientUtil.renderFace(
					-sideLen, 0, 		   	0, 		32/w, 0,
					0, 		0, 		   	sideLen, 	24/w, 0,
					0, 		l, 	sideLen, 	24/w, 1,
					-sideLen, l, 	0, 		32/w, 1);
			
			HookClientUtil.renderFace(
					-sideLen, 0, 		   	0, 	    0,   0,
					0, 		0, 		   	-sideLen, 8/w, 0,
					0, 		l, 	-sideLen, 8/w, 1,
					-sideLen, l, 	0, 	    0,   1);
			HookClientUtil.renderFace(
					sideLen+0.5, 0,       0, 32/w, 0,
					sideLen+0.5, l, 0, 32/w, 1,
					sideLen-0.5, l, 0, 48/w, 1,
					sideLen-0.5, 0,       0, 48/w, 0
					);
			
			t.draw();
			
			GL11.glTranslated(0, l-overlap*px, 0);
		}
		GL11.glPopMatrix();
		
		GL11.glPopMatrix();
	}
	
}
