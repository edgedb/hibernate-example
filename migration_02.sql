-- migration 2: migrate Position to use UUID

-- prepare position table: add column id_new
ALTER TABLE position ADD COLUMN id_new uuid DEFAULT gen_random_uuid();

-- fixup carrot table that is referencing position
ALTER TABLE carrot ADD COLUMN position_id_new uuid;
UPDATE carrot SET position_id_new = (SELECT id_new FROM position WHERE position.id = carrot.position_id);
ALTER TABLE carrot DROP COLUMN position_id;
ALTER TABLE carrot RENAME COLUMN position_id_new to position_id;

-- fixup figure table that is referencing position
ALTER TABLE figure ADD COLUMN position_id_new uuid;
UPDATE figure SET position_id_new = (SELECT id_new FROM position WHERE position.id = figure.position_id);
ALTER TABLE figure DROP COLUMN position_id;
ALTER TABLE figure RENAME COLUMN position_id_new to position_id;

-- finish position table: replace id with id_new
ALTER TABLE position DROP CONSTRAINT position_pkey;
ALTER TABLE position DROP COLUMN id;
ALTER TABLE position RENAME COLUMN id_new TO id;
ALTER TABLE position ADD CONSTRAINT position_pkey PRIMARY KEY (id);

-- finish foreign keys
ALTER TABLE carrot ADD FOREIGN KEY (position_id) REFERENCES position (id);
ALTER TABLE figure ADD FOREIGN KEY (position_id) REFERENCES position (id);
