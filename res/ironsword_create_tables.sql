
create cached table IF NOT EXISTS ROOT_FOLDER(
    ROOT_FOLDER_ID INT PRIMARY KEY,
    ROOT_PATH VARCHAR,
    INCLUDES BLOB,
    EXCLUDES BLOB,
    RF_SCAN_DATE TIMESTAMP
);

create cached table IF NOT EXISTS PROJECT(
    PROJECT_ID INT,
    PROJECT_NAME VARCHAR,
    PROJECT_DESC VARCHAR,
    MODIFIED TIMESTAMP,
    PRIMARY KEY (PROJECT_ID)
);

create cached table IF NOT EXISTS PROJECT_FOLDERS(
    PROJECT_ID INT not null,
    ROOT_FOLDER_ID INT not null,
    PRIMARY KEY (PROJECT_ID, ROOT_FOLDER_ID),
    FOREIGN KEY(PROJECT_ID) REFERENCES PROJECT(PROJECT_ID) ON DELETE CASCADE,
    FOREIGN KEY(ROOT_FOLDER_ID) REFERENCES ROOT_FOLDER(ROOT_FOLDER_ID) ON DELETE CASCADE
);

-- store folders 
CREATE cached TABLE IF NOT EXISTS FILE_TREE(
    FILE_TREE_ID      INT,
    FT_NAME    VARCHAR,
    FT_UPPER_NAME VARCHAR AS UPPER(FT_NAME),
    PARENT_ID   INT,
    ROOT_FOLDER_ID INT,
    PATH    VARCHAR,
    FT_UPPER_PATH VARCHAR AS UPPER(PATH),
    DIR     BOOLEAN,
    NUM_FILE       INT,
    FT_CHECK_FLAG       INT,
    MODIFIED    TIMESTAMP,
    PRIMARY KEY (FILE_TREE_ID),
    FOREIGN KEY(PARENT_ID) REFERENCES FILE_TREE(FILE_TREE_ID) ON DELETE CASCADE,
    FOREIGN KEY(ROOT_FOLDER_ID) REFERENCES ROOT_FOLDER(ROOT_FOLDER_ID) ON DELETE CASCADE
);
create cached table if not exists src_file (
    SRC_FILE_ID   INT, 
    SF_NAME    VARCHAR,
    SF_UPPER_NAME VARCHAR AS UPPER(SF_NAME),
    PACKAGE   VARCHAR,
    FILE_TREE_ID  INT,
    ROOT_FOLDER_ID INT,
    SRC_TYPE    CHAR(10),
    GRAM_DATA   BLOB,
    SF_CHECK_FLAG INT,
    MODIFIED TIMESTAMP,
    SF_LAST_MODIF TIMESTAMP,
    PRIMARY KEY (SRC_FILE_ID),
    CONSTRAINT FK_SF_ROOT_FOLDER_ID FOREIGN KEY(ROOT_FOLDER_ID) REFERENCES ROOT_FOLDER(ROOT_FOLDER_ID) ON DELETE CASCADE,
    FOREIGN KEY(FILE_TREE_ID) REFERENCES FILE_TREE(FILE_TREE_ID) ON DELETE CASCADE
);
create table if not exists src_package(
    src_package_id INT,
    PROJECT_ID INT, 
    sp_name VARCHAR,
    PRIMARY KEY (src_package_id),
    FOREIGN KEY (PROJECT_ID) REFERENCES PROJECT(PROJECT_ID) ON DELETE CASCADE
);
create table if not exists src_file_error(
    SRC_FILE_ID   INT,
    PRIMARY KEY (SRC_FILE_ID),
    FOREIGN KEY (SRC_FILE_ID) REFERENCES src_file(SRC_FILE_ID) ON DELETE CASCADE
);

create table if not exists lang_reference(
    lang_reference_id   int,
    SRC_FILE_ID         int,
    lr_name             VARCHAR,
    lr_qname            VARCHAR,
    lr_parent           INT,
    lr_desc             VARCHAR,
    lr_type             int,
    PRIMARY KEY (lang_reference_id),
    FOREIGN KEY(SRC_FILE_ID) REFERENCES src_file(SRC_FILE_ID) ON DELETE CASCADE,
    CONSTRAINT FK_LR_PARENT FOREIGN KEY(lr_parent) REFERENCES lang_reference(lang_reference_id) ON DELETE CASCADE
);

create table if not exists snap_shot(
    ss_id           INT,
    ss_time         TIMESTAMP,
    ss_desc         VARCHAR,
    PRIMARY KEY (ss_id)
);

create table if not exists snap_shot_file(
    ssf_id              INT,
    ssf_full_path       VARCHAR,
    ssf_LAST_MODIF      TIMESTAMP,
    root_folder_id      INT,
    SRC_FILE_ID         INT,
    ss_id               INT,
    PRIMARY KEY (ssf_id),
    FOREIGN KEY(ss_id) REFERENCES snap_shot(ss_id) ON DELETE CASCADE,
    FOREIGN KEY(root_folder_id) REFERENCES ROOT_FOLDER(root_folder_id) ON DELETE CASCADE
);

create table if not exists users(
    username        VARCHAR(50) not null,
    password        varchar(50) not null,
    enabled         boolean,
    email           varchar,
    tel             varchar(30),
    birthday        date,
    gender          char(1),
    PRIMARY KEY (username)
    
);
create table if not exists authorities(
    username varchar(50) not null,
    authority varchar(50) not null,
    constraint fk_authorities_users foreign key(username) references users(username)
);
create unique index if not exists ix_auth_username on authorities (username,authority);
    
--------------------  temporary tablers ----------------------
create cached local temporary table if not exists SRC_FILE_UPDATED(
    SRC_FILE_ID   INT, 
    PRIMARY KEY (SRC_FILE_ID),
    FOREIGN KEY (SRC_FILE_ID) REFERENCES src_file(SRC_FILE_ID) ON DELETE CASCADE
);

create cached local temporary table if not exists SRC_FILE_DELETE(
    SRC_FILE_ID   INT,
    PRIMARY KEY (SRC_FILE_ID)
);

create cached local temporary table if not exists FILE_TREE_EMPTY(
    FILE_TREE_ID INT,
    PRIMARY KEY (FILE_TREE_ID),
    FOREIGN KEY(FILE_TREE_ID) REFERENCES FILE_TREE(FILE_TREE_ID) ON DELETE CASCADE
);


--alter table SRC_FILE add if not exists CHECK_FLAG INT before MODIFIED;
--alter table FILE_TREE add if not exists CHECK_FLAG INT before MODIFIED;
--alter table ROOT_FOLDER alter column PATH rename to ROOT_PATH;
--alter table FILE_TREE alter column NAME rename to FT_NAME;
--alter table SRC_FILE alter column NAME rename to SF_NAME;
--alter table FILE_TREE alter column CHECK_FLAG rename to FT_CHECK_FLAG;
--alter table SRC_FILE alter column CHECK_FLAG rename to SF_CHECK_FLAG;
alter table ROOT_FOLDER add column if not exists RF_SCAN_DATE TIMESTAMP;
alter table SRC_FILE add column if not exists SF_LAST_MODIF TIMESTAMP;
alter table src_file add column if not exists SF_UPPER_NAME VARCHAR AS UPPER(SF_NAME);
alter table file_tree add column if not exists FT_UPPER_NAME VARCHAR AS UPPER(FT_NAME);
alter table file_tree add column if not exists FT_UPPER_PATH VARCHAR AS UPPER(PATH);
alter table snap_shot add column if not exists ss_desc VARCHAR;
alter table lang_reference add column if not exists lr_qname VARCHAR before lr_desc;
alter table lang_reference add column if not exists lr_parent INT before lr_desc;
alter table lang_reference add CONSTRAINT IF NOT EXISTS FK_LR_PARENT FOREIGN KEY(lr_parent) REFERENCES lang_reference(lang_reference_id) ON DELETE CASCADE;
alter table SRC_FILE add column if not exists ROOT_FOLDER_ID INT;
alter table SRC_FILE add CONSTRAINT IF NOT EXISTS FK_SF_ROOT_FOLDER_ID FOREIGN KEY(ROOT_FOLDER_ID) REFERENCES ROOT_FOLDER(ROOT_FOLDER_ID) ON DELETE CASCADE;

CREATE SEQUENCE IF NOT EXISTS ROOT_FOLDER_SEQ START WITH 1 INCREMENT BY 1 CACHE 1 ;
CREATE SEQUENCE IF NOT EXISTS SRC_FILE_SEQ START WITH 1 INCREMENT BY 1 CACHE 1 ;
CREATE INDEX IF NOT EXISTS SRC_FILE_NAME_IDX ON SRC_FILE (SF_NAME);
CREATE INDEX IF NOT EXISTS SRC_UPP_NAME_IDX ON SRC_FILE (SF_UPPER_NAME);
CREATE INDEX IF NOT EXISTS FT_UPP_NAME_IDX ON FILE_TREE (FT_UPPER_NAME);
CREATE INDEX IF NOT EXISTS FT_UPP_PATH_IDX ON FILE_TREE (FT_UPPER_PATH);

CREATE SEQUENCE IF NOT EXISTS FILE_TREE_SEQ START WITH 1 INCREMENT BY 1 CACHE 1 ;
CREATE INDEX IF NOT EXISTS FILE_TREE_PATH_IDX ON FILE_TREE (PATH);
CREATE SEQUENCE IF NOT EXISTS PROJECT_SEQ START WITH 1 INCREMENT BY 1 CACHE 1 ;
CREATE SEQUENCE IF NOT EXISTS snap_shot_seq START WITH 1 INCREMENT BY 1 CACHE 1 ;
CREATE SEQUENCE IF NOT EXISTS snap_shot_file_seq START WITH 1 INCREMENT BY 1 CACHE 1 ;

