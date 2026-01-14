# Reponses aux Questions - Partie Visite

## Questions et Reponses

### 1. Quel est le role de l entite Visite dans notre application ?

L entite **Visite** sert de **table d association enrichie** entre Utilisateur et Lieu. Elle permet de :
- **Tracer** qui a visite quel lieu et quand
- **Enregistrer** des informations supplementaires (date, commentaire, note)
- **Calculer** des statistiques (nombre de visites, notes moyennes)
- **Gerer** l historique complet des visites de chaque utilisateur

**Structure de l entite** :

```java
@Entity
@Table(name = "visite")
public class Visite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;  // Cle etrangere vers Utilisateur
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lieu_id", nullable = false)
    private Lieu lieu;                 // Cle etrangere vers Lieu
    
    private LocalDateTime dateVisite;
    private String commentaire;
    private Integer note;              // Note sur 5
}
```

**En resume** : C est le journal des visites qui relie les utilisateurs aux lieux avec des metadonnees.

---

### 2. Pourquoi utilisons-nous @ManyToOne dans l entite Visite ?

Nous utilisons `@ManyToOne` car c est le type de relation approprie pour notre cas d usage :

**Relation Visite -> Utilisateur** :
- **Plusieurs visites** peuvent etre effectuees par **un seul utilisateur**
- Un utilisateur peut visiter plusieurs lieux differents
- Chaque visite appartient a exactement un utilisateur

**Relation Visite -> Lieu** :
- **Plusieurs visites** peuvent concerner **un seul lieu**
- Un lieu peut etre visite par plusieurs utilisateurs
- Chaque visite concerne exactement un lieu

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "utilisateur_id", nullable = false)
private Utilisateur utilisateur;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "lieu_id", nullable = false)
private Lieu lieu;
```

**Avantages de @ManyToOne** :
1. Cree automatiquement les cles etrangeres (utilisateur_id, lieu_id)
2. Garantit l integrite referentielle (pas de visite sans utilisateur/lieu valide)
3. Permet d enrichir la relation avec des attributs (date, note, commentaire)
4. Plus flexible que @ManyToMany qui ne permet pas d attributs supplementaires

**Comparaison avec les alternatives** :

| Annotation | Description | Notre cas |
|------------|-------------|-----------|
| @ManyToOne | Plusieurs visites -> Un utilisateur/lieu | **Solution optimale** |
| @ManyToMany | Plusieurs utilisateurs <-> Plusieurs lieux | Ne permet pas d attributs |
| @OneToOne | Une visite -> Un utilisateur | Trop restrictif |

---

### 3. Quel est le role principal de l API JakartaWeatherResource ?

L API **JakartaWeatherResource** est une **API REST** qui :

- Sert de **pont** entre l application et l API meteo externe (Open-Meteo)
- **Recupere** les donnees meteo en temps reel selon les coordonnees GPS
- **Enrichit** l experience utilisateur avec des informations contextuelles
- **Valide** les parametres et gere les erreurs

```java
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JakartaWeatherResource {
    
    @GET
    public Response getWeather(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude) {
        // Appel a l API Open-Meteo
        // Validation des coordonnees
        // Retour des donnees meteo en JSON
    }
}
```

**Endpoint principal** :
```
GET /api/weather?latitude={lat}&longitude={lon}
```

**Exemple d utilisation** : 
- Pour Jakarta : GET /api/weather?latitude=-6.2088&longitude=106.8456
- Pour Bali : GET /api/weather?latitude=-8.4095&longitude=115.1889

---

### 4. Pourquoi avons-nous utilise @GET et @QueryParam pour recuperer la meteo d un lieu ?

#### Pourquoi @GET ?

- **Semantique REST** : GET = lecture de donnees (pas de modification serveur)
- **Idempotence** : Plusieurs appels identiques donnent le meme resultat
- **Cache** : Les reponses GET peuvent etre mises en cache par le navigateur
- **Simplicite** : Testable directement dans un navigateur web

#### Pourquoi @QueryParam ?

```java
@GET
public Response getWeather(
        @QueryParam("latitude") double latitude,
        @QueryParam("longitude") double longitude) {
    // ...
}
```

- **Lisibilite** : URL explicite /weather?latitude=X&longitude=Y
- **Flexibilite** : Facile d ajouter de nouveaux parametres optionnels
- **Standard** : Methode standard pour passer des donnees avec GET
- **Multiples parametres** : Gere facilement plusieurs valeurs

**Alternative rejetee** : @PathParam donnerait /weather/{lat}/{lon} -> moins lisible pour des valeurs decimales.

---

### 5. Comment et a quel niveau avez-vous integre cette API dans l application ?

**Niveau d integration** : **Couche REST (Ressources)**

```
Architecture de l application :

+----------------------------------------------------------------+
|                         VUES (XHTML)                            |
|  enregistrerVisite.xhtml | mesVisites.xhtml | lieuDetails.xhtml |
|                    | Actions JSF / JavaScript                   |
+----------------------------------------------------------------+
|                    MANAGED BEANS (JSF)                          |
|         VisiteBean | LieuBean | SessionBean                     |
|         - Gestion des formulaires                               |
|         - Interaction avec l utilisateur                        |
|                    | Injection @Inject                          |
+----------------------------------------------------------------+
|                   COUCHE METIER (EJB)                           |
|     VisiteEntrepriseBean | LieuEntrepriseBean                   |
|         - Logique metier                                        |
|         - Operations CRUD                                       |
|                    | EntityManager                              |
+----------------------------------------------------------------+
|  [REST API] JakartaWeatherResource  <- INTEGRATION ICI          |
|     @Path("/weather")                                           |
|     - Endpoints HTTP GET                                        |
|     - Appel API externe (Open-Meteo)                            |
+----------------------------------------------------------------+
|                      ENTITES (JPA)                              |
|            Visite | Utilisateur | Lieu                          |
|         - Mapping objet-relationnel                             |
|         - Relations @ManyToOne                                  |
|                    | SQL                                        |
+----------------------------------------------------------------+
|                    BASE DE DONNEES                              |
|         Tables: visite, utilisateur, lieu                       |
|         Cles etrangeres: utilisateur_id, lieu_id                |
+----------------------------------------------------------------+
```

**Configuration REST** :
```java
@ApplicationPath("/api")
public class JakartaRestConfiguration extends Application { }
```

**URL finale d acces** : http://localhost:8080/jakartamission2-1.0/api/weather?latitude=X&longitude=Y

**Modes d utilisation** :
1. **Depuis JavaScript** dans les pages XHTML pour afficher la meteo
2. **Depuis les Managed Beans** pour enrichir les donnees des lieux
3. **Depuis des applications externes** via appels REST

---

### 6. Etapes de mise en place de la partie visite (elements essentiels)

#### ETAPE 1 : Creation de l entite Visite (Couche Persistance)

```java
// entities/Visite.java
@Entity
@Table(name = "visite")
public class Visite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lieu_id", nullable = false)
    private Lieu lieu;
    
    @Column(name = "date_visite", nullable = false)
    private LocalDateTime dateVisite;
    
    @Column(length = 500)
    private String commentaire;
    
    private Integer note; // Note sur 5
    
    // Constructeurs, getters, setters...
}
```

**Points cles** :
- @Entity : Declare la classe comme entite JPA
- @ManyToOne : Relations avec Utilisateur et Lieu
- @JoinColumn : Specifie les colonnes de cles etrangeres

---

#### ETAPE 2 : Creation du VisiteEntrepriseBean (Couche Metier)

```java
// business/VisiteEntrepriseBean.java
@Stateless
@LocalBean
public class VisiteEntrepriseBean {
    
    @PersistenceContext(unitName = "indonesiaPU")
    private EntityManager em;
    
    // Enregistrer une visite
    @Transactional
    public void enregistrerVisite(Long utilisateurId, int lieuId, 
                                   String commentaire, Integer note) {
        Utilisateur user = em.find(Utilisateur.class, utilisateurId);
        Lieu lieu = em.find(Lieu.class, lieuId);
        
        if (user != null && lieu != null) {
            Visite visite = new Visite(user, lieu, commentaire, note);
            em.persist(visite);
        }
    }
    
    // Lister les visites d un utilisateur
    public List<Visite> listerVisitesParUtilisateur(Long utilisateurId) {
        return em.createQuery(
            "SELECT v FROM Visite v WHERE v.utilisateur.id = :userId ORDER BY v.dateVisite DESC", 
            Visite.class)
            .setParameter("userId", utilisateurId)
            .getResultList();
    }
    
    // Autres methodes : supprimer, compter, noter moyenne...
}
```

**Points cles** :
- @Stateless : EJB sans etat pour les operations transactionnelles
- @PersistenceContext : Injection de l EntityManager
- @Transactional : Gestion automatique des transactions

---

#### ETAPE 3 : Creation du VisiteBean (Couche Controleur)

```java
// beans/VisiteBean.java
@Named("visiteBean")
@SessionScoped
public class VisiteBean implements Serializable {
    
    @Inject
    private VisiteEntrepriseBean visiteService;
    
    @Inject
    private SessionBean sessionBean;
    
    private int lieuId;
    private String commentaire;
    private Integer note;
    private List<Visite> mesVisites;
    
    // Enregistrer une visite
    public String enregistrerVisite() {
        Utilisateur user = sessionBean.getUtilisateurConnecte();
        
        if (user == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Erreur", "Vous devez etre connecte."));
            return null;
        }
        
        visiteService.enregistrerVisite(user.getId(), lieuId, commentaire, note);
        return "mesVisites?faces-redirect=true";
    }
    
    // Charger les visites de l utilisateur
    public List<Visite> getMesVisites() {
        if (mesVisites == null) {
            Utilisateur user = sessionBean.getUtilisateurConnecte();
            if (user != null) {
                mesVisites = visiteService.listerVisitesParUtilisateur(user.getId());
            }
        }
        return mesVisites;
    }
}
```

**Points cles** :
- @Named : Rend le bean accessible dans les pages XHTML
- @SessionScoped : Conserve l etat pendant la session
- @Inject : Injection des dependances (CDI)

---

#### ETAPE 4 : Creation des pages XHTML (Couche Vue)

**Page enregistrerVisite.xhtml** :
```xml
<h:form>
    <div class="mb-3">
        <label>Selectionner un lieu</label>
        <h:selectOneMenu id="lieu" value="#{visiteBean.lieuId}" styleClass="form-select">
            <f:selectItem itemLabel="-- Choisir un lieu --" itemValue="0"/>
            <f:selectItems value="#{lieuBean.lieux}" var="l" 
                           itemLabel="#{l.nom}" itemValue="#{l.id}"/>
        </h:selectOneMenu>
    </div>
    
    <div class="mb-3">
        <label>Commentaire</label>
        <h:inputTextarea value="#{visiteBean.commentaire}" styleClass="form-control"/>
    </div>
    
    <div class="mb-3">
        <label>Note</label>
        <h:selectOneMenu value="#{visiteBean.note}" styleClass="form-select">
            <f:selectItem itemLabel="-- Pas de note --" itemValue="#{null}"/>
            <f:selectItem itemLabel="1 - Decevant" itemValue="1"/>
            <f:selectItem itemLabel="2 - Moyen" itemValue="2"/>
            <f:selectItem itemLabel="3 - Bien" itemValue="3"/>
            <f:selectItem itemLabel="4 - Tres bien" itemValue="4"/>
            <f:selectItem itemLabel="5 - Excellent" itemValue="5"/>
        </h:selectOneMenu>
    </div>
    
    <h:commandButton value="Enregistrer" action="#{visiteBean.enregistrerVisite()}" 
                     styleClass="btn btn-primary"/>
</h:form>
```

**Page mesVisites.xhtml** :
```xml
<h:dataTable value="#{visiteBean.mesVisites}" var="visite" styleClass="table">
    <h:column>
        <f:facet name="header">Lieu</f:facet>
        #{visite.lieu.nom}
    </h:column>
    <h:column>
        <f:facet name="header">Date</f:facet>
        <h:outputText value="#{visite.dateVisite}">
            <f:convertDateTime pattern="dd/MM/yyyy HH:mm"/>
        </h:outputText>
    </h:column>
    <h:column>
        <f:facet name="header">Note</f:facet>
        #{visite.note}/5
    </h:column>
    <h:column>
        <f:facet name="header">Actions</f:facet>
        <h:commandButton value="Supprimer" 
                         action="#{visiteBean.supprimerVisite(visite.id)}"
                         styleClass="btn btn-danger btn-sm"/>
    </h:column>
</h:dataTable>
```

---

#### ETAPE 5 : Creation de l API Meteo (Couche REST)

```java
// resources/JakartaWeatherResource.java
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class JakartaWeatherResource {
    
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
    
    @GET
    public Response getWeather(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude) {
        
        // Validation
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Coordonnees invalides\"}")
                    .build();
        }
        
        // Appel API externe
        String urlString = String.format(
            "%s?latitude=%.4f&longitude=%.4f&current_weather=true",
            WEATHER_API_URL, latitude, longitude);
        
        String weatherData = callWeatherAPI(urlString);
        return Response.ok(weatherData).build();
    }
}
```

---

## Resume des fichiers crees

| Fichier | Chemin | Role |
|---------|--------|------|
| Visite.java | entities/ | Entite JPA avec relations @ManyToOne |
| VisiteEntrepriseBean.java | business/ | Logique metier (CRUD) |
| VisiteBean.java | beans/ | Managed Bean JSF (controleur) |
| JakartaWeatherResource.java | resources/ | API REST meteo |
| enregistrerVisite.xhtml | webapp/ | Formulaire d enregistrement |
| mesVisites.xhtml | webapp/ | Historique des visites |

---

## Verification de la compilation

```
BUILD SUCCESS
[INFO] Compiling 14 source files with javac [debug release 21]
[INFO] Total time: 1.868 s
```

---

**Date** : 14 janvier 2026  
**Java** : 21 (LTS)  
**Jakarta EE** : 10.0.0
