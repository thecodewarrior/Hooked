package com.thecodewarrior.hooks.util;

import net.minecraft.client.renderer.Tessellator;

public class HookClientUtil
{
	public static void renderFace(
			double x1, double y1, double z1, double u1, double v1,
			double x2, double y2, double z2, double u2, double v2,
			double x3, double y3, double z3, double u3, double v3,
			double x4, double y4, double z4, double u4, double v4) {
		Tessellator tess = Tessellator.instance;
		tess.addVertexWithUV(x1, y1, z1, u1, v1);
		tess.addVertexWithUV(x2, y2, z2, u2, v2);
		tess.addVertexWithUV(x3, y3, z3, u3, v3);
		tess.addVertexWithUV(x4, y4, z4, u4, v4);
		
		tess.addVertexWithUV(x4, y4, z4, u4, v4);
		tess.addVertexWithUV(x3, y3, z3, u3, v3);
		tess.addVertexWithUV(x2, y2, z2, u2, v2);
		tess.addVertexWithUV(x1, y1, z1, u1, v1);
		
	}
}
