-- Schemas pro Bounded Context (Loser Kopplung: eigene Datenhaltung pro Domaene)
CREATE SCHEMA IF NOT EXISTS artikel;
CREATE SCHEMA IF NOT EXISTS lager;
CREATE SCHEMA IF NOT EXISTS wareneingang;
CREATE SCHEMA IF NOT EXISTS kommission;
CREATE SCHEMA IF NOT EXISTS kontingent;
CREATE SCHEMA IF NOT EXISTS nachbestellung;
CREATE SCHEMA IF NOT EXISTS messaging;

-- Sequence fuer Wareneingangs-Nummernkreis (WE-YYYY-NNNNN)
CREATE SEQUENCE IF NOT EXISTS wareneingang.goods_receipt_seq START 1;
