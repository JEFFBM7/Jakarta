    # Documentation - Partie Visite de l'Application Jakarta

## 📋 Vue d'ensemble de l'implémentation

Cette documentation explique la mise en place complète de la fonctionnalité de visite dans l'application Jakarta, incluant l'entité Visite, les beans métiers et l'API météo.

---

## 🎯 1. Rôle de l'entité Visite dans notre application

### **Définition et objectif**
L'entité **Visite** représente l'enregistrement d'une visite effectuée par un utilisateur à un lieu spécifique. Elle sert de table de liaison (table d'association) entre les entités `Utilisateur` et `Lieu`.

### **Rôle principal**
- **Traçabilité** : Permet de savoir qui a visité quel lieu et quand
- **Historique** : Conserve l'historique complet des visites de chaque utilisateur
- **Évaluation** : Permet aux utilisateurs de noter et commenter leurs visites
- **Statistiques** : Facilite le calcul du nombre de visites par lieu et les notes moyennes
- **Relation Many-to-Many** : Établit une relation plusieurs-à-plusieurs entre utilisateurs et lieux avec des informations supplémentaires (date, note, commentaire)

### **Structure de l'entité**
```java
@Entity
@Table(name = "visite")
public class Visite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @ManyToOne
    @JoinColumn(name = "lieu_id", nullable = false)
    private Lieu lieu;
    
    private LocalDateTime dateVisite;
    private String commentaire;
    private Integer note;
}
```

---

## 🔗 2. Pourquoi utilisons-nous @ManyToOne dans l'entité Visite ?

### **Explication du @ManyToOne**

L'annotation `@ManyToOne` indique qu'**une entité (Visite) peut être associée à une seule instance d'une autre entité (Utilisateur ou Lieu), mais cette autre entité peut être associée à plusieurs instances de la première entité**.

### **Relation avec Utilisateur**
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "utilisateur_id", nullable = false)
private Utilisateur utilisateur;
```

**Signification** : 
- **Plusieurs visites** → **Un seul utilisateur**
- Un utilisateur peut avoir plusieurs visites (relation One-to-Many du côté Utilisateur)
- Chaque visite appartient à un seul utilisateur

### **Relation avec Lieu**
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "lieu_id", nullable = false)
private Lieu lieu;
```

**Signification** :
- **Plusieurs visites** → **Un seul lieu**
- Un lieu peut recevoir plusieurs visites de différents utilisateurs
- Chaque visite concerne un seul lieu

### **Pourquoi pas @OneToMany ou @ManyToMany ?**

| Annotation | Utilisation | Pourquoi pas ici ? |
|-----------|-------------|-------------------|
| `@OneToMany` | Un utilisateur → Plusieurs visites | Se met du côté Utilisateur, pas Visite |
| `@ManyToMany` | Plusieurs utilisateurs ↔ Plusieurs lieux | Ne permet pas d'ajouter des attributs supplémentaires (date, note, commentaire) |
| `@ManyToOne` | ✅ Plusieurs visites → Un utilisateur/lieu | **Solution optimale** : permet d'enrichir la relation avec des données |

### **Avantages du @ManyToOne**
1. **Clés étrangères** : Crée automatiquement les colonnes `utilisateur_id` et `lieu_id` dans la table visite
2. **Intégrité référentielle** : Garantit qu'une visite ne peut exister sans utilisateur et lieu valides
3. **Chargement des données** : `FetchType.EAGER` charge immédiatement les données de l'utilisateur et du lieu
4. **Flexibilité** : Permet d'ajouter autant d'attributs que nécessaire à la relation (date, note, commentaire, etc.)

---

## 🌤️ 3. Rôle principal de l'API JakartaWeatherResource

### **Définition**
`JakartaWeatherResource` est une **API REST** (Representational State Transfer) qui expose des endpoints HTTP pour récupérer les informations météorologiques d'un lieu en fonction de ses coordonnées géographiques.

### **Rôles principaux**

#### **A. Exposition d'un service web RESTful**
```java
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JakartaWeatherResource {
    // ...
}
```
- Crée une ressource accessible via l'URL : `/api/weather`
- Produit et consomme du JSON pour l'échange de données

#### **B. Pont entre l'application et une API météo externe**
```java
private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
```
- Interroge l'API Open-Meteo (service externe gratuit)
- Récupère les données météo en temps réel
- Transforme et retourne les données à l'application

#### **C. Enrichissement des informations des lieux**
- Fournit des informations contextuelles sur la météo d'un lieu
- Aide les utilisateurs à planifier leurs visites
- Améliore l'expérience utilisateur avec des données en temps réel

#### **D. Gestion des erreurs et validation**
```java
if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
    return Response.status(Response.Status.BAD_REQUEST)
            .entity("{\"error\": \"Coordonnées invalides\"}")
            .build();
}
```
- Valide les paramètres d'entrée
- Gère les erreurs de connexion
- Retourne des messages d'erreur appropriés

---

## 🔧 4. Pourquoi utilisons-nous @GET et @QueryParam pour récupérer la météo d'un lieu ?

### **A. Pourquoi @GET ?**

```java
@GET
public Response getWeather(
    @QueryParam("latitude") double latitude,
    @QueryParam("longitude") double longitude) {
    // ...
}
```

#### **Raisons d'utilisation de @GET**

| Raison | Explication |
|--------|-------------|
| **Sémantique HTTP** | GET est conçu pour **récupérer** des données sans les modifier |
| **Sécurité** | Opération **idempotente** : plusieurs appels donnent le même résultat |
| **Cache** | Les réponses GET peuvent être mises en cache par le navigateur |
| **Simplicité** | Facile à tester directement dans un navigateur ou avec curl |
| **REST Standard** | Respecte les principes REST : GET = lecture de ressources |

#### **Alternatives et pourquoi elles ne conviennent pas**
- `@POST` : Pour **créer** des ressources (pas pour lire)
- `@PUT` : Pour **mettre à jour** des ressources complètes
- `@DELETE` : Pour **supprimer** des ressources
- `@PATCH` : Pour **modifier partiellement** des ressources

### **B. Pourquoi @QueryParam ?**

```java
@QueryParam("latitude") double latitude
@QueryParam("longitude") double longitude
```

#### **Raisons d'utilisation de @QueryParam**

1. **Paramètres optionnels ou multiples**
   - Facilite la transmission de plusieurs paramètres
   - Permet des valeurs par défaut

2. **Lisibilité de l'URL**
   ```
   GET /api/weather?latitude=-6.2088&longitude=106.8456
   ```
   - L'URL est explicite et facile à comprendre
   - Les paramètres sont visibles et modifiables

3. **Compatibilité avec les requêtes GET**
   - Les query params sont la méthode standard pour passer des données avec GET
   - Fonctionne avec tous les clients HTTP

4. **Flexibilité**
   - Permet d'ajouter facilement de nouveaux paramètres sans casser l'API
   - Exemple : `?latitude=X&longitude=Y&units=metric&lang=fr`

#### **Alternatives et comparaison**

| Méthode | Exemple | Cas d'usage |
|---------|---------|-------------|
| `@QueryParam` | `/weather?lat=5&lon=10` | ✅ Multiples paramètres optionnels |
| `@PathParam` | `/weather/5/10` | Identifiant de ressource unique |
| Body JSON | POST avec `{"lat":5,"lon":10}` | Données complexes ou sensibles |

### **C. Exemple d'utilisation complète**

```bash
# Requête HTTP GET avec query parameters
GET http://localhost:8080/jakartamission2/api/weather?latitude=-6.2088&longitude=106.8456

# Réponse JSON
{
  "current_weather": {
    "temperature": 28.5,
    "windspeed": 12.0,
    "weathercode": 3
  },
  "hourly": {
    "temperature_2m": [27.2, 27.8, 28.5, ...],
    "precipitation": [0, 0, 0.1, ...]
  }
}
```

---

## 🏗️ 5. Comment et à quel niveau avez-vous intégré l'API dans l'application ?

### **Architecture d'intégration**

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION JAKARTA                       │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Niveau 1: VUE (Présentation)                                │
│  ┌──────────────────────────────────────────────────┐       │
│  │  lieuDetails.xhtml / detailLieu.xhtml            │       │
│  │  - Affiche les informations du lieu              │       │
│  │  - Appel JavaScript/AJAX à l'API météo           │       │
│  └──────────────────────────────────────────────────┘       │
│                        ↓                                      │
│  Niveau 2: CONTRÔLEUR (Managed Beans)                        │
│  ┌──────────────────────────────────────────────────┐       │
│  │  VisiteBean.java                                  │       │
│  │  LieuBean.java                                    │       │
│  │  - Gestion de la logique d'affichage             │       │
│  │  - Peut appeler l'API météo pour enrichir        │       │
│  └──────────────────────────────────────────────────┘       │
│                        ↓                                      │
│  Niveau 3: COUCHE REST (Ressources)                          │
│  ┌──────────────────────────────────────────────────┐       │
│  │  JakartaWeatherResource.java  ← INTÉGRATION ICI  │       │
│  │  @Path("/weather")                                │       │
│  │  - Expose les endpoints REST                      │       │
│  │  - Appelle l'API externe                          │       │
│  └──────────────────────────────────────────────────┘       │
│                        ↓                                      │
│  Niveau 4: SERVICE MÉTIER (EJB)                              │
│  ┌──────────────────────────────────────────────────┐       │
│  │  VisiteEntrepriseBean.java                        │       │
│  │  LieuEntrepriseBean.java                          │       │
│  │  - Logique métier des visites                     │       │
│  │  - Peut intégrer les données météo                │       │
│  └──────────────────────────────────────────────────┘       │
│                        ↓                                      │
│  Niveau 5: PERSISTANCE (Entités JPA)                         │
│  ┌──────────────────────────────────────────────────┐       │
│  │  Visite.java, Lieu.java, Utilisateur.java        │       │
│  │  - Mapping objet-relationnel                      │       │
│  └──────────────────────────────────────────────────┘       │
│                        ↓                                      │
│  Niveau 6: BASE DE DONNÉES                                   │
│  ┌──────────────────────────────────────────────────┐       │
│  │  Tables: visite, lieu, utilisateur               │       │
│  └──────────────────────────────────────────────────┘       │
│                                                               │
└─────────────────────────────────────────────────────────────┘
                        ↓
              ┌─────────────────────┐
              │  API EXTERNE        │
              │  Open-Meteo         │
              │  (Météo en temps    │
              │   réel)             │
              └─────────────────────┘
```

### **Détails de l'intégration**

#### **Niveau 3 : Couche REST - Point d'intégration principal**

**Fichier** : `JakartaWeatherResource.java`
**Localisation** : `src/main/java/com/jakarta2/udbl/jakartamission2/resources/`

```java
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
public class JakartaWeatherResource {
    @GET
    public Response getWeather(
        @QueryParam("latitude") double latitude,
        @QueryParam("longitude") double longitude) {
        // Intégration ici
    }
}
```

**Pourquoi à ce niveau ?**
1. **Séparation des préoccupations** : La logique REST est isolée
2. **Réutilisabilité** : Peut être appelée depuis n'importe quelle partie de l'app
3. **Testabilité** : Facile à tester indépendamment
4. **Évolutivité** : Facile d'ajouter de nouveaux endpoints

#### **Configuration REST**

**Fichier** : `JakartaRestConfiguration.java`

```java
@ApplicationPath("/api")
public class JakartaRestConfiguration extends Application {
}
```

**Résultat** : L'API météo est accessible à :
```
http://localhost:8080/jakartamission2/api/weather?latitude=-6.2088&longitude=106.8456
```

### **Scénarios d'utilisation**

#### **Scénario 1 : Depuis une page XHTML (JavaScript)**
```html
<!-- lieuDetails.xhtml -->
<script>
function chargerMeteo(latitude, longitude) {
    fetch('/jakartamission2/api/weather?latitude=' + latitude + '&longitude=' + longitude)
        .then(response => response.json())
        .then(data => {
            document.getElementById('meteo').innerHTML = 
                'Température : ' + data.current_weather.temperature + '°C';
        });
}
</script>
```

#### **Scénario 2 : Depuis un Managed Bean**
```java
// Dans LieuBean.java
public String getMeteoLieu(Lieu lieu) {
    try {
        URL url = new URL("http://localhost:8080/jakartamission2/api/weather" +
                         "?latitude=" + lieu.getLatitude() + 
                         "&longitude=" + lieu.getLongitude());
        // Appel HTTP et traitement
    } catch (Exception e) {
        return "Météo indisponible";
    }
}
```

#### **Scénario 3 : Depuis une application externe**
```bash
curl "http://localhost:8080/jakartamission2/api/weather?latitude=-6.2088&longitude=106.8456"
```

---

## 📚 6. Étapes de mise en place de la partie visite (Éléments essentiels)

### **ÉTAPE 1 : Création de l'entité Visite (Persistance)**

**Fichier** : `Visite.java`
**Objectif** : Définir le modèle de données

```java
@Entity
@Table(name = "visite")
public class Visite implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
    
    @ManyToOne
    @JoinColumn(name = "lieu_id")
    private Lieu lieu;
    
    private LocalDateTime dateVisite;
    private String commentaire;
    private Integer note;
}
```

**Résultat** : 
- Table `visite` créée automatiquement par JPA
- Colonnes : `id`, `utilisateur_id`, `lieu_id`, `date_visite`, `commentaire`, `note`
- Clés étrangères vers `utilisateur` et `lieu`

---

### **ÉTAPE 2 : Création du bean métier (Logique métier)**

**Fichier** : `VisiteEntrepriseBean.java`
**Objectif** : Implémenter les opérations CRUD

```java
@Stateless
@LocalBean
public class VisiteEntrepriseBean {
    @PersistenceContext
    private EntityManager em;
    
    public void enregistrerVisite(Long userId, int lieuId, ...) { }
    public List<Visite> listerVisitesParUtilisateur(Long userId) { }
    public List<Visite> listerVisitesParLieu(int lieuId) { }
    public Long compterVisitesLieu(int lieuId) { }
    public Double obtenirNoteMoyenneLieu(int lieuId) { }
}
```

**Services fournis** :
- ✅ Enregistrement de visites
- ✅ Consultation de l'historique
- ✅ Statistiques (comptage, moyenne)
- ✅ Validation et vérification

---

### **ÉTAPE 3 : Création du Managed Bean (Contrôleur)**

**Fichier** : `VisiteBean.java`
**Objectif** : Interface entre la vue et la logique métier

```java
@Named(value = "visiteBean")
@SessionScoped
public class VisiteBean implements Serializable {
    @Inject
    private VisiteEntrepriseBean visiteService;
    
    private int lieuId;
    private String commentaire;
    private Integer note;
    
    public String enregistrerVisite() { }
    public void chargerMesVisites() { }
    public boolean aVisiteLieu(int idLieu) { }
}
```

**Responsabilités** :
- Gestion des formulaires
- Validation des données
- Messages utilisateur
- Navigation entre pages

---

### **ÉTAPE 4 : Création des vues XHTML (Interface utilisateur)**

**Fichiers** :
- `enregistrerVisite.xhtml` : Formulaire d'enregistrement
- `mesVisites.xhtml` : Liste des visites de l'utilisateur

```xml
<!-- enregistrerVisite.xhtml -->
<h:form>
    <h:selectOneMenu value="#{visiteBean.lieuId}">
        <f:selectItems value="#{lieuBean.lieux}"/>
    </h:selectOneMenu>
    
    <h:inputTextarea value="#{visiteBean.commentaire}"/>
    
    <h:selectOneMenu value="#{visiteBean.note}">
        <f:selectItem itemValue="5" itemLabel="⭐⭐⭐⭐⭐"/>
    </h:selectOneMenu>
    
    <h:commandButton value="Enregistrer" 
                     action="#{visiteBean.enregistrerVisite()}"/>
</h:form>
```

**Fonctionnalités** :
- Sélection du lieu
- Saisie de commentaire
- Attribution de note
- Validation et soumission

---

### **ÉTAPE 5 : Création de l'API météo (Service REST)**

**Fichier** : `JakartaWeatherResource.java`
**Objectif** : Fournir des données météo pour enrichir l'expérience

```java
@Path("/weather")
public class JakartaWeatherResource {
    @GET
    public Response getWeather(
        @QueryParam("latitude") double latitude,
        @QueryParam("longitude") double longitude) {
        // Appel API externe
        return Response.ok(weatherData).build();
    }
}
```

**Intégration** :
- Appel depuis JavaScript dans les pages de lieux
- Affichage de la météo actuelle
- Aide à la décision de visite

---

### **ÉTAPE 6 : Configuration de la persistance**

**Fichier** : `persistence.xml` (déjà existant)

```xml
<persistence-unit name="indonesiaPU" transaction-type="JTA">
    <jta-data-source>jdbc/indonesiadb</jta-data-source>
</persistence-unit>
```

**Action** : Aucune modification nécessaire, JPA détecte automatiquement la nouvelle entité Visite.

---

### **ÉTAPE 7 : Intégration dans l'application existante**

**Modifications à apporter** :

1. **Dans `home.xhtml`** : Ajouter un lien vers "Mes visites"
```xml
<h:link value="Mes visites" outcome="mesVisites"/>
```

2. **Dans `lieuDetails.xhtml`** : Ajouter un bouton "Enregistrer une visite"
```xml
<h:commandButton value="J'ai visité ce lieu" 
                 action="#{visiteBean.enregistrerVisiteRapide(lieu.id)}"/>
```

3. **Afficher la météo** : Ajouter un composant météo dans les détails du lieu
```html
<div id="meteo">
    <script>chargerMeteo(#{lieu.latitude}, #{lieu.longitude})</script>
</div>
```

---

## 🎓 Résumé des concepts clés

### **Relations JPA**
- `@ManyToOne` : Plusieurs visites → Un utilisateur/lieu
- Permet d'enrichir une relation avec des attributs supplémentaires
- Crée automatiquement les clés étrangères

### **Architecture REST**
- `@GET` : Récupération de données (lecture seule)
- `@QueryParam` : Passage de paramètres dans l'URL
- Séparation claire des responsabilités

### **API externe**
- Intégration au niveau de la couche REST
- Isolation de la logique d'appel externe
- Gestion des erreurs et validation

### **Pattern MVC**
- **Modèle** : Entités JPA (Visite, Lieu, Utilisateur)
- **Vue** : Pages XHTML
- **Contrôleur** : Managed Beans + EJB

---

## ✅ Checklist de vérification

- [x] Entité Visite créée avec @ManyToOne
- [x] VisiteEntrepriseBean implémenté
- [x] VisiteBean créé avec injection de dépendances
- [x] Pages XHTML pour enregistrement et consultation
- [x] API météo JakartaWeatherResource
- [x] Endpoints REST avec @GET et @QueryParam
- [x] Intégration dans l'architecture existante
- [x] Documentation complète

---

## 🚀 Tests de l'implémentation

### **1. Tester l'enregistrement d'une visite**
1. Se connecter à l'application
2. Aller sur "Enregistrer une visite"
3. Sélectionner un lieu
4. Ajouter un commentaire et une note
5. Valider le formulaire

### **2. Tester l'API météo**
```bash
curl "http://localhost:8080/jakartamission2/api/weather?latitude=-6.2088&longitude=106.8456"
```

### **3. Tester la consultation des visites**
1. Aller sur "Mes visites"
2. Vérifier l'affichage du tableau
3. Tester la suppression d'une visite

---

## 📌 Points d'amélioration futurs

1. **Géolocalisation** : Détecter automatiquement la position de l'utilisateur
2. **Photos** : Permettre d'ajouter des photos aux visites
3. **Partage social** : Partager les visites sur les réseaux sociaux
4. **Recommandations** : Suggérer des lieux basés sur l'historique
5. **Notifications** : Alertes météo avant une visite planifiée

---

**Auteur** : Système de documentation automatique  
**Date** : 14 janvier 2026  
**Version** : 1.0
