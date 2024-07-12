# alba-invoice
AlbaInvoice is a simple GUI that allows for creating of invoices before sending them to PE.

## Configuration
The program expects a config.properties file in the resources folder.
BASE_URL=SETME
API_KEY=SETME
CLIENT_ID=SETME
DAYS_BACK=SETME

## Running the app
If you have a development environment you can simply run FXMain.java

If you are a developer and want to create an executable for someone with a mac you do the following
mvn package
./create-dmg.sh
send the created dmg file to the user

No support for other OSs at the moment. But there is no reason it shouldn't work more than the fact that I haven't tried it.

## Good to know
1. The program usually takes around a minute to start up, this is because it has to get all the data from PE. There is a ticket with PE to make some of our searches faster so we don't have to fetch it on startup but can do it as needed instead, but currently this gives us the best user experience. 
2. If something goes wrong you will have to check the logs in alba.log.
3. This app isn't written to keep any state. Every time it is started it gets all information from PE as if it has never been run before.
4. Agentarvode and KlientId are the required fields, if they are not set in PE they will have to be set manually in AlbaInvoice.
5. The KlientId is a number expected to be copy pasted from an excel created with from within AlbaInvoice.
