CREATE DATABASE k9_abakus_unit;
CREATE USER k9_abakus_unit WITH PASSWORD 'k9_abakus_unit';
GRANT ALL ON DATABASE k9_abakus_unit TO k9_abakus_unit;
ALTER DATABASE k9_abakus_unit SET timezone TO 'Europe/Oslo';
ALTER DATABASE k9_abakus_unit OWNER TO k9_abakus_unit;

CREATE DATABASE k9abakus;
CREATE USER k9abakus WITH PASSWORD 'k9abakus';
GRANT ALL ON DATABASE k9abakus TO k9abakus;
ALTER DATABASE k9abakus SET timezone TO 'Europe/Oslo';
ALTER DATABASE k9abakus OWNER TO k9abakus;
