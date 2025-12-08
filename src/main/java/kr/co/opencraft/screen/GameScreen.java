package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import kr.co.opencraft.camera.CameraController;
import kr.co.opencraft.camera.FPSCamera;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.input.InputHandler;
import kr.co.opencraft.physics.CollisionDetector;
import kr.co.opencraft.render.Renderer;
import kr.co.opencraft.world.BlockManager;
import kr.co.opencraft.world.World;

public class GameScreen implements Screen {

    private FPSCamera fpsCamera;
    private CameraController cameraController;
    private World world;
    private CollisionDetector collisionDetector;
    private InputHandler inputHandler;
    private Renderer renderer;

    private final OpenCraftGame game;

    public GameScreen(OpenCraftGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("[GameScreen] show()");

        // 이전 화면의 InputProcessor 제거
        Gdx.input.setInputProcessor(null);

        // OpenGL 설정
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        // 화면 크기 정보
        int logicalWidth = Gdx.graphics.getWidth();
        int logicalHeight = Gdx.graphics.getHeight();
        int backBufferWidth = Gdx.graphics.getBackBufferWidth();
        int backBufferHeight = Gdx.graphics.getBackBufferHeight();

        System.out.println("[GameScreen] Logical size: " + logicalWidth + "x" + logicalHeight);
        System.out.println("[GameScreen] Back buffer size: " + backBufferWidth + "x" + backBufferHeight);

        // 뷰포트 설정 (Retina 대응)
        Gdx.gl.glViewport(0, 0, backBufferWidth, backBufferHeight);

        // 컴포넌트 초기화
        fpsCamera = new FPSCamera(67f, logicalWidth, logicalHeight);
        fpsCamera.setPosition(0f, 0f, 5f);

        BlockManager blockManager = new BlockManager();
        world = new World(blockManager);
        world.createStaircaseGround(5, 1f, 1f); // 5x5 계단식 땅

        collisionDetector = new CollisionDetector(world);
        inputHandler = new InputHandler();
        cameraController = new CameraController(fpsCamera, collisionDetector, inputHandler);
        renderer = new Renderer(logicalWidth, logicalHeight);
    }

    @Override
    public void render(float delta) {
        // 입력 처리
        inputHandler.update(delta);

        // 카메라 업데이트
        cameraController.update(delta);

        // 렌더링
        int logicalWidth = Gdx.graphics.getWidth();
        int logicalHeight = Gdx.graphics.getHeight();
        renderer.render(fpsCamera, world, logicalWidth, logicalHeight);
    }

    @Override
    public void resize(int width, int height) {
        if (fpsCamera != null) {
            fpsCamera.resize(width, height);
        }
        if (renderer != null) {
            renderer.resize(width, height);
        }

        // 뷰포트는 실제 백버퍼 크기 사용 (Retina 대응)
        int backBufferWidth = Gdx.graphics.getBackBufferWidth();
        int backBufferHeight = Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glViewport(0, 0, backBufferWidth, backBufferHeight);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (renderer != null) renderer.dispose();
        if (world != null) world.dispose();
    }
}
