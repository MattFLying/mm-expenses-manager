db.createUser(
    {
        user: "root",
        pwd: "gP_h3cS!Lqu-4",
        roles: [
            {
                role: "readWrite",
                db: "expenses-manager-finance"
            }
        ]
    }
)