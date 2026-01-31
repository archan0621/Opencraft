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
     * 월드 초기화 (모든 블록 제거)
     */
    public void clear() {
        blockPositions.clear();
        allBlockInstances.clear();
    }

    /**
     * 단일 블록 추가
     */
    public void addBlock(Vector3 position) {
        blockPositions.add(new Vector3(position));
        List<ModelInstance> instances = blockManager.createBlockInstances(position);
        allBlockInstances.addAll(instances);
    }
    
    /**
     * 평평한 땅 추가
     */
    public void addFlatGround(int gridSize, float blockSpacing, float yPosition) {
        // 그리드 중심을 원점으로 맞추기 위한 오프셋
        float offset = (gridSize - 1) * blockSpacing * 0.5f;

        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                // 블록 위치 계산 (중앙 정렬)
                float blockX = x * blockSpacing - offset;
                float blockZ = z * blockSpacing - offset;

                Vector3 blockPosition = new Vector3(blockX, yPosition, blockZ);
                blockPositions.add(blockPosition);

                // 블록 인스턴스 생성
                List<ModelInstance> instances = blockManager.createBlockInstances(blockPosition);
                allBlockInstances.addAll(instances);
            }
        }
    }

    /**
     * 계단식 땅 추가
     */
    public void addStaircaseGround(int gridSize, float blockSpacing, float blockHeight) {
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

    /**
     * 평평한 땅 생성 (기존 블록 제거 후 생성)
     */
    public void createFlatGround(int gridSize, float blockSpacing, float yPosition) {
        clear();
        addFlatGround(gridSize, blockSpacing, yPosition);
    }

    /**
     * 계단식 땅 생성 (기존 블록 제거 후 생성)
     */
    public void createStaircaseGround(int gridSize, float blockSpacing, float blockHeight) {
        clear();
        addStaircaseGround(gridSize, blockSpacing, blockHeight);
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

