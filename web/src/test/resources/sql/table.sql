CREATE TABLE USERBEAN(USERID BIGINT NOT NULL PRIMARY KEY,PASSWORD VARCHAR(255),USERNAME VARCHAR(255),ADDRESS VARCHAR(255),COMPANY VARCHAR(255),EMAIL VARCHAR(255),PHONE VARCHAR(255),WEBURL VARCHAR(255));
CREATE TABLE USERKEY(SKEYID BIGINT NOT NULL PRIMARY KEY,KEYID VARCHAR(255),KEYNAME VARCHAR(255),KEYSECRET VARCHAR(255),KEYTYPE VARCHAR(255),USERID BIGINT,KEYDESCRIPTION VARCHAR(255),PROJECT VARCHAR(255));
CREATE TABLE GOOGCREDENTIAL(USERID VARCHAR(255) NOT NULL PRIMARY KEY,ACCESSTOKEN VARCHAR(255),EXPIRATIONTIMEMILLIS BIGINT,REFRESHTOKEN VARCHAR(255));
CREATE TABLE OPENJPA_SEQUENCE_TABLE(ID TINYINT NOT NULL PRIMARY KEY,SEQUENCE_VALUE BIGINT);