/// <reference types="node" />
import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import { DynamoDBDocumentClient, ScanCommand } from '@aws-sdk/lib-dynamodb';
const client = new DynamoDBClient({});
const docClient = DynamoDBDocumentClient.from(client);
// Haversine formula to calculate distance between two coordinates
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Radius of the Earth in kilometers
    const dLat = toRadians(lat2 - lat1);
    const dLon = toRadians(lon2 - lon1);
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRadians(lat1)) *
            Math.cos(toRadians(lat2)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c; // Distance in kilometers
    return distance;
}
function toRadians(degrees) {
    return degrees * (Math.PI / 180);
}
export const handler = async (event) => {
    console.log('Event:', JSON.stringify(event, null, 2));
    const { latitude, longitude, radiusKm = 5 } = event.arguments;
    if (latitude === undefined || longitude === undefined) {
        throw new Error('latitude and longitude are required');
    }
    try {
        const locationTableName = process.env.LOCATION_TABLE_NAME;
        const checkInTableName = process.env.CHECKIN_TABLE_NAME;
        if (!locationTableName) {
            throw new Error('Location table name not configured');
        }
        // Scan all locations (for MVP, optimize with geohashing later)
        const locationsResponse = await docClient.send(new ScanCommand({
            TableName: locationTableName
        }));
        if (!locationsResponse.Items) {
            return [];
        }
        // Filter locations within radius
        const nearbyLocations = locationsResponse.Items
            .map((location) => {
            const distance = calculateDistance(latitude, longitude, location.latitude, location.longitude);
            return { ...location, distance };
        })
            .filter((location) => location.distance <= radiusKm)
            .sort((a, b) => a.distance - b.distance);
        // Count active check-ins for each location
        if (checkInTableName) {
            for (const location of nearbyLocations) {
                try {
                    const checkInsResponse = await docClient.send(new ScanCommand({
                        TableName: checkInTableName,
                        FilterExpression: 'locationId = :locationId AND isActive = :isActive',
                        ExpressionAttributeValues: {
                            ':locationId': location.id,
                            ':isActive': true
                        }
                    }));
                    location.activeUsers = checkInsResponse.Items?.length || 0;
                }
                catch (error) {
                    console.log(`Failed to count check-ins for location ${location.id}:`, error);
                    location.activeUsers = 0;
                }
            }
        }
        else {
            // If no check-in table, set activeUsers to 0
            nearbyLocations.forEach((location) => {
                location.activeUsers = 0;
            });
        }
        return nearbyLocations;
    }
    catch (error) {
        console.error('Error:', error);
        throw new Error(`Failed to list nearby locations: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
};
