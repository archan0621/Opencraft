package kr.co.opencraft.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import kr.co.opencraft.world.World;

import java.util.List;

/**
 * 블록 렌더링
 */
public class BlockRenderer {
    private ModelBatch modelBatch;
    private Environment environment;

    public BlockRenderer() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(
            ColorAttribute.AmbientLight,
            1f, 1f, 1f, 1f
        ));
    }

    public void render(PerspectiveCamera camera, World world) {
        modelBatch.begin(camera);
        List<ModelInstance> instances = world.getAllBlockInstances();
        for (ModelInstance instance : instances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();
    }

    public void dispose() {
        if (modelBatch != null) {
            modelBatch.dispose();
        }
    }
}

