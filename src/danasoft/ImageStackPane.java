package danasoft;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

class ImageStackPane extends StackPane {
    private Canvas mCanvas;
    private ImageView mImageView;

    ImageStackPane() {
        resetPane();
    }

    private void resetPane() {
        mCanvas = new Canvas();
        mImageView = new ImageView();
        getChildren().clear();
        mImageView.setFitHeight(400);
        mImageView.setFitWidth(400);
        mImageView.setPreserveRatio(true);
        mImageView.setVisible(false);
        mCanvas.setHeight(400);
        mCanvas.setWidth(400);
        getChildren().addAll(mCanvas, mImageView);
    }

    void setImage(Image i) {
        mCanvas.getGraphicsContext2D().clearRect(0, 0, 400, 400);
        mCanvas.getGraphicsContext2D().drawImage(i, 0, 0);
        mImageView.setImage(i);
        mImageView.setVisible(false);
    }

    void rotate(double ang) {
        if (!mImageView.isVisible()) mImageView.setVisible(true);
        mCanvas.getGraphicsContext2D().clearRect(0, 0, 400, 400);
        mImageView.setRotate(mImageView.getRotate() + ang);
    }

    Image getImage() {
        return mImageView.snapshot(null, null);
    }
}
