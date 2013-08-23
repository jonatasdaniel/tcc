--  createuser -P dicom
--  createdb -O dicom -E UTF8 dicomdb
--  


CREATE TABLE dcmseries (
    seriesinstanceuid character varying(64) PRIMARY KEY NOT NULL,
    studyinstanceuid character varying(64),
    modality character varying(16),
    sopclassuid character varying(64),
    patientid character varying(64),
    patientname character varying(64),
    patientbirthdate date,
    patientsex character varying(16),
    studydescription character varying(64),
    studydate date,
    bodypartexamined character varying(64),
    studytime time(0) without time zone,
    manufacturer character varying(64),
    manufacturermodel character varying(64),
    institution character varying(64),
    institutionaddress character varying(1024),
    stationname character varying(16),
    seriesnumber character varying(10),
    acquisitionnumber character varying(10)
);
COMMENT ON COLUMN dcmseries.seriesinstanceuid IS '(0020,000E) one series of a study';
COMMENT ON COLUMN dcmseries.studyinstanceuid IS '(0020,000D) one study contains one or more series';
COMMENT ON COLUMN dcmseries.modality IS '(0008,0060) modality';
COMMENT ON COLUMN dcmseries.sopclassuid IS '(0008,0016) the storage service class (e.g. ct image storage) for mapping';
COMMENT ON COLUMN dcmseries.patientid IS '(0010,0020) Patient ID';
COMMENT ON COLUMN dcmseries.patientname IS '(0010,0010) Patient´s Name';
COMMENT ON COLUMN dcmseries.patientbirthdate IS '(0010,0030) Patient`s Birth Date';
COMMENT ON COLUMN dcmseries.patientsex IS '(0010,0040) Patient´s Sex';
COMMENT ON COLUMN dcmseries.studydescription IS '(0008,1030) Study Description';
COMMENT ON COLUMN dcmseries.studydate IS '(0008,0020) Study Date';
COMMENT ON COLUMN dcmseries.bodypartexamined IS '(0018,0015) body part examined';
COMMENT ON COLUMN dcmseries.studytime IS '(0008,0030) Study Time';
COMMENT ON COLUMN dcmseries.manufacturer IS '(0008,0070) manufacturer';
COMMENT ON COLUMN dcmseries.manufacturermodel IS '(0008,1090) manufacturer´s model name';
COMMENT ON COLUMN dcmseries.institution IS '(0008,0080) institution name';
COMMENT ON COLUMN dcmseries.institutionaddress IS '(0008,0081) institution address';
COMMENT ON COLUMN dcmseries.stationname IS '(0008,1010) station name';

CREATE TABLE dcmobject (
    id serial PRIMARY KEY NOT NULL,
    sopinstanceuid character varying(64) UNIQUE NOT NULL,
    seriesuid character varying(64) NOT NULL,
    entrydate timestamp(0) without time zone,
    checksumimage character varying(64),
    dbimporterversion character varying(10)
);

ALTER TABLE ONLY dcmobject
    ADD CONSTRAINT dcmobject_fk FOREIGN KEY (seriesuid) REFERENCES dcmseries(seriesinstanceuid) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE;

COMMENT ON COLUMN dcmobject.id IS 'db-generated id';
COMMENT ON COLUMN dcmobject.sopinstanceuid IS '(0008,0018) one dicom object';
COMMENT ON COLUMN dcmobject.entrydate IS 'date of db insert';
COMMENT ON COLUMN dcmobject.checksumimage IS 'md5 over image data';
COMMENT ON COLUMN dcmobject.dbimporterversion IS 'version number of the importing software';



CREATE TABLE dcmtag (
    dcmobject_id INTEGER NOT NULL,
    tag character varying(9) NOT NULL,
    vr character varying(2) NOT NULL,
    value text,
    itemorder integer NOT NULL,
    parentid integer
);

ALTER TABLE ONLY dcmtag
    ADD CONSTRAINT dcmtag_fk FOREIGN KEY (dcmobject_id) REFERENCES dcmobject(id) ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE;

COMMENT ON COLUMN dcmtag.dcmobject_id IS 'foreign key on one dcmobject (id)';
COMMENT ON COLUMN dcmtag.tag IS 'number of the tag';
COMMENT ON COLUMN dcmtag.vr IS 'value representation';
COMMENT ON COLUMN dcmtag.value IS 'value of the dicom tag';
COMMENT ON COLUMN dcmtag.itemorder IS 'running id per image';
COMMENT ON COLUMN dcmtag.parentid IS 'parent id if exists';


CREATE INDEX "Index_bodyPartExamined" ON dcmseries USING btree (bodypartexamined);
CREATE INDEX "Index_stationName" ON dcmseries USING btree (stationname);
CREATE INDEX "Index_studyDate" ON dcmseries USING btree (studydate);
CREATE INDEX "Index_studyInstanceUid" ON dcmseries USING btree (studyinstanceuid);
CREATE INDEX "Index_institution" ON dcmseries USING btree (institution);
CREATE INDEX "Index_manufacturerModel" ON dcmseries USING btree (manufacturermodel);
CREATE INDEX "Index_modality" ON dcmseries USING btree (modality);
CREATE INDEX "Index_patientBirthdate" ON dcmseries USING btree (patientbirthdate);
CREATE INDEX "Index_patientSex" ON dcmseries USING btree (patientsex);
CREATE INDEX "Index_seriesInstanceUid" ON dcmseries USING btree (seriesinstanceuid);

CREATE INDEX "Index_seriesUid" ON dcmobject USING btree (seriesuid);
CREATE INDEX "Index_sopInstanceUid" ON dcmobject USING btree (sopinstanceuid);

CREATE INDEX "Index_Id" ON dcmtag USING btree (dcmobject_id);
CREATE INDEX "Index_tag" ON dcmtag USING btree (tag);
