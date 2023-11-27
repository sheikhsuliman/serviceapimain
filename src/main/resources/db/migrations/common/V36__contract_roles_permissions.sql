-- declare
CREATE OR REPLACE FUNCTION grantPermissionsToRole(IN roleName text, IN permissionNames text[]) RETURNS VOID
AS
$function$
DECLARE
    i            INT = 0 ;
    roleId       int;
    permissionId int;
BEGIN
    SELECT id INTO roleId FROM role WHERE name = roleName;

    RAISE INFO 'role : % [%] has permissions: ', roleName, permissionNames;

    FOR i in array_lower(permissionNames, 1) .. array_upper(permissionNames, 1)
        LOOP
            SELECT id INTO permissionId FROM permission WHERE name = permissionNames[i];
            DELETE FROM role_permission WHERE role_id = roleId AND permission_id = permissionId;
            INSERT INTO role_permission (role_id, permission_id) VALUES (roleId, permissionId);
        END LOOP;

    RETURN;
END;
$function$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION revokePermissionsFromRole(IN roleName text, IN permissionNames text[]) RETURNS VOID
AS
$function$
DECLARE
    i            INT = 0 ;
    roleId       int;
    permissionId int;
BEGIN
    SELECT id INTO roleId FROM role WHERE name = roleName;

    RAISE INFO 'role : % [%] has permissions: ', roleName, permissionNames;

    FOR i in array_lower(permissionNames, 1) .. array_upper(permissionNames, 1)
        LOOP
            SELECT id INTO permissionId FROM permission WHERE name = permissionNames[i];
            DELETE FROM role_permission WHERE role_id = roleId AND permission_id = permissionId;
        END LOOP;

    RETURN;
END;
$function$
    LANGUAGE plpgsql;
--

DO
$$
    DECLARE
        maxRoleId       int;
        maxPermissionId int;
    BEGIN
        SELECT max(id) INTO maxRoleId FROM role;

        INSERT INTO role (id, name, description, deprecated, member_default, owner_default, project_role, system_role)
        VALUES (maxRoleId + 1, 'CUSTOMER', 'Owner of "customer" company', false, false, false, false, true)
             , (maxRoleId + 2, 'PROJECT_CUSTOMER', 'Project customer', false, false, false, true, true);

        select max(id) into maxPermissionId from permission;

        INSERT INTO permission (id, name, description, deprecated, project_permission, admin_permission)
        VALUES (maxPermissionId + 1, 'CONTRACT_SEND_OFFER',
                'Allows to send contract offers from contractor to customer', false, true, false)
             , (maxPermissionId + 2, 'CONTRACT_SEND_INVITATION',
                'Allows to send contract invitation from customer to contractor', false, true, false)
             , (maxPermissionId + 3, 'CONTRACT_ACCEPT_DECLINE_OFFER', 'Allows to accept/decline contractor offer',
                false, true, false)
             , (maxPermissionId + 4, 'CONTRACT_ACCEPT_DECLINE_INVITATION',
                'Allows to accept/decline customer invitation', false, true, false)
             , (maxPermissionId + 5, 'CONTRACT_LIST', 'Allows to see list of contracts', false, true, false)
             , (maxPermissionId + 6, 'CONTRACT_CREATE', 'Allows to creat a new contract', false, true, false)
             , (maxPermissionId + 7, 'CONTRACT_UPDATE', 'Allows to update existing contract', false, true, false)
             , (maxPermissionId + 8, 'CONTRACT_ARCHIVE', 'Allows to archive contract', false, true, false)
             , (maxPermissionId + 9, 'CONTRACT_VIEW_STATUS', 'Allows to view contract status', false, true, false)
             , (maxPermissionId + 10, 'CONTRACT_VIEW_DETAILS', 'Allows to view all contract details', false, true,
                false);

    END
$$;

SELECT roleHasPermissions(
               'CUSTOMER',
               ARRAY [
                   'COMPANY_TEAM_READ_LIST',
                   'COMPANY_TEAM_READ_DETAILS',
                   'COMPANY_TEAM_INVITE_USER',
                   'COMPANY_TEAM_UPDATE',
                   'COMPANY_TEAM_ARCHIVE',
                   'COMPANY_READ_PROFILE',
                   'COMPANY_READ_FINANCIAL_DETAILS',
                   'COMPANY_LIST',
                   'COMPANY_UPDATE',
                   'COMPANY_ARCHIVE',
                   'COMPANY_PROFILES_VIEW',
                   'COMPANY_PROFILES_EDIT',

                   'COMPANY_PROJECT_CREATE'
                   ]
           );

SELECT roleHasPermissions(
               'PROJECT_CUSTOMER',
               ARRAY [
                   'PROJECT_READ_DETAILS',
                   'PROJECT_UPDATE',
                   'PROJECT_ARCHIVE',
                   'LOCATION_READ',
                   'LOCATION_CREATE',
                   'LOCATION_UPDATE',
                   'LOCATION_ARCHIVE',
                   'TASK_READ_LIST',
                   'TASK_READ_DETAILS',
                   'TASK_CREATE_UPDATE_MAIN',
                   'TASK_ARCHIVE_MAIN',

                   'MEDIA_CREATE',
                   'MEDIA_READ',
                   'MEDIA_UPDATE',
                   'MEDIA_ARCHIVE',

                   'PROJECT_TEAM_ADD_COMPANY',
                   'PROJECT_TEAM_ARCHIVE_COMPANY',

                   'CONTRACT_LIST',
                   'CONTRACT_CREATE',
                   'CONTRACT_UPDATE',
                   'CONTRACT_ARCHIVE',
                   'CONTRACT_VIEW_STATUS',
                   'CONTRACT_VIEW_DETAILS',
                   'CONTRACT_SEND_INVITATION',
                   'CONTRACT_ACCEPT_DECLINE_OFFER'
                   ]
           );

SELECT grantPermissionsToRole('PROJECT_OWNER',
                              ARRAY [
                                  'CONTRACT_LIST',
                                  'CONTRACT_CREATE',
                                  'CONTRACT_UPDATE',
                                  'CONTRACT_ARCHIVE',
                                  'CONTRACT_VIEW_STATUS',
                                  'CONTRACT_VIEW_DETAILS',
                                  'CONTRACT_SEND_OFFER',
                                  'CONTRACT_ACCEPT_DECLINE_INVITATION'
                                  ]
           );

SELECT revokePermissionsFromRole('PROJECT_OWNER',
                                 ARRAY [
                                     'PROJECT_TEAM_INVITE_COMPANY'
                                     ]
           );

SELECT grantPermissionsToRole('PROJECT_MANAGER',
                              ARRAY [
                                  'CONTRACT_LIST',
                                  'CONTRACT_CREATE',
                                  'CONTRACT_UPDATE',
                                  'CONTRACT_ARCHIVE',
                                  'CONTRACT_VIEW_STATUS',
                                  'CONTRACT_VIEW_DETAILS',
                                  'CONTRACT_SEND_OFFER',
                                  'CONTRACT_ACCEPT_DECLINE_INVITATION'
                                  ]
           )
;

SELECT revokePermissionsFromRole('PROJECT_ADMIN',
                                 ARRAY [
                                     'PROJECT_TEAM_INVITE_COMPANY'
                                     ]
           );


ALTER TABLE contract_task
    ADD COLUMN unit_id INTEGER REFERENCES unit(id),
    ADD COLUMN price_per_unit DECIMAL(19, 2),
    ADD COLUMN amount decimal(19, 2)
;
