package br.gov.caixa.gitecsa.dto;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;

public class RelatorioFaturamentoDTO {

    private Integer nuRequisicao;
    private RequisicaoVO requisicao;
    private DocumentoVO documento;
    private BaseVO base;
    private Date dataAbertura;
    private Date dataAtendimento;
    private Date prazoAtendimento;
    private SuporteVO suporte;
    private Integer qtdSolicitada;
    private Integer qtdDisponibilizada;
    private Integer qtdDispNoPrazo;
    private Integer qtdNaoLocalizada;
    private Double idlp;
    private Double idnl;

    // MÉT0D0S DE ACESSO
    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public Integer getNuRequisicao() {
        return nuRequisicao;
    }

    public void setNuRequisicao(Integer nuRequisicao) {
        this.nuRequisicao = nuRequisicao;
    }

    public DocumentoVO getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoVO documento) {
        this.documento = documento;
    }

    public Date getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public String getDataAberturaFormatada() {
        if (dataAbertura != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataAbertura);
        }
        return "";
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
    
    public Date getPrazoAtendimento() {
        return prazoAtendimento;
    }

    public void setPrazoAtendimento(Date prazoAtendimento) {
        this.prazoAtendimento = prazoAtendimento;
    }

    public String getPrazoAtendimentoFormatado() {
        if (prazoAtendimento != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(prazoAtendimento);
        }
        return "";
    }

    public Integer getQtdSolicitada() {
        return qtdSolicitada;
    }

    public void setQtdSolicitada(Integer qtdSolicitada) {
        this.qtdSolicitada = qtdSolicitada;
    }

    public Integer getQtdDisponibilizada() {
        return qtdDisponibilizada;
    }

    public void setQtdDisponibilizada(Integer qtdDisponibilizada) {
        this.qtdDisponibilizada = qtdDisponibilizada;
    }

    public Integer getQtdDispNoPrazo() {
        return qtdDispNoPrazo;
    }

    public void setQtdDispNoPrazo(Integer qtdDispNoPrazo) {
        this.qtdDispNoPrazo = qtdDispNoPrazo;
    }

    public Integer getQtdNaoLocalizada() {
        return qtdNaoLocalizada;
    }

    public void setQtdNaoLocalizada(Integer qtdNaoLocalizada) {
        this.qtdNaoLocalizada = qtdNaoLocalizada;
    }

    /**
     * METODOS DE ACESSO AO ATRIBUTO IDLP
     */
    public Double getIdlp() {
        idlp = (new Double(this.qtdDispNoPrazo) / new Double(this.qtdSolicitada)) * 100;
        return idlp;
    }

    public String getIdlpFormatado() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(getIdlp());
    }

    public void setIdlp(Double idlp) {
        this.idlp = idlp;
    }

    /**
     * METODOS DE ACESSO AO ATRIBUTO IDNL
     */
    public Double getIdnl() {
        idnl = (new Double(getQtdNaoLocalizada()) / new Double(getQtdSolicitada())) * 100;
        return idnl;
    }

    public String getIdnlFormatado() {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(getIdnl());
    }

    public void setIdnl(Double idnl) {
        this.idnl = idnl;
    }

    public RequisicaoVO getRequisicao() {
        return requisicao;
    }

    public void setRequisicao(RequisicaoVO requisicao) {
        this.requisicao = requisicao;
    }

    public SuporteVO getSuporte() {
        return suporte;
    }

    public void setSuporte(SuporteVO suporte) {
        this.suporte = suporte;
    }

}
