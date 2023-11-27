DO
$$
    DECLARE
        maxPermissionId int;
    BEGIN
        select max(id) into maxPermissionId from permission;

        INSERT INTO permission (id, name, description, deprecated, project_permission, admin_permission)
        VALUES (maxPermissionId + 1, 'CONTRACT_SELF_ACCEPT_OFFER',
                'Allows contractor to self accept an offer', false, true, false)
        ;
    END
$$;

select grantpermissionstorole('PROJECT_OWNER', ARRAY [ 'CONTRACT_SELF_ACCEPT_OFFER']);