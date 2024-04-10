package com.example.demo;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class ArticoliController 
{

    // 1. Inserimento di un nuovo ordine. Input: ArticoloID (solo PF quindi solo ArticoloID=1 oppure ArticoloID=2)  
    // e QuantitaDaProdurre (numero intero).  La  WebAPI inserisce il nuovo Ordine in TOrdini. 
    // Non inserire il CostoTotaleSemilavorati perché verrà aggiornato in una chiamata successiva
    @RequestMapping(value = "/aggiungiordine", method=RequestMethod.GET)
    public String requestMethodName(@RequestParam int id,int quant) {
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String dbURL = "jdbc:sqlserver://localhost;encrypt=true;databaseName=DBJava;integratedSecurity=true;trustServerCertificate=true;";
        Connection conn;

        try {
            conn = DriverManager.getConnection(dbURL);
            String insertQuery = "INSERT INTO TOrdini (ArticoloID, QuantitaDaProdurre, ScaricoEffettuato) VALUES (?, ?, 'false')";
            PreparedStatement stat = conn.prepareStatement(insertQuery);
                if (id == 1 || id == 2) {
                    try {
                        stat.setString(1, String.valueOf(id));
                        stat.setString(2, String.valueOf(quant));
                        stat.executeUpdate();
                        return("L'Articolo e la quantità sono stati aggiunti al database!");
                            } catch (Exception e1) {
                                return(e1.getMessage());
                            }
                        } else {
                            System.out.print("Attenzione! Non esiste l'Articolo ID inserito.");
                            System.exit(0);
                        }
                    return "Nessun return";                     
        } catch (SQLException e) {
            return e.toString();
        }
        
    }


    // 2. Calcolo dei fabbisogni per un ordine.  Input: OrdineID.  
    // La WebAPI aggiunge i vari fabbisogni in TFabbisogni per ogni SL  necessario per produrre il PF. 
    // Attenzione che se all’OrdineID sono già associati dei fabbisogni-> vanno prima cancellati altrimenti si rischia di 
    // avere fabbisogni duplicati per un singolo ordine
    @RequestMapping(value="/calcolofabbisogni", method=RequestMethod.GET)
    public String requestMethodName(@RequestParam int ordineid) {

        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String dbURL = "jdbc:sqlserver://localhost;encrypt=true;databaseName=DBJava;integratedSecurity=true;trustServerCertificate=true;";
        Connection conn;
        
        try {
            conn = DriverManager.getConnection(dbURL);
            String checkOrderSql = "SELECT * FROM TFabbisogni WHERE OrdineID = ?";
            PreparedStatement preparedStatementCheck = conn.prepareStatement(checkOrderSql);
            preparedStatementCheck.setInt(1, ordineid);
            ResultSet resultSet = preparedStatementCheck.executeQuery();
            if (resultSet.next()) {
                String deleteFabbisogni = "DELETE FROM TFabbisogni where OrdineID = ?";
                PreparedStatement deleteStatement = conn.prepareStatement(deleteFabbisogni);
                deleteStatement.setInt(1, ordineid);
                deleteStatement.execute();
            }
            String getArticoliQuantita = "SELECT ArticoloID, QuantitaDaProdurre from TOrdini where OrdineID = ?";
            PreparedStatement getStatement = conn.prepareStatement(getArticoliQuantita);
            getStatement.setInt(1, ordineid);
            ResultSet getResult = getStatement.executeQuery();
            int ArticoloID = 0, QuantitaDaProdurre = 0;
            if (getResult.next()) {
                ArticoloID = getResult.getInt("ArticoloID");
                QuantitaDaProdurre = getResult.getInt("QuantitaDaProdurre");
            }
            String selectLegami = "SELECT ArticoloID_figlio, CoefficienteFabbisogno FROM TLegami WHERE ArticoloID_padre = ?";
            PreparedStatement selectLegamiStatement = conn.prepareStatement(selectLegami);
            selectLegamiStatement.setInt(1, ArticoloID);
            ResultSet resultSelect = selectLegamiStatement.executeQuery();
            while (resultSelect.next()) {
                int ArticoloFiglio = resultSelect.getInt("ArticoloID_figlio");
                int CoefficienteFabbisogno = resultSelect.getInt("CoefficienteFabbisogno");

                int QuantitaFabbisogno = CoefficienteFabbisogno * QuantitaDaProdurre;
                String insertFabbisogni = "insert into TFabbisogni (OrdineID, ArticoloID, QuantitaFabbisogno) values (?,?,?)";
                PreparedStatement insertStatement = conn.prepareStatement(insertFabbisogni);

                insertStatement.setInt(1, ordineid);
                insertStatement.setInt(2, ArticoloFiglio);
                insertStatement.setInt(3, QuantitaFabbisogno);

                insertStatement.execute();

            }
            return "La tabella dei fabbisogni è stata aggiornata!";
        } catch (Exception e2) {
            return e2.toString();
        }
    }


    // 3. Visualizzazione dei fabbisogni di un certo OrdineID:  
    // Input: OrdineID. La WebAPI visualizza  i fabbisogni  (TFabbisogni) per ogni SL 
    // (calcolati in precedenza tramite la funzionalità numero 2)
    @RequestMapping(value="/visualizzafabbisogni", method=RequestMethod.GET)
    public String requestMethodName(@RequestParam int ordineid) {
        return "ok";
    }










} //Main