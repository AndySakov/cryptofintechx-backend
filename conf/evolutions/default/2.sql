-- cryptofintechx schema

-- !Ups

CREATE TABLE IF NOT EXISTS cryptofintechx.users
(
    id serial NOT NULL,
    user_id uuid NOT NULL,
    full_name varchar(100) NOT NULL,
    email varchar(100) NOT NULL,
    password varchar(100) NOT NULL,
    phone_number varchar(20),
    country varchar(100) NOT NULL,
    category varchar(7) NOT NULL,
    avatar_url varchar,
    dob date,
    created_at timestamp without time zone DEFAULT now(),
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_uk UNIQUE (user_id, email)
);

-- !Downs

DROP TABLE cryptofintechx.users;