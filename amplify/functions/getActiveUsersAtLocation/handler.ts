/// <reference types="node" />
import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import { DynamoDBDocumentClient, QueryCommand } from '@aws-sdk/lib-dynamodb';

const client = new DynamoDBClient({});
const docClient = DynamoDBDocumentClient.from(client);

export const handler = async (event: any) => {
  console.log('Event:', JSON.stringify(event, null, 2));

  const { locationId } = event.arguments;

  if (!locationId) {
    throw new Error('locationId is required');
  }

  try {
    // Get environment variable for CheckIn table name
    const checkInTableName = process.env.CHECKIN_TABLE_NAME;
    const userTableName = process.env.USER_TABLE_NAME;

    if (!checkInTableName || !userTableName) {
      throw new Error('Table names not configured');
    }

    // Query active check-ins at this location
    const checkInsResponse = await docClient.send(new QueryCommand({
      TableName: checkInTableName,
      IndexName: 'byLocationAndTime',
      KeyConditionExpression: 'locationId = :locationId',
      FilterExpression: 'isActive = :isActive',
      ExpressionAttributeValues: {
        ':locationId': locationId,
        ':isActive': true
      }
    }));

    if (!checkInsResponse.Items || checkInsResponse.Items.length === 0) {
      return [];
    }

    // Get user details for each check-in
    const userIds = checkInsResponse.Items.map((item: any) => item.userId);
    const users = [];

    for (const userId of userIds) {
      const userResponse = await docClient.send(new QueryCommand({
        TableName: userTableName,
        KeyConditionExpression: 'id = :userId',
        ExpressionAttributeValues: {
          ':userId': userId
        }
      }));

      if (userResponse.Items && userResponse.Items.length > 0) {
        users.push(userResponse.Items[0]);
      }
    }

    return users;
  } catch (error) {
    console.error('Error:', error);
    throw new Error(`Failed to get active users: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
};
