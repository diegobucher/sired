package br.gov.caixa.gitecsa.arquitetura;

public interface BaseEnumNumberValue {

    Long getValue();

    BaseEnumNumberValue getEnumFromValue(Long value);
}
