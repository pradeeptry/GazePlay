package net.gazeplay.ui.scenes.gamemenu;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.MouseEvent.*;

@Slf4j
@Data
@Component
public class GameMenuFactory {

    private final static double THUMBNAIL_WIDTH_RATIO = 1;
    private final static double THUMBNAIL_HEIGHT_RATIO = 0.4;

    private final static long FAVORITE_SWITCH_FIXATION_DURATION_IN_MILLISECONDS = 1000;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GameMenuController gameMenuController;

    public GameButtonPane createGameButton(
        @NonNull final GazePlay gazePlay,
        @NonNull final Region root,
        @NonNull final Configuration config,
        @NonNull final Translator translator,
        @NonNull final GameSpec gameSpec,
        @NonNull final GameButtonOrientation orientation,
        final boolean isFavorite
    ) {

        final GameSummary gameSummary = gameSpec.getGameSummary();
        final String gameName = translator.translate(gameSummary.getNameCode());

        final Image heartIcon;
        if (isFavorite) {
            heartIcon = new Image("data/common/images/heart_filled.png");
        } else {
            heartIcon = new Image("data/common/images/heart_empty.png");
        }

        ImageView favGamesImageView = new ImageView(heartIcon);
        //ImagePattern favGamesImagePattern = new ImagePattern(heartIcon);

        // can't understand the goal of the following 3 lines
        // favGamesImageView.imageProperty().addListener((listener) -> {
        // isFavourite.setValue(favGamesImageView.getImage().equals(new Image("data/common/images/heart_filled.png")));
        // config.saveConfigIgnoringExceptions();
        // });

        final I18NText gameTitleText = new I18NText(translator, gameSummary.getNameCode());
        gameTitleText.getStyleClass().add("gameChooserButtonTitle");

        I18NText gameDesc = null;
        if (gameSummary.getDescription() != null) {
            gameDesc = new I18NText(translator, gameSummary.getDescription());
            gameDesc.getStyleClass().add("gameChooserButtonDesc");
        }

        BorderPane thumbnailContainer = new BorderPane();
        thumbnailContainer.setPadding(new Insets(1, 1, 1, 1));
        thumbnailContainer.setOpaqueInsets(new Insets(1, 1, 1, 1));

        GameButtonPane gameCard = new GameButtonPane(gameSpec);
        switch (orientation) {
            case HORIZONTAL:
                gameCard.getStyleClass().add("gameChooserButton");
                gameCard.getStyleClass().add("gameChooserButtonHorizontal");
                break;
            case VERTICAL:
                gameCard.getStyleClass().add("gameChooserButton");
                gameCard.getStyleClass().add("gameChooserButtonVertical");
                break;
        }


        gameCard.getStyleClass().add("button");

        double thumbnailBorderSize = 28d;

        BorderPane gameDescriptionPane = new BorderPane();


        if (gameSummary.getGameThumbnail() != null) {

            Image buttonGraphics = new Image(gameSummary.getGameThumbnail(),200,200,true,false);
            ImageView imageView = new ImageView(buttonGraphics);
            imageView.getStyleClass().add("gameChooserButtonThumbnail");
            imageView.setPreserveRatio(true);
            thumbnailContainer.setCenter(imageView);

            double imageSizeRatio = buttonGraphics.getWidth() / buttonGraphics.getHeight();

            switch (orientation) {
                case HORIZONTAL:
                    gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                        double preferredHeight = newValue.doubleValue() - thumbnailBorderSize;
                        imageView.setFitHeight(preferredHeight - 10);
                        imageView.setFitWidth(preferredHeight * imageSizeRatio);
                    });

                    break;
                case VERTICAL:
                    gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                        double preferredWidth = newValue.doubleValue() * THUMBNAIL_WIDTH_RATIO;
                        imageView.setFitWidth(preferredWidth);
                    });
                    gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> imageView.setFitHeight(newValue.doubleValue() * THUMBNAIL_HEIGHT_RATIO));
                    break;
            }
        }

        final HBox gameCategoryContainer = new HBox();
        final VBox favIconContainer = new VBox(favGamesImageView);
        switch (orientation) {
            case HORIZONTAL:
                gameCategoryContainer.setAlignment(Pos.BOTTOM_RIGHT);
                gameCard.setBottom(gameCategoryContainer);
                gameCard.setTop(favIconContainer);
                break;
            case VERTICAL:
                gameCategoryContainer.setAlignment(Pos.TOP_RIGHT);
                gameCard.setTop(gameCategoryContainer);
                gameCard.setLeft(favIconContainer);
                break;
        }
        for (GameCategories.Category gameCategory : gameSummary.getCategories()) {
            if (gameCategory.getThumbnail() != null) {
                Image buttonGraphics = new Image(gameCategory.getThumbnail());
                ImageView imageView = new ImageView(buttonGraphics);
                imageView.getStyleClass().add("gameChooserButtonGameTypeIndicator");
                imageView.setPreserveRatio(true);
                switch (orientation) {
                    case HORIZONTAL:
                        gameCard.heightProperty().addListener(
                            (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 10));
                        gameCategoryContainer.getChildren().add(imageView);
                        break;
                    case VERTICAL:
                        gameCard.widthProperty().addListener(
                            (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 10));
                        gameCategoryContainer.getChildren().add(imageView);
                        break;
                }
            }
        }

        final VBox gameTitleContainer = new VBox();
        gameTitleContainer.getChildren().add(gameTitleText);
        gameDescriptionPane.setTop(gameTitleContainer);

        if (gameDesc != null) {
            gameDesc.wrappingWidthProperty().bind(gameDescriptionPane.prefWidthProperty());
            gameDesc.setFont(Font.font("Arial", 10));
            gameDesc.setTextAlignment(TextAlignment.JUSTIFY);
            gameDescriptionPane.setCenter(gameDesc);
        }

        switch (orientation) {
            case HORIZONTAL:
                gameDescriptionPane.setPadding(new Insets(0, 10, 0, 10));

                gameCard.setRight(gameDescriptionPane);
                gameCard.setLeft(thumbnailContainer);

                gameTitleContainer.setAlignment(Pos.TOP_RIGHT);
                gameTitleText.setTextAlignment(TextAlignment.RIGHT);

                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                    // thumbnailContainer.setPrefWidth(newValue.doubleValue() / 2);
                    thumbnailContainer.setPrefHeight(newValue.doubleValue() / 2 / 16 * 9);
                    gameDescriptionPane.setPrefHeight(newValue.doubleValue() - thumbnailBorderSize);
                });
                gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                    thumbnailContainer.setPrefWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                    thumbnailContainer.setMaxWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                    gameDescriptionPane.setPrefWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                    gameDescriptionPane.setMaxWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                    gameTitleText.setWrappingWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);

                });

                break;
            case VERTICAL:
                gameDescriptionPane.setPadding(new Insets(10, 0, 10, 0));

                gameCard.setBottom(gameDescriptionPane);
                gameCard.setCenter(thumbnailContainer);

                gameTitleContainer.setAlignment(Pos.TOP_CENTER);
                gameTitleText.setTextAlignment(TextAlignment.CENTER);

                gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                    thumbnailContainer.setPrefWidth(newValue.doubleValue() - thumbnailBorderSize);
                    gameDescriptionPane.setPrefWidth(newValue.doubleValue() - thumbnailBorderSize);
                    gameTitleText.setWrappingWidth(newValue.doubleValue() - thumbnailBorderSize);
                });
                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                    thumbnailContainer.setPrefHeight(newValue.doubleValue() / 2);
                    gameDescriptionPane.setPrefHeight(newValue.doubleValue() / 2);
                });

                break;
        }

        gameCard.addEventHandler(MOUSE_PRESSED, (MouseEvent e) -> gameMenuController.onGameSelection(gazePlay, root, gameSpec, gameName));

        @Data
        class EventState {
            private final long time;
            private final boolean wasFavorite;
        }

        EventHandler favoriteGameSwitchEventHandler = new EventHandler<MouseEvent>() {

            private final AtomicReference<EventState> enteredState = new AtomicReference<>();
            private final AtomicReference<EventState> exitedState = new AtomicReference<>();

            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MOUSE_ENTERED) {
                    boolean wasFavorite = config.getFavoriteGamesProperty().contains(gameSummary.getNameCode());
                    enteredState.set(new EventState(System.currentTimeMillis(), wasFavorite));
                    log.info("enteredState = {}", enteredState);
                    return;
                }
                if (event.getEventType() == MOUSE_EXITED) {
                    boolean wasFavorite = config.getFavoriteGamesProperty().contains(gameSummary.getNameCode());
                    exitedState.set(new EventState(System.currentTimeMillis(), wasFavorite));
                    log.info("exitedState = {}", exitedState);
                    //return;
                }
                //if (event.getEventType() != MOUSE_MOVED) {
                //    return;
                //}
                long fixationDuration = System.currentTimeMillis() - enteredState.get().time;
                if (fixationDuration < FAVORITE_SWITCH_FIXATION_DURATION_IN_MILLISECONDS) {
                    // too early
                    return;
                }
                boolean isFavorite = !enteredState.get().wasFavorite;
                if (isFavorite) {
                    config.getFavoriteGamesProperty().add(gameSummary.getNameCode());
                    favGamesImageView.setImage(new Image("data/common/images/heart_filled.png"));
                } else {
                    config.getFavoriteGamesProperty().remove(gameSummary.getNameCode());
                    favGamesImageView.setImage(new Image("data/common/images/heart_empty.png"));
                }
                config.saveConfigIgnoringExceptions();
            }
        };
        favIconContainer.addEventFilter(MOUSE_ENTERED, favoriteGameSwitchEventHandler);
        favIconContainer.addEventFilter(MOUSE_MOVED, favoriteGameSwitchEventHandler);
        favIconContainer.addEventFilter(MOUSE_EXITED, favoriteGameSwitchEventHandler);

        // pausedEvents.add(gameCard);
        return gameCard;
    }


}
