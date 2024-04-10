package com.example.demo;

public class Fabbisogno {
    private Integer FabbisognoID;
    private Integer OrdineID;
    private Integer ArticoloID;
    private Integer QuantitaFabbisogno;
    
    public Integer getFabbisognoID() {
        return FabbisognoID;
    }
    public void setFabbisognoID(Integer fabbisognoID) {
        FabbisognoID = fabbisognoID;
    }
    public Integer getOrdineID() {
        return OrdineID;
    }
    public void setOrdineID(Integer ordineID) {
        OrdineID = ordineID;
    }
    public Integer getArticoloID() {
        return ArticoloID;
    }
    public void setArticoloID(Integer articoloID) {
        ArticoloID = articoloID;
    }
    public Integer getQuantitaFabbisogno() {
        return QuantitaFabbisogno;
    }
    public void setQuantitaFabbisogno(Integer quantitaFabbisogno) {
        QuantitaFabbisogno = quantitaFabbisogno;
    }
    public Fabbisogno(){}
}
