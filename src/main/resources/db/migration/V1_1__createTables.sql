    create table if not exists `'case'` (
        id varchar(255) not null,
        delivery_status varchar(255),
        openecase longtext,
        primary key (id)
    ) engine=InnoDB;

    create table if not exists case_mapping (
        external_case_id varchar(255) not null,
        case_id varchar(255) not null,
        case_type varchar(255) not null,
        service_name varchar(255),
        timestamp datetime(6),
        primary key (case_id, external_case_id)
    ) engine=InnoDB;

    alter table if exists case_mapping
       add constraint uq_external_case_id unique (external_case_id);
