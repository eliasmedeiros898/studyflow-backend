alter table study_tasks add column origin varchar(20) not null default 'MANUAL';
alter table study_tasks add column source_session_id uuid references study_sessions(id) on delete set null;
create index idx_tasks_source_session on study_tasks(source_session_id);
