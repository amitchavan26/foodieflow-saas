-- V2__seed_test_data.sql

-- 1. Get the owner's ID (we know it's owner@test.com from the seeder)
DO $$
DECLARE
    v_owner_id UUID;
    v_restaurant_id UUID;
BEGIN
    SELECT id INTO v_owner_id FROM users WHERE email = 'owner@test.com' LIMIT 1;

    -- 2. Insert a test restaurant
    INSERT INTO restaurants (
        id, owner_user_id, name, slug, description, phone, address_line, 
        latitude, longitude, status, prep_time_minutes, delivery_radius_meters
    ) VALUES (
        gen_random_uuid(), 
        v_owner_id, 
        'Pizza Paradise', 
        'pizza-paradise', 
        'The best pizza in town!', 
        '555-0199', 
        '123 Main St, Foodie City',
        40.712800, 
        -74.006000, 
        'ACTIVE', 
        30, 
        5000
    ) RETURNING id INTO v_restaurant_id;

    -- 3. Insert test menu items
    INSERT INTO menu_items (id, restaurant_id, name, description, price_in_cents, is_available)
    VALUES 
        (gen_random_uuid(), v_restaurant_id, 'Margherita Pizza', 'Classic cheese and fresh basil', 899, true),
        (gen_random_uuid(), v_restaurant_id, 'Pepperoni Pizza', 'Spicy pepperoni with mozzarella', 1099, true);

    RAISE NOTICE 'Test restaurant and menu items seeded successfully!';
END $$;