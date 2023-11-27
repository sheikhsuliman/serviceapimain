DO
$$
    DECLARE
        maxRoleId int;
        maxPermissionId int;
    BEGIN
        select max(id) into maxRoleId from role;
        select max(id) into maxPermissionId from permission;

        INSERT INTO role (id, name, description, deprecated, member_default, owner_default, project_role, system_role)
        VALUES
            (maxRoleId+1, 'GLOBAL_NOTIFICATION_ADMIN', 'This role allows to control global and any worker notification settings', false, false, false, false, false)
        ;

        INSERT INTO permission (id, name, description, deprecated, project_permission, admin_permission)
        VALUES
            (maxPermissionId + 1, 'VIEW_SELF_NOTIFICATION_SETTINGS','Allows member to view own notification settings', false, false, false),
            (maxPermissionId + 2, 'VIEW_OTHER_NOTIFICATION_SETTINGS','Allows administrator to view some one else notification settings', false, false, false),
            (maxPermissionId + 3, 'VIEW_GLOBAL_NOTIFICATION_SETTINGS','Allows administrator to view default notification settings', false, false, false),
            (maxPermissionId + 4, 'UPDATE_SELF_NOTIFICATION_SETTINGS','Allows member to update own notification settings', false, false, false),
            (maxPermissionId + 5, 'UPDATE_OTHER_NOTIFICATION_SETTINGS','Allows administrator to update some one else notification settings', false, false, false),
            (maxPermissionId + 6, 'UPDATE_GLOBAL_NOTIFICATION_SETTINGS','Allows administrator to update default notification settings', false, false, false)
        ;
    END
$$;
select grantpermissionstorole('GLOBAL_NOTIFICATION_ADMIN', ARRAY [
    'VIEW_SELF_NOTIFICATION_SETTINGS', 'VIEW_OTHER_NOTIFICATION_SETTINGS', 'VIEW_GLOBAL_NOTIFICATION_SETTINGS',
    'UPDATE_SELF_NOTIFICATION_SETTINGS', 'UPDATE_OTHER_NOTIFICATION_SETTINGS', 'UPDATE_GLOBAL_NOTIFICATION_SETTINGS'
]);


select grantpermissionstorole('COMPANY_OWNER', ARRAY [
    'VIEW_SELF_NOTIFICATION_SETTINGS', 'VIEW_OTHER_NOTIFICATION_SETTINGS', 'VIEW_GLOBAL_NOTIFICATION_SETTINGS',
    'UPDATE_SELF_NOTIFICATION_SETTINGS', 'UPDATE_OTHER_NOTIFICATION_SETTINGS'
]);

select grantpermissionstorole('COMPANY_WORKER', ARRAY [
    'VIEW_SELF_NOTIFICATION_SETTINGS',  'VIEW_GLOBAL_NOTIFICATION_SETTINGS',
    'UPDATE_SELF_NOTIFICATION_SETTINGS'
]);

