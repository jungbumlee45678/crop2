const express = require('express');
const bodyParser = require('body-parser');
const mariadb = require('mariadb');
const nodemailer = require('nodemailer');
const crypto = require('crypto');
const multer = require('multer');
const fs = require('fs');
const path = require('path');

const app = express();
const port = 3000;

const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 3010 });

const server = "13.124.55.241"
const co = "192.168.0.21"
const co8 = "172.31.58.80"

const address = co

const ip = "http://"+address+":3000/"

const pool = mariadb.createPool({
  host: 'localhost',      // MariaDB 호스트 주소 입력 (e.g., localhost)
  user: 'root',  // MariaDB 사용자명 입력
  password: 'multi',  // MariaDB 비밀번호 입력
  database: 'project',  // 사용할 데이터베이스 이름 입력
  charset: 'utf8mb4',
  connectionLimit: 30,
});

// MariaDB 연결 풀에서 커넥션을 가져오는 함수
async function getConnection() {
  try {
    return await pool.getConnection();
  } catch (err) {
    console.error('MariaDB 연결 실패:', err);
    throw err;
  }
}

function generateRandomKey(length) {
  const characters = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let key = '';

  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * characters.length);
    key += characters.charAt(randomIndex);
  }

  return key;
}

// body-parser를 사용하여 요청의 본문을 파싱합니다.
app.use(bodyParser.json());

wss.on('connection', (ws) => {
  // 클라이언트로부터 메시지를 받았을 때 호출되는 콜백 함수
  ws.on('message', async(message) => {
	const jsonObject = JSON.parse(message.toString());

	try{
		const conn = await  getConnection();
		const rows = await  conn.query("select conid from participants where userid=?",[jsonObject.userid]);
		conn.release();
		
        //rows.push({ protectid:"0", alarm: "소식없음", lv: 0 , timestamp:"null", video:"0"});
		// 클라이언트로부터 받은 메시지를 그대로 다시 전송합니다.
		
		ws.send(JSON.stringify(rows));
	}catch (err) {
		console.log("실패");
		console.error(err);
	}
	
  });

  // 클라이언트와의 연결이 종료되었을 때 호출되는 콜백 함수
  ws.on('close', () => {
    //console.log('클라이언트와 연결이 종료되었습니다.');
  });

  // 연결이 실패했을 때 호출되는 콜백 함수
  ws.on('error', (error) => {
    console.error('에러 발생:', error);
  });
});

app.post('/email', async (req, res) => {
	const { userid, userpw } = req.body;
	
	const conn = await getConnection();
		
	const query = 'SELECT * FROM member WHERE userid = ?';
		
	const results = await conn.query(query, [userid]);
	
	res.status(200).send(results[0].email);
})

app.post('/emailsend', async (req, res) => {
	const { userid, userpw } = req.body;
	const email = userpw;
	
	const conn = await getConnection();
	const query = 'update member set email=? where userid=?';
	await conn.query(query, [email,userid]);
	
	const transporter = nodemailer.createTransport({
	  service: 'gmail',
	  auth: {
		user: 'uset0209@gmail.com',
		pass: 'lfft ibai oloq jevu' // Gmail에서 생성한 앱 비밀번호
	  }
	});
	
	const code = generateRandomKey(20);
	
	const insertQuery = 'insert into authcode values (?,?)';
	const data = await conn.query("select * from authcode where userid=?", [userid]);
	
	if(data.length > 0){
		await conn.query("update authcode set code=? where userid=?", [code, userid]);
	}else{
		await conn.query(insertQuery, [userid, code]);
	}
	
	const mailOptions = {
	  from: 'uset0209@gmail.com',
	  to: email,
	  subject: 'UT 이메일 인증',
	  text: ip+'email/'+userid+'/'+code
	};
	
	transporter.sendMail(mailOptions, (error, info) => {
	  if (error) {
		console.error(error);
	  } else {
		res.status(200).send("0");
	  }
	});
})

app.post('/emailch', async (req, res) => {
	const { userid, userpw } = req.body;
	
	const folderPath = 'video/'+userid;
	
	const conn = await getConnection();
	const auth = await conn.query("select auth from member where userid=?",[userid]);

	const query = 'SELECT * FROM member WHERE userid = ?';
	const results = await conn.query(query, [userid]);

	if(auth[0].auth==0){
		res.status(200).send("0")
	}else{
		fs.mkdir(folderPath, (err) => {
			if (err) {
				console.error('userid => not create folder');
			} else {
				
			}
		});

		res.status(200).send(results[0].username)
	}	
	conn.release();
})

// signup 엔드포인트 핸들러
app.post('/signup', async (req, res) => {
	const { userid, userpw, username, email, address } = req.body;

	const conn = await getConnection();
		
	const query1 = 'SELECT * FROM member WHERE userid = ?';
	const query2 = 'SELECT * FROM member WHERE username = ?';
		
	const results1 = await conn.query(query1, [userid]);
	const results2 = await conn.query(query2, [username]);
	
	if (results1.length > 0) {
		res.status(200).send("userid");
	}else if(results2.length > 0){
		res.status(200).send("username");
	}else{
		const insertQuery = 'INSERT INTO member (userid, userpw, username,email, address)VALUES (?, ?, ?, ?, ?)';
		await conn.query(insertQuery, [userid, userpw, username, email, address]);
		
		res.status(200).send("successfully");
	}
	conn.release();
});

app.post('/login', async (req, res) => {
	const { userid, userpw } = req.body;
	  
	const conn = await getConnection();
	const login = await conn.query('SELECT username,email FROM member where userid=? and userpw=?', [userid, userpw]);
	const auth = await conn.query("select auth from member where userid=?",[userid]);

	if(login.length == 0){
		res.status(200).send("0")
	}else{
		if(auth[0].auth==0){
			res.status(200).send("auth")
		}else{
			res.status(200).send(login[0].username)
		}	
	}
	conn.release();
});

app.post('/findid', async (req, res) => {
  const { username, email } = req.body;
  
  const conn = await getConnection();
  const login = await conn.query('SELECT userid FROM member where username=? and email=?', [username, email]);
  conn.release();
  
  if(login.length == 0){
		res.status(200).send("0")
  }else{
	  console.log(login[0].userid)
		res.status(200).send(login[0].userid)
  }
});

app.post('/findpw', async (req, res) => {
  const { userid, username, email } = req.body;
  
  const conn = await getConnection();
  const login = await conn.query('SELECT userpw FROM member where userid=? and username=? and email=?',
  [userid, username, email]);
  conn.release();
  
  if(login.length == 0){
		res.status(200).send("0")
  }else{
		const transporter = nodemailer.createTransport({
		  service: 'gmail',
		  auth: {
			user: 'uset0209@gmail.com',
			pass: 'lfft ibai oloq jevu' // Gmail에서 생성한 앱 비밀번호
		  }
		});
	
		const userpw = generateRandomKey(8);
		
		const insertQuery = 'update member set userpw=? where userid=?';
		await conn.query(insertQuery, [userpw, userid]);
		
		const mailOptions = {
		  from: 'uset0209@gmail.com',
		  to: email,
		  subject: 'UT 임시 비밀번호',
		  text: 'userpw : '+userpw
		};

		transporter.sendMail(mailOptions, (error, info) => {
		  if (error) {
			console.error(error);
		  } else {
			
		  }
		});
		res.status(200).send("1")
  }
});

app.post('/info', async (req, res) => {
  const { userid } = req.body;
  
  const conn = await getConnection();
  const info = await conn.query('SELECT userid,username,address,email FROM member where userid=?',[userid]);
  conn.release();
  
  res.status(200).json(info)
});

app.post('/change_username', async (req, res) => {
	const { userid, userpw, change } = req.body;
	  
	const conn = await getConnection();
	const data = await conn.query('SELECT * FROM member where userid=? and userpw=?', [userid, userpw]);
	const name = await conn.query("select * from member where username=?",[change]);

	if(data.length > 0){
		if(name.length == 0){
			await conn.query("update member set username=? where userid=?",[change,userid]);
			res.status(200).send("success");
		}else{
			res.status(200).send("exis");
		}
	}else{
		res.status(200).send("nothing");
	}
	conn.release();
});

app.post('/change_email', async (req, res) => {
	const { userid, userpw, change } = req.body;
	  
	const conn = await getConnection();
	const data = await conn.query('SELECT * FROM member where userid=? and userpw=?', [userid, userpw]);

	if(data.length > 0){
		await conn.query("update member set email=?, auth=0 where userid=?",[change,userid]);
		res.status(200).send("success")
	}else{
		res.status(200).send("nothing")	
	}
	conn.release();
});

app.post('/change_address', async (req, res) => {
	const { userid, userpw, change } = req.body;
	  
	const conn = await getConnection();
	const data = await conn.query('SELECT * FROM member where userid=? and userpw=?', [userid, userpw]);

	if(data.length > 0){
		await conn.query("update member set address=? where userid=?",[change,userid]);
		res.status(200).send("success")
	}else{
		res.status(200).send("nothing")	
	}
	conn.release();
});

app.post('/change_pw', async (req, res) => {
	const { userid, userpw, change} = req.body;
	  
	const conn = await getConnection();
	const data = await conn.query('SELECT * FROM member where userid=? and userpw=?', [userid, userpw]);

	if(data.length > 0){
		await conn.query("update member set userpw=? where userid=?",[change,userid]);
		res.status(200).send("success")
	}else{
		res.status(200).send("nothing")	
	}
	conn.release();
});

const storage = multer.diskStorage({
  destination: (req, file, cb) => { 
	const uploadPath = "video/"+req.body.userid.replace(/"/g, '');
	cb(null, uploadPath); // uploads 폴더에 저장
  },
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  },
});

const upload = multer({ storage: storage });

app.post('/video', upload.single('video'), async(req, res) => {
	const title  = req.body.title.replace(/"/g, '');
	const username = req.body.username.replace(/"/g, '');
	const userid = req.body.userid.replace(/"/g, '');

    // 업로드된 파일 정보는 req.file에서 사용 가능
	const conn = await getConnection();
	const num_0 = await conn.query("select max(num) as num from board");
	await conn.query('insert into board (num, video, title, username, userid) values (?,?,?,?,?)',
	[(num_0[0].num+1), req.file.originalname, title, username, userid]);
	
	const keyword = await conn.query("select * from alarmkeyword where userid != ?",[userid]);
	
	let sql = "select num from board where title like ? and category not in ("
	sql += "select interest from interestcategory where userid=?) and num=?"
	
	for(let i=0;i<keyword.length;i++){
		const keywordText = "%"+keyword[i].keyword+"%";
		const num = await conn.query(sql,[keywordText, keyword[i].userid, num_0[0].num+1]);
		
		if(num.length>0){
			await conn.query("insert into alert values (?,?)",[keyword[i].userid, num[0].num]);
		}
	}
	
    res.status(200).send("1");
});

app.post('/board', async (req, res) => {
	const conn = await getConnection();
	const data = await conn.query('select num,title,username from board');
	conn.release();

	res.status(200).json({items:data});
});

app.post('/carto', async (req, res) => {
	const {userid} = req.body;
	
	const sql = "select interest from interestcategory where userid=?"
	
	const conn = await getConnection();
	const data1 = await conn.query('select * from category order by classification');
	const data2 = await conn.query('select * from category where category in ('+sql+') order by classification',[userid]);
	conn.release();

	const combinedData = {
	  data1: data1,  // 또는 data1[0] 등의 형태로 데이터 추출
	  data2: data2,  // 또는 data2[0] 등의 형태로 데이터 추출
	};

	res.status(200).json(combinedData);
});

app.post('/carto_input', async (req, res) => {
	const {category, userid} = req.body;
	
	const conn = await getConnection();
	await conn.query('delete from interestcategory where userid=?', [userid]);

	for (let i = 0; i < category.length; i++) {
	    await conn.query('insert into interestcategory Values (?,?)', [userid,category[i]]);
	}

	conn.release();

	res.status(200).send("1");
});

app.post('/keyword', async (req, res) => {
	const {userid} = req.body;

	const conn = await getConnection();
	const data = await conn.query('SELECT keyword FROM alarmkeyword where userid=?',
	[userid]);

	res.status(200).json(data);
});

app.post('/keyword_input', async (req, res) => {
	const {userid, keyword, input} = req.body;

	let query = ""

	if(input==1){
		query = 'insert into alarmkeyword (keyword, userid) values (? , ?)'
	}else{
		query = 'delete from alarmkeyword where keyword=? and userid=?'
	}

	const conn = await getConnection();
	await conn.query(query, [keyword,userid]);
	conn.release();

	res.status(200).send("1");
});

app.post('/alert', async (req, res) => {
	const {userid} = req.body;

	const conn = await getConnection();
	const data = await conn.query('select num,title,username from board where num in (select boardid from alert where userid=?)',[userid]);
	conn.release();

	res.status(200).json({items:data});
});

app.post('/chat', async (req, res) => {
	const {boardid, userid} = req.body;

	const conn = await getConnection();
	
	let query = "select conid from participants where conid in (";
	query += "select num from conversation where boardid=?) and userid=?"
	const data = await conn.query(query,[boardid,userid]);
	if(data.length>0){
		conn.release();
		data[0].conid
		res.status(200).json({num:data[0].conid});
	}else{
		const data1 = await conn.query("select max(num) as max from conversation");
		let num = 1;
		if(data1!=null){
			num += data1[0].max;
		}
		
		const data2 = await conn.query("select userid from board where num = ?",[boardid]);
		
		await conn.query("insert into conversation values (?,?)",[num,boardid]);
		await conn.query("insert into participants values (?,?)",[num,userid]);
		await conn.query("insert into participants values (?,?)",[num,data2[0].userid]);
		conn.release();
		
		res.status(200).json({num:num});
	}
});

app.get('/',(req, res) => {
	res.status(200).send("<h1>안녕</h1>");
});

app.get('/email/:param1/:param2', async(req, res) => {
	const userid = req.params.param1;
	const code = req.params.param2;
  
	const conn = await getConnection();
	const login = await conn.query('SELECT * FROM authcode where userid=? and code=?',
	[userid, code]);
	
	if(login.length>0){
		conn.query("update member set auth=1 where userid=?",[userid])
		res.status(200).send("<h1>인증이 성공했습니다.</h1>")
	}else{
		res.status(200).send("<h1>인증이 실패하였습니다.</h1>");
	}
	conn.release();	
});

app.get('/video/:num', async(req, res) => {
  try {
    const { num } = req.params; //req.body를 이용?
	
	const conn = await getConnection();
	const data = await conn.query('SELECT video, userid FROM board where num=?',
	[parseInt(num)]);
	conn.release();
	
    const videoPath = path.join('video', data[0].userid, data[0].video);

    const stat = fs.statSync(videoPath);
    const fileSize = stat.size;

    const head = {
      'Content-Length': fileSize,
      'Content-Type': 'video/mp4',
    };

    res.writeHead(200, head);
    fs.createReadStream(videoPath).pipe(res);
  } catch (err) {
    console.error(err);
    res.status(500).send('Server Error');
  }
});

app.get('/image', async(req, res) => {
  // 이미지 파일의 경로
  const imagePath = path.join(__dirname,"img",'test.png');

  // 이미지 파일을 클라이언트로 전송
  res.sendFile(imagePath);
});

// 서버 시작
app.listen(port, () => {
  console.log(`Server is running`);
});