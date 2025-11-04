/**
 * Get all active users currently checked in at a specific location
 */

const AWS = require('aws-sdk');
const docClient = new AWS.DynamoDB.DocumentClient();

exports.handler = async (event) => {
  console.log('Event:', JSON.stringify(event, null, 2));

  const { locationId } = event.arguments;

  if (!locationId) {
    return {
      statusCode: 400,
      body: JSON.stringify({ error: 'locationId is required' })
    };
  }

  try {
    // Query CheckIns table for active check-ins at this location
    const checkInsParams = {
      TableName: process.env.CHECKIN_TABLE,
      IndexName: 'byLocation',
      KeyConditionExpression: 'locationId = :locationId',
      FilterExpression: 'isActive = :isActive',
      ExpressionAttributeValues: {
        ':locationId': locationId,
        ':isActive': true
      }
    };

    const checkInsResult = await docClient.query(checkInsParams).promise();
    console.log('Active check-ins:', checkInsResult.Items.length);

    // Get unique user IDs
    const userIds = [...new Set(checkInsResult.Items.map(ci => ci.userId))];

    if (userIds.length === 0) {
      return [];
    }

    // Batch get user details
    const batchGetParams = {
      RequestItems: {
        [process.env.USER_TABLE]: {
          Keys: userIds.map(id => ({ id }))
        }
      }
    };

    const usersResult = await docClient.batchGet(batchGetParams).promise();
    const users = usersResult.Responses[process.env.USER_TABLE];

    console.log('Active users found:', users.length);
    return users;

  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
