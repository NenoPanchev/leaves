create table departments
(
    id               bigserial
        primary key,
    created_at       timestamp,
    created_by       varchar(255),
    deleted          boolean,
    last_modified_at timestamp,
    last_modified_by varchar(255),
    name             varchar(255) not null
        constraint uk_j6cwks7xecs5jov19ro8ge3qk
            unique,
    admin_id         bigint
);

alter table departments
    owner to postgres;

create table permissions
(
    id               bigserial
        primary key,
    created_at       timestamp,
    created_by       varchar(255),
    deleted          boolean,
    last_modified_at timestamp,
    last_modified_by varchar(255),
    name             varchar(255)
);

alter table permissions
    owner to postgres;

create table roles
(
    id               bigserial
        primary key,
    created_at       timestamp,
    created_by       varchar(255),
    deleted          boolean,
    last_modified_at timestamp,
    last_modified_by varchar(255),
    name             varchar(255) not null
        constraint uk_ofx66keruapi6vyqpv6f2or37
            unique
);

alter table roles
    owner to postgres;

create table roles_permissions
(
    role_entity_id bigint not null
        constraint fkcdbljndeppr51frgjy734pnvh
            references public.roles,
    permissions_id bigint not null
        constraint fk570wuy6sacdnrw8wdqjfh7j0q
            references public.permissions
);

alter table roles_permissions
    owner to postgres;

create table users
(
    id               bigserial
        primary key,
    created_at       timestamp,
    created_by       varchar(255),
    deleted          boolean,
    last_modified_at timestamp,
    last_modified_by varchar(255),
    email            varchar(255) not null
        constraint uk_6dotkott2kjsp8vw4d0m25fb7
            unique,
    name             varchar(255),
    password         varchar(255) not null,
    department_id    bigint
        constraint fksbg59w8q63i0oo53rlgvlcnjq
            references public.departments
);

alter table users
    owner to postgres;

alter table departments
    add constraint fkcvxp4osj5mqfkv196khtumdqh
        foreign key (admin_id) references public.users;

create table departments_employees
(
    department_entity_id bigint not null
        constraint fkm43pi51roimdraacqgbme76ce
            references public.departments,
    employees_id         bigint not null
        constraint uk_kkqm4u9yevlnut2gxh9dp20u3
            unique
        constraint fkq15i68aqj5ys9lypsyaxybufj
            references public.users
);

alter table departments_employees
    owner to postgres;

create table users_roles
(
    user_entity_id bigint not null
        constraint fk7v417qhe0i2m9h8njggvciv00
            references public.users,
    roles_id       bigint not null
        constraint fka62j07k5mhgifpp955h37ponj
            references public.roles
);

alter table users_roles
    owner to postgres;


