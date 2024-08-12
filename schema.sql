
    create table "Carrot" (
        id uuid not null,
        "position_id" uuid unique,
        primary key (id)
    );

    create table "Figure" (
        score integer not null,
        stat_moves integer not null,
        id uuid not null,
        "position_id" uuid unique,
        name varchar(255) not null,
        primary key (id)
    );

    create table "Fox" (
        "id" uuid not null,
        primary key ("id")
    );

    create table "Position" (
        x integer not null,
        y integer not null,
        id uuid not null,
        primary key (id)
    );

    create table "Rabbit" (
        nutrition integer not null,
        "id" uuid not null,
        primary key ("id")
    );

    alter table if exists "Carrot" 
       add constraint FKtn8t64h3e5jvh9h3cmu3xbupj 
       foreign key ("position_id") 
       references "Position";

    alter table if exists "Figure" 
       add constraint FK695ohe28gm1fadpwhdpri2qot 
       foreign key ("position_id") 
       references "Position";

    alter table if exists "Fox" 
       add constraint FKn8fo7dgo3synr08hfih9ou9c9 
       foreign key ("id") 
       references "Figure";

    alter table if exists "Rabbit" 
       add constraint FKi43d6kmw9ew5de5dk4b6rbx7k 
       foreign key ("id") 
       references "Figure";
