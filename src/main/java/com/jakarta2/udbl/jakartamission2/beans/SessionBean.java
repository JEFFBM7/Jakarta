package com.jakarta2.udbl.jakartamission2.beans;

import com.jakarta2.udbl.jakartamission2.business.UtilisateurEntrepriseBean;
import com.jakarta2.udbl.jakartamission2.entities.Utilisateur;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("sessionBean")
@SessionScoped
public class SessionBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String email;
    private String password;
    private String ancienMotDePasse;
    private String nouveauMotDePasse;
    private String confirmationMotDePasse;
    private Utilisateur utilisateurConnecte;
    
    @Inject
    private UtilisateurEntrepriseBean utilisateurEntrepriseBean;
    
    // Getters et Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getAncienMotDePasse() { return ancienMotDePasse; }
    public void setAncienMotDePasse(String ancienMotDePasse) { this.ancienMotDePasse = ancienMotDePasse; }
    
    public String getNouveauMotDePasse() { return nouveauMotDePasse; }
    public void setNouveauMotDePasse(String nouveauMotDePasse) { this.nouveauMotDePasse = nouveauMotDePasse; }
    
    public String getConfirmationMotDePasse() { return confirmationMotDePasse; }
    public void setConfirmationMotDePasse(String confirmationMotDePasse) { this.confirmationMotDePasse = confirmationMotDePasse; }
    
    public Utilisateur getUtilisateurConnecte() { return utilisateurConnecte; }
    public void setUtilisateurConnecte(Utilisateur utilisateurConnecte) { this.utilisateurConnecte = utilisateurConnecte; }
    
    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }
    
    public String authentifier() {
        FacesContext context = FacesContext.getCurrentInstance();
        Utilisateur utilisateur = utilisateurEntrepriseBean.authentifier(email, password);
        
        if (utilisateur != null) {
            this.utilisateurConnecte = utilisateur;
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Connexion réussie", "Bienvenue " + utilisateur.getUsername() + " !"));
            this.email = "";
            this.password = "";
            return "home?faces-redirect=true";
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Échec de connexion", "Email ou mot de passe incorrect"));
            return null;
        }
    }
    
    public String deconnecter() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        this.utilisateurConnecte = null;
        return "index?faces-redirect=true";
    }
    
    public String modifierMotDePasse() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (utilisateurConnecte == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Vous devez être connecté"));
            return null;
        }
        
        if (!utilisateurEntrepriseBean.verifierMotDePasse(ancienMotDePasse, utilisateurConnecte.getPassword())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Ancien mot de passe incorrect"));
            return null;
        }
        
        if (!nouveauMotDePasse.equals(confirmationMotDePasse)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Les mots de passe ne correspondent pas"));
            return null;
        }
        
        if (nouveauMotDePasse.length() < 6) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Minimum 6 caractères"));
            return null;
        }
        
        utilisateurEntrepriseBean.modifierMotDePasse(utilisateurConnecte.getId(), nouveauMotDePasse);
        utilisateurConnecte = utilisateurEntrepriseBean.trouverUtilisateurParId(utilisateurConnecte.getId());
        ancienMotDePasse = "";
        nouveauMotDePasse = "";
        confirmationMotDePasse = "";
        
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Mot de passe modifié"));
        return null;
    }
}
