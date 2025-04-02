    alter table if exists case_meta_data
      add column if not exists stats_only bit default 0;
