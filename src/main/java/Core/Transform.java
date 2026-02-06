package Core;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.swing.text.Position;

public class Transform {
    public Vector3f position = new Vector3f();
    public Vector3f rotation = new Vector3f();
    public Vector3f scale    = new Vector3f(1,1,1);


    public Transform (Vector3f position){
        this.position = position;
    }

    public Matrix4f getMatrix() {
        return new Matrix4f()
                .identity()
                .translate(position)
                .rotateXYZ(rotation)
                .scale(scale);
    }
}
