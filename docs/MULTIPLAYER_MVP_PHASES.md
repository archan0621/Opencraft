# OpenCraft 멀티플레이 MVP 개발 기록

## 1. 이 문서는 무엇인가요?

이 문서는 `voxelite`, `voxelient`, `voxeliver`, `opencraft`를 이용해서  
`싱글플레이는 유지하고`, `멀티플레이를 새로 추가`한 과정을 처음부터 끝까지 기록한 문서입니다.

아주 쉽게 말하면:

- `voxelite` = 게임의 뇌와 몸
- `voxelient` = 화면, 카메라, 입력
- `voxeliver` = 서버
- `opencraft` = 실제 게임 내용

이번 작업의 목표는 이것이었습니다.

1. 기존 싱글플레이를 깨지 않는다.
2. 멀티플레이는 서버가 월드를 들고 있는 구조로 만든다.
3. 클라이언트는 바로 반응하되, 서버가 최종 판정을 한다.
4. 최소 기능만 먼저 되는 `MVP`를 완성한다.

---

## 2. 먼저 아주 쉽게 전체 그림 보기

### 예전 구조

예전에는 거의 이런 느낌이었습니다.

- 클라이언트가 혼자 월드를 만들고
- 혼자 움직이고
- 혼자 블록을 놓고 부수고
- 혼자 렌더링했습니다.

즉, 싱글플레이용 구조였습니다.

### 바꾼 뒤 구조

이제는 두 모드가 있습니다.

### 싱글플레이

- 기존처럼 로컬에서 바로 게임 실행
- `opencraft + voxelite + voxelient`

### 멀티플레이

- 서버가 월드를 가짐
- 클라이언트는 서버에 접속함
- 서버가 "이건 된다 / 안 된다"를 결정함

즉:

- `voxeliver`가 월드 서버
- `opencraft`는 멀티플레이 클라이언트

---

## 3. 최종 역할 분리

### `voxelite`

머리 없는 core입니다.  
여기에는 화면이 없습니다.

담당:

- 월드
- 청크
- 블록 데이터
- 플레이어 상태
- 물리
- 충돌
- 레이캐스트
- 직렬화

한마디로:

> "게임 세계가 어떻게 돌아가는지"를 담당합니다.

### `voxelient`

클라이언트 전용 계층입니다.

담당:

- 렌더링
- ChunkMesh
- 카메라
- 입력
- HUD
- 선택 블록 계산

한마디로:

> "사람이 게임을 보고 조작하는 부분"을 담당합니다.

### `voxeliver`

멀티플레이 서버입니다.

담당:

- 네트워크 연결
- 세션 관리
- 서버 틱
- 서버 월드 보유
- 플레이어 상태 관리
- 청크 전송
- 블록 변경 검증

한마디로:

> "여러 사람이 같은 월드에 들어오게 해주는 심판"입니다.

### `opencraft`

실제 게임입니다.

담당:

- 블록 종류
- 텍스처 규칙
- 월드 규칙
- 메뉴/로딩 화면
- 싱글플레이 진입
- 멀티플레이 진입

한마디로:

> "이 게임이 무엇을 플레이하게 할지"를 담당합니다.

---

## 4. 이번 MVP에서 꼭 이루고 싶었던 것

우리가 목표로 잡은 최소 버전은 다음이었습니다.

1. 서버 1개를 실행할 수 있다.
2. 클라이언트 여러 개가 접속할 수 있다.
3. 같은 월드에 스폰된다.
4. 서버가 주변 청크를 보내준다.
5. 서로 움직이는 것이 보인다.
6. 내 플레이어는 바로 움직이는 것처럼 느껴진다.
7. 블록 설치/파괴는 서버가 검증한다.
8. 기존 싱글플레이는 계속 작동한다.

이것이 바로 이번 `MVP`입니다.

---

## 5. Phase 1: 네트워크 바닥 공사

### 목표

먼저 `voxeliver`가 믿을 수 있는 네트워크 레이어가 되게 만드는 것.

### 왜 이 단계가 필요했나요?

집을 짓기 전에 바닥이 튼튼해야 합니다.  
패킷이 잘못 읽히거나, 같은 패킷이 두 번 가거나, 세션 정리가 이상하면  
그 위에 무엇을 올려도 계속 흔들립니다.

### 한 일

- 패킷 프레이밍 규칙을 정리했습니다.
- 인코더/디코더를 안정화했습니다.
- 브로드캐스트 버그를 고쳤습니다.
- 세션 관리 코드를 정리했습니다.
- 회귀 테스트를 추가했습니다.

### 핵심 결과

이제 서버는:

- 패킷을 안정적으로 읽고
- 올바르게 보내고
- 세션을 제대로 정리할 수 있게 되었습니다.

### 이 단계의 의미

> "말을 주고받는 방법"을 먼저 바로잡은 단계입니다.

### 코드에서 실제로는 이렇게 했습니다

핵심 파일:

- `voxeliver/src/main/java/kr/co/voxeliver/network/codec/PacketDecoder.java`
- `voxeliver/src/main/java/kr/co/voxeliver/network/codec/PacketEncoder.java`
- `voxeliver/src/main/java/kr/co/voxeliver/network/BroadCast.java`

아주 단순하게 보면 이런 느낌입니다.

```java
// 보내는 쪽
[패킷길이][패킷ID][내용]

// 받는 쪽
길이를 읽는다
ID를 읽는다
ID에 맞는 Packet 클래스를 찾는다
내용을 그 Packet에 채운다
```

브로드캐스트도 이렇게 정리했습니다.

```java
send(packet)              // 모두에게 보냄
send(packet, excluded)    // 한 명만 빼고 보냄
```

즉, 이 단계는  
"패킷을 어떻게 담고, 어떻게 꺼내고, 누구에게 보낼지"를 깨끗하게 정리한 단계입니다.

---

## 6. Phase 2: `voxeliver`를 진짜 서버로 만들기

### 목표

`voxeliver`가 단순 소켓 서버가 아니라  
실제로 `voxelite` 월드를 품고 있는 서버가 되게 만드는 것.

### 왜 이 단계가 필요했나요?

멀티플레이에서 가장 중요한 것은  
"누가 진짜 월드를 가지고 있느냐"입니다.

이번 구조에서는 서버가 진짜 월드를 가져야 했습니다.

### 한 일

- `voxeliver`가 `voxelite`를 의존하도록 연결
- `ServerRuntime` 추가
- 서버 틱 루프 추가
- 서버 전용 플레이어 상태(`ServerPlayer`) 추가
- 로그인/스폰 패킷 추가
- 서버 시작 시 headless `VoxeliteEngine` 생성

### 핵심 결과

이제 `voxeliver`는:

- 월드를 가지고 있고
- 서버 틱이 돌고
- 접속한 플레이어를 서버 쪽에서 관리할 수 있습니다.

### 이 단계의 의미

> "패킷 서버"가 "월드 서버"로 바뀐 단계입니다.

### 코드에서 실제로는 이렇게 했습니다

핵심 파일:

- `voxeliver/src/main/java/kr/co/voxeliver/server/ServerRuntime.java`
- `voxeliver/src/main/java/kr/co/voxeliver/server/player/ServerPlayer.java`
- `voxeliver/src/main/java/kr/co/voxeliver/VoxeliverApplication.java`

서버가 시작될 때 실제로는 이런 흐름입니다.

```java
engine = VoxeliteEngine.builder()
    .chunkGenerator(...)
    .chunkLoadPolicy(...)
    .build();

engine.initialize();
tickExecutor.scheduleAtFixedRate(this::tick, ...);
```

뜻은 간단합니다.

- 서버 안에서 `VoxeliteEngine`을 만든다
- 월드를 초기화한다
- 일정한 간격으로 `tick()`을 계속 돌린다

그리고 플레이어가 로그인하면:

```java
ServerPlayer created = new ServerPlayer(session, username, spawnPosition, engine.getWorld());
playersById.put(created.getPlayerId(), created);
```

즉,

- 네트워크 세션과
- 게임 안의 플레이어 상태를

서버 안에서 연결한 것입니다.

---

## 7. Phase 3: 싱글플레이는 유지하고 멀티플레이 진입 경로 추가

### 목표

기존 싱글플레이는 그대로 두고,  
멀티플레이로 들어가는 새 길을 만드는 것.

### 왜 이 단계가 필요했나요?

기존 코드를 한 번에 멀티플레이로 바꾸면  
싱글플레이까지 같이 망가질 수 있습니다.

그래서 모드를 분리했습니다.

### 한 일

- 메뉴에 `Single Player`와 `Multiplayer (Local)` 분리
- 멀티플레이 클라이언트 세션 추가
- 로그인/접속 처리 추가
- 멀티플레이용 로딩 화면 추가
- 멀티플레이용 게임 화면 추가

### 핵심 결과

이제 게임에는 두 개의 길이 생겼습니다.

- 싱글플레이 길
- 멀티플레이 길

서로 섞이지 않게 만들었습니다.

### 이 단계의 의미

> "기존 게임을 보존하면서 새 모드를 안전하게 붙인 단계"입니다.

### 코드에서 실제로는 이렇게 했습니다

핵심 파일:

- `opencraft/src/main/java/kr/co/opencraft/screen/MenuScreen.java`
- `opencraft/src/main/java/kr/co/opencraft/network/MultiplayerClient.java`
- `opencraft/src/main/java/kr/co/opencraft/screen/MultiplayerLoadingScreen.java`

메뉴에서는 아주 직접적으로 갈라집니다.

```java
btnSingle -> new LoadingScreen(game)
btnMulti  -> new MultiplayerLoadingScreen(game)
```

즉 버튼 하나는 싱글플레이 길로,
다른 버튼 하나는 멀티플레이 길로 갑니다.

멀티플레이 클라이언트는 이런 식으로 서버에 붙습니다.

```java
channel = bootstrap.connect(host, port).sync().channel();
channel.writeAndFlush(new LoginRequestPacket(username));
```

뜻은 단순합니다.

- 서버에 연결하고
- 로그인 패킷을 보낸 뒤
- 서버 응답을 기다립니다.

---

## 8. Phase 4: 서버가 월드를 보내주기 시작함

### 목표

클라이언트가 자기 마음대로 월드를 만들지 않고,  
서버가 보내준 청크와 플레이어 상태를 받아서 보여주게 만드는 것.

### 왜 이 단계가 필요했나요?

마인크래프트식 멀티플레이에서는  
서버가 월드를 가지고 있고, 클라이언트는 그 월드를 "받아와서 보기"만 해야 합니다.

### 한 일

- `ChunkData`, `ChunkUnload` 패킷 추가
- `PlayerJoined`, `PlayerState`, `PlayerLeft` 패킷 추가
- 서버가 플레이어 주변 청크를 계산해서 전송
- 클라이언트가 받은 청크를 로컬 world에 반영
- 클라이언트가 받은 플레이어 상태를 저장
- 멀티플레이 클라이언트 world를 "빈 세계"로 시작하게 변경

### 핵심 결과

이제 멀티플레이 클라이언트는:

- 자기 혼자 지형을 만들지 않고
- 서버가 보내준 데이터로 월드를 보여줍니다.

### 이 단계의 의미

> "월드의 주인"이 클라이언트에서 서버로 완전히 넘어간 단계입니다.

### 코드에서 실제로는 이렇게 했습니다

핵심 파일:

- `voxeliver/src/main/java/kr/co/voxeliver/server/ServerRuntime.java`
- `opencraft/src/main/java/kr/co/opencraft/screen/MultiplayerLoadingScreen.java`
- `opencraft/src/main/java/kr/co/opencraft/screen/MultiplayerGameScreen.java`

서버는 플레이어 주변 청크를 계산해서 보냅니다.

```java
Set<ChunkCoord> visibleChunks = getVisibleChunkCoords(playerPosition);

for (ChunkCoord coord : visibleChunks) {
    player.getSession().send(new ChunkDataPacket(chunk));
}
```

클라이언트는 받은 청크를 월드에 넣습니다.

```java
if (packet instanceof ChunkDataPacket chunkDataPacket) {
    coreEngine.getWorld().applyChunk(chunkDataPacket.toChunk());
}
```

중요한 점은 여기입니다.

멀티플레이 로딩 화면에서는 클라이언트 world를 일부러 "빈 상태"로 시작시켰습니다.

```java
.autoCreateGround(false)
.chunkGenerator((chunk, blockType) -> {})
.initialChunkRadius(0)
```

즉,

- 땅을 자동 생성하지 않고
- 청크도 미리 만들지 않고
- 오직 서버가 보낸 것만 보여주게 했습니다.

---

## 9. Phase 5: 내 플레이어는 바로 움직이고, 서버가 나중에 확인함

### 목표

클라이언트는 입력했을 때 즉시 반응하고,  
서버는 나중에 그것이 맞는지 확인하는 구조를 넣는 것.

이것을 보통 `prediction + reconciliation`이라고 합니다.

### 아주 쉽게 설명하면

아이와 선생님 비유로 생각하면 쉽습니다.

- 아이(클라이언트): "나 여기로 갔어!"
- 선생님(서버): "응, 맞아" 또는 "아니, 거긴 아니고 여기야"

즉:

- 클라이언트는 먼저 움직여봄
- 서버가 최종 답을 줌
- 틀리면 조금 고침

### 한 일

- `MovePacket`에 `sequence` 추가
- 서버가 마지막으로 처리한 이동 번호를 기억
- `PlayerStatePacket`에 `acknowledgedMoveSequence` 추가
- 클라이언트가 아직 승인되지 않은 이동을 잠깐 저장
- 서버 위치가 오면 승인된 이동은 제거
- 남은 이동만 다시 적용해서 위치 보정

그리고 블록 행동도 서버 검증형으로 바꿨습니다.

- `PlaceBlockRequestPacket` 추가
- `BreakBlockRequestPacket` 추가
- `BlockUpdatePacket` 추가
- 서버가 거리, 유효성, 충돌을 확인
- 성공 시 전체 브로드캐스트
- 실패 시 클라이언트에 정답 상태 전송

### 핵심 결과

이제 멀티플레이에서:

- 내 캐릭터는 바로 반응하고
- 서버가 나중에 위치를 확인하며
- 블록 설치/파괴는 반드시 서버를 거칩니다.

### 이 단계의 의미

> "조작감은 빠르게, 정답은 서버가"라는 구조를 만든 단계입니다.

### 코드에서 실제로는 이렇게 했습니다

핵심 파일:

- `voxeliver/src/main/java/kr/co/voxeliver/network/protocol/impl/MovePacket.java`
- `voxeliver/src/main/java/kr/co/voxeliver/network/protocol/impl/PlayerStatePacket.java`
- `voxeliver/src/main/java/kr/co/voxeliver/server/ServerRuntime.java`
- `opencraft/src/main/java/kr/co/opencraft/screen/MultiplayerGameScreen.java`
- `opencraft/src/main/java/kr/co/opencraft/input/MultiplayerInputHandler.java`

클라이언트는 움직일 때마다 번호를 붙여서 보냅니다.

```java
int sequence = nextMoveSequence++;
multiplayerClient.send(new MovePacket(sequence, position.x, position.y, position.z));
pendingMoveDeltas.put(sequence, deltaSinceLastSent);
```

쉽게 말하면:

- "이건 1번 이동"
- "이건 2번 이동"
- "이건 3번 이동"

이렇게 번호표를 붙여 서버에 보내는 것입니다.

서버는 그 번호를 보고 검사합니다.

```java
if (movePacket.getSequence() <= player.getLastProcessedMoveSequence()) {
    return false;
}

if (moveDistance > config.maxMoveDistancePerRequest) {
    sendAuthoritativePlayerState(player);
    return false;
}
```

뜻은 이렇습니다.

- 이미 처리한 이동이면 무시
- 너무 멀리 순간이동하면 거절
- 이상하면 서버 정답 위치를 다시 보냄

클라이언트는 서버가 승인한 번호까지 버립니다.

```java
if (entry.getKey() <= playerStatePacket.getAcknowledgedMoveSequence()) {
    iterator.remove();
}
```

그리고 아직 승인 안 된 이동만 다시 더합니다.

```java
Vector3 reconciledPosition = playerStatePacket.getPosition();
for (Vector3 delta : pendingMoveDeltas.values()) {
    reconciledPosition.add(delta);
}
```

이것이 바로 `reconciliation`입니다.

블록도 직접 바꾸지 않고 요청만 보냅니다.

```java
multiplayerClient.send(new BreakBlockRequestPacket(selectedBlock));
multiplayerClient.send(new PlaceBlockRequestPacket(placePos, BlockTypes.ORIGIN_STONE));
```

서버는 실제로 확인한 뒤에만 world를 바꿉니다.

```java
if (!canInteractWithBlock(player, blockPosition)) {
    return false;
}

engine.addBlock(blockPosition, packet.getBlockType());
broadcastBlockUpdate(...);
```

---

## 10. Phase 6: 원격 플레이어를 실제로 보이게 만들기

### 목표

네트워크 상으로만 존재하던 다른 플레이어를  
화면에서도 보이게 만드는 것.

### 왜 이 단계가 필요했나요?

서버와 동기화가 잘 되어도  
다른 사람이 보이지 않으면 멀티플레이라고 느끼기 어렵습니다.

### 한 일

- 원격 플레이어 상태 클래스 추가
- 원격 플레이어 위치 보간 추가
- 멀티플레이 화면 전용 렌더러 추가
- 다른 플레이어를 와이어프레임 박스로 렌더링

### 핵심 결과

이제 다른 플레이어가:

- 서버에서 상태를 받아오고
- 부드럽게 움직이며
- 화면에 보입니다.

### 이 단계의 의미

> "진짜로 여러 명이 같이 있는 느낌"을 만든 단계입니다.

### 코드에서 실제로는 이렇게 했습니다

핵심 파일:

- `opencraft/src/main/java/kr/co/opencraft/render/RemotePlayerState.java`
- `opencraft/src/main/java/kr/co/opencraft/render/RemotePlayerRenderer.java`
- `opencraft/src/main/java/kr/co/opencraft/screen/MultiplayerGameScreen.java`

다른 플레이어는 바로 텔레포트시키지 않고,  
현재 위치에서 목표 위치로 조금씩 움직이게 했습니다.

```java
public void update(float delta) {
    float alpha = Math.min(1f, Math.max(0f, delta * 10f));
    renderPosition.lerp(targetPosition, alpha);
}
```

이 뜻은:

- 서버가 새 위치를 주면
- 그 점까지 한 번에 점프하지 않고
- 살짝 부드럽게 따라간다는 뜻입니다.

그리고 렌더는 아주 단순한 박스로 시작했습니다.

```java
shapeRenderer.box(minX, minY, minZ, width, height, depth);
```

즉 지금은

- 멋진 캐릭터 모델은 아직 없고
- "여기 다른 플레이어가 있다"를 확실히 보여주는
- 최소한의 상자 렌더링을 넣은 상태입니다.

---

## 11. 최종적으로 완성된 흐름

이제 멀티플레이에서 실제 흐름은 이렇습니다.

### 접속

1. 클라이언트가 서버에 접속
2. 로그인 요청 전송
3. 서버가 playerId와 spawn 위치 응답

### 월드 받기

1. 서버가 주변 청크 계산
2. `ChunkData` 전송
3. 클라이언트가 빈 world에 chunk 반영
4. `voxelient`가 그 world를 렌더

### 이동

1. 플레이어가 키를 누름
2. 클라이언트가 먼저 이동
3. 이동 패킷을 서버에 보냄
4. 서버가 검증
5. 서버가 authoritative 상태 전송
6. 클라이언트가 필요하면 위치를 조금 고침

### 블록 설치/파괴

1. 플레이어가 설치/파괴 시도
2. 클라이언트가 요청 패킷 전송
3. 서버가 거리/충돌/유효성 검증
4. 서버가 world를 변경
5. `BlockUpdate`를 모든 사람에게 전송
6. 모든 클라이언트가 같은 상태를 보게 됨

### 코드로 보면 가장 중요한 줄은 이것들입니다

서버가 월드를 실제로 바꾸는 곳:

```java
engine.addBlock(...)
engine.removeBlock(...)
```

클라이언트가 서버 월드를 받아서 반영하는 곳:

```java
coreEngine.getWorld().applyChunk(...)
coreEngine.addBlock(...)
coreEngine.removeBlock(...)
```

클라이언트가 먼저 움직이고 나중에 서버 답을 맞추는 곳:

```java
pendingMoveDeltas.put(sequence, ...)
reconciledPosition = serverPosition + pendingDeltas
```

즉 전체 시스템은 결국 세 줄로 요약할 수 있습니다.

1. 서버가 진짜 월드를 바꾼다.
2. 클라이언트는 그 결과를 받아 그린다.
3. 내 움직임만 잠깐 먼저 보여주고 나중에 서버 답에 맞춘다.

---

## 12. 테스트와 검증은 어떻게 했나요?

이번 작업에서 목표로 잡은 검증은 `GUI 자동화`가 아니라  
`빌드와 핵심 테스트 통과`였습니다.

최종 확인한 것:

- `voxelite ./gradlew test`
- `voxeliver ./gradlew test`
- `voxelient ./gradlew build`
- `opencraft ./gradlew build`

즉:

- core는 테스트 통과
- 서버는 테스트 통과
- 클라이언트 모듈은 빌드 통과
- 실제 게임 앱도 빌드 통과

### 테스트가 보장하는 것

- 패킷이 제대로 encode/decode 되는지
- 서버 runtime이 기본적으로 잘 도는지
- 과도한 이동을 서버가 거절하는지
- 블록 설치/파괴가 서버 기준으로 반영되는지
- 프로젝트 전체가 컴파일되는지

### 아직 자동화하지 않은 것

- 서버 1개 + 클라이언트 2개를 실제로 띄워보는 완전 자동 E2E 테스트
- GUI 스모크 테스트

즉:

> 코드 레벨 검증은 했고, 실제 플레이 감각 검증은 수동 확인이 필요합니다.

### 테스트 파일도 코드로 보면 이해가 쉽습니다

핵심 테스트 파일:

- `voxeliver/src/test/java/kr/co/voxeliver/network/codec/PacketCodecTest.java`
- `voxeliver/src/test/java/kr/co/voxeliver/server/ServerRuntimeTest.java`

예를 들어 이런 생각으로 테스트했습니다.

```java
MovePacket을 넣었을 때
decode 후에도 sequence/x/y/z가 같아야 한다
```

또 이런 것도 확인했습니다.

```java
너무 큰 이동 요청을 보내면
서버는 거절하고
정답 위치(PlayerStatePacket)를 돌려줘야 한다
```

또 블록에 대해서는:

```java
설치 요청 -> BlockUpdate 전송
파괴 요청 -> BlockUpdate 전송
```

즉 테스트도 복잡한 철학이 아니라,

- "패킷이 안 깨지나?"
- "서버가 이상한 이동을 막나?"
- "블록 변경이 서버 기준으로 일어나나?"

를 직접 확인하는 방식이었습니다.

---

## 13. 이번 MVP에서 "일단 여기까지"로 둔 것

이번 MVP는 최소 기능을 목표로 했기 때문에  
일부는 일부러 다음 단계로 남겼습니다.

- 원격 플레이어 정식 모델 렌더링
- 더 정교한 prediction
- 더 정교한 reconciliation
- 인벤토리
- 저장/복구
- 치트 방지 강화
- 몹/엔티티 시스템
- 월드 최적화

즉 지금은:

- 구조는 올바른 방향
- 기능은 최소한 완성
- 품질 향상은 다음 단계

---

## 14. 이번 작업에서 가장 중요한 설계 판단

이번 작업에서 가장 중요한 결정은 세 가지였습니다.

### 1. 싱글플레이와 멀티플레이를 분리했다

기존 싱글플레이를 억지로 멀티플레이 구조에 맞추지 않았습니다.  
덕분에 기존 경로를 보호할 수 있었습니다.

### 2. 서버가 월드의 주인이 되게 했다

클라이언트가 월드를 마음대로 바꾸는 구조가 아니라  
서버가 최종 판정을 하게 만들었습니다.

### 3. 클라이언트는 즉시 반응하게 했다

서버 판정을 기다리기만 하면 조작감이 나빠집니다.  
그래서 클라이언트 prediction을 넣었습니다.

이 세 가지 덕분에:

- 구조는 멀티플레이답고
- 조작감은 너무 느리지 않고
- 기존 싱글플레이도 살아남았습니다.

---

## 15. 한 줄 요약

이번 작업은

> "`싱글플레이는 유지하면서`, `서버 authoritative 멀티플레이`를 `MVP 수준까지` 실제로 올린 작업"

이었습니다.

그리고 그 결과:

- `voxelite`는 core
- `voxelient`는 client
- `voxeliver`는 server
- `opencraft`는 game

라는 역할 분리가 실제 동작하는 구조가 되었습니다.

---

## 16. 다음에 이어서 하면 좋은 일

다음 단계 후보는 이런 것들입니다.

1. 원격 플레이어를 실제 모델로 렌더링
2. 멀티플레이 접속 UI에 host/port/username 입력창 추가
3. 서버/클라이언트 실행 스모크 절차 문서화
4. 블록 변경과 이동에 대한 E2E 테스트 추가
5. 싱글플레이도 장기적으로 로컬 서버 방식으로 통합할지 검토

---

## 17. 관련 파일을 빠르게 찾고 싶다면

### 서버 핵심

- `voxeliver/src/main/java/kr/co/voxeliver/server/ServerRuntime.java`
- `voxeliver/src/main/java/kr/co/voxeliver/server/ServerConfig.java`
- `voxeliver/src/main/java/kr/co/voxeliver/server/player/ServerPlayer.java`

### 네트워크 패킷

- `voxeliver/src/main/java/kr/co/voxeliver/network/protocol/PacketRegistry.java`
- `voxeliver/src/main/java/kr/co/voxeliver/network/protocol/impl/`

### 멀티플레이 클라이언트

- `opencraft/src/main/java/kr/co/opencraft/network/MultiplayerClient.java`
- `opencraft/src/main/java/kr/co/opencraft/screen/MultiplayerLoadingScreen.java`
- `opencraft/src/main/java/kr/co/opencraft/screen/MultiplayerGameScreen.java`
- `opencraft/src/main/java/kr/co/opencraft/input/MultiplayerInputHandler.java`

### 원격 플레이어 표시

- `opencraft/src/main/java/kr/co/opencraft/render/RemotePlayerState.java`
- `opencraft/src/main/java/kr/co/opencraft/render/RemotePlayerRenderer.java`

### core / client 엔진 경계

- `voxelite/src/main/java/kr/co/voxelite/engine/VoxeliteEngine.java`
- `voxelient/src/main/java/kr/co/voxelient/engine/VoxelientEngine.java`

---

이 문서는 "왜 이렇게 만들었는지"를 다시 떠올릴 때 보는 용도입니다.  
코드 세부 구현은 위 파일들을 보면 됩니다.
