package org.example.app;

import org.example.app.model.Asset;
import org.example.app.model.City;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Service {

    static {
        new City("Tel-Aviv");
        new City("Jerusalem");

    }

    private static final Collection<Asset> assets = List.of(
            new Asset("Chair", 0)
            , new Asset( "Table",  1)
    );

    public Collection<City> getCities() {
        return City.cities.values();
    }
    public Optional<City> getCityByName(String name){
        return City.cities.values().stream().filter((v->v.name().equals(name))).findFirst();
    }

    public Collection<Asset> getAssets() {
        return assets;
    }
}

