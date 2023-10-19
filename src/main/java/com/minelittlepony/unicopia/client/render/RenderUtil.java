package com.minelittlepony.unicopia.client.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public class RenderUtil {
    private static final Vector4f TEMP_VECTOR = new Vector4f();
    public static final Vertex[] UNIT_FACE = new Vertex[] {
            new Vertex(new Vector3f(0, 0, 0), 1, 1),
            new Vertex(new Vector3f(0, 1, 0), 1, 0),
            new Vertex(new Vector3f(1, 1, 0), 0, 0),
            new Vertex(new Vector3f(1, 0, 0), 0, 1)
    };

    public static void renderFace(MatrixStack matrices, Tessellator te, BufferBuilder buffer, float r, float g, float b, float a, int light) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);

        Vertex[] UNIT_FACE = new Vertex[] {
                new Vertex(new Vector3f(0, 0, 0), 1, 1),
                new Vertex(new Vector3f(0, 1, 0), 1, 0),
                new Vertex(new Vector3f(1, 1, 0), 0, 0),
                new Vertex(new Vector3f(1, 0, 0), 0, 1)
        };

        Matrix4f transformation = matrices.peek().getPositionMatrix();
        for (Vertex vertex : UNIT_FACE) {
            transformation.transform(TEMP_VECTOR.set(vertex.position(), 1));
            buffer.vertex(TEMP_VECTOR.x, TEMP_VECTOR.y, TEMP_VECTOR.z).texture(vertex.u(), vertex.v()).color(r, g, b, a).light(light).next();
        }
        te.draw();
    }

    record Vertex(Vector3f position, float u, float v) {}
}