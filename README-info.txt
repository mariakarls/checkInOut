To add a new geofence area to monitorate:
1. go to Constants file
2. add a new Landmark with correct coordinates


Basic Database description:
table Users( User Text (PK), Password Text) 
table Children (IdChild Int (PK), ChildName Text)
table ChildUser (IdChild Int, User Text, PrimaryKey(IdChild,User))
table CheckInOut(Date Text, IdChild Int, CheckInTime Text, CheckOutTime Text, PersonCheckOut Text, CheckOut Int, PrimaryKey(Date, IdChild))

To add new data in the database (I mean data for the following tables: Users, Children, ChildUser, CheckInOut):
1. go to DatabaseManager
2. modify the "populateDatabase" method

Information about the implementation of the notification service:
1. notification can be send only if there's a user stored in the file SharedPreferences, this means only after that the user clicked on the remember me option during the login [The button notify works only in this case]
2. notification type (check in or check out) depends on the status of the child (for now it's implemented with only a child for user) for the user stored in the SharedPreferences
3. The button Notify allows to test the notification service (that should be invoked by the geofence intent), it works only when the child of the stored user has to be check in and/or check out
4. Check in notification (after click on Check in) open the ChildActivity
5. Check out notification (after click on Check out) directly invoke the check out method

General information
1. Save function is invoked (after click on Save button) only if the Pick-up Person is not empty
2. you can choose a Check Out time only in the future
3. you can check in only after the insert of check out time and pick up person
4. In the login view if there is a "user" stored in the SharedPreferences the fields are precompiled
