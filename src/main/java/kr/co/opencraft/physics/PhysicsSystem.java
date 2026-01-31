package kr.co.opencraft.physics;

import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.entity.Player;
import kr.co.opencraft.world.World;

/**
 * 물리 시스템 - Minecraft 스타일 축 분리 충돌 해결
 * 
 * 책임:
 * - 중력 적용
 * - 축 분리 이동 (Y → X → Z)
 * - 충돌 감지 및 해결
 * - onGround 상태 결정
 * 
 * 특징:
 * - Y축 먼저 처리 (중력/점프)
 * - 각 축마다 충돌 체크 + 위치 보정
 * - 터널링 방지 (step 기반 이동)
 */
public class PhysicsSystem {
    private final World world;
    private static final float BLOCK_SIZE = 1.0f;
    
    public PhysicsSystem(World world) {
        this.world = world;
    }
    
    /**
     * 플레이어 물리 업데이트 (메인 진입점)
     */
    public void update(Player player, float delta) {
        // 1. 중력 적용
        applyGravity(player, delta);
        
        // 2. 속도를 위치 변화로 변환
        float dx = player.getVelocity().x * delta;
        float dy = player.getVelocity().y * delta;
        float dz = player.getVelocity().z * delta;
        
        // 3. 축 분리 이동 및 충돌 해결
        moveAndCollide(player, dx, dy, dz);
    }
    
    /**
     * 중력 적용
     */
    private void applyGravity(Player player, float delta) {
        if (!player.isOnGround()) {
            player.getVelocity().y += Player.GRAVITY * delta;
            
            // 최대 낙하 속도 제한
            if (player.getVelocity().y < Player.TERMINAL_VELOCITY) {
                player.getVelocity().y = Player.TERMINAL_VELOCITY;
            }
        }
    }
    
    /**
     * 축 분리 이동 및 충돌 해결
     * Y → X → Z 순서로 처리
     */
    private void moveAndCollide(Player player, float dx, float dy, float dz) {
        Vector3 pos = player.getPosition();
        AABB aabb = player.getAABB();
        
        // Y축 이동 (중력/점프)
        if (dy != 0) {
            pos.y += dy;
            player.setPosition(pos);
            
            if (checkCollision(aabb)) {
                // 충돌 발생 - 블록 표면으로 정확히 보정
                if (dy > 0) {
                    // 위로 이동 중 천장 충돌
                    // 머리(aabb.max.y)가 블록 아래면(blockY - 0.5)에 닿음
                    float ceilingY = findCeilingY(aabb);
                    pos.y = ceilingY - Player.HEIGHT;
                    player.setPosition(pos);
                    player.getVelocity().y = 0;
                } else {
                    // 아래로 이동 중 바닥 충돌
                    // 발(pos.y)이 블록 윗면(blockY + 0.5)에 닿음
                    float floorY = findFloorY(aabb);
                    pos.y = floorY;
                    player.setPosition(pos);
                    player.getVelocity().y = 0;
                    player.setOnGround(true);
                }
            } else if (dy < 0) {
                // 아래로 이동했는데 충돌 없음 = 공중
                player.setOnGround(false);
            }
        }
        
        // X축 이동
        if (dx != 0) {
            pos.x += dx;
            player.setPosition(pos);
            
            if (checkCollision(aabb)) {
                // 충돌 발생 - 벽 표면으로 정확히 보정
                if (dx > 0) {
                    // 오른쪽 벽 충돌
                    // 플레이어 오른쪽 면(pos.x + width/2)이 블록 왼쪽 면(blockX - 0.5)에 닿음
                    float wallX = findWallXPositive(aabb);
                    pos.x = wallX - Player.WIDTH / 2f;
                } else {
                    // 왼쪽 벽 충돌
                    // 플레이어 왼쪽 면(pos.x - width/2)이 블록 오른쪽 면(blockX + 0.5)에 닿음
                    float wallX = findWallXNegative(aabb);
                    pos.x = wallX + Player.WIDTH / 2f;
                }
                player.setPosition(pos);
                player.getVelocity().x = 0;
            }
        }
        
        // Z축 이동
        if (dz != 0) {
            pos.z += dz;
            player.setPosition(pos);
            
            if (checkCollision(aabb)) {
                // 충돌 발생 - 벽 표면으로 정확히 보정
                if (dz > 0) {
                    // 앞쪽 벽 충돌
                    // 플레이어 앞면(pos.z + width/2)이 블록 뒷면(blockZ - 0.5)에 닿음
                    float wallZ = findWallZPositive(aabb);
                    pos.z = wallZ - Player.WIDTH / 2f;
                } else {
                    // 뒤쪽 벽 충돌
                    // 플레이어 뒷면(pos.z - width/2)이 블록 앞면(blockZ + 0.5)에 닿음
                    float wallZ = findWallZNegative(aabb);
                    pos.z = wallZ + Player.WIDTH / 2f;
                }
                player.setPosition(pos);
                player.getVelocity().z = 0;
            }
        }
        
        // X/Z 이동 후 발 바로 아래 재검사
        // 절벽 가장자리에서 공중부양 방지
        if (dx != 0 || dz != 0) {
            checkGroundBelow(player);
        }
    }
    
    /**
     * 충돌 체크 - 모든 블록과 AABB 충돌 검사
     */
    private boolean checkCollision(AABB playerAABB) {
        for (Vector3 blockPos : world.getBlockPositions()) {
            AABB blockAABB = new AABB(blockPos, BLOCK_SIZE / 2f);
            if (playerAABB.intersects(blockAABB)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 바닥 Y 좌표 찾기 (플레이어 발 위치)
     * 플레이어와 충돌하는 블록 중 가장 높은 윗면 반환
     */
    private float findFloorY(AABB playerAABB) {
        float highestFloor = -Float.MAX_VALUE;
        
        for (Vector3 blockPos : world.getBlockPositions()) {
            AABB blockAABB = new AABB(blockPos, BLOCK_SIZE / 2f);
            if (playerAABB.intersects(blockAABB)) {
                // 블록 윗면 = blockY + 0.5
                float blockTop = blockPos.y + BLOCK_SIZE / 2f;
                if (blockTop > highestFloor) {
                    highestFloor = blockTop;
                }
            }
        }
        
        return highestFloor;
    }
    
    /**
     * 천장 Y 좌표 찾기 (플레이어 머리 위치)
     * 플레이어와 충돌하는 블록 중 가장 낮은 아래면 반환
     */
    private float findCeilingY(AABB playerAABB) {
        float lowestCeiling = Float.MAX_VALUE;
        
        for (Vector3 blockPos : world.getBlockPositions()) {
            AABB blockAABB = new AABB(blockPos, BLOCK_SIZE / 2f);
            if (playerAABB.intersects(blockAABB)) {
                // 블록 아래면 = blockY - 0.5
                float blockBottom = blockPos.y - BLOCK_SIZE / 2f;
                if (blockBottom < lowestCeiling) {
                    lowestCeiling = blockBottom;
                }
            }
        }
        
        return lowestCeiling;
    }
    
    /**
     * 오른쪽 벽 X 좌표 찾기
     * 플레이어와 충돌하는 블록 중 가장 왼쪽 면 반환
     */
    private float findWallXPositive(AABB playerAABB) {
        float leftmostWall = Float.MAX_VALUE;
        
        for (Vector3 blockPos : world.getBlockPositions()) {
            AABB blockAABB = new AABB(blockPos, BLOCK_SIZE / 2f);
            if (playerAABB.intersects(blockAABB)) {
                // 블록 왼쪽 면 = blockX - 0.5
                float blockLeft = blockPos.x - BLOCK_SIZE / 2f;
                if (blockLeft < leftmostWall) {
                    leftmostWall = blockLeft;
                }
            }
        }
        
        return leftmostWall;
    }
    
    /**
     * 왼쪽 벽 X 좌표 찾기
     * 플레이어와 충돌하는 블록 중 가장 오른쪽 면 반환
     */
    private float findWallXNegative(AABB playerAABB) {
        float rightmostWall = -Float.MAX_VALUE;
        
        for (Vector3 blockPos : world.getBlockPositions()) {
            AABB blockAABB = new AABB(blockPos, BLOCK_SIZE / 2f);
            if (playerAABB.intersects(blockAABB)) {
                // 블록 오른쪽 면 = blockX + 0.5
                float blockRight = blockPos.x + BLOCK_SIZE / 2f;
                if (blockRight > rightmostWall) {
                    rightmostWall = blockRight;
                }
            }
        }
        
        return rightmostWall;
    }
    
    /**
     * 앞쪽 벽 Z 좌표 찾기
     * 플레이어와 충돌하는 블록 중 가장 뒤쪽 면 반환
     */
    private float findWallZPositive(AABB playerAABB) {
        float backmostWall = Float.MAX_VALUE;
        
        for (Vector3 blockPos : world.getBlockPositions()) {
            AABB blockAABB = new AABB(blockPos, BLOCK_SIZE / 2f);
            if (playerAABB.intersects(blockAABB)) {
                // 블록 뒤쪽 면 = blockZ - 0.5
                float blockBack = blockPos.z - BLOCK_SIZE / 2f;
                if (blockBack < backmostWall) {
                    backmostWall = blockBack;
                }
            }
        }
        
        return backmostWall;
    }
    
    /**
     * 뒤쪽 벽 Z 좌표 찾기
     * 플레이어와 충돌하는 블록 중 가장 앞쪽 면 반환
     */
    private float findWallZNegative(AABB playerAABB) {
        float frontmostWall = -Float.MAX_VALUE;
        
        for (Vector3 blockPos : world.getBlockPositions()) {
            AABB blockAABB = new AABB(blockPos, BLOCK_SIZE / 2f);
            if (playerAABB.intersects(blockAABB)) {
                // 블록 앞쪽 면 = blockZ + 0.5
                float blockFront = blockPos.z + BLOCK_SIZE / 2f;
                if (blockFront > frontmostWall) {
                    frontmostWall = blockFront;
                }
            }
        }
        
        return frontmostWall;
    }
    
    /**
     * 발 바로 아래 블록 체크 - Minecraft 방식
     * 절벽 가장자리에서 공중부양 방지
     */
    private void checkGroundBelow(Player player) {
        Vector3 pos = player.getPosition();
        
        // 발 바로 아래 epsilon(0.01f) 만큼 떨어진 위치에 블록이 있는지 체크
        Vector3 belowPos = new Vector3(pos.x, pos.y - 0.01f, pos.z);
        AABB belowAABB = new AABB(belowPos, Player.WIDTH / 2f, 0.01f, Player.WIDTH / 2f);
        
        boolean hasGroundBelow = checkCollision(belowAABB);
        player.setOnGround(hasGroundBelow);
    }
    
    /**
     * 점프 시도
     */
    public void tryJump(Player player) {
        if (player.isOnGround()) {
            player.getVelocity().y = Player.JUMP_VELOCITY;
            player.setOnGround(false);
        }
    }
}
