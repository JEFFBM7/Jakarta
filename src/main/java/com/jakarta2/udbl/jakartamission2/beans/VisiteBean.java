package com.jakarta2.udbl.jakartamission2.beans;

import com.jakarta2.udbl.jakartamission2.business.LieuEntrepriseBean;
import com.jakarta2.udbl.jakartamission2.business.UtilisateurEntrepriseBean;
import com.jakarta2.udbl.jakartamission2.business.VisiteEntrepriseBean;
import com.jakarta2.udbl.jakartamission2.entities.Lieu;
import com.jakarta2.udbl.jakartamission2.entities.Utilisateur;
import com.jakarta2.udbl.jakartamission2.entities.Visite;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * VisiteBean - Managed Bean pour la gestion des visites dans l'interface utilisateur
 * Gère les interactions entre la vue JSF et la couche métier
 * 
 * Rôle : Ce bean fait le lien entre les pages XHTML (vues) et la couche métier (VisiteEntrepriseBean)
 * Il gère l'enregistrement des visites, l'affichage de l'historique et les statistiques.
 * 
 * @author user
 */
@Named(value = "visiteBean")
@SessionScoped
public class VisiteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private VisiteEntrepriseBean visiteService;

    @Inject
    private LieuEntrepriseBean lieuService;

    @Inject
    private UtilisateurEntrepriseBean utilisateurService;
    
    @Inject
    private SessionBean sessionBean; // Injection du SessionBean pour accéder à l'utilisateur connecté

    // Propriétés pour l'enregistrement d'une visite
    private int lieuId;
    private String commentaire;
    private Integer note;
    
    // Liste des visites
    private List<Visite> visites;
    private List<Visite> mesVisites;
    private Visite visiteSelectionnee;

    // Constructeur
    public VisiteBean() {
    }
    
    /**
     * Récupérer l'utilisateur connecté depuis le SessionBean
     * Utilise l'injection CDI pour accéder à l'utilisateur authentifié
     */
    private Utilisateur getUtilisateurConnecte() {
        return sessionBean.getUtilisateurConnecte();
    }

    /**
     * Enregistrer une nouvelle visite
     */
    public String enregistrerVisite() {
        try {
            Utilisateur utilisateurConnecte = getUtilisateurConnecte();
            
            if (utilisateurConnecte == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Erreur", "Vous devez être connecté pour enregistrer une visite."));
                return null;
            }

            if (lieuId <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Erreur", "Veuillez sélectionner un lieu."));
                return null;
            }

            // Vérifier si l'utilisateur a déjà visité ce lieu
            if (visiteService.utilisateurAVisiteLieu(utilisateurConnecte.getId(), lieuId)) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                        "Attention", "Vous avez déjà enregistré une visite pour ce lieu."));
            }

            visiteService.enregistrerVisite(
                utilisateurConnecte.getId(), 
                lieuId, 
                commentaire, 
                note
            );

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Succès", "Visite enregistrée avec succès !"));

            // Réinitialiser le formulaire
            reinitialiserFormulaire();
            
            return "mesVisites.xhtml?faces-redirect=true";
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Erreur", "Erreur lors de l'enregistrement de la visite : " + e.getMessage()));
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Enregistrer une visite rapide (depuis la page d'un lieu)
     */
    public String enregistrerVisiteRapide(int idLieu) {
        this.lieuId = idLieu;
        return enregistrerVisite();
    }

    /**
     * Charger toutes les visites
     */
    public void chargerToutesLesVisites() {
        try {
            visites = visiteService.listerToutesLesVisites();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Erreur", "Erreur lors du chargement des visites."));
            e.printStackTrace();
        }
    }

    /**
     * Charger les visites de l'utilisateur connecté
     */
    public void chargerMesVisites() {
        try {
            Utilisateur utilisateurConnecte = getUtilisateurConnecte();
            if (utilisateurConnecte != null) {
                mesVisites = visiteService.listerVisitesParUtilisateur(utilisateurConnecte.getId());
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Erreur", "Erreur lors du chargement de vos visites."));
            e.printStackTrace();
        }
    }

    /**
     * Supprimer une visite
     */
    public void supprimerVisite(Long visiteId) {
        try {
            visiteService.supprimerVisite(visiteId);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Succès", "Visite supprimée avec succès."));
            chargerMesVisites();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Erreur", "Erreur lors de la suppression de la visite."));
            e.printStackTrace();
        }
    }

    /**
     * Vérifier si l'utilisateur a visité un lieu
     */
    public boolean aVisiteLieu(int idLieu) {
        Utilisateur utilisateurConnecte = getUtilisateurConnecte();
        if (utilisateurConnecte != null) {
            return visiteService.utilisateurAVisiteLieu(utilisateurConnecte.getId(), idLieu);
        }
        return false;
    }

    /**
     * Obtenir le nombre de visites d'un lieu
     */
    public Long getNombreVisitesLieu(int lieuId) {
        return visiteService.compterVisitesLieu(lieuId);
    }

    /**
     * Obtenir la note moyenne d'un lieu
     */
    public Double getNoteMoyenneLieu(int lieuId) {
        return visiteService.obtenirNoteMoyenneLieu(lieuId);
    }

    /**
     * Réinitialiser le formulaire
     */
    private void reinitialiserFormulaire() {
        lieuId = 0;
        commentaire = null;
        note = null;
    }

    // Getters et Setters
    public int getLieuId() {
        return lieuId;
    }

    public void setLieuId(int lieuId) {
        this.lieuId = lieuId;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public List<Visite> getVisites() {
        if (visites == null) {
            chargerToutesLesVisites();
        }
        return visites;
    }

    public void setVisites(List<Visite> visites) {
        this.visites = visites;
    }

    public List<Visite> getMesVisites() {
        if (mesVisites == null) {
            chargerMesVisites();
        }
        return mesVisites;
    }

    public void setMesVisites(List<Visite> mesVisites) {
        this.mesVisites = mesVisites;
    }

    public Visite getVisiteSelectionnee() {
        return visiteSelectionnee;
    }

    public void setVisiteSelectionnee(Visite visiteSelectionnee) {
        this.visiteSelectionnee = visiteSelectionnee;
    }
}
