package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.world.BlockTypes;
import kr.co.opencraft.input.InputHandler;
import kr.co.voxelite.engine.VoxeliteEngine;
import kr.co.opencraft.entity.OpenCraftPlayer;
import kr.co.opencraft.camera.OpenCraftCameraController;
import kr.co.voxelite.util.PerformanceLogger;

public class GameScreen implements Screen {
    private final OpenCraftGame game;
    private VoxeliteEngine engine;
    private OpenCraftPlayer player;
    private InputHandler inputHandler;
    private OpenCraftCameraController cameraController;

    public GameScreen(OpenCraftGame game, VoxeliteEngine engine, OpenCraftPlayer player) {
        this.game = game;
        this.engine = engine;
        this.player = player;
    }

    @Override
    public void show() {
        System.out.println("[GameScreen] show()");

        // Remove previous InputProcessor
        Gdx.input.setInputProcessor(null);

        // Initialize engine (이미 빌드된 엔진 초기화)
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        engine.initialize(width, height);
        
        // Initialize input handler with OpenCraft player
        inputHandler = new InputHandler(engine, player);
        
        // Replace engine's camera controller with OpenCraft-specific controller
        cameraController = new OpenCraftCameraController(
            engine.getCamera(), 
            player, 
            engine.getPhysics(), 
            engine.getInput()
        );
        cameraController.setMoveSpeed(5f);
        
        // Inject custom camera controller into engine
        engine.setCameraController(cameraController);
        
        System.out.println("[GameScreen] Initialized with OpenCraft camera controller!");
    }

    @Override
    public void render(float delta) {
        long frameStart = PerformanceLogger.now();
        
        engine.update(delta);
        long afterUpdate = PerformanceLogger.now();
        
        inputHandler.handleInput(delta);  // Pass delta for timing
        long afterInput = PerformanceLogger.now();
        
        engine.render();
        long afterRender = PerformanceLogger.now();
        
        int frame = PerformanceLogger.tickFrame();
        if (PerformanceLogger.ENABLED) {
            long totalMs = afterRender - frameStart;
            long updateMs = afterUpdate - frameStart;
            long inputMs = afterInput - afterUpdate;
            long renderMs = afterRender - afterInput;
            int fps = Gdx.graphics.getFramesPerSecond();
            // Log every frame if slow (>16ms), else every LOG_INTERVAL frames
            boolean slow = totalMs > 16;
            boolean interval = (frame % PerformanceLogger.LOG_INTERVAL) == 0;
            if (slow || interval) {
                Runtime rt = Runtime.getRuntime();
                long usedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
                System.out.printf("[PERF][Frame] total=%dms update=%dms input=%dms render=%dms delta=%.3f fps=%d mem=%dMB%s%n",
                    totalMs, updateMs, inputMs, renderMs, delta, fps, usedMB, slow ? " [SLOW]" : "");
            }
        }
        PerformanceLogger.tickFrame();
    }

    @Override
    public void resize(int width, int height) {
        if (engine != null && engine.isInitialized()) {
            engine.resize(width, height);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (engine != null) {
            engine.dispose();
        }
    }
}
