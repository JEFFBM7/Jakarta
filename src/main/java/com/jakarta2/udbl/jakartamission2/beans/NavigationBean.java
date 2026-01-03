/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jakarta2.udbl.jakartamission2.beans;

import com.jakarta2.udbl.jakartamission2.business.LieuEntrepriseBean;
import com.jakarta2.udbl.jakartamission2.entities.Lieu;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author leona
 */
@Named(value = "navigationController")
@SessionScoped
public class NavigationBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private LieuEntrepriseBean lieuService;
    
    private String lieuNom;
    private String lieuDescription;
    private double lieuLatitude;
    private double lieuLongitude;
    private boolean showDetails = false;
    private List<Lieu> lieux;
    private boolean isEditing = false;
    private Lieu lieuEnEdition;

    private void loadLieux() {
        if (lieuService != null) {
            try {
                lieux = lieuService.listerTousLesLieux();
            } catch (Exception e) {
                e.printStackTrace();
                lieux = new java.util.ArrayList<>();
            }
        } else {
            lieux = new java.util.ArrayList<>();
        }
    }

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
    }

    public void ajouterLieuAction() {
        if (lieuService != null && lieuNom != null && !lieuNom.isEmpty() && lieuDescription != null && !lieuDescription.isEmpty()) {
            try {
                lieuService.ajouterLieuEntreprise(lieuNom, lieuDescription, lieuLatitude, lieuLongitude);
                // Reset fields after adding
                lieuNom = "";
                lieuDescription = "";
                lieuLatitude = 0.0;
                lieuLongitude = 0.0;
                // Force reload on next access
                lieux = null;
                // Redirect to lieu page to see the updated list
                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect("lieu.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void retourAccueil() {
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("home.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void masquerDetails() {
        showDetails = false;
        lieuNom = "";
        lieuDescription = "";
        lieuLatitude = 0.0;
        lieuLongitude = 0.0;
        isEditing = false;
        lieuEnEdition = null;
    }

    public void editerLieu(Lieu lieu) {
        lieuEnEdition = lieu;
        lieuNom = lieu.getNom();
        lieuDescription = lieu.getDescription();
        lieuLatitude = lieu.getLatitude();
        lieuLongitude = lieu.getLongitude();
        isEditing = true;
        showDetails = false; // hide details if shown
    }

    public void sauvegarderEdition() {
        if (lieuEnEdition != null && lieuService != null) {
            lieuEnEdition.setNom(lieuNom);
            lieuEnEdition.setDescription(lieuDescription);
            lieuEnEdition.setLatitude(lieuLatitude);
            lieuEnEdition.setLongitude(lieuLongitude);
            try {
                lieuService.modifierLieu(lieuEnEdition);
                // Reset fields
                masquerDetails();
                // Reload list
                lieux = null;
                // Redirect to lieu page
                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect("lieu.xhtml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void supprimerLieu(int id) {
        if (lieuService != null) {
            try {
                lieuService.supprimerLieu(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadLieux();
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

    public boolean isShowDetails() {
        return showDetails;
    }

    public List<Lieu> getLieux() {
        if (lieux == null) {
            loadLieux();
        }
        return lieux;
    }

    public boolean isEditing() {
        return isEditing;
    }
}

