-- V2__seed_test_data.sql

DO $$
DECLARE
    v_owner_id UUID;
    v_restaurant_id UUID;
BEGIN
    -- 1. Create the owner user if it doesn't exist
    -- (The password hash below is the bcrypt hash for 'password123')
    INSERT INTO users (id, email, password_hash, full_name, status, role)
    SELECT gen_random_uuid(), 'owner@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test Owner', 'ACTIVE', 'RESTAURANT_OWNER'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'owner@test.com')
    RETURNING id INTO v_owner_id;

    -- If it already existed, fetch its ID
    IF v_owner_id IS NULL THEN
        SELECT id INTO v_owner_id FROM users WHERE email = 'owner@test.com' LIMIT 1;
    END IF;

    -- 2. Create the customer user if it doesn't exist
    INSERT INTO users (id, email, password_hash, full_name, status, role)
    SELECT gen_random_uuid(), 'customer@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test Customer', 'ACTIVE', 'CUSTOMER'
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer@test.com');

    -- 3. Insert a test restaurant
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

    -- 4. Insert test menu items
    INSERT INTO menu_items (id, restaurant_id, name, description, price_in_cents, is_available)
    VALUES 
        (gen_random_uuid(), v_restaurant_id, 'Margherita Pizza', 'Classic cheese and fresh basil', 899, true),
        (gen_random_uuid(), v_restaurant_id, 'Pepperoni Pizza', 'Spicy pepperoni with mozzarella', 1099, true);

    RAISE NOTICE 'Test users, restaurant, and menu items seeded successfully!';
END $$;