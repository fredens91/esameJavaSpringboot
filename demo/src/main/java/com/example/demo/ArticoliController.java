package com.example.demo;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RestController
public class ArticoliController {
   @RequestMapping(value="/articoli", method = RequestMethod.GET)
    public String articolo() 
    {
        try 
        {
            String dbURL = "jdbc:sqlserver://localhost;encrypt=true;databaseName=DBJava;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
            ResultSet myResultSet = null;
            Connection myConnection = DriverManager.getConnection(dbURL);
            Statement myStatement = (Statement) myConnection.createStatement();
            String strSQL = "select * from TArticoli";
            myResultSet = ((java.sql.Statement) myStatement).executeQuery(strSQL);

            ArrayList<Articolo> myListArticoli = new ArrayList<>();
            while(myResultSet.next()) {
                Articolo myArticolo = new Articolo();
                myArticolo.setArticoloID(myResultSet.getInt(1));
                myArticolo.setNome(myResultSet.getString(2));
                myArticolo.setTipologia(myResultSet.getString(3));
                myArticolo.setGiacenza(myResultSet.getInt(4));
                myArticolo.setPrezzoUnitario(myResultSet.getFloat(5));
         
                myListArticoli.add(myArticolo);
            }
            Gson myG=new GsonBuilder().setPrettyPrinting().create();
            String ret=myG.toJson(myListArticoli);  
            
            return ret; 

        }   
        catch(Exception ex) {
            return("Error: "+ex);
        }
    }
}
