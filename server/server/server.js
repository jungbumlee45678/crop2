const express = require('express');
const bodyParser = require('body-parser');
const mariadb = require('mariadb');
const nodemailer = require('nodemailer');
const crypto = require('crypto');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const app = express();
const port = 3000;

const ffmpeg = require('fluent-ffmpeg');
const ffmpegPath = require('@ffmpeg-installer/ffmpeg').path;
ffmpeg.setFfmpegPath(ffmpegPath);

const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 3010 });

const server = "52.79.233.116"
const co = "192.168.0.21"
const co8 = "172.31.58.80"

const address = server

const ip = "http://"+address+":3000/"

const pool = mariadb.createPool({
  host: 'localhost',      // MariaDB 호스트 주소 입력 (e.g., localhost)
  user: 'root',  // MariaDB 사용자명 입력
  password: 'multi',  // MariaDB 비밀번호 입력
  database: 'project',  // 사용할 데이터베이스 이름 입력
  charset: 'utf8mb4',
  connectionLimit: 100,
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
	
	const userid = jsonObject.userid
	const num = jsonObject.num
	const conid = jsonObject.conid

	try{
		const conn = await  getConnection();

		const rows2 = await conn.query(
		`
		select board.state as state from message 
		JOIN conversation ON conversation.num = message.conid
		JOIN board ON conversation.boardid = board.num
		where conid=? and message.num=?
		`
		,[conid,num]);		

		if(rows2.length>0 && rows2[0].state==1){
			ws.send(JSON.stringify(rows2));
		} else{
			const rows = await  conn.query(
			`
			select * from message
			where userid!=? and num>? and conid=?
			`
			,[userid, num, conid]);
		
			const count = await conn.query("SELECT COUNT(num) as count FROM message WHERE conid=?",[conid]);
			await conn.query("update participants set num=? where userid=? and conid=?",[count[0].count,userid,conid]);

			ws.send(JSON.stringify(rows));
		}

		conn.release();
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
	
	const conn = await getConnection();
	const auth = await conn.query("select auth from member where userid=?",[userid]);

	const query = 'SELECT * FROM member WHERE userid = ?';
	const results = await conn.query(query, [userid]);

	if(auth[0].auth==0){
		res.status(200).send("0")
	}else{
		res.status(200).send(results[0].username)
	}	
	conn.release();
})

// signup 엔드포인트 핸들러
app.post('/signup', async (req, res) => {
	const { userid, userpw, username, email, address, latitude, longitude } = req.body;

	const conn = await getConnection();
		
	const query1 = 'SELECT * FROM member WHERE userid = ?';
	const query2 = 'SELECT * FROM member WHERE username = ?';
		
	const results1 = await conn.query(query1, [userid]);
	const results2 = await conn.query(query2, [username]);
	
	const folder_video = 'video/'+userid;
	const folder_profile = 'profile/'+userid;
	const folder_img = 'img/'+userid;
	
	if (results1.length > 0) {
		res.status(200).send("userid");
	}else if(results2.length > 0){
		res.status(200).send("username");
	}else{
		const insertQuery = 'INSERT INTO member (userid, userpw, username,email, address, latitude, longitude)VALUES (?, ?, ?, ?, ?, ?, ?)';
		await conn.query(insertQuery, [userid, userpw, username, email, address, latitude, longitude]);
		
		fs.mkdir(folder_video, (err) => {
			if (err) {
				console.error(userid+' => not create folder_video');
			} else {
				
			}
		});
		
		fs.mkdir(folder_profile, (err) => {
			if (err) {
				console.error(userid+' => not create folder_profile');
			} else {
				
			}
		});
		
		fs.mkdir(folder_img, (err) => {
			if (err) {
				console.error(userid+' => not create folder_img');
			} else {
				
			}
		});
		
		res.status(200).send("successfully");
	}
	conn.release();
});

app.post('/login', async (req, res) => {
	const { userid, userpw } = req.body;
	  
	const conn = await getConnection();
	const login = await conn.query('SELECT userpw, username,email FROM member where userid=? and userpw=?', [userid,userpw]);
	const auth = await conn.query("select auth from member where userid=?",[userid]);

	if(login.length == 0){
		res.status(200).send("0")
	}else{
		if(userpw != login[0].userpw){
			res.status(200).send("0")
		} else if(auth[0].auth==0){
			res.status(200).send("auth")
		} else{
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
	const { userid, userpw, change, latitude, longitude } = req.body;
	  
	const conn = await getConnection();
	const data = await conn.query('SELECT * FROM member where userid=? and userpw=?', [userid, userpw]);

	if(data.length > 0){
		await conn.query("update member set address=?,latitude=?,longitude=? where userid=?",
		[change,latitude, longitude,userid]);
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
	const uploadPath = "video/"+req.body.userid.replace(/"/g, '');//"를 찾고 ''공백화
	cb(null, uploadPath); // uploads 폴더에 저장
  },
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  },
});

const upload = multer({ storage: storage });

app.post('/video', upload.single('video'), async(req, res) => {
	const num  = req.body.num.replace(/"/g, '');
	const title  = req.body.title.replace(/"/g, '');
	const userid = req.body.userid.replace(/"/g, '');
	const credit = req.body.credit.replace(/"/g, '');
	const content = req.body.content.replace(/"/g, '');
	const category = req.body.category.replace(/"/g, '');

	const currentTime = new Date().toISOString().slice(0, 19).replace('T', ' ');

    // 업로드된 파일 정보는 req.file에서 사용 가능
	const conn = await getConnection();
	let re_num = 0;
	
	if(num != 0){
		await conn.query("update board set path=?, title=?, credit=?, content=?, category=?, time=? where num=?",
		[req.file.originalname.replace(/\.[^/.]+$/, ''), title, credit, content, category, currentTime, num]);
		await conn.query("delete from alert where boardid=?",[num]);
		re_num = num;
	} else{
		const num_0 = await conn.query("select max(num) as num from board");
		await conn.query('insert into board (num, path, title, userid, credit, content, category, time) values (?,?,?,?,?,?,?,?)',
		[(num_0[0].num+1), req.file.originalname.replace(/\.[^/.]+$/, ''), title, userid, credit, content, category, currentTime]);
		re_num = num_0[0].num+1;
	}
	
	const keyword = await conn.query("select * from alarmkeyword where userid != ?",[userid]);
		
	let sql = "select num from board where title like ? and category not in ("
	sql += "select interest from interestcategory where userid=?) and num=?"
	
	for(let i=0;i<keyword.length;i++){
		const keywordText = "%"+keyword[i].keyword+"%";
		const num = await conn.query(sql,[keywordText, keyword[i].userid, re_num]);
		
		if(num.length>0){
			await conn.query("insert into alert values (?,?)",[keyword[i].userid, re_num]);
		}
	}
	
	conn.release();

	const videoPath = 'video/'+userid+'/'+req.file.originalname;

	ffmpeg(videoPath)
    .screenshots({
      timestamps: ['0'], // 5초에 대한 스크린샷을 찍습니다.
      folder: "img/"+userid,
      filename: req.file.originalname.replace(/\.[^/.]+$/, '.png'), // 추출된 이미지의 파일 이름
    })
    .on('end', () => {
	  res.status(200).json(re_num);
    })
    .on('error', (err) => {
      console.error('이미지 추출 중 오류 발생:', err);
	  res.status(500).json("img_err");
    });
});

app.post('/re_board', async(req,res)=>{
	const {num,title,credit,content,category} = req.body;
	
	const currentTime = new Date().toISOString().slice(0, 19).replace('T', ' ');
	
	const conn = await getConnection();
	await conn.query("update board set title=?,credit=?,content=?,category=?,time=? where num=?",
	[title,credit,content,category,currentTime,num]);
	
	res.status(200).send("1");
});

// Haversine 공식을 사용하여 두 지점 간의 거리를 계산하는 함수
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // 지구의 반지름 (단위: km)

  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  const distance = R * c; // 최종 거리 (단위: km)
  return distance;
}

// 각도를 라디안으로 변환하는 함수
function toRadians(degrees) {
  return degrees * (Math.PI / 180);
}

app.post('/board', async (req, res) => {
	const {search, userid, sale, mode} = req.body;
	let sql = "";
	const conn = await getConnection();

	if(mode=="search"){
		sql = 'SELECT board.*, member.address, member.latitude, member.longitude FROM board LEFT JOIN member ON board.userid = member.userid where board.userid != ? and state=0 and life=1 order by board.num'
		let data = await conn.query(sql,[userid]);
		const roat = await conn.query("select latitude, longitude from member where userid=?",[userid]);
		conn.release();

		for(let i=0;i<data.length;i++){
			const memberLatitude = data[i].latitude;
			const memberLongitude = data[i].longitude;
			
			const distance = calculateDistance(roat[0].latitude, roat[0].longitude, memberLatitude, memberLongitude);
			
			if (distance > 10) {
			  // 거리가 10km를 초과하는 경우 해당 항목 삭제
			  delete data[i];
			}
		}
		
		data = data.filter(item => item !== null);
		
		res.status(200).json({items:data});
	}else if(mode=="purchase"){
		sql = 'SELECT b.*, m.address FROM purchase as p JOIN board AS b ON b.num = p.boardid JOIN member AS m ON m.userid = b.userid where p.userid = ? order by b.num'
		const data = await conn.query(sql,[userid]);
		conn.release();

		res.status(200).json({items:data});
	}else if(sale==0){
		sql = 'SELECT board.*, member.address, member.latitude, member.longitude FROM board LEFT JOIN member ON board.userid = member.userid where board.userid = ? and state=0 and life=1 order by board.num'
		const data = await conn.query(sql,[userid]);
		conn.release();
		
		res.status(200).json({items:data});
	}else if(sale==1){
		sql = 'SELECT board.*, member.address FROM board LEFT JOIN member ON board.userid = member.userid where board.userid = ? and state=1 and life=1 order by board.num'
		const data = await conn.query(sql,[userid]);
		conn.release();
		
		res.status(200).json({items:data});
	}else{
		const {classification, category, distance, min_credit, max_credit} = req.body;

		sql = 'SELECT board.*, member.address, member.latitude, member.longitude FROM board LEFT JOIN member ON board.userid = member.userid where board.userid != ? and state=0 and life=1'
		
		if(search!=null && search!=""){
			sql += " and title like '%"+search+"%'"
		}	
		
		if(category!=null && category.length!=0){
		    sql += " and board.category in ("
		    for(let i=0;i<category.length;i++){
		        sql += "'"+category[i]+"'"
		        if(i != category.length-1){
		            sql += ","
		        }
		    }
		    sql += ")"
		}
		else if(classification!=null && classification!=""){
			sql += " and board.category in (select category from category where classification='"+classification+"')"
		}	
		
		if(min_credit!=-1){
			sql += " and board.credit >= "+min_credit
		}
		if(max_credit!=-1){
			sql += " and board.credit <= "+max_credit
		}

		sql += " order by board.num"
		
		let data = await conn.query(sql,[userid]);
		const roat = await conn.query("select latitude, longitude from member where userid=?",[userid]);
		
		const search_data = await conn.query("select search from search where userid=? and search=?",[userid,search]);
		const currentTime = new Date().toISOString().slice(0, 19).replace('T', ' ');
		
		if(search_data.length > 0){
			await conn.query("update search set time=? where userid=? and search=?",[currentTime, userid, search]);
		}else if(search!=null && search.trim() != ""){
			await conn.query("insert into search (userid, search, time) values (?,?,?)",[userid, search, currentTime]);
		}
		conn.release();

		for(let i=0;i<data.length;i++){
			const memberLatitude = data[i].latitude;
			const memberLongitude = data[i].longitude;
			
			const move_distance = calculateDistance(roat[0].latitude, roat[0].longitude, memberLatitude, memberLongitude);
			
			if (move_distance > distance) {
			  delete data[i];
			}
		}

		data = data.filter(item => item !== null);
		
		res.status(200).json({items:data});
	}
});

app.post('/search', async (req, res) => {
	const {userid} = req.body;

	const conn = await getConnection();
	sql = "select search from search where userid=? order by time desc"
	const data = await conn.query(sql,[userid]);
	
	conn.release();
		
	res.status(200).json(data);
});

app.post('/board_info', async (req, res) => {
	const {num, userid} = req.body;

	const conn = await getConnection();
	const board = await conn.query("select userid, state from board where num=?", [num]);

	if(board[0].userid != userid && board[0].state == 0){
		await conn.query('UPDATE board SET views = views + 1 WHERE num = ?',[parseInt(req.body.num)]);
	}

	const data = await conn.query('SELECT board.*, member.address, member.username, member.profile FROM board LEFT JOIN member ON board.userid = member.userid where board.num=?',
		[num]);
	conn.release();

	res.status(200).json(data[0]);
});

app.post('/board_delete', async (req, res) => {
	const conn = await getConnection();
	const data = await conn.query('update board set life = 0 where board.num=?', [req.body.num]);
	conn.release();
	
	res.status(200).json(data[0]);
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

app.post('/class_ca', async (req, res) => {

	const conn = await getConnection();
	const data = await conn.query('select * from classification order by classification');
	conn.release();

	res.status(200).json(data);
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
	let data = await conn.query('SELECT board.*, member.address, member.latitude, member.longitude from board join member on member.userid=board.userid where num in (select boardid from alert where userid=?) AND state=0 AND life=1',[userid]);
	const roat = await conn.query("select latitude, longitude from member where userid=?",[userid]);
	conn.release();

	for(let i=0;i<data.length;i++){
			const memberLatitude = data[i].latitude;
			const memberLongitude = data[i].longitude;
			
			const move_distance = calculateDistance(roat[0].latitude, roat[0].longitude, memberLatitude, memberLongitude);
			
			if (move_distance > 10) {
			  delete data[i];
			}
		}

	data = data.filter(item => item !== null);
		
	res.status(200).json({items:data});
});

app.post('/chat', async (req, res) => {
	const {boardid, userid} = req.body;

	const conn = await getConnection();
	
	let query = "select conid from participants where conid in (";
	query += "select num from conversation where boardid=?) and userid=?"
	const data = await conn.query(query,[boardid,userid]);
	if(data.length>0){
		res.status(200).json({num:data[0].conid});
	}else{
		const data1 = await conn.query("select max(num) as max from conversation");
		let num = 1;
		if(data1!=null){
			num += data1[0].max;
		}
		
		const data2 = await conn.query("select userid from board where num = ?",[boardid]);
		
		await conn.query("insert into conversation values (?,?)",[num,boardid]);
		await conn.query("insert into participants (conid,userid) values (?,?)",[num,userid]);
		await conn.query("insert into participants (conid,userid)  values (?,?)",[num,data2[0].userid]);
		
		res.status(200).json({num:num});
	}
	conn.release();
});

app.post('/chat_board', async (req, res) => {
	const {userid} = req.body;
	
	const conn = await getConnection();
	const data = await conn.query(
	`
	SELECT p.conid, b.title, p2.num AS min_num, COUNT(DISTINCT m2.num) as max_num, c.boardid, me.username as di_username, b.userid as bo_userid, b.state, m.content
	FROM participants p
	JOIN conversation c ON p.conid = c.num
	JOIN member me ON me.userid = p.userid
	JOIN board b ON b.num = c.boardid
	JOIN message m ON m.conid = p.conid AND m.num = 
	(SELECT MAX(num) FROM message WHERE conid = p.conid)
	JOIN message m2 ON m2.conid = p.conid
	join participants p2 on p2.conid=p.conid and p2.userid=?
	WHERE p.conid IN 
	(SELECT conid FROM participants WHERE userid =?)
	AND p.userid != ?
	group by p.conid
	order by m.num desc;
	`, [userid,userid,userid]);
	
	function convertBigIntsToNumbers(data) {
		const recursiveMapper = (key, value) => {
		if (typeof value === 'bigint') {
			return Number(value);
		} else if (typeof value === 'object') {
			for (const k in value) {
				value[k] = recursiveMapper(k, value[k]);
			}
		}
		return value;
		};

		return data.map(row => {
			for (const key in row) {
				row[key] = recursiveMapper(key, row[key]);
			}
			return row;
		});
	}

	// 전체 결과를 BigInt에서 Number로 변환
	const convertedData = convertBigIntsToNumbers(data);
	
	res.status(200).json(convertedData);
});

const storage2 = multer.diskStorage({
	destination: (req, file, cb) => {
		const path = "profile/"+req.body.userid.replace(/"/g, '');
		cb(null, path); // Save uploaded files to the 'uploads' directory
  },
	filename: (req, file, cb) =>  {
		cb(null, file.originalname); // Rename files with a timestamp
  }
});

const upload2 = multer({ storage: storage2 });

app.post('/profile', upload2.single('image'), async(req, res) => {
  if (req.file) {
    // Image uploaded successfully
	const userid = req.body.userid.replace(/"/g, '');
	
	const conn = await getConnection();
	await conn.query('update member set profile=? where userid=?',[req.file.originalname,userid]);
	
    res.status(200).send("1");
  } else {
    // Image upload failed
    res.status(400).json("Image upload failed");
  }
});

app.post('/message_load', async(req, res) => {
  const {conid, userid} = req.body;
  
  const conn = await getConnection();
  const data = await conn.query('select num ,userid, content, time from message where conid=? order by num',[conid]);
  
  const count = await conn.query("SELECT COUNT(num) as count FROM message WHERE conid=?",[conid]);
  await conn.query("update participants set num=? where userid=? and conid=?",[count[0].count,userid,conid]);

  res.status(200).json(data)
});

app.post('/message', async(req, res) => {
  const {conid,userid,content} = req.body;
  
  const time = new Date().toISOString().slice(0, 19).replace('T', ' ');

  const conn = await getConnection();
  await conn.query('insert into message (conid,userid,content,time) values (?,?,?,?)',
  [conid,userid,content,time]);
  
  res.status(200).send("0");
});

app.post('/state', async(req, res) => {
  const { num, chat_num, userid } = req.body;
  
  const conn = await getConnection();
  await conn.query('update board set state=1 where num=?',[num]);
  const data = await conn.query('SELECT userid FROM participants WHERE conid=? AND userid != ?', [chat_num, userid]);

  await conn.query("insert into purchase values (?,?,?)",[chat_num, num, data[0].userid]);

  res.status(200).send("0");
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
	const data = await conn.query('SELECT path, userid FROM board where num=?', [parseInt(num)]);
	
	conn.release();
	
    const videoPath = path.join('video', data[0].userid, data[0].path+".mp4");

    const stat = fs.statSync(videoPath);
    const fileSize = stat.size;

    const head = {
      'Content-Length': fileSize,
      'Content-Type': 'video/mp4',
    };

    res.writeHead(200, head);
	
	// Create a readable stream from the video file
    const videoStream = fs.createReadStream(videoPath);
	
    // Pipe the video stream through the gzip stream and then to the response
    videoStream.pipe(res);
  } catch (err) {
    console.error(err);
    res.status(500).send('Server Error');
  }
});

app.get('/image/:num', async(req, res) => {
	const { num } = req.params;
	
	const conn = await getConnection();
	const data = await conn.query('SELECT path, userid FROM board where num=?', [num]);
	conn.release();
	
	// 이미지 파일의 경로
	const imagePath = path.join(__dirname,"img",data[0].userid,data[0].path+".png");

	// 이미지 파일을 클라이언트로 전송
	res.sendFile(imagePath);
});

app.get('/image/server/:image', async(req, res) => {
	const { image } = req.params;
  
	// 이미지 파일의 경로
	const imagePath = path.join(__dirname,"server",image+".png");

	// 이미지 파일을 클라이언트로 전송
	res.sendFile(imagePath);
});

app.get('/profile/:userid', async(req, res) => {
  // 이미지 파일의 경로
	const { userid } = req.params;
	const { timestamp } = req.query;
  
	const conn = await getConnection();
	const data = await conn.query('SELECT profile FROM member where userid=?', [userid]);
	conn.release();
  
	if (data.length > 0) {
		if (data[0]?.profile) { // 프로필이 존재할 때만 이미지를 전송
			const imagePath = path.join(__dirname, "profile", userid, data[0].profile);
			res.sendFile(imagePath);
		} else {
			res.status(404).send("프로필 이미지가 없습니다.");
		}
	} else {
		res.status(404).send("회원을 찾을 수 없습니다.");
	}
});

// 서버 시작
app.listen(port, () => {
  console.log(`Server is running`);
});