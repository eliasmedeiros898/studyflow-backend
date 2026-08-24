alter table user_accounts
    add column token_version integer not null default 0,
    add column failed_login_attempts integer not null default 0,
    add column locked_until timestamp with time zone;

create table password_reset_tokens (
    id uuid primary key,
    user_id uuid not null references user_accounts(id) on delete cascade,
    token_hash varchar(64) not null unique,
    expires_at timestamp with time zone not null,
    used_at timestamp with time zone,
    created_at timestamp with time zone not null
);
create index idx_password_reset_user on password_reset_tokens(user_id);
create index idx_password_reset_expiry on password_reset_tokens(expires_at);

create table user_preferences (
    user_id uuid primary key references user_accounts(id) on delete cascade,
    focus_minutes integer not null default 25 check (focus_minutes between 1 and 180),
    short_break_minutes integer not null default 5 check (short_break_minutes between 1 and 60),
    long_break_minutes integer not null default 15 check (long_break_minutes between 1 and 120),
    focus_cycles integer not null default 4 check (focus_cycles between 1 and 8),
    sound_enabled boolean not null default true,
    browser_notifications boolean not null default false,
    updated_at timestamp with time zone not null
);

insert into user_preferences (user_id, updated_at)
select id, now() from user_accounts;
