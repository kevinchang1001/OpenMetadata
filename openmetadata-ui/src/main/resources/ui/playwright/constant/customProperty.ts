/*
 *  Copyright 2024 Collate.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import { EntityTypeEndpoint } from '../support/entity/Entity.interface';

export const CustomPropertySupportedEntityList = [
  EntityTypeEndpoint.Database,
  EntityTypeEndpoint.DatabaseSchema,
  EntityTypeEndpoint.Table,
  EntityTypeEndpoint.StoreProcedure,
  EntityTypeEndpoint.Topic,
  EntityTypeEndpoint.Dashboard,
  EntityTypeEndpoint.Pipeline,
  EntityTypeEndpoint.Container,
  EntityTypeEndpoint.MlModel,
  EntityTypeEndpoint.GlossaryTerm,
  EntityTypeEndpoint.SearchIndex,
  EntityTypeEndpoint.DataModel,
  EntityTypeEndpoint.API_COLLECTION,
  EntityTypeEndpoint.API_ENDPOINT,
];

export const ENTITY_REFERENCE_PROPERTIES = [
  'Entity Reference',
  'Entity Reference List',
];

export const CUSTOM_PROPERTIES_ENTITIES = {
  entity_container: {
    name: 'container',
    description: 'This is Container custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'containers',
  },

  entity_dashboard: {
    name: 'dashboard',
    description: 'This is Dashboard custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'dashboards',
  },

  entity_database: {
    name: 'database',
    description: 'This is Database custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'databases',
  },

  entity_databaseSchema: {
    name: 'databaseSchema',
    description: 'This is Database Schema custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'databaseSchemas',
  },

  entity_glossaryTerm: {
    name: 'glossaryTerm',
    description: 'This is Glossary Term custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'glossaryTerm',
  },

  entity_mlmodel: {
    name: 'mlmodel',
    description: 'This is ML Model custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'mlmodels',
  },

  entity_pipeline: {
    name: 'pipeline',
    description: 'This is Pipeline custom property',
    integerValue: '78',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: true,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'pipelines',
  },

  entity_searchIndex: {
    name: 'searchIndex',
    description: 'This is Search Index custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'searchIndexes',
  },

  entity_storedProcedure: {
    name: 'storedProcedure',
    description: 'This is Stored Procedure custom property',
    integerValue: '14',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'storedProcedures',
  },

  entity_table: {
    name: 'table',
    description: 'This is Table custom property',
    integerValue: '45',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'tables',
  },

  entity_topic: {
    name: 'topic',
    description: 'This is Topic custom property',
    integerValue: '23',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'topics',
  },
  entity_apiCollection: {
    name: 'apiCollection',
    description: 'This is API Collection custom property',
    integerValue: '23',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'apiCollections',
  },

  entity_apiEndpoint: {
    name: 'apiEndpoint',
    description: 'This is API Endpoint custom property',
    integerValue: '23',
    stringValue: 'This is string propery',
    markdownValue: 'This is markdown value',
    enumConfig: {
      values: ['enum1', 'enum2', 'enum3'],
      multiSelect: false,
    },
    dateFormatConfig: 'yyyy-MM-dd',
    dateTimeFormatConfig: 'yyyy-MM-dd HH:mm:ss',
    timeFormatConfig: 'HH:mm:ss',
    entityReferenceConfig: ['User', 'Team'],
    entityObj: {},
    entityApiType: 'apiEndpoints',
  },
};

export const CUSTOM_PROPERTY_INVALID_NAMES = {
  CAPITAL_CASE: 'CapitalCase',
  WITH_UNDERSCORE: 'with_underscore',
  WITH_DOTS: 'with.',
  WITH_SPACE: 'with ',
};

export const CUSTOM_PROPERTY_NAME_VALIDATION_ERROR =
  'Name must start with lower case with no space, underscore, or dots.';
