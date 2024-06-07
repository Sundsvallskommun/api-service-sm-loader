
    create table `'case'` (
        id varchar(255) not null,
        delivery_status enum ('PENDING','CREATED','FAILED'),
        family_id varchar(255),
        instance enum ('INTERNAL','EXTERNAL'),
        `open-e-case` longtext,
        `open-e-case-id` varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table case_mapping (
        errand_id varchar(255) not null,
        external_case_id varchar(255) not null,
        case_type varchar(255) not null,
        modified datetime(6),
        primary key (errand_id, external_case_id)
    ) engine=InnoDB;

    alter table if exists `'case'`
       add constraint `uq_open-e_case_id_instance` unique (`open-e-case-id`, instance);
