/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jakarta2.udbl.jakartamission2.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;

/**
 *
 * @author leona
 */
@Named(value = "navigationController")
@RequestScoped
public class NavigationBean {
    
    private String lieuNom;
    private String lieuDescription;
    private double lieuLatitude;
    private double lieuLongitude;

    public void voirApropos(){
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("a_propos.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void ajouterLieu() {
        
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                  .redirect("lieu.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Logique temporaire : afficher les valeurs dans la console
        System.out.println("Nom: " + lieuNom);
        System.out.println("Description: " + lieuDescription);
        System.out.println("Latitude: " + lieuLatitude);
        System.out.println("Longitude: " + lieuLongitude);
        // Remettre à zéro
        lieuNom = "";
        lieuDescription = "";
        lieuLatitude = 0.0;
        lieuLongitude = 0.0;
    }

    // Getters and setters
    public String getLieuNom() {
        return lieuNom;
    }

    public void setLieuNom(String lieuNom) {
        this.lieuNom = lieuNom;
    }

    public String getLieuDescription() {
        return lieuDescription;
    }

    public void setLieuDescription(String lieuDescription) {
        this.lieuDescription = lieuDescription;
    }

    public double getLieuLatitude() {
        return lieuLatitude;
    }

    public void setLieuLatitude(double lieuLatitude) {
        this.lieuLatitude = lieuLatitude;
    }

    public double getLieuLongitude() {
        return lieuLongitude;
    }

    public void setLieuLongitude(double lieuLongitude) {
        this.lieuLongitude = lieuLongitude;
    }
}
