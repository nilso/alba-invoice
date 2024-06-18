# alba-invoice
This is a simple invoice generator for Alba. Current version(2024-06-18) reads matching client invoices from PE and creates a matching supplier invoice as a PDF that will have to be uploaded manually to PE.

## Configuration
The program expects a config.properties file in the resources folder.
BASE_URL=SETME
API_KEY=SETME
CLIENT_ID=SETME
DAYS_BACK=SETME

## Running the app
If you have a development environment you can simply run Main.java

If you are a developer and want to create an executable for someone with a mac you do the following
mvn package
./create-dmg.sh
send the created dmg file to the user

No support for other OSs at the moment.

## Good to know
This app doesn't have any error handling. If something goes wrong you will have to check the logs in alba.log.
This app isn't written to keep any state. Every time it is started it gets all information from PE as if it has never been run before.
When filtering invoices we require Agentarvode and KlientID to be set. If they are not set the invoice will not be created.
The KlientId is a number expected to be copy pasted from an excel created with SupplierExcelMain.java. The intended work process is that anyone who is creating client invoices also has that excel so they can se the correct KlientId.
Long term we want PE to create a drop down for us but that isn't available at the moment.
