package br.gov.caixa.gitecsa.sired.enumerator;

public enum MimeTypeEnum {

    TXT(".txt", "text/plain"), 
    ZIP(".zip", "application/zip"),
    X_ZIP(".zip", "application/x-zip"),
    ZIP_COMPRESSED(".zip", "application/x-zip-compressed"),
    OCTET_STREAM_ZIP(".zip", "application/octet-stream"),
    PDF(".pdf", "application/pdf"),
    CSV(".csv", "application/vnd.ms-excel");
    
    private final String extension;
    
    private final String mimeType;
    
    private MimeTypeEnum(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }
}
