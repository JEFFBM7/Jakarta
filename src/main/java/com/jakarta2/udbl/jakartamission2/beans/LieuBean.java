/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jakarta2.udbl.jakartamission2.beans;

import com.jakarta2.udbl.jakartamission2.business.LieuEntrepriseBean;
import com.jakarta2.udbl.jakartamission2.entities.Lieu;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leona
 */
@Named(value = "lieuBean")
@RequestScoped
public class LieuBean implements Serializable{

    private String nom;
    private String description;
    private double longitude;
    private double latitude;
    private List<Lieu> lieux = new ArrayList<>();

    @Inject
    private LieuEntrepriseBean lieuEntrepriseBean;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public List<Lieu> getLieux() { return lieuEntrepriseBean.listerTousLesLieux(); }

    public void ajouterLieu() {
        if (nom != null && !nom.isEmpty() && description != null && !description.isEmpty()) {
            lieuEntrepriseBean.ajouterLieuEntreprise(nom, description, latitude, longitude);
        }
    }
}