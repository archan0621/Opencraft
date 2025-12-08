package kr.co.opencraft.physics;

import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.world.World;

/**
 * 충돌 감지 및 처리
 */
public class CollisionDetector {
    private static final float CAMERA_RADIUS = 0.2f;
    private static final float BLOCK_HALF_SIZE = 0.5f;

    private World world;

    public CollisionDetector(World world) {
        this.world = world;
    }

    /**
     * 카메라 위치가 블록과 충돌하는지 확인
     * @param position 카메라 위치
     * @return 충돌 여부
     */
    public boolean checkCollision(Vector3 position) {
        for (Vector3 blockPos : world.getBlockPositions()) {
            // AABB 경계 계산
            float minX = blockPos.x - BLOCK_HALF_SIZE;
            float maxX = blockPos.x + BLOCK_HALF_SIZE;
            float minY = blockPos.y - BLOCK_HALF_SIZE;
            float maxY = blockPos.y + BLOCK_HALF_SIZE;
            float minZ = blockPos.z - BLOCK_HALF_SIZE;
            float maxZ = blockPos.z + BLOCK_HALF_SIZE;

            // X, Z 축에서 블록과 겹치는지 확인
            boolean overlapsXZ = position.x + CAMERA_RADIUS > minX && position.x - CAMERA_RADIUS < maxX &&
                                 position.z + CAMERA_RADIUS > minZ && position.z - CAMERA_RADIUS < maxZ;

            if (!overlapsXZ) {
                continue; // X, Z에서 겹치지 않으면 충돌 없음
            }

            // Y 축 충돌 체크
            // 카메라가 블록 위에 있을 때: 윗면에서 충돌
            if (position.y > maxY) {
                if (position.y - CAMERA_RADIUS <= maxY) {
                    return true; // 윗면과 충돌
                }
            }
            // 카메라가 블록 아래에 있을 때: 밑면에서 충돌
            else if (position.y < minY) {
                if (position.y + CAMERA_RADIUS >= minY) {
                    return true; // 밑면과 충돌
                }
            }
            // 카메라가 블록 안에 있을 때: 항상 충돌
            else {
                return true; // 블록 내부 충돌
            }
        }
        return false; // 충돌 없음
    }
}

