SELECT roleHasPermissions(
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
                   'PROJECT_ROLES_ASSIGN',
                   'VIEW_SELF_NOTIFICATION_SETTINGS',
                   'VIEW_GLOBAL_NOTIFICATION_SETTINGS',
                   'UPDATE_SELF_NOTIFICATION_SETTINGS',
                   'UPDATE_GLOBAL_NOTIFICATION_SETTINGS'
                   ]
           );
