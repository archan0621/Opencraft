package kr.co.opencraft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Collection;
import kr.co.voxelient.camera.FPSCamera;

public class RemotePlayerRenderer {
    private static final float PLAYER_HALF_WIDTH = 0.3f;
    private static final float PLAYER_HEIGHT = 1.8f;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    public void render(FPSCamera camera, Collection<RemotePlayerState> remotePlayers) {
        if (camera == null || remotePlayers == null || remotePlayers.isEmpty()) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        shapeRenderer.setProjectionMatrix(camera.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.95f, 0.35f, 0.15f, 1f);
        for (RemotePlayerState remotePlayer : remotePlayers) {
            var position = remotePlayer.copyRenderPosition();
            float minX = position.x - PLAYER_HALF_WIDTH;
            float minY = position.y;
            float minZ = position.z - PLAYER_HALF_WIDTH;
            shapeRenderer.box(minX, minY, minZ, PLAYER_HALF_WIDTH * 2f, PLAYER_HEIGHT, PLAYER_HALF_WIDTH * 2f);
        }
        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
