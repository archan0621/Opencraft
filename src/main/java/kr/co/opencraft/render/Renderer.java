package kr.co.opencraft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import kr.co.opencraft.camera.FPSCamera;
import kr.co.opencraft.world.World;

/**
 * 렌더링 통합 관리
 */
public class Renderer {
    private BlockRenderer blockRenderer;
    private CrosshairRenderer crosshairRenderer;

    public Renderer(int screenWidth, int screenHeight) {
        blockRenderer = new BlockRenderer();
        crosshairRenderer = new CrosshairRenderer(screenWidth, screenHeight);
    }

    public void render(FPSCamera fpsCamera, World world, int logicalWidth, int logicalHeight) {
        // 뷰포트 설정 (Retina 대응)
        int backBufferWidth = Gdx.graphics.getBackBufferWidth();
        int backBufferHeight = Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glViewport(0, 0, backBufferWidth, backBufferHeight);

        // 화면 지우기
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 카메라 뷰포트 업데이트
        fpsCamera.resize(logicalWidth, logicalHeight);

        // 블록 렌더링
        blockRenderer.render(fpsCamera.getCamera(), world);

        // 십자선 렌더링
        crosshairRenderer.render(logicalWidth, logicalHeight);
    }

    public void resize(int width, int height) {
        crosshairRenderer.resize(width, height);
    }

    public void dispose() {
        if (blockRenderer != null) blockRenderer.dispose();
        if (crosshairRenderer != null) crosshairRenderer.dispose();
    }
}

