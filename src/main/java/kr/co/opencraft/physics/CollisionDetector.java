package kr.co.opencraft.physics;

import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.world.World;

/**
 * 충돌 감지 및 처리 (원자적 블록 기반)
 * - 블록은 1x1x1 크기의 원자적 단위
 * - 플레이어는 0.6x1.8x0.6 크기의 AABB
 * - 두 AABB가 겹치면 충돌
 */
public class CollisionDetector {
    private static final float BLOCK_HALF_SIZE = 0.5f;

    private World world;

    public CollisionDetector(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }
    
    /**
     * 플레이어 AABB와 블록이 충돌하는지 확인
     * @param playerFootPosition 플레이어 발 위치
     * @param playerWidth 플레이어 너비 (0.6)
     * @param playerHeight 플레이어 높이 (1.8)
     * @return 충돌 여부
     */
    public boolean checkPlayerCollision(Vector3 playerFootPosition, float playerWidth, float playerHeight) {
        float playerHalfWidth = playerWidth / 2f;
        
        // 플레이어 AABB 경계
        float playerMinX = playerFootPosition.x - playerHalfWidth;
        float playerMaxX = playerFootPosition.x + playerHalfWidth;
        float playerMinY = playerFootPosition.y;
        float playerMaxY = playerFootPosition.y + playerHeight;
        float playerMinZ = playerFootPosition.z - playerHalfWidth;
        float playerMaxZ = playerFootPosition.z + playerHalfWidth;
        
        // 모든 블록과 충돌 체크
        for (Vector3 blockPos : world.getBlockPositions()) {
            // 블록 AABB 경계
            float blockMinX = blockPos.x - BLOCK_HALF_SIZE;
            float blockMaxX = blockPos.x + BLOCK_HALF_SIZE;
            float blockMinY = blockPos.y - BLOCK_HALF_SIZE;
            float blockMaxY = blockPos.y + BLOCK_HALF_SIZE;
            float blockMinZ = blockPos.z - BLOCK_HALF_SIZE;
            float blockMaxZ = blockPos.z + BLOCK_HALF_SIZE;
            
            // AABB vs AABB 충돌 체크: 모든 축에서 겹치면 충돌
            boolean xOverlap = playerMaxX > blockMinX && playerMinX < blockMaxX;
            boolean yOverlap = playerMaxY > blockMinY && playerMinY < blockMaxY;
            boolean zOverlap = playerMaxZ > blockMinZ && playerMinZ < blockMaxZ;
            
            if (xOverlap && yOverlap && zOverlap) {
                return true; // 충돌!
            }
        }
        
        return false; // 충돌 없음
    }
    
    /**
     * 레거시 메서드 (호환성 유지)
     * @deprecated checkPlayerCollision 사용 권장
     */
    @Deprecated
    public boolean checkCollision(Vector3 position) {
        // 간단한 점 체크로 대체
        return checkPlayerCollision(position, 0.01f, 0.01f);
    }
}

