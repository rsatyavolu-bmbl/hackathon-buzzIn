const https = require('https');
const API_URL = 'https://yutl6o6xvfhq3eeij2j3teg7aq.appsync-api.us-east-1.amazonaws.com/graphql';
const API_KEY = 'da2-v4hc5ft3pnapbgtnxaacc2hnhu';

// IDs to delete
const checkInIds = ["069b3400-6d88-4155-9647-262ee663281a","b6188544-9a06-4d21-85f6-06a3b5b45120","6f11a190-3de0-4e03-b007-21466cf633e4","2727a450-827b-430c-9a4d-cfd88c1f5840","17e021cc-ce2a-4f67-b8f7-b66dad0724dd","31a3850a-b58c-4f3b-9323-a61ff636590b","86d0dbe3-d47a-4722-9d6c-82ce2826257c","4455cb68-7122-4531-b929-bf7c24d908e0","4b754d1a-c99e-48e3-9b5a-b7dad9160dbf","f3a04d08-4a30-41ed-a53f-5bec7d3ea067","0c3f72e0-8723-47ab-827f-79b7a2f1bb9f","431d381e-3ca8-49d8-9d8d-e546256fbbc6","7e9fdb0b-86c2-4266-9cef-69ef88b98c54","dc41e985-f6da-4a3b-bfce-e1539acaf3c6","7961e710-4e07-4352-9fc7-8e3c8205e65a","0e661e50-06c2-4ee8-b35f-58972fe109a2","3786ab6e-cfc5-4eb1-ae9a-72b48772dd64","db99a99a-59e8-488a-9a1f-e5de67280aab","3c301574-9062-4385-80cc-a3921d6c39ca","583794e9-d62f-4286-9cb8-0320b57363af","9d28e4b1-66d3-4b70-940c-cae50438ff71","89b623f7-d5c1-442a-8839-6ecd0e31c951","f2c832d5-9258-4143-8a06-eb6545440d25","28af4252-b8a0-4301-81bd-76a4b72676ee","3b9e3678-ddd8-4007-a34d-003c6a5aa107","55925ed7-4b17-4e04-9c83-43cf1c506d70","b6deecdb-dbea-4acd-8be6-fa6288fe7d58","51a39a3a-bc41-490b-b7ce-fec27c85fea5","a642bfc6-4414-4a61-b08c-e95e99034af1","24f899f5-4935-4bf7-8135-81e9b335691d","a79a362d-002f-4371-981e-99035ca482c7","5c94dd18-815b-4136-abef-28b7ee06e48f","2297ad5c-4bfb-4faf-8a23-fb6d6d766d44","58fa3013-cf98-47ac-a72b-9fa989889eec","5c0979e7-402b-42b2-85fc-db680640c448","a99d4d5e-f3a8-4f07-b8bc-2172fe7e8c62","0d601a35-d385-430a-b5d3-bcaeb95c4b9a","0cc1c4c4-fe14-43d0-8f77-1fd3ff32f78e","87f47551-aed1-4816-8fb7-6690c1ceeecb","d411990e-e60f-4c6e-866c-35241fd7c4c0","a1aae965-8c9e-4a69-8002-e0b79eed1c0c","51db95d2-a288-4658-bf20-66dc077cb9ed","9fb34a57-40fe-4c45-9288-1fd7cee20d26","a94f9607-9455-471d-a725-deec9e72f1bc","7ca5603d-4662-452e-90e1-ed2a741a0405","0fbd1fc1-9ca5-4ce8-b59e-be4c87d663a2","ee2fef04-dc73-4cb4-9971-5438a088bf26","16f6f2aa-2ee5-4adf-88d6-fce63a2546ec","2c441354-6377-4713-aef9-cf8d99b0cadc","c60ff013-afeb-4da5-818f-1382ef8025ec","bf6358ab-c722-404b-ba6f-3bf2e6c4cbab","e5701ad1-8211-4e9c-9e55-e82ab02b4331","e269338a-31f7-4078-b324-deb5c7d6e07a","0a055acc-c62b-4242-9593-9c4dbc2a85bb","0a941be1-0574-4fa9-bb22-1de810d050c5","2db7ffd8-31cf-4042-810c-82b6a3a97f8e","c367e04f-5724-44f3-86e5-60f0b4be09e6","5d3385f4-b82c-443c-b368-6dbe8f2c6620","90e3a5e9-380c-4788-9beb-4651499d27fa","5575fcb2-be92-4750-bc89-cb5d23109167","b7e1a6c4-37e4-4bd9-8d66-2c591561c8c9","1f3b7981-31ff-4f25-8295-4e5f15436bb1","84ce5d2f-631e-4144-ac20-88f0265baa43","6de2773d-78d0-4aee-a583-c7c764d048fb","1e3a61d0-8c76-44bf-bb7f-77130bd1f18f","6d337567-fc8c-42ad-bbfc-7e545fd01b56","df44b84d-4d9b-499d-8af2-244d4662ed8b","c5b7a388-292c-4a33-adf6-05da2b3a4d06","2250df7c-dbd9-4f26-a91c-db6f15a67896","9f028dfa-ca6d-4df2-b243-bc3351b5c1ec","971b7188-2d68-48a8-bfb5-323ad15323d4","a2a2ad75-b717-4425-803f-592a6487d220","a8f05e8e-fab5-401a-b7b4-84d2acfbc7c8","61d40628-ed3e-4841-9c7a-1b47094f0de9","c5cfed2d-d67e-475f-bd0f-df113727e903","10f6c134-0639-4e07-9a96-ef7f44a8ea3d","ae9fb2f9-b62e-4528-aa3d-af5debf21ed7","3345442c-d9cb-4514-ad56-cd6d802624dd","126d12b9-f1e3-449f-920c-950e7eb1a9e6","c673f063-3471-4823-9790-f64c083b84ec","3d42b0aa-36be-4e82-a0e4-71dcc5028326","bf0238a9-0b11-4567-88ae-782e7b4f898e","45989bb6-4317-4f65-a0aa-c803e2cc2d89","916186b1-8452-4315-9160-8da88546282b","6ab55ba1-e57f-4cf1-a2dd-cf229bd4a7fa","290ceefd-f3a1-41d3-a001-ef3bebdf4bed","793c0b72-291b-4d1b-b507-456e6fe207d9","eeeb78cb-9f44-48dc-b75c-9576775fab32","8fdd891d-ab94-4b76-9c74-7600ea7ee06a","a52f821d-ac17-40aa-8422-3d1b3b8cfa9d","ed4ba05a-a1fa-470e-94c8-eed789906fab","7faefd3c-c670-44e1-9514-4b859e2dd775","f9ea55cf-a150-46ae-9c76-a812cf9ac9c0","2abaf69c-8b08-414f-b9ce-756f93872659","2751efe4-b8aa-4704-829c-f7d68b2a6e83","ec50e556-cc09-4d7a-8298-897f4e644a18","f09e17df-ea3b-48c0-97f0-3596d8bb2a0e","b9fb203f-5f6d-400d-ac17-c158e3ecfce7","fdc76d63-efbe-42a5-b1af-93723d1a4af9","4982bea5-4189-4565-ab8f-9d9ca81a5f71"];

const swipeIds = ["a2e34ba2-7b3a-4150-8d61-e3ca62e43e45","899097e3-6429-49cd-85ed-3a868741adbc","51f170a1-9998-46a7-b708-e753a2320970","001ba928-ab6d-43c6-91f0-f3e3c747eb59","df09cb59-883e-4984-82ed-cb4706166b21","1bd4d9ef-a74c-43e6-a299-be6ea8b69215","06d49990-cda4-4234-baaa-6b3d4196fbbc","210db6c2-7fc6-4cc1-84bc-dd8e8ab6a31d","e59b67cc-b482-4278-891c-dc500e049d7d","reverse-swipe-1","8e1f2e54-ecaa-484e-8e1f-032141add9a0","8f80f5f5-89e0-46ab-b62a-eceed3e96a92","3a9603ee-607f-41b9-9ac0-40993fb963ee","2ab1c5ca-6b58-4707-ba87-4e47445d0a9d","9563d785-cb05-42e8-b48a-2ce4abd5b24a","3c190d69-a826-49dc-bdf9-c9ed7fe55971","28f35982-ea7c-4faf-bbc3-d386b56b4c6e","168b1351-0bbe-4ce7-8212-2be918ac6825","2d9ca648-6d3a-426a-9085-f83f17af2e08","d13c7232-bc70-4f12-8507-4a2cd86717fd","0f02acb8-7c12-407f-a342-bedadc95f658","8960a3b7-6f6b-40d3-9895-cd7f3b9483ee","5b86a7de-47f8-4e51-890f-0825ce2a1024","fce506d7-f0bf-4951-af6d-7ea5f5d8ced7","57ebc94b-7c0d-4825-9a6d-60c08422ae9b","dfa0b40d-5baa-4062-852e-dab37a98ccbb","c627fc4f-eb33-481c-a52e-f17eaa4199bb","f8cd9651-e4c9-450d-bf99-d8546b118397","06cca83b-9185-4d82-b8f4-bb779870832c","1fbff850-a25b-4914-9077-6a6582cb382b","21b5bf71-d7f2-4bf9-8cb1-e0a64a768c3a","0bc98240-1f5c-4cf6-9272-41f81cb79487","c2973bfb-78e6-4d47-a75d-3fe4bb7d2894","f3a6e5a0-6743-4e4d-a59e-ade859cfe3af","960d0ed8-c3ee-423a-873d-f1dbb9f6f099","4503339b-40c9-4036-a0dd-b50af299d792","d68110e3-7493-4de8-8062-f7ddff328403","dad7c91e-7944-4445-bc75-9466d6c86fb2","2f8e59bf-9019-4eda-80e0-6e26081245b5","c67715e7-d00b-4b14-bb9b-d0c73e25e5b1","48207a25-d366-44ea-8c25-4685775707bb","b14dc0b9-c616-496b-a5ff-ffdfdd9bdc23","a2854b05-b72b-4835-a3a0-d23dbb6af339","a06eb5d6-e6cf-4e07-9d38-cd56597719aa","1da99496-17e1-47a2-9b1c-75995c3a912f","c816ef08-9fe6-4383-84b7-dee769083170","738181fb-b883-4334-8cf0-4c9e3fb7c440"];

const matchIds = ["605b1fb2-2af6-45eb-8272-f389f42e0746","fb7c56c1-fc68-4d48-87cb-55282f58a114","2d8e6065-a190-4054-8515-0310701d14a9","2550a9b0-1616-4151-9e96-345197462e47","5a2d2022-8f02-464b-bc87-90a2af359848","test-match-1","950b61a8-c695-47b4-975d-d1489427b54e"];

function deleteItem(type, id) {
  return new Promise((resolve, reject) => {
    const mutation = `
      mutation Delete${type} {
        delete${type}(input: {id: "${id}"}) {
          id
        }
      }
    `;

    const postData = JSON.stringify({ query: mutation });
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

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        resolve(data);
      });
    });

    req.on('error', (err) => reject(err));
    req.write(postData);
    req.end();
  });
}

async function clearAllData() {
  console.log(`Deleting ${checkInIds.length} check-ins...`);
  for (const id of checkInIds) {
    try {
      await deleteItem('CheckIn', id);
      console.log(`Deleted check-in: ${id}`);
    } catch (err) {
      console.error(`Failed to delete check-in ${id}:`, err.message);
    }
  }

  console.log(`\nDeleting ${swipeIds.length} swipes...`);
  for (const id of swipeIds) {
    try {
      await deleteItem('Swipe', id);
      console.log(`Deleted swipe: ${id}`);
    } catch (err) {
      console.error(`Failed to delete swipe ${id}:`, err.message);
    }
  }

  console.log(`\nDeleting ${matchIds.length} matches...`);
  for (const id of matchIds) {
    try {
      await deleteItem('Match', id);
      console.log(`Deleted match: ${id}`);
    } catch (err) {
      console.error(`Failed to delete match ${id}:`, err.message);
    }
  }

  console.log('\n✅ All data cleared!');
}

clearAllData();
