package com.medicare.app.services;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineInfoService {
    private static final String TAG = "MedicineInfoService";
    private static final String BASE_URL = "https://api.fda.gov/drug/label.json";
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    public interface MedicineInfoCallback {
        void onSuccess(MedicineInfo medicineInfo);
        void onError(String error);
    }

    public static class MedicineInfo {
        public String drugName;
        public String description;
        public String dosageAndAdministration;
        public String contraindications;
        public String adverseReactions;
        public String warnings;
        public String activeIngredient;
        public String manufacturer;

        public MedicineInfo() {
            this.drugName = "";
            this.description = "";
            this.dosageAndAdministration = "";
            this.contraindications = "";
            this.adverseReactions = "";
            this.warnings = "";
            this.activeIngredient = "";
            this.manufacturer = "";
        }

        public boolean isEmpty() {
            return drugName.isEmpty() && description.isEmpty() && 
                   dosageAndAdministration.isEmpty() && contraindications.isEmpty() &&
                   adverseReactions.isEmpty() && warnings.isEmpty();
        }
    }

    public void getMedicineInfo(String medicineName, MedicineInfoCallback callback) {
        executor.execute(() -> {
            try {
                String encodedName = URLEncoder.encode(medicineName, "UTF-8");
                String urlString = BASE_URL + "?search=openfda.brand_name:" + encodedName + 
                                 "&limit=1";
                
                Log.d(TAG, "Fetching medicine info for: " + medicineName);
                Log.d(TAG, "API URL: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    MedicineInfo info = parseResponse(response.toString(), medicineName);
                    callback.onSuccess(info);
                } else {
                    Log.e(TAG, "API request failed with code: " + responseCode);
                    // Try alternative search by generic name
                    tryGenericNameSearch(medicineName, callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching medicine info", e);
                callback.onError("Failed to fetch medicine information: " + e.getMessage());
            }
        });
    }

    private void tryGenericNameSearch(String medicineName, MedicineInfoCallback callback) {
        try {
            String encodedName = URLEncoder.encode(medicineName, "UTF-8");
            String urlString = BASE_URL + "?search=openfda.generic_name:" + encodedName + 
                             "&limit=1";
            
            Log.d(TAG, "Trying generic name search: " + urlString);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                MedicineInfo info = parseResponse(response.toString(), medicineName);
                callback.onSuccess(info);
            } else {
                callback.onError("Medicine information not found in FDA database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in generic name search", e);
            callback.onError("Failed to fetch medicine information: " + e.getMessage());
        }
    }

    private MedicineInfo parseResponse(String jsonResponse, String searchedName) {
        MedicineInfo info = new MedicineInfo();
        
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray results = root.getJSONArray("results");
            
            if (results.length() == 0) {
                info.drugName = searchedName;
                info.description = "No detailed information found in FDA database";
                return info;
            }

            JSONObject drug = results.getJSONObject(0);

            // Extract drug name
            if (drug.has("openfda")) {
                JSONObject openFda = drug.getJSONObject("openfda");
                if (openFda.has("brand_name")) {
                    JSONArray brandNames = openFda.getJSONArray("brand_name");
                    if (brandNames.length() > 0) {
                        info.drugName = brandNames.getString(0);
                    }
                }
                if (info.drugName.isEmpty() && openFda.has("generic_name")) {
                    JSONArray genericNames = openFda.getJSONArray("generic_name");
                    if (genericNames.length() > 0) {
                        info.drugName = genericNames.getString(0);
                    }
                }
                if (openFda.has("manufacturer_name")) {
                    JSONArray manufacturers = openFda.getJSONArray("manufacturer_name");
                    if (manufacturers.length() > 0) {
                        info.manufacturer = manufacturers.getString(0);
                    }
                }
            }

            if (info.drugName.isEmpty()) {
                info.drugName = searchedName;
            }

            // Extract active ingredient
            if (drug.has("active_ingredient")) {
                JSONArray activeIngredients = drug.getJSONArray("active_ingredient");
                if (activeIngredients.length() > 0) {
                    info.activeIngredient = activeIngredients.getString(0);
                }
            }

            // Extract description/purpose
            if (drug.has("purpose")) {
                JSONArray purposes = drug.getJSONArray("purpose");
                if (purposes.length() > 0) {
                    info.description = purposes.getString(0);
                }
            } else if (drug.has("indications_and_usage")) {
                JSONArray indications = drug.getJSONArray("indications_and_usage");
                if (indications.length() > 0) {
                    info.description = indications.getString(0);
                }
            }

            // Extract dosage and administration
            if (drug.has("dosage_and_administration")) {
                JSONArray dosage = drug.getJSONArray("dosage_and_administration");
                if (dosage.length() > 0) {
                    info.dosageAndAdministration = dosage.getString(0);
                }
            }

            // Extract contraindications
            if (drug.has("contraindications")) {
                JSONArray contraindications = drug.getJSONArray("contraindications");
                if (contraindications.length() > 0) {
                    info.contraindications = contraindications.getString(0);
                }
            }

            // Extract adverse reactions (side effects)
            if (drug.has("adverse_reactions")) {
                JSONArray adverseReactions = drug.getJSONArray("adverse_reactions");
                if (adverseReactions.length() > 0) {
                    info.adverseReactions = adverseReactions.getString(0);
                }
            }

            // Extract warnings
            if (drug.has("warnings")) {
                JSONArray warnings = drug.getJSONArray("warnings");
                if (warnings.length() > 0) {
                    info.warnings = warnings.getString(0);
                }
            }

            Log.d(TAG, "Successfully parsed medicine info for: " + info.drugName);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing medicine info response", e);
            info.drugName = searchedName;
            info.description = "Error parsing medicine information";
        }

        return info;
    }
}