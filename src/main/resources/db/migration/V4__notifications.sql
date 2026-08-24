create table notifications (
    id uuid primary key,
    user_id uuid not null references user_accounts(id) on delete cascade,
    notification_type varchar(30) not null,
    title varchar(120) not null,
    message varchar(300) not null,
    action_target varchar(30) not null,
    related_task_id uuid,
    dedup_key varchar(180) not null,
    read_at timestamp with time zone,
    created_at timestamp with time zone not null,
    unique (user_id, dedup_key)
);
create index idx_notifications_user_created on notifications(user_id, created_at desc);

