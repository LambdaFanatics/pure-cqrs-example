CREATE EXTENSION "uuid-ossp";

ALTER TABLE plants ALTER COLUMN id DROP DEFAULT,
                    ALTER COLUMN id TYPE uuid USING (uuid_generate_v4()),
                    ALTER COLUMN id SET DEFAULT uuid_generate_v4();
DROP SEQUENCE IF EXISTS plants_id_seq;