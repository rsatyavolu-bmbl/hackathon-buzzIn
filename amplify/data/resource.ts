import { type ClientSchema, a, defineData } from '@aws-amplify/backend';
import { getActiveUsersAtLocation } from '../functions/getActiveUsersAtLocation/resource';
import { listNearbyLocations } from '../functions/listNearbyLocations/resource';
import { performSwipe } from '../functions/performSwipe/resource';

// BuzzIn Schema - Location-based dating app
const schema = a.schema({
  User: a
    .model({
      name: a.string().required(),
      age: a.integer().required(),
      bio: a.string(),
      photoRes: a.string().required(),
      interests: a.string().array(),
    })
    .authorization((allow) => [allow.publicApiKey()]),

  Location: a
    .model({
      name: a.string().required(),
      address: a.string().required(),
      latitude: a.float().required(),
      longitude: a.float().required(),
      type: a.enum(['COFFEE', 'RESTAURANT', 'BAR', 'PARK', 'GYM', 'OTHER']),
    })
    .authorization((allow) => [allow.publicApiKey()]),

  CheckIn: a
    .model({
      userId: a.id().required(),
      locationId: a.id().required(),
      checkInTime: a.datetime().required(),
      checkOutTime: a.datetime(),
      isActive: a.boolean().required(),
      latitude: a.float(),
      longitude: a.float(),
    })
    .authorization((allow) => [allow.publicApiKey()]),

  Swipe: a
    .model({
      userId: a.id().required(),
      targetUserId: a.id().required(),
      locationId: a.id().required(),
      action: a.enum(['LIKE', 'IGNORE']),
      timestamp: a.datetime().required(),
    })
    .authorization((allow) => [allow.publicApiKey()]),

  Match: a
    .model({
      user1Id: a.id().required(),
      user2Id: a.id().required(),
      locationId: a.id().required(),
      matchTime: a.datetime().required(),
      messageSent: a.boolean().required(),
    })
    .authorization((allow) => [allow.publicApiKey()]),

  Message: a
    .model({
      matchId: a.id().required(),
      senderId: a.id().required(),
      text: a.string().required(),
      photoRes: a.string(),
      timestamp: a.datetime().required(),
      isRead: a.boolean().required(),
    })
    .authorization((allow) => [allow.publicApiKey()]),

  // Custom Queries
  getActiveUsersAtLocation: a
    .query()
    .arguments({
      locationId: a.id().required(),
    })
    .returns(a.json())
    .authorization((allow) => [allow.publicApiKey()])
    .handler(a.handler.function(getActiveUsersAtLocation)),

  listNearbyLocations: a
    .query()
    .arguments({
      latitude: a.float().required(),
      longitude: a.float().required(),
      radiusKm: a.float(),
    })
    .returns(a.json())
    .authorization((allow) => [allow.publicApiKey()])
    .handler(a.handler.function(listNearbyLocations)),

  // Custom Mutations
  performSwipe: a
    .mutation()
    .arguments({
      userId: a.id().required(),
      targetUserId: a.id().required(),
      locationId: a.id().required(),
      action: a.string().required(),
    })
    .returns(a.json())
    .authorization((allow) => [allow.publicApiKey()])
    .handler(a.handler.function(performSwipe)),
});

export type Schema = ClientSchema<typeof schema>;

export const data = defineData({
  schema,
  authorizationModes: {
    defaultAuthorizationMode: 'apiKey',
    apiKeyAuthorizationMode: {
      expiresInDays: 365,
    },
  },
});
