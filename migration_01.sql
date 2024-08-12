-- migration 1: migrate Carrot to use UUID

-- create column id_new
ALTER TABLE carrot ADD COLUMN id_new uuid DEFAULT gen_random_uuid();

-- drop old primary key
ALTER TABLE carrot DROP CONSTRAINT carrot_pkey;

-- drop old id column
ALTER TABLE carrot DROP COLUMN id;

-- rename column id_new to id
ALTER TABLE carrot RENAME COLUMN id_new TO id;

-- create new primary key
ALTER TABLE carrot ADD CONSTRAINT carrot_pkey PRIMARY KEY (id);
