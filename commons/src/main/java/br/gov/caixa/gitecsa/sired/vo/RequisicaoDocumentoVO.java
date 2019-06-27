package br.gov.caixa.gitecsa.sired.vo;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;

@Entity
@Table(name = "redtbc15_requisicao_documento", schema = Constantes.SCHEMADB_NAME)
public class RequisicaoDocumentoVO extends BaseEntity {

    private static final long serialVersionUID = 3642537522968409385L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_requisicao_documento", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_unidade_geradora_a02")
    private UnidadeVO unidadeGeradora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_tipo_demanda_a04", columnDefinition = "int2")
    private TipoDemandaVO tipoDemanda;

    @Column(name = "nu_documento_exigido")
    private String nuDocumentoExigido;

    @Column(name = "de_observacao")
    private String observacao;

    @Column(name = "nu_valor")
    private String nuValor;

    @Column(name = "no_nome")
    private String nome;

    @Column(name = "nu_documento")
    private String numeroDocumento;

    @Column(name = "nu_lote_sequencia")
    private String nuLoteSequencia;

    @Column(name = "nu_conta", columnDefinition = "bpchar")
    private String numeroConta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_operacao_a11", columnDefinition = "bpchar")
    private OperacaoVO operacao;

    @Column(name = "nu_digito_verificador", columnDefinition = "bpchar")
    private String nuDigitoVerificador;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_data_geracao")
    private Date dataGeracao;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_inicio")
    private Date dataInicio;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_fim")
    private Date dataFim;

    @Column(name = "nu_mes_ano_fim", columnDefinition = "bpchar")
    private String nuMesAnoFim;

    @Column(name = "nu_mes_ano_inicio", columnDefinition = "bpchar")
    private String nuMesAnoInicio;

    @Column(name = "nu_codigo_evento", columnDefinition = "bpchar")
    private String CodigoEvento;

    @Column(name = "nu_caixa_arquivo", columnDefinition = "int4")
    private Integer nuCaixaArquivo;

    @Column(name = "nu_cnpj")
    private String numeroCnpj;

    @Column(name = "nu_cpf")
    private String numeroCpf;

    @Column(name = "nu_codigo_fundo", columnDefinition = "int2")
    private Integer nuCodigoFundo;

    @Column(name = "nu_terminal_financeiro", columnDefinition = "bpchar")
    private String nuTerminalFinanceiro;

    @Column(name = "nu_encerramento", columnDefinition = "int2")
    private Integer nuEncerramento;
    
    @Column(name = "nu_folder", columnDefinition = "numeric")
    private BigDecimal numeroFolder;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumeroCnpj() {
        return numeroCnpj;
    }

    public void setNumeroCnpj(String cnpj) {
        if (!ObjectUtils.isNullOrEmpty(cnpj)) {
            cnpj = cnpj.replace(".", "").replace("/", "").replace("-", "");
        }
        this.numeroCnpj = cnpj;
    }

    public String getCodigoEvento() {
        return CodigoEvento;
    }

    public void setCodigoEvento(String codigoEvento) {
        CodigoEvento = codigoEvento;
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }

    public String getNumeroCpf() {
        return numeroCpf;
    }

    public void setNumeroCpf(String cpf) {
        if (!ObjectUtils.isNullOrEmpty(cpf)) {
            cpf = cpf.replace(".", "").replace("-", "");
        }
        this.numeroCpf = cpf;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNuDocumentoExigido() {
        return nuDocumentoExigido;
    }

    public void setNuDocumentoExigido(String nuDocumentoExigido) {
        this.nuDocumentoExigido = nuDocumentoExigido;
    }

    public String getNuLoteSequencia() {
        return nuLoteSequencia;
    }

    public void setNuLoteSequencia(String nuLoteSequencia) {
        this.nuLoteSequencia = nuLoteSequencia;
    }

    public String getNuMesAnoFim() {
        return nuMesAnoFim;
    }

    public void setNuMesAnoFim(String nuMesAnoFim) {
        this.nuMesAnoFim = nuMesAnoFim;
    }

    public String getNuMesAnoInicio() {
        return nuMesAnoInicio;
    }

    public void setNuMesAnoInicio(String nuMesAnoInicio) {
        this.nuMesAnoInicio = nuMesAnoInicio;
    }

    public String getNuTerminalFinanceiro() {
        return nuTerminalFinanceiro;
    }

    public void setNuTerminalFinanceiro(String nuTerminalFinanceiro) {
        this.nuTerminalFinanceiro = nuTerminalFinanceiro;
    }

    public String getNuValor() {
        return nuValor;
    }

    public void setNuValor(String nuValor) {
        this.nuValor = nuValor;
    }

    public UnidadeVO getUnidadeGeradora() {
        return unidadeGeradora;
    }

    public void setUnidadeGeradora(UnidadeVO unidadeGeradora) {
        this.unidadeGeradora = unidadeGeradora;
    }

    public TipoDemandaVO getTipoDemanda() {
        return tipoDemanda;
    }

    public void setTipoDemanda(TipoDemandaVO tipoDemanda) {
        this.tipoDemanda = tipoDemanda;
    }

    public OperacaoVO getOperacao() {
        return operacao;
    }

    public void setOperacao(OperacaoVO operacao) {
        this.operacao = operacao;
    }

    public String getNuDigitoVerificador() {
        return nuDigitoVerificador;
    }

    public void setNuDigitoVerificador(String nuDigitoVerificador) {
        this.nuDigitoVerificador = nuDigitoVerificador;
    }

    public Date getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Date dataGeracao) {
        this.dataGeracao = dataGeracao;
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

    public Integer getNuCaixaArquivo() {
        return nuCaixaArquivo;
    }

    public void setNuCaixaArquivo(Integer nuCaixaArquivo) {
        this.nuCaixaArquivo = nuCaixaArquivo;
    }

    public Integer getNuCodigoFundo() {
        return nuCodigoFundo;
    }

    public void setNuCodigoFundo(Integer nuCodigoFundo) {
        this.nuCodigoFundo = nuCodigoFundo;
    }

    public Integer getNuEncerramento() {
        return nuEncerramento;
    }

    public void setNuEncerramento(Integer nuEncerramento) {
        this.nuEncerramento = nuEncerramento;
    }

    public BigDecimal getNumeroFolder() {
      return numeroFolder;
    }

    public void setNumeroFolder(BigDecimal numeroFolder) {
      this.numeroFolder = numeroFolder;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequisicaoDocumentoVO other = (RequisicaoDocumentoVO) obj;
        if (CodigoEvento == null) {
            if (other.CodigoEvento != null)
                return false;
        } else if (!CodigoEvento.trim().equals(other.CodigoEvento.trim()))
            return false;
        if (dataFim == null) {
            if (other.dataFim != null)
                return false;
        } else if (!dataFim.equals(other.dataFim))
            return false;
        if (dataGeracao == null) {
            if (other.dataGeracao != null)
                return false;
        } else if (!dataGeracao.equals(other.dataGeracao))
            return false;
        if (dataInicio == null) {
            if (other.dataInicio != null)
                return false;
        } else if (!dataInicio.equals(other.dataInicio))
            return false;
        if (nome == null) {
            if (other.nome != null)
                return false;
        } else if (other.nome == null || !nome.trim().equals(other.nome.trim()))
            return false;
        if (nuCaixaArquivo == null) {
            if (other.nuCaixaArquivo != null)
                return false;
        } else if (!nuCaixaArquivo.equals(other.nuCaixaArquivo))
            return false;
        if (nuCodigoFundo == null) {
            if (other.nuCodigoFundo != null)
                return false;
        } else if (!nuCodigoFundo.equals(other.nuCodigoFundo))
            return false;
        if (nuDigitoVerificador == null) {
            if (other.nuDigitoVerificador != null)
                return false;
        } else if (other.nuDigitoVerificador == null || !nuDigitoVerificador.trim().equals(other.nuDigitoVerificador.trim()))
            return false;
        if (nuDocumentoExigido == null) {
            if (other.nuDocumentoExigido != null)
                return false;
        } else if (other.nuDocumentoExigido == null || !nuDocumentoExigido.trim().equals(other.nuDocumentoExigido.trim()))
            return false;
        if (nuLoteSequencia == null) {
            if (other.nuLoteSequencia != null)
                return false;
        } else if (other.nuLoteSequencia == null || !nuLoteSequencia.trim().equals(other.nuLoteSequencia.trim()))
            return false;
        if (nuMesAnoFim == null) {
            if (other.nuMesAnoFim != null)
                return false;
        } else if (other.nuMesAnoFim == null || !nuMesAnoFim.trim().equals(other.nuMesAnoFim.trim()))
            return false;
        if (nuMesAnoInicio == null) {
            if (other.nuMesAnoInicio != null)
                return false;
        } else if (other.nuMesAnoInicio == null || !nuMesAnoInicio.trim().equals(other.nuMesAnoInicio.trim()))
            return false;
        if (nuTerminalFinanceiro == null) {
            if (other.nuTerminalFinanceiro != null)
                return false;
        } else if (other.nuTerminalFinanceiro == null || !nuTerminalFinanceiro.trim().equals(other.nuTerminalFinanceiro.trim()))
            return false;
        if (nuValor == null) {
            if (other.nuValor != null)
                return false;
        } else if (other.nuValor == null || !nuValor.trim().equals(other.nuValor.trim()))
            return false;
        if (numeroCnpj == null) {
            if (other.numeroCnpj != null)
                return false;
        } else if (other.numeroCnpj == null || !numeroCnpj.trim().equals(other.numeroCnpj.trim()))
            return false;
        if (numeroConta == null) {
            if (other.numeroConta != null)
                return false;
        } else if (other.numeroConta == null || !numeroConta.trim().equals(other.numeroConta.trim()))
            return false;
        if (numeroCpf == null) {
            if (other.numeroCpf != null)
                return false;
        } else if (other.numeroCpf == null || !numeroCpf.trim().equals(other.numeroCpf.trim()))
            return false;
        if (numeroDocumento == null) {
            if (other.numeroDocumento != null)
                return false;
        } else if (other.numeroDocumento == null || !numeroDocumento.trim().equals(other.numeroDocumento.trim()))
            return false;
        if (operacao == null) {
            if (other.operacao != null)
                return false;
        } else if (!operacao.equals(other.operacao))
            return false;
        if (tipoDemanda == null) {
            if (other.tipoDemanda != null)
                return false;
        } else if (!tipoDemanda.equals(other.tipoDemanda))
            return false;
        if (unidadeGeradora == null) {
            if (other.unidadeGeradora != null)
                return false;
        } else if (!unidadeGeradora.equals(other.unidadeGeradora))
            return false;
        if (nuEncerramento == null) {
            if (other.nuEncerramento != null)
                return false;
        } else if (!nuEncerramento.equals(other.nuEncerramento))
            return false;
        if (numeroFolder == null) {
            if (other.numeroFolder != null)
                return false;
        } else if (!numeroFolder.equals(other.numeroFolder))
            return false;
        
        return true;
    }

}
