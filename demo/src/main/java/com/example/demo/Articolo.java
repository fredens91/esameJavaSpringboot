package com.example.demo;

public class Articolo {
    private Integer ArticoloID;
    private String Nome;
    private String Tipologia;
    private Integer Giacenza;
    private float PrezzoUnitario;
    public Integer getArticoloID() {
        return ArticoloID;
    }
    public void setArticoloID(Integer articoloID) {
        ArticoloID = articoloID;
    }
    public String getNome() {
        return Nome;
    }
    public void setNome(String nome) {
        Nome = nome;
    }
    public String getTipologia() {
        return Tipologia;
    }
    public void setTipologia(String tipologia) {
        Tipologia = tipologia;
    }
    public Integer getGiacenza() {
        return Giacenza;
    }
    public void setGiacenza(Integer giacenza) {
        Giacenza = giacenza;
    }
    public float getPrezzoUnitario() {
        return PrezzoUnitario;
    }
    public void setPrezzoUnitario(float prezzoUnitario) {
        PrezzoUnitario = prezzoUnitario;
    }
    public Articolo(){}
}
