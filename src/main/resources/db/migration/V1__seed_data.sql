-- =============================================================
-- V1__seed_data.sql
-- ZipRun initial seed data
-- Flyway runs this once after Hibernate creates the schema.
-- Re-running the app drops and recreates tables (create-drop),
-- then Flyway re-applies this migration automatically.
-- =============================================================

-- ─── Agents ──────────────────────────────────────────────────

INSERT INTO agents (id, name, status, active_order_count, current_zone, max_capacity, created_at, updated_at)
VALUES
    ('AGT-001', 'Priya Sharma', 'BUSY',      2, 'Koramangala', 5, NOW(), NOW()),
    ('AGT-002', 'Rahul Verma',  'AVAILABLE', 0, 'Indiranagar', 5, NOW(), NOW()),
    ('AGT-003', 'Ananya Iyer',  'BUSY',      1, 'Whitefield',  5, NOW(), NOW()),
    ('AGT-004', 'Kiran Nair',   'AVAILABLE', 0, 'MG Road',     5, NOW(), NOW()),
    ('AGT-005', 'Deepak Mehta', 'BUSY',      3, 'Bellandur',   5, NOW(), NOW());

-- ─── Agent weight class support ──────────────────────────────

INSERT INTO agent_supported_weight_classes (agent_id, weight_class)
VALUES
    ('AGT-001', 'LIGHT'),
    ('AGT-001', 'HEAVY'),
    ('AGT-002', 'LIGHT'),
    ('AGT-003', 'LIGHT'),
    ('AGT-003', 'HEAVY'),
    ('AGT-004', 'LIGHT'),
    ('AGT-004', 'HEAVY'),
    ('AGT-005', 'LIGHT'),
    ('AGT-005', 'HEAVY');

-- ─── Orders ──────────────────────────────────────────────────

INSERT INTO orders (id, description, assigned_agent_id, status, pickup_zone, dropoff_zone, weight_class, priority, created_at, updated_at)
VALUES
    ('ORD-001', 'Electronics — Koramangala to Indiranagar', 'AGT-001', 'ASSIGNED', 'Koramangala',  'Indiranagar',    'LIGHT', 'NORMAL',  NOW(), NOW()),
    ('ORD-002', 'Groceries — HSR Layout to BTM',            'AGT-001', 'ASSIGNED', 'HSR Layout',   'BTM',            'HEAVY', 'NORMAL',  NOW(), NOW()),
    ('ORD-003', 'Pharma — Whitefield to Marathahalli',      'AGT-003', 'ASSIGNED', 'Whitefield',   'Marathahalli',   'LIGHT', 'HIGH',    NOW(), NOW()),
    ('ORD-004', 'Documents — MG Road to Jayanagar',         'AGT-005', 'ASSIGNED', 'MG Road',      'Jayanagar',      'LIGHT', 'NORMAL',  NOW(), NOW()),
    ('ORD-005', 'Food — Bellandur to Electronic City',      'AGT-005', 'ASSIGNED', 'Bellandur',    'Electronic City','LIGHT', 'HIGH',    NOW(), NOW()),
    ('ORD-006', 'Apparel — Malleshwaram to Rajajinagar',    'AGT-005', 'ASSIGNED', 'Malleshwaram', 'Rajajinagar',    'HEAVY', 'NORMAL',  NOW(), NOW()),
    ('ORD-007', 'Books — Banashankari to JP Nagar',         'AGT-003', 'ASSIGNED', 'Banashankari', 'JP Nagar',       'LIGHT', 'LOW',     NOW(), NOW()),
    ('ORD-008', 'Hardware — Peenya to Yeshwanthpur',        'AGT-001', 'ASSIGNED', 'Peenya',       'Yeshwanthpur',   'HEAVY', 'PREMIUM', NOW(), NOW());