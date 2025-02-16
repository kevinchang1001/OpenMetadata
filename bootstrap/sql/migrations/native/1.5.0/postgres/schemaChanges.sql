-- Add a new table di_chart_entity
CREATE TABLE IF NOT EXISTS di_chart_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> 'id') STORED NOT NULL,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> 'name') STORED NOT NULL,
    fullyQualifiedName VARCHAR(256) GENERATED ALWAYS AS (json ->> 'fullyQualifiedName') STORED NOT NULL,
    json JSONB NOT NULL,
    updatedAt BIGINT GENERATED ALWAYS AS ((json ->> 'updatedAt')::bigint) STORED NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> 'updatedBy') STORED NOT NULL,
    fqnHash VARCHAR(768) DEFAULT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS ((json ->> 'deleted')::bool) STORED,
    UNIQUE(name)
);

UPDATE kpi_entity
SET json = jsonb_set(
        json,
        '{targetValue}',
        to_jsonb((json->'targetDefinition'->0->>'value')::numeric * 100)
       ) #- '{targetDefinition}'
WHERE json->>'metricType' = 'PERCENTAGE';

UPDATE kpi_entity
SET json = jsonb_set(
        json,
        '{targetValue}',
        to_jsonb((json->'targetDefinition'->0->>'value')::numeric)
       ) #- '{targetDefinition}'
WHERE json->>'metricType' = 'NUMBER';

UPDATE dbservice_entity
SET json = JSONB_SET(
  JSONB_SET(
    json,
    '{connection,config,configSource}',
    JSONB_BUILD_OBJECT('connection', json->'connection'->'config'->'metastoreConnection')
  ),
  '{connection,config,configSource,appName}',
  json->'connection'->'config'->'appName'
) #- '{connection,config,metastoreConnection}' #- '{connection,config,appName}'
WHERE serviceType = 'DeltaLake';


-- Allow all bots to update the ingestion pipeline status
UPDATE policy_entity
SET json = jsonb_set(
  json,
  '{rules}',
  (json->'rules')::jsonb || to_jsonb(ARRAY[
    jsonb_build_object(
      'name', 'BotRule-IngestionPipeline',
      'description', 'A bot can Edit ingestion pipelines to pass the status',
      'resources', jsonb_build_array('ingestionPipeline'),
      'operations', jsonb_build_array('ViewAll', 'EditIngestionPipelineStatus'),
      'effect', 'allow'
    )
  ]),
  true
)
WHERE json->>'name' = 'DefaultBotPolicy';

-- create API service entity
CREATE TABLE IF NOT EXISTS api_service_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> 'id') STORED NOT NULL,
    nameHash VARCHAR(256)  NOT NULL,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> 'name') STORED NOT NULL,
    serviceType VARCHAR(256) GENERATED ALWAYS AS (json ->> 'serviceType') STORED NOT NULL,
    json JSONB NOT NULL,
    updatedAt BIGINT GENERATED ALWAYS AS ((json ->> 'updatedAt')::bigint) STORED NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> 'updatedBy') STORED NOT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS ((json ->> 'deleted')::boolean) STORED,
    PRIMARY KEY (id),
    UNIQUE (nameHash)
);

-- create API collection entity
CREATE TABLE IF NOT EXISTS api_collection_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> 'id') STORED NOT NULL,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> 'name') STORED NOT NULL,
    fqnHash VARCHAR(256) NOT NULL,
    json JSONB NOT NULL,
    updatedAt BIGINT GENERATED ALWAYS AS ((json ->> 'updatedAt')::bigint) STORED NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> 'updatedBy') STORED NOT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS ((json ->> 'deleted')::boolean) STORED,
    PRIMARY KEY (id),
    UNIQUE (fqnHash)
);

-- create API Endpoint entity
CREATE TABLE IF NOT EXISTS api_endpoint_entity (
    id VARCHAR(36) GENERATED ALWAYS AS (json ->> 'id') STORED NOT NULL,
    name VARCHAR(256) GENERATED ALWAYS AS (json ->> 'name') STORED NOT NULL,
    fqnHash VARCHAR(256) NOT NULL,
    json JSONB NOT NULL,
    updatedAt BIGINT GENERATED ALWAYS AS ((json ->> 'updatedAt')::bigint) STORED NOT NULL,
    updatedBy VARCHAR(256) GENERATED ALWAYS AS (json ->> 'updatedBy') STORED NOT NULL,
    deleted BOOLEAN GENERATED ALWAYS AS ((json ->> 'deleted')::boolean) STORED,
    PRIMARY KEY (id),
    UNIQUE (fqnHash)
);


-- Clean dangling workflows not removed after test connection
truncate automations_workflow;

-- Remove date, dateTime, time from type_entity, as they are no more om-field-types, instead we have date-cp, time-cp, dateTime-cp as om-field-types
DELETE FROM type_entity
WHERE name IN ('date', 'dateTime', 'time');

-- Update BigQuery,Bigtable & Datalake model for gcpCredentials to move `gcpConfig` value to `gcpConfig.path`
UPDATE dbservice_entity
SET json = jsonb_set(
  json #-'{connection,config,credentials,gcpConfig}',
  '{connection,config,credentials,gcpConfig}',
  jsonb_build_object('path', json#>'{connection,config,credentials,gcpConfig}')
)
WHERE serviceType IN ('BigQuery', 'BigTable') and json#>>'{connection,config,credentials,gcpConfig}' is not null 
and json#>>'{connection,config,credentials,gcpConfig,type}' is null 
and json#>>'{connection,config,credentials,gcpConfig,externalType}' is null 
and json#>>'{connection,config,credentials,gcpConfig,path}' is null;

UPDATE dbservice_entity
SET json = jsonb_set(
  json #-'{connection,config,configSource,securityConfig,gcpConfig}',
  '{connection,config,configSource,securityConfig,gcpConfig}',
  jsonb_build_object('path', json#>'{connection,config,configSource,securityConfig,gcpConfig}')
)
WHERE serviceType IN ('Datalake') and json#>>'{connection,config,configSource,securityConfig,gcpConfig}' is not null 
and json#>>'{connection,config,configSource,securityConfig,gcpConfig,type}' is null 
and json#>>'{connection,config,configSource,securityConfig,gcpConfig,externalType}' is null 
and json#>>'{connection,config,configSource,securityConfig,gcpConfig,path}' is null;


-- Update Powerbi model for pbitFilesSource to move `gcpConfig` value to `gcpConfig.path`

UPDATE dashboard_service_entity
SET json = jsonb_set(
  json #-'{connection,config,pbitFilesSource,securityConfig,gcpConfig}',
  '{connection,config,pbitFilesSource,securityConfig,gcpConfig}',
  jsonb_build_object('path', json#>'{connection,config,pbitFilesSource,securityConfig,gcpConfig}')
)
WHERE serviceType IN ('PowerBI') and 
json#>>'{connection,config,pbitFilesSource,securityConfig,gcpConfig}' is not null 
and json#>>'{connection,config,pbitFilesSource,securityConfig,gcpConfig,type}' is null 
and json#>>'{connection,config,pbitFilesSource,securityConfig,gcpConfig,externalType}' is null 
and json#>>'{connection,config,pbitFilesSource,securityConfig,gcpConfig,path}' is null;

UPDATE storage_service_entity
SET json = jsonb_set(
  json #-'{connection,config,credentials,gcpConfig}',
  '{connection,config,credentials,gcpConfig}',
  jsonb_build_object('path', json#>'{connection,config,credentials,gcpConfig}')
) where serviceType = 'GCS' and
json#>>'{connection,config,credentials,gcpConfig}' is not null 
and json#>>'{connection,config,credentials,gcpConfig,type}' is null 
and json#>>'{connection,config,credentials,gcpConfig,externalType}' is null 
and json#>>'{connection,config,credentials,gcpConfig,path}' is null;

UPDATE ingestion_pipeline_entity 
SET json = jsonb_set(
  json::jsonb #- '{sourceConfig,config,dbtConfigSource,dbtSecurityConfig,gcpConfig}'::text[],
  '{sourceConfig,config,dbtConfigSource,dbtSecurityConfig,gcpConfig}',
  jsonb_build_object('path', json#>'{sourceConfig,config,dbtConfigSource,dbtSecurityConfig,gcpConfig}')
)  
 WHERE json#>>'{sourceConfig,config,type}' = 'DBT' 
  AND json#>>'{sourceConfig,config,dbtConfigSource,dbtSecurityConfig,gcpConfig}' IS NOT NULL 
  AND json#>>'{sourceConfig,config,dbtConfigSource,dbtSecurityConfig,gcpConfig,type}' IS NULL 
  AND json#>>'{sourceConfig,config,dbtConfigSource,dbtSecurityConfig,gcpConfig,externalType}' IS NULL 
  AND json#>>'{sourceConfig,config,dbtConfigSource,dbtSecurityConfig,gcpConfig,path}' IS NULL;

-- Update Owner Field to Owners
DELETE from event_subscription_entity where name = 'ActivityFeedAlert';

-- Update thread_entity to move previousOwner and updatedOwner to array
UPDATE thread_entity
SET json = jsonb_set(
    json,
    '{feedInfo,entitySpecificInfo,previousOwner}',
    to_jsonb(ARRAY[json->'feedInfo'->'entitySpecificInfo'->'previousOwner'])
)
WHERE jsonb_path_exists(json, '$.feedInfo.entitySpecificInfo.previousOwner')
  AND jsonb_path_query_first(json, '$.feedInfo.entitySpecificInfo.previousOwner ? (@ != null)') IS NOT null
  AND jsonb_typeof(json->'feedInfo'->'entitySpecificInfo'->'updatedOwner') <> 'array';

UPDATE thread_entity
SET json = jsonb_set(
    json,
    '{feedInfo,entitySpecificInfo,updatedOwner}',
    to_jsonb(ARRAY[json->'feedInfo'->'entitySpecificInfo'->'updatedOwner'])
)
WHERE jsonb_path_exists(json, '$.feedInfo.entitySpecificInfo.updatedOwner')
  AND jsonb_path_query_first(json, '$.feedInfo.entitySpecificInfo.updatedOwner ? (@ != null)') IS NOT null
  AND jsonb_typeof(json->'feedInfo'->'entitySpecificInfo'->'updatedOwner') <> 'array';

-- Update entity_extension to move owner to array
UPDATE entity_extension
SET json = jsonb_set(
    json,
    '{owner}',
    to_jsonb(ARRAY[jsonb_path_query_first(json, '$.owner')])
)
WHERE jsonb_path_exists(json, '$.owner')
  AND jsonb_path_query_first(json, '$.owner ? (@ != null)') IS NOT null
  AND jsonb_typeof(json->'owner') <> 'array';
