-- migration 3: migrate Figure to use UUID

-- prepare figure table: add column id_new
ALTER TABLE figure ADD COLUMN id_new uuid DEFAULT gen_random_uuid();

-- fixup rabbit table that is referencing figure
ALTER TABLE rabbit ADD COLUMN id_new uuid;
UPDATE rabbit SET id_new = (SELECT figure.id_new FROM figure WHERE figure.id = rabbit.id);
ALTER TABLE rabbit DROP COLUMN id;
ALTER TABLE rabbit RENAME COLUMN id_new to id;

-- fixup fox table that is referencing figure
ALTER TABLE fox ADD COLUMN id_new uuid;
UPDATE fox SET id_new = (SELECT figure.id_new FROM figure WHERE figure.id = fox.id);
ALTER TABLE fox DROP COLUMN id;
ALTER TABLE fox RENAME COLUMN id_new to id;

-- finish figure table: replace id with id_new
ALTER TABLE figure DROP CONSTRAINT figure_pkey;
ALTER TABLE figure DROP COLUMN id;
ALTER TABLE figure RENAME COLUMN id_new TO id;
ALTER TABLE figure ADD CONSTRAINT figure_pkey PRIMARY KEY (id);

-- finish foreign keys
ALTER TABLE rabbit ADD FOREIGN KEY (id) REFERENCES figure (id);
ALTER TABLE fox ADD FOREIGN KEY (id) REFERENCES figure (id);
