const https = require('https');
const API_URL = 'https://yutl6o6xvfhq3eeij2j3teg7aq.appsync-api.us-east-1.amazonaws.com/graphql';
const API_KEY = 'da2-v4hc5ft3pnapbgtnxaacc2hnhu';

// IDs to keep
const keepIds = [
  '26d085ae-5f8b-4de4-bea7-cb1459f5c0b4', // Jessica Williams
  '0f428bde-e950-4c2e-92c4-dc6e4155d4d2', // Alex Chen
  'cf6a3073-3728-4d54-a961-a60ebbd4781d'  // Sarah Martinez
];

async function deleteUsers() {
  // Get all users
  const query = 'query { listUsers { items { id name } } }';
  const postData = JSON.stringify({ query });
  const url = new URL(API_URL);

  const options = {
    hostname: url.hostname,
    path: url.pathname,
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': API_KEY,
      'Content-Length': Buffer.byteLength(postData)
    }
  };

  const users = await new Promise((resolve, reject) => {
    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        const result = JSON.parse(data);
        resolve(result.data.listUsers.items);
      });
    });
    req.on('error', reject);
    req.write(postData);
    req.end();
  });

  console.log(`Total users: ${users.length}`);

  // Filter users to delete (not in keepIds)
  const usersToDelete = users.filter(u => !keepIds.includes(u.id));

  console.log(`\nKeeping: 3 users`);
  console.log(`  - Jessica Williams (26d085ae-5f8b-4de4-bea7-cb1459f5c0b4)`);
  console.log(`  - Alex Chen (0f428bde-e950-4c2e-92c4-dc6e4155d4d2)`);
  console.log(`  - Sarah Martinez (cf6a3073-3728-4d54-a961-a60ebbd4781d)`);
  console.log(`\nDeleting: ${usersToDelete.length} users\n`);

  // Delete each user
  let deleted = 0;
  for (const user of usersToDelete) {
    const mutation = `mutation { deleteUser(input: {id: "${user.id}"}) { id } }`;
    const mutationData = JSON.stringify({ query: mutation });

    const mutationOptions = {
      hostname: url.hostname,
      path: url.pathname,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': API_KEY,
        'Content-Length': Buffer.byteLength(mutationData)
      }
    };

    await new Promise((resolve) => {
      const req = https.request(mutationOptions, (res) => {
        let data = '';
        res.on('data', (chunk) => data += chunk);
        res.on('end', () => {
          deleted++;
          const idDisplay = user.id.length > 30 ? user.id.substring(0, 30) + '...' : user.id;
          console.log(`✓ Deleted ${deleted}/${usersToDelete.length}: ${user.name} (${idDisplay})`);
          resolve();
        });
      });
      req.on('error', () => resolve());
      req.write(mutationData);
      req.end();
    });
  }

  console.log(`\n✅ Cleanup complete! Deleted ${deleted} users.`);
  console.log(`\n📊 Remaining users: 3`);
}

deleteUsers();
