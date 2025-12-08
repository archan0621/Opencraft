package kr.co.opencraft.world;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * 월드 관리 클래스
 */
public class World {
    private BlockManager blockManager;
    private List<Vector3> blockPositions;
    private List<ModelInstance> allBlockInstances;

    public World(BlockManager blockManager) {
        this.blockManager = blockManager;
        this.blockPositions = new ArrayList<>();
        this.allBlockInstances = new ArrayList<>();
    }

    /**
     * 계단식 땅 생성
     */
    public void createStaircaseGround(int gridSize, float blockSpacing, float blockHeight) {
        blockPositions.clear();
        allBlockInstances.clear();

        // 그리드 중심을 원점으로 맞추기 위한 오프셋
        float offset = (gridSize - 1) * blockSpacing * 0.5f;

        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                // 블록 위치 계산 (중앙 정렬)
                float blockX = x * blockSpacing - offset;
                float blockZ = z * blockSpacing - offset;
                // 계단식 배치: x와 z의 합에 따라 높이 증가
                float blockY = (x + z) * blockHeight; // 대각선 방향으로 계단

                Vector3 blockPosition = new Vector3(blockX, blockY, blockZ);
                blockPositions.add(blockPosition);

                // 블록 인스턴스 생성
                List<ModelInstance> instances = blockManager.createBlockInstances(blockPosition);
                allBlockInstances.addAll(instances);
            }
        }
    }

    public List<Vector3> getBlockPositions() {
        return blockPositions;
    }

    public List<ModelInstance> getAllBlockInstances() {
        return allBlockInstances;
    }

    public void dispose() {
        if (blockManager != null) {
            blockManager.dispose();
        }
    }
}

