package kr.co.opencraft.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector3;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import kr.co.opencraft.engine.OpenCraftGame;
import kr.co.opencraft.entity.OpenCraftPlayer;
import kr.co.opencraft.input.MultiplayerInputHandler;
import kr.co.opencraft.network.MultiplayerClient;
import kr.co.opencraft.render.RemotePlayerRenderer;
import kr.co.opencraft.render.RemotePlayerState;
import kr.co.opencraft.world.BlockTextureProvider;
import kr.co.voxelite.engine.VoxeliteEngine;
import kr.co.voxelite.world.ChunkCoord;
import kr.co.voxelient.engine.VoxelientEngine;
import kr.co.voxeliver.network.protocol.impl.BlockUpdatePacket;
import kr.co.voxeliver.network.protocol.Packet;
import kr.co.voxeliver.network.protocol.impl.ChunkDataPacket;
import kr.co.voxeliver.network.protocol.impl.ChunkUnloadPacket;
import kr.co.voxeliver.network.protocol.impl.MovePacket;
import kr.co.voxeliver.network.protocol.impl.PlayerJoinedPacket;
import kr.co.voxeliver.network.protocol.impl.PlayerLeftPacket;
import kr.co.voxeliver.network.protocol.impl.PlayerStatePacket;

public class MultiplayerGameScreen implements Screen {
    private static final float MOVE_SEND_INTERVAL = 1f / 20f;

    private final OpenCraftGame game;
    private final VoxeliteEngine coreEngine;
    private final OpenCraftPlayer player;
    private final MultiplayerClient multiplayerClient;
    private final int localPlayerId;
    private final Map<Integer, RemotePlayerState> remotePlayers = new HashMap<>();
    private final Vector3 lastSentPosition = new Vector3(Float.NaN, Float.NaN, Float.NaN);
    private final LinkedHashMap<Integer, Vector3> pendingMoveDeltas = new LinkedHashMap<>();
    private final Vector3 lastMoveSamplePosition = new Vector3(Float.NaN, Float.NaN, Float.NaN);

    private VoxelientEngine clientEngine;
    private MultiplayerInputHandler inputHandler;
    private RemotePlayerRenderer remotePlayerRenderer;
    private float moveSendAccumulator;
    private int nextMoveSequence = 1;

    public MultiplayerGameScreen(OpenCraftGame game, VoxeliteEngine coreEngine, OpenCraftPlayer player, MultiplayerClient multiplayerClient, int localPlayerId) {
        this.game = game;
        this.coreEngine = coreEngine;
        this.player = player;
        this.multiplayerClient = multiplayerClient;
        this.localPlayerId = localPlayerId;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        clientEngine = VoxelientEngine.builder(coreEngine)
            .textureAtlasPath("texture/block.png")
            .textureProvider(new BlockTextureProvider())
            .playerSpeed(5f)
            .cameraPitch(-20f)
            .cameraFar(144f)
            .updateCoreEngine(false)
            .build();
        clientEngine.initialize(width, height);
        inputHandler = new MultiplayerInputHandler(clientEngine, player, multiplayerClient);
        remotePlayerRenderer = new RemotePlayerRenderer();
        lastMoveSamplePosition.set(player.getPosition());
    }

    @Override
    public void render(float delta) {
        if (multiplayerClient == null || !multiplayerClient.isConnected()) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        clientEngine.update(delta);
        inputHandler.handleInput();
        sendLocalMovement(delta);
        multiplayerClient.update(delta);
        applyIncomingPackets();
        updateRemotePlayers(delta);
        clientEngine.render();
        remotePlayerRenderer.render(clientEngine.getCamera(), remotePlayers.values());
    }

    private void sendLocalMovement(float delta) {
        moveSendAccumulator += delta;
        Vector3 position = new Vector3(player.getPosition());
        boolean moved = !position.epsilonEquals(lastSentPosition, 0.001f);
        if (!moved && moveSendAccumulator < MOVE_SEND_INTERVAL) {
            return;
        }

        int sequence = nextMoveSequence++;
        Vector3 deltaSinceLastSent = new Vector3(position).sub(lastMoveSamplePosition);
        pendingMoveDeltas.put(sequence, deltaSinceLastSent);
        multiplayerClient.send(new MovePacket(sequence, position.x, position.y, position.z));
        lastMoveSamplePosition.set(position);
        lastSentPosition.set(position);
        moveSendAccumulator = 0f;
    }

    private void applyIncomingPackets() {
        Packet packet;
        while ((packet = multiplayerClient.pollPacket()) != null) {
            if (packet instanceof ChunkDataPacket chunkDataPacket) {
                coreEngine.getWorld().applyChunk(chunkDataPacket.toChunk());
                coreEngine.getPhysics().invalidateCache();
                continue;
            }

            if (packet instanceof ChunkUnloadPacket chunkUnloadPacket) {
                ChunkCoord coord = chunkUnloadPacket.getChunkCoord();
                coreEngine.getWorld().unloadChunk(coord);
                coreEngine.getPhysics().invalidateCache();
                continue;
            }

            if (packet instanceof BlockUpdatePacket blockUpdatePacket) {
                applyBlockUpdate(blockUpdatePacket);
                continue;
            }

            if (packet instanceof PlayerJoinedPacket playerJoinedPacket) {
                if (playerJoinedPacket.getPlayerId() != localPlayerId) {
                    RemotePlayerState remotePlayer = remotePlayers.computeIfAbsent(
                        playerJoinedPacket.getPlayerId(),
                        playerId -> new RemotePlayerState(
                            playerId,
                            playerJoinedPacket.getUsername(),
                            playerJoinedPacket.getPosition()
                        )
                    );
                    remotePlayer.setUsername(playerJoinedPacket.getUsername());
                    remotePlayer.setTargetPosition(playerJoinedPacket.getPosition());
                }
                continue;
            }

            if (packet instanceof PlayerStatePacket playerStatePacket) {
                applyPlayerState(playerStatePacket);
                continue;
            }

            if (packet instanceof PlayerLeftPacket playerLeftPacket) {
                remotePlayers.remove(playerLeftPacket.getPlayerId());
            }
        }
    }

    private void applyPlayerState(PlayerStatePacket playerStatePacket) {
        if (playerStatePacket.getPlayerId() != localPlayerId) {
            RemotePlayerState remotePlayer = remotePlayers.computeIfAbsent(
                playerStatePacket.getPlayerId(),
                playerId -> new RemotePlayerState(playerId, "player-" + playerId, playerStatePacket.getPosition())
            );
            remotePlayer.setTargetPosition(playerStatePacket.getPosition());
            return;
        }

        Iterator<Map.Entry<Integer, Vector3>> iterator = pendingMoveDeltas.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Vector3> entry = iterator.next();
            if (entry.getKey() <= playerStatePacket.getAcknowledgedMoveSequence()) {
                iterator.remove();
            }
        }

        Vector3 reconciledPosition = playerStatePacket.getPosition();
        for (Vector3 delta : pendingMoveDeltas.values()) {
            reconciledPosition.add(delta);
        }

        player.setPosition(reconciledPosition);
        clientEngine.getCamera().setPosition(player.getEyePosition());
        clientEngine.getCamera().update();
        lastMoveSamplePosition.set(player.getPosition());
    }

    private void updateRemotePlayers(float delta) {
        for (RemotePlayerState remotePlayer : remotePlayers.values()) {
            remotePlayer.update(delta);
        }
    }

    private void applyBlockUpdate(BlockUpdatePacket blockUpdatePacket) {
        if (blockUpdatePacket.isRemoval()) {
            coreEngine.removeBlock(blockUpdatePacket.getPosition());
            return;
        }

        coreEngine.addBlock(blockUpdatePacket.getPosition(), blockUpdatePacket.getBlockType());
    }

    @Override
    public void resize(int width, int height) {
        if (clientEngine != null && clientEngine.isInitialized()) {
            clientEngine.resize(width, height);
        }
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
        if (remotePlayerRenderer != null) {
            remotePlayerRenderer.dispose();
        }
        if (clientEngine != null) {
            clientEngine.dispose();
        }
        if (multiplayerClient != null) {
            multiplayerClient.close();
        }
        if (coreEngine != null) {
            coreEngine.dispose();
        }
    }
}
