import { defineFunction } from '@aws-amplify/backend';

export const performSwipe = defineFunction({
  name: 'performSwipe',
  entry: './handler.ts'
});
