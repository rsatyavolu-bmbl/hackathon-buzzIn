import { defineFunction } from '@aws-amplify/backend';

export const listNearbyLocations = defineFunction({
  name: 'listNearbyLocations',
  entry: './handler.ts'
});
