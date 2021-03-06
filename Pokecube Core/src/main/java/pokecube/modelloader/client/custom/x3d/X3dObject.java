package pokecube.modelloader.client.custom.x3d;

import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.IExtendedModelPart;
import pokecube.modelloader.client.custom.IPartTexturer;
import pokecube.modelloader.client.custom.IRetexturableModel;
import thut.api.maths.Vector3;

public class X3dObject implements IExtendedModelPart, IRetexturableModel
{
    private int                meshId    = 0;
    public int                 GLMODE    = GL11.GL_TRIANGLES;
    public boolean             triangles = true;
    public Vertex[]            vertices;
    public Vertex[]            normals;
    public TextureCoordinate[] textureCoordinates;
    public Integer[]           order;

    public HashMap<String, IExtendedModelPart> childParts = new HashMap<String, IExtendedModelPart>();
    public final String                        name;
    public IExtendedModelPart                  parent     = null;
    IPartTexturer                              texturer;

    public Vector4 preRot    = new Vector4();
    public Vector4 postRot   = new Vector4();
    public Vector4 postRot1  = new Vector4();
    public Vector3 preTrans  = Vector3.getNewVectorFromPool();
    public Vector3 postTrans = Vector3.getNewVectorFromPool();

    public Vector3   offset    = Vector3.getNewVectorFromPool();
    public Vector4   rotations = new Vector4();
    public Vertex    scale     = new Vertex(1, 1, 1);
    private double[] uvShift   = { 0, 0 };

    public X3dObject(String name)
    {
        this.name = name;
    }

    public int red        = 255, green = 255, blue = 255, alpha = 255;
    public int brightness = 15728640;

    @Override
    public void addChild(IExtendedModelPart subPart)
    {
        this.childParts.put(subPart.getName(), subPart);
        subPart.setParent(this);
    }

    @Override
    public void setParent(IExtendedModelPart parent)
    {
        this.parent = parent;
    }

    @Override
    public void setPreTranslations(Vector3 point)
    {
        preTrans.set(point);
    }

    @Override
    public void setPostRotations(Vector4 angles)
    {
        postRot = angles;
    }

    @Override
    public void setPostTranslations(Vector3 point)
    {
        postTrans.set(point);
    }

    @Override
    public void setPreRotations(Vector4 angles)
    {
        preRot = angles;
    }

    public void render()
    {
        // Rotate to the offset of the parent.
        rotateToParent();
        // Translate of offset for rotation.
        GL11.glTranslated(offset.x, offset.y, offset.z);
        // Rotate by this to account for a coordinate difference.
        GL11.glRotatef(90, 1, 0, 0);
        GL11.glTranslated(preTrans.x, preTrans.y, preTrans.z);
        // UnRotate coordinate difference.
        GL11.glRotatef(-90, 1, 0, 0);
        // Apply initial part rotation
        rotations.glRotate();
        // Rotate by this to account for a coordinate difference.
        GL11.glRotatef(90, 1, 0, 0);
        // Apply PreOffset-Rotations.
        preRot.glRotate();
        // Translate by post-PreOffset amount.
        GL11.glTranslated(postTrans.x, postTrans.y, postTrans.z);
        // UnRotate coordinate difference.
        GL11.glRotatef(-90, 1, 0, 0);
        // Undo pre-translate offset.
        GL11.glTranslated(-offset.x, -offset.y, -offset.z);
        GL11.glPushMatrix();
        // Translate to Offset.
        GL11.glTranslated(offset.x, offset.y, offset.z);

        // Apply first postRotation
        postRot.glRotate();
        // Apply second post rotation.
        postRot1.glRotate();
        // Scale
        GL11.glScalef(scale.x, scale.y, scale.z);
        // Renders the model.
        addForRender();
        GL11.glPopMatrix();
    }

    public void addForRender()
    {
        boolean textureShift = false;
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.applyTexture(this.getName());
            if (textureShift = texturer.shiftUVs(name, uvShift))
            {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glTranslated(uvShift[0], uvShift[1], 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
        }
        // Applies Colour.
        GL11.glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
        // Compiles the list of the meshId is invalid.
        compileList();
        // Call the list
        GL11.glCallList(meshId);
        GL11.glFlush();
        // Reset Texture Matrix if changed.
        if (textureShift)
        {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }
    }

    @Override
    public String getType()
    {
        return "x3d";
    }

    @Override
    public void renderAll()
    {
        render();
        for (IExtendedModelPart o : childParts.values())
        {
            GL11.glPushMatrix();
            GL11.glTranslated(offset.x, offset.y, offset.z);
            o.renderAll();
            GL11.glPopMatrix();
        }
    }

    @Override
    public void renderOnly(String... groupNames)
    {
        for (IExtendedModelPart o : childParts.values())
        {
            for (String s : groupNames)
            {
                if (s.equalsIgnoreCase(o.getName()))
                {
                    o.renderOnly(groupNames);
                }
            }
        }
        for (String s : groupNames)
        {
            if (s.equalsIgnoreCase(name)) render();
        }
    }

    @Override
    public void renderPart(String partName)
    {
        if (this.name.equalsIgnoreCase(partName)) render();
        if (childParts.containsKey(partName)) childParts.get(partName).renderPart(partName);
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames)
    {
        for (String s : childParts.keySet())
        {
            for (String s1 : excludedGroupNames)
                if (!s.equalsIgnoreCase(s1))
                {
                    childParts.get(s).renderAllExcept(excludedGroupNames);
                }
        }
        for (String s1 : excludedGroupNames)
            if (s1.equalsIgnoreCase(name)) render();
    }

    @Override
    public int[] getRGBAB()
    {
        return new int[] { red, green, blue, alpha, brightness };
    }

    @Override
    public void setRGBAB(int[] array)
    {
        red = array[0];
        blue = array[1];
        green = array[2];
        alpha = array[3];
        brightness = array[4];
    }

    @Override
    public HashMap<String, IExtendedModelPart> getSubParts()
    {
        return childParts;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Vector3 getDefaultTranslations()
    {
        return offset;
    }

    @Override
    public Vector4 getDefaultRotations()
    {
        return rotations;
    }

    @Override
    public IExtendedModelPart getParent()
    {
        return parent;
    }

    @Override
    public void setPostRotations2(Vector4 rotations)
    {
        postRot1 = rotations;
    }

    private void compileList()
    {
        if (!GL11.glIsList(meshId))
        {
            meshId = GL11.glGenLists(1);
            GL11.glNewList(meshId, GL11.GL_COMPILE);
            Vertex vertex;
            TextureCoordinate textureCoordinate;
            if (triangles)
            {
                Vector3f[] normalList = new Vector3f[order.length];
                // Calculate the normals for each triangle.
                for (int i = 0; i < order.length; i += 3)
                {
                    Vector3f v1, v2, v3;
                    vertex = vertices[order[i]];
                    v1 = new Vector3f(vertex.x, vertex.y, vertex.z);
                    vertex = vertices[order[i + 1]];
                    v2 = new Vector3f(vertex.x, vertex.y, vertex.z);
                    vertex = vertices[order[i + 2]];
                    v3 = new Vector3f(vertex.x, vertex.y, vertex.z);
                    Vector3f a = new Vector3f(v2);
                    a.sub(v1);
                    Vector3f b = new Vector3f(v3);
                    b.sub(v1);
                    Vector3f c = new Vector3f();
                    c.cross(a, b);
                    c.normalize();
                    normalList[i] = c;
                    normalList[i+1] = c;
                    normalList[i+2] = c;
                }
                //TODO see if there is a better way to interpolate the normals.
                GL11.glBegin(GLMODE);
                int n = 0;
                for (Integer i : order)
                {
                    textureCoordinate = textureCoordinates[i];
                    GL11.glTexCoord2d(textureCoordinate.u, textureCoordinate.v);
                    vertex = vertices[i];
                    GL11.glVertex3f(vertex.x, vertex.y, vertex.z);
                    if(n%3==0)
                    {
                        Vector3f norm = normalList[n];
                        GL11.glNormal3f(norm.x, norm.y, norm.z);
                    }
                    n++;
                }
                GL11.glEnd();
            }
            else
            {
                List<Integer> modes = Lists.newArrayList();
                int num = 0, n = 0, n1 = 0;

                List<Vector3f> normalList = Lists.newArrayList();
                Vector3f[] norms = new Vector3f[order.length];
                Vector3f c = null;
                for (Integer i : order)
                {
                    if (i == -1)
                    {
                        if (num == 3)
                        {
                            modes.add(GL11.GL_TRIANGLES);
                        }
                        else if (num == 4)
                        {
                            modes.add(GL11.GL_QUADS);
                        }
                        else
                        {
                            modes.add(GL11.GL_TRIANGLE_FAN);
                        }
                        num = 0;
                    }
                    else
                    { 
                        // Calculate a face normal, using just the first 3
                        // points, Every face has at least them
                        if (num == 0)
                        {
                            Vector3f v1, v2, v3;
                            vertex = vertices[order[n]];
                            v1 = new Vector3f(vertex.x, vertex.y, vertex.z);
                            vertex = vertices[order[n + 1]];
                            v2 = new Vector3f(vertex.x, vertex.y, vertex.z);
                            vertex = vertices[order[n + 2]];
                            v3 = new Vector3f(vertex.x, vertex.y, vertex.z);
                            Vector3f a = new Vector3f(v2);
                            a.sub(v1);
                            Vector3f b = new Vector3f(v3);
                            b.sub(v1);
                            c = new Vector3f();
                            c.cross(a, b);
                            c.normalize();
                            normalList.add(c);
                        }
                        norms[n1] = c;
                        n1++;
                        num++;
                    }
                    n++;
                }
                num = n = 0;
                GL11.glBegin(modes.get(num++));
                for (Integer i : order)
                {
                    if (i != -1)
                    {
                        vertex = vertices[i];
                        textureCoordinate = textureCoordinates[i];
                        GL11.glTexCoord2d(textureCoordinate.u, textureCoordinate.v);
                        GL11.glVertex3f(vertex.x, vertex.y, vertex.z);      
                        
                        Vector3f norm1 = norms[n];
                        GL11.glNormal3f(norm1.x, norm1.y, norm1.z);
                        n++;
                    }
                    else
                    {
                        GL11.glEnd();
                        if (num < modes.size())
                        {
                            GL11.glBegin(modes.get(num++));
                        }
                    }
                }
            }
            GL11.glEndList();
        }
    }

    private void rotateToParent()
    {
        if (parent != null)
        {
            if (parent instanceof X3dObject)
            {
                X3dObject parent = ((X3dObject) this.parent);
                parent.postRot.glRotate();
                parent.postRot1.glRotate();
            }
        }
    }

    @Override
    public void resetToInit()
    {
        preRot.set(0, 1, 0, 0);
        postRot.set(0, 1, 0, 0);
        postRot1.set(0, 1, 0, 0);
        preTrans.clear();
        postTrans.clear();
    }

    @Override
    public void setTexturer(IPartTexturer texturer)
    {
        this.texturer = texturer;
        for (IExtendedModelPart part : childParts.values())
        {
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
        }
    }
}
