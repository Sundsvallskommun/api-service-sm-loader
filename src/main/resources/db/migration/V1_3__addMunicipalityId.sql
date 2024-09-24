    alter table if exists case_mapping
      add column if not exists municipality_id varchar(255);
    create index municipality_id_index
      on case_mapping (municipality_id);
