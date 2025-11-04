/**
 * List locations within a specified radius of given coordinates
 */

const AWS = require('aws-sdk');
const docClient = new AWS.DynamoDB.DocumentClient();

/**
 * Calculate distance between two coordinates using Haversine formula
 * Returns distance in kilometers
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // Earth's radius in km
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);

  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function toRad(degrees) {
  return degrees * (Math.PI / 180);
}

exports.handler = async (event) => {
  console.log('Event:', JSON.stringify(event, null, 2));

  const { latitude, longitude, radiusKm } = event.arguments;

  if (!latitude || !longitude || !radiusKm) {
    return {
      statusCode: 400,
      body: JSON.stringify({ error: 'latitude, longitude, and radiusKm are required' })
    };
  }

  try {
    // Scan all locations (for MVP - in production, use geohashing or DynamoDB geo library)
    const scanParams = {
      TableName: process.env.LOCATION_TABLE
    };

    const locationsResult = await docClient.scan(scanParams).promise();
    console.log('Total locations:', locationsResult.Items.length);

    // Filter locations within radius
    const nearbyLocations = locationsResult.Items.filter(location => {
      const distance = calculateDistance(
        latitude,
        longitude,
        location.latitude,
        location.longitude
      );
      return distance <= radiusKm;
    });

    // Sort by distance (closest first)
    nearbyLocations.sort((a, b) => {
      const distA = calculateDistance(latitude, longitude, a.latitude, a.longitude);
      const distB = calculateDistance(latitude, longitude, b.latitude, b.longitude);
      return distA - distB;
    });

    console.log('Nearby locations found:', nearbyLocations.length);
    return nearbyLocations;

  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
