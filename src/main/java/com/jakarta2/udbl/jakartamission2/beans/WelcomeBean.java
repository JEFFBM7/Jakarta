/*
 * WelcomeBean - Gestion de l'authentification utilisateur
 */
package com.jakarta2.udbl.jakartamission2.beans;

import com.jakarta2.udbl.jakartamission2.business.UtilisateurEntrepriseBean;
import com.jakarta2.udbl.jakartamission2.entities.Utilisateur;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Bean gérant l'authentification et la session utilisateur
 */
@Named("welcomeBean")
@SessionScoped
public class WelcomeBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Propriétés pour l'authentification
    private String email;
    private String password;
    
    // Propriétés pour la modification du mot de passe
    private String ancienMotDePasse;
    private String nouveauMotDePasse;
    private String confirmationMotDePasse;
    
    // Utilisateur connecté
    private Utilisateur utilisateurConnecte;
    
    // Injection de la logique métier
    @Inject
    private UtilisateurEntrepriseBean utilisateurEntrepriseBean;
    
    // ==================== GETTERS & SETTERS ====================
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
    
    public void setUtilisateurConnecte(Utilisateur utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     * @return true si connecté, false sinon
     */
    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }
    
    // Getters et Setters pour la modification du mot de passe
    public String getAncienMotDePasse() {
        return ancienMotDePasse;
    }
    
    public void setAncienMotDePasse(String ancienMotDePasse) {
        this.ancienMotDePasse = ancienMotDePasse;
    }
    
    public String getNouveauMotDePasse() {
        return nouveauMotDePasse;
    }
    
    public void setNouveauMotDePasse(String nouveauMotDePasse) {
        this.nouveauMotDePasse = nouveauMotDePasse;
    }
    
    public String getConfirmationMotDePasse() {
        return confirmationMotDePasse;
    }
    
    public void setConfirmationMotDePasse(String confirmationMotDePasse) {
        this.confirmationMotDePasse = confirmationMotDePasse;
    }
    
    // ==================== MÉTHODES D'AUTHENTIFICATION ====================
    
    /**
     * Méthode d'authentification appelée depuis la page de connexion
     * @return la page de redirection (home si succès, null si échec)
     */
    public String authentifier() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Appel de la méthode métier pour authentifier
        Utilisateur utilisateur = utilisateurEntrepriseBean.authentifier(email, password);
        
        if (utilisateur != null) {
            // Authentification réussie
            this.utilisateurConnecte = utilisateur;
            
            // Message de succès
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO, 
                "Connexion réussie", 
                "Bienvenue " + utilisateur.getUsername() + " !"
            ));
            
            // Réinitialiser les champs
            this.email = "";
            this.password = "";
            
            // Redirection vers la page d'accueil
            return "home?faces-redirect=true";
        } else {
            // Authentification échouée
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, 
                "Échec de connexion", 
                "Email ou mot de passe incorrect"
            ));
            
            // Rester sur la même page
            return null;
        }
    }
    
    /**
     * Déconnexion de l'utilisateur
     * @return redirection vers la page d'accueil
     */
    public String deconnecter() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Invalider la session
        context.getExternalContext().invalidateSession();
        
        // Réinitialiser l'utilisateur connecté
        this.utilisateurConnecte = null;
        this.email = "";
        this.password = "";
        
        // Message de déconnexion
        context.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_INFO, 
            "Déconnexion", 
            "Vous avez été déconnecté avec succès"
        ));
        
        return "index?faces-redirect=true";
    }
    
    /**
     * Modifier le mot de passe de l'utilisateur connecté
     * @return null pour rester sur la même page
     */
    public String modifierMotDePasse() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        // Vérifier que l'utilisateur est connecté
        if (utilisateurConnecte == null) {
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Erreur",
                "Vous devez être connecté pour modifier votre mot de passe"
            ));
            return null;
        }
        
        // Vérifier que l'ancien mot de passe est correct
        if (!utilisateurEntrepriseBean.verifierMotDePasse(ancienMotDePasse, utilisateurConnecte.getPassword())) {
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Erreur",
                "L'ancien mot de passe est incorrect"
            ));
            return null;
        }
        
        // Vérifier que les nouveaux mots de passe correspondent
        if (!nouveauMotDePasse.equals(confirmationMotDePasse)) {
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Erreur",
                "Les nouveaux mots de passe ne correspondent pas"
            ));
            return null;
        }
        
        // Vérifier la longueur minimale du nouveau mot de passe
        if (nouveauMotDePasse.length() < 6) {
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Erreur",
                "Le nouveau mot de passe doit contenir au moins 6 caractères"
            ));
            return null;
        }
        
        // Mettre à jour le mot de passe
        try {
            utilisateurEntrepriseBean.modifierMotDePasse(utilisateurConnecte.getId(), nouveauMotDePasse);
            
            // Recharger l'utilisateur avec le nouveau mot de passe
            utilisateurConnecte = utilisateurEntrepriseBean.trouverUtilisateurParId(utilisateurConnecte.getId());
            
            // Réinitialiser les champs
            ancienMotDePasse = "";
            nouveauMotDePasse = "";
            confirmationMotDePasse = "";
            
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Succès",
                "Votre mot de passe a été modifié avec succès"
            ));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Erreur",
                "Une erreur est survenue lors de la modification du mot de passe"
            ));
        }
        
        return null;
    }
}
