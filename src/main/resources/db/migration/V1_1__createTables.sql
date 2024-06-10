
    create table `'case'` (
        id varchar(255) not null,
        delivery_status varchar(255),
        external_case_id varchar(255),
        family_id varchar(255),
        instance varchar(255),
        open_e_case longtext,
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
       add constraint uq_external_case_id_instance unique (external_case_id, instance);