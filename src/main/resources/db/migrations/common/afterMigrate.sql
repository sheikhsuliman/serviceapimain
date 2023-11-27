DO
$$
    DECLARE
        t TEXT;
    BEGIN
        FOR t IN (
            SELECT pgt.tablename
            FROM pg_catalog.pg_tables pgt
            WHERE schemaname != 'pg_catalog'
              AND pgt.tablename NOT IN
                  (
                   'flyway_schema_history', -- not ours
                   'language', 'currency', 'usr_role', 'catalog_item', -- non integer PK
                   'gender', 'entity_tree_root', -- non serial PK
                   'jv_commit', 'jv_commit_property', 'jv_global_id', 'jv_snapshot', -- non ours
                    'shedlock' -- used to prevent concurrent db access (non serial PK)
                      )
              AND schemaname != 'information_schema'
            ORDER BY 1 ASC
        )
            LOOP
                EXECUTE 'SELECT setval(pg_get_serial_sequence(''' || t || ''', ''id''), coalesce(max(id),0) + 1, false) FROM "' || t || '";';
                -- RAISE INFO 'table name: % - %', t, nextval(pg_get_serial_sequence( t, 'id'));
            END LOOP;
    END
$$;

SET session_replication_role = 'origin';
