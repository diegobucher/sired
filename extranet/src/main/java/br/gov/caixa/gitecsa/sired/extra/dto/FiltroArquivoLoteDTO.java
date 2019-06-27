package br.gov.caixa.gitecsa.sired.extra.dto;

import java.util.Date;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;

public class FiltroArquivoLoteDTO {

    private UsuarioLdap usuario;

    private Date dataInicio;

    private Date dataFim;

    public UsuarioLdap getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioLdap usuario) {
        this.usuario = usuario;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

}
