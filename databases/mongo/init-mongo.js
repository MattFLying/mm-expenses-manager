use admin
db.auth("admin","gP_h3cS!Lqu-4")
db.createUser({user: "em_root", pwd: "gP_h3cS!Lqu-4", roles: [{role: "readWrite", db: "em_finance"}, {role: "dbAdmin", db: "em_finance"}]})