package br.gov.caixa.gitecsa.sired.extra.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;

public class TramiteRequisicaoSuporteDTO {

    private Date dataHoraRegistro;
    private Long quantidade;
    private SuporteVO suporte;
    private BaseVO base;
    private Integer quantidadePapel;
    private Integer quantidadeMicroficha;
    private Integer quantidadeMicrofilme;
    private Integer quantidadeMidiaOptica;

    private Integer quantidadeTotalPapel;
    private Integer quantidadeTotalMicroficha;
    private Integer quantidadeTotalMicrofilme;
    private Integer quantidadeTotalMidiaOptica;

    public String getDataHoraRegistroFormatado() {
        if (dataHoraRegistro != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataHoraRegistro);
        }
        return "";
    }

    public Date getDataHoraRegistro() {
        return dataHoraRegistro;
    }

    public void setDataHoraRegistro(Date dataHoraRegistro) {
        this.dataHoraRegistro = dataHoraRegistro;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    public SuporteVO getSuporte() {
        return suporte;
    }

    public void setSuporte(SuporteVO suporte) {
        this.suporte = suporte;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public Integer getQuantidadeMidiaOptica() {
        return quantidadeMidiaOptica;
    }

    public void setQuantidadeMidiaOptica(Integer quantidadeMidiaOptica) {
        this.quantidadeMidiaOptica = quantidadeMidiaOptica;
    }

    public Integer getQuantidadeMicrofilme() {
        return quantidadeMicrofilme;
    }

    public void setQuantidadeMicrofilme(Integer quantidadeMicrofilme) {
        this.quantidadeMicrofilme = quantidadeMicrofilme;
    }

    public Integer getQuantidadePapel() {
        return quantidadePapel;
    }

    public void setQuantidadePapel(Integer quantidadePapel) {
        this.quantidadePapel = quantidadePapel;
    }

    public Integer getQuantidadeMicroficha() {
        return quantidadeMicroficha;
    }

    public void setQuantidadeMicroficha(Integer quantidadeMicroficha) {
        this.quantidadeMicroficha = quantidadeMicroficha;
    }

    public Integer getQuantidadeTotalPapel() {
        return quantidadeTotalPapel;
    }

    public void setQuantidadeTotalPapel(Integer quantidadeTotalPapel) {
        this.quantidadeTotalPapel = quantidadeTotalPapel;
    }

    public Integer getQuantidadeTotalMicroficha() {
        return quantidadeTotalMicroficha;
    }

    public void setQuantidadeTotalMicroficha(Integer quantidadeTotalMicroficha) {
        this.quantidadeTotalMicroficha = quantidadeTotalMicroficha;
    }

    public Integer getQuantidadeTotalMicrofilme() {
        return quantidadeTotalMicrofilme;
    }

    public void setQuantidadeTotalMicrofilme(Integer quantidadeTotalMicrofilme) {
        this.quantidadeTotalMicrofilme = quantidadeTotalMicrofilme;
    }

    public Integer getQuantidadeTotalMidiaOptica() {
        return quantidadeTotalMidiaOptica;
    }

    public void setQuantidadeTotalMidiaOptica(Integer quantidadeTotalMidiaOptica) {
        this.quantidadeTotalMidiaOptica = quantidadeTotalMidiaOptica;
    }

}
