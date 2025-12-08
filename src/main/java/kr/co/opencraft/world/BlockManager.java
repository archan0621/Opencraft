package kr.co.opencraft.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * 블록 생성 및 모델 관리
 */
public class BlockManager {
    private Model[] blockFaceModels;
    private float blockSize = 0.5f;
    private float thickness = 0.01f;

    public BlockManager() {
        createBlockFaceModels();
    }

    private void createBlockFaceModels() {
        ModelBuilder builder = new ModelBuilder();
        blockFaceModels = new Model[6];

        // 각 면의 색상 정의
        Color[] faceColors = {
            Color.RED,      // 앞면 (z+)
            Color.BLUE,     // 뒷면 (z-)
            Color.GREEN,    // 왼쪽면 (x-)
            Color.YELLOW,   // 오른쪽면 (x+)
            Color.CYAN,     // 위면 (y+)
            Color.MAGENTA   // 아래면 (y-)
        };

        // 앞면 (z+)
        blockFaceModels[0] = builder.createBox(1f, 1f, thickness,
            new Material(ColorAttribute.createDiffuse(faceColors[0])),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // 뒷면 (z-)
        blockFaceModels[1] = builder.createBox(1f, 1f, thickness,
            new Material(ColorAttribute.createDiffuse(faceColors[1])),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // 왼쪽면 (x-)
        blockFaceModels[2] = builder.createBox(thickness, 1f, 1f,
            new Material(ColorAttribute.createDiffuse(faceColors[2])),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // 오른쪽면 (x+)
        blockFaceModels[3] = builder.createBox(thickness, 1f, 1f,
            new Material(ColorAttribute.createDiffuse(faceColors[3])),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // 위면 (y+)
        blockFaceModels[4] = builder.createBox(1f, thickness, 1f,
            new Material(ColorAttribute.createDiffuse(faceColors[4])),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // 아래면 (y-)
        blockFaceModels[5] = builder.createBox(1f, thickness, 1f,
            new Material(ColorAttribute.createDiffuse(faceColors[5])),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    /**
     * 블록 위치로부터 ModelInstance 리스트 생성
     */
    public List<ModelInstance> createBlockInstances(Vector3 blockPosition) {
        List<ModelInstance> instances = new ArrayList<>();

        // 각 블록의 6개 면 생성
        // 앞면 (z+)
        ModelInstance frontFace = new ModelInstance(blockFaceModels[0]);
        frontFace.transform.setToTranslation(blockPosition.x, blockPosition.y, blockPosition.z + blockSize);
        instances.add(frontFace);

        // 뒷면 (z-)
        ModelInstance backFace = new ModelInstance(blockFaceModels[1]);
        backFace.transform.setToTranslation(blockPosition.x, blockPosition.y, blockPosition.z - blockSize);
        instances.add(backFace);

        // 왼쪽면 (x-)
        ModelInstance leftFace = new ModelInstance(blockFaceModels[2]);
        leftFace.transform.setToTranslation(blockPosition.x - blockSize, blockPosition.y, blockPosition.z);
        instances.add(leftFace);

        // 오른쪽면 (x+)
        ModelInstance rightFace = new ModelInstance(blockFaceModels[3]);
        rightFace.transform.setToTranslation(blockPosition.x + blockSize, blockPosition.y, blockPosition.z);
        instances.add(rightFace);

        // 위면 (y+)
        ModelInstance topFace = new ModelInstance(blockFaceModels[4]);
        topFace.transform.setToTranslation(blockPosition.x, blockPosition.y + blockSize, blockPosition.z);
        instances.add(topFace);

        // 아래면 (y-)
        ModelInstance bottomFace = new ModelInstance(blockFaceModels[5]);
        bottomFace.transform.setToTranslation(blockPosition.x, blockPosition.y - blockSize, blockPosition.z);
        instances.add(bottomFace);

        return instances;
    }

    public Model[] getBlockFaceModels() {
        return blockFaceModels;
    }

    public void dispose() {
        if (blockFaceModels != null) {
            for (Model model : blockFaceModels) {
                if (model != null) model.dispose();
            }
        }
    }
}

