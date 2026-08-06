package kr.co.opencraft.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import kr.co.opencraft.world.BlockDefinition;

public final class HotbarRenderer {
    private static final int ATLAS_COLUMNS = 16;
    private static final int TILE_SIZE = 16;
    private static final float SLOT_SIZE = 48f;
    private static final float SLOT_GAP = 4f;
    private static final float ICON_PADDING = 8f;
    private static final float BOTTOM_MARGIN = 18f;

    private final Hotbar hotbar;
    private final Texture atlas;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout textLayout = new GlyphLayout();
    private final Matrix4 projection = new Matrix4();

    public HotbarRenderer(Hotbar hotbar, String atlasPath) {
        this.hotbar = hotbar;
        atlas = new Texture(Gdx.files.internal(atlasPath));
    }

    public void render(int screenWidth, int screenHeight) {
        float totalWidth = hotbar.size() * SLOT_SIZE + (hotbar.size() - 1) * SLOT_GAP;
        float startX = (screenWidth - totalWidth) / 2f;
        projection.setToOrtho2D(0f, 0f, screenWidth, screenHeight);
        shapes.setProjectionMatrix(projection);
        batch.setProjectionMatrix(projection);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < hotbar.size(); i++) {
            float x = startX + i * (SLOT_SIZE + SLOT_GAP);
            if (i == hotbar.getSelectedIndex()) {
                shapes.setColor(1f, 0.82f, 0.25f, 1f);
                shapes.rect(x - 3f, BOTTOM_MARGIN - 3f, SLOT_SIZE + 6f, SLOT_SIZE + 6f);
            }
            shapes.setColor(0.08f, 0.08f, 0.1f, 0.88f);
            shapes.rect(x, BOTTOM_MARGIN, SLOT_SIZE, SLOT_SIZE);
        }
        shapes.end();

        batch.begin();
        for (int i = 0; i < hotbar.size(); i++) {
            float x = startX + i * (SLOT_SIZE + SLOT_GAP);
            BlockDefinition block = hotbar.getSlot(i);
            batch.draw(
                textureRegion(block.textureForFace(4)),
                x + ICON_PADDING,
                BOTTOM_MARGIN + ICON_PADDING,
                SLOT_SIZE - ICON_PADDING * 2f,
                SLOT_SIZE - ICON_PADDING * 2f
            );
            font.setColor(Color.WHITE);
            font.draw(batch, Integer.toString(i + 1), x + 4f, BOTTOM_MARGIN + SLOT_SIZE - 4f);
        }

        String selectedName = hotbar.getSelectedBlock().displayName();
        font.getData().setScale(1.1f);
        textLayout.setText(font, selectedName);
        font.draw(
            batch,
            selectedName,
            (screenWidth - textLayout.width) / 2f,
            BOTTOM_MARGIN + SLOT_SIZE + 28f
        );
        font.getData().setScale(1f);
        batch.end();
    }

    private TextureRegion textureRegion(int textureIndex) {
        int tileX = textureIndex % ATLAS_COLUMNS;
        int tileY = textureIndex / ATLAS_COLUMNS;
        return new TextureRegion(atlas, tileX * TILE_SIZE, tileY * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    public void dispose() {
        atlas.dispose();
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
