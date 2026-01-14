package com.jakarta2.udbl.jakartamission2.business;

import com.jakarta2.udbl.jakartamission2.entities.Lieu;
import com.jakarta2.udbl.jakartamission2.entities.Utilisateur;
import com.jakarta2.udbl.jakartamission2.entities.Visite;
import jakarta.ejb.Stateless;
import jakarta.ejb.LocalBean;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * VisiteEntrepriseBean - Bean métier pour gérer les visites
 * Contient la logique métier pour les opérations CRUD sur les visites
 * 
 * @author user
 */
@Stateless
@LocalBean
public class VisiteEntrepriseBean {

    @PersistenceContext(unitName = "indonesiaPU")
    private EntityManager em;

    /**
     * Enregistrer une nouvelle visite
     */
    @Transactional
    public void enregistrerVisite(Utilisateur utilisateur, Lieu lieu) {
        Visite visite = new Visite(utilisateur, lieu);
        em.persist(visite);
    }

    /**
     * Enregistrer une nouvelle visite avec commentaire et note
     */
    @Transactional
    public void enregistrerVisite(Utilisateur utilisateur, Lieu lieu, String commentaire, Integer note) {
        Visite visite = new Visite(utilisateur, lieu, commentaire, note);
        em.persist(visite);
    }

    /**
     * Enregistrer une visite avec IDs
     */
    @Transactional
    public void enregistrerVisite(Long utilisateurId, int lieuId, String commentaire, Integer note) {
        Utilisateur utilisateur = em.find(Utilisateur.class, utilisateurId);
        Lieu lieu = em.find(Lieu.class, lieuId);
        
        if (utilisateur != null && lieu != null) {
            Visite visite = new Visite(utilisateur, lieu, commentaire, note);
            em.persist(visite);
        }
    }

    /**
     * Lister toutes les visites
     */
    public List<Visite> listerToutesLesVisites() {
        return em.createQuery("SELECT v FROM Visite v ORDER BY v.dateVisite DESC", Visite.class)
                .getResultList();
    }

    /**
     * Lister les visites d'un utilisateur
     */
    public List<Visite> listerVisitesParUtilisateur(Long utilisateurId) {
        TypedQuery<Visite> query = em.createQuery(
            "SELECT v FROM Visite v WHERE v.utilisateur.id = :userId ORDER BY v.dateVisite DESC", 
            Visite.class
        );
        query.setParameter("userId", utilisateurId);
        return query.getResultList();
    }

    /**
     * Lister les visites d'un lieu
     */
    public List<Visite> listerVisitesParLieu(int lieuId) {
        TypedQuery<Visite> query = em.createQuery(
            "SELECT v FROM Visite v WHERE v.lieu.id = :lieuId ORDER BY v.dateVisite DESC", 
            Visite.class
        );
        query.setParameter("lieuId", lieuId);
        return query.getResultList();
    }

    /**
     * Trouver une visite par ID
     */
    public Visite trouverVisiteParId(Long id) {
        return em.find(Visite.class, id);
    }

    /**
     * Modifier une visite
     */
    @Transactional
    public void modifierVisite(Visite visite) {
        em.merge(visite);
    }

    /**
     * Supprimer une visite
     */
    @Transactional
    public void supprimerVisite(Long id) {
        Visite visite = em.find(Visite.class, id);
        if (visite != null) {
            em.remove(visite);
        }
    }

    /**
     * Vérifier si un utilisateur a déjà visité un lieu
     */
    public boolean utilisateurAVisiteLieu(Long utilisateurId, int lieuId) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(v) FROM Visite v WHERE v.utilisateur.id = :userId AND v.lieu.id = :lieuId", 
            Long.class
        );
        query.setParameter("userId", utilisateurId);
        query.setParameter("lieuId", lieuId);
        return query.getSingleResult() > 0;
    }

    /**
     * Obtenir le nombre total de visites d'un lieu
     */
    public Long compterVisitesLieu(int lieuId) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(v) FROM Visite v WHERE v.lieu.id = :lieuId", 
            Long.class
        );
        query.setParameter("lieuId", lieuId);
        return query.getSingleResult();
    }

    /**
     * Obtenir la note moyenne d'un lieu
     */
    public Double obtenirNoteMoyenneLieu(int lieuId) {
        TypedQuery<Double> query = em.createQuery(
            "SELECT AVG(v.note) FROM Visite v WHERE v.lieu.id = :lieuId AND v.note IS NOT NULL", 
            Double.class
        );
        query.setParameter("lieuId", lieuId);
        Double result = query.getSingleResult();
        return result != null ? result : 0.0;
    }

    /**
     * Lister les dernières visites (limite)
     */
    public List<Visite> listerDernieresVisites(int limite) {
        TypedQuery<Visite> query = em.createQuery(
            "SELECT v FROM Visite v ORDER BY v.dateVisite DESC", 
            Visite.class
        );
        query.setMaxResults(limite);
        return query.getResultList();
    }
}
