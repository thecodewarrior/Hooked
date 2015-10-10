package com.thecodewarrior.hooks.util;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import codechicken.lib.vec.Vector3;

public class HookUtil
{
	public static Vector3 attemptBoundingBoxMove(AxisAlignedBB aabb, World world, Vector3 offset, Entity e)
	{
        double d3 = ( aabb.maxX + aabb.minX )/2;
        double d4 = aabb.minY;
        double d5 = ( aabb.maxZ + aabb.minZ )/2;

        double vx = offset.x, vy = offset.y, vz = offset.z;
        
        double origVX = vx;
        double origVY = vy;
        double origVZ = vz;
        AxisAlignedBB origAABB = aabb.copy();

        List list = world.getCollidingBoundingBoxes(e, aabb.addCoord(vx, vy, vz));

        for (int i = 0; i < list.size(); ++i)
        {
            vy = ((AxisAlignedBB)list.get(i)).calculateYOffset(aabb, vy);
        }

        aabb.offset(0.0D, vy, 0.0D);

        if (origVY != vy)
        {
            vz = 0.0D;
            vy = 0.0D;
            vx = 0.0D;
        }

        boolean flag1 = origVY != vy && origVY < 0.0D;
        int j;

        for (j = 0; j < list.size(); ++j)
        {
            vx = ((AxisAlignedBB)list.get(j)).calculateXOffset(aabb, vx);
        }

        aabb.offset(vx, 0.0D, 0.0D);

        if (origVX != vx)
        {
            vz = 0.0D;
            vy = 0.0D;
            vx = 0.0D;
        }

        for (j = 0; j < list.size(); ++j)
        {
            vz = ((AxisAlignedBB)list.get(j)).calculateZOffset(aabb, vz);
        }

        aabb.offset(0.0D, 0.0D, vz);

        if (origVZ != vz)
        {
            vz = 0.0D;
            vy = 0.0D;
            vx = 0.0D;
        }
        
        double distanceX = ( (aabb.minX + aabb.maxX) / 2.0D ) - (( origAABB.minX + origAABB.maxX ) / 2.0D);
        double distanceY = ( (aabb.minY + aabb.maxY) / 2.0D ) - (( origAABB.minY + origAABB.maxY ) / 2.0D);
        double distanceZ = ( (aabb.minZ + aabb.maxZ) / 2.0D ) - (( origAABB.minZ + origAABB.maxZ ) / 2.0D);
        
        return new Vector3(distanceX, distanceY, distanceZ);
        
	}
}
