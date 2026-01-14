package com.jakarta2.udbl.jakartamission2.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * JakartaWeatherResource - API REST pour récupérer les informations météo
 * Utilise @GET et @QueryParam pour récupérer la météo d'un lieu spécifique
 * 
 * Cette ressource REST expose un endpoint pour interroger les données météo
 * en fonction des coordonnées géographiques (latitude/longitude) d'un lieu.
 * 
 * @author user
 */
@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JakartaWeatherResource {

    // URL de l'API météo Open-Meteo (gratuite et sans clé API)
    // Utilisation de HTTP pour éviter les problèmes de certificat SSL en développement
    private static final String WEATHER_API_URL = "http://api.open-meteo.com/v1/forecast";

    /**
     * Récupérer la météo d'un lieu via ses coordonnées géographiques
     * 
     * @GET : Indique que cette méthode répond aux requêtes HTTP GET
     * @QueryParam : Permet de récupérer les paramètres de l'URL (latitude, longitude)
     * 
     * Exemple d'utilisation :
     * GET /api/weather?latitude=-6.2088&longitude=106.8456
     * 
     * @param latitude Latitude du lieu
     * @param longitude Longitude du lieu
     * @return Response contenant les données météo en JSON
     */
    @GET
    public Response getWeather(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude) {
        
        try {
            // Validation des paramètres
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Coordonnées invalides\"}")
                        .build();
            }

            // Construction de l'URL avec les paramètres (Locale.US pour avoir des points décimaux)
            String urlString = String.format(Locale.US,
                "%s?latitude=%.4f&longitude=%.4f&current_weather=true&hourly=temperature_2m,precipitation,windspeed_10m&timezone=auto",
                WEATHER_API_URL, latitude, longitude
            );

            // Appel à l'API météo externe
            String weatherData = callWeatherAPI(urlString);

            // Retour de la réponse
            return Response.ok(weatherData).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur lors de la récupération de la météo: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Récupérer la météo d'un lieu via son nom
     * 
     * @param lieuNom Nom du lieu
     * @return Response contenant les données météo en JSON
     */
    @GET
    @Path("/by-name")
    public Response getWeatherByName(@QueryParam("nom") String lieuNom) {
        
        try {
            if (lieuNom == null || lieuNom.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Nom du lieu requis\"}")
                        .build();
            }

            // Note: Pour une vraie application, il faudrait utiliser une API de géocodage
            // pour convertir le nom du lieu en coordonnées
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity("{\"error\": \"Fonctionnalité non implémentée. Utilisez les coordonnées.\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Endpoint de test pour vérifier que l'API fonctionne
     */
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public Response testEndpoint() {
        return Response.ok("Weather API is working!").build();
    }

    /**
     * Méthode privée pour effectuer l'appel HTTP à l'API météo externe
     */
    private String callWeatherAPI(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String output;

        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        conn.disconnect();
        return response.toString();
    }

    /**
     * Récupérer des informations météo détaillées pour les 7 prochains jours
     */
    @GET
    @Path("/forecast")
    public Response getWeatherForecast(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude) {
        
        try {
            String urlString = String.format(Locale.US,
                "%s?latitude=%.4f&longitude=%.4f&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,windspeed_10m_max&timezone=auto",
                WEATHER_API_URL, latitude, longitude
            );

            String weatherData = callWeatherAPI(urlString);
            return Response.ok(weatherData).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
