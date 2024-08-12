-- migration 5: rename primary keys of child tables

ALTER TABLE "Fox" RENAME COLUMN id to figure_id;
ALTER TABLE "Rabbit" RENAME COLUMN id to figure_id;