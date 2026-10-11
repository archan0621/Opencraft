# OpenCraft original terrain textures

Generated with the built-in ImageGen tool on 2026-10-11, using a text-only prompt and no existing game images. Vegetation was subsequently redesigned with the same tool using our generated sheet as the edit target: muted sage/olive greens and larger, lower-contrast color patches. Only atlas indices 3, 5, 16, 22 and 23 were replaced; all other atlas pixels were preserved. Grass and leaf tiles were checked for lower mean HSV saturation and lower mean adjacent-pixel RGB contrast. The current source sheet is `earthbound-source.png`; `earthbound-preview.png` shows the actual final atlas tiles enlarged with nearest-neighbor sampling.

Both `src/main/resources/texture/block.png` (multiplayer) and `block-trees.png` (single-player and hotbar) contain the same 512×512 RGBA atlas: 16 columns × 16 rows, 32×32 pixels per tile. World UVs already use normalized 1/16 cells. Hotbar regions derive tile size from atlas width. Water alpha comes from the existing translucent material (0.65); all image pixels are opaque.

Source sheet tiles are counted from zero, left to right, top to bottom. Crop boundaries use `round(column * sourceWidth / 4)` and `round(row * sourceHeight / 4)`, then resize each crop to 32×32 with nearest-neighbor sampling.

| Atlas index | Source tile | Material |
| --- | --- | --- |
| 0 | 0 | Slate stone |
| 1 | 1 | Granite |
| 2 | 2 | Dirt |
| 3 | 3 | Grass top |
| 4 | 4 | Sandy earth (reserved) |
| 5 | 5 | Grass side |
| 6 | 6 | Water |
| 7 | 7 | Dark stone (reserved) |
| 16 | 15 | Grass variation (reserved) |
| 17 | 12 | Bedrock |
| 18 | 13 | Granite variation (reserved) |
| 19 | 14 | Dirt variation (reserved) |
| 20 | 8 | Oak bark |
| 21 | 9 | Oak end grain |
| 22 | 10 | Oak leaves |
| 23 | 11 | Mossy stone (reserved) |

Other atlas cells repeat slate stone as a fallback. No external game assets were used in this texture generation; this provenance record is not a legal clearance opinion.

## Generation prompt

```text
Use case: stylized-concept
Asset type: production-ready voxel terrain texture atlas, one square raster sprite sheet.
Primary request: Create entirely original attractive low-resolution pixel-art block materials for OpenCraft, an independent voxel sandbox. Familiar readable voxel-game aesthetic, but develop your own patterns and palette. Do not copy, trace, reference or recreate any existing game's textures.
Composition: EXACTLY a 4 by 4 grid of equal square tiles filling the entire 1024 by 1024 image edge to edge. Each tile 256 by 256. Absolutely NO gutters, borders, outlines between tiles, labels, text, margins or perspective cubes. Only flat square orthographic surface textures.
Style: each tile designed on a coarse 16 by 16 pixel grid, enlarged with crisp nearest-neighbor pixel edges. 5-7 restrained colors per material, low contrast clusters, handcrafted calm earthy pixel art. Broad 2-4 pixel clusters rather than random static or photographic detail. Neutral even lighting, no gradients, shadows, bevels or baked 3D lighting. Tileable repeat motifs without prominent central focal point.
Tile order left to right, rows top to bottom:
Row 1: (1) cool blue-slate stone with subtle angular strata; (2) warm gray granite with small ochre mineral flecks; (3) warm umber earth with grouped terracotta clods and a few muted pebbles; (4) meadow grass top, sage and fern green short broken blade clusters filling the square.
Row 2: (5) muted pale ochre sandy earth; (6) grass block SIDE with same umber earth as row1 col3, top quarter sage grass sod with a softly irregular stepped lower boundary and sparse tiny root marks; (7) opaque muted teal-blue water with sparse short horizontal pale ripple dashes, no white foam; (8) cool dark stone, subtle slate flecks.
Row 3: (9) warm oak bark side, broad vertical brown grooves interrupted with tan flecks, NOT planks; (10) oak log cut end with a small off-center irregular growth-ring pattern in honey tan and walnut, coarse square pixel stair steps; (11) dense oak leaves, olive and forest green leaf clusters and subdued lighter leaf tips, fully opaque no holes; (12) mossy stone with small sage patches.
Row 4: (13) dark charcoal bedrock with compact fractured blue-gray plates and restrained dark cracks; (14) granite variation; (15) earth variation; (16) meadow grass variation.
Constraints: fully opaque. Original assets only. No symbols, letters, watermarks, known game assets or characteristic copied patterns. Texture data only. Accurate uniform 4x4 tile layout.
```
