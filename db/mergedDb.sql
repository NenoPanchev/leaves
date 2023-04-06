create table if not exists departments
(
    id               bigserial
        constraint departments_pkey
            primary key,
    created_at       timestamp,
    created_by       varchar(255),
    deleted          boolean,
    last_modified_at timestamp,
    last_modified_by varchar(255),
    name             varchar(255) not null,
    admin_id         bigint
);

alter table departments
    owner to postgres;

create table if not exists permissions
(
    id               bigserial
        constraint permissions_pkey
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

create table if not exists roles
(
    id               bigserial
        constraint roles_pkey
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

create table if not exists roles_permissions
(
    role_id        bigint not null
        constraint roles_permissions_roles_id_fk
            references roles,
    permissions_id bigint not null
        constraint roles_permissions_permissions_id_fk
            references permissions
);

alter table roles_permissions
    owner to postgres;

create table if not exists users
(
    id               bigserial
        constraint users_pkey
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
        constraint users_departments_id_fk
            references departments
);

alter table users
    owner to postgres;

alter table departments
    add constraint departments_users_id_fk
        foreign key (admin_id) references users;

create table if not exists departments_employees
(
    department_id bigint not null
        constraint departments_employees_departments_id_fk
            references departments,
    employees_id  bigint not null
        constraint departments_employees_users_id_fk
            references users
);

alter table departments_employees
    owner to postgres;

create table if not exists users_roles
(
    user_entity_id bigint not null
        constraint users_roles_users_id_fk
            references users,
    roles_id       bigint not null
        constraint users_roles_roles_id_fk
            references roles
);

alter table users_roles
    owner to postgres;

create table if not exists types
(
    type_name        integer               not null,
    type_days        integer               not null,
    id               serial
        constraint types_pk
            primary key,
    created_at       timestamp             not null,
    created_by       varchar               not null,
    last_modified_at timestamp,
    last_modified_by varchar,
    deleted          boolean default false not null
);

alter table types
    owner to postgres;

create table if not exists employee_info
(
    id               serial
        constraint employee_info_pk
            primary key,
    type_id          integer
        constraint employee_info_types_id_fk
            references types,
    days_leave       integer default 20 not null,
    user_id          integer
        constraint employee_info_users_id_fk
            references users,
    created_at       timestamp,
    created_by       varchar,
    deleted          boolean,
    last_modified_at timestamp,
    last_modified_by varchar
);

alter table employee_info
    owner to postgres;

create table if not exists leave_requests
(
    id               integer generated by default as identity
        constraint leave_requests_pk
            primary key,
    approved         boolean,
    start_date       date not null,
    end_date         date not null,
    employee_info_id integer
        constraint leave_requests_employee_info_id_fk
            references employee_info,
    deleted          boolean,
    created_at       timestamp,
    created_by       varchar,
    last_modified_at timestamp,
    last_modified_by varchar
);

alter table leave_requests
    owner to postgres;
