-- The application accesses PostgreSQL only through the Spring backend.
-- Supabase's public API roles must not read or mutate application tables directly.
do $$
begin
    if exists (select 1 from pg_roles where rolname = 'anon') then
        execute 'revoke all on table user_accounts, subjects, study_tasks, study_sessions, study_goals,
            notifications, password_reset_tokens, user_preferences from anon';
    end if;
    if exists (select 1 from pg_roles where rolname = 'authenticated') then
        execute 'revoke all on table user_accounts, subjects, study_tasks, study_sessions, study_goals,
            notifications, password_reset_tokens, user_preferences from authenticated';
    end if;
end $$;

alter table user_accounts enable row level security;
alter table subjects enable row level security;
alter table study_tasks enable row level security;
alter table study_sessions enable row level security;
alter table study_goals enable row level security;
alter table notifications enable row level security;
alter table password_reset_tokens enable row level security;
alter table user_preferences enable row level security;
