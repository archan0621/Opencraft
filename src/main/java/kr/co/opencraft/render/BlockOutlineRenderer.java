package kr.co.opencraft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Minecraft 스타일 블록 선택 테두리 렌더러 (Mesh 기반)
 * 
 * 핵심 원리:
 * - Line Mesh를 사용하여 큐브의 12개 edge 렌더링
 * - Mesh는 한 번만 생성하고 재사용
 * - 블록 위치는 transform matrix로 이동
 * - 깊이 테스트 ON, 깊이 쓰기 OFF
 * - Z-fighting 방지를 위해 epsilon만큼 확장
 */
public class BlockOutlineRenderer {
    private Mesh lineMesh;
    private ShaderProgram shader;
    private Matrix4 transform;
    
    // Z-fighting 방지용 오프셋
    private static final float EPSILON = 0.01f;
    
    // 블록 크기 (중심 기준 ±0.5)
    private static final float HALF_SIZE = 0.5f;
    
    // AABB 확장 크기
    private static final float MIN = -HALF_SIZE - EPSILON;
    private static final float MAX = HALF_SIZE + EPSILON;
    
    // 테두리 색상 (RGBA) - 검정색
    private static final float R = 0.0f;
    private static final float G = 0.0f;
    private static final float B = 0.0f;
    private static final float A = 1.0f;
    
    public BlockOutlineRenderer() {
        createLineMesh();
        createShader();
        transform = new Matrix4();
    }
    
    /**
     * 큐브 AABB의 12개 edge를 표현하는 Line Mesh 생성
     * - GL_LINES 사용
     * - 각 edge는 2개 정점 = 12 edge * 2 = 24 정점
     * - 정점 속성: position(3) + color(4) = 7 floats
     * - 총 24 * 7 = 168 floats
     */
    private void createLineMesh() {
        // 8개 꼭짓점 (로컬 좌표)
        float[][] corners = {
            {MIN, MIN, MIN}, // 0
            {MAX, MIN, MIN}, // 1
            {MAX, MIN, MAX}, // 2
            {MIN, MIN, MAX}, // 3
            {MIN, MAX, MIN}, // 4
            {MAX, MAX, MIN}, // 5
            {MAX, MAX, MAX}, // 6
            {MIN, MAX, MAX}  // 7
        };
        
        // 12개 edge (각 edge는 시작점-끝점 인덱스)
        int[][] edges = {
            // 아래면 (y = MIN)
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            // 위면 (y = MAX)
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            // 수직
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        
        // 정점 데이터 배열 (24 정점 * 7 floats)
        float[] vertices = new float[24 * 7];
        int idx = 0;
        
        for (int[] edge : edges) {
            // 시작 정점
            float[] start = corners[edge[0]];
            vertices[idx++] = start[0]; // x
            vertices[idx++] = start[1]; // y
            vertices[idx++] = start[2]; // z
            vertices[idx++] = R;        // r
            vertices[idx++] = G;        // g
            vertices[idx++] = B;        // b
            vertices[idx++] = A;        // a
            
            // 끝 정점
            float[] end = corners[edge[1]];
            vertices[idx++] = end[0];   // x
            vertices[idx++] = end[1];   // y
            vertices[idx++] = end[2];   // z
            vertices[idx++] = R;        // r
            vertices[idx++] = G;        // g
            vertices[idx++] = B;        // b
            vertices[idx++] = A;        // a
        }
        
        // Mesh 생성 (정적, 최대 24개 정점)
        lineMesh = new Mesh(true, 24, 0,
            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
        );
        
        lineMesh.setVertices(vertices);
    }
    
    /**
     * 간단한 색상 셰이더 생성
     */
    private void createShader() {
        String vertexShader = 
            "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
            "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
            "uniform mat4 u_projViewTrans;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "varying vec4 v_color;\n" +
            "void main() {\n" +
            "    v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
            "    gl_Position = u_projViewTrans * u_worldTrans * vec4(" + ShaderProgram.POSITION_ATTRIBUTE + ", 1.0);\n" +
            "}\n";
        
        String fragmentShader = 
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "void main() {\n" +
            "    gl_FragColor = v_color;\n" +
            "}\n";
        
        shader = new ShaderProgram(vertexShader, fragmentShader);
        
        if (!shader.isCompiled()) {
            throw new RuntimeException("Shader compilation failed: " + shader.getLog());
        }
    }
    
    /**
     * 선택된 블록의 테두리 렌더링
     * 
     * @param camera 현재 카메라
     * @param blockPos 선택된 블록의 중심 좌표
     */
    public void render(Camera camera, Vector3 blockPos) {
        if (blockPos == null) {
            return;
        }
        
        // Transform matrix 설정 (블록 위치로 이동)
        transform.idt().setToTranslation(blockPos);
        
        // OpenGL 상태 설정
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false); // 깊이 쓰기 OFF
        Gdx.gl.glLineWidth(2.0f);
        
        // 셰이더 바인드 및 uniform 설정
        shader.bind();
        shader.setUniformMatrix("u_projViewTrans", camera.combined);
        shader.setUniformMatrix("u_worldTrans", transform);
        
        // Mesh 렌더링 (GL_LINES)
        lineMesh.render(shader, GL20.GL_LINES);
        
        // OpenGL 상태 복원
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glLineWidth(1.0f);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    public void dispose() {
        if (lineMesh != null) {
            lineMesh.dispose();
        }
        if (shader != null) {
            shader.dispose();
        }
    }
}
