package br.gov.caixa.gitecsa.util;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {
    public String id;
    public String link;
    public String styleClass;
    public String nome;
    public List<MenuItem> subItens;

    public MenuItem() {
        subItens = new ArrayList<MenuItem>();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<MenuItem> getSubItens() {
        return subItens;
    }

    public void setSubItens(List<MenuItem> subItens) {
        this.subItens = subItens;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
