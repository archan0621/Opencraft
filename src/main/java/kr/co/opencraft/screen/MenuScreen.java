package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import kr.co.opencraft.engine.OpenCraftGame;

public class MenuScreen implements Screen {

    private final OpenCraftGame game;

    private Stage stage;
    private Skin skin;

    public MenuScreen(OpenCraftGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("Menuscreen show");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.classpath("texture/uiskin.json"));

        TextButton btnSingle = new TextButton("Single Player", skin);
        TextField multiplayerAddressField = new TextField("127.0.0.1:25565", skin);
        TextField usernameField = new TextField(System.getProperty("user.name", "player"), skin);
        TextButton btnMulti = new TextButton("Connect Multiplayer", skin);
        TextButton btnSettings = new TextButton("Settings", skin);
        TextButton btnQuit = new TextButton("Quit Game", skin);
        Label multiplayerErrorLabel = new Label("", skin);
        multiplayerErrorLabel.setColor(1f, 0.45f, 0.45f, 1f);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        float btnWidth = 300f;
        float btnHeight = 60f;
        float fieldHeight = 50f;

        table.add(btnSingle).width(btnWidth).height(btnHeight).pad(10);
        table.row();
        table.add(new Label("Server Address (host[:port])", skin)).width(btnWidth).left().pad(4);
        table.row();
        table.add(multiplayerAddressField).width(btnWidth).height(fieldHeight).pad(4);
        table.row();
        table.add(new Label("Username", skin)).width(btnWidth).left().pad(4);
        table.row();
        table.add(usernameField).width(btnWidth).height(fieldHeight).pad(4);
        table.row();
        table.add(btnMulti).width(btnWidth).height(btnHeight).pad(10);
        table.row();
        table.add(multiplayerErrorLabel).width(btnWidth).padBottom(10);
        table.row();
        table.add(btnSettings).width(btnWidth).height(btnHeight).pad(10);
        table.row();
        table.add(btnQuit).width(btnWidth).height(btnHeight).pad(10);

        stage.addActor(table);

        btnSingle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.setScreen(new LoadingScreen(game));
            }
        });

        btnMulti.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                try {
                    ConnectionTarget target = parseConnectionTarget(multiplayerAddressField.getText());
                    String username = usernameField.getText().trim();
                    if (username.isEmpty()) {
                        multiplayerErrorLabel.setText("Username is required.");
                        return;
                    }

                    multiplayerErrorLabel.setText("");
                    game.setScreen(new MultiplayerLoadingScreen(game, target.host(), target.port(), username));
                } catch (IllegalArgumentException e) {
                    multiplayerErrorLabel.setText(e.getMessage());
                }
            }
        });

        btnSettings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Settings clicked!");
            }
        });

        btnQuit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Quit clicked!");
                Gdx.app.exit();
            }
        });

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.07f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
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
        // 다른 화면으로 전환할 때 InputProcessor 제거
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    private ConnectionTarget parseConnectionTarget(String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Server address is required.");
        }

        String host = value;
        int port = 25565;
        int separatorIndex = value.lastIndexOf(':');
        if (separatorIndex >= 0) {
            if (separatorIndex == 0 || separatorIndex == value.length() - 1) {
                throw new IllegalArgumentException("Use host[:port], for example 127.0.0.1:25565");
            }
            host = value.substring(0, separatorIndex).trim();
            String portValue = value.substring(separatorIndex + 1).trim();
            try {
                port = Integer.parseInt(portValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Port must be a number.");
            }
        }

        if (host.isEmpty()) {
            throw new IllegalArgumentException("Host is required.");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

        return new ConnectionTarget(host, port);
    }

    private record ConnectionTarget(String host, int port) {
    }
}
