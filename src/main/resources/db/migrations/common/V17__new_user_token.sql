CREATE TABLE user_token
(
    id                     serial,

    user_id                integer not null references "user" (id),

    token_hash             varchar(60) not null, -- 60 chars will be the size of output of "bcrypt" including salt    

    token_type             varchar(15) check (token_type in ('PASS_LOGIN', 'SMS_LOGIN')),

    expires_on             timestamp not null
);

CREATE INDEX user_token_user_id ON user_token (user_id);

CREATE TABLE user_token_request
(
    id                     serial,

    user_ip                varchar(45),

    last_request           timestamp not null
)