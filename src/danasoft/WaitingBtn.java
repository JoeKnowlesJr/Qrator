package danasoft;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WaitingBtn extends Button {
    private final ImageView iv;
    private final Image waitingImg;
    private final Image readyImg;

    public WaitingBtn(final Image wImg, final Image rImg) {
        super();
        waitingImg = wImg;
        readyImg = rImg;
        iv = new ImageView(readyImg);
        iv.setFitWidth(24);
        iv.setFitHeight(24);
        this.getChildren().add(iv);
        super.setGraphic(iv);
    }

    void setWaiting(boolean waiting) {
        iv.setImage( waiting ? waitingImg : readyImg);

    }
}
