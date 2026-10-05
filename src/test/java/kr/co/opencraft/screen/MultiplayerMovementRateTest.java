package kr.co.opencraft.screen;

import com.badlogic.gdx.math.Vector3;
import kr.co.opencraft.entity.OpenCraftPlayer;
import kr.co.opencraft.network.MultiplayerClient;
import kr.co.voxeliver.network.protocol.Packet;
import kr.co.voxeliver.network.protocol.impl.MovePacket;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiplayerMovementRateTest {
    @Test
    void movementAt144FpsRespectsRateAndPreservesSequenceAndAccumulatedDelta() throws Exception {
        OpenCraftPlayer player = new OpenCraftPlayer(new Vector3());
        RecordingClient client = new RecordingClient();
        MultiplayerGameScreen screen = new MultiplayerGameScreen(null, null, player, client, 1);
        initializeMovementSample(screen);
        Method send = sendMethod();

        for (int frame = 1; frame <= 144; frame++) {
            player.setPosition(new Vector3(frame, 0, 0));
            send.invoke(screen, 1f / 144f);
        }

        assertTrue(client.moves.size() >= 18);
        assertTrue(client.moves.size() <= 20);
        assertEquals(8f, client.moves.get(0).getX());
        assertEquals(144f, client.moves.get(client.moves.size() - 1).getX());
        for (int i = 0; i < client.moves.size(); i++) assertEquals(i + 1, client.moves.get(i).getSequence());

        Field pendingField = MultiplayerGameScreen.class.getDeclaredField("pendingMoveDeltas");
        pendingField.setAccessible(true);
        @SuppressWarnings("unchecked")
        LinkedHashMap<Integer, Vector3> pending = (LinkedHashMap<Integer, Vector3>) pendingField.get(screen);
        Vector3 total = new Vector3();
        pending.values().forEach(total::add);
        assertEquals(new Vector3(144, 0, 0), total);
    }

    @Test
    void stationaryHeartbeatWaitsForIntervalAndSlowFrameDoesNotBurst() throws Exception {
        OpenCraftPlayer player = new OpenCraftPlayer(new Vector3());
        RecordingClient client = new RecordingClient();
        MultiplayerGameScreen screen = new MultiplayerGameScreen(null, null, player, client, 1);
        initializeMovementSample(screen);
        Method send = sendMethod();

        send.invoke(screen, 0.025f);
        assertTrue(client.moves.isEmpty());
        send.invoke(screen, 0.025f);
        assertEquals(1, client.moves.size());
        send.invoke(screen, 1f);
        assertEquals(2, client.moves.size());
        send.invoke(screen, 0f);
        assertEquals(2, client.moves.size());
    }

    private static void initializeMovementSample(MultiplayerGameScreen screen) throws Exception {
        Field field = MultiplayerGameScreen.class.getDeclaredField("lastMoveSamplePosition");
        field.setAccessible(true);
        ((Vector3) field.get(screen)).setZero();
    }

    private static Method sendMethod() throws Exception {
        Method method = MultiplayerGameScreen.class.getDeclaredMethod("sendLocalMovement", float.class);
        method.setAccessible(true);
        return method;
    }

    private static class RecordingClient extends MultiplayerClient {
        final List<MovePacket> moves = new ArrayList<>();
        RecordingClient() { super("localhost", 1); }
        @Override public void send(Packet packet) { moves.add((MovePacket) packet); }
    }
}
