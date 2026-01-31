package kr.co.opencraft.entity;

import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.physics.AABB;

/**
 * 플레이어 엔티티
 * 
 * 책임:
 * - 플레이어 상태 보유 (위치, 속도, onGround)
 * - AABB 제공
 * - 물리 상수 제공
 * 
 * 책임 없음:
 * - 충돌 감지/해결 (PhysicsSystem 담당)
 * - 이동 로직 (Controller 담당)
 */
public class Player {
    // 상태
    private final Vector3 position; // 발 위치 (바닥)
    private final Vector3 velocity; // 속도 벡터
    private boolean onGround;       // 땅에 있는지 여부
    
    // AABB (재사용)
    private final AABB aabb;
    
    // 플레이어 크기 (Minecraft 기준)
    public static final float WIDTH = 0.6f;         // 너비
    public static final float HEIGHT = 1.8f;        // 전체 높이
    public static final float EYE_HEIGHT = 1.62f;   // 눈 높이 (카메라 위치)
    
    // 물리 상수
    public static final float GRAVITY = -20f;           // 중력 가속도 (blocks/s²)
    public static final float JUMP_VELOCITY = 7f;      // 점프 초기 속도 (blocks/s)
    public static final float TERMINAL_VELOCITY = -50f; // 최대 낙하 속도
    
    public Player(Vector3 startPosition) {
        this.position = new Vector3(startPosition);
        this.velocity = new Vector3(0, 0, 0);
        this.onGround = true;
        
        // AABB 생성: 중심은 발 + 높이/2
        Vector3 center = new Vector3(startPosition.x, startPosition.y + HEIGHT / 2f, startPosition.z);
        this.aabb = new AABB(center, WIDTH / 2f, HEIGHT / 2f, WIDTH / 2f);
    }
    
    /**
     * 위치 업데이트 (AABB도 자동 업데이트)
     */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        updateAABB();
    }
    
    public void setPosition(Vector3 newPosition) {
        position.set(newPosition);
        updateAABB();
    }
    
    /**
     * AABB 중심 업데이트
     */
    private void updateAABB() {
        aabb.setCenter(position.x, position.y + HEIGHT / 2f, position.z);
    }
    
    /**
     * 눈 높이 위치 (카메라 위치)
     */
    public Vector3 getEyePosition() {
        return new Vector3(position.x, position.y + EYE_HEIGHT, position.z);
    }
    
    // Getters (상태 읽기)
    public Vector3 getPosition() { return position; }
    public Vector3 getVelocity() { return velocity; }
    public AABB getAABB() { return aabb; }
    public boolean isOnGround() { return onGround; }
    
    // Setters (PhysicsSystem이 사용)
    public void setOnGround(boolean onGround) { this.onGround = onGround; }
}
