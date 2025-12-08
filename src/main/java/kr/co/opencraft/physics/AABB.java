package kr.co.opencraft.physics;

import com.badlogic.gdx.math.Vector3;

/**
 * 축 정렬 경계 상자 (Axis-Aligned Bounding Box)
 */
public class AABB {
    private Vector3 min;
    private Vector3 max;
    private Vector3 center;
    private float halfSize;

    public AABB(Vector3 center, float halfSize) {
        this.center = new Vector3(center);
        this.halfSize = halfSize;
        this.min = new Vector3(center.x - halfSize, center.y - halfSize, center.z - halfSize);
        this.max = new Vector3(center.x + halfSize, center.y + halfSize, center.z + halfSize);
    }

    public Vector3 getMin() {
        return min;
    }

    public Vector3 getMax() {
        return max;
    }

    public Vector3 getCenter() {
        return center;
    }

    public float getHalfSize() {
        return halfSize;
    }

    public void setCenter(Vector3 center) {
        this.center.set(center);
        this.min.set(center.x - halfSize, center.y - halfSize, center.z - halfSize);
        this.max.set(center.x + halfSize, center.y + halfSize, center.z + halfSize);
    }
}

