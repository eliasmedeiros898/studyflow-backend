alter table user_preferences
    add column review_difficulty_days integer not null default 1 check (review_difficulty_days between 1 and 90),
    add column review_developing_days integer not null default 3 check (review_developing_days between 1 and 90),
    add column review_proficient_days integer not null default 7 check (review_proficient_days between 1 and 90),
    add column review_mastered_days integer not null default 15 check (review_mastered_days between 1 and 180);

alter table study_tasks add column review_topic_key varchar(120);

update study_tasks task
set review_topic_key = lower(regexp_replace(trim(session.topic), '\s+', ' ', 'g'))
from study_sessions session
where task.source_session_id = session.id
  and task.origin = 'AUTOMATIC_REVIEW';

with duplicates as (
    select id, row_number() over (
        partition by user_id, subject_id, review_topic_key
        order by created_at desc, id
    ) as position
    from study_tasks
    where origin = 'AUTOMATIC_REVIEW'
      and completed = false
      and review_topic_key is not null
)
delete from study_tasks task
using duplicates duplicate
where task.id = duplicate.id and duplicate.position > 1;

create unique index idx_unique_pending_automatic_review
    on study_tasks (user_id, subject_id, review_topic_key)
    where origin = 'AUTOMATIC_REVIEW' and completed = false and review_topic_key is not null;

create index idx_sessions_user_subject_topic_date
    on study_sessions (user_id, subject_id, lower(trim(topic)), studied_on desc);

create index idx_tasks_user_automatic_pending
    on study_tasks (user_id, planned_date)
    where origin = 'AUTOMATIC_REVIEW' and completed = false;
