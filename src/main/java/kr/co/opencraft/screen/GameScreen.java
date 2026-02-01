package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.world.BlockTypes;
import kr.co.opencraft.input.InputHandler;
import kr.co.voxelite.engine.VoxeliteEngine;

public class GameScreen implements Screen {
    private final OpenCraftGame game;
    private VoxeliteEngine engine;
    private InputHandler inputHandler;

    public GameScreen(OpenCraftGame game, VoxeliteEngine engine) {
        this.game = game;
        this.engine = engine;
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
        
        // Initialize input handler
        inputHandler = new InputHandler(engine);
        
        System.out.println("[GameScreen] Initialized with flat ground!");
    }

    @Override
    public void render(float delta) {
        engine.update(delta);
        inputHandler.handleInput();
        engine.render();
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
