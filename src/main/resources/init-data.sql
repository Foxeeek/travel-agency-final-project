INSERT INTO USERS
(
 id,
 username,
 password,
 role,
 balance,
 phone_number,
 account_status
)
VALUES
    (
     'f3e02ce0-365d-4c03-90a1-98f00cf6d3d2',
     'admin1',
     '$2a$12$lGMyH/q2tkSYyDQ4M6Bp8ObuzHAkrPqDBi/.gp8JWmYjRjRCLRwkW',
     'ADMIN',
     500.00,
     null,
     true
    );

INSERT INTO VOUCHER
(
    id,
    title,
    description,
    price,
    tour_type,
    transfer_type,
    hotel_type,
    status,
    arrival_date,
    eviction_date,
    connected_user,
    is_hot
)
VALUES
    ('f3e02ce0-365d-4c03-90a1-98f00cf6d3d3',
     'Summer in Antalya',
     '7 days in a 5-star hotel near the beach',
     850.00,
     'ADVENTURE',
     'PLANE',
     'FIVE_STARS',
     'REGISTERED',
     '2027-06-10',
     '2027-06-17',
     NULL,
     true
    );
