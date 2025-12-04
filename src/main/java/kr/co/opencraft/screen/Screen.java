package kr.co.opencraft.screen;

public interface Screen {
    // 로직
    void update(float deltaTime);

    // 그리기
    void render();

    // 이 화면으로 들어올 때
    void onEnter();

    // 이 화면에서 나갈 때
    void onExit();
}
