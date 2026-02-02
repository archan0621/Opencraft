package kr.co.opencraft.camera;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import kr.co.voxelite.camera.CameraController;
import kr.co.voxelite.camera.FPSCamera;
import kr.co.voxelite.input.InputHandler;
import kr.co.voxelite.physics.PhysicsSystem;
import kr.co.opencraft.entity.OpenCraftPlayer;

/**
 * OpenCraft-specific camera controller with fly mode support.
 * Extends base movement with game-specific fly mode logic.
 */
public class OpenCraftCameraController extends CameraController {
    private final OpenCraftPlayer openCraftPlayer;

    public OpenCraftCameraController(FPSCamera camera, OpenCraftPlayer player, PhysicsSystem physicsSystem, InputHandler inputHandler) {
        super(camera, player, physicsSystem, inputHandler);
        this.openCraftPlayer = player;
    }

    @Override
    public void update(float delta) {
        // Handle mouse look
        if (inputHandler.isMouseLocked()) {
            int deltaX = inputHandler.getMouseDeltaX();
            int deltaY = inputHandler.getMouseDeltaY();
            float deltaYaw = deltaX * inputHandler.getMouseSensitivity();
            float deltaPitch = deltaY * inputHandler.getMouseSensitivity();
            camera.addYaw(deltaYaw);
            camera.addPitch(deltaPitch);
        }

        Vector3 direction = camera.getDirection();
        Vector3 moveDir = new Vector3();

        if (openCraftPlayer.isFlyMode()) {
            // === FLY MODE ===
            // Horizontal movement based on camera direction (no Y component)
            Vector3 horizontalDir = new Vector3(direction);
            horizontalDir.y = 0;  // Remove Y component for horizontal movement
            horizontalDir.nor();
            
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
            
            // Vertical movement in fly mode (Space/Shift only)
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                moveDir.y += 1f;  // Ascend
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                moveDir.y -= 1f;  // Descend
            }
            
            if (moveDir.len() > 0.001f) {
                moveDir.nor().scl(OpenCraftPlayer.FLY_SPEED);
                player.getVelocity().set(moveDir);
            } else {
                player.getVelocity().set(0, 0, 0);
            }
            
            // Fly mode with collision detection (gravity disabled via gravityEnabled flag)
            physicsSystem.update(player, delta);
        } else {
            // === NORMAL MODE ===
            // Handle jump
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                physicsSystem.tryJump(player);
            }
            
            // Horizontal movement only
            Vector3 horizontalDir = new Vector3(direction);
            horizontalDir.y = 0;
            horizontalDir.nor();

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

            if (moveDir.len() > 0.001f) {
                moveDir.nor().scl(moveSpeed);
                player.getVelocity().x = moveDir.x;
                player.getVelocity().z = moveDir.z;
            } else {
                player.getVelocity().x = 0;
                player.getVelocity().z = 0;
            }

            // Physics update (gravity, collision, etc.)
            physicsSystem.update(player, delta);
        }
        
        updateCameraPosition();
        camera.update();
    }
    
    public OpenCraftPlayer getOpenCraftPlayer() {
        return openCraftPlayer;
    }
}
