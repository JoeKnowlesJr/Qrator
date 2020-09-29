package danasoft;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class QratorView extends BaseView implements TargetView.TargetListener {
    private static final int CANVAS_SIZE = 400;
    private static final String TARGET = "SELECTED_TARGET";
    private static final String SELECT = "SELECT_4_EACH";
    private static final String CLICK = "CLICK_2_SEND";
    private final Stage qStage;
    private final BorderPane mView;
    private final DirectoryChooser dirChooser;
    private File currentFolderFile;
    private TextField currentFolderTextField;
    private TreeItem<String> currentTreeItem;
    private final TreeView<String> fileTree;
    private final ImageStackPane imgStack;
    private final List<TargetView> targets;
    private final FlowPane targetPane;
    private final TargetStore tStore;
    private final TargetView currentTarget;
    private String currentOption;
    private File currentImageFile;
    private Image currentImage;
    private final TextField renameText;
    private final WaitingBtn btnWkDirChsr;
    private final List<String> processList;
    private boolean PROCESSING;
    private boolean AUTO;
    private final Map<String, Image> thumbMap;

    QratorView() {
        qStage = new Stage();
        dirChooser = new DirectoryChooser();
        imgStack = new ImageStackPane();
        tStore = new TargetStore(this);
        targets = tStore.getTargets();
        targetPane = new FlowPane(Orientation.HORIZONTAL, 5, 5);
        targetPane.setPadding(new Insets(10));
        renameText = new TextField();
        currentTarget = new TargetView();
        currentOption = CLICK;
        processList = new ArrayList<>();
        mView = new BorderPane();
        fileTree = new TreeView<>();
        thumbMap = new HashMap<>();
        btnWkDirChsr = new WaitingBtn(new Image(Paths.get("loader-ani.gif").toFile().toURI().toString()), new Image(Paths.get("folder_icon.png").toFile().toURI().toString()));
        createView();
    }

    @Override
    protected void createView() {
        RotInt rotated = new RotInt(0, 3);
        HBox root = new HBox(15);
        root.setId("center");

        Scene scene = new Scene(mView, 1200, 600, Color.BLACK);
        File css = new File("qrator.css");
        scene.getStylesheets().add(css.toURI().toString());
        File ico = new File("sphinx_ico.png");
        qStage.getIcons().add(new Image(ico.toURI().toString()));
        qStage.setScene(scene);
        qStage.show();

        ////////////////////////////////////////////////////////////
        ///////////////////////// LEFT /////////////////////////////
        ////////////////////////////////////////////////////////////

        VBox centerLeft = new VBox(10);
        centerLeft.setAlignment(Pos.TOP_CENTER);
        centerLeft.setTranslateX(15);
        ////////////////////////////////////////////////////////////
        HBox leftTitle = new HBox();
        leftTitle.setAlignment(Pos.CENTER);
        Text leftTitleText = new Text("Working Directory");
        leftTitleText.setFont(Font.font ("Verdana", 31));
        leftTitle.getChildren().add(leftTitleText);

        ////////////////////////////////////////////////////////////

        HBox currentFolderBox = new HBox(5);
        currentFolderTextField = new TextField();
        currentFolderTextField.setMinWidth(250);
        DirectoryChooser workingDirChooser = new DirectoryChooser();
        workingDirChooser.setTitle("Working Directory");
        btnWkDirChsr.setMinWidth(50);
        btnWkDirChsr.setOnAction((ActionEvent event) -> {
            File f = workingDirChooser.showDialog(qStage);
            if (f == null || !f.isDirectory()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Could not open directory");
                alert.setContentText("The file is invalid.");
                alert.showAndWait();
            } else {
                btnWkDirChsr.setWaiting(true);
                Platform.runLater(new Task<Void>() {

                    @Override
                    protected Void call() {
                        fileTree.setRoot(getNodesForDirectory(f));
                        btnWkDirChsr.setWaiting(false);
                        return null;
                    }
                });
                currentFolderTextField.setText(Paths.get(f.getPath()).toString().replace("\\", "/"));
                currentFolderFile = f;
            }
        });
        currentFolderBox.getChildren().addAll(currentFolderTextField, btnWkDirChsr);

        ////////////////////////////////////////////////////////////

        fileTree.setShowRoot(false);
        fileTree.setCellFactory(param -> new TreeCell<String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                    setGraphic(null);
                } else {
                    setText(item.substring(item.lastIndexOf('/') + 1).replace("%20", " "));
                    final Image i = thumbMap.get(getText());
                    if (i != null) {
                        ImageView iv = new ImageView(i);
                        iv.setFitHeight(20);
                        iv.setFitWidth(20);
                        setGraphic(iv);
                    }
                }
            }
        });
        fileTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileTree.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> {
            if (newValue == null) return;
            currentTreeItem = v.getValue();
            Filename fileName = new Filename(newValue.getValue());
            if (fileName.isValid()) {
                String name = fileName.fullPath().replace("%20", " ");
                rotated.reset();
                String filePath = getCurrentFilePathPlus(name);
                displayImage(Paths.get(filePath));
            }
        });

        ////////////////////////////////////////////////////////////

        HBox actionBtns = new HBox(15);
        actionBtns.setAlignment(Pos.CENTER);
        Button btnProcessAll = new Button("Process All");
        Button btnProcessSelection = new Button("Process Selected");
        btnProcessAll.setOnAction(e -> {
            fileTree.getSelectionModel().selectAll();
            process();
        });
        btnProcessSelection.setOnAction(e -> process());
        actionBtns.getChildren().addAll(btnProcessAll, btnProcessSelection);

        centerLeft.getChildren().addAll(leftTitle, currentFolderBox, fileTree, actionBtns);

        ////////////////////////////////////////////////////////////
        /////////////////////// CENTER /////////////////////////////
        ////////////////////////////////////////////////////////////

        VBox centerCenter = new VBox(10);
        centerCenter.setAlignment(Pos.TOP_CENTER);
        centerCenter.setMinWidth(400);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER);
        Text title = new Text("Qrator");
        title.setId("title");
        title.setFont(Font.font ("Verdana", 31));
        top.getChildren().add(title);


        HBox btnBox = new HBox(5);
        btnBox.setAlignment(Pos.CENTER);
        Button btnAddTarget = new Button("Add Target");
        btnAddTarget.setOnAction(e -> addTarget());
        btnBox.getChildren().add(btnAddTarget);
        refreshTargets();

        centerCenter.getChildren().addAll(top, getSeparator(), btnBox, getSeparator(), targetPane);

        ////////////////////////////////////////////////////////////
        //////////////////////// RIGHT /////////////////////////////
        ////////////////////////////////////////////////////////////

        VBox centerRight = new VBox(10);
        centerRight.setAlignment(Pos.TOP_RIGHT);
        HBox imgButtons = new HBox(5);
        imgButtons.setAlignment(Pos.CENTER);
        imgButtons.setPrefWidth(CANVAS_SIZE);
        Button btnRotL = new Button("< Rotate");
        Button btnSet = new Button("Set");
        btnSet.setVisible(false);
        Button btnRotR = new Button("Rotate >");
        btnRotL.setOnAction(e -> {
            rotated.decreaseBy(1);
            if (rotated.getValue() != 0 && !btnSet.isVisible())
                btnSet.setVisible(true);
            else if (rotated.getValue() == 0 && btnSet.isVisible())
                btnSet.setVisible(false);
            imgStack.rotate(-90.0);
        });
        btnRotR.setOnAction(e -> {
            rotated.increaseBy(1);
            if (rotated.getValue() != 0 && !btnSet.isVisible())
                btnSet.setVisible(true);
            else if (rotated.getValue() == 0 && btnSet.isVisible())
                btnSet.setVisible(false);
            imgStack.rotate(90.0);
        });
        btnSet.setOnAction(e -> {
            Image i = imgStack.getImage();
            BufferedImage bImage = SwingFXUtils.fromFXImage(i, null);
            try {
                ImageIO.write(bImage, "png", currentImageFile);
                rotated.reset();
                btnSet.setVisible(false);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        });
        imgButtons.getChildren().addAll(btnRotL, btnSet, btnRotR);

        ////////////////////////////////////////////////////////////

        imgStack.setAlignment(Pos.CENTER);
        imgStack.setPrefWidth(CANVAS_SIZE);
        imgStack.setPrefHeight(CANVAS_SIZE);
        imgStack.setImage(new Image(ico.toURI().toString(), CANVAS_SIZE, CANVAS_SIZE, true, false));
        HBox renameBox = new HBox(5);
        renameBox.setAlignment(Pos.CENTER);
        renameText.setMinWidth(300);
        renameText.setFont(Font.font ("Verdana", 17));
        Button btnGo = new Button("Go");
        btnGo.setOnAction(e -> {
            PROCESSING = false;
            execute();
            fileTree.getSelectionModel().selectAll();
            for (TreeItem<String> ti : fileTree.getSelectionModel().getSelectedItems()) {
                if (ti.getChildren().size() == 0) {
                    fileTree.getSelectionModel().select(ti);
                    break;
                }
            }
        });
        renameBox.getChildren().addAll(renameText, btnGo);
        VBox optionsBox = new VBox(5);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        final ToggleGroup tg = new ToggleGroup();
        RadioButton btnToTarget = new RadioButton("Selected Target");
        RadioButton btnSelectEach = new RadioButton("Select for Each");
        RadioButton btnClickToSend = new RadioButton("Click To Send");
        btnToTarget.getStyleClass().add("rBtn");
        btnSelectEach.getStyleClass().add("rBtn");
        btnClickToSend.getStyleClass().add("rBtn");
        btnToTarget.setToggleGroup(tg);
        btnSelectEach.setToggleGroup(tg);
        btnClickToSend.setToggleGroup(tg);
        btnToTarget.setUserData(TARGET);
        btnSelectEach.setUserData(SELECT);
        btnClickToSend.setUserData(CLICK);
        tg.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) -> {
            if (new_toggle != null) {
                currentOption = new_toggle.getUserData().toString();
            }
        });
        CheckBox btnAuto = new CheckBox("Auto");
        btnAuto.selectedProperty().addListener((observable, oldValue, newValue) -> AUTO = newValue);
        btnAuto.getStyleClass().add("btn-auto");
        tg.selectToggle(btnClickToSend);
        optionsBox.getChildren().addAll(btnToTarget, btnSelectEach, btnClickToSend, btnAuto);

        centerRight.getChildren().addAll(imgButtons, imgStack, renameBox, getSeparator(), optionsBox);
        ////////////////////////////////////////////////////////////

        root.getChildren().addAll(centerLeft, centerCenter, centerRight);
        mView.setCenter(root);


    }

    @Override
    public Node getView() {
        return mView;
    }

    private void process() {
        PROCESSING = true;
        processList.clear();
        fileTree.getSelectionModel().getSelectedItems().forEach(ti -> processList.add(ti.getValue()));
        execute();
        refreshTree();
    }

    private void displayImage(@NotNull Path p) {
        currentImageFile = p.toFile();
        currentImage = new Image(currentImageFile.toURI().toString(), CANVAS_SIZE, CANVAS_SIZE, true, false);
        imgStack.setImage(currentImage);
    }

    private void addTarget() {
        File targetDir = dirChooser.showDialog(qStage);
        if (targetDir == null || !targetDir.isDirectory()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Could not open directory");
            alert.setContentText("The path is invalid.");
            alert.showAndWait();
        } else {
            TargetView tv = new TargetView(targets.size(), targetDir.getName(), targetDir.getPath(), this);
            tv.getView().getStyleClass().add("target");
            tStore.addTarget(tv);
            refreshTargets();
        }
    }

    private void addTarget(TargetView tv) {
        if (!targets.contains(tv)) targets.add(tv);
    }

    @Override
    public void targetClicked(int id) {
        Optional<TargetView> oT = findTargetById(id);
        if (oT.isPresent()) {
            TargetView tv = oT.get();
            if (tv.isSelected()) {
                tv.setSelected(false);
                currentTarget.set(null);
            } else {
                clearTargetSelection();
                tv.setSelected(true);
                currentTarget.set(tv);
            }
            refreshTargets();
        }

    }

    private void clearTargetSelection() {
        targets.forEach(t -> t.setSelected(false));
    }

    private Optional<TargetView> findTargetById(int id) {
        for (TargetView tv : targets) {
            if (id == tv.getId())
                return Optional.of(tv);
        }
        return Optional.empty();
    }

    private void refreshTree() {
        fileTree.setRoot(getNodesForDirectory(currentFolderFile));
        currentFolderTextField.setText(currentFolderFile.getPath());
    }

    private void refreshTargets() {
        targetPane.getChildren().clear();
        for (TargetView target : targets) {
            targetPane.getChildren().add(target.getView());
        }
    }

    private void execute() {
        String targetPath = "";

        switch (currentOption) {
            case TARGET:
                String dir = currentTarget.getDir();
                if (dir != null && !dir.isEmpty()) {
                    File dirFile = new File(dir);
                    if (!dirFile.isDirectory()) {
                        System.out.println("Target is not a directory!");
                    } else {
                        targetPath = dir + "/";
                    }
                }
                break;
            case CLICK:
                if (currentTarget != null) {
                    targetPath = currentTarget.getDir();
                    break;
                }
            case SELECT:
            default:
                File target = dirChooser.showDialog(qStage);
                if (target.isDirectory()) {
                    targetPath = target.getPath() + "/";
                    if (!isTarget(target.getPath()))
                        addTarget(new TargetView(targets.size(), target.getName(), target.getPath(), QratorView.this));
                } else {
                    System.out.println("Target is not a directory!");
                }
                break;
        }
        if (targetPath.isEmpty()) return;
        final String savePath = targetPath;
        if (PROCESSING) {
            processList.forEach(s -> {
                String filePath = getCurrentFilePathPlus(s);
                Path p = Paths.get(filePath);
                displayImage(p);
                String newName = getNewName(s);
                File currentFile = p.toFile();
                if (currentFile != null) {
                    renameFile(currentFile, new File(savePath + newName));
                }
            });
        } else {
            if (currentImageFile != null) {
                String newNameAndPath = targetPath + renameText.getText();
                renameFile(currentImageFile, new File(newNameAndPath));
                removeCurrentAndSelectNext();
            }
        }
    }

    private String getCurrentFilePathPlus(String s) {
        return String.format("%s\\%s", currentFolderFile.getPath(), s);
    }

    @Nullable
    private TreeItem<String> selectNext() {
        if (currentTreeItem.equals(fileTree.getRoot())) return null;
        ObservableList<TreeItem<String>> tiList = fileTree.getRoot().getChildren();
        int index = tiList.indexOf(currentTreeItem);
        TreeItem<String> oldSelection = currentTreeItem;
        do {
            if (++index < tiList.size()) {
                TreeItem<String> item = tiList.get(index);
                if (item.getChildren().size() == 0) {
                    fileTree.getSelectionModel().select(item);
                    break;
                }
            }
        } while (index < tiList.size());
        return oldSelection;
    }

    private void removeCurrentAndSelectNext() {
        TreeItem<String> item = selectNext();
        if (item == null) return;
        item.getParent().getChildren().remove(item);
        fileTree.requestFocus();
    }

    private boolean isTarget(final String targetPath) {
        final BoolWrap retVal = new BoolWrap(false);
        targets.forEach(tv -> {
            if (tv.getDir().equals(targetPath))
                retVal.setValue(true);
        });
        return retVal.getValue();
    }

    private void renameFile(@NotNull File of, File nf) {
        if (of.renameTo(nf)) {
            boolean del = currentImageFile.delete();
            if (!del) {
                currentTreeItem.getParent().getChildren().remove(currentTreeItem);
                fileTree.getRoot().setExpanded(true);
                System.out.println("File moved!");
            } else {
                System.out.println("Error moving file!");
            }
        }
    }

    private String getNewName(@NotNull String filePath) {
        String ext = filePath.substring(filePath.lastIndexOf(".") + 1);
        TextInputDialog td = new TextInputDialog("New Filename");
        td.showAndWait();
        String newFileName = td.getEditor().getText();
        if (newFileName.lastIndexOf('.') == -1)
            newFileName += "." + ext;
        return newFileName;
    }

    @NotNull
    @Contract(pure = true)
    private String coerceName(@NotNull String name) {
        return name.replace("%20", " ");
    }

    @Nullable
    private TreeItem<String> getNodesForDirectory(@NotNull File directory) { //Returns a TreeItem representation of the specified directory
        TreeItem<String> root = new TreeItem<>(directory.getName());
        root.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    root.setGraphic(new Text("x"));
                } else {
                    root.setGraphic(new Text("+"));
                }
            }
        });
        File[] files = directory.listFiles((dir, name) -> (name.endsWith("jpg") || name.endsWith("jpeg") || name.endsWith("png") || name.endsWith("gif")));
        thumbMap.clear();
        if (files == null || files.length == 0) return null;
        System.out.println("Working Directory: " + directory.getPath());
        for(File f : files) {
            if(f.isDirectory()) {
                root.getChildren().add(getNodesForDirectory(f));
            } else {
                String name = coerceName(f.getName());
                System.out.println("Loading " + name);
                TreeItem<String> ti = new TreeItem<>(name);
                Image image = new Image(f.toURI().toString(), 20, 20, false, false, true);
                thumbMap.put(name, image);
                root.getChildren().add(ti);
            }
        }
        root.setExpanded(true);
        return root;
    }

    private Separator getSeparator() {
        final Separator separator = new Separator();
        separator.getStyleClass().add("line-sep");
        separator.setMaxWidth(300);
        separator.setHalignment(HPos.CENTER);
        return separator;
    }

    // TODO Add a delete option

}
