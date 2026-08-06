package kr.co.opencraft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.entity.OpenCraftPlayer;
import kr.co.opencraft.ui.Hotbar;
import kr.co.voxelient.engine.VoxelientEngine;
import kr.co.voxelite.physics.RaycastHit;

/**
 * Handles player input for block interactions and gameplay controls
 */
public class InputHandler extends InputAdapter {
    private final VoxelientEngine engine;
    private final OpenCraftPlayer player;
    private final Hotbar hotbar;
    
    // Double-tap detection for fly mode toggle
    private static final float DOUBLE_TAP_TIME = 0.3f;  // 300ms window
    private float lastSpaceTapTime = -1f;
    private float timeSinceLastTap = 0f;
    public InputHandler(VoxelientEngine engine, OpenCraftPlayer player, Hotbar hotbar) {
        this.engine = engine;
        this.player = player;
        this.hotbar = hotbar;
    }
    
    /**
     * Process all input events
     */
    public void handleInput(float delta) {
        // Handle fly mode toggle
        handleFlyModeToggle(delta);
        
        // Auto-disable fly mode when landing
        handleFlyModeLanding();

        handleBlockSelection();
        
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

    private void handleBlockSelection() {
        for (int i = 0; i < Math.min(hotbar.size(), 9); i++) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_1 + i)) {
                hotbar.selectSlot(i);
                logSelectedBlock();
                return;
            }
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (amountY == 0f) {
            return false;
        }
        hotbar.scroll(amountY > 0f ? 1 : -1);
        logSelectedBlock();
        return true;
    }

    private void logSelectedBlock() {
        System.out.println("[InputHandler] Selected block: " + hotbar.getSelectedBlock().displayName());
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
            int selectedBlockType = hotbar.getSelectedBlock().typeId();
            
            if (!wouldCollideWithPlayer(placePos, selectedBlockType)) {
                engine.addBlock(placePos, selectedBlockType);
            } else {
                System.out.println("Cannot place block: would collide with player");
            }
        }
    }
    
    /**
     * Check if a block at the given position would collide with the player
     */
    private boolean wouldCollideWithPlayer(Vector3 blockPos, int blockType) {
        return engine.getCoreEngine().getWorld().getBlockManager().isSolid(blockType)
            && player.collidesWithBlock(blockPos);
    }
}
