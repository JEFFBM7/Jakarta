package com.jakarta2.udbl.jakartamission2.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entité Visite - Représente une visite d'un utilisateur à un lieu
 * Cette entité établit une relation Many-to-One avec Utilisateur et Lieu
 * 
 * @author user
 */
@Entity
@Table(name = "visite")
public class Visite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relation Many-to-One avec Utilisateur
     * Plusieurs visites peuvent être effectuées par un même utilisateur
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    /**
     * Relation Many-to-One avec Lieu
     * Plusieurs visites peuvent concerner un même lieu
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lieu_id", nullable = false)
    private Lieu lieu;

    @Column(name = "date_visite", nullable = false)
    private LocalDateTime dateVisite;

    @Column(length = 500)
    private String commentaire;

    @Column(name = "note")
    private Integer note; // Note sur 5

    // Constructeurs
    public Visite() {
        this.dateVisite = LocalDateTime.now();
    }

    public Visite(Utilisateur utilisateur, Lieu lieu) {
        this.utilisateur = utilisateur;
        this.lieu = lieu;
        this.dateVisite = LocalDateTime.now();
    }

    public Visite(Utilisateur utilisateur, Lieu lieu, String commentaire, Integer note) {
        this.utilisateur = utilisateur;
        this.lieu = lieu;
        this.dateVisite = LocalDateTime.now();
        this.commentaire = commentaire;
        this.note = note;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Lieu getLieu() {
        return lieu;
    }

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
    }

    public LocalDateTime getDateVisite() {
        return dateVisite;
    }

    public void setDateVisite(LocalDateTime dateVisite) {
        this.dateVisite = dateVisite;
    }

    /**
     * Retourne la date de visite formatée pour l'affichage
     */
    public String getDateVisiteFormatee() {
        if (dateVisite == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateVisite.format(formatter);
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Visite)) {
            return false;
        }
        Visite other = (Visite) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "Visite{" +
                "id=" + id +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getUsername() : "null") +
                ", lieu=" + (lieu != null ? lieu.getNom() : "null") +
                ", dateVisite=" + dateVisite +
                ", note=" + note +
                '}';
    }
}
