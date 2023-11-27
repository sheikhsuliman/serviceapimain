DO
$$
    DECLARE
        maxPermissionId int;
    BEGIN
        select max(id) into maxPermissionId from permission;

        INSERT INTO permission (id, name, description, deprecated, project_permission, admin_permission)
        VALUES
               (maxPermissionId + 1, 'COMPANY_PROJECT_RESTORE',
                'Allows user to see archived projects and restore them', false, false, false),
            (maxPermissionId + 2, 'WORKLOG_MULTI_START',
                'Allows worker to start multiple timers at the same time', false, true, false)
        ;
    END
$$;

select grantpermissionstorole('COMPANY_OWNER', ARRAY [ 'COMPANY_PROJECT_RESTORE']);
select grantpermissionstorole('PROJECT_OWNER', ARRAY [ 'WORKLOG_MULTI_START']);
select grantpermissionstorole('PROJECT_MANAGER', ARRAY [ 'WORKLOG_MULTI_START']);