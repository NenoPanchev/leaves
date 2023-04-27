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

