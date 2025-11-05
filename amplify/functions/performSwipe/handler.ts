/// <reference types="node" />
import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import { DynamoDBDocumentClient, PutCommand, QueryCommand } from '@aws-sdk/lib-dynamodb';
import { v4 as uuidv4 } from 'uuid';

const client = new DynamoDBClient({});
const docClient = DynamoDBDocumentClient.from(client);

export const handler = async (event: any) => {
  console.log('Event:', JSON.stringify(event, null, 2));

  const { userId, targetUserId, locationId, action } = event.arguments;

  if (!userId || !targetUserId || !locationId || !action) {
    throw new Error('userId, targetUserId, locationId, and action are required');
  }

  try {
    const swipeTableName = process.env.SWIPE_TABLE_NAME;
    const matchTableName = process.env.MATCH_TABLE_NAME;

    if (!swipeTableName || !matchTableName) {
      throw new Error('Table names not configured');
    }

    // Record the swipe
    const swipeId = uuidv4();
    const timestamp = new Date().toISOString();

    await docClient.send(new PutCommand({
      TableName: swipeTableName,
      Item: {
        id: swipeId,
        userId,
        targetUserId,
        locationId,
        action,
        timestamp,
        createdAt: timestamp,
        updatedAt: timestamp
      }
    }));

    let matchCreated = false;
    let matchId: string | null = null;

    // If this is a LIKE, check for mutual match
    if (action === 'LIKE') {
      // Check if target user also liked this user
      const reverseSwipeResponse = await docClient.send(new QueryCommand({
        TableName: swipeTableName,
        IndexName: 'byUserAndTarget',
        KeyConditionExpression: 'userId = :targetUserId',
        FilterExpression: 'targetUserId = :userId AND #action = :likeAction',
        ExpressionAttributeNames: {
          '#action': 'action'
        },
        ExpressionAttributeValues: {
          ':targetUserId': targetUserId,
          ':userId': userId,
          ':likeAction': 'LIKE'
        }
      }));

      // If mutual like exists, create a match
      if (reverseSwipeResponse.Items && reverseSwipeResponse.Items.length > 0) {
        matchId = uuidv4();
        const matchTime = new Date().toISOString();

        await docClient.send(new PutCommand({
          TableName: matchTableName,
          Item: {
            id: matchId,
            user1Id: userId < targetUserId ? userId : targetUserId,
            user2Id: userId < targetUserId ? targetUserId : userId,
            locationId,
            matchTime,
            messageSent: false,
            createdAt: matchTime,
            updatedAt: matchTime
          }
        }));

        matchCreated = true;

        // TODO: Send push notifications to both users
        console.log(`Match created: ${matchId} between ${userId} and ${targetUserId}`);
      }
    }

    return {
      swipeId,
      matchCreated,
      matchId,
      message: matchCreated
        ? 'Swipe recorded and match created!'
        : 'Swipe recorded successfully'
    };
  } catch (error) {
    console.error('Error:', error);
    throw new Error(`Failed to perform swipe: ${error instanceof Error ? error.message : 'Unknown error'}`);
  }
};
