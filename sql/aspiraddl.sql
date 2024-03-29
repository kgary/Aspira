-- ============================

-- This file was created using Derby's dblook utility.
-- Timestamp: 2013-04-11 21:20:21.696
-- Specified schema is: APP
-- appendLogs: false

-- ----------------------------------------------
-- DDL Statements for sequences
-- ----------------------------------------------

CREATE SEQUENCE "APP"."ASPIRA_SEQ"
    AS INTEGER
    START WITH -2147483648
    INCREMENT BY 1
    MAXVALUE 9999999
    MINVALUE -2147483648
    CYCLE
;

-- ----------------------------------------------
-- DDL Statements for tables
-- ----------------------------------------------

CREATE TABLE "APP"."PARTICLEREADING" ("DEVICEID" VARCHAR(16) NOT NULL, "PATIENTID" VARCHAR(16) NOT NULL, "READINGTIME" TIMESTAMP NOT NULL, "SMALLPARTICLE" INTEGER NOT NULL, "LARGEPARTICLE" INTEGER, "GROUPID" INTEGER);

CREATE TABLE "APP"."PATIENT" ("PATIENTID" VARCHAR(16) NOT NULL, "SEX" CHAR(1), "PATIENTNOTES" VARCHAR(4096), "BESTVALUETYPE" VARCHAR(16), "BESTVALUETARGET" INTEGER, "RATEHIGH" INTEGER, "RATELOW" INTEGER, "VALUEHIGH" INTEGER, "VALUELOW" INTEGER);

CREATE TABLE "APP"."AQMONITOR" ("SERIALID" VARCHAR(64) NOT NULL, "VENDOR" VARCHAR(64) NOT NULL, "MODEL" VARCHAR(64) NOT NULL, "DESCRIPTION" VARCHAR(1024), "PATIENTID" VARCHAR(16));

CREATE TABLE "APP"."SPIROMETER" ("SERIALID" VARCHAR(64) NOT NULL, "VENDOR" VARCHAR(64) NOT NULL, "MODEL" VARCHAR(64) NOT NULL, "DESCRIPTION" VARCHAR(1024), "PATIENTID" VARCHAR(16));

CREATE TABLE "APP"."CLINICIAN" ("CLINICIANID" VARCHAR(16) NOT NULL);

CREATE TABLE "APP"."SPIROMETERREADING" ("DEVICEID" VARCHAR(16) NOT NULL, "PATIENTID" VARCHAR(16) NOT NULL, "READINGTIME" TIMESTAMP NOT NULL, "MANUAL" BOOLEAN DEFAULT false, "PEFVALUE" INTEGER NOT NULL, "FEV1VALUE" REAL NOT NULL, "ERROR" INTEGER DEFAULT 0, "BESTVALUE" INTEGER DEFAULT 0, "GROUPID" INTEGER, "MEASUREID" INTEGER DEFAULT 0, "SYMPTOMS" BOOLEAN DEFAULT NULL);

CREATE TABLE "APP"."PATIENT_CLINICIAN" ("PATIENTID" VARCHAR(16) NOT NULL, "CLINICIANID" VARCHAR(16) NOT NULL);

CREATE TABLE "APP"."UIEVENT" ("DEVICEID" VARCHAR(16) NOT NULL, "PATIENTID" VARCHAR(16) NOT NULL, "VERSION" VARCHAR(16) NOT NULL, "EVENTTYPE" VARCHAR(64) NOT NULL, "EVENTTARGET" VARCHAR(64) NOT NULL, "EVENTVALUE" VARCHAR(1024) NOT NULL, "EVENTTIME" TIMESTAMP NOT NULL, "GROUPID" INTEGER NOT NULL);

CREATE TABLE "APP"."SERVERPUSHEVENTLOG" ("EVENTTIME" TIMESTAMP NOT NULL, RESPONSECODE INTEGER NOT NULL, OBJECTTYPE INTEGER, MESSAGE VARCHAR(1024));

-- ----------------------------------------------
-- DDL Statements for keys
-- ----------------------------------------------

-- primary/unique
ALTER TABLE "APP"."SPIROMETERREADING" ADD CONSTRAINT "SQL130411000611060" PRIMARY KEY ("DEVICEID", "PATIENTID", "READINGTIME");

ALTER TABLE "APP"."AQMONITOR" ADD CONSTRAINT "AQM_PK" PRIMARY KEY ("SERIALID");

ALTER TABLE "APP"."CLINICIAN" ADD CONSTRAINT "C_PK" PRIMARY KEY ("CLINICIANID");

ALTER TABLE "APP"."PARTICLEREADING" ADD CONSTRAINT "SQL130411000134490" PRIMARY KEY ("DEVICEID", "PATIENTID", "READINGTIME");

ALTER TABLE "APP"."SPIROMETER" ADD CONSTRAINT "SP_PK" PRIMARY KEY ("SERIALID");

ALTER TABLE "APP"."PATIENT_CLINICIAN" ADD CONSTRAINT "SQL130404223910090" PRIMARY KEY ("PATIENTID", "CLINICIANID");

ALTER TABLE "APP"."PATIENT" ADD CONSTRAINT "SQL130404222951130" PRIMARY KEY ("PATIENTID");

alter table "APP"."UIEVENT" add constraint PK primary key (deviceid,patientid,version,eventtype,eventtarget,eventvalue,eventtime);

-- foreign
ALTER TABLE "APP"."PATIENT_CLINICIAN" ADD CONSTRAINT "PATIENT_FK" FOREIGN KEY ("PATIENTID") REFERENCES "APP"."PATIENT" ("PATIENTID") ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE "APP"."PATIENT_CLINICIAN" ADD CONSTRAINT "CLINICIAN_FK" FOREIGN KEY ("CLINICIANID") REFERENCES "APP"."CLINICIAN" ("CLINICIANID") ON DELETE NO ACTION ON UPDATE NO ACTION;

