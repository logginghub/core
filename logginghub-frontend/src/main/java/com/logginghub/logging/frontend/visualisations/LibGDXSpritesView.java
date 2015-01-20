package com.logginghub.logging.frontend.visualisations;

import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.logginghub.logging.frontend.modules.ViewDetails;
import com.logginghub.logging.frontend.visualisations.configuration.BoxConfig;
import com.logginghub.logging.frontend.visualisations.configuration.VisualisationConfig;

public class LibGDXSpritesView implements ApplicationListener {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private ParticleEffect prototype;
    private ParticleEffectPool pool;
    private Array<PooledEffect> effects;
    private VisualiserModel model;
    // private FPSLogger fpsLogger = new FPSLogger();
    private int renderedParticles = 0;
    private int renderedFrames = 0;

    private boolean additive = true;
    // private Sprite sprite;
    private Texture texture;
    private Stage stage;
    private VisualiserModel fireworksModel;
    // private FireworksController controller;
    private int textureWidth;
    private int textureHeight;
    private volatile String newShape;
    private List<BoxConfig> boxes;
    private BitmapFont font;
    private ViewDetails viewDetails;

    public LibGDXSpritesView(VisualiserModel model, ViewDetails viewDetails) {
        this.model = model;
        // this.controller = controller;
        this.viewDetails = viewDetails;
    }

    public void create() {
        // TextureAtlas textureAtlas = new TextureAtlas("data/main");
        font = new BitmapFont();

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        additive = false;
//        loadTexture("circle");
        loadTexture(model.getShape());

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        // if (controller.getConfig().getShowGui()) {
        // Skin skin = new Skin(Gdx.files.internal("libgdx/uiskin.json"));
        //
        // VerticalGroup g = new VerticalGroup();
        // g.setPosition(10, 100);
        // g.setReverse(true);
        // g.setSpacing(5);
        // stage.addActor(g);
        //
        // final Slider releaseXSlider = new Slider(0, 1000, 1, false, skin);
        // releaseXSlider.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getReleaseX().set((int) releaseXSlider.getValue());
        // }
        // });
        // g.addActor(releaseXSlider);
        // g.addActor(new Label("Release X", skin));
        //
        // final Slider releaseYSlider = new Slider(0, 1000, 1, false, skin);
        // releaseYSlider.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getReleaseY().set((int) releaseYSlider.getValue());
        // }
        // });
        // g.addActor(releaseYSlider);
        // g.addActor(new Label("Release Y", skin));
        //
        // final Slider releaseRotationSpeed = new Slider(0, 100, 0.1f, false, skin);
        // releaseRotationSpeed.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getReleaseRotationSpeed().set(releaseRotationSpeed.getValue());
        // }
        // });
        // g.addActor(releaseRotationSpeed);
        // g.addActor(new Label("Release rotation speed", skin));
        //
        // final Slider releaseAngleSlider = new Slider(0, 100, 1, false, skin);
        // releaseAngleSlider.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getReleaseAngle().set(releaseAngleSlider.getValue());
        // }
        // });
        // g.addActor(releaseAngleSlider);
        // g.addActor(new Label("Release angle:", skin));
        //
        // final Slider releaseVelocitySlider = new Slider(0, 50, 1, false, skin);
        // releaseVelocitySlider.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getReleaseVelocity().set(releaseVelocitySlider.getValue());
        // }
        // });
        // g.addActor(releaseVelocitySlider);
        // g.addActor(new Label("Release velocity:", skin));
        //
        // // TODO : bind back the other way
        // final CheckBox varyX = new CheckBox("Vary launch X based on values", skin);
        // varyX.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getVaryX().set(varyX.isChecked());
        // }
        // });
        // g.addActor(varyX);
        //
        // final Slider xDeviation = new Slider(0, 1000, 1, false, skin);
        // xDeviation.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getXDeviation().set(xDeviation.getValue());
        // }
        // });
        //
        // g.addActor(xDeviation);
        // g.addActor(new Label("X Deviation:", skin));
        //
        // final Slider gravitySlider = new Slider(0, 0.1f, 0.01f, false, skin);
        // gravitySlider.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // model.getGravity().set(-1 * gravitySlider.getValue());
        // }
        // });
        //
        // g.addActor(gravitySlider);
        // g.addActor(new Label("Gravity:", skin));
        //
        // final CheckBox additiveBlending = new CheckBox("Additive blending", skin);
        // additiveBlending.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // boolean value = additiveBlending.isChecked();
        // model.getAdditiveBlending().set(value);
        // additive = value;
        // }
        // });
        // g.addActor(additiveBlending);
        //
        // String[] items = new String[] { "circle", "diamond", "star", "grey-small-ball",
        // "grey-small-halo", "grey-small", "red-small", "red" };
        // final SelectBox selectBox = new SelectBox(items, skin);
        // selectBox.addListener(new ChangeListener() {
        // @Override public void changed(ChangeEvent changeEvent, Actor arg1) {
        // String selection = selectBox.getSelection();
        // loadTexture(selection);
        // }
        // });
        // g.addActor(selectBox);
        //
        // g.pack();
        // }

    }

    private void loadTexture(String selection) {
        // TODO : work out how resources are loaded in this thing
        if (texture != null) {
            texture.dispose();
        }

        Files files = new LwjglFiles();
        FileHandle handle = files.classpath("particles/" + selection + ".png");
        texture = new Texture(handle, false);

        textureHeight = texture.getHeight();
        textureWidth = texture.getWidth();
    }

    public void render() {

        if (spriteBatch == null) {
            System.out.println("Sprite batch was disposed");
            return;
        }

        if (newShape != null) {
            loadTexture(newShape);
            newShape = null;
        }

        float delta = Gdx.graphics.getDeltaTime();

        additive = model.isAdditive();
        
        // controller.update(delta);
        model.update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (additive) {
            spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
        }

        spriteBatch.begin();

        renderedParticles = 0;

        Entity entity = model.getLiveEntitiesHead().next;
        while (entity != model.getLiveEntitiesTail()) {

            Vector3 position = entity.getPosition();
            java.awt.Color color = entity.getColor();

            int size = (int) entity.getSize();
            int halfSize = size / 2;

            // Debug.out("Entity {} position {}",
            // System.identityHashCode(entity),
            // position);

            int x = (int) position.x - halfSize;
            int y = (int) position.y - halfSize;

            spriteBatch.setColor(color.getRed() / 255f,
                                 color.getGreen() / 255f,
                                 color.getBlue() / 255f,
                                 1f - (float) (entity.getLifetime() / entity.getLifeLimit()));

            float originX = textureWidth / 2;// x + halfSize;
            float originY = textureHeight / 2;// y + halfSize;
            float width = textureWidth;
            float height = textureHeight;
            float scaleX = (float) entity.getSize();
            float scaleY = (float) entity.getSize();
            float rotation = entity.getRotation();
            int srcX = 0;
            int srcY = 0;
            int srcWidth = textureWidth;
            int srcHeight = textureHeight;
            boolean flipX = false;
            boolean flipY = false;

            spriteBatch.draw(texture, x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
            renderedParticles++;

            entity = entity.next;
        }

        spriteBatch.end();

        if (additive) {
            spriteBatch.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        }

        // fpsLogger.log();

        stage.act(delta);
        stage.draw();

        drawBoxes();

        renderedFrames++;
    }

    private void drawBoxes() {
        if (boxes != null) {
            shapeRenderer.begin(ShapeType.Filled);
            for (BoxConfig boxConfig : boxes) {
                shapeRenderer.setColor(boxConfig.getLibGDXColour());
                shapeRenderer.rect((float) boxConfig.getX(), (float) boxConfig.getY(), (float) boxConfig.getWidth(), (float) boxConfig.getHeight());
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeType.Line);
            for (BoxConfig boxConfig : boxes) {
                Gdx.gl10.glLineWidth(boxConfig.getBorderWidth());
                shapeRenderer.setColor(boxConfig.getLibGDXBorderColour());
                shapeRenderer.rect((float) boxConfig.getX(), (float) boxConfig.getY(), (float) boxConfig.getWidth(), (float) boxConfig.getHeight());
            }
            shapeRenderer.end();

            spriteBatch.begin();
            for (BoxConfig boxConfig : boxes) {
                font.draw(spriteBatch, boxConfig.getText(), (float) boxConfig.getX() + (float) boxConfig.getTextX(), (float) boxConfig.getY() +
                                                                                                                     (float) boxConfig.getHeight() +
                                                                                                                     (float) boxConfig.getTextY());
            }
            spriteBatch.end();
        }
    }

    public int getRenderedFrames() {
        return renderedFrames;
    }

    public int getRenderedParticles() {
        return renderedParticles;
    }

    public void resize(int width, int height) {}

    public void pause() {}

    public void resume() {}

    public void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
            spriteBatch = null;
        }
    }

    // public void show() {
    //
    // VisualisationConfig config = controller.getConfig();
    // final VectorConfig screenSize = config.getScreenSize();
    //
    // final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
    // cfg.title = "Logging Hub Visualisation";
    // cfg.width = screenSize.getXInt();
    // cfg.height = screenSize.getYInt();
    // cfg.x = config.getScreenPosition().getXInt();
    // cfg.y = config.getScreenPosition().getYInt();
    // cfg.fullscreen = false;
    // cfg.useGL20 = false;
    // cfg.forceExit = true;
    // cfg.vSyncEnabled = false;
    // cfg.resizable = true;
    //
    // SwingUtilities.invokeLater(new Runnable() {
    // @Override public void run() {
    // // LwjglCanvas canvas = new LwjglCanvas(LibGDXSpritesView.this, cfg);
    // // TestFrame.show(canvas.getCanvas(), screenSize.getXInt(), screenSize.getYInt());
    //
    // JFrame frame = new JFrame();
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // Container container = frame.getContentPane();
    //
    // LwjglAWTCanvas canvas = new LwjglAWTCanvas(LibGDXSpritesView.this, false);
    // canvas.getCanvas().setSize(800, 600);
    // container.add(canvas.getCanvas(), BorderLayout.CENTER);
    //
    // frame.setSize(800, 600);
    // frame.setVisible(true);
    //
    // }
    // });
    // // new LwjglApplication(this, cfg);
    // }

    public void start(VisualisationConfig config) {
        model.getAdditiveBlending().set(config.useAdditiveBlending());
        additive = config.useAdditiveBlending();
        if (config.getShape() != null && config.getShape().length() > 0) {
            newShape = config.getShape();
        }

        boxes = config.getBoxes();
    }

    public void updateShape() {
        newShape = model.getShape();
    }
}