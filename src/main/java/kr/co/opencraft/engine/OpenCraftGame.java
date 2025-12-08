package kr.co.opencraft.engine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import kr.co.opencraft.screen.MenuScreen;

public class OpenCraftGame extends Game {
    @Override
    public void create() {
        // 게임 시작 시 한 번만 호출
        Gdx.app.log("OpenCraft", "Game created!");
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        // 매 프레임 호출 (메인 루프)
        // Screen에서 뷰포트와 클리어를 처리하므로 여기서는 super.render()만 호출
        super.render();
    }

    @Override
    public void dispose() {
        // 리소스 정리
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
