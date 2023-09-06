package io.eagle.util.jts;

public enum DocumentFormatType {
    JSON_CHART("json"),
    JSON_STANDARD("json"),
    JSON("json"),
    CSV("csv"),
    FIXED_WIDTH("dat");

    String extension;

    DocumentFormatType(String extension) {
        this.extension = extension;
    }


    public String getExtension() {
        return extension;
    }

}
