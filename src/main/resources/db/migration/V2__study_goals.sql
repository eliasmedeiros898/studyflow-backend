create table study_goals (
    id uuid primary key,
    user_id uuid not null references user_accounts(id) on delete cascade,
    period_start date not null,
    period_end date not null,
    target_minutes integer not null check (target_minutes between 1 and 10080),
    target_questions integer not null check (target_questions between 1 and 100000),
    target_accuracy integer not null check (target_accuracy between 1 and 100),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    unique (user_id, period_start, period_end)
);

create index idx_goals_user_period on study_goals(user_id, period_start, period_end);
