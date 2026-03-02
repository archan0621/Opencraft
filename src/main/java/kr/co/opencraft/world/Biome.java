package kr.co.opencraft.world;

/**
 * 바이옴(생물군계) 정의
 * - 각 바이옴은 지형 특성과 블록 구성을 가짐
 */
public enum Biome {
    /** 평원: 평평한 지형, 잔디/흙 */
    PLAINS(8.0, 0, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.MY_STONE),
    
    /** 숲: 완만한 구릉, 잔디/흙 */
    FOREST(12.0, 2, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.MY_STONE),
    
    /** 구릉지: 롤링 힐, 잔디/흙 */
    HILLS(25.0, 4, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.MY_STONE),
    
    /** 산맥: 높고 급한 지형, 잔디/흙/돌 (고도에 따라 돌 노출) */
    MOUNTAIN(55.0, 8, BlockTypes.GRASS, BlockTypes.DIRT, BlockTypes.ORIGIN_STONE);
    
    /** 지형 높이 변화 진폭 (클수록 높고 급함) */
    public final double amplitude;
    /** 기본 높이 오프셋 */
    public final int baseHeightOffset;
    /** 표면 블록 */
    public final int surfaceBlock;
    /** 표면 아래 블록 (흙층) */
    public final int subsurfaceBlock;
    /** 깊은 지하 블록 (돌층) */
    public final int stoneBlock;
    
    Biome(double amplitude, int baseHeightOffset,
          int surfaceBlock, int subsurfaceBlock, int stoneBlock) {
        this.amplitude = amplitude;
        this.baseHeightOffset = baseHeightOffset;
        this.surfaceBlock = surfaceBlock;
        this.subsurfaceBlock = subsurfaceBlock;
        this.stoneBlock = stoneBlock;
    }
}
