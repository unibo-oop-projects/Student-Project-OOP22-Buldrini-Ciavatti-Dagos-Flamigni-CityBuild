package it.unibo.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import it.unibo.model.api.Resource;

public class GameScreen extends ScreenAdapter {

    private static final String SOUND_FOLDER = "sounds" + File.separator;
    private static final String IMAGE_FOLDER = "images" + File.separator;
    private static final Rectangle NULL_RECTANGLE = new Rectangle(0, 0, 0, 0);
    private static final String EXTENSION = ".png";
    private static final float BUTTON_WIDTH = 64;
    private static final float BUTTON_HEIGHT = 64;

    private final Table tablePlayer;
    private final Map<Resource, Integer> resources;
    private final Music theme;
    private final ShapeRenderer shapeRenderer;
    private final List<Rectangle> buildings;
    private final Skin skin;
    private final Dialog warning;
    private final Label costWindow;
    private final Label costUpgrade;
    private final Stage stage;
    private final Rectangle border;
    private Optional<Rectangle> selected; //The building that the user selected from the icon menù to build.

    private int index = 0;
    private final String[] imageList = {"icon1", "icon2", "icon3"};
    private static final int NUMBUTTONS = 3;
    private Table tableBuildings = new Table();

    public GameScreen() {
        this.skin = new Skin(Gdx.files.internal("skin_flatEarth" + File.separator + "flat-earth-ui.json"));
        this.tablePlayer = new Table(skin);
        //Setting up the tablePlayer that contains the resources in possesion of the player
        this.resources = new HashMap<>(); //TODO, now empty filling
        Arrays.stream(Resource.values()).forEach(res -> this.resources.put(res, 0));
        this.resources.entrySet().forEach(entry -> {
            final String s = entry.getKey() + ": " + entry.getValue();
            this.tablePlayer.add(new Label(s, skin));
            this.tablePlayer.row();
        });

        this.theme = Gdx.audio.newMusic(Gdx.files.internal(SOUND_FOLDER + "Chill_Day.mp3"));
        this.buildings = new ArrayList<>();
        this.shapeRenderer = new ShapeRenderer();
        this.selected = Optional.empty();
        this.warning = new Dialog("Warning", this.skin);
        this.costWindow = new Label("Building label", this.skin);
        this.costUpgrade = new Label("Upgrade label", this.skin);
        this.stage = new Stage(new ScreenViewport());
        this.border = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(new GameProcessor());
    }

    /**{@inheritDoc} */
    @Override
    public void show() {
        this.startMusic();
        this.warning.hide();
        this.warning.text("Wrong position");
        this.stage.addActor(this.warning);
        this.stage.addActor(this.costWindow);
        this.stage.addActor(this.tablePlayer);
        this.stage.addActor(this.costUpgrade);
        this.tablePlayer.setFillParent(true);
        this.tablePlayer.top().right();
        this.setColorLabel(this.costUpgrade, Color.DARK_GRAY);

        this.costWindow.setFontScale(1.2f);
        tableBuildings.setFillParent(true);
        tableBuildings.top().left();
        this.stage.addActor(tableBuildings);
        this.selectButton(this.index);
    }

    /**{@inheritDoc} */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawRectangle(this.selected.orElse(NULL_RECTANGLE));
        this.buildings.forEach(this::drawRectangle);
        shapeRenderer.end();
        this.stage.act(delta);
        this.stage.draw();
    }

    /**{@inheritDoc} */
    @Override
    public void dispose() {
        this.shapeRenderer.dispose();
        this.theme.dispose();
        this.stage.dispose();
    }

    private void selectButton(int index){

        //crea un pane con un bottone
        tableBuildings.clear();
        String buildingPath = IMAGE_FOLDER + imageList[index] + EXTENSION;
        Texture iconTexture = new Texture(buildingPath);
        TextureRegion icon = new TextureRegion(iconTexture);
        ImageButton button = new ImageButton(new TextureRegionDrawable(icon));
        button.setName(imageList[index]);
        this.costWindow.setText(button.getName());
        tableBuildings.add(button).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(5);
        tableBuildings.add(this.costWindow);
        //posiziona la tabella in alto a sinistra rispetto allo schermo
    }

    private void startMusic() {
        this.theme.play();
        this.theme.setVolume(0.25f);
        this.theme.setOnCompletionListener(Music::play);
    }

    private void drawRectangle(final Rectangle rectangle) {
        this.shapeRenderer.setColor(this.isValidPosition(rectangle)
            ? Color.GREEN
            : Color.RED);
        this.shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    private boolean isValidPosition(final Rectangle rectangle) {
        return buildings.stream().noneMatch(rect -> rect.overlaps(rectangle))
            && this.border.contains(rectangle);
    }

    //A method for coloring the background of labels
    private void setColorLabel(final Label l, final Color c) {
        final Pixmap labelColor = new Pixmap((int) l.getWidth(), (int) l.getHeight(), Pixmap.Format.RGB888);
        labelColor.setColor(c);
        labelColor.fill();
        l.getStyle().background = new Image(new Texture(labelColor)).getDrawable();
    }

    private class GameProcessor extends InputAdapter {

        private static final int RECT_WIDTH = 200;
        private static final int RECT_HEIGHT = 200;

        private final Sound selection;
        private final Sound destruction;
        private final Sound construction;
        private final Sound wrong;
        private final Sound scroll;
        private final Sound upgrading;
        private boolean pressingShift;
        private boolean pressingCtrl;

        public GameProcessor() {
            this.selection = Gdx.audio.newSound(Gdx.files.internal(SOUND_FOLDER + "select_building.ogg"));
            this.destruction = Gdx.audio.newSound(Gdx.files.internal(SOUND_FOLDER + "destruction.ogg"));
            this.construction = Gdx.audio.newSound(Gdx.files.internal(SOUND_FOLDER + "construction.ogg"));
            this.wrong = Gdx.audio.newSound(Gdx.files.internal(SOUND_FOLDER + "wrong1.ogg"));
            this.scroll = Gdx.audio.newSound(Gdx.files.internal(SOUND_FOLDER + "scroll.ogg"));
            this.upgrading = Gdx.audio.newSound(Gdx.files.internal(SOUND_FOLDER + "upgrade.ogg"));
            this.pressingShift = false;
            this.pressingCtrl = false;
        }

        /**{@inheritDoc} */
        @Override
        public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
            return selected.isPresent()
                ? this.handlePlacement()
                : this.handleTouch(screenX, screenY);
        }

        /**{@inheritDoc} */
        @Override
        public boolean keyDown(final int keycode) {
            switch(keycode) {
                case Input.Keys.Q -> this.selectingBuilding();
                case Input.Keys.SHIFT_LEFT -> this.pressingShift = true;
                case Input.Keys.CONTROL_LEFT -> this.pressingCtrl = true;
                case Input.Keys.UP -> this.roundButtonList(1);
                case Input.Keys.DOWN -> this.roundButtonList(-1);
                case Input.Keys.ESCAPE -> Gdx.app.exit(); //TODO exit game.
            }
            return false;
        }

        /**{@inheritDoc} */
        @Override
        public boolean keyUp(final int keycode) {
            switch(keycode) {
                case Input.Keys.SHIFT_LEFT -> this.pressingShift = false;
                case Input.Keys.CONTROL_LEFT -> this.pressingCtrl = false;
            }
            return true;
        }

        /**{@inheritDoc} */
        @Override
        public boolean mouseMoved(final int screenX, final int screenY) {
            costUpgrade.setVisible(false);
            if (selected.isPresent()) { //Verifies if the user has selected a building to place
                selected.get().setPosition(this.computeX(screenX), this.computeY(screenY));
                return true;
            } else { //Allows to display a label containing info about upgrade costs when mouse is over buildings
                var building = buildings.stream().filter(b -> b.contains(screenX, Gdx.graphics.getHeight() - screenY)).findFirst();
                if (building.isPresent() && pressingCtrl) {
                    costUpgrade.setVisible(true);
                    costUpgrade.setPosition(screenX, Gdx.graphics.getHeight() - screenY);
                }
            }
            return false;
        }

        /**{@inheritDoc} */
        @Override
        public boolean scrolled(final float amountX, final float amountY) {
            this.roundButtonList((int) amountY);
            return true;
        }

        private float computeX(final float screenX) {
            return screenX - RECT_WIDTH / 2;
        }
    
        private float computeY(final float screenY) {
            return Gdx.graphics.getHeight() - screenY - RECT_HEIGHT / 2;
        }

        private boolean selectingBuilding() {
            if (selected.isEmpty()) {
                this.selection.play();
                selected = Optional.of(new Rectangle(
                    computeX(Gdx.input.getX()), 
                    computeY(Gdx.input.getY()),
                    RECT_WIDTH, RECT_HEIGHT));
                    return true;
            }
            return false;
        }

        /*When the user has selected a building from the icon menù, this method 
        is used to determine the consequences of a click of the mouse */
        private boolean handlePlacement() {
            if (isValidPosition(selected.get())) {
                this.construction.play();
                buildings.add(selected.get()); //TODO: need to add the building, not the rectangle
            } else {
                warning.show(stage);
                warning.setPosition(Gdx.graphics.getWidth() / 2 - warning.getWidth() / 2, 
                    Gdx.graphics.getHeight() - warning.getHeight() - Gdx.graphics.getHeight() / 12);
                Timer.schedule(new Task() {
                    @Override
                    public void run() {
                        warning.hide();
                    }
                }, 2f);
                this.wrong.play();
            }
            selected = Optional.empty();
            return true;
        }

        /*This method is used to determine the consequences of a click of the mouse without carrying a building for placement. */
        private boolean handleTouch(final int screenX, final int screenY) {
            final var touched = buildings.stream()
                .filter(rect -> rect.contains(screenX, Gdx.graphics.getHeight() - screenY))
                .findFirst();
            if (touched.isPresent()) {
                if (this.pressingShift) {
                    this.destruction.play();
                    buildings.remove(touched.get());
                } else if (this.pressingCtrl) {
                    this.upgrading.play();
                }
                return true;
            }
            return false;
        }

        private void roundButtonList(int param){
            this.scroll.play();
            if (param == 1){
                index++;
                if (index == NUMBUTTONS){
                    index = 0;
                }
            } else if (param == -1){
                index--;
                if (index == -1){
                    index = NUMBUTTONS-1;
                }
            }
            selectButton(index);
        }
    }
}