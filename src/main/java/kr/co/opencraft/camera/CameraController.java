package kr.co.opencraft.camera;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.input.InputHandler;
import kr.co.opencraft.physics.CollisionDetector;

/**
 * 카메라 이동 및 회전 제어
 */
public class CameraController {
    private FPSCamera camera;
    private CollisionDetector collisionDetector;
    private InputHandler inputHandler;
    private float moveSpeed = 5f;

    public CameraController(FPSCamera camera, CollisionDetector collisionDetector, InputHandler inputHandler) {
        this.camera = camera;
        this.collisionDetector = collisionDetector;
        this.inputHandler = inputHandler;
    }

    public void update(float delta) {
        // 마우스 회전 처리
        if (inputHandler.isMouseLocked()) {
            int deltaX = inputHandler.getMouseDeltaX();
            int deltaY = inputHandler.getMouseDeltaY();
            float deltaYaw = deltaX * inputHandler.getMouseSensitivity();
            float deltaPitch = -deltaY * inputHandler.getMouseSensitivity();
            camera.addYaw(deltaYaw);
            camera.addPitch(deltaPitch);
        }

        // 이동 처리
        Vector3 direction = camera.getDirection();
        Vector3 moveDir = new Vector3();

        // 수평 방향 벡터
        Vector3 horizontalDir = new Vector3(direction);
        horizontalDir.y = 0;
        horizontalDir.nor();

        // WASD 이동 처리
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

        // 수직 이동 처리
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            moveDir.y += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            moveDir.y -= 1f;
        }

        // 이동 벡터 정규화 및 적용
        if (moveDir.len() > 0.001f) {
            moveDir.nor();
            moveDir.scl(moveSpeed * delta);
            Vector3 newPosition = new Vector3(camera.getPosition()).add(moveDir);

            // 충돌 체크: 새 위치가 블록과 충돌하지 않으면 이동 허용
            if (!collisionDetector.checkCollision(newPosition)) {
                camera.setPosition(newPosition);
            }
        }

        // 카메라 업데이트
        camera.update();
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }
}

