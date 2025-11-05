import { defineFunction } from '@aws-amplify/backend';

export const getActiveUsersAtLocation = defineFunction({
  name: 'getActiveUsersAtLocation',
  entry: './handler.ts'
});
