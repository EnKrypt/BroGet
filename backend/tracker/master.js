var application_root = __dirname;
var http = require("http");
var express = require("express");
var path = require("path");
var bodyParser = require("body-parser");
var methodOverride = require("method-override");
var collection = "list";
var db = require("mongojs")("broget",[collection]);
var ObjectId = require("mongojs").ObjectId;

var app = express();

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

app.post('/init', function (req, res) {
    req.body.uidlist=JSON.parse(req.body.uidlist);
    req.body.pidlist=JSON.parse(req.body.pidlist);
	db[collection].save({link: req.body.link, uidlist: req.body.uidlist, pidlist: req.body.pidlist}, function(err,saved){
		if (err||!saved){
			res.send('{"response": "error"}');
		}
		else{
			res.send('{"response": "success"}');
		}
		res.end();
	});
});

app.post('/refresh', function (req, res) {
    console.log(req.body.pid);
	db[collection].find({pidlist: req.body.pid}, function(err,users){
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
				row.pidlist.splice(row.pidlist.indexOf(req.body.pid),1);
				db[collection].update({_id: row._id}, {$set: {pidlist: row.pidlist}},function(err,updated){
					if (err||!updated){
						res.send('{"response": "error"}');
						res.end();
						return;
					}
					else{
						counter--;
					}
					if (counter==0){
						res.send('{"response": "success", "back": '+JSON.stringify(users)+'}');
						res.end();
						return;
					}
				});
			});
		}
	});
});

app.post('/pop', function (req, res) {
	db[collection].find({_id: ObjectId(req.body._id)}, function(err,users){
		if (err||!users){
			res.send('{"response": "error"}');
			res.end();
		}
		else if (users.length!=1){ console.log(users);
			res.send('{"response": "error"}');
			res.end();
		}
		else{
			users[0].uidlist.splice(users[0].uidlist.indexOf(req.body.uid),1);
			db[collection].update({_id: users[0]._id}, {$set: {uidlist: users[0].uidlist}},function(err,updated){
				if (err||!updated){
					res.send('{"response": "error"}');
					res.end();
					return;
				}
				else{
					res.send('{"response": "success"}');
					res.end();
					return;
				}
			});
		}
	});
});

app.listen(8081);
