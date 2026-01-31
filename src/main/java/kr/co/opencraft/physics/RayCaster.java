package kr.co.opencraft.physics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import kr.co.opencraft.world.World;

import java.util.List;

/**
 * 레이캐스팅을 통한 블록 선택
 */
public class RayCaster {
    private static final float MAX_DISTANCE = 10f;
    private static final float STEP = 0.05f; // 더 정밀한 탐지

    /**
     * 카메라 방향으로 레이를 발사하여 가장 가까운 블록 찾기
     * @param ray 카메라에서 발사되는 레이
     * @param world 월드 (블록 위치 정보)
     * @return 선택된 블록의 위치 (없으면 null)
     */
    public static Vector3 raycast(Ray ray, World world) {
        Vector3 current = new Vector3(ray.origin);
        Vector3 step = new Vector3(ray.direction).nor().scl(STEP);
        
        List<Vector3> blockPositions = world.getBlockPositions();
        
        // 최대 거리까지 레이를 진행
        for (float distance = 0; distance < MAX_DISTANCE; distance += STEP) {
            current.add(step);
            
            // 현재 위치에 블록이 있는지 확인
            for (Vector3 blockPos : blockPositions) {
                if (isPointInBlock(current, blockPos)) {
                    return new Vector3(blockPos);
                }
            }
        }
        
        return null; // 블록을 찾지 못함
    }
    
    /**
     * 점이 블록 내부에 있는지 확인
     * 블록은 (blockPos.x-0.5 ~ blockPos.x+0.5) 범위를 가짐
     */
    private static boolean isPointInBlock(Vector3 point, Vector3 blockPos) {
        float halfSize = 0.5f;
        
        return point.x >= blockPos.x - halfSize && point.x <= blockPos.x + halfSize &&
               point.y >= blockPos.y - halfSize && point.y <= blockPos.y + halfSize &&
               point.z >= blockPos.z - halfSize && point.z <= blockPos.z + halfSize;
    }
}
