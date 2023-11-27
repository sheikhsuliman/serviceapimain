ALTER TABLE location
    DROP CONSTRAINT location_surface_unit_fkey,
    DROP CONSTRAINT location_volume_unit_fkey,

    ADD CONSTRAINT location_surface_unit_fkey FOREIGN KEY (surface_unit) REFERENCES unit(id),
    ADD CONSTRAINT location_volume_unit_fkey FOREIGN KEY (volume_unit) REFERENCES unit(id);

UPDATE
    location

    SET
        surface_unit = CASE WHEN surface_unit is null THEN null ELSE ( SELECT id FROM unit WHERE symbol = (SELECT name FROM surface_unit WHERE id = location.surface_unit)) END,
        volume_unit =  CASE WHEN volume_unit is null THEN null ELSE ( SELECT id FROM unit WHERE symbol = (SELECT name FROM volume_units WHERE id = location.volume_unit)) END
    WHERE
        surface_unit IS NOT NULL OR volume_unit IS NOT NULL;
