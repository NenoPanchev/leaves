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

create table if not exists types
(
    type_name        varchar               not null,
    type_days        integer               not null,
    id               serial
        constraint types_pk
            primary key,
    created_at       timestamp,
    created_by       varchar,
    last_modified_at timestamp,
    last_modified_by varchar,
    deleted          boolean default false not null
);

alter table types
    owner to postgres;

create table if not exists employee_info
(
    id                      serial
        constraint employee_info_pk
            primary key,
    type_id                 integer
        constraint employee_info_types_id_fk
            references types,
    carryover_days_leave    integer default 0  not null,
    current_year_days_leave integer default 20 not null,
    contract_start_date     date,
    created_at              timestamp,
    created_by              varchar,
    deleted                 boolean,
    last_modified_at        timestamp,
    last_modified_by        varchar,
    ssn                     varchar,
    address                 varchar,
    position                varchar
);

alter table employee_info
    owner to postgres;

create table if not exists employee_history
(
    id                      serial
        constraint employee_info_history_pk
            primary key,
    employee_info_id integer constraint employee_info_history_fk
        references employee_info,
    calendar_year             INTEGER default 0 not null,
    days_from_previous_year   INTEGER default 0 not null,
    contract_days             INTEGER default 0 not null,
    days_used                 INTEGER default 0 not null,
    days_left        INTEGER default 0 not null,
    created_at              timestamp,
    created_by              varchar,
    deleted                 boolean,
    last_modified_at        timestamp,
    last_modified_by        varchar
);
alter table employee_history
    owner to postgres;

create table if not exists employee_info_history
(
    employee_info_id BIGSERIAL,
    year             INTEGER,
    days_used        INTEGER,
    PRIMARY KEY (employee_info_id, year),
    FOREIGN KEY (employee_info_id) REFERENCES employee_info (id)
);
alter table employee_info_history
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
            references departments,
    employee_info_id integer
        constraint users_employee_info_id_fk
            references employee_info
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

create table if not exists requests
(
    id                  integer generated by default as identity
        constraint requests_pk
            primary key,
    request_type        varchar(15) not null,
    approved            boolean,
    start_date          date not null,
    end_date            date not null,
    employee_info_id    integer
        constraint requests_employee_info_id_fk
            references employee_info,
    deleted             boolean,
    created_at          timestamp,
    created_by          varchar,
    last_modified_at    timestamp,
    last_modified_by    varchar,
    approved_start_date date,
    approved_end_date   date
);

alter table requests
    owner to postgres;

create table if not exists password_reset_tokens
(
    id          bigserial
        constraint password_reset_tokens_pk
            primary key,
    token       varchar   not null,
    expiry_date timestamp not null,
    user_id     integer
        constraint password_reset_tokens_users_id_fk
            references users
);

alter table password_reset_tokens
    owner to postgres;

