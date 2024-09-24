    alter table if exists case_mapping
      add column if not exists municipality_id varchar(255);
