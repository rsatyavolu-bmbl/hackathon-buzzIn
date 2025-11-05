import { defineBackend } from '@aws-amplify/backend';
import { data } from './data/resource';
import { getActiveUsersAtLocation } from './functions/getActiveUsersAtLocation/resource';
import { listNearbyLocations } from './functions/listNearbyLocations/resource';
import { performSwipe } from './functions/performSwipe/resource';
import { Policy, PolicyStatement } from 'aws-cdk-lib/aws-iam';

// BuzzIn backend - API Key auth only (no user authentication)
const backend = defineBackend({
  data,
  getActiveUsersAtLocation,
  listNearbyLocations,
  performSwipe,
});

// Grant Lambda functions access to DynamoDB tables
const locationTable = backend.data.resources.tables['Location'];
const userTable = backend.data.resources.tables['User'];
const checkInTable = backend.data.resources.tables['CheckIn'];
const swipeTable = backend.data.resources.tables['Swipe'];
const matchTable = backend.data.resources.tables['Match'];

// Configure listNearbyLocations Lambda
backend.listNearbyLocations.resources.lambda.addEnvironment('LOCATION_TABLE_NAME', locationTable.tableName);
backend.listNearbyLocations.resources.lambda.addToRolePolicy(
  new PolicyStatement({
    actions: ['dynamodb:Scan', 'dynamodb:Query'],
    resources: [locationTable.tableArn],
  })
);

// Configure getActiveUsersAtLocation Lambda
backend.getActiveUsersAtLocation.resources.lambda.addEnvironment('CHECKIN_TABLE_NAME', checkInTable.tableName);
backend.getActiveUsersAtLocation.resources.lambda.addEnvironment('USER_TABLE_NAME', userTable.tableName);
backend.getActiveUsersAtLocation.resources.lambda.addToRolePolicy(
  new PolicyStatement({
    actions: ['dynamodb:Query'],
    resources: [checkInTable.tableArn, checkInTable.tableArn + '/index/*', userTable.tableArn],
  })
);

// Configure performSwipe Lambda
backend.performSwipe.resources.lambda.addEnvironment('SWIPE_TABLE_NAME', swipeTable.tableName);
backend.performSwipe.resources.lambda.addEnvironment('MATCH_TABLE_NAME', matchTable.tableName);
backend.performSwipe.resources.lambda.addToRolePolicy(
  new PolicyStatement({
    actions: ['dynamodb:PutItem', 'dynamodb:Query'],
    resources: [swipeTable.tableArn, swipeTable.tableArn + '/index/*', matchTable.tableArn],
  })
);
