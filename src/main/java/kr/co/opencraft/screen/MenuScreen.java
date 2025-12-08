package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
        TextButton btnSettings = new TextButton("Settings", skin);
        TextButton btnQuit = new TextButton("Quit Game", skin);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        float btnWidth = 300f;
        float btnHeight = 60f;

        table.add(btnSingle).width(btnWidth).height(btnHeight).pad(10);
        table.row();
        table.add(btnSettings).width(btnWidth).height(btnHeight).pad(10);
        table.row();
        table.add(btnQuit).width(btnWidth).height(btnHeight).pad(10);

        stage.addActor(table);

        btnSingle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                game.setScreen(new GameScreen(game));
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
}
