# Example Hibernate project

An example project using Spring Boot and Hibernate implementing a simple game
named Rabbits, Carrots and Foxes.

It is meant to showcase a migration from a plain PostgreSQL to EdgeDB's SQL adapter.

![](./docs/screenshot.png)

## Running

### Developer environment

Dev env is described in [flake.nix](./flake.nix). Currently, it is basically just JDK and Gradle.

### PostgreSQL database

This project requires PostgreSQL with schema described in [`schema.sql`](./schema.sql).
PostgreSQL connection params can be set in [`application.properties`](./src/main/resources/application.properties).

Start PostgreSQL in docker:

    $ docker run -it -p 5432:5432 -e POSTGRES_PASSWORD=pass1234 --name=example-hibernate postgres

Apply the schema:

    $ psql postgres://postgres:pass1234@localhost:5432 --file schema.sql

### Run gradle

Then, start the Spring Boot project:

    $ gradle bootRun

## Migration

This repo describes all steps needed to migrate from the plain PostgreSQL to EdgeDB.
Each of the steps is visible in the [git commit log](https://github.com/edgedb/hibernate-example/commits/):

1. Fixup Hibernate models (and PostgreSQL schema) to fit EdgeDB requirements:

   1. convert model ids to use UUID instead of sequential integers,
   2. rename tables to use Title case instead of lowercase,
   3. rename primary keys from of child tables from `id` to `parent_type_id`,

2. Create EdgeDB project

3. Export the with `pg_dump`

4. Import into EdgeDB

When working on migrating your Hibernate project to EdgeDB, you might not need to make all of the fixups in step 1 or might have to do additional fixups to make your schema expressible in ESDL. The recommended workflow is to first start with no fix-ups and try to express your models with ESDL. During this process, you will find the fixups needed to match the SQL schema expected by Hibernate and SQL schema that is translated from your ESDL.

### Fixup Hibernate models to fit EdgeDB

Database schema in EdgeDB is managed by using the EdgeDB Schema Definition Language, which is an object-relational data model.
That schema is then translated into a PostgreSQL schema using a few rules:

| ESDL                    | PostgreSQL                                                                                                         |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------ |
| object type named `Foo` | table `"Foo"` that contains columns `id uuid`, `__type__ uuid` and one column for each of the properties and links |
| property `bar`          | column `bar`                                                                                                       |
| link `baz`              | column `baz_id` of type uuid                                                                                       |

These translation rules mean that our tables must have primary keys named `id` and be of type `uuid`, otherwise we cannot import them into EdgeDB.

For this Hibernate-example project, we had to change our Hibernate models, so they expect a schema that is expressible by ESDL:

1.  **convert model ids to use UUID instead of sequential integers**

    To do that, we added `@GeneratedValue(strategy = GenerationType.UUID)` annotation to id of each model.
    Then, we added SQL migrations that ALTERed existing tables and converted columns from INT to UUID.
    The tricky part was also changing the types of foreign keys pointing at that primary key.

2.  **rename tables to use Title case instead of lowercase**

    In EdgeDB object types usually use Title case instead of lowercase. While this is optional, we do think it is a good practice, so we added `@Table(name = "\"Figure\"")` to each of the models to instruct Hibernate to look for table `Figure` instead of `figure`.

3.  **rename primary keys from of child tables from `id` to `parent_type_id`**

    Our Hibernate models use inheritance, for example `class Fox extends Figure`. ESDL does support inheritance, which would be expressed with:

    ```esdl
    type Figure {
        required property name: str;
    }
    type Fox extending Figure;
    ```

    Such schema would be translated into:

    ```SQL
    CREATE TABLE "Figure" (id UUID, __type__ UUID, name TEXT);
    CREATE TABLE "Fox" (id UUID, __type__ UUID, name TEXT);

    INSERT INTO "Fox" (name) VALUES ('Ferdinand');
    ```

    As you can see, table `Fox` inherits the column `name`. When a new `Fox` is inserted, a row is also created in the `Figure` table (this is only visible on the outside, underlying database stores a single row of data).

    But Hibernate does not expect this behavior, which causes problems.
    For example, to create a new `Fox`, Hibernate would issue these queries:

    ```SQL
    INSERT INTO "Figure" (id, name) VALUES ('1253a356-abb1-4f78-b8f3-3054b88a8d98', 'Ferdinand');
    -- OK

    INSERT INTO "Fox" (id) VALUES ('1253a356-abb1-4f78-b8f3-3054b88a8d98');
    -- Error: duplicate primary key in "Figure"
    -- Error: Fox is missing a required property "name"
    ```

    So we cannot use EdgeDB inheritance, but instead translate it to links:

    ```esdl
    type Figure {
        required property name: str;
    };
    type Fox {
        link figure: Figure;
    };
    ```

    Now the following PostgreSQL schema is expected:

    ```sql
    CREATE TABLE "Figure" (id UUID, __type__ UUID, name TEXT);
    CREATE TABLE "Fox" (id UUID, __type__ UUID, figure_id UUID);
    ```

    ... which we get by adding `@PrimaryKeyJoinColumn(name = "figure_id")` to `Fox` model.

### Create EdgeDB project

For this step, we will need `edgedb-cli` and an development edgedb-server, which can be downloaded and started by the cli.
Then you can run:

```
$ edgedb project init
```

... which will ask a few questions about versions and names.

After that, we can edit `dbschema/default.esdl` file and describe our database schema.
To create a migration and apply it, we run:

```
$ edgedb migration create
Created example-hibernate/dbschema/migrations/00001-m1dlr2f.edgeql, id: m1dlr2fv7zbcfx5estvqgq4dqx6che37muke2uz2urtkfcgaloelra

$ edgedb migration apply
Applying m1dlr2fv7zbcfx5estvqgq4dqx6che37muke2uz2urtkfcgaloelra (00001-m1dlr2f.edgeql)
... parsed
... applied
```

### Export the with `pg_dump`

pg_dump is a PostgreSQL utility tool that can export all of the data in a PostgreSQL database into an .sql file.
It is the recommended tool for upgrading instances and the easiest way of getting data out of PostgreSQL and into EdgeDB.

There is a few options you should use to get a dump that can be imported into EdgeDB:

- `--data-only` will prevent DDL commands (`CREATE TABLE` etc.), since schema is manged by ESDL,
- `--inserts` will use `INSERT` instead of `COPY`, since EdgeDB currently does not support `COPY` statements,
- `--column-inserts` will be more explicit about which columns are inserted, which is needed in some cases.

The full command to dump the data would look like this:

```bash
$ pg_dump postgres://postgres:pass1234@localhost:5432/postgres \
    --inserts --column-inserts --data-only > dump.sql
```

### Import into EdgeDB

To connect to a development EdgeDB server over SQL adapter, we can use the credentials printed by the following command:

```
$ edgedb instance credentials
┌──────────────┬───────────┐
│ Host         │ localhost │
│ Port         │ 10344     │
│ User         │ edgedb    │
│ Password     │ <none>    │
│ Database     │ edgedb    │
│ TLS Security │ Default   │
└──────────────┴───────────┘
```

We can test the connection using psql (or any other PostgreSQL client):

```bash
$ psql postgres://edgedb@localhost:10344

psql (14.9, server 13.9)
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

edgedb=# SELECT * FROM "Fox"
 id | __type__ | figure_id
----+----------+-----------
(0 rows)
```

The data can now we imported using psql:

```
$ psql postgres://edgedb@localhost:10344 --file dump.sql

INSERT 0 1
... (many repeating rows) ...
INSERT 0 1
```

We can now point Hibernate at EdgeDB, by editing `application.properties` and the migration is complete.
