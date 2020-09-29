package danasoft;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.jetbrains.annotations.Contract;

class TargetView extends BaseView {
    private int id;
    private String name;
    private String dir;
    private BorderPane tView;
    private boolean selected;
    private TargetListener mListener;

    @Contract(pure = true)
    TargetView(){}

    @Contract(pure = true)
    TargetView(int id, String name, String dir, TargetListener l) {
        this.id = id;
        this.name = name;
        this.dir = dir;
        mListener = l;
    }

    int getId() { return id; }
    String getName() { return name; }
    String getDir() { return dir; }
    boolean isSelected() { return selected; }
    void setSelected(boolean selected) { this.selected = selected; createView(); }

    protected void createView() {
        tView = new BorderPane();
        tView.getStyleClass().add("target");
        tView.setOnMousePressed(e -> mListener.targetClicked(id));
        Tooltip t = new Tooltip(dir);
        Tooltip.install(tView, t);
        HBox center = new HBox();
        center.setAlignment(Pos.CENTER);
        center.setPrefWidth(100);
        center.getChildren().add(new Text(name));
        tView.setCenter(center);
        tView.setLeft(new Circle(8, selected ? Color.BLUE : Color.BLACK));
    }

    void set(TargetView tv) {
        if (tv != null) {
            this.id = tv.id;
            this.name = tv.getName();
            this.dir = tv.getDir();
        } else {
            this.id = -1;
            this.name = "";
            this.dir = "";
        }
    }

    @Override
    public Node getView() {
        if (tView == null) createView();
        return tView;
    }

    interface TargetListener {
        void targetClicked(int id);
    }
}