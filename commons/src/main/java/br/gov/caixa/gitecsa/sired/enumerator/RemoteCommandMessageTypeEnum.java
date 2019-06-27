package br.gov.caixa.gitecsa.sired.enumerator;

import com.google.gson.annotations.SerializedName;

public enum RemoteCommandMessageTypeEnum {
    @SerializedName("error") ERROR, 
    @SerializedName("info") INFO, 
    @SerializedName("warning") WARNING, 
    @SerializedName("success") SUCCESS;
}
