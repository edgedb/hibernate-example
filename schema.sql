
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
        figure_id uuid not null,
        primary key (figure_id)
    );

    create table "Position" (
        x integer not null,
        y integer not null,
        id uuid not null,
        primary key (id)
    );

    create table "Rabbit" (
        nutrition integer not null,
        figure_id uuid not null,
        primary key (figure_id)
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
       add constraint FK3xdohoh9h6cwh7r5q25t3camd 
       foreign key (figure_id) 
       references "Figure";

    alter table if exists "Rabbit" 
       add constraint FKjxsmj48dcvhsvw0atbg64n001 
       foreign key (figure_id) 
       references "Figure";
