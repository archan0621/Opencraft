package kr.co.opencraft.render;

import com.badlogic.gdx.math.Vector3;

public class RemotePlayerState {
    private final int playerId;
    private String username;
    private final Vector3 renderPosition = new Vector3();
    private final Vector3 targetPosition = new Vector3();

    public RemotePlayerState(int playerId, String username, Vector3 initialPosition) {
        this.playerId = playerId;
        this.username = username;
        renderPosition.set(initialPosition);
        targetPosition.set(initialPosition);
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
    }

    public Vector3 getRenderPosition() {
        return new Vector3(renderPosition);
    }

    public Vector3 copyRenderPosition() {
        return new Vector3(renderPosition);
    }

    public void setTargetPosition(Vector3 position) {
        if (position != null) {
            targetPosition.set(position);
        }
    }

    public void update(float delta) {
        float alpha = Math.min(1f, Math.max(0f, delta * 10f));
        renderPosition.lerp(targetPosition, alpha);
    }
}
