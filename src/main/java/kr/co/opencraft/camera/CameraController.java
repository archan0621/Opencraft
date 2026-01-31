package kr.co.opencraft.camera;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.entity.Player;
import kr.co.opencraft.input.InputHandler;
import kr.co.opencraft.physics.PhysicsSystem;

/**
 * 카메라 및 플레이어 제어
 * 
 * 책임:
 * - 입력 처리 (WASD, 점프, 마우스)
 * - 이동 의도를 velocity로 변환
 * - PhysicsSystem 호출
 * - 카메라를 플레이어 눈 높이로 동기화
 */
public class CameraController {
    private final FPSCamera camera;
    private final Player player;
    private final PhysicsSystem physicsSystem;
    private final InputHandler inputHandler;
    private float moveSpeed = 5f;

    public CameraController(FPSCamera camera, Player player, PhysicsSystem physicsSystem, InputHandler inputHandler) {
        this.camera = camera;
        this.player = player;
        this.physicsSystem = physicsSystem;
        this.inputHandler = inputHandler;
        
        // 카메라를 플레이어 눈 높이로 초기화
        updateCameraPosition();
    }

    public void update(float delta) {
        // 1. 마우스 회전 처리
        if (inputHandler.isMouseLocked()) {
            int deltaX = inputHandler.getMouseDeltaX();
            int deltaY = inputHandler.getMouseDeltaY();
            float deltaYaw = deltaX * inputHandler.getMouseSensitivity();
            float deltaPitch = deltaY * inputHandler.getMouseSensitivity();
            camera.addYaw(deltaYaw);
            camera.addPitch(deltaPitch);
        }

        // 2. 점프 입력
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            physicsSystem.tryJump(player);
        }

        // 3. WASD 입력을 속도로 변환
        Vector3 direction = camera.getDirection();
        Vector3 moveDir = new Vector3();

        // 수평 방향 벡터
        Vector3 horizontalDir = new Vector3(direction);
        horizontalDir.y = 0;
        horizontalDir.nor();

        // WASD 이동
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveDir.add(horizontalDir);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveDir.sub(horizontalDir);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            Vector3 right = new Vector3();
            Vector3 up = new Vector3(0, 1, 0);
            right.set(horizontalDir).crs(up).nor();
            moveDir.sub(right);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            Vector3 right = new Vector3();
            Vector3 up = new Vector3(0, 1, 0);
            right.set(horizontalDir).crs(up).nor();
            moveDir.add(right);
        }

        // 수평 속도 설정
        if (moveDir.len() > 0.001f) {
            moveDir.nor().scl(moveSpeed);
            player.getVelocity().x = moveDir.x;
            player.getVelocity().z = moveDir.z;
        } else {
            player.getVelocity().x = 0;
            player.getVelocity().z = 0;
        }

        // 4. 물리 시스템 업데이트 (중력, 충돌, 이동)
        physicsSystem.update(player, delta);
        
        // 5. 카메라 동기화
        updateCameraPosition();
        camera.update();
    }
    
    /**
     * 카메라를 플레이어 눈 높이로 동기화
     */
    private void updateCameraPosition() {
        camera.setPosition(player.getEyePosition());
    }
    
    public Player getPlayer() {
        return player;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }
}

