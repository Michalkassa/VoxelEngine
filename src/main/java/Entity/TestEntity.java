package Entity;

import Core.Mesh;
import Core.Transform;
import World.Chunk;
import org.joml.Vector3f;

import java.util.ArrayList;

public class TestEntity extends Entity{


    public TestEntity(Vector3f position){
        super(new Transform(position));
        mesh = new Mesh(buildTestEntity(), 216);
    }

    private static void addCuboid(
            ArrayList<Float> v,
            float cx, float cy, float cz,
            float hx, float hy, float hz
    ) {
        float x1 = cx - hx, x2 = cx + hx;
        float y1 = cy - hy, y2 = cy + hy;
        float z1 = cz - hz, z2 = cz + hz;

        float[] cube = {
                // FRONT
                x1,y1,z2,0,0,  x2,y1,z2,1,0,  x2,y2,z2,1,1,
                x2,y2,z2,1,1,  x1,y2,z2,0,1,  x1,y1,z2,0,0,

                // BACK
                x1,y1,z1,0,0,  x1,y2,z1,0,1,  x2,y2,z1,1,1,
                x2,y2,z1,1,1,  x2,y1,z1,1,0,  x1,y1,z1,0,0,

                // LEFT
                x1,y1,z1,0,0,  x1,y1,z2,1,0,  x1,y2,z2,1,1,
                x1,y2,z2,1,1,  x1,y2,z1,0,1,  x1,y1,z1,0,0,

                // RIGHT
                x2,y1,z2,0,0,  x2,y2,z2,0,1,  x2,y2,z1,1,1,
                x2,y2,z1,1,1,  x2,y1,z1,1,0,  x2,y1,z2,0,0,

                // BOTTOM
                x1,y1,z1,0,0,  x2,y1,z1,1,0,  x2,y1,z2,1,1,
                x2,y1,z2,1,1,  x1,y1,z2,0,1,  x1,y1,z1,0,0,

                // TOP
                x1,y2,z1,0,0,  x1,y2,z2,0,1,  x2,y2,z2,1,1,
                x2,y2,z2,1,1,  x2,y2,z1,1,0,  x1,y2,z1,0,0
        };

        for (float f : cube) v.add(f);
    }


    private static float[] buildTestEntity() {

        ArrayList<Float> v = new ArrayList<>();

        // BODY (0.5 × 0.75 × 0.25)
        addCuboid(v, 0f, 0f, 0f,
                0.25f, 0.375f, 0.125f);

// HEAD (0.5 × 0.5 × 0.5)
        addCuboid(v, 0f, 0.625f, 0f,
                0.25f, 0.25f, 0.25f);

// LEFT ARM (0.25 × 0.75 × 0.25)
        addCuboid(v, -0.375f, 0f, 0f,
                0.125f, 0.375f, 0.125f);

// RIGHT ARM
        addCuboid(v,  0.375f, 0f, 0f,
                0.125f, 0.375f, 0.125f);

// LEFT LEG (0.25 × 0.75 × 0.25)
        addCuboid(v, -0.125f, -0.75f, 0f,
                0.125f, 0.375f, 0.125f);

// RIGHT LEG
        addCuboid(v,  0.125f, -0.75f, 0f,
                0.125f, 0.375f, 0.125f);


        float[] out = new float[v.size()];
        for (int i = 0; i < v.size(); i++) out[i] = v.get(i);
        return out;
    }


}
