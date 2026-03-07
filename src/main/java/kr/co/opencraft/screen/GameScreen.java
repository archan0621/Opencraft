package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.input.InputHandler;
import kr.co.voxelite.engine.VoxeliteEngine;
import kr.co.voxelient.engine.VoxelientEngine;
import kr.co.opencraft.entity.OpenCraftPlayer;
import kr.co.opencraft.camera.OpenCraftCameraController;
import kr.co.opencraft.world.BlockTextureProvider;
import kr.co.voxelite.util.PerformanceLogger;

public class GameScreen implements Screen {
    private final OpenCraftGame game;
    private final VoxeliteEngine coreEngine;
    private OpenCraftPlayer player;
    private VoxelientEngine clientEngine;
    private InputHandler inputHandler;
    private OpenCraftCameraController cameraController;

    public GameScreen(OpenCraftGame game, VoxeliteEngine engine, OpenCraftPlayer player) {
        this.game = game;
        this.coreEngine = engine;
        this.player = player;
    }

    @Override
    public void show() {
        System.out.println("[GameScreen] show()");

        // Remove previous InputProcessor
        Gdx.input.setInputProcessor(null);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        clientEngine = VoxelientEngine.builder(coreEngine)
            .textureAtlasPath("texture/block.png")
            .textureProvider(new BlockTextureProvider())
            .playerSpeed(5f)
            .cameraPitch(-20f)
            .cameraFar(144f)
            .build();
        clientEngine.initialize(width, height);

        inputHandler = new InputHandler(clientEngine, player);
        
        cameraController = new OpenCraftCameraController(
            clientEngine.getCamera(),
            player, 
            coreEngine.getPhysics(),
            clientEngine.getInput()
        );
        cameraController.setMoveSpeed(5f);

        clientEngine.setCameraController(cameraController);

        System.out.println("[GameScreen] Initialized with OpenCraft camera controller!");
    }

    @Override
    public void render(float delta) {
        long frameStart = PerformanceLogger.now();
        
        clientEngine.update(delta);
        long afterUpdate = PerformanceLogger.now();
        
        inputHandler.handleInput(delta);  // Pass delta for timing
        long afterInput = PerformanceLogger.now();
        
        clientEngine.render();
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
    }

    @Override
    public void resize(int width, int height) {
        if (clientEngine != null && clientEngine.isInitialized()) {
            clientEngine.resize(width, height);
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
        if (clientEngine != null) {
            clientEngine.dispose();
        }
        if (coreEngine != null) {
            coreEngine.dispose();
        }
    }
}
