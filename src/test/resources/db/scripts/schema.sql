
    create table `'case'` (
        id varchar(255) not null,
        created datetime(6),
        delivery_status varchar(255),
        external_case_id varchar(255),
        open_e_case longtext,
        family_id varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table case_mapping (
        errand_id varchar(255) not null,
        external_case_id varchar(255) not null,
        case_type varchar(255) not null,
        modified datetime(6),
        municipality_id varchar(255),
        primary key (errand_id, external_case_id)
    ) engine=InnoDB;

    create table case_meta_data (
        family_id varchar(255) not null,
        instance varchar(255),
        municipality_id varchar(255),
        namespace varchar(255),
        open_e_import_status varchar(255),
        open_e_update_status varchar(255),
        primary key (family_id)
    ) engine=InnoDB;

    create index municipality_id_index
       on case_mapping (municipality_id);

    alter table if exists `'case'`
       add constraint fk_case_case_meta_data_family_id
       foreign key (family_id)
       references case_meta_data (family_id);

