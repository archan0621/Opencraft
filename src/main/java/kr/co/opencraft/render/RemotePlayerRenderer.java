package kr.co.opencraft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import java.util.Collection;
import kr.co.voxelite.entity.Player;
import kr.co.voxelient.camera.FPSCamera;

public class RemotePlayerRenderer {
    private static final float MODEL_HEIGHT = Player.HEIGHT;
    private static final float MODEL_SCALE = MODEL_HEIGHT / 32f;
    private static final float HEAD_SIZE = 8f * MODEL_SCALE;
    private static final float BODY_WIDTH = 8f * MODEL_SCALE;
    private static final float BODY_HEIGHT = 12f * MODEL_SCALE;
    private static final float BODY_DEPTH = 4f * MODEL_SCALE;
    private static final float ARM_WIDTH = 4f * MODEL_SCALE;
    private static final float ARM_HEIGHT = 12f * MODEL_SCALE;
    private static final float ARM_DEPTH = 4f * MODEL_SCALE;
    private static final float LEG_WIDTH = 4f * MODEL_SCALE;
    private static final float LEG_HEIGHT = 12f * MODEL_SCALE;
    private static final float LEG_DEPTH = 4f * MODEL_SCALE;
    private static final float NAME_TAG_PADDING = 10f;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private final Vector3 projectedCorner = new Vector3();
    private final Vector3 projectedCenter = new Vector3();

    public RemotePlayerRenderer() {
        font.getData().setScale(1.8f);
        font.setUseIntegerPositions(false);
    }

    public void render(FPSCamera camera, Collection<RemotePlayerState> remotePlayers) {
        if (camera == null || remotePlayers == null || remotePlayers.isEmpty()) {
            return;
        }

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        shapeRenderer.setProjectionMatrix(camera.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.78f, 0.32f, 1f);
        for (RemotePlayerState remotePlayer : remotePlayers) {
            drawPlayerModel(remotePlayer.copyRenderPosition());
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        spriteBatch.begin();
        for (RemotePlayerState remotePlayer : remotePlayers) {
            String username = remotePlayer.getUsername();
            if (username == null || username.isBlank()) {
                continue;
            }

            Vector3 position = remotePlayer.copyRenderPosition();
            ScreenAnchor anchor = projectTopCenter(camera, position);
            if (anchor == null) {
                continue;
            }

            glyphLayout.setText(font, username);
            float drawX = anchor.centerX() - glyphLayout.width / 2f;
            float drawY = anchor.topY() + glyphLayout.height + NAME_TAG_PADDING;
            font.draw(spriteBatch, glyphLayout, drawX, drawY);
        }
        spriteBatch.end();
    }

    private ScreenAnchor projectTopCenter(FPSCamera camera, Vector3 position) {
        float topY = Float.NEGATIVE_INFINITY;
        boolean hasVisibleCorner = false;

        projectedCenter.set(position.x, position.y + MODEL_HEIGHT, position.z);
        camera.getCamera().project(projectedCenter);
        if (projectedCenter.z < 0f || projectedCenter.z > 1f) {
            return null;
        }

        float halfHead = HEAD_SIZE / 2f;
        float[] xOffsets = {-halfHead, halfHead};
        float[] zOffsets = {-halfHead, halfHead};
        for (float xOffset : xOffsets) {
            for (float zOffset : zOffsets) {
                projectedCorner.set(position.x + xOffset, position.y + MODEL_HEIGHT, position.z + zOffset);
                camera.getCamera().project(projectedCorner);
                if (projectedCorner.z < 0f || projectedCorner.z > 1f) {
                    continue;
                }

                hasVisibleCorner = true;
                topY = Math.max(topY, projectedCorner.y);
            }
        }

        if (!hasVisibleCorner) {
            return null;
        }

        return new ScreenAnchor(projectedCenter.x, topY);
    }

    private void drawPlayerModel(Vector3 position) {
        float centerX = position.x;
        float baseY = position.y;
        float centerZ = position.z;

        float legOffsetX = LEG_WIDTH / 2f;
        drawCenteredBox(centerX - legOffsetX, baseY, centerZ, LEG_WIDTH, LEG_HEIGHT, LEG_DEPTH);
        drawCenteredBox(centerX + legOffsetX, baseY, centerZ, LEG_WIDTH, LEG_HEIGHT, LEG_DEPTH);

        float bodyBaseY = baseY + LEG_HEIGHT;
        drawCenteredBox(centerX, bodyBaseY, centerZ, BODY_WIDTH, BODY_HEIGHT, BODY_DEPTH);

        float armBaseY = bodyBaseY;
        float armOffsetX = BODY_WIDTH / 2f + ARM_WIDTH / 2f;
        drawCenteredBox(centerX - armOffsetX, armBaseY, centerZ, ARM_WIDTH, ARM_HEIGHT, ARM_DEPTH);
        drawCenteredBox(centerX + armOffsetX, armBaseY, centerZ, ARM_WIDTH, ARM_HEIGHT, ARM_DEPTH);

        float headBaseY = bodyBaseY + BODY_HEIGHT;
        drawCenteredBox(centerX, headBaseY, centerZ, HEAD_SIZE, HEAD_SIZE, HEAD_SIZE);
    }

    private void drawCenteredBox(float centerX, float minY, float centerZ, float width, float height, float depth) {
        float minX = centerX - width / 2f;
        // ShapeRenderer.box flips depth internally, so z must start at the positive side to stay centered.
        float minZ = centerZ + depth / 2f;
        shapeRenderer.box(minX, minY, minZ, width, height, depth);
    }

    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }

    private record ScreenAnchor(float centerX, float topY) {
    }
}
