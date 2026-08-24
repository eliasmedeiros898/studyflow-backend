create table user_accounts (
    id uuid primary key,
    name varchar(100) not null,
    email varchar(180) not null unique,
    password_hash varchar(100) not null,
    timezone varchar(80) not null default 'America/Sao_Paulo',
    target_exam_name varchar(120),
    target_exam_date date,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table subjects (
    id uuid primary key,
    user_id uuid not null references user_accounts(id) on delete cascade,
    name varchar(80) not null,
    color varchar(20) not null,
    archived boolean not null default false,
    created_at timestamp with time zone not null,
    unique (user_id, name)
);
create index idx_subjects_user on subjects(user_id);

create table study_tasks (
    id uuid primary key,
    user_id uuid not null references user_accounts(id) on delete cascade,
    subject_id uuid not null references subjects(id),
    title varchar(120) not null,
    planned_date date not null,
    task_type varchar(30) not null,
    completed boolean not null default false,
    completed_at timestamp with time zone,
    created_at timestamp with time zone not null
);
create index idx_tasks_user_date on study_tasks(user_id, planned_date);

create table study_sessions (
    id uuid primary key,
    user_id uuid not null references user_accounts(id) on delete cascade,
    subject_id uuid not null references subjects(id),
    topic varchar(120) not null,
    duration_minutes integer not null check (duration_minutes between 1 and 1440),
    studied_on date not null,
    questions integer not null default 0 check (questions >= 0),
    correct_answers integer not null default 0 check (correct_answers between 0 and questions),
    session_type varchar(30) not null,
    created_at timestamp with time zone not null
);
create index idx_sessions_user_date on study_sessions(user_id, studied_on);

