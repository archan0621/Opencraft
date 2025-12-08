package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.engine.OpenCraftGame;

public class GameScreen implements Screen {

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private Model cubeModel;
    private ModelInstance cubeInstance;

    private final OpenCraftGame game;

    // FPS 카메라 변수
    private float yaw = 0f;      // 좌우 회전 (수평)
    private float pitch = 0f;    // 상하 회전 (수직)
    private float moveSpeed = 5f; // 이동 속도
    private float mouseSensitivity = 0.1f; // 마우스 감도

    // 마우스 커서 중앙 고정을 위한 변수
    private boolean mouseLocked = true;
    private boolean firstMouse = true;
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    public GameScreen(OpenCraftGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("[GameScreen] show()");

        // 깊이 버퍼 사용
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        // 논리적 화면 크기 (카메라용)
        int logicalWidth = Gdx.graphics.getWidth();
        int logicalHeight = Gdx.graphics.getHeight();
        
        // 실제 백버퍼 크기 (뷰포트용 - Retina 대응)
        int backBufferWidth = Gdx.graphics.getBackBufferWidth();
        int backBufferHeight = Gdx.graphics.getBackBufferHeight();
        
        System.out.println("[GameScreen] Logical size: " + logicalWidth + "x" + logicalHeight);
        System.out.println("[GameScreen] Back buffer size: " + backBufferWidth + "x" + backBufferHeight);
        
        // 뷰포트를 실제 백버퍼 크기로 설정 (Retina 대응)
        Gdx.gl.glViewport(0, 0, backBufferWidth, backBufferHeight);

        // 카메라는 논리적 크기 사용
        camera = new PerspectiveCamera(67f, logicalWidth, logicalHeight);
        camera.position.set(0f, 0f, 5f);   // 큐브 앞에서 시작
        camera.lookAt(0f, 0f, 0f);         // 큐브(원점)을 바라보게
        camera.near = 0.1f;
        camera.far = 100f;

        // 초기 카메라 방향 설정
        yaw = -90f; // 초기 yaw 값 (앞쪽을 바라봄)
        pitch = 0f;

        modelBatch = new ModelBatch();

        // 환경광 (밝기)
        environment = new Environment();
        environment.set(new ColorAttribute(
                ColorAttribute.AmbientLight,
                1f, 1f, 1f, 1f
        ));

        // 큐브 하나 만들기
        ModelBuilder builder = new ModelBuilder();
        cubeModel = builder.createBox(
                1f, 1f, 1f,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        cubeInstance = new ModelInstance(cubeModel);
        cubeInstance.transform.setToTranslation(new Vector3(0f, 0f, 0f)); // 원점에 놓기

        // 마우스 커서를 화면 중앙에 설정
        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;
        Gdx.input.setCursorPosition(centerX, centerY);
        lastMouseX = centerX;
        lastMouseY = centerY;
        
        // 마우스 커서 잠금 시도 (지원되는 경우)
        try {
            Gdx.input.setCursorCatched(true);
        } catch (Exception e) {
            // setCursorCatched가 지원되지 않는 경우 무시
            System.out.println("setCursorCatched not supported, using manual cursor control");
        }
    }

    private void updateCamera(float delta) {
        // 마우스 입력 처리
        if (mouseLocked) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();

            if (firstMouse) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                firstMouse = false;
            }

            int deltaX = mouseX - lastMouseX;
            int deltaY = lastMouseY - mouseY; // Y축은 반대 방향

            yaw += deltaX * mouseSensitivity;
            pitch += deltaY * mouseSensitivity;

            // pitch 제한 (위아래 회전 제한)
            pitch = MathUtils.clamp(pitch, -89f, 89f);

            // 마우스 커서를 화면 중앙으로 리셋
            int centerX = Gdx.graphics.getWidth() / 2;
            int centerY = Gdx.graphics.getHeight() / 2;
            Gdx.input.setCursorPosition(centerX, centerY);
            lastMouseX = centerX;
            lastMouseY = centerY;
        }

        // 카메라 방향 벡터 계산
        Vector3 direction = new Vector3();
        direction.x = MathUtils.cosDeg(yaw) * MathUtils.cosDeg(pitch);
        direction.y = MathUtils.sinDeg(pitch);
        direction.z = MathUtils.sinDeg(yaw) * MathUtils.cosDeg(pitch);
        direction.nor();

        // WASD 이동 처리
        Vector3 moveDir = new Vector3();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveDir.add(direction);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveDir.sub(direction);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            // 오른쪽 벡터 계산 (카메라 방향과 위쪽 벡터의 외적)
            Vector3 right = new Vector3();
            Vector3 up = new Vector3(0, 1, 0);
            right.set(direction).crs(up).nor();
            moveDir.sub(right);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            // 오른쪽 벡터 계산
            Vector3 right = new Vector3();
            Vector3 up = new Vector3(0, 1, 0);
            right.set(direction).crs(up).nor();
            moveDir.add(right);
        }

        // Y축 이동 제거 (수평 이동만)
        moveDir.y = 0;
        moveDir.nor();

        // 카메라 위치 업데이트
        moveDir.scl(moveSpeed * delta);
        camera.position.add(moveDir);

        // 카메라가 바라볼 위치 계산
        Vector3 target = new Vector3(camera.position).add(direction);
        camera.lookAt(target);
        camera.up.set(0, 1, 0); // 위쪽 방향 유지
    }

    @Override
    public void render(float delta) {
        // 카메라 업데이트 (FPS 컨트롤)
        updateCamera(delta);
        
        // 카메라는 논리적 크기 사용
        int logicalWidth = Gdx.graphics.getWidth();
        int logicalHeight = Gdx.graphics.getHeight();
        camera.viewportWidth = logicalWidth;
        camera.viewportHeight = logicalHeight;
        camera.update();

        // 뷰포트는 실제 백버퍼 크기 사용 (Retina 대응)
        int backBufferWidth = Gdx.graphics.getBackBufferWidth();
        int backBufferHeight = Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glViewport(0, 0, backBufferWidth, backBufferHeight);
        
        // 화면 지우기 (배경색 진하게)
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 큐브 렌더링
        modelBatch.begin(camera);
        modelBatch.render(cubeInstance, environment);
        modelBatch.end();

        // ESC 키로 마우스 커서 잠금 해제
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            mouseLocked = false;
            firstMouse = true;
            try {
                Gdx.input.setCursorCatched(false);
            } catch (Exception e) {
                // 무시
            }
        }
        // 마우스 클릭으로 다시 잠금
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !mouseLocked) {
            mouseLocked = true;
            firstMouse = true;
            int centerX = Gdx.graphics.getWidth() / 2;
            int centerY = Gdx.graphics.getHeight() / 2;
            Gdx.input.setCursorPosition(centerX, centerY);
            lastMouseX = centerX;
            lastMouseY = centerY;
            try {
                Gdx.input.setCursorCatched(true);
            } catch (Exception e) {
                // 무시
            }
        }
    }

    @Override public void resize(int width, int height) {
        // width, height는 논리적 크기
        // 카메라는 논리적 크기 사용
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
        
        // 뷰포트는 실제 백버퍼 크기 사용 (Retina 대응)
        int backBufferWidth = Gdx.graphics.getBackBufferWidth();
        int backBufferHeight = Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glViewport(0, 0, backBufferWidth, backBufferHeight);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (modelBatch != null) modelBatch.dispose();
        if (cubeModel != null) cubeModel.dispose();
    }
}