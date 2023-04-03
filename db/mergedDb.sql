create table leave_manager.departments
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
        constraint fkcvxp4osj5mqfkv196khtumdqh
            references users
);

alter table leave_manager.departments
    owner to postgres;

create table leave_manager.permissions
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

alter table leave_manager.permissions
    owner to postgres;

create table leave_manager.roles
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

alter table leave_manager.roles
    owner to postgres;

create table leave_manager.roles_permissions
(
    role_entity_id bigint not null
        constraint fkcdbljndeppr51frgjy734pnvh
            references roles,
    permissions_id bigint not null
        constraint fk570wuy6sacdnrw8wdqjfh7j0q
            references permissions
);

alter table leave_manager.roles_permissions
    owner to postgres;

create table leave_manager.departments_employees
(
    department_entity_id bigint not null
        constraint fkm43pi51roimdraacqgbme76ce
            references departments,
    employees_id         bigint not null
        constraint uk_kkqm4u9yevlnut2gxh9dp20u3
            unique
        constraint fkq15i68aqj5ys9lypsyaxybufj
            references users
);

alter table leave_manager.departments_employees
    owner to postgres;

create table leave_manager.users_roles
(
    user_entity_id bigint not null
        constraint fk7v417qhe0i2m9h8njggvciv00
            references users,
    roles_id       bigint not null
        constraint fka62j07k5mhgifpp955h37ponj
            references roles
);

alter table leave_manager.users_roles
    owner to postgres;

create table leave_manager.types
(
    type_name    integer               not null,
    type_days    integer               not null,
    id           serial
        constraint types_pk
            primary key,
    date_created timestamp             not null,
    created_by   varchar               not null,
    last_updated timestamp,
    updated_by   varchar,
    is_deleted   boolean default false not null
);

alter table leave_manager.types
    owner to postgres;

create table leave_manager.employee_info
(
    id         serial
        constraint employee_info_pk
            primary key,
    type_id    integer
        constraint employee_info_types_id_fk
            references leave_manager.types,
    days_leave integer default 20 not null
);

alter table leave_manager.employee_info
    owner to postgres;

create table leave_manager.users
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
            references departments,
    employee_info_id integer
        constraint users_employee_info_id_fk
            references leave_manager.employee_info
);

alter table leave_manager.users
    owner to postgres;

create table leave_manager.leave_requests
(
    employee_id  integer
        constraint leave_requests_employee_info_id_fk
            references leave_manager.employee_info,
    approved     boolean,
    start_date   date                  not null,
    end_date     date                  not null,
    id           serial
        constraint leave_requests_pk
            primary key,
    date_created timestamp             not null,
    created_by   varchar,
    last_updated timestamp,
    updated_by   varchar,
    is_deleted   boolean default false not null
);

alter table leave_manager.leave_requests
    owner to postgres;


