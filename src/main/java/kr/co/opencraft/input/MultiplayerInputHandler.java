package kr.co.opencraft.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.entity.OpenCraftPlayer;
import kr.co.opencraft.network.MultiplayerClient;
import kr.co.opencraft.world.BlockTypes;
import kr.co.voxelient.engine.VoxelientEngine;
import kr.co.voxelite.physics.RaycastHit;
import kr.co.voxeliver.network.protocol.impl.BreakBlockRequestPacket;
import kr.co.voxeliver.network.protocol.impl.PlaceBlockRequestPacket;

public class MultiplayerInputHandler {
    private final VoxelientEngine engine;
    private final OpenCraftPlayer player;
    private final MultiplayerClient multiplayerClient;

    public MultiplayerInputHandler(VoxelientEngine engine, OpenCraftPlayer player, MultiplayerClient multiplayerClient) {
        this.engine = engine;
        this.player = player;
        this.multiplayerClient = multiplayerClient;
    }

    public void handleInput() {
        if (!Gdx.input.justTouched()) {
            return;
        }

        int button = getMouseButton();
        if (button == Buttons.LEFT) {
            handleBlockDestruction();
        } else if (button == Buttons.RIGHT) {
            handleBlockPlacement();
        }
    }

    private int getMouseButton() {
        if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
            return Buttons.LEFT;
        }
        if (Gdx.input.isButtonJustPressed(Buttons.RIGHT)) {
            return Buttons.RIGHT;
        }
        return -1;
    }

    private void handleBlockDestruction() {
        Vector3 selectedBlock = engine.getSelectedBlock();
        if (selectedBlock != null) {
            multiplayerClient.send(new BreakBlockRequestPacket(selectedBlock));
        }
    }

    private void handleBlockPlacement() {
        RaycastHit hit = engine.getRaycastHit();
        if (hit == null) {
            return;
        }

        Vector3 placePos = hit.getPlacementPosition();
        if (wouldCollideWithPlayer(placePos)) {
            return;
        }

        multiplayerClient.send(new PlaceBlockRequestPacket(placePos, BlockTypes.ORIGIN_STONE));
    }

    private boolean wouldCollideWithPlayer(Vector3 blockPos) {
        Vector3 playerPos = player.getPosition();

        float playerMinX = playerPos.x - 0.3f;
        float playerMaxX = playerPos.x + 0.3f;
        float playerMinY = playerPos.y;
        float playerMaxY = playerPos.y + 1.8f;
        float playerMinZ = playerPos.z - 0.3f;
        float playerMaxZ = playerPos.z + 0.3f;

        float blockMinX = blockPos.x - 0.5f;
        float blockMaxX = blockPos.x + 0.5f;
        float blockMinY = blockPos.y - 0.5f;
        float blockMaxY = blockPos.y + 0.5f;
        float blockMinZ = blockPos.z - 0.5f;
        float blockMaxZ = blockPos.z + 0.5f;

        return !(playerMaxX <= blockMinX || playerMinX >= blockMaxX
            || playerMaxY <= blockMinY || playerMinY >= blockMaxY
            || playerMaxZ <= blockMinZ || playerMinZ >= blockMaxZ);
    }
}
