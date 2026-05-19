-- =====================================================
-- V1 DATA.SQL
-- Stammdaten für dein Einkaufssystem
-- Reihenfolge wichtig wegen Foreign Keys
-- =====================================================
CREATE SEQUENCE IF NOT EXISTS order_number_seq
START WITH 1000
INCREMENT BY 1;

-- =====================================================
-- PAYMENT TERMS
-- =====================================================

INSERT INTO payment_term (id, definition, description) VALUES
                                                           (1, '7 Tage netto', 'Zahlbar innerhalb von 7 Tagen ohne Abzug'),
                                                           (2, '14 Tage netto', 'Zahlbar innerhalb von 14 Tagen ohne Abzug'),
                                                           (3, '30 Tage netto', 'Standard Zahlungsziel 30 Tage'),
                                                           (4, '30 Tage 2% Skonto', '2 Prozent Skonto innerhalb 10 Tagen'),
                                                           (5, 'Vorkasse', 'Zahlung vor Lieferung');



-- =====================================================
-- CATEGORIES
-- =====================================================

INSERT INTO category (id, name, description) VALUES
                                                 (1, 'Getränke', 'Wasser, Saft, Softdrinks'),
                                                 (2, 'Snacks', 'Chips, Riegel, Süßwaren'),
                                                 (3, 'Kosmetik', 'Pflegeprodukte und Beauty'),
                                                 (4, 'Haushalt', 'Reinigung und Haushalt'),
                                                 (5, 'Bürobedarf', 'Büroartikel'),
                                                 (6, 'Elektronik', 'Kleinelektronik'),
                                                 (7, 'Tierbedarf', 'Produkte für Haustiere'),
                                                 (8, 'Lebensmittel', 'Allgemeine Lebensmittel'),
                                                 (9, 'Tiefkühl', 'Tiefkühlprodukte'),
                                                 (10, 'Bio', 'Bio-Lebensmittel'),
                                                 (11, 'Drogerie', 'Drogerieartikel'),
                                                 (12, 'Garten', 'Gartenbedarf'),
                                                 (13, 'Werkzeug', 'Werkzeuge und Zubehör'),
                                                 (14, 'Spielwaren', 'Spielzeug und Games'),
                                                 (15, 'Bekleidung', 'Textilien'),
                                                 (16, 'Auto', 'Auto-Zubehör'),
                                                 (17, 'Sport', 'Sportartikel'),
                                                 (18, 'Wein', 'Weine und Spirituosen'),
                                                 (19, 'Backwaren', 'Brot und Backwaren'),
                                                 (20, 'Saisonal', 'Saisonale Produkte');



-- =====================================================
-- CONTACT PERSONS
-- =====================================================

INSERT INTO contact_person (id, first_name, last_name, role, phone, email) VALUES
                                                                               (1, 'Max', 'Müller', 'Vertrieb', '+49 511 10001', 'max.mueller@supplier.de'),
                                                                               (2, 'Anna', 'Schmidt', 'Key Account', '+49 511 10002', 'anna.schmidt@supplier.de'),
                                                                               (3, 'Peter', 'Weber', 'Sales Manager', '+49 511 10003', 'peter.weber@supplier.de'),
                                                                               (4, 'Julia', 'Fischer', 'Innendienst', '+49 511 10004', 'julia.fischer@supplier.de'),
                                                                               (5, 'Lukas', 'Klein', 'Vertrieb', '+49 511 10005', 'lukas.klein@supplier.de'),
                                                                               (6, 'Laura', 'Wolf', 'Sales', '+49 511 10006', 'laura.wolf@supplier.de'),
                                                                               (7, 'Tim', 'Becker', 'Disposition', '+49 511 10007', 'tim.becker@supplier.de'),
                                                                               (8, 'Nina', 'Hoffmann', 'Key Account', '+49 511 10008', 'nina.hoffmann@supplier.de'),
                                                                               (9, 'Jan', 'Richter', 'Sales', '+49 511 10009', 'jan.richter@supplier.de'),
                                                                               (10, 'Lisa', 'Krüger', 'Vertrieb', '+49 511 10010', 'lisa.krueger@supplier.de');



-- =====================================================
-- SUPPLIERS
-- =====================================================

INSERT INTO supplier (
    id, name, street, house_number, zip, city, country,
    email, phone, payment_term_id, is_active
) VALUES
      (1, 'Fresh Supply GmbH', 'Lister Str.', '10', '30161', 'Hannover', 'Deutschland', 'info@fresh.de', '0511-1111', 3, true),
      (2, 'Nordhandel AG', 'Marktweg', '22', '20095', 'Hamburg', 'Deutschland', 'kontakt@nordhandel.de', '040-2222', 4, true),
      (3, 'Beauty Trade GmbH', 'Rosenweg', '8', '50667', 'Köln', 'Deutschland', 'sales@beauty.de', '0221-3333', 2, true),
      (4, 'Office Star GmbH', 'Büroallee', '4', '44135', 'Dortmund', 'Deutschland', 'office@star.de', '0231-4444', 3, true),
      (5, 'Clean Products KG', 'Sauberweg', '12', '60311', 'Frankfurt', 'Deutschland', 'mail@clean.de', '069-5555', 1, true),
      (6, 'Snack Point GmbH', 'Knabberstr.', '9', '28195', 'Bremen', 'Deutschland', 'hello@snack.de', '0421-6666', 3, true),
      (7, 'Electro Base GmbH', 'Tech Park', '7', '70173', 'Stuttgart', 'Deutschland', 'info@electro.de', '0711-7777', 5, true),
      (8, 'Pet World GmbH', 'Tierweg', '15', '01067', 'Dresden', 'Deutschland', 'info@petworld.de', '0351-8888', 3, true),
      (9, 'Food Import GmbH', 'Hafenstr.', '31', '20457', 'Hamburg', 'Deutschland', 'food@import.de', '040-9999', 4, true),
      (10, 'Regional Markt GmbH', 'Dorfstr.', '5', '30159', 'Hannover', 'Deutschland', 'regional@markt.de', '0511-1010', 2, true),
      (11, 'Globus Trade GmbH', 'Zollstr.', '1', '10115', 'Berlin', 'Deutschland', 'info@globus.de', '030-1011', 3, true),
      (12, 'BavariaSupply KG', 'Bergweg', '12', '80331', 'München', 'Deutschland', 'kontakt@bavaria.de', '089-1212', 3, true),
      (13, 'NorthPort Handel', 'Kaistr.', '3', '24103', 'Kiel', 'Deutschland', 'info@northport.de', '0431-1313', 4, true),
      (14, 'RuhrLogistik GmbH', 'Industrieweg', '8', '45127', 'Essen', 'Deutschland', 'info@ruhrlog.de', '0201-1414', 2, true),
      (15, 'Saar Versand', 'Talstr.', '5', '66111', 'Saarbrücken', 'Deutschland', 'info@saar.de', '0681-1515', 1, true),
      (16, 'Alpenwaren GmbH', 'Bergstr.', '2', '83022', 'Rosenheim', 'Deutschland', 'info@alpen.de', '08031-1616', 3, true),
      (17, 'Hansa Trade KG', 'Speicherstr.', '7', '23552', 'Lübeck', 'Deutschland', 'mail@hansa.de', '0451-1717', 2, true),
      (18, 'OstSupply UG', 'Plattenweg', '4', '04109', 'Leipzig', 'Deutschland', 'info@ost.de', '0341-1818', 3, true),
      (19, 'WestExpress GmbH', 'Rheinstr.', '11', '40213', 'Düsseldorf', 'Deutschland', 'info@west.de', '0211-1919', 4, true),
      (20, 'CityFresh GmbH', 'Hauptstr.', '20', '90402', 'Nürnberg', 'Deutschland', 'info@cityfresh.de', '0911-2020', 3, true);



-- =====================================================
-- SUPPLIER <-> CONTACT PERSON
-- =====================================================

INSERT INTO connector_supplier_cp (supplier_id, cp_id) VALUES
                                                           (1,1),(1,2),
                                                           (2,3),
                                                           (3,4),
                                                           (4,5),
                                                           (5,6),
                                                           (6,7),
                                                           (7,8),
                                                           (8,9),
                                                           (9,10),
                                                           (10,1);



-- =====================================================
-- ARTICLES
-- =====================================================

INSERT INTO article (
    article_number,
    name,
    purchase_price,
    tax_rate_percent,
    selling_price,
    manufacturer,
    stock_level,
    description,
    is_available,
    has_deposit,
    product_image,
    date_created,
    expiration_date,
    width_cm,
    height_cm,
    depth_cm,
    main_supplier_id
) VALUES

      ('40000001','Mineralwasser 1L',0.25,19,0.79,'Aqua GmbH',150,'Stilles Wasser',true,true,'img1.jpg',NOW(),'2027-12-31',8,30,8,1),

      ('40000002','Cola 1L',0.45,19,1.49,'Soft Drinks AG',120,'Klassische Cola',true,true,'img2.jpg',NOW(),'2027-12-31',8,30,8,2),

      ('40000003','Kartoffelchips',0.60,7,1.99,'Snack Food',80,'Paprika Chips',true,false,'img3.jpg',NOW(),'2027-06-30',18,25,6,6),

      ('40000004','Schokoriegel',0.30,7,0.99,'Sweet Factory',220,'Milchschokolade',true,false,'img4.jpg',NOW(),'2027-08-31',12,4,2,6),

      ('40000005','Shampoo Classic',1.20,19,3.99,'Beauty Corp',60,'Pflegeshampoo',true,false,'img5.jpg',NOW(),'2028-01-31',7,24,7,3),

      ('40000006','Duschgel Fresh',0.95,19,2.99,'Beauty Corp',75,'Frischer Duft',true,false,'img6.jpg',NOW(),'2028-01-31',7,22,5,3),

      ('40000007','Küchenreiniger',1.10,19,3.49,'Clean Co',90,'Reinigungsspray',true,false,'img7.jpg',NOW(),'2028-06-30',9,28,6,5),

      ('40000008','Kugelschreiber Blau',0.15,19,0.79,'Office Star',500,'Schreibfarbe Blau',true,false,'img8.jpg',NOW(),NULL,1,14,1,4),

      ('40000009','USB Kabel 1m',1.80,19,5.99,'Electro Base',110,'USB-A auf USB-C',true,false,'img9.jpg',NOW(),NULL,3,15,2,7),

      ('40000010','Katzenfutter 500g',0.75,7,2.49,'Pet Food',140,'Geflügel Menü',true,false,'img10.jpg',NOW(),'2027-10-31',10,14,4,8),

      ('40000011','Pasta 500g',0.40,7,1.29,'Food Import',160,'Spaghetti',true,false,'img11.jpg',NOW(),'2028-02-28',5,25,3,9),

      ('40000012','Hafermilch 1L',0.85,7,2.29,'Regional Markt',95,'Pflanzliche Milchalternative',true,true,'img12.jpg',NOW(),'2027-09-30',7,24,7,10);



-- =====================================================
-- ARTICLE <-> CATEGORY
-- =====================================================

INSERT INTO connector_article_category (article_id, category_id) VALUES
                                                                     (1,1),
                                                                     (2,1),
                                                                     (3,2),
                                                                     (4,2),
                                                                     (5,3),
                                                                     (6,3),
                                                                     (7,4),
                                                                     (8,5),
                                                                     (9,6),
                                                                     (10,7),
                                                                     (11,8),
                                                                     (12,8);



-- =====================================================
-- ARTICLE <-> SUPPLIER
-- =====================================================

INSERT INTO connector_article_supplier (article_id, supplier_id) VALUES
                                                                     (1,1),
                                                                     (2,2),
                                                                     (3,6),
                                                                     (4,6),
                                                                     (5,3),
                                                                     (6,3),
                                                                     (7,5),
                                                                     (8,4),
                                                                     (9,7),
                                                                     (10,8),
                                                                     (11,9),
                                                                     (12,10),

                                                                     (3,2),
                                                                     (8,1),
                                                                     (9,2),
                                                                     (11,10);



-- =====================================================
-- OPTIONAL: SEQUENCES RESETTEN (PostgreSQL)
-- Nur falls IDs manuell gesetzt werden
-- =====================================================

SELECT setval('payment_term_id_seq', (SELECT MAX(id) FROM payment_term));
SELECT setval('category_id_seq', (SELECT MAX(id) FROM category));
SELECT setval('contact_person_id_seq', (SELECT MAX(id) FROM contact_person));
SELECT setval('supplier_id_seq', (SELECT MAX(id) FROM supplier));
SELECT setval('article_id_seq', (SELECT MAX(id) FROM article));