var application_root = __dirname;
var http = require("http");
var express = require("express");
var request = require("request");
var path = require("path");
var bodyParser = require("body-parser");
var methodOverride = require("method-override");
var macaddress = require("macaddress");
var collection = "list";
var db = require("mongojs")("broget",[collection]);
var ObjectId = require("mongojs").ObjectId;

var sys = require('sys')
var exec = require('child_process').exec;

var app = express();

var cloud_url = "http://192.168.0.102:8081";
//Change with appropriate url for your config

function puts(error, stdout, stderr) { sys.puts(stdout) }

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));

app.use(methodOverride());
app.use(express.static(path.join(application_root, "public")));
app.use(function(req, res, next){
	res.header("Access-Control-Allow-Origin", "*");
	res.header("Access-Control-Allow-Methods", "GET, POST");
	next();
});

interval=setInterval(function(){
	macaddress.one("wlan0",function(err,mac){
		request({
			url: cloud_url+"/refresh",
			method: 'POST',
			form: {
				pid: mac
			}
		},function(error, response, body){
			if (error||JSON.parse(body).response=="error"){
				console.log("lmao"+error);
			}
			else{
				var downloads=JSON.parse(body).back;
				console.log(downloads);
				downloads.forEach(function(down){
					exec("aria2c --allow-overwrite=true " + decodeURIComponent(down.link), function(){
						db[collection].save(down, function(err,saved){
							if (err||!saved){
								console.log(err);
							}
							else{
								console.log("Downloaded and inserted");
							}
						});
					});
				});
			}
		});
	});
},10000);

app.post('/download', function (req, res) {
	db[collection].find({uidlist: req.body.uid}, function(err,users){
		if (err||!users){
			res.send('{"response": "error"}');
			res.end();
		}
		else if (users.length==0){
			res.send('{"response": "success", "back": []}');
			res.end();
		}
		else{
			var counter=0;
			users.forEach(function(row){
				counter++;
				row.uidlist.splice(row.uidlist.indexOf(req.body.uid),1);
				db[collection].update({_id: row._id}, {$set: {uidlist: row.uidlist}},function(err,updated){
					if (err||!updated){
						res.send('{"response": "error"}');
						res.end();
						return;
					}
					else{
						counter--;
						request({
							url: cloud_url+"/pop",
							method: 'POST',
							form: {
								_id: row._id,
								uid: req.body.uid
							}
						},function(error, response, body){
							if (error||JSON.parse(body).response=="error"){
								console.log(error);
							}
						})
					}
					if (counter==0){
						var xim=[];
						users.forEach(function(user){
							xim.push(user.link);
						});
						res.send(JSON.stringify(xim));
						res.end();
						return;
					}
				});
			});
		}
	});
});


app.listen(8081);;