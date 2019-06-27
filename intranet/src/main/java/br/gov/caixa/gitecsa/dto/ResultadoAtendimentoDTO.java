package br.gov.caixa.gitecsa.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.gov.caixa.gitecsa.sired.util.Util;

/**
 * Classe DTO para armazenar resultado da consulta do Relatório de Atendimento.
 * 
 * @author jteixeira
 *
 */
public class ResultadoAtendimentoDTO {

    private String base;
    private Date dataAtendimento;
    private String documento;
    private Long qtdSolicitada;
    private Long qtdDisponibilizada;
    private Long qtdNaoLocalizada;

    public ResultadoAtendimentoDTO(String base, Date dataAtendimento, String documento, Long qtdSolicitada, Long qtdDisponibilizada, Long qtdNaoLocalizada) {

        super();
        this.base = base;
        this.dataAtendimento = dataAtendimento;
        this.documento = documento;
        this.qtdSolicitada = qtdSolicitada;
        this.qtdDisponibilizada = qtdDisponibilizada;
        this.qtdNaoLocalizada = qtdNaoLocalizada;

    }

    public Date getDataAtendimento() {
        return dataAtendimento;
    }

    public void setDataAtendimento(Date dataAtendimento) {
        this.dataAtendimento = dataAtendimento;
    }

    public String getDataAtendimentoFormatada() {
        if (dataAtendimento != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataAtendimento);
        }
        return "";
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public Long getQtdSolicitada() {
        return qtdSolicitada;
    }

    public void setQtdSolicitada(Long qtdSolicitada) {
        this.qtdSolicitada = qtdSolicitada;
    }

    public Long getQtdDisponibilizada() {
        return qtdDisponibilizada;
    }

    public void setQtdDisponibilizada(Long qtdDisponibilizada) {
        this.qtdDisponibilizada = qtdDisponibilizada;
    }

    public Long getQtdNaoLocalizada() {
        if (!Util.isNullOuVazio(qtdNaoLocalizada) && qtdNaoLocalizada < 0L) {
            qtdNaoLocalizada = 0L;
        }
        return qtdNaoLocalizada;
    }

    public void setQtdNaoLocalizada(Long qtdNaoLocalizada) {
        this.qtdNaoLocalizada = qtdNaoLocalizada;
    }

}
