package kr.co.opencraft.entity;

import com.badlogic.gdx.math.Vector3;
import kr.co.voxelite.entity.Player;

/**
 * OpenCraft-specific Player implementation with fly mode support.
 * Extends Voxelite engine's base Player with game-specific features.
 */
public class OpenCraftPlayer extends Player {
    private boolean flyMode = false;
    
    public static final float FLY_SPEED = 8f;
    
    public OpenCraftPlayer(Vector3 startPosition) {
        super(startPosition);
    }
    
    /**
     * Check if fly mode is enabled.
     */
    public boolean isFlyMode() {
        return flyMode;
    }
    
    /**
     * Set fly mode state.
     * When entering fly mode, disables gravity and resets vertical velocity.
     */
    public void setFlyMode(boolean flyMode) {
        this.flyMode = flyMode;
        
        // Engine integration: disable gravity via engine's extension point
        setGravityEnabled(!flyMode);
        
        if (flyMode) {
            // Reset vertical velocity when entering fly mode
            getVelocity().y = 0;
        }
    }
    
    /**
     * Toggle fly mode on/off.
     */
    public void toggleFlyMode() {
        setFlyMode(!flyMode);
    }
    
    /**
     * Auto-disable fly mode when landing (called by app logic).
     */
    public void onLanding() {
        if (flyMode) {
            setFlyMode(false);
            System.out.println("[OpenCraftPlayer] Fly mode disabled: landed on ground");
        }
    }
}
