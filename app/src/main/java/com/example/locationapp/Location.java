package com.example.locationapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class Location extends AppCompatActivity implements LocationListener {

    private static final String[] A = {"n/a", "fine", "coarse"};
    private static final String[] P = {"n/a", "low", "medium", "high"};
    private static final String[] S = {"out of service", "temporarily unavailable", "available"};

    private static final int PERMISSION_REQUEST_CODE = 1001;

    private LocationManager mgr;
    private TextView output;
    private String best;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = findViewById(R.id.output);
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Display student name at the top
        log("===========================================");
        log("LOCATION TEST APP");
        log("Student: Kevin Koros");
        log("Course: MIS 6120 - Mobile Computing");
        log("Lab 5: Location-Based Services");
        log("===========================================\n");

        // Check and request permissions
        if (checkLocationPermissions()) {
            initializeLocationServices();
        } else {
            requestLocationPermissions();
        }
    }

    /**
     * Check if location permissions are granted
     */
    private boolean checkLocationPermissions() {
        boolean fineLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return fineLocation && coarseLocation;
    }

    /**
     * Request location permissions at runtime
     */
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                PERMISSION_REQUEST_CODE);
    }

    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                log("\n✓ Location permissions granted!\n");
                initializeLocationServices();
            } else {
                log("\n✗ Location permissions denied!");
                log("Please grant location permissions to use this app.\n");
                Toast.makeText(this,
                        "Location permissions are required for this app",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Initialize location services after permissions are granted
     */
    private void initializeLocationServices() {
        log("Location providers:");
        dumpProviders();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        best = mgr.getBestProvider(criteria, true);
        log("\n✓ Best provider is: " + best);

        log("\nLocations (starting with last known):");

        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = mgr.getLastKnownLocation(best);
                dumpLocation(location);
            }
        } catch (SecurityException e) {
            log("\nError getting location: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkLocationPermissions() && best != null) {
            try {
                // Request location updates every 15 seconds or 1 meter movement
                mgr.requestLocationUpdates(best, 15000, 1, this);
                log("\n✓ Started receiving location updates...\n");
            } catch (SecurityException e) {
                log("\nError starting location updates: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (checkLocationPermissions()) {
            try {
                mgr.removeUpdates(this);
                log("\n✓ Stopped location updates (app paused)\n");
            } catch (SecurityException e) {
                log("\nError stopping location updates: " + e.getMessage());
            }
        }
    }

    // LocationListener interface methods

    @Override
    public void onLocationChanged(@NonNull Location location) {
        log("\n--- NEW LOCATION UPDATE ---");
        dumpLocation(location);
        log("---------------------------\n");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        log("\n⚠ Provider disabled: " + provider);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        log("\n✓ Provider enabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        log("\n⚠ Provider status changed: " + provider +
                ", status=" + S[status] +
                ", extras=" + extras);
    }

    // Helper methods

    private void log(String string) {
        output.append(string + "\n");
    }

    private void dumpProviders() {
        List<String> providers = mgr.getAllProviders();
        for (String provider : providers) {
            dumpProvider(provider);
        }
    }

    private void dumpProvider(String provider) {
        LocationProvider info = mgr.getProvider(provider);
        if (info == null) {
            log("  Provider '" + provider + "' is null");
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("  LocationProvider[\n")
                .append("    name=").append(info.getName()).append("\n")
                .append("    enabled=").append(mgr.isProviderEnabled(provider)).append("\n")
                .append("    getAccuracy=").append(A[info.getAccuracy()]).append("\n")
                .append("    getPowerRequirement=").append(P[info.getPowerRequirement()]).append("\n")
                .append("    hasMonetaryCost=").append(info.hasMonetaryCost()).append("\n")
                .append("    requiresCell=").append(info.requiresCell()).append("\n")
                .append("    requiresNetwork=").append(info.requiresNetwork()).append("\n")
                .append("    requiresSatellite=").append(info.requiresSatellite()).append("\n")
                .append("    supportsAltitude=").append(info.supportsAltitude()).append("\n")
                .append("    supportsBearing=").append(info.supportsBearing()).append("\n")
                .append("    supportsSpeed=").append(info.supportsSpeed()).append("\n")
                .append("  ]");
        log(builder.toString());
    }

    private void dumpLocation(Location location) {
        if (location == null) {
            log("  Location[unknown - no location data available]");
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("  Location Details:\n")
                    .append("    Provider: ").append(location.getProvider()).append("\n")
                    .append("    Latitude: ").append(location.getLatitude()).append("°\n")
                    .append("    Longitude: ").append(location.getLongitude()).append("°\n")
                    .append("    Accuracy: ").append(location.hasAccuracy() ?
                            location.getAccuracy() + "m" : "n/a").append("\n")
                    .append("    Altitude: ").append(location.hasAltitude() ?
                            location.getAltitude() + "m" : "n/a").append("\n")
                    .append("    Speed: ").append(location.hasSpeed() ?
                            location.getSpeed() + "m/s" : "n/a").append("\n")
                    .append("    Bearing: ").append(location.hasBearing() ?
                            location.getBearing() + "°" : "n/a").append("\n")
                    .append("    Time: ").append(new java.util.Date(location.getTime()));
            log(builder.toString());
        }
    }
}