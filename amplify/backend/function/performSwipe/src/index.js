/**
 * Handle swipe action and detect matches
 * Returns SwipeResult { swipe, matched, match }
 */

const AWS = require('aws-sdk');
const docClient = new AWS.DynamoDB.DocumentClient();
const sns = new AWS.SNS();

/**
 * Check if reverse swipe exists (targetUser liked currentUser)
 */
async function checkForReverseSwipe(userId, targetUserId, locationId) {
  const params = {
    TableName: process.env.SWIPE_TABLE,
    IndexName: 'byUser',
    KeyConditionExpression: 'userId = :targetUserId',
    FilterExpression: 'targetUserId = :userId AND locationId = :locationId AND #action = :like',
    ExpressionAttributeNames: {
      '#action': 'action'
    },
    ExpressionAttributeValues: {
      ':targetUserId': targetUserId,
      ':userId': userId,
      ':locationId': locationId,
      ':like': 'LIKE'
    }
  };

  const result = await docClient.query(params).promise();
  return result.Items.length > 0;
}

/**
 * Create a match between two users
 */
async function createMatch(user1Id, user2Id, locationId) {
  const matchId = `${Date.now()}-${user1Id}-${user2Id}`;
  const now = new Date().toISOString();

  const match = {
    id: matchId,
    user1Id: user1Id,
    user2Id: user2Id,
    locationId: locationId,
    matchTime: now,
    messageSent: false,
    createdAt: now,
    updatedAt: now,
    __typename: 'Match'
  };

  await docClient.put({
    TableName: process.env.MATCH_TABLE,
    Item: match
  }).promise();

  return match;
}

/**
 * Get user details
 */
async function getUser(userId) {
  const result = await docClient.get({
    TableName: process.env.USER_TABLE,
    Key: { id: userId }
  }).promise();
  return result.Item;
}

/**
 * Get location details
 */
async function getLocation(locationId) {
  const result = await docClient.get({
    TableName: process.env.LOCATION_TABLE,
    Key: { id: locationId }
  }).promise();
  return result.Item;
}

/**
 * Send push notification via SNS
 */
async function sendMatchNotification(user1, user2, location) {
  try {
    const message = {
      default: `It's a match! You and ${user2.name} both buzzed in at ${location.name}`,
      GCM: JSON.stringify({
        notification: {
          title: "It's a Match!",
          body: `You and ${user2.name} both buzzed in at ${location.name}`,
          sound: 'default'
        },
        data: {
          type: 'MATCH',
          userId: user2.id,
          userName: user2.name,
          locationId: location.id,
          locationName: location.name
        }
      })
    };

    // Send to user1 (assuming endpoint ARN is stored in user record)
    if (user1.deviceEndpointArn) {
      await sns.publish({
        TargetArn: user1.deviceEndpointArn,
        Message: JSON.stringify(message),
        MessageStructure: 'json'
      }).promise();
    }

    // Send to user2
    if (user2.deviceEndpointArn) {
      const message2 = {
        default: `It's a match! You and ${user1.name} both buzzed in at ${location.name}`,
        GCM: JSON.stringify({
          notification: {
            title: "It's a Match!",
            body: `You and ${user1.name} both buzzed in at ${location.name}`,
            sound: 'default'
          },
          data: {
            type: 'MATCH',
            userId: user1.id,
            userName: user1.name,
            locationId: location.id,
            locationName: location.name
          }
        })
      };

      await sns.publish({
        TargetArn: user2.deviceEndpointArn,
        Message: JSON.stringify(message2),
        MessageStructure: 'json'
      }).promise();
    }

    console.log('Match notifications sent successfully');
  } catch (error) {
    console.error('Error sending notification:', error);
    // Don't throw - notification failure shouldn't break the match
  }
}

exports.handler = async (event) => {
  console.log('Event:', JSON.stringify(event, null, 2));

  const { userId, targetUserId, locationId, action } = event.arguments;

  if (!userId || !targetUserId || !locationId || !action) {
    throw new Error('userId, targetUserId, locationId, and action are required');
  }

  if (userId === targetUserId) {
    throw new Error('Cannot swipe on yourself');
  }

  try {
    // Create swipe record
    const swipeId = `${Date.now()}-${userId}-${targetUserId}`;
    const now = new Date().toISOString();

    const swipe = {
      id: swipeId,
      userId: userId,
      targetUserId: targetUserId,
      locationId: locationId,
      action: action,
      timestamp: now,
      createdAt: now,
      updatedAt: now,
      __typename: 'Swipe'
    };

    await docClient.put({
      TableName: process.env.SWIPE_TABLE,
      Item: swipe
    }).promise();

    console.log('Swipe created:', swipeId);

    // Initialize result
    let result = {
      swipe: swipe,
      matched: false,
      match: null
    };

    // Only check for match if action is LIKE
    if (action === 'LIKE') {
      const hasReverseSwipe = await checkForReverseSwipe(userId, targetUserId, locationId);

      if (hasReverseSwipe) {
        console.log('Match detected! Creating match record...');

        // Create match
        const match = await createMatch(userId, targetUserId, locationId);
        result.matched = true;
        result.match = match;

        // Get user and location details for notification
        const [user1, user2, location] = await Promise.all([
          getUser(userId),
          getUser(targetUserId),
          getLocation(locationId)
        ]);

        // Send push notifications
        await sendMatchNotification(user1, user2, location);

        console.log('Match created:', match.id);
      }
    }

    return result;

  } catch (error) {
    console.error('Error in performSwipe:', error);
    throw error;
  }
};
