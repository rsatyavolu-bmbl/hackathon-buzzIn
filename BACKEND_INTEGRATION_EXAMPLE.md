# Backend Integration Example

## Quick Start: Using Application IDs in Your Backend

This guide shows how to integrate the BuzzIn Device ID system with your AWS backend.

## Current Application ID

Based on the logs, your current test device has:
- **Application ID**: `5fbb5562-77cc-44b6-907b-9506725360f2`
- **Android Device ID**: `00e04c7c7b1268d4`

## AWS DynamoDB Table Setup

### 1. Create DynamoDB Table for Device Context

```bash
aws dynamodb create-table \
    --table-name BuzzInDevices \
    --attribute-definitions \
        AttributeName=applicationId,AttributeType=S \
        AttributeName=userId,AttributeType=S \
    --key-schema \
        AttributeName=applicationId,KeyType=HASH \
    --global-secondary-indexes \
        "[{
            \"IndexName\": \"UserIdIndex\",
            \"KeySchema\": [{\"AttributeName\":\"userId\",\"KeyType\":\"HASH\"}],
            \"Projection\": {\"ProjectionType\":\"ALL\"},
            \"ProvisionedThroughput\": {\"ReadCapacityUnits\":5,\"WriteCapacityUnits\":5}
        }]" \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5
```

### 2. Create Table for Buzz-Ins

```bash
aws dynamodb create-table \
    --table-name BuzzInLocations \
    --attribute-definitions \
        AttributeName=buzzInId,AttributeType=S \
        AttributeName=applicationId,AttributeType=S \
        AttributeName=createdAt,AttributeType=N \
    --key-schema \
        AttributeName=buzzInId,KeyType=HASH \
    --global-secondary-indexes \
        "[{
            \"IndexName\": \"ApplicationIdIndex\",
            \"KeySchema\": [
                {\"AttributeName\":\"applicationId\",\"KeyType\":\"HASH\"},
                {\"AttributeName\":\"createdAt\",\"KeyType\":\"RANGE\"}
            ],
            \"Projection\": {\"ProjectionType\":\"ALL\"},
            \"ProvisionedThroughput\": {\"ReadCapacityUnits\":5,\"WriteCapacityUnits\":5}
        }]" \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5
```

## AWS Lambda Functions

### 1. Device Registration Lambda

```javascript
// lambda/registerDevice.js
const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();

exports.handler = async (event) => {
    console.log('Event:', JSON.stringify(event, null, 2));
    
    const body = JSON.parse(event.body);
    const {
        applicationId,
        androidDeviceId,
        appVersion,
        deviceModel,
        osVersion,
        userId
    } = body;
    
    // Validate required fields
    if (!applicationId || !androidDeviceId) {
        return {
            statusCode: 400,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: false,
                message: 'Missing required fields: applicationId, androidDeviceId'
            })
        };
    }
    
    try {
        // Save device info to DynamoDB
        await dynamodb.put({
            TableName: 'BuzzInDevices',
            Item: {
                applicationId: applicationId,
                androidDeviceId: androidDeviceId,
                userId: userId || 'anonymous',
                appVersion: appVersion,
                deviceModel: deviceModel,
                osVersion: osVersion,
                registeredAt: new Date().toISOString(),
                lastActiveAt: new Date().toISOString(),
                updatedAt: Date.now()
            }
        }).promise();
        
        console.log('Device registered:', applicationId);
        
        return {
            statusCode: 200,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: true,
                deviceId: applicationId,
                message: 'Device registered successfully'
            })
        };
    } catch (error) {
        console.error('Error registering device:', error);
        
        return {
            statusCode: 500,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: false,
                message: 'Failed to register device',
                error: error.message
            })
        };
    }
};
```

### 2. Save Context Lambda

```javascript
// lambda/saveContext.js
const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();

exports.handler = async (event) => {
    console.log('Event:', JSON.stringify(event, null, 2));
    
    // Get headers
    const applicationId = event.headers['X-Application-Id'] || event.headers['x-application-id'];
    const userId = event.headers['X-User-Id'] || event.headers['x-user-id'] || 'anonymous';
    
    if (!applicationId) {
        return {
            statusCode: 400,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: false,
                message: 'Missing X-Application-Id header'
            })
        };
    }
    
    const body = JSON.parse(event.body);
    const { lastLocation, preferences } = body;
    
    try {
        // Update device context
        await dynamodb.update({
            TableName: 'BuzzInDevices',
            Key: {
                applicationId: applicationId
            },
            UpdateExpression: 'SET userId = :userId, lastLocation = :location, preferences = :prefs, lastActiveAt = :timestamp, updatedAt = :now',
            ExpressionAttributeValues: {
                ':userId': userId,
                ':location': lastLocation || null,
                ':prefs': preferences || {},
                ':timestamp': new Date().toISOString(),
                ':now': Date.now()
            }
        }).promise();
        
        console.log('Context saved for:', applicationId);
        
        return {
            statusCode: 200,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: true,
                message: 'Context saved successfully'
            })
        };
    } catch (error) {
        console.error('Error saving context:', error);
        
        return {
            statusCode: 500,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: false,
                message: 'Failed to save context',
                error: error.message
            })
        };
    }
};
```

### 3. Buzz In Lambda

```javascript
// lambda/buzzIn.js
const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();
const { v4: uuidv4 } = require('uuid');

exports.handler = async (event) => {
    console.log('Event:', JSON.stringify(event, null, 2));
    
    const applicationId = event.headers['X-Application-Id'] || event.headers['x-application-id'];
    const sessionId = event.headers['X-Session-Id'] || event.headers['x-session-id'];
    
    if (!applicationId) {
        return {
            statusCode: 400,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: false,
                message: 'Missing X-Application-Id header'
            })
        };
    }
    
    const body = JSON.parse(event.body);
    const { placeId, placeName, location, duration, visibility } = body;
    
    try {
        const buzzInId = uuidv4();
        const createdAt = Date.now();
        const expiresAt = createdAt + (duration * 60 * 1000); // Convert minutes to milliseconds
        
        // Save buzz-in
        await dynamodb.put({
            TableName: 'BuzzInLocations',
            Item: {
                buzzInId: buzzInId,
                applicationId: applicationId,
                sessionId: sessionId,
                placeId: placeId,
                placeName: placeName,
                location: location,
                duration: duration,
                visibility: visibility || 'public',
                createdAt: createdAt,
                expiresAt: expiresAt,
                status: 'active'
            }
        }).promise();
        
        // Query nearby buzz-ins (simplified - in production use geospatial queries)
        const nearbyResult = await dynamodb.scan({
            TableName: 'BuzzInLocations',
            FilterExpression: 'expiresAt > :now AND #status = :status',
            ExpressionAttributeNames: {
                '#status': 'status'
            },
            ExpressionAttributeValues: {
                ':now': Date.now(),
                ':status': 'active'
            }
        }).promise();
        
        console.log('Buzz-in created:', buzzInId);
        
        return {
            statusCode: 200,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: true,
                buzzInId: buzzInId,
                expiresAt: expiresAt,
                nearbyUsers: nearbyResult.Items.length - 1 // Exclude self
            })
        };
    } catch (error) {
        console.error('Error creating buzz-in:', error);
        
        return {
            statusCode: 500,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                success: false,
                message: 'Failed to create buzz-in',
                error: error.message
            })
        };
    }
};
```

## API Gateway Setup

### 1. Create REST API

```bash
aws apigateway create-rest-api \
    --name BuzzInAPI \
    --description "BuzzIn Backend API" \
    --endpoint-configuration types=REGIONAL
```

### 2. Create Resources and Methods

```bash
# Get API ID
API_ID=$(aws apigateway get-rest-apis --query "items[?name=='BuzzInAPI'].id" --output text)

# Get root resource ID
ROOT_ID=$(aws apigateway get-resources --rest-api-id $API_ID --query "items[?path=='/'].id" --output text)

# Create /devices resource
DEVICES_ID=$(aws apigateway create-resource \
    --rest-api-id $API_ID \
    --parent-id $ROOT_ID \
    --path-part devices \
    --query 'id' --output text)

# Create /devices/register resource
REGISTER_ID=$(aws apigateway create-resource \
    --rest-api-id $API_ID \
    --parent-id $DEVICES_ID \
    --path-part register \
    --query 'id' --output text)

# Create POST method for /devices/register
aws apigateway put-method \
    --rest-api-id $API_ID \
    --resource-id $REGISTER_ID \
    --http-method POST \
    --authorization-type NONE
```

## Testing with cURL

### 1. Register Device

```bash
curl -X POST https://your-api-gateway-url/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "applicationId": "5fbb5562-77cc-44b6-907b-9506725360f2",
    "androidDeviceId": "00e04c7c7b1268d4",
    "appVersion": "1.0",
    "deviceModel": "BasicAppEmulator",
    "osVersion": "11"
  }'
```

### 2. Save Context

```bash
curl -X POST https://your-api-gateway-url/context/save \
  -H "Content-Type: application/json" \
  -H "X-Application-Id: 5fbb5562-77cc-44b6-907b-9506725360f2" \
  -H "X-User-Id: user_12345" \
  -d '{
    "lastLocation": {
      "latitude": 37.7749,
      "longitude": -122.4194,
      "timestamp": 1730736000000
    },
    "preferences": {
      "theme": "dark",
      "notifications": true,
      "radius": 1000
    }
  }'
```

### 3. Buzz In

```bash
curl -X POST https://your-api-gateway-url/buzzin \
  -H "Content-Type: application/json" \
  -H "X-Application-Id: 5fbb5562-77cc-44b6-907b-9506725360f2" \
  -H "X-Session-Id: 765e289e-0a01-4903-a45e-41525e56f95b" \
  -d '{
    "placeId": "place_001",
    "placeName": "Blue Bottle Coffee",
    "location": {
      "latitude": 37.7752,
      "longitude": -122.4192,
      "timestamp": 1730736000000
    },
    "duration": 60,
    "visibility": "public"
  }'
```

## Query Examples

### 1. Get Device by Application ID

```javascript
const result = await dynamodb.get({
    TableName: 'BuzzInDevices',
    Key: {
        applicationId: '5fbb5562-77cc-44b6-907b-9506725360f2'
    }
}).promise();

console.log('Device:', result.Item);
```

### 2. Get All Devices for a User

```javascript
const result = await dynamodb.query({
    TableName: 'BuzzInDevices',
    IndexName: 'UserIdIndex',
    KeyConditionExpression: 'userId = :userId',
    ExpressionAttributeValues: {
        ':userId': 'user_12345'
    }
}).promise();

console.log('User devices:', result.Items);
```

### 3. Get Active Buzz-Ins for an Application

```javascript
const result = await dynamodb.query({
    TableName: 'BuzzInLocations',
    IndexName: 'ApplicationIdIndex',
    KeyConditionExpression: 'applicationId = :appId',
    FilterExpression: 'expiresAt > :now',
    ExpressionAttributeValues: {
        ':appId': '5fbb5562-77cc-44b6-907b-9506725360f2',
        ':now': Date.now()
    }
}).promise();

console.log('Active buzz-ins:', result.Items);
```

## Monitoring and Logging

### CloudWatch Logs Insights Queries

#### 1. Track Device Registrations

```
fields @timestamp, applicationId, androidDeviceId, appVersion
| filter @message like /Device registered/
| sort @timestamp desc
| limit 100
```

#### 2. Track Context Updates

```
fields @timestamp, applicationId, userId
| filter @message like /Context saved/
| stats count() by applicationId
| sort count desc
```

#### 3. Track Buzz-Ins

```
fields @timestamp, buzzInId, applicationId, placeName
| filter @message like /Buzz-in created/
| sort @timestamp desc
| limit 100
```

## Security Best Practices

1. **Never use Application ID for authentication** - Always verify user tokens
2. **Implement rate limiting** - Prevent abuse per Application ID
3. **Validate all inputs** - Check Application ID format (UUID)
4. **Log suspicious activity** - Track unusual patterns
5. **Use HTTPS only** - Never send IDs over unencrypted connections
6. **Implement CORS properly** - Restrict origins in production

## Next Steps

1. Deploy Lambda functions to AWS
2. Configure API Gateway endpoints
3. Test with your current Application ID: `5fbb5562-77cc-44b6-907b-9506725360f2`
4. Update Android app with your API Gateway URL
5. Implement user authentication (AWS Cognito)
6. Add geospatial queries for nearby buzz-ins (DynamoDB Streams + Lambda)
7. Set up CloudWatch alarms for monitoring

## Support

For questions or issues:
- Check CloudWatch Logs for Lambda errors
- Verify DynamoDB table structure
- Test endpoints with cURL before integrating with app
- Review [DEVICE_ID_SYSTEM.md](DEVICE_ID_SYSTEM.md) for client-side documentation

