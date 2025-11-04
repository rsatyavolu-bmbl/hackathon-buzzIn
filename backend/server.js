const express = require('express');
const { graphqlHTTP } = require('express-graphql');
const { buildSchema } = require('graphql');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');

// In-memory data stores
const users = new Map();
const locations = new Map();
const checkIns = new Map();
const swipes = new Map();
const matches = new Map();
const messages = new Map();

// GraphQL Schema
const schema = buildSchema(`
  type User {
    id: ID!
    name: String!
    age: Int!
    bio: String
    photoRes: String!
    interests: [String]
  }

  type Location {
    id: ID!
    name: String!
    address: String!
    latitude: Float!
    longitude: Float!
    type: LocationType!
  }

  enum LocationType {
    COFFEE
    RESTAURANT
    BAR
    PARK
    GYM
    OTHER
  }

  type CheckIn {
    id: ID!
    userId: ID!
    locationId: ID!
    checkInTime: String!
    checkOutTime: String
    isActive: Boolean!
    latitude: Float
    longitude: Float
  }

  type Swipe {
    id: ID!
    userId: ID!
    targetUserId: ID!
    locationId: ID!
    action: SwipeAction!
    timestamp: String!
  }

  enum SwipeAction {
    LIKE
    IGNORE
  }

  type Match {
    id: ID!
    user1: User
    user2: User
    location: LocationInfo
    matchTime: String!
    messageSent: Boolean!
  }

  type LocationInfo {
    id: ID
    name: String!
  }

  type Message {
    id: ID!
    matchId: ID!
    senderId: ID!
    text: String!
    photoRes: String
    timestamp: String!
    isRead: Boolean!
  }

  type SwipeResult {
    matched: Boolean!
    match: Match
  }

  input CreateUserInput {
    name: String!
    age: Int!
    photoRes: String!
    bio: String
  }

  input CreateCheckInInput {
    userId: ID!
    locationId: ID!
    checkInTime: String!
    isActive: Boolean!
    latitude: Float
    longitude: Float
  }

  input UpdateCheckInInput {
    id: ID!
    checkOutTime: String
    isActive: Boolean
  }

  input CreateMessageInput {
    matchId: ID!
    senderId: ID!
    text: String!
    photoRes: String
    timestamp: String!
    isRead: Boolean!
  }

  type Query {
    getUser(id: ID!): User
    listLocations: [Location!]!
    listNearbyLocations(latitude: Float!, longitude: Float!, radiusKm: Float!): [Location!]!
    getActiveUsersAtLocation(locationId: ID!): [User!]!
    listMatches(userId: ID!): [Match!]!
    listMessages(matchId: ID!): [Message!]!
  }

  type Mutation {
    createUser(input: CreateUserInput!): User!
    createCheckIn(input: CreateCheckInInput!): CheckIn!
    updateCheckIn(input: UpdateCheckInInput!): CheckIn!
    performSwipe(userId: ID!, targetUserId: ID!, locationId: ID!, action: SwipeAction!): SwipeResult!
    createMessage(input: CreateMessageInput!): Message!
  }
`);

// Helper: Calculate distance between two coordinates (Haversine formula)
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // Earth's radius in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

// Resolvers
const root = {
  // Queries
  getUser: ({ id }) => {
    return users.get(id);
  },

  listLocations: () => {
    return Array.from(locations.values());
  },

  listNearbyLocations: ({ latitude, longitude, radiusKm }) => {
    return Array.from(locations.values()).filter(loc => {
      const distance = calculateDistance(latitude, longitude, loc.latitude, loc.longitude);
      return distance <= radiusKm;
    });
  },

  getActiveUsersAtLocation: ({ locationId }) => {
    const activeCheckIns = Array.from(checkIns.values()).filter(
      checkIn => checkIn.locationId === locationId && checkIn.isActive
    );
    return activeCheckIns.map(checkIn => users.get(checkIn.userId)).filter(Boolean);
  },

  listMatches: ({ userId }) => {
    return Array.from(matches.values())
      .filter(match => match.user1Id === userId || match.user2Id === userId)
      .map(match => ({
        ...match,
        user1: users.get(match.user1Id),
        user2: users.get(match.user2Id),
        location: match.location
      }));
  },

  listMessages: ({ matchId }) => {
    return Array.from(messages.values()).filter(msg => msg.matchId === matchId);
  },

  // Mutations
  createUser: ({ input }) => {
    const user = {
      id: uuidv4(),
      ...input,
      interests: input.interests || []
    };
    users.set(user.id, user);
    console.log(`Created user: ${user.name} (${user.id})`);
    return user;
  },

  createCheckIn: ({ input }) => {
    const checkIn = {
      id: uuidv4(),
      ...input
    };
    checkIns.set(checkIn.id, checkIn);
    console.log(`User ${checkIn.userId} checked in to ${checkIn.locationId}`);
    return checkIn;
  },

  updateCheckIn: ({ input }) => {
    const checkIn = checkIns.get(input.id);
    if (!checkIn) throw new Error('CheckIn not found');

    const updated = { ...checkIn, ...input };
    checkIns.set(input.id, updated);
    console.log(`Updated check-in ${input.id}`);
    return updated;
  },

  performSwipe: ({ userId, targetUserId, locationId, action }) => {
    // Create swipe record
    const swipe = {
      id: uuidv4(),
      userId,
      targetUserId,
      locationId,
      action,
      timestamp: new Date().toISOString()
    };
    swipes.set(swipe.id, swipe);
    console.log(`User ${userId} ${action} user ${targetUserId} at ${locationId}`);

    // Check for match (mutual like)
    if (action === 'LIKE') {
      const reverseSwipe = Array.from(swipes.values()).find(
        s => s.userId === targetUserId &&
             s.targetUserId === userId &&
             s.locationId === locationId &&
             s.action === 'LIKE'
      );

      if (reverseSwipe) {
        // Create match
        const location = locations.get(locationId);
        const match = {
          id: uuidv4(),
          user1Id: userId,
          user2Id: targetUserId,
          locationId,
          location: location ? { id: location.id, name: location.name } : { name: 'Unknown' },
          matchTime: new Date().toISOString(),
          messageSent: false
        };
        matches.set(match.id, match);
        console.log(`🎉 MATCH! ${userId} and ${targetUserId}`);

        return {
          matched: true,
          match: {
            ...match,
            user1: users.get(userId),
            user2: users.get(targetUserId)
          }
        };
      }
    }

    return { matched: false, match: null };
  },

  createMessage: ({ input }) => {
    const message = {
      id: uuidv4(),
      ...input
    };
    messages.set(message.id, message);

    // Update match to mark message as sent
    const match = Array.from(matches.values()).find(m => m.id === input.matchId);
    if (match) {
      match.messageSent = true;
      matches.set(match.id, match);
    }

    console.log(`Message sent in match ${input.matchId}`);
    return message;
  }
};

// Seed some test data
function seedData() {
  // Create test locations in San Francisco
  const testLocations = [
    { id: 'loc1', name: 'Blue Bottle Coffee', address: '66 Mint St', latitude: 37.7825, longitude: -122.4078, type: 'COFFEE' },
    { id: 'loc2', name: 'Ritual Coffee Roasters', address: '1026 Valencia St', latitude: 37.7564, longitude: -122.4211, type: 'COFFEE' },
    { id: 'loc3', name: 'Dolores Park', address: '19th St & Dolores St', latitude: 37.7596, longitude: -122.4269, type: 'PARK' },
    { id: 'loc4', name: 'The Alembic', address: '1725 Haight St', latitude: 37.7699, longitude: -122.4469, type: 'BAR' },
    { id: 'loc5', name: 'Tartine Bakery', address: '600 Guerrero St', latitude: 37.7614, longitude: -122.4240, type: 'RESTAURANT' }
  ];

  testLocations.forEach(loc => locations.set(loc.id, loc));
  console.log(`Seeded ${testLocations.length} locations`);

  // Create test users
  const testUsers = [
    { id: 'user1', name: 'Alex', age: 28, photoRes: 'avatar_1', bio: 'Coffee enthusiast and hiking lover' },
    { id: 'user2', name: 'Sam', age: 25, photoRes: 'avatar_2', bio: 'Foodie exploring SF' },
    { id: 'user3', name: 'Jordan', age: 30, photoRes: 'avatar_3', bio: 'Tech professional, dog parent' },
    { id: 'user4', name: 'Taylor', age: 27, photoRes: 'avatar_4', bio: 'Artist and coffee addict' }
  ];

  testUsers.forEach(user => users.set(user.id, { ...user, interests: [] }));
  console.log(`Seeded ${testUsers.length} users`);
}

// Create Express app
const app = express();
app.use(cors());
app.use(express.json());

// GraphQL endpoint
app.use('/graphql', graphqlHTTP({
  schema: schema,
  rootValue: root,
  graphiql: true, // Enable GraphiQL interface
}));

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    users: users.size,
    locations: locations.size,
    checkIns: checkIns.size,
    matches: matches.size
  });
});

// Start server
const PORT = process.env.PORT || 4000;
app.listen(PORT, '0.0.0.0', () => {
  console.log('=================================');
  console.log('🚀 BuzzIn Backend Server Started');
  console.log('=================================');
  console.log(`GraphQL API: http://localhost:${PORT}/graphql`);
  console.log(`GraphiQL UI: http://localhost:${PORT}/graphql`);
  console.log(`Health Check: http://localhost:${PORT}/health`);
  console.log('=================================');

  // Seed test data
  seedData();
});
