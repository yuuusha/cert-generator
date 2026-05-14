package com.diev.generator.entity;

public class RelativeFieldBox {
    private final double xRatio;
    private final double yRatio;
    private final double widthRatio;
    private final double heightRatio;
    private final int fontSize;

    public RelativeFieldBox(double xRatio, double yRatio, double widthRatio, double heightRatio, int fontSize) {
        this.xRatio = xRatio;
        this.yRatio = yRatio;
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        this.fontSize = fontSize;
    }

    public FieldBox toAbsolute(int imageWidth, int imageHeight) {
        int x = (int) (imageWidth * xRatio);
        int y = (int) (imageHeight * yRatio);
        int width = (int) (imageWidth * widthRatio);
        int height = (int) (imageHeight * heightRatio);

        return new FieldBox(x, y, width, height, fontSize);
    }
}
