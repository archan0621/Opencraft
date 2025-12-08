package kr.co.opencraft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * 마우스 입력 처리
 */
public class MouseHandler {
    private boolean mouseLocked = true;
    private boolean firstMouse = true;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private int mouseDeltaX = 0;
    private int mouseDeltaY = 0;
    private float mouseSensitivity = 0.1f;

    public MouseHandler() {
        initializeMouse();
    }

    private void initializeMouse() {
        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;
        Gdx.input.setCursorPosition(centerX, centerY);
        lastMouseX = centerX;
        lastMouseY = centerY;

        try {
            Gdx.input.setCursorCatched(true);
        } catch (Exception e) {
            System.out.println("setCursorCatched not supported, using manual cursor control");
        }
    }

    public void update() {
        if (mouseLocked) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();

            if (firstMouse) {
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                firstMouse = false;
                mouseDeltaX = 0;
                mouseDeltaY = 0;
            } else {
                // 델타 계산
                mouseDeltaX = mouseX - lastMouseX;
                mouseDeltaY = lastMouseY - mouseY; // Y축은 반대 방향
            }

            // 마우스 커서를 화면 중앙으로 리셋
            int centerX = Gdx.graphics.getWidth() / 2;
            int centerY = Gdx.graphics.getHeight() / 2;
            Gdx.input.setCursorPosition(centerX, centerY);
            lastMouseX = centerX;
            lastMouseY = centerY;
        } else {
            mouseDeltaX = 0;
            mouseDeltaY = 0;
        }
    }

    public int getMouseDeltaX() {
        return mouseDeltaX;
    }

    public int getMouseDeltaY() {
        return mouseDeltaY;
    }

    public void lockMouse() {
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

    public void unlockMouse() {
        mouseLocked = false;
        firstMouse = true;
        try {
            Gdx.input.setCursorCatched(false);
        } catch (Exception e) {
            // 무시
        }
    }

    public boolean isMouseLocked() {
        return mouseLocked;
    }

    public void setMouseSensitivity(float sensitivity) {
        this.mouseSensitivity = sensitivity;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public boolean handleMouseClick() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !mouseLocked) {
            lockMouse();
            return true;
        }
        return false;
    }
}

