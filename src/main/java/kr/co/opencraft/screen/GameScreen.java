package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import kr.co.opencraft.camera.CameraController;
import kr.co.opencraft.camera.FPSCamera;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.entity.Player;
import kr.co.opencraft.input.InputHandler;
import kr.co.opencraft.physics.CollisionDetector;
import kr.co.opencraft.physics.PhysicsSystem;
import kr.co.opencraft.physics.RayCaster;
import kr.co.opencraft.render.Renderer;
import kr.co.opencraft.world.BlockManager;
import kr.co.opencraft.world.World;

public class GameScreen implements Screen {

    private FPSCamera fpsCamera;
    private Player player;
    private CameraController cameraController;
    private World world;
    private PhysicsSystem physicsSystem;
    private InputHandler inputHandler;
    private Renderer renderer;
    
    private Vector3 selectedBlock; // 현재 선택된 블록

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

        // 월드 생성
        BlockManager blockManager = new BlockManager();
        world = new World(blockManager);
        world.addFlatGround(11, 1f, -1f); // 11x11 평평한 땅 (y=-1)
        
        // 2칸 계단 만들기
        // 11x11 그리드: -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5 (정수 좌표!)
        world.addBlock(new Vector3(0f, 0f, 3f)); // 1단 계단 (높이 1블록)
        world.addBlock(new Vector3(0f, 1f, 2f)); // 2단 계단 (높이 2블록)

        // 플레이어 생성 (발 위치 기준)
        // 땅 블록 중심: y=-1, 윗면: y=-0.5
        // 플레이어는 땅 윗면(y=-0.5)에서 시작
        // 블록 범위: x=-5~5, z=-5~5 → 스폰은 z=0 (그리드 중앙)
        Vector3 playerStartPos = new Vector3(0f, -0.5f, 0f);
        player = new Player(playerStartPos);

        // 카메라 초기화
        fpsCamera = new FPSCamera(67f, logicalWidth, logicalHeight);
        fpsCamera.setPitch(-20f); // 약간 아래를 보도록

        // 시스템 초기화
        physicsSystem = new PhysicsSystem(world);
        inputHandler = new InputHandler();
        cameraController = new CameraController(fpsCamera, player, physicsSystem, inputHandler);
        renderer = new Renderer(logicalWidth, logicalHeight);
    }

    @Override
    public void render(float delta) {
        // 입력 처리
        inputHandler.update(delta);

        // 카메라 업데이트
        cameraController.update(delta);

        // 레이캐스팅으로 선택된 블록 찾기
        Ray ray = fpsCamera.getCamera().getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        selectedBlock = RayCaster.raycast(ray, world);

        // 렌더링
        int logicalWidth = Gdx.graphics.getWidth();
        int logicalHeight = Gdx.graphics.getHeight();
        renderer.render(fpsCamera, world, logicalWidth, logicalHeight, selectedBlock);
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
