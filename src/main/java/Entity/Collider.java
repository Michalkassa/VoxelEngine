package Entity;

import Core.Transform;
import org.joml.Vector3f;

public class Collider {
    Transform transform;

    public Collider(Transform transform) {
       this.transform = transform;
    }

    public static boolean collidersColliding(Collider collider1, Collider collider2) {

        float maxX1 = collider1.transform.position.x + collider1.transform.scale.x / 2;
        float minX1 = collider1.transform.position.x - collider1.transform.scale.x / 2;

        float maxZ1 = collider1.transform.position.z + collider1.transform.scale.z / 2;
        float minZ1 = collider1.transform.position.z - collider1.transform.scale.z / 2;

        float maxY1 = collider1.transform.position.y + collider1.transform.scale.y / 2;
        float minY1 = collider1.transform.position.y - collider1.transform.scale.y / 2;

        float maxX2 = collider2.transform.position.x + collider2.transform.scale.x / 2;
        float minX2 = collider2.transform.position.x - collider2.transform.scale.x / 2;

        float maxZ2 = collider2.transform.position.z + collider2.transform.scale.z / 2;
        float minZ2 = collider2.transform.position.z - collider2.transform.scale.z / 2;

        float maxY2 = collider2.transform.position.y + collider2.transform.scale.y / 2;
        float minY2 = collider2.transform.position.y - collider2.transform.scale.y / 2;

        return (
                minX1 <= maxX2 &&
                        maxX1 >= minX2 &&
                        minY1 <= maxY2 &&
                        maxY1 >= minY2 &&
                        minZ1 <= maxZ2 &&
                        maxZ1 >= minZ2
        );
    }

}