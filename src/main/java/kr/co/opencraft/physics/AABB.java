package kr.co.opencraft.physics;

import com.badlogic.gdx.math.Vector3;

/**
 * 축 정렬 경계 상자 (Axis-Aligned Bounding Box)
 * 
 * 책임:
 * - 직육면체 AABB 표현 (halfWidth, halfHeight, halfDepth)
 * - mutable 구조로 GC 최소화
 * - min/max 좌표 계산
 * - AABB vs AABB 충돌 검사
 */
public class AABB {
    // 중심 좌표
    private final Vector3 center;
    
    // 반 크기 (중심에서 각 면까지 거리)
    private float halfWidth;  // X 축
    private float halfHeight; // Y 축
    private float halfDepth;  // Z 축
    
    // 경계 좌표 (캐시)
    private final Vector3 min;
    private final Vector3 max;
    
    /**
     * 직육면체 AABB 생성
     * @param center 중심 좌표
     * @param halfWidth X 축 반 크기
     * @param halfHeight Y 축 반 크기
     * @param halfDepth Z 축 반 크기
     */
    public AABB(Vector3 center, float halfWidth, float halfHeight, float halfDepth) {
        this.center = new Vector3(center);
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.halfDepth = halfDepth;
        this.min = new Vector3();
        this.max = new Vector3();
        updateBounds();
    }
    
    /**
     * 정육면체 AABB 생성 (편의 생성자)
     */
    public AABB(Vector3 center, float halfSize) {
        this(center, halfSize, halfSize, halfSize);
    }
    
    /**
     * 경계 좌표 재계산
     */
    private void updateBounds() {
        min.set(center.x - halfWidth, center.y - halfHeight, center.z - halfDepth);
        max.set(center.x + halfWidth, center.y + halfHeight, center.z + halfDepth);
    }
    
    /**
     * 중심 좌표 설정 (mutable)
     */
    public void setCenter(Vector3 newCenter) {
        center.set(newCenter);
        updateBounds();
    }
    
    /**
     * 중심 좌표 설정 (개별 좌표)
     */
    public void setCenter(float x, float y, float z) {
        center.set(x, y, z);
        updateBounds();
    }
    
    /**
     * 크기 설정
     */
    public void setSize(float halfWidth, float halfHeight, float halfDepth) {
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.halfDepth = halfDepth;
        updateBounds();
    }
    
    /**
     * 다른 AABB와 충돌 체크
     */
    public boolean intersects(AABB other) {
        return this.max.x > other.min.x && this.min.x < other.max.x &&
               this.max.y > other.min.y && this.min.y < other.max.y &&
               this.max.z > other.min.z && this.min.z < other.max.z;
    }
    
    /**
     * 이동 (offset 적용)
     */
    public void offset(float dx, float dy, float dz) {
        center.add(dx, dy, dz);
        updateBounds();
    }
    
    // Getters
    public Vector3 getCenter() { return center; }
    public Vector3 getMin() { return min; }
    public Vector3 getMax() { return max; }
    public float getHalfWidth() { return halfWidth; }
    public float getHalfHeight() { return halfHeight; }
    public float getHalfDepth() { return halfDepth; }
}

