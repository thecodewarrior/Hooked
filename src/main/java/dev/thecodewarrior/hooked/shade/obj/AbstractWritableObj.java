/*
 * www.javagl.de - Obj
 *
 * Copyright (c) 2008-2015 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.thecodewarrior.hooked.shade.obj;

import dev.thecodewarrior.hooked.shade.obj.FloatTuple;
import dev.thecodewarrior.hooked.shade.obj.FloatTuples;
import dev.thecodewarrior.hooked.shade.obj.ObjFace;
import dev.thecodewarrior.hooked.shade.obj.ObjFaces;
import dev.thecodewarrior.hooked.shade.obj.WritableObj;

import java.util.Collection;
import java.util.Objects;

/**
 * Abstract base implementation of a {@link dev.thecodewarrior.hooked.shade.obj.WritableObj}. <br>
 * <br>
 * The final implementations of the methods in this class all delegate
 * to the following (non-final) methods, which may be overridden by
 * implementors:
 * <ul>
 *    <li>{@link #addVertex(dev.thecodewarrior.hooked.shade.obj.FloatTuple)}</li>
 *    <li>{@link #addTexCoord(dev.thecodewarrior.hooked.shade.obj.FloatTuple)}</li>
 *    <li>{@link #addFace(dev.thecodewarrior.hooked.shade.obj.ObjFace)}</li>
 *    <li>{@link #setActiveGroupNames(Collection)}</li>
 *    <li>{@link #setActiveMaterialGroupName(String)}</li>
 *    <li>{@link #setMtlFileNames(Collection)}</li>
 * </ul>
 */
public class AbstractWritableObj implements WritableObj
{
    /**
     * Default constructor
     */
    protected AbstractWritableObj()
    {
        // Default constructor
    }
    
    @Override
    public final void addVertex(float x, float y, float z)
    {
        addVertex(dev.thecodewarrior.hooked.shade.obj.FloatTuples.create(x, y, z));
    }
    
    @Override
    public void addVertex(dev.thecodewarrior.hooked.shade.obj.FloatTuple vertex)
    {
        // Empty default implementation
    }
    
    @Override
    public final void addTexCoord(float x)
    {
        addTexCoord(dev.thecodewarrior.hooked.shade.obj.FloatTuples.create(x));
    }
    
    @Override
    public final void addTexCoord(float x, float y)
    {
        addTexCoord(dev.thecodewarrior.hooked.shade.obj.FloatTuples.create(x, y));
    }
    
    @Override
    public final void addTexCoord(float x, float y, float z)
    {
        addTexCoord(dev.thecodewarrior.hooked.shade.obj.FloatTuples.create(x, y, z));
    }
    
    @Override
    public void addTexCoord(dev.thecodewarrior.hooked.shade.obj.FloatTuple texCoord)
    {
        // Empty default implementation
    }
    

    @Override
    public void addNormal(FloatTuple normal)
    {
        // Empty default implementation
    }

    @Override
    public final void addNormal(float x, float y, float z)
    {
        addNormal(FloatTuples.create(x, y, z));
    }
    
    @Override
    public void setActiveGroupNames(
        Collection<? extends String> groupNames)
    {
        // Empty default implementation
    }
    
    
    @Override
    public void setActiveMaterialGroupName(String materialGroupName)
    {
        // Empty default implementation
    }
    
    @Override
    public void addFace(ObjFace face)
    {
        // Empty default implementation
    }
    
    @Override
    public final void addFace(int ... v)
    {
        addFace(v, null, null);
    }

    @Override
    public final void addFaceWithTexCoords(int... v)
    {
        addFace(v, v, null);
    }

    @Override
    public final void addFaceWithNormals(int... v)
    {
        addFace(v, null, v);
    }

    @Override
    public final void addFaceWithAll(int... v)
    {
        addFace(v, v, v);
    }
    
    @Override
    public final void addFace(int[] v, int[] vt, int[] vn)
    {
        Objects.requireNonNull(v, "The vertex indices are null");
        addFace(ObjFaces.create(v, vt, vn));
    }

    @Override
    public void setMtlFileNames(Collection<? extends String> mtlFileNames)
    {
        // Empty default implementation
    }

    
    
}
