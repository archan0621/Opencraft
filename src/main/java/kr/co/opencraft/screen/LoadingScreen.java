package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.world.BlockTypes;
import kr.co.opencraft.world.*;
import kr.co.voxelite.engine.VoxeliteEngine;
import kr.co.opencraft.entity.OpenCraftPlayer;
import com.badlogic.gdx.math.Vector3;

public class LoadingScreen implements Screen {

    private final OpenCraftGame game;
    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    
    private static final float BAR_WIDTH = 400f;
    private static final float BAR_HEIGHT = 30f;
    private static final float MIN_LOADING_TIME = 1f; // 최소 로딩 시간 1초

    private volatile float progress = 0f;
    private volatile boolean loadingComplete = false;
    private VoxeliteEngine engine;
    private OpenCraftPlayer player;
    private float elapsedTime = 0f;

    public LoadingScreen(OpenCraftGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("LoadingScreen show");

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.classpath("texture/uiskin.json"));
        shapeRenderer = new ShapeRenderer();

        Label loadingLabel = new Label("Building terrain...", skin);
        loadingLabel.setFontScale(1.5f);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(loadingLabel);

        stage.addActor(table);

        // 백그라운드 스레드에서 월드 생성
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                progress = 0.2f;
                
                // === 읽기 쉬운 애플리케이션 코드 ===
                
                // 1. 월드 설정
                long seed = System.currentTimeMillis();
                String worldPath = "saves/world1";
                
                // 2. 지형 생성 정책 (애플리케이션이 결정)
                TerrainGenerator terrainGenerator = new TerrainGenerator(seed);
                TerrainGeneratorAdapter generatorAdapter = new TerrainGeneratorAdapter(terrainGenerator);
                
                // 3. 청크 로딩 정책 (애플리케이션이 결정)
                ChunkLoadPolicy loadPolicy = new ChunkLoadPolicy(
                    3,  // 렌더 거리: 3청크
                    5   // 사전 생성: 5청크 (렌더 거리 + 2)
                );
                ChunkLoadPolicyAdapter policyAdapter = new ChunkLoadPolicyAdapter(loadPolicy);
                
                // 4. 게임별 플레이어 생성 (fly mode 지원)
                player = new OpenCraftPlayer(new Vector3(0f, 0.5f, 0f));
                
                // 5. 엔진 생성 (정책 주입 + 커스텀 플레이어)
                engine = VoxeliteEngine.builder(player)
                    .textureAtlasPath("texture/block.png")
                    .playerStart(0f, 0.5f, 0f)
                    .playerSpeed(5f)
                    .cameraPitch(-20f)
                    .autoCreateGround(true)
                    .worldSavePath(worldPath)
                    .chunkGenerator(generatorAdapter)
                    .chunkLoadPolicy(policyAdapter)
                    .initialChunkRadius(16)   // 초기 16청크 반경 생성
                    .chunkPreloadRadius(1)    // 그 중 1청크만 메모리 로드
                    .defaultGroundBlockType(BlockTypes.ORIGIN_STONE)  // 정상 땅 블록
                    .build();
                
                System.out.println("[LoadingScreen] World created with seed: " + seed);
                
                progress = 0.9f;
                
                // 최소 로딩 시간 보장
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = (long)(MIN_LOADING_TIME * 1000) - elapsed;
                if (remaining > 0) {
                    Thread.sleep(remaining);
                }
                
                progress = 1f;
                loadingComplete = true;
                
                System.out.println("[LoadingScreen] World generation complete");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;
        
        // 로딩 완료 및 최소 시간 경과 시 GameScreen으로 전환
        if (loadingComplete && elapsedTime >= MIN_LOADING_TIME) {
            game.setScreen(new GameScreen(game, engine, player));
            return;
        }

        // 화면 클리어
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 텍스트 렌더링
        stage.act(delta);
        stage.draw();

        // 로딩바 렌더링
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f - 50f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 로딩바 배경 (어두운 회색)
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(centerX - BAR_WIDTH / 2f, centerY, BAR_WIDTH, BAR_HEIGHT);
        
        // 로딩바 진행 (밝은 초록색)
        shapeRenderer.setColor(0.3f, 0.8f, 0.3f, 1);
        shapeRenderer.rect(centerX - BAR_WIDTH / 2f, centerY, BAR_WIDTH * progress, BAR_HEIGHT);
        
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();
    }
}
