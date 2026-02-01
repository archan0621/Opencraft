# OpenCraft

Voxelite 엔진을 사용하는 Minecraft 스타일 복셀 게임입니다.

## 개요

OpenCraft는 Voxelite 엔진 위에 구축된 게임 애플리케이션입니다. 엔진이 제공하는 기본 기능을 활용하여 게임 규칙과 정책을 구현합니다.

## 구조

- **MenuScreen**: 메인 메뉴 화면
- **LoadingScreen**: 월드 생성 및 로딩 화면
- **GameScreen**: 실제 게임 플레이 화면
- **TerrainGenerator**: 지형 생성 정책 (SimplexNoise 기반)
- **ChunkLoadPolicy**: 청크 로딩/언로딩 정책

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew run
```

## 의존성

- Voxelite 엔진 (libs/voxelite-1.0-SNAPSHOT.jar)
- LibGDX 1.14.0
- Gson 2.10.1

## 월드 저장

월드는 `saves/world1/` 디렉토리에 청크 단위로 저장됩니다.
