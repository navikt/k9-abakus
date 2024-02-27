CREATE DATABASE k9_abakus_unit;
CREATE USER k9_abakus_unit WITH PASSWORD 'k9_abakus_unit';
GRANT ALL ON DATABASE k9_abakus_unit TO k9_abakus_unit;
ALTER DATABASE k9_abakus_unit SET timezone TO 'Europe/Oslo';
ALTER DATABASE k9_abakus_unit OWNER TO k9_abakus_unit;

CREATE DATABASE k9-abakus;
CREATE USER k9-abakus WITH PASSWORD 'k9-abakus';
GRANT ALL ON DATABASE k9-abakus TO 'k9-abakus';
ALTER DATABASE k9-abakus SET timezone TO 'Europe/Oslo';
ALTER DATABASE k9-abakus OWNER TO 'k9-abakus';
