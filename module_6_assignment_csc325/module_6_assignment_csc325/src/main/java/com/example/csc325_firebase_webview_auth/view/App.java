package com.example.csc325_firebase_webview_auth.view;


import com.example.csc325_firebase_webview_auth.model.FirestoreContext;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * JavaFX App
 */
public class App extends Application {

    public static final String APPLICATION_ICON = "http://cdn1.iconfinder.com/data/icons/Copenhagen/PNG/32/people.png";
    public static final String SPLASH_IMAGE = "https://libn.com/wp-content/blogs.dir/1/files/2024/08/DJI_0342-full-scaled.jpg";

    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    private Stage mainStage;
    private static final int SPLASH_WIDTH = 900;
    private static final int SPLASH_HEIGHT = 500;

    public static Firestore fstore;
    public static FirebaseAuth fauth;
    public static Scene scene;
    private final FirestoreContext contxtFirebase = new FirestoreContext();




    @Override
    public void start(final Stage initStage) throws Exception {
        mainStage = initStage; //initialize mainStage to prevent nullPointerException
        final Task<ObservableList<String>> fscProgramsTask = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> foundAcademicPrograms = FXCollections.<String>observableArrayList();
                ObservableList<String> availableStudy = FXCollections.observableArrayList(
                        "CSC", "EET", "CPIS", "BUS", "NUR",
                        "STS", "MTH", "CHM", "PHY",
                        "ENG", "AAR", "MGT", "AIM", "MGM");

                updateMessage("Loading study programs . . .");
                for (int i = 0; i < availableStudy.size(); i++) {
                    Thread.sleep(400);
                    updateProgress(i + 1, availableStudy.size());
                    String nextProgram = availableStudy.get(i);
                    foundAcademicPrograms.add(nextProgram);
                    updateMessage("Loading programs . . . all available " + nextProgram);
                }
                Thread.sleep(400);
                updateMessage("All programs loaded");

                return foundAcademicPrograms;
            }

        };

        showSplash(
                initStage,
                fscProgramsTask,
                () -> {
                    try {
                        showMainStage(fscProgramsTask.valueProperty());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        new Thread(fscProgramsTask).start();
    }


    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml ));
        return fxmlLoader.load();
    }


    public static void main(String[] args) {
        launch(args);
    }


    //splash methods
    @Override
    public void init()
    {
        //create components of splash screen
        ImageView splash = new ImageView(new Image(SPLASH_IMAGE));
        //splash.setViewport(new Rectangle2D(100, 2, SPLASH_WIDTH, SPLASH_HEIGHT));
        splash.setFitWidth(SPLASH_WIDTH);
        splash.setFitHeight(SPLASH_HEIGHT);
        //splash.setPreserveRatio(true);

        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH);
        progressText = new Label("Will find courses . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                        "-fx-background-color: cornsilk; " +
                        "-fx-border-width:5; " +
                        "-fx-border-color: " +
                        "linear-gradient(" +
                        "to bottom, " +
                        "chocolate, " +
                        "derive(chocolate, 50%)" +
                        ");");
        splashLayout.setEffect(new DropShadow());

    }

    private void showMainStage(ReadOnlyObjectProperty<ObservableList<String>> prgms) throws IOException {
        mainStage = new Stage(StageStyle.DECORATED); //ensure it is decorated (x out, minimize, drag around)
        fstore = contxtFirebase.firebase();
        fauth = FirebaseAuth.getInstance();
        scene = new Scene(loadFXML("/files/AccessFBView.fxml"));
        mainStage.setScene(scene);

        mainStage.show();
    }

    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler) {
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            } // todo add code to gracefully handle other task states.
        });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public interface InitCompletionHandler {
        void complete();
    }




}
