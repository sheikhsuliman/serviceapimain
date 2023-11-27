CREATE TABLE custom_role
(
    id              SERIAL,
    name            VARCHAR(255),

    description     VARCHAR(1024),
    deprecated      BOOLEAN DEFAULT false,
    member_default  BOOLEAN DEFAULT false,
    company_default BOOLEAN DEFAULT false,
    project_role    BOOLEAN DEFAULT false,
    system_role     BOOLEAN DEFAULT false,

    PRIMARY KEY (id)
);

CREATE TABLE custom_permission
(
    id                 SERIAL,
    name               VARCHAR(255),

    description        VARCHAR(1024),
    deprecated         BOOLEAN DEFAULT false,
    project_permission BOOLEAN DEFAULT false,
    system_permission  BOOLEAN DEFAULT false,

    PRIMARY KEY (id)
);

CREATE TABLE custom_role_permission
(
    id            SERIAL,
    role_id       INTEGER REFERENCES custom_role (id),
    permission_id INTEGER REFERENCES custom_permission (id),
    PRIMARY KEY (id)
);



-- define roles
INSERT INTO custom_role (id, name, description, member_default, company_default, project_role, system_role ) VALUES
(1, 'PROJECT_OWNER', 'Company Owner of Company which created the project', false, false, true, false),
(2, 'PROJECT_ADMIN', 'Delegate of Project Owner', false, false, true, false),
(3, 'PROJECT_MANAGER', 'Company Owner of Contractor Company invited to the project', false, false, true, false),
(4, 'PROJECT_WORKER', 'Worker of Contractor Company invited to the project', false, false, true, false),
(100, 'COMPANY_BOOTSTRAP', 'System Role, used to invite first company to the system', false, false, false, true),
(101, 'COMPANY_OWNER', 'Company Owner', false, true, false, false),
(102, 'COMPANY_ADMIN', 'Delegate of Company Owner', false, false, false, false),
(103, 'COMPANY_WORKER', 'Company Member', true, false, false, false);

-- define permissions
INSERT INTO custom_permission (id, name, description, deprecated, system_permission, project_permission) VALUES
(1, 'PROJECT_READ_LIST', 'View List of Projects', false, false, false),
(2, 'PROJECT_READ_DETAILS', 'View Project details', false, false, true),
(3, 'PROJECT_UPDATE', 'Update Project', false, false, true),
(4, 'PROJECT_ARCHIVE', 'Archive Project', false, false, true),
(10, 'LOCATION_READ', 'View Locations', false, false, true),
(11, 'LOCATION_CREATE', 'Create new Location', false, false, true),
(12, 'LOCATION_UPDATE', 'Update a Location', false, false, true),
(13, 'LOCATION_ARCHIVE', 'Archive a Location', false, false, true),
(20, 'TASK_READ_LIST', 'View Tasks', false, false, true),
(21, 'TASK_READ_DETAILS', 'View Task details', false, false, true),
(22, 'TASK_CREATE_UPDATE_MAIN', 'Create/Update Main Task', false, false, true),
(23, 'TASK_CREATE_UPDATE_SUB', 'Create/Update Sub Task', false, false, true),
(24, 'TASK_REVIEW', 'Review Task', false, false, true),
(25, 'TASK_APPROVE_REJECT', 'Approve REject task', true, false, true),
(26, 'TASK_ARCHIVE_MAIN', 'Archive Main Task', false, false, true),
(27, 'TASK_ARCHIVE_SUB', 'Archive Sub-Task', false, false, true),
(30, 'WORKLOG_READ', 'View Task Work Log', false, false, true),
(31, 'WORKLOG_START_STOP', 'Start/Stop timer on a task/sub-task', false, false, true),
(32, 'WORKLOG_UPDATE', 'Update Task Work Log Entry', false, false, true),
(33, 'WORKLOG_ARCHIVE', 'Archie Task Work Log Entry', false, false, true),
(40, 'TASK_TEAM_SET_USER', 'Assign worker to task team', false, false, true),
(41, 'TASK_TEAM_ARCHIVE', 'Remove worker from task team', false, false, true),
(50, 'MEDIA_READ', 'View file/folders in Media Widget', false, false, true),
(51, 'MEDIA_UPDATE', 'Update files/folders in Media Widget', false, false, true),
(52, 'MEDIA_ARCHIVE', 'Archive files/folders in Media Widget', false, false, true),
(60, 'PROJECT_TEAM_ADD_COMPANY', 'Invite Company to Project', false, false, true),
(61, 'PROJECT_TEAM_INVITE_COMPANY', 'Invite Company to Project', true, false, true),
(62, 'PROJECT_TEAM_ARCHIVE_COMPANY', 'Remove Company from the Project', false, false, true),
(63, 'PROJECT_TEAM_CHANGE_ADMIN_ROLE', 'Change admin role', true, false, true),
(64, 'PROJECT_TEAM_CHANGE_NON_ADMIN_ROLE', 'Change non-admin role', true, false, true),
(65, 'PROJECT_TEAM_ADD_USER', 'Add worker to project', false, false, true),
(66, 'PROJECT_TEAM_ARCHIVE_USER', 'Remove worker from project', false, false, true),
(100, 'COMPANY_TEAM_READ_LIST', 'View list of members of Company', true, false, false),
(101, 'COMPANY_TEAM_READ_DETAILS', 'View details of member of Company', true, false, false),
(103, 'COMPANY_TEAM_INVITE_USER', 'Invite new member to Company', false, false, false),
(104, 'COMPANY_TEAM_UPDATE', '???', true, false, false),
(105, 'COMPANY_TEAM_MAKE_REVOKE_ADMIN', 'Make/Revoke company member an Admin', true, false, false),
(106, 'COMPANY_TEAM_ARCHIVE', 'Archive company member', false, false, false),
(120, 'COMPANY_READ_PROFILE', 'View Company Profile', false, false, false),
(121, 'COMPANY_READ_FINANCIAL_DETAILS', 'View Company Financial Details', false, false, false),
(122, 'COMPANY_LIST', 'View List of Companies in Directory', false, false, false),
(123, 'COMPANY_INVITE', 'Invite new Company to the System', false, false, false),
(124, 'COMPANY_UPDATE', 'Update Company Profile', false, false, false),
(125, 'COMPANY_ARCHIVE', 'Archive Company', false, false, false),
(140, 'COMPANY_PROJECT_CREATE', 'Create Project', false, false, false),
(141, 'TASK_COMPLETE', 'Complete Task', false, false, true),
(142, 'TASK_CONTRACTOR_REVIEW', 'Approve/Reject Task in Contractor Review state', false, false, true),
(143, 'MEDIA_CREATE','Create new File/Folder in Media Widget', false, false, true),
(144, 'GLOBAL_CATALOG_EDIT', 'Edit Global Catalog', false, false, false),
(145, 'COMPANY_CATALOG_EDIT', 'Edit Company Catalog', false, false, false),
(146, 'COMPANY_PROFILES_VIEW', 'View profiles of Company Members', false, false, false),
(147, 'COMPANY_PROFILES_EDIT', 'Edit profiles of Company Members', false, false, false),
(148, 'ROLES_UPDATE', 'Create/Update/Archive roles', false, true, false),
(149, 'COMPANY_ROLES_ASSIGN', 'Assign Company Roles', false, false, false),
(159, 'PROJECT_ROLES_ASSIGN', 'Assign Project Roles', false, false, true)
;

-- declare
CREATE OR REPLACE FUNCTION customRoleHasPermissions( IN roleName text, IN permissionNames text[]) RETURNS VOID
AS $function$
DECLARE
    i INT = 0 ;
    roleId int;
    permissionId int;
BEGIN
    SELECT id INTO roleId FROM custom_role WHERE  name = roleName;
    DELETE FROM custom_role_permission WHERE role_id = roleId;

    RAISE INFO 'role : % [%] has permissions: ', roleName, permissionNames;

    FOR i in array_lower(permissionNames, 1) .. array_upper(permissionNames, 1)
        LOOP
            SELECT id INTO permissionId FROM custom_permission WHERE name = permissionNames[i];
            INSERT INTO custom_role_permission ( role_id, permission_id) VALUES (roleId, permissionId);
            -- RAISE INFO ' - % [%]', permissionNames[i], permissionId;
        END LOOP;
    RETURN;
END;
$function$
    LANGUAGE plpgsql;

-- assign permissions to roles
SELECT customRoleHasPermissions(
               'PROJECT_OWNER',
               ARRAY [
                   'PROJECT_READ_LIST',
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
                   'TASK_CREATE_UPDATE_SUB',
                   'TASK_ARCHIVE_MAIN',
                   'TASK_ARCHIVE_SUB',
                   'WORKLOG_READ',
                   'WORKLOG_START_STOP',
                   'WORKLOG_UPDATE',
                   'WORKLOG_ARCHIVE',
                   'TASK_TEAM_SET_USER',
                   'TASK_TEAM_ARCHIVE',
                   'MEDIA_CREATE',
                   'MEDIA_READ',
                   'MEDIA_UPDATE',
                   'MEDIA_ARCHIVE',
                   'PROJECT_TEAM_ADD_COMPANY',
                   'PROJECT_TEAM_INVITE_COMPANY',
                   'PROJECT_TEAM_ARCHIVE_COMPANY',
                   'PROJECT_TEAM_CHANGE_ADMIN_ROLE',
                   'PROJECT_TEAM_CHANGE_NON_ADMIN_ROLE',
                   'PROJECT_TEAM_ADD_USER',
                   'PROJECT_TEAM_ARCHIVE_USER',
                   'TASK_REVIEW',
                   'TASK_CONTRACTOR_REVIEW',
                   'PROJECT_ROLES_ASSIGN'
                   ]
           );

SELECT customRoleHasPermissions(
               'PROJECT_ADMIN',
               ARRAY [
                   'PROJECT_READ_LIST',
                   'PROJECT_READ_DETAILS',
                   'PROJECT_UPDATE',
                   'LOCATION_READ',
                   'LOCATION_CREATE',
                   'LOCATION_UPDATE',
                   'LOCATION_ARCHIVE',
                   'TASK_READ_LIST',
                   'TASK_READ_DETAILS',
                   'TASK_CREATE_UPDATE_MAIN',
                   'TASK_CREATE_UPDATE_SUB',
                   'TASK_ARCHIVE_MAIN',
                   'TASK_ARCHIVE_SUB',
                   'WORKLOG_READ',
                   'WORKLOG_START_STOP',
                   'WORKLOG_UPDATE',
                   'WORKLOG_ARCHIVE',
                   'TASK_TEAM_SET_USER',
                   'TASK_TEAM_ARCHIVE',
                   'MEDIA_CREATE',
                   'MEDIA_READ',
                   'MEDIA_UPDATE',
                   'MEDIA_ARCHIVE',
                   'PROJECT_TEAM_ADD_COMPANY',
                   'PROJECT_TEAM_INVITE_COMPANY',
                   'PROJECT_TEAM_ARCHIVE_COMPANY',
                   'PROJECT_TEAM_CHANGE_NON_ADMIN_ROLE',
                   'PROJECT_TEAM_ADD_USER',
                   'PROJECT_TEAM_ARCHIVE_USER',
                   'TASK_REVIEW',
                   'TASK_CONTRACTOR_REVIEW',
                   'PROJECT_ROLES_ASSIGN'
                   ]
           );

SELECT customRoleHasPermissions(
               'PROJECT_MANAGER',
               ARRAY [
                   'PROJECT_READ_LIST',
                   'PROJECT_READ_DETAILS',
                   'LOCATION_READ',
                   'LOCATION_CREATE',
                   'TASK_READ_LIST',
                   'TASK_READ_DETAILS',
                   'TASK_CREATE_UPDATE_SUB',
                   'TASK_ARCHIVE_SUB',
                   'WORKLOG_READ',
                   'WORKLOG_START_STOP',
                   'WORKLOG_UPDATE',
                   'WORKLOG_ARCHIVE',
                   'TASK_TEAM_SET_USER',
                   'TASK_TEAM_ARCHIVE',
                   'MEDIA_CREATE',
                   'MEDIA_READ',
                   'MEDIA_UPDATE',
                   'MEDIA_ARCHIVE',
                   'PROJECT_TEAM_CHANGE_NON_ADMIN_ROLE',
                   'PROJECT_TEAM_ADD_USER',
                   'PROJECT_TEAM_ARCHIVE_USER',
                   'TASK_COMPLETE',
                   'TASK_CONTRACTOR_REVIEW'
                   ]
           );

SELECT customRoleHasPermissions(
               'PROJECT_WORKER',
               ARRAY [
                   'PROJECT_READ_LIST',
                   'PROJECT_READ_DETAILS',
                   'LOCATION_READ',
                   'TASK_READ_LIST',
                   'TASK_READ_DETAILS',
                   'WORKLOG_READ',
                   'WORKLOG_START_STOP',
                   'WORKLOG_UPDATE',
                   'MEDIA_CREATE',
                   'MEDIA_READ',
                   'MEDIA_UPDATE',
                   'MEDIA_ARCHIVE'
                   ]
           );

SELECT customRoleHasPermissions(
               'COMPANY_BOOTSTRAP',
               ARRAY [
                   'COMPANY_TEAM_READ_LIST',
                   'COMPANY_TEAM_READ_DETAILS',
                   'COMPANY_READ_PROFILE',
                   'COMPANY_READ_FINANCIAL_DETAILS',
                   'COMPANY_LIST',
                   'COMPANY_INVITE',
                   'ROLES_UPDATE',
                   'COMPANY_ROLES_ASSIGN',
                   'PROJECT_ROLES_ASSIGN'
                   ]
           );

SELECT customRoleHasPermissions(
               'COMPANY_OWNER',
               ARRAY [
                   'COMPANY_TEAM_READ_LIST',
                   'COMPANY_TEAM_READ_DETAILS',
                   'COMPANY_TEAM_INVITE_USER',
                   'COMPANY_TEAM_UPDATE',
                   'COMPANY_TEAM_MAKE_REVOKE_ADMIN',
                   'COMPANY_TEAM_ARCHIVE',
                   'COMPANY_READ_PROFILE',
                   'COMPANY_READ_FINANCIAL_DETAILS',
                   'COMPANY_LIST',
                   'COMPANY_INVITE',
                   'COMPANY_UPDATE',
                   'COMPANY_ARCHIVE',
                   'COMPANY_PROJECT_CREATE',
                   'COMPANY_CATALOG_EDIT',
                   -- this is temporary. just for testing. have to remove in prod
                   'GLOBAL_CATALOG_EDIT',
                   'COMPANY_PROFILES_VIEW',
                   'COMPANY_PROFILES_EDIT',
                   'ROLES_UPDATE',
                   'COMPANY_ROLES_ASSIGN'
                   ]
           );

SELECT customRoleHasPermissions(
               'COMPANY_ADMIN',
               ARRAY [
                   'COMPANY_TEAM_READ_LIST',
                   'COMPANY_TEAM_READ_DETAILS',
                   'COMPANY_TEAM_INVITE_USER',
                   'COMPANY_TEAM_UPDATE',
                   'COMPANY_TEAM_ARCHIVE',
                   'COMPANY_READ_PROFILE',
                   'COMPANY_READ_FINANCIAL_DETAILS',
                   'COMPANY_LIST',
                   'COMPANY_INVITE',
                   'COMPANY_UPDATE',
                   'COMPANY_PROJECT_CREATE',
                   'COMPANY_PROFILES_VIEW',
                   'COMPANY_PROFILES_EDIT',
                   'COMPANY_ROLES_ASSIGN'
                   ]
           );

SELECT customRoleHasPermissions(
               'COMPANY_WORKER',
               ARRAY [
                   'COMPANY_TEAM_READ_LIST',
                   'COMPANY_TEAM_READ_DETAILS',
                   'COMPANY_READ_PROFILE',
                   'COMPANY_READ_FINANCIAL_DETAILS'
                   ]
           );
