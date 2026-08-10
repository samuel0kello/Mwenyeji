const {GoogleAuth} = require('google-auth-library');
const fetch = require('node-fetch');
(async()=>{
  try{
    const keyFile = './serviceAccountKey.json';
    const project = 'mwenyeji-d7ad3';
    const auth = new GoogleAuth({keyFilename: keyFile, scopes:['https://www.googleapis.com/auth/datastore']});
    const client = await auth.getClient();
    const tok = await client.getAccessToken();
    console.log('got_token:', !!(tok && (tok.token || tok)));
    const token = tok.token || tok;
    const url = `https://firestore.googleapis.com/v1/projects/${project}/databases/(default)/documents?pageSize=1`;
    const res = await fetch(url, {headers:{Authorization:`Bearer ${token}`}});
    console.log('rest_status:', res.status);
    const text = await res.text();
    console.log('rest_body_preview:', text.slice(0,500));
  }catch(e){
    console.error('error', e && e.message ? e.message : e);
    if(e && e.response && e.response.data) console.error('resp data', e.response.data);
  }
})();