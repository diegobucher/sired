package br.gov.caixa.gitecsa.util;

import java.util.ArrayList;
import java.util.List;

public class MenuUtil {

    public static final String MENU_ITEM_ATIVO_CLASS = "atual";
    public static final String MENU_ITEM_INATIVO_CLASS = "";

    public List<MenuItem> itens;

    public MenuUtil() {
        itens = new ArrayList<MenuItem>();
    }

    public List<MenuItem> getItens() {
        return itens;
    }

    public void setItens(List<MenuItem> itens) {
        this.itens = itens;
    }
}
