create table author
(
     id             serial       primary key,
     full_name      text         not null,
     creation_date   timestamp   not null
);
alter table budget add column author_id int;
