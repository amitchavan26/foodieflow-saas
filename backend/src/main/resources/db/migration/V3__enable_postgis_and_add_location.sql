-- V3__enable_postgis_and_add_location.sql

-- 1. Enable the PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Add a geography column to the restaurants table
ALTER TABLE restaurants ADD COLUMN location GEOGRAPHY(POINT, 4326);

-- 3. Populate the location column for existing restaurants using their lat/lng
UPDATE restaurants 
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE location IS NULL;