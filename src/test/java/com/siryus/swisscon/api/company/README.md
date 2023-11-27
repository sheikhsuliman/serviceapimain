# Integration tests for Company API
The tests in this directory will cover all operations
in the CompanyController in a setup that is modeled after
actual workflows.

The call paths below are understood to be prefixed by the
base paths for the Companies API, currently `/api/rest/companies`.

The expression `{id}` in the paths is a placeholder for the
company the call pertains to.

The lifecycle of a company is as follows:

##Invitation
Testing steps happy path:
  * pick a company name
  * call `/directory` and expect to _not_ find the name in there
  * call `/invite-company`, expect to find a link in the outgoing e-mail and
    call that
  * call `/directory` again and now expect to find the company in there

Unhappy paths:
  * call `/invite-company` without logging in, should give 403
  * call `/invite-company` logged in as a user who is not supposed to have
    permission to invite companies, should give 403
    * -> does _not_ work atm, because the call is always authorized against the user's own company,
         and therefore auth always suceeds. I. e., the unhappy path does not exist.
  * call the signup link with a wrong token and expect failure
  * call the correct signup link again after signing up and expect failure

(All done in `InviteCompanyAndSignupIT`)

##Update information
Testing steps, happy path:
  * `POST` to `/{id}/profile` setting a new name
  * `GET` from `/directory` and expect the new name to show
  * `POST` to `/{id}/registration-details` and update registration details
  * `GET` from `/{id}/contact-details` and remember result
  * `POST` to `/{id}/contact-details` and add / change contact information,
    e. g. phone number
  * `GET` again and compare with previous result, expect exactly the change
    made in between

Unhappy paths:
  * expect a 403 for doing all `POST` calls
    * without logging in
    (* with all user roles that are not supposed to have permission for that operation)

(All done in `CompanyIT`)

##Setup and remove team members
Happy path:
  * `GET` from  `/{id}/details-team` and note team size
  * `POST` to `/auth/invite`
  * `GET`  from  `/{id}/details-team` again, expect team size to have changed
     and to find the new user in the team
  * `POST` to `/{id}/remove-user` with the new user id
  * `GET`  from  `/{id}/details-team` again, expect team size to be back to 
     the original size and to _not_ find the new user in the team anymore.

Unhappy paths:
  * try to remove user without logging in, expect 403
  * try to retrieve details without logging in, expect 403
  * trying to invite a user without proper authorizatrion should be tested in the user API

(All done in `CompanyDetailsIT`)

##Remove company
`/{id}/remove-company`, expect 403 without login

(Done in `CompanyRemoveIT`)

##Open questions / To-Dos
  * Would it make more sense to make the call inviting a new company include
    a parameter stating the company one is being invited _to_? This would make
    authorization more explicit and consistent, and it would alleviate issues
    with users who might own multiple companies, or users who do not own any
    but should still be authorized to send invitations (say, an admin or system user).
  * we should set up a suite of stereotype users representing the typical roles
    we expect to exist, and run all these tests with each of them, to check
    if all permissions work as expected.
