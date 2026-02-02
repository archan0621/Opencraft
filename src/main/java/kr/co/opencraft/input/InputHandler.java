package kr.co.opencraft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;
import kr.co.voxelite.engine.VoxeliteEngine;
import kr.co.voxelite.physics.RaycastHit;
import kr.co.opencraft.world.BlockTypes;
import kr.co.opencraft.entity.OpenCraftPlayer;

/**
 * Handles player input for block interactions and gameplay controls
 */
public class InputHandler {
    
    private final VoxeliteEngine engine;
    private final OpenCraftPlayer player;
    
    // Double-tap detection for fly mode toggle
    private static final float DOUBLE_TAP_TIME = 0.3f;  // 300ms window
    private float lastSpaceTapTime = -1f;
    private float timeSinceLastTap = 0f;
    
    public InputHandler(VoxeliteEngine engine, OpenCraftPlayer player) {
        this.engine = engine;
        this.player = player;
    }
    
    /**
     * Process all input events
     */
    public void handleInput(float delta) {
        // Handle fly mode toggle
        handleFlyModeToggle(delta);
        
        // Auto-disable fly mode when landing
        handleFlyModeLanding();
        
        // Check mouse button input
        if (Gdx.input.justTouched()) {
            int button = getMouseButton();
            processMouseInput(button);
        }
    }
    
    /**
     * Handle fly mode toggle with double-tap detection
     */
    private void handleFlyModeToggle(float delta) {
        timeSinceLastTap += delta;
        
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            if (timeSinceLastTap <= DOUBLE_TAP_TIME && lastSpaceTapTime >= 0) {
                // Double tap detected - toggle fly mode
                player.toggleFlyMode();
                System.out.println("[InputHandler] Fly mode: " + (player.isFlyMode() ? "ON" : "OFF"));
                lastSpaceTapTime = -1f;  // Reset
            } else {
                // First tap - record time
                lastSpaceTapTime = timeSinceLastTap;
                timeSinceLastTap = 0f;
            }
        }
    }
    
    /**
     * Auto-disable fly mode when landing (game-specific logic)
     */
    private void handleFlyModeLanding() {
        if (player.isFlyMode() && player.isOnGround()) {
            player.onLanding();
        }
    }
    
    /**
     * Check if fly mode double-tap should trigger jump
     * Called by CameraController to handle jump logic
     */
    public boolean shouldTriggerJump() {
        // Jump only if not in fly mode and not a double-tap
        return !player.isFlyMode() && 
               (timeSinceLastTap > DOUBLE_TAP_TIME || lastSpaceTapTime < 0);
    }
    
    /**
     * Get currently pressed mouse button
     */
    private int getMouseButton() {
        if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
            return Buttons.LEFT;
        }
        if (Gdx.input.isButtonJustPressed(Buttons.RIGHT)) {
            return Buttons.RIGHT;
        }
        if (Gdx.input.isButtonJustPressed(Buttons.MIDDLE)) {
            return Buttons.MIDDLE;
        }
        return -1;
    }
    
    /**
     * Process mouse button input using switch statement
     */
    private void processMouseInput(int button) {
        switch (button) {
            case Buttons.LEFT:
                handleBlockDestruction();
                break;
                
            case Buttons.RIGHT:
                handleBlockPlacement();
                break;
                
            case Buttons.MIDDLE:
                // Reserved for future use (e.g., block picker)
                break;
                
            default:
                // Unknown button
                break;
        }
    }
    
    /**
     * Handle block destruction (left-click)
     */
    private void handleBlockDestruction() {
        Vector3 selectedBlock = engine.getSelectedBlock();
        if (selectedBlock != null) {
            engine.removeBlock(selectedBlock);
        }
    }
    
    /**
     * Handle block placement (right-click)
     */
    private void handleBlockPlacement() {
        RaycastHit hit = engine.getRaycastHit();
        if (hit != null) {
            Vector3 placePos = hit.getPlacementPosition();
            
            if (!wouldCollideWithPlayer(placePos)) {
                engine.addBlock(placePos, BlockTypes.ORIGIN_STONE);
            } else {
                System.out.println("Cannot place block: would collide with player");
            }
        }
    }
    
    /**
     * Check if a block at the given position would collide with the player
     */
    private boolean wouldCollideWithPlayer(Vector3 blockPos) {
        Vector3 playerPos = player.getPosition();
        
        // Player AABB (width 0.6, height 1.8)
        float playerMinX = playerPos.x - 0.3f;
        float playerMaxX = playerPos.x + 0.3f;
        float playerMinY = playerPos.y;
        float playerMaxY = playerPos.y + 1.8f;
        float playerMinZ = playerPos.z - 0.3f;
        float playerMaxZ = playerPos.z + 0.3f;
        
        // Block AABB (1x1x1 block, center at blockPos)
        float blockMinX = blockPos.x - 0.5f;
        float blockMaxX = blockPos.x + 0.5f;
        float blockMinY = blockPos.y - 0.5f;
        float blockMaxY = blockPos.y + 0.5f;
        float blockMinZ = blockPos.z - 0.5f;
        float blockMaxZ = blockPos.z + 0.5f;
        
        return !(playerMaxX <= blockMinX || playerMinX >= blockMaxX ||
                 playerMaxY <= blockMinY || playerMinY >= blockMaxY ||
                 playerMaxZ <= blockMinZ || playerMinZ >= blockMaxZ);
    }
}
