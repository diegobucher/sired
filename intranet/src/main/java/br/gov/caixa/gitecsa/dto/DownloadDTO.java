package br.gov.caixa.gitecsa.dto;

import java.io.Serializable;

import org.primefaces.model.StreamedContent;

public class DownloadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;

    private String link;

    private StreamedContent streamedContent;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public StreamedContent getStreamedContent() {
        return streamedContent;
    }

    public void setStreamedContent(StreamedContent streamedContent) {
        this.streamedContent = streamedContent;
    }

}
