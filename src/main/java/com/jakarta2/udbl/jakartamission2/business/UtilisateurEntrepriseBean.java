/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jakarta2.udbl.jakartamission2.business;

import com.jakarta2.udbl.jakartamission2.entities.Utilisateur;
import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author joelm
 */
@Stateless
@LocalBean
public class UtilisateurEntrepriseBean {
    @PersistenceContext
    private EntityManager em;
    private String password;
    @Transactional
    public void ajouterUtilisateurEntreprise(String username, String email, String password, String description) throws Exception {
        // Vérifier si l'utilisateur existe déjà par nom d'utilisateur ou email
        Utilisateur existingUserByUsername = trouverUtilisateurParUsername(username);
        Utilisateur existingUserByEmail = trouverUtilisateurParEmail(email);

        if (existingUserByUsername != null || existingUserByEmail != null) {
            throw new Exception("Ce nom d'utilisateur et cette adresse existent déjà.");
        }
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        Utilisateur utilisateur = new Utilisateur(username, email, hashedPassword, description);
        em.persist(utilisateur);
    }
    public List<Utilisateur> listerTousLesUtilisateurs() {
        return em.createQuery("SELECT u FROM Utilisateur u", Utilisateur.class).getResultList();
    }
    @Transactional
    public void supprimerUtilisateur(Long id) {
        Utilisateur utilisateur = em.find(Utilisateur.class, id);
        if (utilisateur != null) {
            em.remove(utilisateur);
        }
    }
    public Utilisateur trouverUtilisateurParId(Long id) {
        return em.find(Utilisateur.class, id);
    }
    public Utilisateur trouverUtilisateurParEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    public Utilisateur trouverUtilisateurParUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM Utilisateur u WHERE u.username = :username", Utilisateur.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Authentifie un utilisateur avec son email et mot de passe
     * @param email L'email de l'utilisateur
     * @param password Le mot de passe en clair
     * @return L'utilisateur si authentification réussie, null sinon
     */
    public Utilisateur authentifier(String email, String password) {
        Utilisateur utilisateur = trouverUtilisateurParEmail(email);
        if (utilisateur != null && verifierMotDePasse(password, utilisateur.getPassword())) {
            return utilisateur;
        }
        return null;
    }
    
    /**
     * Vérifie si un mot de passe en clair correspond au hash stocké
     * @param plainPassword Le mot de passe en clair
     * @param hashedPassword Le mot de passe hashé
     * @return true si les mots de passe correspondent
     */
    public boolean verifierMotDePasse(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
    
    /**
     * Modifie le mot de passe d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param nouveauMotDePasse Le nouveau mot de passe en clair
     * @return true si la modification a réussi
     */
    @Transactional
    public boolean modifierMotDePasse(Long userId, String nouveauMotDePasse) {
        Utilisateur utilisateur = trouverUtilisateurParId(userId);
        if (utilisateur != null) {
            String hashedPassword = BCrypt.hashpw(nouveauMotDePasse, BCrypt.gensalt());
            utilisateur.setPassword(hashedPassword);
            em.merge(utilisateur);
            return true;
        }
        return false;
    }
}