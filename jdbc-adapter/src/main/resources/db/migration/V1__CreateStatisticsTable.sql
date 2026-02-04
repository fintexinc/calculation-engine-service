create table if not exists fas_usage_statistics
(
    id                serial primary key,
    day_0_count       numeric(10) not null,
    day_1_count       numeric(10) not null,
    day_2_count       numeric(10) not null,
    day_3_count       numeric(10) not null,
    day_4_count       numeric(10) not null,
    day_5_count       numeric(10) not null,
    day_6_count       numeric(10) not null,
    cache_name_entity varchar(60) not null,
    cache_category    varchar(60) not null,
    provider          varchar(40) not null,
    holding_id        varchar(60) not null,
    holding_id_type   varchar(40) not null,
    holding_type      varchar(30) not null
);