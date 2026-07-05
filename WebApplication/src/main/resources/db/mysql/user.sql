CREATE DATABASE IF NOT EXISTS petclinic;

ALTER DATABASE petclinic
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

-- WARNING: This is a development-only setup script. In production,
-- database credentials must be configured via environment variables
-- (spring.datasource.petclinic.password, spring.datasource.pii.password)
CREATE USER 'petclinic'@'%' IDENTIFIED BY 'petclinic';
GRANT ALL PRIVILEGES ON petclinic.* TO 'petclinic'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


CREATE DATABASE IF NOT EXISTS pii;
ALTER DATABASE pii
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

GRANT ALL PRIVILEGES ON pii.* TO 'petclinic'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
