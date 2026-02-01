package kr.co.opencraft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;
import kr.co.voxelite.engine.VoxeliteEngine;
import kr.co.voxelite.physics.RaycastHit;
import kr.co.opencraft.world.BlockTypes;

/**
 * Handles player input for block interactions
 */
public class InputHandler {
    
    private final VoxeliteEngine engine;
    
    public InputHandler(VoxeliteEngine engine) {
        this.engine = engine;
    }
    
    /**
     * Process all input events
     */
    public void handleInput() {
        // Check mouse button input
        if (Gdx.input.justTouched()) {
            int button = getMouseButton();
            processMouseInput(button);
        }
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
        Vector3 playerPos = engine.getPlayer().getPosition();
        
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
