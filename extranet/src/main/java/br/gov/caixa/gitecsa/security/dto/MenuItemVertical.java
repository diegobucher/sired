package br.gov.caixa.gitecsa.security.dto;

import java.util.List;

import br.gov.caixa.gitecsa.sired.vo.FuncionalidadeVO;

public class MenuItemVertical {

    private String nomeMenu;
    private String linkMenu;
    private int nivelMenu;
    private int perfilConsulta;

    private List<FuncionalidadeVO> lista;

    public int getNivelMenu() {
        return nivelMenu;
    }

    public void setNivelMenu(int nivelMenu) {
        this.nivelMenu = nivelMenu;
    }

    public final String getNomeMenu() {
        return nomeMenu;
    }

    public final void setNomeMenu(String nomeMenu) {
        this.nomeMenu = nomeMenu;
    }

    public final String getLinkMenu() {
        return linkMenu;
    }

    public final void setLinkMenu(String linkMenu) {
        this.linkMenu = linkMenu;
    }

    public final List<FuncionalidadeVO> getLista() {
        return lista;
    }

    public final void setLista(List<FuncionalidadeVO> lista) {
        this.lista = lista;
    }

    public int getPerfilConsulta() {
        return perfilConsulta;
    }

    public void setPerfilConsulta(int perfilConsulta) {
        this.perfilConsulta = perfilConsulta;
    }

}
