package kr.co.opencraft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * 입력 처리 통합 관리
 */
public class InputHandler {
    private MouseHandler mouseHandler;

    public InputHandler() {
        this.mouseHandler = new MouseHandler();
    }

    public void update(float delta) {
        mouseHandler.update();

        // ESC 키로 마우스 커서 잠금 해제
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            mouseHandler.unlockMouse();
        }

        // 마우스 클릭으로 다시 잠금
        mouseHandler.handleMouseClick();
    }

    public MouseHandler getMouseHandler() {
        return mouseHandler;
    }

    public boolean isMouseLocked() {
        return mouseHandler.isMouseLocked();
    }

    public int getMouseDeltaX() {
        return mouseHandler.getMouseDeltaX();
    }

    public int getMouseDeltaY() {
        return mouseHandler.getMouseDeltaY();
    }

    public float getMouseSensitivity() {
        return mouseHandler.getMouseSensitivity();
    }
}

