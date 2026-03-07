package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.entity.OpenCraftPlayer;
import kr.co.opencraft.network.MultiplayerClient;
import kr.co.voxelite.engine.VoxeliteEngine;
import kr.co.voxelite.world.Chunk;
import kr.co.voxelite.world.IChunkLoadPolicy;
import kr.co.voxeliver.network.protocol.impl.LoginAcceptedPacket;

public class MultiplayerLoadingScreen implements Screen {
    private static final float BAR_WIDTH = 400f;
    private static final float BAR_HEIGHT = 30f;

    private final OpenCraftGame game;
    private final String host;
    private final int port;
    private final String username;

    private Stage stage;
    private Skin skin;
    private ShapeRenderer shapeRenderer;
    private Label statusLabel;

    private volatile float progress = 0f;
    private volatile boolean loadingComplete = false;
    private volatile String statusMessage = "Connecting to server...";
    private volatile String errorMessage = null;

    private MultiplayerClient multiplayerClient;
    private VoxeliteEngine engine;
    private OpenCraftPlayer player;
    private int localPlayerId;

    public MultiplayerLoadingScreen(OpenCraftGame game) {
        this(game, "127.0.0.1", 25565, System.getProperty("user.name", "player"));
    }

    public MultiplayerLoadingScreen(OpenCraftGame game, String host, int port, String username) {
        this.game = game;
        this.host = host;
        this.port = port;
        this.username = username;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.classpath("texture/uiskin.json"));
        shapeRenderer = new ShapeRenderer();

        statusLabel = new Label(statusMessage, skin);
        statusLabel.setFontScale(1.3f);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(statusLabel);
        stage.addActor(table);

        new Thread(this::connectAndPrepareWorld, "opencraft-multiplayer-connect").start();
    }

    private void connectAndPrepareWorld() {
        try {
            statusMessage = "Connecting to " + host + ":" + port + "...";
            progress = 0.2f;

            multiplayerClient = new MultiplayerClient(host, port);
            LoginAcceptedPacket loginAccepted = multiplayerClient.connectAndLogin(username, Duration.ofSeconds(5));

            statusMessage = "Preparing multiplayer client...";
            progress = 0.6f;

            Vector3 spawnPosition = loginAccepted.getSpawnPosition();
            localPlayerId = loginAccepted.getPlayerId();
            player = new OpenCraftPlayer(new Vector3(spawnPosition));

            engine = VoxeliteEngine.builder(player)
                .playerStart(spawnPosition.x, spawnPosition.y, spawnPosition.z)
                .autoCreateGround(false)
                .chunkGenerator((chunk, blockType) -> {
                })
                .chunkLoadPolicy(new IChunkLoadPolicy() {
                    @Override
                    public boolean shouldLoadToMemory(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
                        return false;
                    }

                    @Override
                    public boolean shouldKeepLoaded(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
                        return false;
                    }

                    @Override
                    public boolean shouldPregenerate(int chunkX, int chunkZ, int playerChunkX, int playerChunkZ) {
                        return false;
                    }

                    @Override
                    public int getMaxLoadedChunks() {
                        return 4096;
                    }
                })
                .initialChunkRadius(0)
                .chunkPreloadRadius(0)
                .worldSavePath(Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "opencraft-multiplayer-" + UUID.randomUUID()
                ).toString())
                .build();
            engine.initialize();
            player.setPosition(spawnPosition);

            progress = 1f;
            statusMessage = "Connected as " + username;
            loadingComplete = true;
        } catch (Exception e) {
            errorMessage = "Connection failed: " + e.getMessage();
            statusMessage = errorMessage + " (ESC to menu)";
            progress = 0f;
            if (multiplayerClient != null) {
                multiplayerClient.close();
                multiplayerClient = null;
            }
        }
    }

    @Override
    public void render(float delta) {
        if (loadingComplete) {
            game.setScreen(new MultiplayerGameScreen(game, engine, player, multiplayerClient, localPlayerId));
            return;
        }

        if (errorMessage != null && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        statusLabel.setText(statusMessage);

        Gdx.gl.glClearColor(0.05f, 0.07f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f - 50f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(centerX - BAR_WIDTH / 2f, centerY, BAR_WIDTH, BAR_HEIGHT);
        shapeRenderer.setColor(0.2f, 0.6f, 0.85f, 1f);
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
        if (!loadingComplete && multiplayerClient != null) {
            multiplayerClient.close();
        }
        if (!loadingComplete && engine != null) {
            engine.dispose();
        }
    }
}
