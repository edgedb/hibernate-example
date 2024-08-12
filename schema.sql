
    create table carrot (
        id uuid not null,
        position_id uuid unique,
        primary key (id)
    );

    create table figure (
        id uuid not null,
        score integer not null,
        stat_moves integer not null,
        position_id uuid unique,
        name varchar(255) not null,
        primary key (id)
    );

    create table fox (
        id uuid not null,
        primary key (id)
    );

    create table position (
        x integer not null,
        y integer not null,
        id uuid not null,
        primary key (id)
    );

    create table rabbit (
        id uuid not null,
        nutrition integer not null,
        primary key (id)
    );

    alter table if exists carrot 
       add constraint FKhrgdgpuv75jwgktsf9dttb3o3 
       foreign key (position_id) 
       references position;

    alter table if exists figure 
       add constraint FK5l7i6hf0pgpagd1lh4xkf82hd 
       foreign key (position_id) 
       references position;

    alter table if exists fox 
       add constraint FKfgt5adki5traw7le61erlooby 
       foreign key (id) 
       references figure;

    alter table if exists rabbit 
       add constraint FKrywoufcma1x2wnf9tmuosvtbb 
       foreign key (id) 
       references figure;
