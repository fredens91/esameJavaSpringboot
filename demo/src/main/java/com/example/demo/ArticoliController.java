package com.example.demo;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PutMapping;
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
    public String aggiungiOrdine(@RequestParam int id,int quant) {
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
    public String calcoloFabbisogni(@RequestParam int ordineid) {

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
    public String visualizzaFabbisogni(@RequestParam int ordineid) {
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String dbURL = "jdbc:sqlserver://localhost;encrypt=true;databaseName=DBJava;integratedSecurity=true;trustServerCertificate=true;";
        Connection conn;

        try {
            conn = DriverManager.getConnection(dbURL);
            String fabOrderSql = "SELECT * FROM TFabbisogni WHERE OrdineID = ?";
            PreparedStatement statFabOrdine = conn.prepareStatement(fabOrderSql);
            statFabOrdine.setInt(1, ordineid);
            ResultSet resultSet2 = statFabOrdine.executeQuery();

            ArrayList<Fabbisogno> myListFabbisogni = new ArrayList<>();
            while(resultSet2.next()) {
                Fabbisogno myFabbisogno = new Fabbisogno();
                myFabbisogno.setFabbisognoID(resultSet2.getInt(1));
                myFabbisogno.setOrdineID(resultSet2.getInt(2));
                myFabbisogno.setArticoloID(resultSet2.getInt(3));
                myFabbisogno.setQuantitaFabbisogno(resultSet2.getInt(4));
         
                myListFabbisogni.add(myFabbisogno);
            }

            Gson myG=new GsonBuilder().setPrettyPrinting().create();
            String ret=myG.toJson(myListFabbisogni);
            
            return ret; 
         
        } catch (Exception e3) {
            
        }
        return "ok";
    }


    // 4. Scarico Magazzino: Input:  OrdineID. La WebAPI scarica i fabbisogni dalla TArticoli 
    // (fa calare la giacenza di ogni ArticoloID correlato) e memorizza in TOrdini che è stato effettuato 
    // lo scarico di magazzino per quel determinato OrdineID. Attenzione che se TOrdini.
    // ScaricoEffettuato=true è necessario comunicare che lo scarico è già stato effettuato e quindi la procedura 
    // si interrompe altrimenti si rischia uno scarico magazzino multiplo.
    @RequestMapping(value = "/scaricomagazzino", method=RequestMethod.GET)
    public String scaricoMagazzino(@RequestParam int ordineid) {
        try {
            DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String dbURL = "jdbc:sqlserver://localhost;encrypt=true;databaseName=DBJava;integratedSecurity=true;trustServerCertificate=true;";
        Connection conn;
        try {
            conn = DriverManager.getConnection(dbURL);
            String giacOrderSql = "SELECT * FROM TOrdini WHERE OrdineID = ? AND ScaricoEffettuato = 'FALSE'";
            PreparedStatement statGiacOrdine = conn.prepareStatement(giacOrderSql);
            statGiacOrdine.setInt(1, ordineid);

            String scaricaFabQuery = "SELECT * FROM TFabbisogni WHERE OrdineID = ?";
            PreparedStatement statScarica = conn.prepareStatement(scaricaFabQuery);
            statScarica.setInt(1, ordineid);
            ResultSet resultSet4 = statScarica.executeQuery();
            int tmpID, tmpQta, giacenzaAttuale, giacenzaNuova;
            while (resultSet4.next()) {
                tmpID = resultSet4.getInt("ArticoloID");
                tmpQta = resultSet4.getInt("QuantitaFabbisogno");

                String estraiArticoloQuery = "SELECT * FROM TArticoli WHERE ArticoloID = ?";
                PreparedStatement statEstrai = conn.prepareStatement(estraiArticoloQuery);
                statEstrai.setInt(1, tmpID);
                ResultSet resultSet5 = statEstrai.executeQuery();
                if (resultSet5.next()) {

                    giacenzaAttuale = resultSet5.getInt("Giacenza");
                    giacenzaNuova = giacenzaAttuale - tmpQta;

                    String aggiornaGiacenzaQuery = "UPDATE TArticoli SET Giacenza = ? WHERE ArticoloID = ? ";
                    PreparedStatement statAggiorna = conn.prepareStatement(aggiornaGiacenzaQuery);
                    statAggiorna.setInt(1, giacenzaNuova);
                    statAggiorna.setInt(2, tmpID);
                    statAggiorna.executeUpdate();

                }
                System.out.println("Articolo UPDATE");
            }

            String scaricaQuery = "UPDATE TOrdini SET ScaricoEffettuato = 1 WHERE OrdineID = ?";
            PreparedStatement statScarico = conn.prepareStatement(scaricaQuery);
            statScarico.setInt(1, ordineid);
            statScarico.executeUpdate();

            return "Magazzino aggiornato!";

        } catch (Exception e6) {
            System.out.println(e6.getMessage());
        }
              
        return new String();
    }



    //5.  Calcolo del Costo Totale dei Semilavorati per un Ordine.  Input OrdineID.  
    // La WebAPI calcola il costo totale dei semilavorati per quello specifico Ordine 
    // e va ad aggiornare la TOrdini compilando il campo CostoTotaleSemilavorati 
    
    @PutMapping("/calcolacostosemilavorati")
    public String calcolaCostoTotale(@RequestParam Integer ordineId) 
    {
        try 
        {
            String dbURL = "jdbc:sqlserver://localhost; databaseName=DBJava;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
            Connection myConnection = DriverManager.getConnection(dbURL);

            float costoTotaleSemilavorati = 0;

            String queryCostoSemilavorati = "SELECT dbo.TArticoli.ArticoloID, dbo.TFabbisogni.QuantitaFabbisogno, dbo.TArticoli.CostoUnitario FROM dbo.TArticoli INNER JOIN dbo.TFabbisogni ON dbo.TArticoli.ArticoloID = dbo.TFabbisogni.ArticoloID WHERE OrdineID = ?";
            PreparedStatement preparedStatement = myConnection.prepareStatement(queryCostoSemilavorati);
            preparedStatement.setInt(1, ordineId);
            int quantitaFabbisogno;
            float costoUnitario;

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                quantitaFabbisogno = resultSet.getInt("QuantitaFabbisogno");
                costoUnitario = resultSet.getFloat("CostoUnitario");
                
                costoTotaleSemilavorati += costoUnitario * quantitaFabbisogno;   
            }
            String updateTotalCost = "UPDATE dbo.TOrdini SET CostoTotaleSemilavorati = ? WHERE OrdineID = ?";
            PreparedStatement updateStmt = myConnection.prepareStatement(updateTotalCost);
            updateStmt.setFloat(1, costoTotaleSemilavorati);
            updateStmt.setInt(2, ordineId);

            int resultUpdate= updateStmt.executeUpdate();
            if(resultUpdate==1) {
                return "Costo semi lavorati "+costoTotaleSemilavorati;            
            }
            else {
                return("Update non riuscito");
            }



            
        }
    catch (Exception e) {
        return e.toString();
    }
    }

    //6. Calcolo del costo Totale del costo Unitario per un prodotto finito: Input ArticoloID (prodotto finito 1 o 2) :  
    // la WebAPI calcola il costo totale dei semilavorati per quello specifico prodotto finito e va ad aggiornare 
    // la TArticoli compilando il campo CostoUnitario solo per quell’articoloID (1 oppure 2).

    



    





} //Main